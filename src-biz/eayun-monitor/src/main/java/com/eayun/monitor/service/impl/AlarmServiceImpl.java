package com.eayun.monitor.service.impl;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.RedisNodeIdConstant;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.database.instance.model.CloudRDSInstance;
import com.eayun.monitor.bean.ContactGroupAvailable;
import com.eayun.monitor.bean.MonitorAlarmUtil;
import com.eayun.monitor.bean.MonitorItem;
import com.eayun.monitor.bean.MonitorMngData;
import com.eayun.monitor.bean.MonitorZB;
import com.eayun.monitor.dao.AlarmContactDao;
import com.eayun.monitor.dao.AlarmMessageDao;
import com.eayun.monitor.dao.AlarmObjectDao;
import com.eayun.monitor.dao.AlarmRuleDao;
import com.eayun.monitor.dao.AlarmTriggerDao;
import com.eayun.monitor.dao.ContactDao;
import com.eayun.monitor.dao.ContactGroupDao;
import com.eayun.monitor.model.AlarmContact;
import com.eayun.monitor.model.AlarmMessage;
import com.eayun.monitor.model.AlarmObject;
import com.eayun.monitor.model.AlarmRule;
import com.eayun.monitor.model.AlarmTrigger;
import com.eayun.monitor.model.BaseAlarmContact;
import com.eayun.monitor.model.BaseAlarmMessage;
import com.eayun.monitor.model.BaseAlarmObject;
import com.eayun.monitor.model.BaseAlarmRule;
import com.eayun.monitor.model.BaseAlarmTrigger;
import com.eayun.monitor.model.BaseContact;
import com.eayun.monitor.model.BaseContactGroup;
import com.eayun.monitor.model.Contact;
import com.eayun.monitor.service.AlarmService;
import com.eayun.monitor.service.MonitorAlarmService;
import com.eayun.virtualization.model.CloudLdPool;
import com.eayun.virtualization.model.CloudVm;

@Service
@Transactional
public class AlarmServiceImpl implements AlarmService{
    
    private static final Logger   log = LoggerFactory.getLogger(AlarmServiceImpl.class);
    @Autowired
    private AlarmRuleDao alarmRuleDao;
    @Autowired
    private AlarmObjectDao alarmObjectDao;
    @Autowired
    private AlarmTriggerDao alarmTriggerDao;
    @Autowired
    private AlarmContactDao alarmContactDao;
    @Autowired
    private AlarmMessageDao alarmMessageDao;
    @Autowired
    private ContactDao contactDao;
    @Autowired
    private ContactGroupDao contactGroupDao;
    @Autowired
    private MonitorAlarmService monitorAlarmService;
    @Autowired
    private JedisUtil jedisUtil;
    @Autowired
    private MongoTemplate mongoTemplate;
    
    private static final int PAGE_SIZE = 5;

    /**
     * 分页的方式获取报警规则列表
     * @param cusId
     * @param name
     * @param page
     * @param queryMap
     * @return
     * @throws AppException
     * @see com.eayun.monitor.service.AlarmService#getPagedAlarmRuleList(java.lang.String, java.lang.String, com.eayun.common.dao.support.Page, com.eayun.common.dao.QueryMap)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Page getPagedAlarmRuleList(String cusId, String name, 
    		String monitorItemID, Page page, QueryMap queryMap)throws AppException {
        log.info("获取报警规则列表");
        List<Object> paramList = new ArrayList<Object>();
        StringBuffer sb = new StringBuffer();
        sb.append("select ar_id, ar_name, ar_modifytime, ar_monitoritem from ecsc_alarmrule where ar_cusid=?");
        paramList.add(cusId);
        if(!StringUtil.isEmpty(monitorItemID)){
        	sb.append(" and ar_monitoritem = ?");
        	paramList.add(monitorItemID);
        }
        if(!StringUtil.isEmpty(name)){
        	sb.append(" and ar_name like ?");
        	paramList.add("%"+name+"%");
        }
        sb.append(" order by ar_modifytime desc");
        page = alarmRuleDao.pagedNativeQuery(sb.toString(), queryMap, paramList.toArray());
        List newList = (List) page.getResult();
        for (int i = 0; i < newList.size(); i++) {
            Object[] objs = (Object[]) newList.get(i);
            AlarmRule alarmRule = new AlarmRule();
            String id = String.valueOf(objs[0]);
            alarmRule.setId(id);
            alarmRule.setCusId(cusId);
            alarmRule.setName(String.valueOf(objs[1]));
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timeStr = sdf.format(objs[2]);
            Date modifyTime = null;
            try {
                modifyTime = sdf.parse(timeStr);
            } catch (ParseException e) {
                throw new AppException(e.getMessage());
            }
            alarmRule.setModifyTime(modifyTime);
            alarmRule.setLastModifyTime(timeStr);
            alarmRule.setMonitorItem(String.valueOf(objs[3]));
            alarmRule.setMonitorItemName(getMonitorItemByNodeID(String.valueOf(objs[3])));
            alarmRule.setTriggerCondition(getTriggerConditionString(id));
            alarmRule.setAlarmObjectNumber(getAlarmObjectNumber(id));
            newList.set(i, alarmRule);
        }
        return page;
    }
    /**
     * 查询获取当前用户可见的报警信息列表
     * 当前用户可看到的（区分监控项类型）
     * 数据中心下拉是dcName列表
     * @Author: duanbinbin
     * @param sessionUser
     * @param vmName
     * @param dcName
     * @param alarmType
     * @param monitorType
     * @param processedSign
     * @param page
     * @param queryMap
     * @return
     * @throws AppException
     *<li>Date: 2017年3月2日</li>
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Page getPagedAlarmMsgList(SessionUserInfo sessionUser, String vmName, String dcName, String alarmType, 
    		String monitorType,String processedSign, Page page, QueryMap queryMap)throws AppException {
        log.info("获取当前用户可见的报警信息列表开始");
        String userId = sessionUser.getIsAdmin()?sessionUser.getCusId():sessionUser.getUserId();
        List<Object> paramList = new ArrayList<Object>();
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ")
          .append(" msg.am_id, ")
          .append(" msg.am_vmid, ")
          .append(" CASE WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_VM+"' THEN vm.vm_name ")
          .append(" WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_CLOUDDATA+"' THEN rds.rds_name ")
          .append(" WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDCOMMON+"' OR ")
          .append(" msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDMASTER+"' THEN ld.pool_name ")
          .append(" END AS objName, ")
  		  .append(" CASE WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_VM+"' THEN dc0.dc_name ")
          .append(" WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_CLOUDDATA+"' THEN dc1.dc_name ")
          .append(" WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDCOMMON+"' OR ")
          .append(" msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDMASTER+"' THEN dc2.dc_name ")
          .append(" END AS dcName, ")
          .append(" msg.am_alarmtype, ")
          .append(" msg.am_monitortype, ")
          .append(" msg.am_detail, ")
          .append(" msg.am_time, ")
          .append(" msg.am_isprocessed ")
          .append(" FROM ")
          .append(" ecsc_alarmmessage msg ");
        sb.append(" LEFT JOIN cloud_vm vm ON vm.vm_id = msg.am_vmid AND msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_VM+"' ");
		sb.append(" LEFT JOIN cloud_project prj0 on prj0.prj_id = vm.prj_id AND msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_VM+"' ");
		sb.append(" LEFT JOIN dc_datacenter dc0 on prj0.dc_id = dc0.id ");
		
		sb.append(" LEFT JOIN cloud_rdsinstance rds ON rds.rds_id = msg.am_vmid AND msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_CLOUDDATA+"' ");
    	sb.append(" LEFT JOIN cloud_project prj1 on prj1.prj_id = rds.prj_id AND msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_CLOUDDATA+"' ");
    	sb.append(" LEFT JOIN dc_datacenter dc1 on prj1.dc_id = dc1.id ");
    	
    	sb.append(" LEFT JOIN cloud_ldpool ld ON ld.pool_id = msg.am_vmid AND ( msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDCOMMON+"' OR ");
    	sb.append(" msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDMASTER+"' )");
    	sb.append(" LEFT JOIN cloud_project prj2 on prj2.prj_id = ld.prj_id AND ( msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDCOMMON+"' OR ");
    	sb.append(" msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDMASTER+"' )");
    	sb.append(" LEFT JOIN dc_datacenter dc2 on prj2.dc_id = dc2.id ");
        if(sessionUser.getIsAdmin()){
            //如果当前用户是超级管理员的话，则直接查询项目中该custom_id创建的项目即可
            sb.append("WHERE ")
            .append(" (prj0.customer_id = ? or prj1.customer_id = ? or prj2.customer_id = ?) ");
        }else{
            //如果当前用户不是超管，则需要查询该用户有权限的项目中产生的报警信息
            sb.append(" LEFT JOIN sys_selfuserprj userprj0 ON userprj0.project_id = vm.prj_id ")
            .append("  AND msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_VM+"' ")
            
            .append(" LEFT JOIN sys_selfuserprj userprj1 ON userprj1.project_id = rds.prj_id ")
            .append("  AND msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_CLOUDDATA+"' ")
            
            .append(" LEFT JOIN sys_selfuserprj userprj2 ON userprj2.project_id = ld.prj_id ")
            .append("  AND (msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDCOMMON+"' OR ")
            .append("  msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDMASTER+"' ) ")
            .append("WHERE (userprj0.user_id = ? OR userprj1.user_id = ? OR userprj2.user_id = ?) ");
        }
        paramList.add(userId);
        paramList.add(userId);
        paramList.add(userId);
        sb.append(" AND CASE WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_VM+"' THEN vm.vm_name LIKE ? ")
          .append(" WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_CLOUDDATA+"' THEN rds.rds_name LIKE ? ")
          .append(" WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDCOMMON+"' OR ")
          .append(" msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDMASTER+"' THEN ld.pool_name LIKE ? ")
          .append(" END ");
        paramList.add("%"+vmName+"%");
        paramList.add("%"+vmName+"%");
        paramList.add("%"+vmName+"%");
        sb.append(" AND CASE WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_VM+"' THEN dc0.dc_name LIKE ? ")
          .append(" WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_CLOUDDATA+"' THEN dc1.dc_name LIKE ? ")
          .append(" WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDCOMMON+"' OR ")
          .append(" msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDMASTER+"' THEN dc2.dc_name LIKE ? ")
          .append(" END ");
        paramList.add("%"+dcName+"%");
        paramList.add("%"+dcName+"%");
        paramList.add("%"+dcName+"%");
        if(!StringUtil.isEmpty(alarmType)){
        	sb.append(" AND msg.am_alarmtype = ?");
        	paramList.add(alarmType);
        }
        if(!StringUtil.isEmpty(monitorType)){
        	sb.append(" AND msg.am_monitortype = ?");
        	paramList.add(monitorType);
        }
        if(!StringUtil.isEmpty(processedSign)){
        	sb.append(" AND msg.am_isprocessed = ?");
        	paramList.add(processedSign);
        }
        sb.append("ORDER BY msg.am_time DESC");
        page = alarmRuleDao.pagedNativeQuery(sb.toString(), queryMap, paramList.toArray());
        List resultList = (List) page.getResult();
        for(int i=0; i<resultList.size(); i++){
            Object[] objs = (Object[]) resultList.get(i);
            AlarmMessage msg = new AlarmMessage();
            msg.setId(String.valueOf(objs[0]));
            msg.setVmId(String.valueOf(objs[1]));
            msg.setVmName(String.valueOf(objs[2]));
            msg.setProjectName(String.valueOf(objs[3]));
            String aType = String.valueOf(objs[4]);
            msg.setAlarmType(aType);
            String mType = String.valueOf(objs[5]);
            msg.setMonitorType(mType);
            msg.setDetail(String.valueOf(objs[6]));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timeStr = sdf.format(objs[7]);
            Date time = null;
            try {
                time = sdf.parse(timeStr);
            } catch (ParseException e) {
                throw new AppException(e.getMessage());
            }
            msg.setTime(time);
            msg.setAlarmTime(timeStr);
            
            String isProcessed = String.valueOf(objs[8]);
            String alarmSign = isProcessed.equals("0")?"未处理":"已处理";
            msg.setIsProcessed(isProcessed);
            msg.setAlarmSign(alarmSign);
            
            String aName = getMonitorItemByNodeID(aType);
            String mName = getMonitorItemByNodeID(mType);
            msg.setAlarmTypeName(aName);
            msg.setMonitorTypeName(mName);
            
            resultList.set(i, msg);
        }
        return page;
    }

    /**
     * 根据nodeId查询数据字典里对应的nodeName
     * @param nodeId
     * @return
     */
    private String getMonitorItemByNodeID(String nodeId) {
        String monitorItem = null;
        try {
            String monitorItemStr = jedisUtil.get(RedisKey.SYS_DATA_TREE+nodeId);
            JSONObject monitorItemJSON = JSONObject.parseObject(monitorItemStr);
            
            monitorItem = monitorItemJSON.getString("nodeName");
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
        return monitorItem;
    }

    /**
     * 根据报警规则ID查询报警对象数量
     * @param ruleId
     * @return
     */
    private int getAlarmObjectNumber(String ruleId) {
        StringBuffer sb = new StringBuffer();
        sb.append(" from BaseAlarmObject where alarmRuleId = ?");
        List<BaseAlarmObject> list = alarmObjectDao.find(sb.toString(), ruleId);
        return list.size();
    }

    /**
     * 根据报警规则ID查询触发条件字符串
     * @param ruleId
     * @return
     */
    @SuppressWarnings({ "unchecked" })
    private String getTriggerConditionString(String ruleId) {
        StringBuffer sb = new StringBuffer();
        String sql = " from BaseAlarmTrigger where alarmRuleId=?";
        List<BaseAlarmTrigger> baseAlarmTriggerList = alarmTriggerDao.find(sql, ruleId);
        for(BaseAlarmTrigger trigger: baseAlarmTriggerList){
            sb.append(getMonitorItemByNodeID(trigger.getZb()))
                .append(trigger.getOperator())
                .append(trigger.getThreshold())
                .append(trigger.getUnit())
                .append("持续"+getLastTime(trigger.getLastTime()))
                .append(";");
        }
        return sb.toString();
    }

    private String getLastTime(int lastTime) {
        if(lastTime>=60 && lastTime<3600){
            return lastTime/60+"分钟";
        }else {
            return lastTime/3600+"小时";
        }
    }

    /**
     * 在数据字典中查询监控项列表
     * @return
     * @throws AppException
     * @see com.eayun.monitor.service.AlarmService#getMonitorItemList()
     */
    @Override
    public List<MonitorItem> getMonitorItemList() throws AppException {
        log.info("在数据字典中查询监控项列表");
        Set<String> monitorItemSet = null;
        List<MonitorItem> monitorItemList = new ArrayList<MonitorItem>();
        try {
            monitorItemSet = jedisUtil.getSet(RedisKey.SYS_DATA_TREE_PARENT_NODEID+RedisNodeIdConstant.MONITOR_TYPE_NODE_ID);
            for (String s : monitorItemSet) {
                String monitorItemStr = jedisUtil.get(RedisKey.SYS_DATA_TREE+s);
                JSONObject monitorItemJSON = JSONObject.parseObject(monitorItemStr);
                
                MonitorItem mi = new MonitorItem();
                mi.setName(monitorItemJSON.getString("nodeName"));
                mi.setNameEN(monitorItemJSON.getString("nodeNameEn"));
                mi.setNodeId(monitorItemJSON.getString("nodeId"));
                
                monitorItemList.add(mi);
            }
        } catch (Exception e) {
            throw new AppException("查询监控项异常");
        }
        return monitorItemList;
    }

    /**
     * 根据监控项nodeId在数据字典中获取该监控项下的所有监控指标列表
     * @param monitorItemNodeId
     * @return
     * @throws AppException
     * @see com.eayun.monitor.service.AlarmService#getMonitorZBList(java.lang.String)
     */
    @Override
    public List<MonitorZB> getMonitorZBList(String monitorItemNodeId) throws AppException {
        log.info("根据监控项nodeId在数据字典中获取该监控项下的所有监控指标列表");
        Set<String> zbSet = null;
        List<MonitorZB> zbList = new ArrayList<MonitorZB>();
        try {
            zbSet = jedisUtil.getSet(RedisKey.SYS_DATA_TREE_PARENT_NODEID +monitorItemNodeId);
            for (String s : zbSet) {
                String zbStr = jedisUtil.get(RedisKey.SYS_DATA_TREE +s);
                JSONObject zbJSON = JSONObject.parseObject(zbStr);
                MonitorZB zb = new MonitorZB();
                zb.setName(zbJSON.getString("nodeName"));
                zb.setNameEN(zbJSON.getString("nodeNameEn"));
                zb.setNodeId(zbJSON.getString("nodeId"));
                zb.setParentNodeId(zbJSON.getString("parentId"));
                zb.setZbUnit(MonitorAlarmUtil.getZbUnitByZb(zbJSON.getString("nodeId")));
                
                zbList.add(zb);
            }
        } catch (Exception e) {
            throw new AppException("查询监控指标异常");
        }
        Collections.sort(zbList,new Comparator<MonitorZB>(){
            public int compare(MonitorZB arg0, MonitorZB arg1) {
            	String value0 = "";
            	String value1 = "";
            	value0 = arg0.getNodeId();
            	value1 = arg1.getNodeId();
            	int result = 0;
            	result = new Double(value0).compareTo(new Double(value1));
                return result;
            }
        });
        return zbList;
    }

    /**
     * 添加报警规则
     * @param alarmRule
     * @return
     * @throws AppException
     * @see com.eayun.monitor.service.AlarmService#addAlarmRule(com.eayun.monitor.model.AlarmRule)
     */
    @Override
    public AlarmRule addAlarmRule(AlarmRule alarmRule, List<Map> triggerConditionList) throws AppException {
        log.info("添加报警规则开始");
        BaseAlarmRule baseAlarmRule = new BaseAlarmRule();
        BeanUtils.copyProperties(alarmRule, baseAlarmRule);
        alarmRuleDao.saveEntity(baseAlarmRule);
        BeanUtils.copyProperties(baseAlarmRule, alarmRule);
        
        for (Map triggerMap : triggerConditionList) {
            if(triggerMap.size()>0){
                AlarmTrigger trigger = new AlarmTrigger();
                trigger.setIsTriggered("0");
                trigger.setAlarmRuleId(alarmRule.getId());
                trigger.setLastTime(Integer.valueOf(triggerMap.get("lastTime").toString()));
                trigger.setOperator(triggerMap.get("operator").toString());
                float threshold = Float.valueOf(triggerMap.get("threshold").toString());
                if(threshold<0.0){
                    threshold*=-1;
                }
                trigger.setThreshold(threshold);
                trigger.setZb(triggerMap.get("zb").toString());
                
                addAlarmTrigger(trigger);
            }
        }
        
        return alarmRule;
    }

    /**
     * 删除报警规则
     * @param alarmRule
     * @return
     * @throws AppException
     * @see com.eayun.monitor.service.AlarmService#deleteAlarmRule(com.eayun.monitor.model.AlarmRule)
     */
    @Override
    public boolean deleteAlarmRule(AlarmRule alarmRule) throws AppException{
        log.info("删除报警规则开始");
        if(alarmRule!=null){
            deleteAllTriggersByRuleId(alarmRule.getId());
            deleteAllContactsByRuleId(alarmRule.getId());
            deleteAllObjectsByRuleId(alarmRule.getId());
            
            alarmRuleDao.delete(alarmRule.getId());
            
            //删除报警规则需要对应删除监控报警项
            monitorAlarmService.deleteMonitorAlarmItemByAlarmRule(alarmRule.getId());
        }
        return true;
    }

    /**
     * 添加报警触发条件
     * @param trigger
     * @return
     * @throws AppException
     * @see com.eayun.monitor.service.AlarmService#addAlarmTrigger(com.eayun.monitor.model.AlarmTrigger)
     */
    @Override
    public AlarmTrigger addAlarmTrigger(AlarmTrigger trigger) throws AppException {
        log.info("添加报警规则触发条件开始");
        String zbUnit = MonitorAlarmUtil.getZbUnitByZb(trigger.getZb());
        trigger.setUnit(zbUnit);
        
        BaseAlarmTrigger baseAlarmTrigger = new BaseAlarmTrigger();
        BeanUtils.copyProperties(trigger, baseAlarmTrigger);
        alarmTriggerDao.saveEntity(baseAlarmTrigger);
        BeanUtils.copyProperties(baseAlarmTrigger, trigger);
        
        String triggerJSON = JSONObject.toJSONString(baseAlarmTrigger);
        try {
            jedisUtil.set(RedisKey.ALARM_TRIGGER+trigger.getId(), triggerJSON);
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
        return trigger;
    }

    /**
     * 复制报警规则
     * @param alarmRule
     * @return
     * @throws AppException
     * @see com.eayun.monitor.service.AlarmService#copyAlarmRule(com.eayun.monitor.model.AlarmRule)
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean copyAlarmRule(AlarmRule alarmRule) throws AppException {
        //1. 调整alarmRule名称为 复制-OOO 然后插入记录到ecsc_alarmrule中
        //2. 在ecsc_alarmtrigger 中查询属于复制源报警规则的触发条件——
        //alarmRuleId关联到新的规则上，作为一条新纪录插入数据库和缓存
        log.info("复制报警规则开始");
        AlarmRule newAlarmRule = new AlarmRule();
        
        String newName = "复制_"+alarmRule.getName();
        newAlarmRule.setName(newName.length()<=20?newName:newName.substring(0, 20));
        
        newAlarmRule.setCusId(alarmRule.getCusId());
        newAlarmRule.setModifyTime(new Date());//复制后，新的报警规则的最后修改时间应该是复制时的时间
        newAlarmRule.setMonitorItem(alarmRule.getMonitorItem());
        
        BaseAlarmRule baseAlarmRule = new BaseAlarmRule();
        BeanUtils.copyProperties(newAlarmRule, baseAlarmRule);
        alarmRuleDao.saveEntity(baseAlarmRule);
        BeanUtils.copyProperties(baseAlarmRule, newAlarmRule);
        
        StringBuffer sb = new StringBuffer();
        sb.append(" from BaseAlarmTrigger where alarmRuleId=?");
        List<BaseAlarmTrigger> triggerList = alarmTriggerDao.find(sb.toString(), alarmRule.getId());
        for (BaseAlarmTrigger oldTrigger : triggerList) {
            BaseAlarmTrigger newTrigger = new BaseAlarmTrigger();
            newTrigger.setAlarmRuleId(newAlarmRule.getId());
            newTrigger.setIsTriggered(oldTrigger.getIsTriggered());
            newTrigger.setLastTime(oldTrigger.getLastTime());
            newTrigger.setOperator(oldTrigger.getOperator());
            newTrigger.setThreshold(oldTrigger.getThreshold());
            newTrigger.setUnit(oldTrigger.getUnit());//XXX 因为addAlarmTrigger中对单位又做了一次设置，所以这里理论上是可以不用设置单位的。
            newTrigger.setZb(oldTrigger.getZb());
            
            AlarmTrigger alarmTrigger = new AlarmTrigger();
            BeanUtils.copyProperties(newTrigger, alarmTrigger);
            addAlarmTrigger(alarmTrigger);
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean checkRuleName(String cusId, String alarmRuleName) throws AppException {
        log.info("判断报警规则是否重名");
        StringBuffer sb = new StringBuffer();
        sb.append("select count(*) from BaseAlarmRule where cusId = ? and name = ?");
        List<Long> list = alarmRuleDao.find(sb.toString(), cusId, alarmRuleName);
        long count = list.get(0);
        return count == 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public AlarmRule getAlarmRuleById(String cusId, String alarmRuleId) throws AppException {
        log.info("根据规则ID查询报警规则");
        StringBuffer sb = new StringBuffer();
        sb.append(" from BaseAlarmRule where cusId=? and id=?");
        List<BaseAlarmRule> list = alarmRuleDao.find(sb.toString(), cusId, alarmRuleId);
        if(list.isEmpty()){
        	return null;
        }
        BaseAlarmRule baseAlarmRule = list.get(0);
        AlarmRule alarmRule = new AlarmRule();
        BeanUtils.copyProperties(baseAlarmRule, alarmRule);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStr = sdf.format(alarmRule.getModifyTime());
        alarmRule.setLastModifyTime(timeStr);
        alarmRule.setMonitorItemName(getMonitorItemByNodeID(alarmRule.getMonitorItem()));
        alarmRule.setTriggerCondition(getTriggerConditionString(alarmRule.getId()));
        alarmRule.setAlarmObjectNumber(getAlarmObjectNumber(alarmRule.getId()));
        
        return alarmRule;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<AlarmTrigger> getAlarmTriggerListByRuleId(String alarmRuleId) throws AppException {
        log.info("根据报警规则查询触发条件开始");
        List<AlarmTrigger> alarmTriggerList = new ArrayList<AlarmTrigger>();
        StringBuffer sb = new StringBuffer();
        sb.append(" from BaseAlarmTrigger where alarmRuleId=?");
        List<BaseAlarmTrigger> baseAlarmTriggerList = alarmTriggerDao.find(sb.toString(),alarmRuleId);
        for (BaseAlarmTrigger baseAlarmTrigger : baseAlarmTriggerList) {
            AlarmTrigger alarmTrigger = new AlarmTrigger();
            BeanUtils.copyProperties(baseAlarmTrigger, alarmTrigger);
            alarmTriggerList.add(alarmTrigger);
        }
        return alarmTriggerList;
    }

    @Override
    public List<MonitorZB> getMonitorZBListByRuleId(String alarmRuleId) throws AppException {
        log.info("根据规则id找到其监控项，根据监控项取得监控指标");
        BaseAlarmRule baseAlarmRule = alarmRuleDao.findOne(alarmRuleId);
        List<MonitorZB> zbList = getMonitorZBList(baseAlarmRule.getMonitorItem());
        return zbList;
    }

    @Override
    public AlarmRule updateAlarmRule(AlarmRule alarmRule, List<Map> triggerConditionList) throws AppException {
        log.info("更新报警规则开始");
        BaseAlarmRule baseRule = new BaseAlarmRule();
        BeanUtils.copyProperties(alarmRule, baseRule);
        alarmRuleDao.saveOrUpdate(baseRule);
        BeanUtils.copyProperties(baseRule, alarmRule);
        
        //针对监控规则的触发条件，采取全删全增的方式。
        boolean isClear = deleteAllTriggersByRuleId(alarmRule.getId());
        if(isClear){
            for (Map triggerMap : triggerConditionList) {
                if(triggerMap.size()>0){
                    AlarmTrigger trigger = new AlarmTrigger();
                    trigger.setIsTriggered("0");
                    trigger.setAlarmRuleId(alarmRule.getId());
                    trigger.setLastTime(Integer.valueOf(triggerMap.get("lastTime").toString()));
                    trigger.setOperator(triggerMap.get("operator").toString());
                    float threshold = Float.valueOf(triggerMap.get("threshold").toString());
                    if(threshold<0.0){
                        threshold*=-1;
                    }
                    trigger.setThreshold(threshold);
                    trigger.setZb(triggerMap.get("zb").toString());
                    
                    addAlarmTrigger(trigger);
                }
            }
        }
        //编辑报警规则需要同时更新监控报警项。
        monitorAlarmService.updateMonitorAlarmItemByAlarmRule(alarmRule.getId());
        return alarmRule;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean deleteAllTriggersByRuleId(String id) throws AppException {
        log.info("删除报警规则下的所有触发条件");
        String triggerSQL = " from BaseAlarmTrigger where alarmRuleId=?";
        List<BaseAlarmTrigger> triggerList = alarmTriggerDao.find(triggerSQL, id);
        for (BaseAlarmTrigger trigger : triggerList) {
            try {
                jedisUtil.delete(RedisKey.ALARM_TRIGGER+trigger.getId());
                alarmTriggerDao.delete(trigger);
            } catch (Exception e) {
                throw new AppException(e.getMessage());
            }
        }
        return true;
    }

    /**
     * 查询报警规则下的报警对象列表(区分监控项类别)
     * @Author: duanbinbin
     * @param alarmRuleId
     * @param monitorType
     * @return
     * @throws AppException
     *<li>Date: 2017年3月2日</li>
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<AlarmObject> getAlarmObjectListByRuleId(String alarmRuleId,String monitorType) throws AppException {
        log.info("查询报警规则下的报警对象列表");
        List<AlarmObject> alarmObjectList = new ArrayList<AlarmObject>();
        StringBuffer sb = new StringBuffer();
        sb.append(" from BaseAlarmObject where alarmRuleId= ? and monitorType = ? ");
        List<BaseAlarmObject> baseAlarmObjectList = alarmObjectDao.find(sb.toString(),alarmRuleId,monitorType);
        for (BaseAlarmObject baseAlarmObject : baseAlarmObjectList) {
            AlarmObject alarmObject = new AlarmObject();
            BeanUtils.copyProperties(baseAlarmObject, alarmObject);
            
            alarmObject= this.getAttributesByResourceId(alarmObject);
            if(alarmObject.getIsDeleted()){		//如遇到资源已删除的垃圾数据，做清除处理
            	log.warn("该报警对象资源已被删除，type:"+alarmObject.getMonitorType()+",resourceId:"+alarmObject.getVmId()+",objectId:"+alarmObject.getId());
            	if(alarmObject.getMonitorType().equals(RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDCOMMON)
            			||alarmObject.getMonitorType().equals(RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDMASTER)){
            		this.clearPoolMsgAfterDeletePool(alarmObject.getVmId());
            	}else{
            		this.cleanAlarmDataAfterDeletingVM(alarmObject.getVmId());
            	}
            	continue;
            }
            alarmObjectList.add(alarmObject);
        }
        return alarmObjectList;
    }
    /**
     * 报警对象赋予属性，用于规则详情页列表和报警计划任务
     * @Author: duanbinbin
     * @param alarmObject
     * @return
     * @throws AppException
     *<li>Date: 2017年3月3日</li>
     */
    private AlarmObject getAttributesByResourceId(AlarmObject alarmObject) throws AppException {
    	String resourceId = alarmObject.getVmId();
    	String dcName = "";
    	String prjId = "";
    	String resourceName = "";
    	String vmIp = "--";
    	String floatIp = "--";
    	String selfIp = "--";
    	alarmObject.setIsDeleted(false);
    	if (alarmObject.getMonitorType().equals(RedisNodeIdConstant.ECSC_MONITOR_TYPE_VM)) {
    		//ECSC监控项-云主机
    		try {
				CloudVm cloudVm = getObjAttrByVmId(resourceId);
				if(cloudVm!=null){
				    if(null!=cloudVm.getVmIp() && !cloudVm.getVmIp().equals("")){
				    	vmIp = cloudVm.getVmIp();
				    }
				    if(null != cloudVm.getFloatIp() && !cloudVm.getFloatIp().equals("")){
				    	floatIp = cloudVm.getFloatIp();
				    }
				    if(null != cloudVm.getSelfIp() && !cloudVm.getSelfIp().equals("")){
				    	selfIp = cloudVm.getSelfIp();
				    }
				    String network = cloudVm.getNetName()==null?"":cloudVm.getNetName();
				    resourceName = cloudVm.getVmName()==null?"":cloudVm.getVmName();
				    dcName = cloudVm.getDcName();
				    prjId = cloudVm.getPrjId();
				    alarmObject.setNetwork(network);
				    alarmObject.setFloatIp(floatIp);
				    alarmObject.setSelfSubIp(selfIp);
				}else{
					alarmObject.setIsDeleted(true);
				}
			} catch (Exception e) {
				throw new AppException(e.getMessage());
			}
    	}else if(alarmObject.getMonitorType().equals(RedisNodeIdConstant.ECSC_MONITOR_TYPE_CLOUDDATA)){
    		//ECSC监控项-云数据库
    		try {
    			CloudRDSInstance instance = getObjAttrByInstanceId(resourceId);
    			if(null!=instance){
    				dcName = instance.getDcName();
    				resourceName = instance.getRdsName();
    				prjId = instance.getPrjId();
    				if(null!=instance.getRdsIp() && !instance.getRdsIp().equals("")){
    			    	vmIp = instance.getRdsIp();
    			    }
    				alarmObject.setIsMaster(instance.getIsMaster());
    				alarmObject.setDataVersionName(instance.getVersion());
    			}else{
					alarmObject.setIsDeleted(true);
				}
			} catch (Exception e) {
				throw new AppException(e.getMessage());
			}
    	}else if(alarmObject.getMonitorType().equals(RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDCOMMON)){
    		//ECSC监控项-负载均衡普通模式
    		try {
    			CloudLdPool ldPool = getObjAttrByPoolId(resourceId,"0");
    			if(null != ldPool){
    				dcName = ldPool.getDcName();
    				resourceName = ldPool.getPoolName();
    				prjId = ldPool.getPrjId();
    				if(null!=ldPool.getSubnetIp() && !ldPool.getSubnetIp().equals("")){
    			    	vmIp = ldPool.getSubnetIp();
    			    }
    				if(null != ldPool.getFloatIp() && !ldPool.getFloatIp().equals("")){
    			    	floatIp = ldPool.getFloatIp();
    			    }
    				alarmObject.setFloatIp(floatIp);
    				alarmObject.setConfig(ldPool.getPoolProtocol()+"："+ldPool.getVipPort());
    				alarmObject.setMode("0");
    				alarmObject.setNetwork(ldPool.getNetName());
    			}else{
					alarmObject.setIsDeleted(true);
				}
			} catch (Exception e) {
				throw new AppException(e.getMessage());
			}
    	}else if(alarmObject.getMonitorType().equals(RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDMASTER)){
    		//ECSC监控项-负载均衡主备模式
    		try {
    			CloudLdPool ldPool = getObjAttrByPoolId(resourceId,"1");
    			if(null != ldPool){
    				dcName = ldPool.getDcName();
    				resourceName = ldPool.getPoolName();
    				prjId = ldPool.getPrjId();
    				if(null!=ldPool.getSubnetIp() && !ldPool.getSubnetIp().equals("")){
    			    	vmIp = ldPool.getSubnetIp();
    			    }
    				if(null != ldPool.getFloatIp() && !ldPool.getFloatIp().equals("")){
    			    	floatIp = ldPool.getFloatIp();
    			    }
    				alarmObject.setFloatIp(floatIp);
    				alarmObject.setConfig(ldPool.getPoolProtocol()+"："+ldPool.getVipPort());
    				alarmObject.setMode("1");
    				alarmObject.setNetwork(ldPool.getNetName());
    			}else{
					alarmObject.setIsDeleted(true);
				}
			} catch (Exception e) {
				throw new AppException(e.getMessage());
			}
    	}
    	alarmObject.setDcName(dcName);
	    alarmObject.setVmName(resourceName);
	    alarmObject.setVmIp(vmIp.substring(vmIp.indexOf(":")+1));
	    alarmObject.setPrjId(prjId);
        return alarmObject;
    }
    /**
     * 查询某客户某项规则未添加的报警对象列表（区分监控项类型）
     * 添加报警对象选择框左侧列表
     * @Author: duanbinbin
     * @param cusId
     * @param alarmRuleId
     * @param monitorType
     * @return
     * @throws AppException
     *<li>Date: 2017年3月2日</li>
     */
    @Override
    public List<AlarmObject> getAvailableAlarmObjectsByCustomer(String cusId, 
    		String alarmRuleId,String monitorType)throws AppException {
        log.info("查询可选的报警对象列表");
        //联查对应的资源表和报警规则表，通过userId找到所有的且没有添加到alarmRule下的资源，组织报警对象。
        List<AlarmObject> alarmObjectList = new ArrayList<AlarmObject>();
        if (monitorType.equals(RedisNodeIdConstant.ECSC_MONITOR_TYPE_VM)) {
    		//ECSC监控项-云主机
        	StringBuffer sb = new StringBuffer();
            sb.append("SELECT ")
              .append("	vm.vm_id,")
              .append(" vm.vm_name,")
              .append(" net.net_name,")
              .append(" vm.vm_ip, ")
              .append("	vm.self_ip,")
              .append(" flo.flo_ip ")
              .append("FROM cloud_vm vm ")
              .append("LEFT JOIN cloud_network net ON vm.net_id = net.net_id ")
              .append("LEFT JOIN cloud_floatip flo ON vm.vm_id = flo.resource_id and flo.resource_type='vm' and flo.is_deleted ='0' ")
              .append("WHERE vm.is_deleted='0' and vm.is_visable = '1' and ")
              .append(" vm.prj_id IN ( ")
              .append(" SELECT prj_id FROM cloud_project WHERE customer_id =?")
              .append(" ) ")
              .append("AND vm.vm_id NOT IN ( ")
              .append("	SELECT ao_vmid FROM ecsc_alarmobject WHERE ao_alarmruleid =? AND ao_monitortype = ?")
              .append(") ");
            Query query = alarmObjectDao.createSQLNativeQuery(sb.toString(), cusId, alarmRuleId,monitorType);
            List resultList = query.getResultList();
            for(int i=0; i<resultList.size(); i++){
                Object[] objs = (Object[]) resultList.get(i);
                AlarmObject alarmObject = new AlarmObject();
                alarmObject.setVmId(objs[0].toString());
                alarmObject.setVmName(objs[1].toString());
                String network = objs[2]==null?"":objs[2].toString();
                alarmObject.setNetwork(network);
                String vmIp = (objs[3]==null || objs[3].equals(""))?"--":objs[3].toString();
                alarmObject.setVmIp(vmIp.substring(vmIp.indexOf(":")+1));
                String selfIp = (objs[4] == null || objs[4].equals("")) ? "--" : objs[4].toString();
                alarmObject.setSelfSubIp(selfIp);
                String floatIp = (objs[5] == null || objs[5].equals("")) ? "--" : objs[5].toString();
                alarmObject.setFloatIp(floatIp);
                
                alarmObject.setAlarmRuleId(alarmRuleId);
                alarmObject.setMonitorType(monitorType);
                alarmObjectList.add(alarmObject);
            }
    	}else if(monitorType.equals(RedisNodeIdConstant.ECSC_MONITOR_TYPE_CLOUDDATA)){
    		//ECSC监控项-云数据库
    		StringBuffer sb = new StringBuffer();
            sb.append("SELECT ")
              .append("	rds.rds_id,")
              .append(" rds.rds_name,")
              .append(" rds.rds_ip")
              .append(" FROM cloud_rdsinstance rds ")
              .append(" WHERE rds.is_deleted = '0' AND rds.is_visible = '1' ")
              .append(" AND rds.prj_id IN (")
              .append(" SELECT prj_id FROM cloud_project WHERE customer_id =?")
              .append(" ) ")
              .append(" AND rds.rds_id NOT IN (")
              .append("	SELECT ao_vmid FROM ecsc_alarmobject WHERE ao_alarmruleid =? AND ao_monitortype = ?")
              .append(") ");
            Query query = alarmObjectDao.createSQLNativeQuery(sb.toString(), cusId, alarmRuleId,monitorType);
            List resultList = query.getResultList();
            for(int i=0; i<resultList.size(); i++){
                Object[] objs = (Object[]) resultList.get(i);
                AlarmObject alarmObject = new AlarmObject();
                alarmObject.setVmId(objs[0].toString());
                alarmObject.setVmName(objs[1].toString());
                String vmIp = (objs[2]==null || objs[2].equals(""))?"--":objs[2].toString();
                alarmObject.setVmIp(vmIp.substring(vmIp.indexOf(":")+1));
                
                alarmObject.setAlarmRuleId(alarmRuleId);
                alarmObject.setMonitorType(monitorType);
                alarmObjectList.add(alarmObject);
            }
    	}else if(monitorType.equals(RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDCOMMON)){
    		//ECSC监控项-负载均衡普通模式
    		StringBuffer sb = new StringBuffer();
            sb.append("SELECT ")
              .append("	ld.pool_id,")
              .append("	ld.pool_name,")
              .append("	sub.gateway_ip,")
              .append("	flo.flo_ip")
              .append(" FROM cloud_ldpool ld ")
              .append(" LEFT JOIN cloud_subnetwork sub ON ld.subnet_id = sub.subnet_id")
              .append(" LEFT JOIN cloud_floatip flo ON ld.pool_id = flo.resource_id AND flo.resource_type = 'lb' AND flo.is_deleted = '0'")
              .append(" WHERE ld.is_visible = '1' and ld.mode = ? ")
              .append(" AND ld.prj_id IN (")
              .append("	SELECT prj_id FROM cloud_project WHERE customer_id =?")
              .append(" ) ")
              .append(" AND ld.pool_id NOT IN (")
              .append("	SELECT ao_vmid FROM ecsc_alarmobject WHERE ao_alarmruleid =? AND ao_monitortype = ?")
              .append(") ");
            Query query = alarmObjectDao.createSQLNativeQuery(sb.toString(), "0",cusId, alarmRuleId,monitorType);
            List resultList = query.getResultList();
            for(int i=0; i<resultList.size(); i++){
                Object[] objs = (Object[]) resultList.get(i);
                AlarmObject alarmObject = new AlarmObject();
                alarmObject.setVmId(objs[0].toString());
                alarmObject.setVmName(objs[1].toString());
                String vmIp = (objs[2]==null || objs[2].equals(""))?"--":objs[2].toString();
                alarmObject.setVmIp(vmIp.substring(vmIp.indexOf(":")+1));
                String floatIp = (objs[3]==null || objs[3].equals(""))?"--":objs[3].toString();
                alarmObject.setFloatIp(floatIp);
                
                alarmObject.setAlarmRuleId(alarmRuleId);
                alarmObject.setMonitorType(monitorType);
                alarmObjectList.add(alarmObject);
            }
    	}else if(monitorType.equals(RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDMASTER)){
    		//ECSC监控项-负载均衡主备模式
    		StringBuffer sb = new StringBuffer();
            sb.append("SELECT ")
              .append("	ld.pool_id,")
              .append("	ld.pool_name,")
              .append("	sub.gateway_ip,")
              .append("	flo.flo_ip")
              .append(" FROM cloud_ldpool ld ")
              .append(" LEFT JOIN cloud_subnetwork sub ON ld.subnet_id = sub.subnet_id")
              .append(" LEFT JOIN cloud_floatip flo ON ld.pool_id = flo.resource_id AND flo.resource_type = 'lb' AND flo.is_deleted = '0'")
              .append(" WHERE ld.is_visible = '1' and ld.mode = ? ")
              .append(" AND ld.prj_id IN (")
              .append("	SELECT prj_id FROM cloud_project WHERE customer_id =?")
              .append(" ) ")
              .append(" AND ld.pool_id NOT IN (")
              .append("	SELECT ao_vmid FROM ecsc_alarmobject WHERE ao_alarmruleid =? AND ao_monitortype = ?")
              .append(") ");
            Query query = alarmObjectDao.createSQLNativeQuery(sb.toString(), "1",cusId, alarmRuleId,monitorType);
            List resultList = query.getResultList();
            for(int i=0; i<resultList.size(); i++){
                Object[] objs = (Object[]) resultList.get(i);
                AlarmObject alarmObject = new AlarmObject();
                alarmObject.setVmId(objs[0].toString());
                alarmObject.setVmName(objs[1].toString());
                String vmIp = (objs[2]==null || objs[2].equals(""))?"--":objs[2].toString();
                alarmObject.setVmIp(vmIp.substring(vmIp.indexOf(":")+1));
                String floatIp = (objs[3]==null || objs[3].equals(""))?"--":objs[3].toString();
                alarmObject.setFloatIp(floatIp);
                
                alarmObject.setAlarmRuleId(alarmRuleId);
                alarmObject.setMonitorType(monitorType);
                alarmObjectList.add(alarmObject);
            }
    	}
        return alarmObjectList;
    }
    
    @Override
    public AlarmObject addAlarmObject(AlarmObject alarmObject) throws AppException {
        log.info("添加报警对象开始");
        BaseAlarmObject baseAlarmObject = new BaseAlarmObject();
        BeanUtils.copyProperties(alarmObject, baseAlarmObject);
        alarmObjectDao.saveEntity(baseAlarmObject);
        BeanUtils.copyProperties(baseAlarmObject, alarmObject);
        
        String objectJSON = JSONObject.toJSONString(baseAlarmObject);
        try {
            jedisUtil.set(RedisKey.ALARM_OBJECT+baseAlarmObject.getId(), objectJSON);
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
        
        return alarmObject;
    }
    /**
     * 删除某一个报警对象需作操作
     * @Author: duanbinbin
     * @param alarmObject
     * @return
     * @throws AppException
     *<li>Date: 2017年3月6日</li>
     */
    @Override
    public boolean deleteAlarmObject(AlarmObject alarmObject) throws AppException {
        log.info("删除报警对象");
        boolean isDeleted = false;
        try {
            isDeleted = jedisUtil.delete(RedisKey.ALARM_OBJECT+alarmObject.getId());
            if(isDeleted){
                alarmObjectDao.delete(alarmObject.getId());
            }
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
        //删除报警对象需要删除指定alarmruleId和objectId的监控报警项
        monitorAlarmService.deleteMonitorAlarmItemByAlarmObject(alarmObject);
        deleteAlarmMessage(alarmObject);
        return isDeleted;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<AlarmContact> getAlarmContactListByRuleId(String alarmRuleId) throws AppException {
        log.info("根据报警规则ID获取报警联系人列表");
        List<AlarmContact> alarmContactList = new ArrayList<AlarmContact>();
        StringBuffer sb = new StringBuffer();
        sb.append(" from BaseAlarmContact where alarmRuleId=?");
        List<BaseAlarmContact> baseAlarmContactList = alarmContactDao.find(sb.toString(),alarmRuleId);
        for (BaseAlarmContact baseAlarmContact : baseAlarmContactList) {
            AlarmContact alarmContact = new AlarmContact();
            BeanUtils.copyProperties(baseAlarmContact, alarmContact);
            
            alarmContact= getAlarmContactAttributes(alarmContact);
            alarmContactList.add(alarmContact);
        }
        return alarmContactList;
    }

    private AlarmContact getAlarmContactAttributes(AlarmContact alarmContact) {
        StringBuffer sb = new StringBuffer();
        sb.append(" SELECT ")
          .append("   c.c_name, cg.cg_name, c.c_smsnotify, c.c_mailnotify ")
          .append(" FROM ")
          .append("   ecsc_contact c ")
          .append(" LEFT JOIN ecsc_contactgroupdetail cgd ON c.c_id = cgd.cgd_contactid ")
          .append(" LEFT JOIN ecsc_contactgroup cg ON cg.cg_id = cgd.cgd_groupid ")
          .append(" WHERE ")
          .append(" c.c_id = ?");
        Query query = alarmContactDao.createSQLNativeQuery(sb.toString(), alarmContact.getContactId());
        List resultList = query.getResultList();
        if(resultList.isEmpty()){
           return null; 
        }
        Object[] objs = (Object[]) query.getResultList().get(0);
        
        alarmContact.setContactName(String.valueOf(objs[0]));
        String contactGroupName = objs[1]==null?"":objs[1].toString();
        if(contactGroupName.equals("default")){
            contactGroupName="默认联系组";
        }
        alarmContact.setContactGroupName(contactGroupName);
        
        String contactMethod = "";
        String isSmsNotify = String.valueOf(objs[2]);
        String isMailNotify = String.valueOf(objs[3]);
        contactMethod = getContactMethod(isSmsNotify, isMailNotify);
        alarmContact.setContactMethod(contactMethod);
        
        return alarmContact;
    }

    private String getContactMethod(String isSmsNotify, String isMailNotify) {
        if(isSmsNotify.equals("1") && isMailNotify.equals("1")){
            return "短信 邮件";
        }else if(isSmsNotify.equals("1") && isMailNotify.equals("0")){
            return "短信";
        }else if(isSmsNotify.equals("0") && isMailNotify.equals("1")){
            return "邮件";
        }
        return "";
    }

    @Override
    public List<ContactGroupAvailable> getAvailableContactGroupsList(String cusId)
                                                                                   throws AppException {
        log.info("获取所有可选的报警联系组列表");
        List<ContactGroupAvailable> contactGroupAvailablelist = new ArrayList<ContactGroupAvailable>();
        StringBuffer sb = new StringBuffer();
        sb.append(" from BaseContactGroup where cusId=?");
        List<BaseContactGroup> groupList = contactGroupDao.find(sb.toString(), cusId);
        for (BaseContactGroup baseContactGroup : groupList) {
            ContactGroupAvailable cga = new ContactGroupAvailable();
            cga.setContactGroupId(baseContactGroup.getId());
            if(baseContactGroup.getName().equals("default")){
                cga.setContactGroupName("默认联系组");
            }else{
                cga.setContactGroupName(baseContactGroup.getName());
            }
            
            String sql = "select c.c_id, c.c_name, c.c_smsnotify, c.c_mailnotify from ecsc_contact c left join ecsc_contactgroupdetail cgd on c.c_id = cgd.cgd_contactid where cgd.cgd_groupid=?";
            Query query  = contactDao.createSQLNativeQuery(sql, baseContactGroup.getId());
            List resultList = query.getResultList();
            List<AlarmContact> contactList = new ArrayList<AlarmContact>();
            for (int i=0; i<resultList.size(); i++) {
                Object[] objs = (Object[]) resultList.get(i);
                AlarmContact contact = new AlarmContact();
                contact.setContactId(String.valueOf(objs[0]));
                contact.setContactName(String.valueOf(objs[1]));
                
                String isSmsNotify = String.valueOf(objs[2]);
                String isMailNotify = String.valueOf(objs[3]);
                contact.setContactMethod(getContactMethod(isSmsNotify, isMailNotify));
                contactList.add(contact);
            }
            cga.setContactList(contactList);
            contactGroupAvailablelist.add(cga);
        }
        return contactGroupAvailablelist;
    }

    @Override
    public AlarmContact addAlarmContact(AlarmContact alarmContact) throws AppException {
        log.info("添加报警联系人开始");
        BaseAlarmContact baseAlarmContact = new BaseAlarmContact();
        BeanUtils.copyProperties(alarmContact, baseAlarmContact);
        alarmContactDao.saveEntity(baseAlarmContact);
        BeanUtils.copyProperties(baseAlarmContact, alarmContact);
        return alarmContact;
    }

    @Override
    public boolean deleteAlarmContact(AlarmContact alarmContact) throws AppException {
        log.info("删除报警联系人开始");
        alarmContactDao.delete(alarmContact.getId());
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean deleteAllObjectsByRuleId(String alarmRuleId) throws AppException {
        log.info("删除报警规则下的所有报警对象");
        String sql = " from BaseAlarmObject where alarmRuleId=?";
        List<BaseAlarmObject> objectList = alarmObjectDao.find(sql, alarmRuleId);
        boolean isDeleted = true;
        for (BaseAlarmObject object : objectList) {
            AlarmObject alarmObject = new AlarmObject();
            BeanUtils.copyProperties(object, alarmObject);
            isDeleted &= deleteAlarmObject(alarmObject);
            
            deleteAlarmMessage(alarmObject);
        }
        return isDeleted;
    }

    @SuppressWarnings("unchecked")
    private void deleteAlarmMessage(AlarmObject alarmObject) {
        //在删除报警对象时该不该删除该主机产生的报警信息？该！
        log.info("删除报警对象产生的报警信息");
        String sql = " from BaseAlarmMessage where vmId=? and alarmRuleId=?";
        List<BaseAlarmMessage> msgList = alarmMessageDao.find(sql, alarmObject.getVmId(), alarmObject.getAlarmRuleId());
        for (BaseAlarmMessage baseAlarmMessage : msgList) {
            alarmMessageDao.delete(baseAlarmMessage);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean deleteAllContactsByRuleId(String alarmRuleId) throws AppException {
        log.info("删除报警规则下的所有报警联系人");
        String sql = " from BaseAlarmContact where alarmRuleId=?";
        List<BaseAlarmContact> contactList = alarmContactDao.find(sql, alarmRuleId);
        for (BaseAlarmContact contact : contactList) {
            alarmContactDao.delete(contact);
        }
        return true;
    }

    /**
     * 添加报警对象（区分监控项类型）
     * @Author: duanbinbin
     * @param alarmRuleId
     * @param alarmObjectList
     * @param monitorType
     * @throws AppException
     *<li>Date: 2017年3月2日</li>
     */
    @Override
    public void addAlarmObject(String alarmRuleId, List<Map> alarmObjectList, String monitorType) throws AppException {
        /*
         * 由于全删全增存在误操作monitorAlarmItem和alarmMessage的情况
         * 所以只能通过alarmRuleId查询出已有的报警对象的vmId与前台提交过来的vmId对比
         * 找出哪些是应该删掉的，哪些是应该新增的，然后分别去新增和删除
         * */
        List<AlarmObject> existedAlarmObjList = getAlarmObjectListByRuleId(alarmRuleId,monitorType);//当前报警规则下已有的报警对象
        List<AlarmObject> toBeAddedAlarmObjList = new ArrayList<AlarmObject>();//前台提交来的列表中有，但是existedAlarmObjList中没有的，属于待新增的报警对象
        List<AlarmObject> toBeDeletedAlarmObjList = new ArrayList<AlarmObject>();//前台提交来的列表中没有，但是existedAlarmObjList中有的，属于待删除的报警对象
        
        List<AlarmObject> frontCommitedAlarmObjList = new ArrayList<AlarmObject>();	//前台提交来的报警对象
        for (Map aoMap : alarmObjectList) {
              AlarmObject ao = new AlarmObject();
              ao.setAlarmRuleId(alarmRuleId);
              ao.setVmId(String.valueOf(aoMap.get("vmId")));
              ao.setMonitorType(monitorType);
              frontCommitedAlarmObjList.add(ao);
        }
        for (AlarmObject alarmObject : frontCommitedAlarmObjList) {
            boolean isExistedInExistedAlarmObjectList = isExistedInExistedAlarmObjectList(alarmObject, existedAlarmObjList);
            if(!isExistedInExistedAlarmObjectList){
                toBeAddedAlarmObjList.add(alarmObject);
            }
        }
        for (AlarmObject alarmObject : existedAlarmObjList) {
            boolean isNotExistedInFrontCommitedList = isNotExistedInFrontCommitedList(alarmObject, frontCommitedAlarmObjList);
            if(isNotExistedInFrontCommitedList){
                toBeDeletedAlarmObjList.add(alarmObject);
            }
        }
        for (AlarmObject alarmObject : toBeAddedAlarmObjList) {
            addAlarmObject(alarmObject);
            monitorAlarmService.addMonitorAlarmItemByAlarmObject(alarmObject);
        }
        for (AlarmObject alarmObject : toBeDeletedAlarmObjList) {
            deleteAlarmObject(alarmObject);
        }
    }

    private boolean isNotExistedInFrontCommitedList(AlarmObject ao,
                                                     List<AlarmObject> frontCommitedAlarmObjList) {
        for (AlarmObject alarmObject : frontCommitedAlarmObjList) {
            if(ao.getVmId().equals(alarmObject.getVmId())){
                return false;
            }
        }
        return true;
    }

    private boolean isExistedInExistedAlarmObjectList(AlarmObject ao,
                                                      List<AlarmObject> existedAlarmObjList) {
        for (AlarmObject alarmObject : existedAlarmObjList) {
            if(ao.getVmId().equals(alarmObject.getVmId())){
                return true;
            }
        }
        return false;
    }

    @Override
    public void addAlarmContact(String alarmRuleId, List<Map> alarmContactList) throws AppException {
        boolean isClear = deleteAllContactsByRuleId(alarmRuleId);
        if(isClear){
            for (Map acMap : alarmContactList) {
                AlarmContact ac = new AlarmContact();
                ac.setAlarmRuleId(alarmRuleId);
                ac.setContactId(String.valueOf(acMap.get("contactId")));
                
                addAlarmContact(ac);
            }
        }
    }
    /**
     * 编辑报警规则时的页面参数
     * @Author: duanbinbin
     * @param cusId
     * @param alarmRuleId
     * @return
     * @throws AppException
     *<li>Date: 2017年3月6日</li>
     */
    @Override
    public JSONObject getAlarmRuleParams(String cusId, String alarmRuleId) throws AppException {
        AlarmRule alarmRule = getAlarmRuleById(cusId, alarmRuleId);
        List<MonitorItem> monitorItemList = getMonitorItemList();
        List<MonitorZB> monitorZBList = getMonitorZBListByRuleId(alarmRuleId);
        List<AlarmTrigger> triggerList = getAlarmTriggerListByRuleId(alarmRuleId);
        AlarmTrigger[] triggerArray = triggerList.toArray(new AlarmTrigger[0]);
        JSONObject json = new JSONObject();
        json.put("alarmRuleModel", alarmRule);
        json.put("monitorItemList", monitorItemList);
        json.put("monitorZBList", monitorZBList);
        json.put("triggerArray", triggerArray);
        return json;
    }
    /**
     * 查询报警对象详情，用于监控报警计划任务中
     * @Author: duanbinbin
     * @param objectId
     * @return
     *<li>Date: 2017年3月6日</li>
     */
    @Override
    public AlarmObject getAlarmObject(String objectId) {
        //在缓存中读取报警对象
        AlarmObject object = new AlarmObject();
        try {
            String jsonString = jedisUtil.get(RedisKey.ALARM_OBJECT+objectId);
            if(StringUtil.isEmpty(jsonString)){
            	log.error("根据报警对象id查询redis信息失败，objectId："+objectId);
            	return object;
            }
            JSONObject json = JSONObject.parseObject(jsonString);
            String alarmRuleId = String.valueOf(json.get("alarmRuleId"));
            String monitorType = String.valueOf(json.get("monitorType"));
            String vmId = String.valueOf(json.get("vmId"));
            object.setId(objectId);
            object.setAlarmRuleId(alarmRuleId);
            object.setVmId(vmId);
            object.setMonitorType(monitorType);
            object = this.getAttributesByResourceId(object);
            if(object.getIsDeleted()){		//如遇到资源已删除的垃圾数据，做清除处理
            	log.warn("该报警对象资源已被删除，请清查垃圾数据，type:"+object.getMonitorType()+",resourceId:"+vmId+",objectId:"+object.getId());
            	if(object.getMonitorType().equals(RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDCOMMON)
            			||object.getMonitorType().equals(RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDMASTER)){
            		this.clearPoolMsgAfterDeletePool(object.getVmId());
            	}else{
            		this.cleanAlarmDataAfterDeletingVM(object.getVmId());
            	}
            }
        } catch (Exception e) {
            log.error("",e);
            throw new AppException("在缓存中获取报警对象异常");
        }
        return object;
    }
    /**
     * 查询触发条件详情，用于监控报警计划任务中
     * @Author: duanbinbin
     * @param triggerId
     * @return
     *<li>Date: 2017年3月6日</li>
     */
    @Override
    public AlarmTrigger getAlarmTrigger(String triggerId) {
        //在缓存中读取触发条件
        AlarmTrigger trigger = new AlarmTrigger();
        try {
            String jsonString = jedisUtil.get(RedisKey.ALARM_TRIGGER+triggerId);
            if(StringUtil.isEmpty(jsonString)){
            	log.error("根据触发条件id查询redis信息失败，triggerId："+triggerId);
            	return trigger;
            }
            JSONObject json = JSONObject.parseObject(jsonString);
            String alarmRuleId = String.valueOf(json.get("alarmRuleId"));
            String isTriggered = String.valueOf(json.get("isTriggered"));
            String lastTimeStr = String.valueOf(json.get("lastTime"));
            int lastTime = Integer.valueOf(lastTimeStr);
            String operator = String.valueOf(json.get("operator"));
            String thresholdStr = String.valueOf(json.get("threshold"));
            float threshold = Float.valueOf(thresholdStr);
            String unit = String.valueOf(json.get("unit"));
            String zb = String.valueOf(json.get("zb"));
            
            trigger.setAlarmRuleId(alarmRuleId);
            trigger.setId(triggerId);
            trigger.setIsTriggered(isTriggered);
            trigger.setLastTime(lastTime);
            trigger.setOperator(operator);
            trigger.setThreshold(threshold);
            trigger.setUnit(unit);
            trigger.setZb(zb);
            
        } catch (Exception e) {
            log.error("",e);
            throw new AppException("在缓存中获取触发条件异常");
        }
        return trigger;
    }
    /**
     * 删除具体资源时（这里用于云主机和数据库实例），同时清除报警相关信息;删除负载均衡时不要直接调用这个接口
     * @Author: duanbinbin
     * @param vmId	主机或实例id
     * @return
     *<li>Date: 2017年3月6日</li>
     */
    @Override
    public boolean cleanAlarmDataAfterDeletingVM(String resourceId) {
        /**
         * 如果主机被删除，则报警对象（即某个主机或数据库实例）和报警对象产生的报警信息都要删除
         * 删除报警对象中删了对应报警对象的缓存和数据表中的数据，还删除了监控报警项
         */
        String findObjSQL = " from BaseAlarmObject where vmId=?";
        List<BaseAlarmObject> objList = alarmObjectDao.find(findObjSQL, resourceId);
        for (BaseAlarmObject baseAlarmObject : objList) {
            AlarmObject alarmObject = new AlarmObject();
            BeanUtils.copyProperties(baseAlarmObject, alarmObject);
            deleteAlarmObject(alarmObject);
        }
        return true;
    }
    /**
     * 删除负载均衡资源所做处理
     * @Author: duanbinbin
     * @param poolId
     *<li>Date: 2017年3月6日</li>
     */
    @Override
    public void clearPoolMsgAfterDeletePool(String poolId){
    	this.cleanAlarmDataAfterDeletingVM(poolId);
    	mongoTemplate.remove(new org.springframework.data.mongodb.core.query.Query(
    			Criteria.where("poolId").is(poolId)), MongoCollectionName.MONITOR_LD_POOL_MEMBER_EXP);
    }
    @Override
    public Contact getContactById(String contactId) {
        BaseContact baseCtc = contactDao.findOne(contactId);
        Contact contact = new Contact();
        BeanUtils.copyProperties(baseCtc, contact);
        return contact;
    }
    @Override
    public List<AlarmContact> getAllAlarmContactList(String cusId) {
        List<AlarmContact> contactsList = new ArrayList<AlarmContact>();
        StringBuffer sb = new StringBuffer();
        sb.append(" SELECT ")
          .append("   c.c_id, c.c_name, c.c_smsnotify, c.c_mailnotify ")
          .append(" FROM ")
          .append("   ecsc_contact c ")
          .append(" WHERE c.c_cusid=? ");
        Query query = alarmContactDao.createSQLNativeQuery(sb.toString(), cusId);
        List list = query.getResultList();
        for(int i=0; i<list.size(); i++){
            Object[] objs = (Object[]) list.get(i);
            
            AlarmContact alarmContact = new AlarmContact();
            alarmContact.setContactId(String.valueOf(objs[0]));
            alarmContact.setContactName(String.valueOf(objs[1]));
            
            String contactMethod = "";
            String isSmsNotify = String.valueOf(objs[2]);
            String isMailNotify = String.valueOf(objs[3]);
            contactMethod = getContactMethod(isSmsNotify, isMailNotify);
            alarmContact.setContactMethod(contactMethod);
            
            contactsList.add(alarmContact);
        }
        
        return contactsList;
    }
    /**
     * 当前登录用户能够看到的未处理的报警信息数量
     * 当前用户可看到的（区分监控项类型）
     * @Author: duanbinbin
     * @param sessionUser
     * @return
     *<li>Date: 2017年3月6日</li>
     */
    @Override
    public int getUnhandledAlarmMsgNumberByCusId(SessionUserInfo sessionUser) {
        /* 未处理的报警消息的数量应当是:
         * 1)如果当前是超管，则直接查询项目中该custom_id创建的项目中的主机;
         * 2)如果是普通用户，则需要根据userId查其有权限的项目中的主机产生的报警信息的数量
         */
        String userId = sessionUser.getIsAdmin()?sessionUser.getCusId():sessionUser.getUserId();
        List<Object> paramList = new ArrayList<Object>();
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ")
          .append(" count(msg.am_id) ")
          .append(" FROM ")
          .append(" ecsc_alarmmessage msg ");
        sb.append(" LEFT JOIN cloud_vm vm ON vm.vm_id = msg.am_vmid AND msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_VM+"' ");
		sb.append(" LEFT JOIN cloud_project prj0 on prj0.prj_id = vm.prj_id AND msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_VM+"' ");
		
		sb.append(" LEFT JOIN cloud_rdsinstance rds ON rds.rds_id = msg.am_vmid AND msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_CLOUDDATA+"' ");
    	sb.append(" LEFT JOIN cloud_project prj1 on prj1.prj_id = rds.prj_id AND msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_CLOUDDATA+"' ");
    	
    	sb.append(" LEFT JOIN cloud_ldpool ld ON ld.pool_id = msg.am_vmid AND ( msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDCOMMON+"' OR ");
    	sb.append(" msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDMASTER+"' )");
    	sb.append(" LEFT JOIN cloud_project prj2 on prj2.prj_id = ld.prj_id AND ( msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDCOMMON+"' OR ");
    	sb.append(" msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDMASTER+"' )");
        if(sessionUser.getIsAdmin()){
            //如果当前用户是超级管理员的话，则直接查询项目中该custom_id创建的项目即可
            sb.append("WHERE ")
            .append(" (prj0.customer_id = ? or prj1.customer_id = ? or prj2.customer_id = ?) ");
        }else{
            //如果当前用户不是超管，则需要查询该用户有权限的项目中产生的报警信息
            sb.append(" LEFT JOIN sys_selfuserprj userprj0 ON userprj0.project_id = vm.prj_id ")
            .append("  AND msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_VM+"' ")
            
            .append(" LEFT JOIN sys_selfuserprj userprj1 ON userprj1.project_id = rds.prj_id ")
            .append("  AND msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_CLOUDDATA+"' ")
            
            .append(" LEFT JOIN sys_selfuserprj userprj2 ON userprj2.project_id = ld.prj_id ")
            .append("  AND (msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDCOMMON+"' OR ")
            .append("  msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDMASTER+"' ) ")
            .append("WHERE (userprj0.user_id = ? OR userprj1.user_id = ? OR userprj2.user_id = ?) ");
        }
        paramList.add(userId);
        paramList.add(userId);
        paramList.add(userId);
        sb.append(" AND msg.am_isprocessed='0' ");
        Query query = alarmMessageDao.createSQLNativeQuery(sb.toString(), paramList.toArray());
        BigInteger bigInt = (BigInteger) query.getResultList().get(0);
        int number = bigInt.intValue();
        return number;
    }
    /**
     * 获取查询到报警信息的所属数据中心名称列表
     * 当前用户可看到的（区分监控项类型）
     * @Author: duanbinbin
     * @param sessionUser
     * @return
     *<li>Date: 2017年3月6日</li>
     */
    @Override
    public List<String> getPrjNamesBySession(SessionUserInfo sessionUser) {
    	List<Object> paramList = new ArrayList<Object>();
        String userId = sessionUser.getIsAdmin()?sessionUser.getCusId():sessionUser.getUserId();
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ")
  		  .append(" CASE WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_VM+"' THEN dc0.dc_name ")
          .append(" WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_CLOUDDATA+"' THEN dc1.dc_name ")
          .append(" WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDCOMMON+"' OR ")
          .append(" msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDMASTER+"' THEN dc2.dc_name ")
          .append(" END AS dcName ")
          .append(" FROM ")
          .append(" ecsc_alarmmessage msg ");
        sb.append(" LEFT JOIN cloud_vm vm ON vm.vm_id = msg.am_vmid AND msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_VM+"' ");
		sb.append(" LEFT JOIN cloud_project prj0 on prj0.prj_id = vm.prj_id AND msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_VM+"' ");
		sb.append(" LEFT JOIN dc_datacenter dc0 on prj0.dc_id = dc0.id ");
		
		sb.append(" LEFT JOIN cloud_rdsinstance rds ON rds.rds_id = msg.am_vmid AND msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_CLOUDDATA+"' ");
    	sb.append(" LEFT JOIN cloud_project prj1 on prj1.prj_id = rds.prj_id AND msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_CLOUDDATA+"' ");
    	sb.append(" LEFT JOIN dc_datacenter dc1 on prj1.dc_id = dc1.id ");
    	
    	sb.append(" LEFT JOIN cloud_ldpool ld ON ld.pool_id = msg.am_vmid AND ( msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDCOMMON+"' OR ");
    	sb.append(" msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDMASTER+"' )");
    	sb.append(" LEFT JOIN cloud_project prj2 on prj2.prj_id = ld.prj_id AND ( msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDCOMMON+"' OR ");
    	sb.append(" msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDMASTER+"' )");
    	sb.append(" LEFT JOIN dc_datacenter dc2 on prj2.dc_id = dc2.id ");
        if(sessionUser.getIsAdmin()){
            //如果当前用户是超级管理员的话，则直接查询项目中该custom_id创建的项目即可
            sb.append("WHERE ")
            .append(" (prj0.customer_id = ? or prj1.customer_id = ? or prj2.customer_id = ?) ");
        }else{
            //如果当前用户不是超管，则需要查询该用户有权限的项目中产生的报警信息
            sb.append(" LEFT JOIN sys_selfuserprj userprj0 ON userprj0.project_id = vm.prj_id ")
            .append("  AND msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_VM+"' ")
            
            .append(" LEFT JOIN sys_selfuserprj userprj1 ON userprj1.project_id = rds.prj_id ")
            .append("  AND msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_CLOUDDATA+"' ")
            
            .append(" LEFT JOIN sys_selfuserprj userprj2 ON userprj2.project_id = ld.prj_id ")
            .append("  AND (msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDCOMMON+"' OR ")
            .append("  msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDMASTER+"' ) ")
            .append("WHERE (userprj0.user_id = ? OR userprj1.user_id = ? OR userprj2.user_id = ?) ");
        }
        paramList.add(userId);
        paramList.add(userId);
        paramList.add(userId);
        sb.append(" GROUP BY dcName ");
        Query query = alarmMessageDao.createSQLNativeQuery(sb.toString(), paramList.toArray());
        List list = query.getResultList();
        List<String> prjNames = new ArrayList<String>();
        for(int i=0; i<list.size(); i++){
            String prjName = list.get(i)==null?"":list.get(i).toString();
            prjNames.add(prjName);
        }
        return prjNames;
    }
    /**
     * 云主机，报警对象属性
     * @Author: duanbinbin
     * @return vm_id	云主机id
     * @return vm_name	云主机名称
     * @return prj_id	项目id
     * @return dc_id	数据中心名称
     * @return vm_ip	受管子网IP
     * @return self_ip	自管子网IP
     * @return prj_name	项目名称
     * @return net_Name	所属网络名称
     * @return flo_ip	弹性公网IP
     * @return dc_name	数据中心名称
     *<li>Date: 2017年3月6日</li>
     */
    private CloudVm getObjAttrByVmId(String vmId) throws Exception {
        StringBuffer sql = new StringBuffer();
        sql.append(" select ");
        sql.append("    vm.vm_id as vmId,   ");
        sql.append("    vm.vm_name as vmName,   ");
        sql.append("    vm.prj_id as prjId, ");
        sql.append("    vm.dc_id as dcId,   ");
        sql.append("    vm.vm_ip as vmIp,   ");
        sql.append("    vm.self_ip as selfIp,   ");
        sql.append("    prj.prj_name as prjName,    ");
        sql.append("    net.net_Name as netName,    ");
        sql.append("    flo.flo_ip as floatIp,  ");
        sql.append("    dc.dc_name as dcName   ");
        sql.append(" from cloud_vm vm ");
        sql.append(" left join cloud_network net on vm.net_id=net.net_id");
        sql.append(" left join cloud_project prj on vm.prj_id=prj.prj_id");
        sql.append(" left join dc_datacenter dc on dc.id = vm.dc_id ");
        sql.append(" left join cloud_floatip flo on flo.resource_id = vm.vm_id and flo.resource_type = 'vm' and flo.is_deleted ='0' ");
        sql.append(" where 1=1 and vm.vm_id= ? and vm.is_deleted= '0' ");
        CloudVm cloudVm = null;
        javax.persistence.Query query = alarmObjectDao.createSQLNativeQuery(sql.toString(), new Object []{vmId});
        @SuppressWarnings("rawtypes")
        List list = new ArrayList();
        if(null!=query ){
            list = query.getResultList();
        }
        if(!list.isEmpty()&& list.size()==1){
            Object[] objs = (Object[]) list.get(0);
            cloudVm = new CloudVm();
            cloudVm.setVmId(String.valueOf(objs[0]));
            cloudVm.setVmName(String.valueOf(objs[1]));
            cloudVm.setPrjId(String.valueOf(objs[2]));
            cloudVm.setDcId(String.valueOf(objs[3]));
            cloudVm.setVmIp(String.valueOf(objs[4]==null?"":objs[4]));
            cloudVm.setSelfIp(String.valueOf(objs[5]==null?"":objs[5]));
            cloudVm.setPrjName(String.valueOf(objs[6]));
            cloudVm.setNetName(String.valueOf(objs[7]));
            cloudVm.setFloatIp(String.valueOf(objs[8]==null?"":objs[8]));
            cloudVm.setDcName(String.valueOf(objs[9]));
        }
        return cloudVm;
    }
    /**
     * 云数据库实例，报警对象属性
     * @Author: duanbinbin
     * @return rds_id	实例id
     * @return rds_name	实例名称
     * @return prj_id	项目id
     * @return dc_id	数据中心id
     * @return rds_ip	受管子网IP
     * @return is_master	是否主库
     * @return storeName + verName	版本名称
     * @return prj_name	项目名称
     * @return dc_name	数据中心名称
     *<li>Date: 2017年3月6日</li>
     */
    private CloudRDSInstance getObjAttrByInstanceId(String instanceId) throws Exception {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT ");
        sql.append("	rds.rds_id,");
        sql.append("	rds.rds_name,");
        sql.append("	rds.prj_id,");
        sql.append("	rds.dc_id,");
        sql.append("	rds.rds_ip,");
        sql.append("	rds.is_master,");
        sql.append("	ver.`name` AS  verName,");
        sql.append("	sto.`name` AS storeName,");
        sql.append("	prj.prj_name,");
        sql.append("	dc.dc_name");
        sql.append(" FROM cloud_rdsinstance rds ");
        sql.append(" LEFT JOIN cloud_project prj ON rds.prj_id = prj.prj_id");
        sql.append(" LEFT JOIN dc_datacenter dc ON dc.id = rds.dc_id");
        sql.append(" LEFT JOIN cloud_datastoreversion ver ON rds.version_id = ver.id");
        sql.append(" LEFT JOIN cloud_datastore sto ON ver.datastore_id = sto.id ");
        sql.append(" where 1=1 and rds.rds_id = ? and rds.is_deleted= '0' ");
        CloudRDSInstance instance = null;
        javax.persistence.Query query = alarmObjectDao.createSQLNativeQuery(sql.toString(), new Object []{instanceId});
        @SuppressWarnings("rawtypes")
        List list = new ArrayList();
        if(null!=query ){
            list = query.getResultList();
        }
        if(!list.isEmpty()&& list.size()==1){
            Object[] objs = (Object[]) list.get(0);
            instance = new CloudRDSInstance();
            instance.setRdsId(String.valueOf(objs[0]));
            instance.setRdsName(String.valueOf(objs[1]));
            instance.setPrjId(String.valueOf(objs[2]));
            instance.setDcId(String.valueOf(objs[3]));
            instance.setRdsIp(String.valueOf(objs[4]));
            instance.setIsMaster(String.valueOf(objs[5]));
            String datastoreName = MonitorAlarmUtil.transferDatastoreName(String.valueOf(objs[7]));
            instance.setVersion(datastoreName+String.valueOf(objs[6]));
            instance.setPrjName(String.valueOf(objs[8]));
            instance.setDcName(String.valueOf(objs[9]));
        }
        return instance;
    }
    /**
     * 负载均衡，报警对象属性
     * @Author: duanbinbin
     * @return pool_id	负载均衡id
     * @return pool_name	负载均衡名称
     * @return prj_id	项目id
     * @return dc_id	数据中心id
     * @return gateway_ip	受管子网IP
     * @return flo_ip	弹性公网IP
     * @return prj_name	项目名称
     * @return dc_name	数据中心名称
     * @return net_name	网络名称
     * @return vip_protocol	配置
     * @return protocol_port	配置端口
     * @return mode		负载均衡模式
     *<li>Date: 2017年3月6日</li>
     */
    private CloudLdPool getObjAttrByPoolId(String poolId,String mode) throws Exception {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT ");
        sql.append("	ld.pool_id,");
        sql.append("	ld.pool_name,");
        sql.append("	ld.prj_id,");
        sql.append("	ld.dc_id,");
        sql.append("	sub.gateway_ip,");
        sql.append("	flo.flo_ip,");
        sql.append("	prj.prj_name,");
        sql.append("	dc.dc_name,");
        sql.append("	net.net_name,");
        sql.append("	vip.vip_protocol,");
        sql.append("	vip.protocol_port,");
        sql.append("	ld.mode");
        sql.append(" FROM cloud_ldpool ld ");
        sql.append(" LEFT JOIN cloud_subnetwork sub ON ld.subnet_id = sub.subnet_id");
        sql.append(" LEFT JOIN cloud_floatip flo ON flo.resource_id = ld.pool_id AND flo.resource_type = 'lb' AND flo.is_deleted = '0'");
        sql.append(" LEFT JOIN cloud_project prj ON prj.prj_id = ld.prj_id");
        sql.append(" LEFT JOIN dc_datacenter dc ON dc.id = ld.dc_id");
        sql.append(" LEFT JOIN cloud_network net ON sub.net_id = net.net_id");
        sql.append(" LEFT JOIN cloud_ldvip vip ON ld.pool_id = vip.pool_id");
        sql.append(" where 1=1 and ld.pool_id = ? and ld.mode = ?");
        CloudLdPool ldpool = null;
        javax.persistence.Query query = alarmObjectDao.createSQLNativeQuery(sql.toString(), new Object []{poolId,mode});
        @SuppressWarnings("rawtypes")
        List list = new ArrayList();
        if(null!=query ){
            list = query.getResultList();
        }
        if(!list.isEmpty()&& list.size()==1){
            Object[] objs = (Object[]) list.get(0);
            ldpool = new CloudLdPool();
            ldpool.setPoolId(String.valueOf(objs[0]));
            ldpool.setPoolName(String.valueOf(objs[1]));
            ldpool.setPrjId(String.valueOf(objs[2]));
            ldpool.setDcId(String.valueOf(objs[3]));
            ldpool.setSubnetIp(String.valueOf(objs[4]));
            ldpool.setFloatIp(String.valueOf(objs[5]));
            ldpool.setPrjName(String.valueOf(objs[6]));
            ldpool.setDcName(String.valueOf(objs[7]));
            ldpool.setNetName(String.valueOf(objs[8]));
            ldpool.setPoolProtocol(String.valueOf(objs[9]));
            ldpool.setVipPort(Long.valueOf(String.valueOf(objs[10])));
            ldpool.setMode(mode);
        }
        return ldpool;
    }
    /**
     * 查询某一数据中心下的前三条未处理的报警信息
     * @Author: duanbinbin
     * @param sessionUser
     * @param object
     * @param prjId
     *<li>Date: 2017年3月6日</li>
     */
    @Override
    public void getUnhandledAlarmMsgList(SessionUserInfo sessionUser, JSONObject object, String prjId){
    	log.info("获取当前用户某一数据中心下的前三条未处理的报警信息列表开始");
        List<Object> paramList = new ArrayList<Object>();
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ")
          .append(" msg.am_id, ")
          .append(" msg.am_vmid, ")
          .append(" CASE WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_VM+"' THEN vm.vm_name ")
          .append(" WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_CLOUDDATA+"' THEN rds.rds_name ")
          .append(" WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDCOMMON+"' OR ")
          .append(" msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDMASTER+"' THEN ld.pool_name ")
          .append(" END AS objName, ")
  		  .append(" CASE WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_VM+"' THEN dc0.dc_name ")
          .append(" WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_CLOUDDATA+"' THEN dc1.dc_name ")
          .append(" WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDCOMMON+"' OR ")
          .append(" msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDMASTER+"' THEN dc2.dc_name ")
          .append(" END AS dcName, ")
          .append(" msg.am_alarmtype, ")
          .append(" msg.am_monitortype, ")
          .append(" msg.am_detail, ")
          .append(" msg.am_time, ")
          .append(" msg.am_isprocessed ")
          .append(" FROM ")
          .append(" ecsc_alarmmessage msg ");
        sb.append(" LEFT JOIN cloud_vm vm ON vm.vm_id = msg.am_vmid AND msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_VM+"' ");
		sb.append(" LEFT JOIN cloud_project prj0 on prj0.prj_id = vm.prj_id AND msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_VM+"' ");
		sb.append(" LEFT JOIN dc_datacenter dc0 on prj0.dc_id = dc0.id ");
		
		sb.append(" LEFT JOIN cloud_rdsinstance rds ON rds.rds_id = msg.am_vmid AND msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_CLOUDDATA+"' ");
    	sb.append(" LEFT JOIN cloud_project prj1 on prj1.prj_id = rds.prj_id AND msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_CLOUDDATA+"' ");
    	sb.append(" LEFT JOIN dc_datacenter dc1 on prj1.dc_id = dc1.id ");
    	
    	sb.append(" LEFT JOIN cloud_ldpool ld ON ld.pool_id = msg.am_vmid AND ( msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDCOMMON+"' OR ");
    	sb.append(" msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDMASTER+"' )");
    	sb.append(" LEFT JOIN cloud_project prj2 on prj2.prj_id = ld.prj_id AND ( msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDCOMMON+"' OR ");
    	sb.append(" msg.am_monitortype = '"+RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDMASTER+"' )");
    	sb.append(" LEFT JOIN dc_datacenter dc2 on prj2.dc_id = dc2.id ");
    	
    	sb.append("WHERE (prj0.prj_id = ? OR prj1.prj_id = ? OR prj2.prj_id = ?) ");
        paramList.add(prjId);
        paramList.add(prjId);
        paramList.add(prjId);
        sb.append(" AND msg.am_isprocessed = '0' ")
          .append(" ORDER BY msg.am_time DESC limit 3");
        javax.persistence.Query query = alarmMessageDao.createSQLNativeQuery(sb.toString(), paramList.toArray());
        List<Object[]> list = query.getResultList();
        List<AlarmMessage> unhandledAlarmMsgList = new ArrayList<AlarmMessage>();
        for(int i = 0;i < list.size();i++){
        	Object[] objs = list.get(i);
        	AlarmMessage msg =	new AlarmMessage();
        	msg.setId(String.valueOf(objs[0]));
            msg.setVmId(String.valueOf(objs[1]));
            msg.setVmName(String.valueOf(objs[2]));
            msg.setProjectName(String.valueOf(objs[3]));
            String aType = String.valueOf(objs[4]);
            msg.setAlarmType(aType);
            String mType = String.valueOf(objs[5]);
            msg.setMonitorType(mType);
            msg.setDetail(String.valueOf(objs[6]));
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timeStr = sdf.format(objs[7]);
            Date time = null;
            try {
                time = sdf.parse(timeStr);
            } catch (ParseException e) {
                throw new AppException(e.getMessage());
            }
            msg.setTime(time);
            msg.setAlarmTime(timeStr);
            
            String isProcessed = String.valueOf(objs[8]);
            String alarmSign = isProcessed.equals("0")?"未处理":"已处理";
            msg.setIsProcessed(isProcessed);
            msg.setAlarmSign(alarmSign);
            
            String aName = getMonitorItemByNodeID(aType);
            String mName = getMonitorItemByNodeID(mType);
            msg.setAlarmTypeName(aName);
            msg.setMonitorTypeName(mName);
            
            unhandledAlarmMsgList.add(msg);
        }
        object.put("unhandledAlarmMsgList",unhandledAlarmMsgList);
    }
    /**
     * 根据项目id查询客户的项目短信配额信息
     * @Author: duanbinbin
     * @param prjId
     * @return
     *<li>Date: 2017年3月3日</li>
     */
    @Override
    public Map<String, String> getCusInfoByPrjId(String prjId) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT prj.prj_id, prj.customer_id ,prj.sms_count FROM cloud_project prj WHERE prj.prj_id = ? ");
        Query query = alarmObjectDao.createSQLNativeQuery(sb.toString(), prjId);
        Object[] objs = (Object[]) query.getResultList().get(0);
        Map<String, String> map = new HashMap<String, String>();
        map.put("projectId", objs[0] == null ? "" : objs[0].toString());
        map.put("customerId", objs[1] == null ? "" : objs[1].toString());
        map.put("smsCount", objs[2] == null ? "" : objs[2].toString());
        return map;
}

    /**
     * 根据监控报警项ID查询最新的一条报警信息
     * @param id 监控报警项ID
     * @return
     */
    @Override
    public AlarmMessage getLatestAlarmMessageByMonitorAlarmItemId(String id) {
    	String hql = "from BaseAlarmMessage where monitorAlarmItemId = ? order by time desc ";
		List<BaseAlarmMessage> msgList = alarmMessageDao.find(hql, id);
		AlarmMessage msg = new AlarmMessage();
		if(msgList.size()>0){
			BaseAlarmMessage baseMsg = msgList.get(0);
			BeanUtils.copyProperties(baseMsg,msg);
			String alarmTime = DateUtil.dateToString(msg.getTime());
			msg.setAlarmTime(alarmTime);
		}
		return msg;
    }

    @Override
    public void syncEcscMonitor(){
        //1.获取alarmObject和alarmTrigger的key，并删除
        //2.读取数据库，完成缓存更新操作，注意数据量大的情况下的内存问题，可以考虑分页
        log.info("同步ECSC监控报警对象和触发条件开始");
        syncAlarmObject();
        syncAlarmTrigger();
    }

    private void syncAlarmObject() {
        try {
            //1.delete keys - alarmObject:[id]
            Set<String> keys = jedisUtil.keys(RedisKey.ALARM_OBJECT+"*");
            for(String key : keys){
                jedisUtil.delete(key);
            }
            keys.clear();
            //2.process alarmObject page by page
            StringBuilder sb = new StringBuilder();
            sb.append(" from BaseAlarmObject ");
            QueryMap qm = new QueryMap();
            int START_PAGE_NUMBER = 1;
            boolean hasNextPage = false;
            do{
                qm.setCURRENT_ROWS_SIZE(PAGE_SIZE);
                qm.setPageNum(START_PAGE_NUMBER);
                Page page = alarmObjectDao.pagedQuery(sb.toString(), qm);
                List<BaseAlarmObject> aoList = (List<BaseAlarmObject>) page.getResult();
                for(BaseAlarmObject ao : aoList){
                    syncAlarmObject(ao);
                }
                hasNextPage = START_PAGE_NUMBER < page.getTotalPageCount();
                START_PAGE_NUMBER++;
            }while(hasNextPage);

            log.info("同步ECSC报警对象成功");
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }

    }

    private void syncAlarmObject(BaseAlarmObject ao) throws Exception {
        String objectJSON = JSONObject.toJSONString(ao);
        try {
            jedisUtil.set(RedisKey.ALARM_OBJECT+ao.getId(), objectJSON);
        } catch (Exception e) {
            throw e;
        }
    }

    private void syncAlarmTrigger() {
        try {
            //1.delete keys
            Set<String> keys = jedisUtil.keys(RedisKey.ALARM_TRIGGER + "*");
            for(String key : keys){
                jedisUtil.delete(key);
            }
            keys.clear();
            //2.process alarmTrigger page by page
            StringBuilder sb = new StringBuilder();
            sb.append(" from BaseAlarmTrigger ");
            QueryMap qm = new QueryMap();
            int START_PAGE_NUMBER = 1;
            boolean hasNextPage = false;
            do{
                qm.setCURRENT_ROWS_SIZE(PAGE_SIZE);
                qm.setPageNum(START_PAGE_NUMBER);
                Page page = alarmTriggerDao.pagedQuery(sb.toString(), qm);
                List<BaseAlarmTrigger> atList = (List<BaseAlarmTrigger>) page.getResult();
                for(BaseAlarmTrigger at : atList){
                    syncAlarmTrigger(at);
                }
                hasNextPage = START_PAGE_NUMBER < page.getTotalPageCount();
                START_PAGE_NUMBER++;
            }while(hasNextPage);

            log.info("同步ECSC监控报警触发条件成功");
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
    }

    private void syncAlarmTrigger(BaseAlarmTrigger at) throws Exception {
        String objectJSON = JSONObject.toJSONString(at);
        try {
            jedisUtil.set(RedisKey.ALARM_TRIGGER + at.getId(), objectJSON);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 根据监控项类型查询报警类型列表
     * @Author: duanbinbin
     * @param monitorType
     * @return
     *<li>Date: 2017年3月2日</li>
     */
	@Override
	public List<MonitorMngData> getAlarmTypeByMonitor(String monitorType) {
		String alarmTypeParentId = MonitorAlarmUtil.getAlarmTypeParentId(monitorType);
		return getMonitorMngData(alarmTypeParentId);
	}
	
	/**
	 * 查询父节点下的子节点目录
	 * @Author: duanbinbin
	 * @param parentId
	 * @return
	 *<li>Date: 2017年3月2日</li>
	 */
	private List<MonitorMngData> getMonitorMngData(String parentId) {
        List<MonitorMngData> monitorMngList = new ArrayList<MonitorMngData>();
        Set<String> mngDataSet = null;
        List<String> mngDataList = new ArrayList<String>();
        try {
            mngDataSet = jedisUtil.getSet(RedisKey.SYS_DATA_TREE_PARENT_NODEID + parentId);
            for (String mngData : mngDataSet) {
                mngDataList.add(mngData);
            }
            Collections.sort(mngDataList);
            for (String mngData : mngDataList) {
                String jsonMng = jedisUtil.get(RedisKey.SYS_DATA_TREE + mngData);

                JSONObject monitorMng = JSONObject.parseObject(jsonMng);
                MonitorMngData monitorMngData = new MonitorMngData();
                monitorMngData.setName(monitorMng.getString("nodeName"));
                monitorMngData.setNameEN(monitorMng.getString("nodeNameEn"));
                monitorMngData.setNodeId(monitorMng.getString("nodeId"));
                monitorMngData.setParam1(monitorMng.getString("para1"));
                monitorMngData.setParam2(monitorMng.getString("para2"));
                monitorMngData.setParentId(monitorMng.getString("parentId"));

                monitorMngList.add(monitorMngData);
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            throw new AppException("查询redis数据异常：" + parentId);
        }
        return monitorMngList;
    }
	/**
	 * 删除成员
	 * @Author: duanbinbin
	 * @param memId
	 *<li>Date: 2017年3月6日</li>
	 */
	@Override
	public void clearExpAfterDeleteMember(String memId) {
		mongoTemplate.remove(new org.springframework.data.mongodb.core.query.Query(
    			Criteria.where("memberId").is(memId)), MongoCollectionName.MONITOR_LD_POOL_MEMBER_EXP);
	}
	/**
	 * 删除健康检查
	 * @Author: duanbinbin
	 * @param healthId
	 *<li>Date: 2017年3月6日</li>
	 */
	@Override
	public void clearExpAfterDeleteHealth(String healthId) {
		mongoTemplate.remove(new org.springframework.data.mongodb.core.query.Query(
    			Criteria.where("healthId").is(healthId)), MongoCollectionName.MONITOR_LD_POOL_MEMBER_EXP);
	}
	/**
	 * 解绑健康检查
	 * @Author: duanbinbin
	 * @param poolId
	 * @param healthId
	 *<li>Date: 2017年3月6日</li>
	 */
	@Override
	public void doAfterUnbundHealth(String poolId, String healthId) {
		org.springframework.data.mongodb.core.query.Query query=new org.springframework.data.mongodb.core.query.Query();
		query.addCriteria(Criteria.where("poolId").is(poolId));
		query.addCriteria(Criteria.where("healthId").is(healthId));
		mongoTemplate.updateMulti(query, new Update().set("isRepair", "0"), 	//修改所有符合条件的记录
        		 MongoCollectionName.MONITOR_LD_POOL_MEMBER_EXP);
	}
	/**
	 * 彻底删除资源时需要清除监控指标记录信息
	 * @Author: duanbinbin
	 * @param resourceType
	 * @param resourceId
	 *<li>Date: 2017年3月16日</li>
	 */
	@Override
	public void deleteMonitorByResource(String resourceType, String resourceId) {
		if(StringUtil.isEmpty(resourceType) || StringUtil.isEmpty(resourceId)){
			return;
		}
		try {
			JSONObject json = new JSONObject();
			json.put("type", resourceType);
			json.put("resourceId", resourceId);
			jedisUtil.addToSet(RedisKey.MONITOR_ITEM_DELETE, json.toJSONString());
			
			log.info("插入一条清除监控指标数据队列："+resourceType+",resourceId:"+resourceId);
		} catch (Exception e) {
			log.error("插入清除监控指标数据队列错误",e);
		}
	}

}