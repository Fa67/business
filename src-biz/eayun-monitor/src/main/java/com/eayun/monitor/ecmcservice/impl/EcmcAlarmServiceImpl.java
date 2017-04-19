package com.eayun.monitor.ecmcservice.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;
import javax.ws.rs.DELETE;

import com.eayun.monitor.model.*;
import com.eayun.virtualization.model.BaseCloudVm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.StringUtil;
import com.eayun.monitor.bean.EcmcConGroupBy;
import com.eayun.monitor.bean.MonitorAlarmUtil;
import com.eayun.monitor.bean.MonitorMngData;
import com.eayun.monitor.bean.QuotaInfo;
import com.eayun.monitor.dao.EcmcAlarmContactDao;
import com.eayun.monitor.dao.EcmcAlarmMessageDao;
import com.eayun.monitor.dao.EcmcAlarmObjectDao;
import com.eayun.monitor.dao.EcmcAlarmRuleDao;
import com.eayun.monitor.dao.EcmcAlarmTriggerDao;
import com.eayun.monitor.dao.EcmcContactDao;
import com.eayun.monitor.dao.EcmcContactGroupDao;
import com.eayun.monitor.ecmcservice.EcmcAlarmService;
import com.eayun.monitor.ecmcservice.EcmcMonitorAlarmService;

@Service
@Transactional
public class EcmcAlarmServiceImpl implements EcmcAlarmService {

    private static final Logger log = LoggerFactory.getLogger(EcmcAlarmServiceImpl.class);
    @Autowired
    private EcmcAlarmRuleDao ecmcAlarmRuleDao;
    @Autowired
    private EcmcAlarmObjectDao ecmcAlarmObjectDao;
    @Autowired
    private EcmcAlarmTriggerDao ecmcAlarmTriggerDao;
    @Autowired
    private EcmcAlarmContactDao ecmcAlarmContactDao;
    @Autowired
    private EcmcAlarmMessageDao ecmcAlarmMessageDao;
    @Autowired
    private EcmcContactDao ecmcContactDao;
    @Autowired
    private EcmcContactGroupDao ecmcContactGroupDao;
    @Autowired
    private EcmcMonitorAlarmService ecmcMonitorAlarmService;
    @Autowired
    private JedisUtil jedisUtil;
    @Autowired
    private MongoTemplate mongoTemplate;

    private static final int PAGE_SIZE = 5;

    /**
     * @Author: duanbinbin
     * @param page
     * @param queryMap
     * @param queryType
     * @param queryName
     * @param amType
     * @param isProcessed
     * @param dcName
     * @return
     * @throws AppException
     *<li>Date: 2017年3月2日</li>
     */
    @Override
    public Page getEcmcPageMsg(Page page, QueryMap queryMap, String queryType, String queryName, String amType,
                               String isProcessed, String dcName) throws AppException {
        log.info("查询运维报警信息列表");
        List<Object> list = new ArrayList<Object>();
        StringBuffer hql = new StringBuffer();
        hql.append("SELECT msg.ip,msg.am_id,msg.am_objectid,msg.am_monitortype,msg.am_detail,msg.am_time,msg.am_isprocessed,");
        
        hql.append(" CASE WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECMC_MONITOR_VM_NODEID+"' THEN vm.vm_name ")
        	.append(" WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECMC_MONITOR_TYPE_CLOUDDATA+"' THEN rds.rds_name ")
        	.append(" WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDCOMMON+"' OR ")
        	.append(" msg.am_monitortype = '"+RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDMASTER+"' THEN ld.pool_name ")
        	/*.append(" WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECMC_MONITOR_TYPE_API+"' THEN '----' ")*/
        	.append(" END AS objName, ");
        hql.append(" dc.dc_name AS dcName, ");
        hql.append(" cus.cus_org,prj.prj_name ");
        hql.append(" FROM ecmc_alarmmessage msg ");
        hql.append(" LEFT JOIN dc_datacenter dc ON msg.dc_id = dc.id ");
        hql.append(" LEFT JOIN sys_selfcustomer cus ON msg.cus_id = cus.cus_id ");
        hql.append(" LEFT JOIN cloud_project prj ON msg.prj_id = prj.prj_id ");
        
        hql.append(" LEFT JOIN cloud_vm vm ON vm.vm_id = msg.am_objectid AND msg.am_monitortype = '"+RedisNodeIdConstant.ECMC_MONITOR_VM_NODEID+"' ");
        hql.append(" LEFT JOIN cloud_rdsinstance rds ON rds.rds_id = msg.am_objectid AND msg.am_monitortype = '"+RedisNodeIdConstant.ECMC_MONITOR_TYPE_CLOUDDATA+"' ");
        hql.append(" LEFT JOIN cloud_ldpool ld ON ld.pool_id = msg.am_objectid AND ( msg.am_monitortype = '"+RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDCOMMON+"' OR ")
           .append(" msg.am_monitortype = '"+RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDMASTER+"' )");
        
        hql.append(" where 1 = 1 ");
        if (null != queryName && !queryName.trim().equals("")) {
            if (queryType.equals("obj")) {
            	queryName = queryName.replaceAll("\\_", "\\\\_");
                //根据对象名称模糊查询
                hql.append(" and CASE WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECMC_MONITOR_VM_NODEID+"' THEN vm.vm_name like ? ")
            	   .append(" WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECMC_MONITOR_TYPE_CLOUDDATA+"' THEN rds.rds_name like ? ")
            	   .append(" WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDCOMMON+"' OR ")
            	   .append(" msg.am_monitortype = '"+RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDMASTER+"' THEN ld.pool_name like ? ")
            	   /*.append(" WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECMC_MONITOR_TYPE_API+"' THEN '----' like ? ")*///不支持----查询
            	   .append(" END ");
                list.add("%" + queryName + "%");
                list.add("%" + queryName + "%");
                list.add("%" + queryName + "%");
                /*list.add("%" + queryName + "%");*/
            } else if (queryType.equals("cus")) {
                //根据所属客户模糊查询
                String[] cusOrgs = queryName.split(",");
                hql.append(" and ( ");
                for (String org : cusOrgs) {
                    hql.append(" binary cus.cus_org like concat('%',?,'%') or ");
                    list.add(org);
                }
                hql.append(" 1 = 2 ) ");

            } else if (queryType.equals("pro")) {
                //根据项目名称精确查询
                String[] prjName = queryName.split(",");
                hql.append(" and ( ");
                for (String prj : prjName) {
                    hql.append(" binary prj.prj_name like concat('%',?,'%') or ");
                    list.add(prj);
                }
                hql.append(" 1 = 2 ) ");
            } else if (queryType.equals("IP")) {
                //根据客户端IP精确查询
                String[] ipInfos = queryName.split(",");
                hql.append(" and ( ");
                for (String ip : ipInfos) {
                    hql.append(" binary msg.ip = ? or ");
                    list.add(ip);
                }
                hql.append(" 1 = 2 ) ");
            } else {
                hql.append(" and  1 = 2 ");
            }
        }
        if (null != amType && !amType.trim().equals("")) {
            hql.append(" and binary msg.am_monitortype = ?");
            list.add(amType);
        }
        if (null != isProcessed && !isProcessed.trim().equals("")) {
            hql.append(" and binary msg.am_isprocessed = ?");
            list.add(isProcessed);
        }
        if (null != dcName && !dcName.trim().equals("")) {
            if (!"----".equals(dcName)) {
                hql.append(" and binary dc.dc_name = ?");
                list.add(dcName);
            }else {
                hql.append(" and binary msg.dc_id = '-'");
            }
        }
        hql.append(" ORDER BY msg.am_time DESC ");
        
        page = ecmcAlarmMessageDao.pagedNativeQuery(hql.toString(), queryMap, list.toArray());
        List datalist = (List) page.getResult();
        for (int i = 0; i < datalist.size(); i++) {
            Object[] obj = (Object[]) datalist.get(i);
            EcmcAlarmMessage message = new EcmcAlarmMessage();
            message.setIp(String.valueOf(obj[0]));
            message.setId(String.valueOf(obj[1]));
            message.setObjId(String.valueOf(obj[2]));
            
            String monitorType = String.valueOf(obj[3]);
            message.setMonitorType(monitorType);
            message.setMonitorTypeName(this.getNodeNameByNodeID(monitorType));
            
            message.setDetail(String.valueOf(obj[4]));
            message.setTime((Date) obj[5]);
            message.setIsProcessed(String.valueOf(obj[6]));
            message.setObjName(String.valueOf(obj[7]));
            message.setDcName(String.valueOf(obj[8]));
            message.setCusName(String.valueOf(obj[9]));
            message.setProjectName(String.valueOf(obj[10]));
            datalist.set(i, message);
        }
        return page;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int getEcmcUnMsgCount(String cusId, String prjId) {
        log.info("查询所有未处理的运维报警信息数量");
        List<Object> plist = new ArrayList<Object>();
        int count = 0;
        StringBuffer hql = new StringBuffer("SELECT COUNT(id) FROM BaseEcmcAlarmMessage WHERE isProcessed ='0'");
        if(null != cusId && !"".equals(cusId)){
        	hql.append("and cusId = ? ");
        	plist.add(cusId);
        	if(null != prjId && !"".equals(prjId)){
            	hql.append(" and prjId = ? ");
            	plist.add(prjId);
            }
        }
        
        List<Long> list = ecmcAlarmMessageDao.find(hql.toString(),plist.toArray());
        count = list.get(0).intValue();
        return count;
    }

    @Override
    public Page getEcmcPageRule(Page page, QueryMap queryMap, String name,
                                String monitorItem) {
        log.info("获取所有运维报警规则列表");
        List<Object> list = new ArrayList<Object>();
        StringBuffer hql = new StringBuffer("FROM BaseEcmcAlarmRule WHERE 1 = 1 ");
        if (null != name && !name.trim().equals("")) {
            name = name.replaceAll("\\_", "\\\\_");
            hql.append(" and name like ?");
            list.add("%" + name + "%");
        }
        if (null != monitorItem && !monitorItem.trim().equals("")) {
            hql.append(" and monitorItem = ?");
            list.add(monitorItem);
        }
        hql.append(" order by modifyTime desc ");
        page = ecmcAlarmRuleDao.pagedQuery(hql.toString(), queryMap, list.toArray());
        List rulelist = (List) page.getResult();
        for (int i = 0; i < rulelist.size(); i++) {
            BaseEcmcAlarmRule baseAlarmRule = new BaseEcmcAlarmRule();
            baseAlarmRule = (BaseEcmcAlarmRule) rulelist.get(i);
            EcmcAlarmRule alarmRule = new EcmcAlarmRule();
            BeanUtils.copyPropertiesByModel(alarmRule, baseAlarmRule);

            alarmRule.setTriggerCondition(getTriggerConditionString(alarmRule.getId()));
            alarmRule.setAlarmObjectNumber(getEcmcAlarmObjCount(alarmRule.getId()));
            alarmRule.setMonitorItemName(getNodeNameByNodeID(alarmRule.getMonitorItem()));

            rulelist.set(i, alarmRule);
        }
        return page;
    }

    /**
     * 查询报警规则下的报警对象数量
     *
     * @param ruleId
     * @return
     */
    private int getEcmcAlarmObjCount(String ruleId) {
        StringBuffer hql = new StringBuffer();
        hql.append("SELECT COUNT(id) from BaseEcmcAlarmObject where alarmRuleId = ?");
        List<Long> list = ecmcAlarmObjectDao.find(hql.toString(), ruleId);
        return list.get(0).intValue();
    }

    /**
     * 根据数据字典的nodeId在数据字典中获取其名称
     *
     * @param nodeId
     * @return
     */
    private String getNodeNameByNodeID(String nodeId) {
        String monitorItem = null;
        try {
            String monitorItemStr = jedisUtil.get(RedisKey.SYS_DATA_TREE + nodeId);
            JSONObject monitorItemJSON = JSONObject.parseObject(monitorItemStr);

            monitorItem = monitorItemJSON.getString("nodeName");
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
        return monitorItem;
    }

    /**
     * 根据报警规则ID查询触发条件字符串
     *
     * @param ruleId
     * @return
     */
    @SuppressWarnings({"unchecked"})
    private String getTriggerConditionString(String ruleId) {
        StringBuffer sb = new StringBuffer();
        String sql = " from BaseEcmcAlarmTrigger where alarmRuleId=?";
        List<BaseEcmcAlarmTrigger> baseEcmcTriggerList = ecmcAlarmTriggerDao.find(sql, ruleId);
        for (BaseEcmcAlarmTrigger trigger : baseEcmcTriggerList) {
            String thresholdString = null ;
            if ("0010003003003".equals(trigger.getZb())){
                //若为请求次数，应该显示整数
                thresholdString = String.valueOf((int)trigger.getThreshold()) ;
            }else if ("0010003003004".equals(trigger.getZb())){
                thresholdString = trigger.getThreshold() + "0" ;
            }else {
                thresholdString = String.valueOf(trigger.getThreshold()) ;
            }
            sb.append(getNodeNameByNodeID(trigger.getZb()))
                    .append(trigger.getOperator())
                    .append(thresholdString)
                    .append(trigger.getUnit())
                    .append("持续" + getLastTime(trigger.getLastTime()))
                    .append(";");
        }
        return sb.toString();
    }

    /**
     * 持续时间
     *
     * @param lastTime
     * @return
     */
    private String getLastTime(int lastTime) {
        if (lastTime >= 60 && lastTime < 3600) {
            return lastTime / 60 + "分钟";
        } else {
            return lastTime / 3600 + "小时";
        }
    }

    /**
     * 复制一份报警规则（只能复制触发条件，不能复制联系人和报警对象）
     * 1.在规则表中插入一条记录，名称为：“复制_”+原名称
     * 2.查出原规则关联的触发条件
     * 3.新规则关联触发条件，同时将触发条件写入redis
     *
     * @return
     */
    @Override
    public EcmcAlarmRule copyEcmcAlarmRule(EcmcAlarmRule ecmcAlarmRule) {
        log.info("复制一条运维报警规则");
        EcmcAlarmRule newRule = new EcmcAlarmRule();
        String newName = "复制_" + ecmcAlarmRule.getName();
        newRule.setName(newName.length() <= 20 ? newName : newName.substring(0, 20));
        newRule.setModifyTime(new Date());
        newRule.setMonitorItem(ecmcAlarmRule.getMonitorItem());

        BaseEcmcAlarmRule newBaseRule = new BaseEcmcAlarmRule();
        BeanUtils.copyPropertiesByModel(newBaseRule, newRule);
        ecmcAlarmRuleDao.saveEntity(newBaseRule);
        BeanUtils.copyPropertiesByModel(newRule, newBaseRule);

        StringBuffer sb = new StringBuffer();
        sb.append(" from BaseEcmcAlarmTrigger where alarmRuleId = ?");
        List<BaseEcmcAlarmTrigger> triggerList = ecmcAlarmTriggerDao.find(sb.toString(), ecmcAlarmRule.getId());
        for (BaseEcmcAlarmTrigger oldTrigger : triggerList) {
            BaseEcmcAlarmTrigger newBaseTrigger = new BaseEcmcAlarmTrigger();

            newBaseTrigger.setAlarmRuleId(newRule.getId());
            newBaseTrigger.setIsTriggered(oldTrigger.getIsTriggered());//mydoing?是否已触发
            newBaseTrigger.setLastTime(oldTrigger.getLastTime());
            newBaseTrigger.setOperator(oldTrigger.getOperator());
            newBaseTrigger.setThreshold(oldTrigger.getThreshold());
            newBaseTrigger.setUnit(oldTrigger.getUnit());
            newBaseTrigger.setZb(oldTrigger.getZb());

            addEcmcAlarmTrigger(newBaseTrigger);
        }
        return newRule;
    }

    /**
     * 添加运维报警规则的触发条件
     *
     * @return
     * @throws AppException
     */
    public BaseEcmcAlarmTrigger addEcmcAlarmTrigger(BaseEcmcAlarmTrigger ecmcBaseTrigger) throws AppException {
        log.info("添加运维报警规则的触发条件");
        ecmcAlarmTriggerDao.saveEntity(ecmcBaseTrigger);

        String triggerJSON = JSONObject.toJSONString(ecmcBaseTrigger);
        try {
            jedisUtil.set(RedisKey.ECMC_ALARM_TRIGGER + ecmcBaseTrigger.getId(), triggerJSON);
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
        return ecmcBaseTrigger;
    }

    /**
     * 删除运维报警规则
     * 1.删除规则关联的触发条件
     * 2.删除规则关联的报警对象
     * 3.删除规则关联的联系人
     * 4.删除规则记录
     * 5.删除规则对应的监控报警项
     * @param ruleId
     * @return
     */
    @Override
    public boolean deleteEcmcAlarmRule(String ruleId) {
        log.info("删除运维报警规则");
        this.deleteAllTriggersByRuleId(ruleId);	//删除规则的触发条件数据记录及redis
        this.deleteAllContactsByRuleId(ruleId);	//删除规则和联系人关联记录
        this.deleteAllObjectsByRuleId(ruleId);	//删除规则的报警对象及每个对象产生的报警信息
        this.deleteApiAlarmMessage(ruleId);		//删除API报警信息

        ecmcAlarmRuleDao.delete(ruleId);		//删除规则
        ecmcMonitorAlarmService.deleteMonAlarmItemByRuleId(ruleId);	//删除监控报警项
        
        return true;
    }

    /**
     * 根据运维报警规则id删除触发条件数据记录及redis信息
     * @param ruleId
     * @return
     * @throws AppException
     */
    @SuppressWarnings("unchecked")
    public boolean deleteAllTriggersByRuleId(String ruleId) throws AppException {
        String triggerSQL = " from BaseEcmcAlarmTrigger where alarmRuleId = ?";
        List<BaseEcmcAlarmTrigger> triggerList = ecmcAlarmTriggerDao.find(triggerSQL, ruleId);
        for (BaseEcmcAlarmTrigger trigger : triggerList) {
            try {
                jedisUtil.delete(RedisKey.ECMC_ALARM_TRIGGER + trigger.getId());
                ecmcAlarmTriggerDao.delete(trigger);
            } catch (Exception e) {
                throw new AppException(e.getMessage());
            }
        }
        return true;
    }

    /**
     * 根据运维报警规则id删除所有的规则-联系人关联记录
     * @param ruleId
     * @return
     * @throws AppException
     */
    public boolean deleteAllContactsByRuleId(String ruleId) throws AppException {
        String hql = " delete BaseEcmcAlarmContact where alarmRuleId=?";
        ecmcAlarmContactDao.executeUpdate(hql, ruleId);
        return true;
    }

    /**
     * 删除规则关联的所有报警对象
     * @param ruleId
     * @return
     * @throws AppException
     */
    @SuppressWarnings("unchecked")
    public boolean deleteAllObjectsByRuleId(String ruleId) throws AppException {
        String sql = " from BaseEcmcAlarmObject where alarmRuleId=?";
        List<BaseEcmcAlarmObject> objectList = ecmcAlarmObjectDao.find(sql, ruleId);
        boolean isDeleted = true;
        for (BaseEcmcAlarmObject baseObject : objectList) {
            EcmcAlarmObject alarmObject = new EcmcAlarmObject();
            BeanUtils.copyPropertiesByModel(alarmObject, baseObject);
            isDeleted &= deleteAlarmObject(alarmObject);	//单个报警对象删除
        }
        return isDeleted;
    }

    /***
     * 删除单个运维报警对象：
     * 1.删除报警对象的redis信息
     * 2.删除报警对象的数据库记录
     * 3.删除对象的监控报警项
     * 4.删除对象的报警信息
     * @param alarmObject
     * @return
     * @throws AppException
     */
    public boolean deleteAlarmObject(EcmcAlarmObject alarmObject) throws AppException {
        log.info("删除单个报警对象");
        boolean isDeleted = false;
        try {
            isDeleted = jedisUtil.delete(RedisKey.ECMC_ALARM_OBJECT + alarmObject.getId());
            if (isDeleted) {
                ecmcAlarmObjectDao.delete(alarmObject.getId());
            }
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
        ecmcMonitorAlarmService.deleteMonAlarmItemByAlarmObject(alarmObject);
        this.deleteAlarmMessage(alarmObject);
        return isDeleted;
    }

    /**
     * 删除某报警对象产生的报警信息
     * @param alarmObject
     */
    private void deleteAlarmMessage(EcmcAlarmObject alarmObject) {
        String sql = " delete BaseEcmcAlarmMessage where objId = ? and alarmRuleId = ?";
        ecmcAlarmMessageDao.executeUpdate(sql, alarmObject.getAoObjectId(), alarmObject.getAlarmRuleId());
    }
    /**
     * 删除API产生的报警信息
     * @param ruleId    报警规则ID
     */
    private void deleteApiAlarmMessage(String ruleId) {
        String sql = " from BaseEcmcAlarmMessage where alarmRuleId = ? and monitorType = '"+RedisNodeIdConstant.ECMC_MONITOR_TYPE_API+"'";
        List<BaseEcmcAlarmMessage> msgList = ecmcAlarmMessageDao.find(sql, ruleId);
        for (BaseEcmcAlarmMessage baseAlarmMessage : msgList) {
            ecmcAlarmMessageDao.delete(baseAlarmMessage);
            //删除数据过程应该与消除报警的操作内容类似，需要消除对应的报警信息，同时删除报警信息
            String redisKeyString = RedisKey.API_MONITORINGALARM_ISNEEDNOTIFY + baseAlarmMessage.getCusId() + ":" + baseAlarmMessage.getIp() + ":" + baseAlarmMessage.getDcId() + ":" + baseAlarmMessage.getAm_alarmtriggerid() ;
            try {
                jedisUtil.delete(redisKeyString);
            }catch (Exception e){
                log.error(e.getMessage(), e);
            }
        }
    }
    /**
	 * @Author: duanbinbin
	 * @param ecmcAlarmRule
	 * @param triggerConditionList
	 * @return
	 *<li>Date: 2017年3月2日</li>
	 */
    @Override
    public EcmcAlarmRule addEcmcAlarmRule(EcmcAlarmRule ecmcAlarmRule,
                                          List<Map> triggerConditionList) {
        log.info("添加运维报警规则记录");
        BaseEcmcAlarmRule baseRule = new BaseEcmcAlarmRule();
        BeanUtils.copyPropertiesByModel(baseRule, ecmcAlarmRule);
        ecmcAlarmRuleDao.saveEntity(baseRule);
        BeanUtils.copyPropertiesByModel(ecmcAlarmRule, baseRule);
        if (null == triggerConditionList || triggerConditionList.isEmpty()) {
            return ecmcAlarmRule;
        }
        for (Map triggerMap : triggerConditionList) {
            if (triggerMap.size() > 0) {
                BaseEcmcAlarmTrigger trigger = new BaseEcmcAlarmTrigger();
                trigger.setIsTriggered("0");
                trigger.setAlarmRuleId(baseRule.getId());
                trigger.setLastTime(Integer.valueOf(triggerMap.get("lastTime").toString()));
                trigger.setOperator(triggerMap.get("operator").toString());
                float threshold = Float.valueOf(triggerMap.get("threshold").toString());
                if (threshold < 0.0) {
                    threshold *= -1;
                }
                trigger.setThreshold(threshold);
                trigger.setZb(triggerMap.get("zb").toString());
                trigger.setUnit(triggerMap.get("unit").toString());
                //todo?	监控指标单位可获取
                this.addEcmcAlarmTrigger(trigger);
            }
        }
        return ecmcAlarmRule;
    }

    @Override
    public List<MonitorMngData> getEcmcItemList(String parentId) {
        log.info("获取所有运维监控项的类型");
        return this.getMonitorMngData(parentId);
    }

    /**
     * 查询mongo基础数据的数据字典
     * 查询父节点下的所有子节点信息
     * @param parentId
     * @return
     */
    public List<MonitorMngData> getMonitorMngData(String parentId) {
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

    @Override
    public List<MonitorMngData> getZbListByItem(String parentId) {
        log.info("获取监控项下所有监控指标");
        return this.getMonitorMngData(parentId);
    }

    @Override
    public List<MonitorMngData> getEcmcTriggerOperator(String parentId) {
        log.info("获取触发条件操作符");
        return this.getMonitorMngData(parentId);
    }

    @Override
    public List<MonitorMngData> getEcmcTriggerTimes(String parentId) {
        log.info("获取触发条件持续时间");
        return this.getMonitorMngData(parentId);
    }

    @Override
    public EcmcAlarmRule getEcmcRuleById(String alarmRuleId) {
        log.info("根据规则ID查询报警规则");
        StringBuffer sb = new StringBuffer();
        sb.append(" from BaseEcmcAlarmRule where id= ? ");
        List<BaseEcmcAlarmRule> list = ecmcAlarmRuleDao.find(sb.toString(), alarmRuleId);
        BaseEcmcAlarmRule baseEcmcAlarmRule = list.get(0);

        EcmcAlarmRule ecmcRule = new EcmcAlarmRule();
        BeanUtils.copyPropertiesByModel(ecmcRule, baseEcmcAlarmRule);
        ecmcRule.setAlarmObjectNumber(this.getEcmcAlarmObjCount(alarmRuleId));			//规则下的报警对象数量
        ecmcRule.setTriggerCondition(this.getTriggerConditionString(alarmRuleId));		//规则的触发条件字符串
        ecmcRule.setMonitorItemName(this.getNodeNameByNodeID(ecmcRule.getMonitorItem()));//规则的监控项名称

        return ecmcRule;
    }

    @Override
    public EcmcAlarmRule updateEcmcRule(EcmcAlarmRule ecmcAlarmRule,
                                        List<Map> triggerConditionList) {
        log.info("编辑运维报警规则");
        BaseEcmcAlarmRule baseEcmcRule = new BaseEcmcAlarmRule();
        BeanUtils.copyPropertiesByModel(baseEcmcRule, ecmcAlarmRule);
        ecmcAlarmRuleDao.saveOrUpdate(baseEcmcRule);

        //针对监控规则的触发条件，采取全删全增的方式
        boolean isClear = this.deleteAllTriggersByRuleId(ecmcAlarmRule.getId());
        if (isClear) {
            for (Map triggerMap : triggerConditionList) {
                if (triggerMap.size() > 0) {
                    BaseEcmcAlarmTrigger baseTrigger = new BaseEcmcAlarmTrigger();
                    baseTrigger.setIsTriggered("0");
                    baseTrigger.setAlarmRuleId(ecmcAlarmRule.getId());
                    baseTrigger.setLastTime(Integer.valueOf(triggerMap.get("lastTime").toString()));
                    baseTrigger.setOperator(triggerMap.get("operator").toString());
                    float threshold = Float.valueOf(triggerMap.get("threshold").toString());
                    if (threshold < 0.0) {
                        threshold *= -1;
                    }
                    baseTrigger.setThreshold(threshold);
                    baseTrigger.setZb(triggerMap.get("zb").toString());
                    baseTrigger.setUnit(triggerMap.get("unit").toString());

                    this.addEcmcAlarmTrigger(baseTrigger);
                }
            }
        }
        //编辑报警规则需要同时更新该规则的监控报警项。
        ecmcMonitorAlarmService.updateEcmcMonItemByRuleId(ecmcAlarmRule.getId());
        return ecmcAlarmRule;
    }

    /**
     * 查询某运维报警规则下的运维报警对象分页列表
     * 用于详情页的分页列表
     * @Author: duanbinbin
     * @param ruleId
     * @param monitorType	监控项类型
     * @param queryMap
     * @return
     *<li>Date: 2017年3月2日</li>
     */
    @Override
    public Page getEcmcObjPageInRule(String ruleId,String monitorType,QueryMap queryMap) {
        log.info("查询某运维报警规则下的运维报警对象分页列表");
        Page page=new Page();
        List<EcmcAlarmObject> ecmcObjectList = new ArrayList<EcmcAlarmObject>();
        List<Object> paramList = new ArrayList<>();
        StringBuffer sb = new StringBuffer("SELECT 	a.ao_id AS id,	a.ao_alarmruleid AS alarmRuleId, ");
        sb.append("	a.ao_type AS aoType, ")
          .append("	a.ao_objectid AS aoObjectId, ")
          .append("	a.cus_id AS cusId, ")
          .append("	a.prj_id AS prjId, ")
          .append("	a.dc_id AS dcId ")
          .append(" FROM 	ecmc_alarmobject a ");
        sb.append(" where a.ao_alarmruleid = ? AND a.ao_type = ?");
        paramList.add(ruleId);
        paramList.add(monitorType);
        page = ecmcAlarmObjectDao.pagedNativeQuery(sb.toString(), queryMap,paramList.toArray());
        
        List<Object> list = (List<Object>) page.getResult();
        for(int i=0;i<list.size();i++){
        	 Object[] obj = (Object[]) list.get(i);
        	 EcmcAlarmObject ecmcObj = new EcmcAlarmObject();
        	 ecmcObj.setId(String.valueOf(obj[0]));
        	 ecmcObj.setAlarmRuleId(String.valueOf(obj[1]));
        	 ecmcObj.setAoType(String.valueOf(obj[2]));
        	 ecmcObj.setAoObjectId(String.valueOf(obj[3]));
        	 ecmcObj.setCusId(String.valueOf(obj[4]));
        	 ecmcObj.setPrjId(String.valueOf(obj[5]));
        	 ecmcObj.setDcId(String.valueOf(obj[6]));
        	 ecmcObj = this.getAttributesForObj(ecmcObj);
        	 if(ecmcObj.getIsDeleted()){		//如遇到资源已删除的垃圾数据，做清除处理
             	log.warn("该运维报警对象的资源已被删除，请清查垃圾数据，type:"+ecmcObj.getAoType()+",resourceId:"+ecmcObj.getAoObjectId()+",objectId:"+ecmcObj.getId());
             	if(ecmcObj.getAoType().equals(RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDCOMMON)
             			||ecmcObj.getAoType().equals(RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDMASTER)){
             		this.clearPoolMsgAfterDeletePool(ecmcObj.getAoObjectId());			//清除负载均衡报警相关
             	}else{
             		this.cleanAlarmDataAfterDeletingObject(ecmcObj.getAoObjectId());	//清除主机或数据库实例报警相关
             	}
             	continue;
             }
             ecmcObjectList.add(ecmcObj);
        }
        page.setResult(ecmcObjectList);
        return page;
    }
    
    /**
     * 查询某运维报警规则下的运维报警对象列表(区分监控项类型)
     * 用于已关联对象列表和添加时显示右侧已选择对象列表
     * @Author: duanbinbin
     * @param ruleId
     * @param monitorType
     * @return
     *<li>Date: 2017年3月2日</li>
     */
    @Override
    public List<EcmcAlarmObject>  getEcmcObjListInRule(String ruleId, String monitorType) {
        log.info("查询某运维报警规则下的运维报警对象列表");
        List<EcmcAlarmObject> ecmcObjectList = new ArrayList<EcmcAlarmObject>();
        StringBuffer sb = new StringBuffer();
        sb.append(" from BaseEcmcAlarmObject where alarmRuleId = ? and aoType = ? ");
        List<BaseEcmcAlarmObject> baseEcmcObjectList = ecmcAlarmObjectDao.find(sb.toString(), ruleId,monitorType);
        for (BaseEcmcAlarmObject baseObject : baseEcmcObjectList) {
            EcmcAlarmObject ecmcObj = new EcmcAlarmObject();
            BeanUtils.copyPropertiesByModel(ecmcObj, baseObject);
            //赋给运维报警对象model属性信息
            ecmcObj = this.getAttributesForObj(ecmcObj);
            if(ecmcObj.getIsDeleted()){		//如遇到资源已删除的垃圾数据，做清除处理
             	log.warn("该运维报警对象的资源已被删除，请清查垃圾数据，type:"+ecmcObj.getAoType()+",resourceId:"+ecmcObj.getAoObjectId()+",objectId:"+ecmcObj.getId());
             	if(ecmcObj.getAoType().equals(RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDCOMMON)
             			||ecmcObj.getAoType().equals(RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDMASTER)){
             		this.clearPoolMsgAfterDeletePool(ecmcObj.getAoObjectId());			//清除负载均衡报警相关
             	}else{
             		this.cleanAlarmDataAfterDeletingObject(ecmcObj.getAoObjectId());	//清除主机或数据库实例报警相关
             	}
             	continue;
             }
            ecmcObjectList.add(ecmcObj);
        }
        return ecmcObjectList;
    }

    /**
     * 查询某客户某项目不在某运维报警规则下的运维报警对象列表
     * 用于添加报警对象时的左侧列表
     * dcId、prjId、cusId已赋值
     * @Author: duanbinbin
     * @param ruleId
     * @param cusId
     * @param prjId
     * @param monitorType
     * @return
     *<li>Date: 2017年3月2日</li>
     */
    @Override
    public List<EcmcAlarmObject> getEcmcObjListOutRule(String ruleId,String cusId, 
    		String prjId,String monitorType) {
        log.info("查询不在某运维报警规则下的运维报警对象列表");
        List<EcmcAlarmObject> ecmcObjectList = new ArrayList<EcmcAlarmObject>();
        BaseEcmcAlarmRule baseRule = ecmcAlarmRuleDao.findOne(ruleId);
        String itemType = baseRule.getMonitorItem();//监控项
        List<Object> paramList = new ArrayList<Object>();
        if (itemType.equals(RedisNodeIdConstant.ECMC_MONITOR_VM_NODEID)) {
        	//ECMC监控项-云主机
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT vm.vm_id,vm.vm_name,vm.vm_ip,vm.prj_id,vm.dc_id,prj.customer_id, ");
            sql.append("net.net_name,ip.flo_ip, vm.self_ip FROM cloud_vm vm ");
            sql.append("LEFT JOIN cloud_network net ON vm.net_id = net.net_id ");
            sql.append("LEFT JOIN cloud_floatip ip ON vm.vm_id = ip.resource_id ");
            sql.append("LEFT JOIN cloud_project prj ON vm.prj_id = prj.prj_id ");
            sql.append("WHERE vm.is_deleted = '0' and vm.is_visable = '1' ");
            sql.append(" AND prj.customer_id = ? ");
            paramList.add(cusId);
            sql.append(" AND vm.prj_id = ? ");
            paramList.add(prjId);

            sql.append(" AND vm.vm_id NOT IN ( ");
            sql.append(" SELECT obj.ao_objectid FROM ecmc_alarmobject obj ");
            sql.append(" WHERE obj.ao_alarmruleid = ? and obj.ao_type = ? ");
            sql.append(" )");
            paramList.add(ruleId);
            paramList.add(itemType);
            sql.append(" ORDER BY vm.create_time DESC");

            javax.persistence.Query query = ecmcAlarmRuleDao.createSQLNativeQuery(sql.toString(), paramList.toArray());
            List list = query.getResultList();
            for (int i = 0; i < list.size(); i++) {
                Object[] obj = (Object[]) list.get(i);
                EcmcAlarmObject ecmcObj = new EcmcAlarmObject();
                ecmcObj.setAlarmRuleId(ruleId);
                ecmcObj.setAoType(itemType);
                ecmcObj.setAoObjectId(String.valueOf(obj[0]));
                ecmcObj.setObjName(String.valueOf(obj[1]));
                ecmcObj.setVmIp(String.valueOf(obj[2]));
                ecmcObj.setPrjId(String.valueOf(obj[3]));
                ecmcObj.setDcId(String.valueOf(obj[4]));
                ecmcObj.setCusId(String.valueOf(obj[5]));
                ecmcObj.setNetwork(String.valueOf(obj[6]));
                ecmcObj.setFloatIp(String.valueOf(obj[7]));
                ecmcObj.setSelfSubIp(String.valueOf(obj[8]));
                ecmcObjectList.add(ecmcObj);
            }
        }else if(itemType.equals(RedisNodeIdConstant.ECMC_MONITOR_TYPE_CLOUDDATA)){
        	//ECMC监控项-数据库实例
        	StringBuffer sql = new StringBuffer();
        	sql.append("SELECT ")
              .append("	rds.rds_id,")
              .append(" rds.rds_name,")
              .append("	rds.dc_id,")
              .append(" rds.prj_id,")
              .append(" rds.rds_ip,")
              .append(" prj.customer_id")
              .append(" FROM cloud_rdsinstance rds ")
              .append(" LEFT JOIN cloud_project prj ON rds.prj_id = prj.prj_id ")
              .append(" WHERE rds.is_deleted = '0' AND rds.is_visible = '1' ")
              .append(" AND rds.prj_id  = ? ")
              .append(" AND prj.customer_id  = ? ")
              .append(" AND rds.rds_id NOT IN (")
              .append(" SELECT obj.ao_objectid FROM ecmc_alarmobject obj ")
              .append(" WHERE obj.ao_alarmruleid = ? and obj.ao_type = ? ")
              .append(") ");
            paramList.add(prjId);
            paramList.add(cusId);
            paramList.add(ruleId);
            paramList.add(itemType);
            sql.append(" ORDER BY rds.create_time DESC");
            Query query = ecmcAlarmRuleDao.createSQLNativeQuery(sql.toString(), paramList.toArray());
            List resultList = query.getResultList();
            for(int i=0; i<resultList.size(); i++){
                Object[] obj = (Object[]) resultList.get(i);
                EcmcAlarmObject ecmcObj = new EcmcAlarmObject();
                ecmcObj.setAlarmRuleId(ruleId);
                ecmcObj.setAoType(itemType);
                ecmcObj.setAoObjectId(String.valueOf(obj[0]));
                ecmcObj.setObjName(String.valueOf(obj[1]));
                ecmcObj.setDcId(String.valueOf(obj[2]));
                ecmcObj.setPrjId(String.valueOf(obj[3]));
                ecmcObj.setVmIp(String.valueOf(obj[4]));
                ecmcObj.setCusId(String.valueOf(obj[5]));
                ecmcObjectList.add(ecmcObj);
            }
        	
        }else if(itemType.equals(RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDCOMMON) || 
        		itemType.equals(RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDMASTER)){
        	//ECMC监控项-负载均衡(普通或主备模式)
    		String mode = MonitorAlarmUtil.LDPOOL_MODE_MASTER;
    		if(itemType.equals(RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDCOMMON)){
    			mode = MonitorAlarmUtil.LDPOOL_MODE_COMMON;
    		}
        	StringBuffer sql = new StringBuffer();
        	sql.append("SELECT ")
            .append("	ld.pool_id,")
            .append("	ld.pool_name,")
            .append("	ld.dc_id,")
            .append("	ld.prj_id,")
            .append("	sub.gateway_ip,")
            .append(" 	prj.customer_id,")
            .append("	flo.flo_ip")
            .append(" FROM cloud_ldpool ld ")
            .append(" LEFT JOIN cloud_subnetwork sub ON ld.subnet_id = sub.subnet_id ")
            .append(" LEFT JOIN cloud_project prj ON ld.prj_id = prj.prj_id ")
            .append(" LEFT JOIN cloud_floatip flo ON ld.pool_id = flo.resource_id AND flo.resource_type = 'lb' AND flo.is_deleted = '0'")
            .append(" WHERE ld.is_visible = '1' and ld.mode = ? ")
            .append(" AND ld.prj_id  = ? ")
            .append(" AND prj.customer_id  = ? ")
            .append(" AND ld.pool_id NOT IN (")
            .append(" SELECT obj.ao_objectid FROM ecmc_alarmobject obj ")
            .append(" WHERE obj.ao_alarmruleid = ? and obj.ao_type = ? ")
            .append(") ");
        	paramList.add(mode);
        	paramList.add(prjId);
            paramList.add(cusId);
            paramList.add(ruleId);
            paramList.add(itemType);
            sql.append(" ORDER BY ld.pool_name ");
            Query query = ecmcAlarmRuleDao.createSQLNativeQuery(sql.toString(), paramList.toArray());
            List resultList = query.getResultList();
            for(int i=0; i<resultList.size(); i++){
                Object[] obj = (Object[]) resultList.get(i);
                EcmcAlarmObject ecmcObj = new EcmcAlarmObject();
                ecmcObj.setAlarmRuleId(ruleId);
                ecmcObj.setAoType(itemType);
                ecmcObj.setAoObjectId(String.valueOf(obj[0]));
                ecmcObj.setObjName(String.valueOf(obj[1]));
                ecmcObj.setDcId(String.valueOf(obj[2]));
                ecmcObj.setPrjId(String.valueOf(obj[3]));
                ecmcObj.setVmIp(String.valueOf(obj[4]));
                ecmcObj.setCusId(String.valueOf(obj[5]));
                ecmcObj.setFloatIp(String.valueOf(obj[6]));
                ecmcObjectList.add(ecmcObj);
            }
        }
        return ecmcObjectList;
    }

    /**
     * 赋给运维报警对象model的扩展属性信息
     * dcName、prjName、cusOrg、objName、vmIp
     * 云主机：        network、floatIp、selfSubIp
     * 数据库实例：dataVersionName
     * 负载均衡：   network、floatIp、config
     * @param ecmcObj
     * @return
     */
    public EcmcAlarmObject getAttributesForObj(EcmcAlarmObject ecmcObj) {
    	String resourceId = ecmcObj.getAoObjectId();		//报警对象对应资源ID
    	String vmIp = "--";
    	String floatIp = "--";
    	String selfIp = "--";
    	ecmcObj.setIsDeleted(false);
    	if (ecmcObj.getAoType().equals(RedisNodeIdConstant.ECMC_MONITOR_VM_NODEID)) {
    		//ECMC监控项-云主机
    		try {
                StringBuffer sql = new StringBuffer(" SELECT ");
                sql.append(" vm.vm_name, vm.vm_ip, net.net_name, ");
                sql.append(" ip.flo_ip, prj.prj_name, cus.cus_org,");
                sql.append(" dc.dc_name , vm.self_ip");
                sql.append(" FROM cloud_vm vm ");
                sql.append("LEFT JOIN cloud_network net ON vm.net_id = net.net_id ");
                sql.append("LEFT JOIN cloud_floatip ip ON vm.vm_id = ip.resource_id ");
                sql.append("LEFT JOIN dc_datacenter dc ON vm.dc_id = dc.id ");
                sql.append("LEFT JOIN cloud_project prj ON vm.prj_id = prj.prj_id ");
                sql.append("LEFT JOIN sys_selfcustomer cus ON prj.customer_id = cus.cus_id ");
                sql.append("WHERE vm.is_deleted= '0' and vm.vm_id = ? ");
                javax.persistence.Query query = ecmcAlarmRuleDao.createSQLNativeQuery(sql.toString(), resourceId);
                List list = query.getResultList();
                if (null!=list && list.size() > 0) {
                    Object[] obj = (Object[]) list.get(0);
                    ecmcObj.setObjName(String.valueOf(obj[0]));
                    if(!StringUtil.isEmpty(String.valueOf(obj[1]))){
                    	vmIp = String.valueOf(obj[1]);
                    	vmIp = vmIp.substring(vmIp.indexOf(":") + 1);
                    }
                    ecmcObj.setVmIp(vmIp);
                    ecmcObj.setNetwork(String.valueOf(obj[2]));
                    if(!StringUtil.isEmpty(String.valueOf(obj[3]))){
                    	floatIp = String.valueOf(obj[3]);
                    }
                    ecmcObj.setFloatIp(floatIp);
                    ecmcObj.setPrjName(String.valueOf(obj[4]));
                    ecmcObj.setCusName(String.valueOf(obj[5]));
                    ecmcObj.setDcName(String.valueOf(obj[6]));
                    if(!StringUtil.isEmpty(String.valueOf(obj[7]))){
                    	selfIp = String.valueOf(obj[7]);
                    }
                    ecmcObj.setSelfSubIp(selfIp);
                }else{
                	ecmcObj.setIsDeleted(true);
                }
            } catch (Exception e) {
                log.error(e.getMessage(),e);
                throw new AppException(e.getMessage());
            }
    	}else if(ecmcObj.getAoType().equals(RedisNodeIdConstant.ECMC_MONITOR_TYPE_CLOUDDATA)){
    		//ECMC监控项-云数据库
    		try {
                StringBuffer sql = new StringBuffer(" SELECT ");
                sql.append("	rds.rds_name,");
                sql.append("	rds.rds_ip,");
                sql.append("	rds.is_master,");
                sql.append("	ver.`name` AS  verName,");
                sql.append("	sto.`name` AS storeName,");
                sql.append("	prj.prj_name,");
                sql.append("	dc.dc_name, ");
                sql.append("	cus.cus_org ");
                sql.append(" FROM cloud_rdsinstance rds ");
                sql.append(" LEFT JOIN cloud_project prj ON rds.prj_id = prj.prj_id");
                sql.append(" LEFT JOIN sys_selfcustomer cus ON prj.customer_id = cus.cus_id ");
                sql.append(" LEFT JOIN dc_datacenter dc ON dc.id = rds.dc_id");
                sql.append(" LEFT JOIN cloud_datastoreversion ver ON rds.version_id = ver.id");
                sql.append(" LEFT JOIN cloud_datastore sto ON ver.datastore_id = sto.id ");
                sql.append(" where 1=1 and rds.rds_id = ? and rds.is_deleted= '0' ");
                javax.persistence.Query query = ecmcAlarmRuleDao.createSQLNativeQuery(sql.toString(), resourceId);
                List list = query.getResultList();
                if (null!=list && list.size() > 0) {
                    Object[] obj = (Object[]) list.get(0);
                    ecmcObj.setObjName(String.valueOf(obj[0]));
                    if(!StringUtil.isEmpty(String.valueOf(obj[1]))){
                    	vmIp = String.valueOf(obj[1]);
                    	vmIp = vmIp.substring(vmIp.indexOf(":") + 1);
                    }
                    ecmcObj.setVmIp(vmIp);
                    ecmcObj.setIsMaster(String.valueOf(obj[2]));
                    String datastoreName = MonitorAlarmUtil.transferDatastoreName(String.valueOf(obj[4]));
                    ecmcObj.setDataVersionName(datastoreName+String.valueOf(obj[3]));
                    ecmcObj.setPrjName(String.valueOf(obj[5]));
                    ecmcObj.setDcName(String.valueOf(obj[6]));
                    ecmcObj.setCusName(String.valueOf(obj[7]));
                }else{
                	ecmcObj.setIsDeleted(true);
                }
            } catch (Exception e) {
                log.error(e.getMessage(),e);
                throw new AppException(e.getMessage());
            }
    		
    	}else if(ecmcObj.getAoType().equals(RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDCOMMON) || 
    			ecmcObj.getAoType().equals(RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDMASTER)){
    		//ECMC监控项-负载均衡(普通或主备模式)
    		String mode = MonitorAlarmUtil.LDPOOL_MODE_MASTER;
    		if(ecmcObj.getAoType().equals(RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDCOMMON)){
    			mode = MonitorAlarmUtil.LDPOOL_MODE_COMMON;
    		}
    		try {
                StringBuffer sql = new StringBuffer(" SELECT ");
                sql.append("	ld.pool_name,");
                sql.append("	sub.gateway_ip,");
                sql.append("	flo.flo_ip,");
                sql.append("	prj.prj_name,");
                sql.append("	dc.dc_name,");
                sql.append("	net.net_name,");
                sql.append("	vip.vip_protocol,");
                sql.append("	vip.protocol_port,");
                sql.append("	ld.mode, ");
                sql.append("	cus.cus_org ");
                sql.append(" FROM cloud_ldpool ld ");
                sql.append(" LEFT JOIN cloud_subnetwork sub ON ld.subnet_id = sub.subnet_id");
                sql.append(" LEFT JOIN cloud_floatip flo ON flo.resource_id = ld.pool_id AND flo.resource_type = 'lb' AND flo.is_deleted = '0'");
                sql.append(" LEFT JOIN cloud_project prj ON prj.prj_id = ld.prj_id");
                sql.append(" LEFT JOIN sys_selfcustomer cus ON prj.customer_id = cus.cus_id ");
                sql.append(" LEFT JOIN dc_datacenter dc ON dc.id = ld.dc_id");
                sql.append(" LEFT JOIN cloud_network net ON sub.net_id = net.net_id");
                sql.append(" LEFT JOIN cloud_ldvip vip ON ld.pool_id = vip.pool_id");
                sql.append(" where 1=1 and ld.pool_id = ? and ld.mode = ?");
                javax.persistence.Query query = ecmcAlarmRuleDao.createSQLNativeQuery(sql.toString(), resourceId,mode);
                List list = query.getResultList();
                if (null!=list && list.size() > 0) {
                    Object[] obj = (Object[]) list.get(0);
                    ecmcObj.setObjName(String.valueOf(obj[0]));
                    if(!StringUtil.isEmpty(String.valueOf(obj[1]))){
                    	vmIp = String.valueOf(obj[1]);
                    	vmIp = vmIp.substring(vmIp.indexOf(":") + 1);
                    }
                    ecmcObj.setVmIp(vmIp);
                    ecmcObj.setFloatIp(String.valueOf(obj[2]));
                    ecmcObj.setPrjName(String.valueOf(obj[3]));
                    ecmcObj.setDcName(String.valueOf(obj[4]));
                    ecmcObj.setNetwork(String.valueOf(obj[5]));
                    ecmcObj.setConfig(String.valueOf(obj[6])+"："+String.valueOf(obj[7]));
                    ecmcObj.setMode(mode);
                    ecmcObj.setCusName(String.valueOf(obj[9]));
                }else{
                	ecmcObj.setIsDeleted(true);
                }
            } catch (Exception e) {
                log.error(e.getMessage(),e);
                throw new AppException(e.getMessage());
            }
    	}
        return ecmcObj;
    }

    /**
     * 查询运维报警对象Bean属性：dcId、prjId、cusId
     * 用于一键添加报警对象和批量添加报警对象
     * @param ecmcObj
     * @return
     */
    public BaseEcmcAlarmObject getBeanAttrForBaseObj(EcmcAlarmObject ecmcObj) {
    	String resourceId = ecmcObj.getAoObjectId();
    	StringBuffer sql = new StringBuffer();
        if (ecmcObj.getAoType().equals(RedisNodeIdConstant.ECMC_MONITOR_VM_NODEID)) {
        	//ECMC监控项-云主机
            try {
                sql.append("SELECT vm.prj_id,vm.dc_id,prj.customer_id FROM cloud_vm vm ");
                sql.append("LEFT JOIN cloud_project prj ON vm.prj_id = prj.prj_id ");
                sql.append("WHERE vm.is_deleted = '0' ");
                sql.append("AND vm.vm_id = ? ");
                javax.persistence.Query query = ecmcAlarmRuleDao.createSQLNativeQuery(sql.toString(), resourceId);
                List list = query.getResultList();
                if (list.size() > 0) {
                    Object[] obj = (Object[]) list.get(0);
                    
                    ecmcObj.setPrjId(String.valueOf(obj[0]));
                    ecmcObj.setDcId(String.valueOf(obj[1]));
                    ecmcObj.setCusId(String.valueOf(obj[2]));
                }
            } catch (Exception e) {
                log.error(e.getMessage(),e);
                throw new AppException(e.getMessage());
            }
        }else if(ecmcObj.getAoType().equals(RedisNodeIdConstant.ECMC_MONITOR_TYPE_CLOUDDATA)){
    		//ECMC监控项-云数据库
    		try {
                sql.append(" SELECT ");
                sql.append(" rds.prj_id,");
                sql.append(" rds.dc_id,");
                sql.append(" prj.customer_id");
                sql.append(" FROM cloud_rdsinstance rds ");
                sql.append(" LEFT JOIN cloud_project prj ON rds.prj_id = prj.prj_id");
                sql.append(" where 1=1 and rds.rds_id = ? and rds.is_deleted= '0' ");
                javax.persistence.Query query = ecmcAlarmRuleDao.createSQLNativeQuery(sql.toString(), resourceId);
                List list = query.getResultList();
                if (null!=list && list.size() > 0) {
                    Object[] obj = (Object[]) list.get(0);
                    
                    ecmcObj.setPrjId(String.valueOf(obj[0]));
                    ecmcObj.setDcId(String.valueOf(obj[1]));
                    ecmcObj.setCusId(String.valueOf(obj[2]));
                }
            } catch (Exception e) {
                log.error(e.getMessage(),e);
                throw new AppException(e.getMessage());
            }
    		
    	}else if(ecmcObj.getAoType().equals(RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDCOMMON) || 
    			ecmcObj.getAoType().equals(RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDMASTER)){
    		//ECMC监控项-负载均衡(普通或主备模式)
    		String mode = MonitorAlarmUtil.LDPOOL_MODE_MASTER;
    		if(ecmcObj.getAoType().equals(RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDCOMMON)){
    			mode = MonitorAlarmUtil.LDPOOL_MODE_COMMON;
    		}
    		try {
    			sql.append(" SELECT ");
                sql.append("	ld.prj_id,");
                sql.append("	ld.dc_id,");
                sql.append(" 	prj.customer_id ");
                sql.append(" FROM cloud_ldpool ld ");
                sql.append(" LEFT JOIN cloud_project prj ON prj.prj_id = ld.prj_id");
                sql.append(" where 1=1 and ld.pool_id = ? and ld.mode = ?");
                javax.persistence.Query query = ecmcAlarmRuleDao.createSQLNativeQuery(sql.toString(), resourceId,mode);
                List list = query.getResultList();
                if (null!=list && list.size() > 0) {
                    Object[] obj = (Object[]) list.get(0);
                    
                    ecmcObj.setPrjId(String.valueOf(obj[0]));
                    ecmcObj.setDcId(String.valueOf(obj[1]));
                    ecmcObj.setCusId(String.valueOf(obj[2]));
                }
            } catch (Exception e) {
                log.error(e.getMessage(),e);
                throw new AppException(e.getMessage());
            }
    	}
        BaseEcmcAlarmObject baseObj = new BaseEcmcAlarmObject();
        BeanUtils.copyPropertiesByModel(baseObj, ecmcObj);
        return baseObj;
    }

    /**
     * 批量添加报警对象
     * @Author: duanbinbin
     * @param ruleId
     * @param ecmcObjectList
     * @param monitorType
     *<li>Date: 2017年3月2日</li>
     */
    @Override
    public void addEcmcObjToRule(String ruleId, List<Map> ecmcObjectList,String monitorType) {
        log.info("批量添加运维报警对象关联到某运维报警规则");
        //需要判断哪些是新增的、哪些是原来有但是去掉、以及哪些是没有变动的

        List<EcmcAlarmObject> existedObjList = this.getEcmcObjListInRule(ruleId,monitorType);//原来关联的所有对象
        List<EcmcAlarmObject> toBeAddObjList = new ArrayList<EcmcAlarmObject>();		//原来没有，需新增
        List<EcmcAlarmObject> toBeDeleteObjList = new ArrayList<EcmcAlarmObject>();		//原来有，但待删除
        List<EcmcAlarmObject> frontCommitedObjList = new ArrayList<EcmcAlarmObject>();	//提交所有

        for (Map objMap : ecmcObjectList) {
            EcmcAlarmObject ecmcObj = new EcmcAlarmObject();
            ecmcObj.setAoObjectId(String.valueOf(objMap.get("aoObjectId")));
            ecmcObj.setAlarmRuleId(ruleId);
            ecmcObj.setAoType(String.valueOf(objMap.get("aoType")));
            frontCommitedObjList.add(ecmcObj);
        }
        for (EcmcAlarmObject ecmcObject : frontCommitedObjList) {
            boolean isObjectInList = this.isObjectInList(ecmcObject, existedObjList);
            if (!isObjectInList) {
                toBeAddObjList.add(ecmcObject);
            }
        }
        for (EcmcAlarmObject ecmcObject : existedObjList) {
            boolean isObjectInList = this.isObjectInList(ecmcObject, frontCommitedObjList);
            if (!isObjectInList) {
                toBeDeleteObjList.add(ecmcObject);
            }
        }
        for (EcmcAlarmObject toBeAddObject : toBeAddObjList) {
            BaseEcmcAlarmObject baseEcmcObj = this.getBeanAttrForBaseObj(toBeAddObject);
            this.addAlarmObject(baseEcmcObj);
            EcmcAlarmObject alarmObject = new EcmcAlarmObject();
            BeanUtils.copyPropertiesByModel(alarmObject, baseEcmcObj);
            ecmcMonitorAlarmService.addEcmcMonItemByObject(alarmObject);
        }
        for (EcmcAlarmObject toBeDeleteObject : toBeDeleteObjList) {
            this.deleteAlarmObject(toBeDeleteObject);
        }
    }

    /**
     * 一键添加所有报警对象
     * 首先会查询出所有的可添加报警对象资源ID
     * @Author: duanbinbin
     * @param ruleId
     * @param monitorType
     *<li>Date: 2017年3月9日</li>
     */
    @Override
    public void addEcmcObjAllToRule(String ruleId, List<String> objsIdList,String monitorType) {
        log.info("一键添加全部运维报警对象关联到某运维报警规则");
        List<EcmcAlarmObject> frontCommitedObjList = new ArrayList<EcmcAlarmObject>();//所有需要添加的报警对象
        for (String objId : objsIdList) {
            EcmcAlarmObject ecmcObj = new EcmcAlarmObject();
            ecmcObj.setAoObjectId(objId);
            ecmcObj.setAlarmRuleId(ruleId);
            ecmcObj.setAoType(monitorType);
            frontCommitedObjList.add(ecmcObj);
        }
       
        for (EcmcAlarmObject toBeAddObject : frontCommitedObjList) {
            BaseEcmcAlarmObject baseEcmcObj = this.getBeanAttrForBaseObj(toBeAddObject);
            this.addAlarmObject(baseEcmcObj);
            EcmcAlarmObject alarmObject = new EcmcAlarmObject();
            BeanUtils.copyPropertiesByModel(alarmObject, baseEcmcObj);
            ecmcMonitorAlarmService.addEcmcMonItemByObject(alarmObject);//添加对象的监控报警项
        }
    }
    /**
     * 判断一个报警对象是否在一个集合中
     *
     * @param ecmcObject
     * @param existedEcmcObjList
     * @return
     */
    private boolean isObjectInList(EcmcAlarmObject ecmcObject, List<EcmcAlarmObject> existedEcmcObjList) {
        for (EcmcAlarmObject Object : existedEcmcObjList) {
            if (ecmcObject.getAoObjectId().equals(Object.getAoObjectId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 添加单个报警对象
     *
     * @return
     * @throws AppException
     */
    public BaseEcmcAlarmObject addAlarmObject(BaseEcmcAlarmObject baseEcmcObject) throws AppException {
        ecmcAlarmObjectDao.saveEntity(baseEcmcObject);
        String objectJSON = JSONObject.toJSONString(baseEcmcObject);
        try {
            jedisUtil.set(RedisKey.ECMC_ALARM_OBJECT + baseEcmcObject.getId(), objectJSON);
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
        return baseEcmcObject;
    }
    /**
     * 根据报警对象ID移除某一报警对象
     * @Author: duanbinbin
     * @param id
     *<li>Date: 2017年3月9日</li>
     */
    @Override
    public void removeEcmcObjById(String id) {
        BaseEcmcAlarmObject baseEcmcObj = ecmcAlarmObjectDao.findOne(id);
        EcmcAlarmObject ecmcObj = new EcmcAlarmObject();
        BeanUtils.copyPropertiesByModel(ecmcObj, baseEcmcObj);
        this.deleteAlarmObject(ecmcObj);	//删除单个报警对象
    }

    @Override
    public List<EcmcAlarmContact> getEcmcConsByRuleId(String ruleId) {
        log.info("查询某报警规则下关联的联系人");
        List<EcmcAlarmContact> ecmcContactList = new ArrayList<EcmcAlarmContact>();

        StringBuffer sql = new StringBuffer();
        sql.append("SELECT ac.ac_id,ac.ac_alarmruleid,ac.ac_contactid,con.mc_name,con.mc_smsnotify,con.mc_mailnotify,con.mc_phone,con.mc_email ");
        sql.append(" FROM ecmc_alarmcontact ac ");
        sql.append(" LEFT JOIN ecmc_contact con ON ac.ac_contactid = con.mc_id ");
        sql.append(" WHERE ac.ac_alarmruleid = ? ");
        Query query = ecmcAlarmContactDao.createSQLNativeQuery(sql.toString(), ruleId);
        List list = query.getResultList();
        for (int i = 0; i < list.size(); i++) {
            Object[] obj = (Object[]) list.get(i);
            EcmcAlarmContact ecmcContact = new EcmcAlarmContact();
            ecmcContact.setId(String.valueOf(obj[0]));
            ecmcContact.setAlarmRuleId(String.valueOf(obj[1]));
            ecmcContact.setContactId(String.valueOf(obj[2]));
            ecmcContact.setContactName(String.valueOf(obj[3]));
            String contactMethod = "";
            String isSmsNotify = String.valueOf(obj[4]);
            String isMailNotify = String.valueOf(obj[5]);
            contactMethod = getContactMethod(isSmsNotify, isMailNotify);
            ecmcContact.setContactMethod(contactMethod) ;
            ecmcContact.setContactPhone(String.valueOf(obj[6]));
            ecmcContact.setContactEmail(String.valueOf(obj[7]));
            ecmcContact = getConGroupAttributes(ecmcContact);

            ecmcContactList.add(ecmcContact);
        }
        return ecmcContactList;
    }

    /**
     * 联系方式字符串
     *
     * @param isSmsNotify
     * @param isMailNotify
     * @return
     */
    private String getContactMethod(String isSmsNotify, String isMailNotify) {
        if (isSmsNotify.equals("1") && isMailNotify.equals("1")) {
            return "短信 邮件";
        } else if (isSmsNotify.equals("1") && isMailNotify.equals("0")) {
            return "短信";
        } else if (isSmsNotify.equals("0") && isMailNotify.equals("1")) {
            return "邮件";
        }
        return "";
    }

    /**
     * 获取联系人组信息
     *
     * @param ecmcContact
     * @return
     */
    private EcmcAlarmContact getConGroupAttributes(EcmcAlarmContact ecmcContact) {
        String groupName = "";
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT cg.mg_name FROM ecmc_contactgroupdetail cgd ");
        sql.append("LEFT JOIN ecmc_contactgroup cg ON cgd.mgd_groupid = cg.mg_id ");
        sql.append("WHERE cgd.mgd_contactid = ?");
        Query query = ecmcAlarmContactDao.createSQLNativeQuery(sql.toString(), ecmcContact.getContactId());
        List list = query.getResultList();
        for (int i = 0; i < list.size(); i++) {
            String name = (String) list.get(i);
            groupName = groupName + name + ",";
        }
        ecmcContact.setContactGroupName(groupName);
        return ecmcContact;
    }

    @Override
    public List<EcmcConGroupBy> getAllConsGroupBy() {
        log.info("按组别查询所有运维报警联系人");
        List<EcmcConGroupBy> conGroupByList = new ArrayList<EcmcConGroupBy>();
        StringBuffer sql = new StringBuffer();
        sql.append(" from BaseEcmcContactGroup ");
        List<BaseEcmcContactGroup> ecmcGroupList = ecmcContactGroupDao.find(sql.toString());
        for (BaseEcmcContactGroup baseEcmcGroup : ecmcGroupList) {
            EcmcConGroupBy conGroupBy = new EcmcConGroupBy();
            conGroupBy.setContactGroupId(baseEcmcGroup.getId());
            conGroupBy.setContactGroupName(baseEcmcGroup.getName());
            StringBuffer sbSql = new StringBuffer();
            sbSql.append("SELECT con.mc_id,con.mc_name,con.mc_smsnotify,con.mc_mailnotify FROM ecmc_contactgroupdetail cgd ");
            sbSql.append(" LEFT JOIN ecmc_contact con ON cgd.mgd_contactid = con.mc_id ");
            sbSql.append(" WHERE cgd.mgd_groupid = ? ");
            Query query = ecmcAlarmContactDao.createSQLNativeQuery(sbSql.toString(), baseEcmcGroup.getId());
            List list = query.getResultList();
            List<EcmcAlarmContact> ecmcContactList = new ArrayList<EcmcAlarmContact>();
            for (int i = 0; i < list.size(); i++) {
                Object[] obj = (Object[]) list.get(i);
                EcmcAlarmContact ecmcContact = new EcmcAlarmContact();
                ecmcContact.setContactId(String.valueOf(obj[0]));
                ecmcContact.setContactName(String.valueOf(obj[1]));
                String contactMethod = "";
                String isSmsNotify = String.valueOf(obj[2]);
                String isMailNotify = String.valueOf(obj[3]);
                contactMethod = getContactMethod(isSmsNotify, isMailNotify);
                ecmcContact.setContactMethod(contactMethod);
                ecmcContactList.add(ecmcContact);
            }
            conGroupBy.setContactList(ecmcContactList);
            conGroupByList.add(conGroupBy);
        }
        return conGroupByList;
    }

    @Override
    public void addEcmcConsToRule(String ruleId, List<String> contactIds) {
        log.info("批量添加报警联系人到某报警规则");
        //全删全增方式
        boolean isClear = deleteAllContactsByRuleId(ruleId);
        if (isClear) {
            for (String contactId : contactIds) {
                EcmcAlarmContact ecmcContact = new EcmcAlarmContact();
                ecmcContact.setAlarmRuleId(ruleId);
                ecmcContact.setContactId(contactId);
                BaseEcmcAlarmContact baseContact = new BaseEcmcAlarmContact();
                BeanUtils.copyPropertiesByModel(baseContact, ecmcContact);
                ecmcAlarmContactDao.saveEntity(baseContact);
            }
        }
    }

    @Override
    public void removeEcmcConFromRule(String alarmContactId) {
        log.info("从某运维报警规则下解绑某运维联系人");
        ecmcAlarmContactDao.delete(alarmContactId);
    }
    /**
     * 查询编辑报警规则时需要查询出触发条件等参数
     * @Author: duanbinbin
     * @param ruleId
     * @return
     *<li>Date: 2017年3月9日</li>
     */
    @Override
    public JSONObject getJsonForEcmcRuleParams(String ruleId) {
        JSONObject json = new JSONObject();
        EcmcAlarmRule ecmcRule = getEcmcRuleById(ruleId);
        List<EcmcAlarmTrigger> ecmcTriggerList = getTriggerListByRuleId(ruleId);
        json.put("alarmRuleModel", ecmcRule);
        json.put("triggerArray", ecmcTriggerList);
        return json;
    }
    /**
     * 根据报警规则查询触发条件
     * @Author: duanbinbin
     * @param ruleId
     * @return
     * @throws AppException
     *<li>Date: 2017年3月9日</li>
     */
    public List<EcmcAlarmTrigger> getTriggerListByRuleId(String ruleId) throws AppException {
        log.info("根据报警规则查询触发条件开始");
        List<EcmcAlarmTrigger> ecmcTriggerList = new ArrayList<EcmcAlarmTrigger>();
        StringBuffer hql = new StringBuffer();
        hql.append(" from BaseEcmcAlarmTrigger where alarmRuleId = ?");
        List<BaseEcmcAlarmTrigger> baseEcmcTriggerList = ecmcAlarmTriggerDao.find(hql.toString(), ruleId);
        for (BaseEcmcAlarmTrigger baseTrigger : baseEcmcTriggerList) {
            EcmcAlarmTrigger ecmcTrigger = new EcmcAlarmTrigger();
            BeanUtils.copyPropertiesByModel(ecmcTrigger, baseTrigger);
            ecmcTriggerList.add(ecmcTrigger);
        }
        return ecmcTriggerList;
    }

    /**
     * 查询redis中运维报警对象信息
     * @return
     */
    @Override
    public EcmcAlarmObject getEcmcObjByObjId(String alarmObjectId) {
        EcmcAlarmObject ecmcAlarmObject = new EcmcAlarmObject();
        try {
            String jsonString = jedisUtil.get(RedisKey.ECMC_ALARM_OBJECT + alarmObjectId);
            if(StringUtil.isEmpty(jsonString)){
            	log.error("根据报警对象id查询redis信息失败，ecmcAlarmObject："+alarmObjectId);
            	return ecmcAlarmObject;
            }
            JSONObject json = JSONObject.parseObject(jsonString);
            String alarmRuleId = String.valueOf(json.get("alarmRuleId"));
            String aoObjectId = String.valueOf(json.get("aoObjectId"));
            String aoType = String.valueOf(json.get("aoType"));
            String cusId = String.valueOf(json.get("cusId"));
            String prjId = String.valueOf(json.get("prjId"));
            String dcId = String.valueOf(json.get("dcId"));

            ecmcAlarmObject.setId(alarmObjectId);
            ecmcAlarmObject.setAlarmRuleId(alarmRuleId);
            ecmcAlarmObject.setAoObjectId(aoObjectId);
            ecmcAlarmObject.setAoType(aoType);
            ecmcAlarmObject.setCusId(cusId);
            ecmcAlarmObject.setPrjId(prjId);
            ecmcAlarmObject.setDcId(dcId);

            ecmcAlarmObject = getAttributesForObj(ecmcAlarmObject);
            if(ecmcAlarmObject.getIsDeleted()){		//如遇到资源已删除的垃圾数据，做清除处理
             	log.warn("该运维报警对象的资源已被删除，请清查垃圾数据，type:"+ecmcAlarmObject.getAoType()+",resourceId:"+ecmcAlarmObject.getAoObjectId()+",objectId:"+ecmcAlarmObject.getId());
             	if(ecmcAlarmObject.getAoType().equals(RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDCOMMON)
             			||ecmcAlarmObject.getAoType().equals(RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDMASTER)){
             		this.clearPoolMsgAfterDeletePool(ecmcAlarmObject.getAoObjectId());			//清除负载均衡报警相关
             	}else{
             		this.cleanAlarmDataAfterDeletingObject(ecmcAlarmObject.getAoObjectId());	//清除主机或数据库实例报警相关
             	}
             }
        } catch (Exception e) {
            log.error("error-ecmcAlarmObject", e);
            throw new AppException("查询redis中运维报警对象信息异常");
        }
        return ecmcAlarmObject;
    }

    /**
     * 查询redis中运维触发条件信息
     *
     * @param triggerId
     * @return
     */
    @Override
    public EcmcAlarmTrigger getEcmcAlarmTriggerByTriId(String triggerId) {
        EcmcAlarmTrigger ecmcAlarmTrigger = new EcmcAlarmTrigger();
        try {
            String jsonString = jedisUtil.get(RedisKey.ECMC_ALARM_TRIGGER + triggerId);
            if(StringUtil.isEmpty(jsonString)){
            	log.error("根据触发条件id查询redis信息失败，triggerId："+triggerId);
            	return ecmcAlarmTrigger;
            }
            JSONObject json = JSONObject.parseObject(jsonString);
            String alarmRuleId = String.valueOf(json.get("alarmRuleId"));
            String zb = String.valueOf(json.get("zb"));
            String operator = String.valueOf(json.get("operator"));
            String thresholdStr = String.valueOf(json.get("threshold"));
            float threshold = Float.valueOf(thresholdStr);
            String unit = String.valueOf(json.get("unit"));
            String lastTimeStr = String.valueOf(json.get("lastTime"));
            int lastTime = Integer.valueOf(lastTimeStr);
            String isTriggered = String.valueOf(json.get("isTriggered"));

            ecmcAlarmTrigger.setId(triggerId);
            ecmcAlarmTrigger.setAlarmRuleId(alarmRuleId);
            ecmcAlarmTrigger.setZb(zb);
            ecmcAlarmTrigger.setOperator(operator);
            ecmcAlarmTrigger.setThreshold(threshold);
            ecmcAlarmTrigger.setUnit(unit);
            ecmcAlarmTrigger.setLastTime(lastTime);
            ecmcAlarmTrigger.setIsTriggered(isTriggered);

        } catch (Exception e) {
            log.error("error-ecmcAlarmObject", e);
            throw new AppException("查询redis中运维触发条件对象信息异常");
        }
        return ecmcAlarmTrigger;
    }

    /**
     * 如删报警对象资源（如云主机、数据库实例）
     * 则需删除运维报警对象及其关联的报警信息、报警监控项等
     * 
     */
    @Override
    public void cleanAlarmDataAfterDeletingObject(String resourceId) {
        String hql = "from BaseEcmcAlarmObject where aoObjectId = ?";
        List<BaseEcmcAlarmObject> baseEcmcAlarmObjList = ecmcAlarmObjectDao.find(hql, resourceId);
        for (BaseEcmcAlarmObject baseEcmcAlarmObj : baseEcmcAlarmObjList) {
            EcmcAlarmObject ecmcAlarmObject = new EcmcAlarmObject();
            BeanUtils.copyPropertiesByModel(ecmcAlarmObject, baseEcmcAlarmObj);
            //删除单个报警对象的操作
            this.deleteAlarmObject(ecmcAlarmObject);
        }
    }

    /**
     * 按照联系人id查询联系人
     *
     * @param contactId
     * @return
     */
    @Override
    public EcmcContact getEcmcContactById(String contactId) {
        BaseEcmcContact baseEcmcContact = ecmcContactDao.findOne(contactId);
        EcmcContact ecmcContact = new EcmcContact();
        BeanUtils.copyPropertiesByModel(ecmcContact, baseEcmcContact);
        return ecmcContact;
    }

    /**
     * 根据监控项id查询最新一条报警信息
     *
     * @param itemId
     * @return
     */
    @Override
    public EcmcAlarmMessage getLatestEcmcAlarmMsgByItemId(String itemId) {
        String hql = "from BaseEcmcAlarmMessage where monitorAlarmItemId = ? order by time desc ";
        List<BaseEcmcAlarmMessage> ecmcMsgList = ecmcAlarmMessageDao.find(hql, itemId);
        EcmcAlarmMessage ecmcMsg = new EcmcAlarmMessage();
        if (ecmcMsgList.size() > 0) {
            BaseEcmcAlarmMessage baseEcmcMsg = ecmcMsgList.get(0);
            BeanUtils.copyPropertiesByModel(ecmcMsg, baseEcmcMsg);
        }
        return ecmcMsg;
    }
    /**
     * 同步项目短信配额
     * @Author: duanbinbin
     * @throws Exception
     *<li>Date: 2017年3月9日</li>
     */
    @Override
    public void resyncSmsQuotaCache() throws Exception {
        //0. 重新同步之前清除缓存中客户项目报警短信配额信息
        beforeResync();
        //1. 找到当前所有的客户项目配额信息列表
        List<QuotaInfo> quotaInfoList = getCustomerQuotaInfoList();
        //2. 分别将客户项目的已发送短信数量和短信总配额写入Redis
        doResyncSmsQuotaCache(quotaInfoList);
    }

    /**
     * 执行客户项目短信配额缓存重新同步
     * @param quotaInfoList
     */
    private void doResyncSmsQuotaCache(List<QuotaInfo> quotaInfoList) throws Exception {
        log.info("执行客户项目短信配额缓存重新同步功能");
        String[] prefix = {RedisKey.SMS_QUOTA_SENT,RedisKey.SMS_QUOTA_TOTAL};
        for (QuotaInfo info: quotaInfoList) {
            String postfix = info.getCustomerID()+":"+info.getProjectID();
            String sentKey = prefix[0]+postfix;
            String totalKey = prefix[1]+postfix;
            jedisUtil.set(sentKey, String.valueOf(info.getSentQuota()));
            jedisUtil.set(totalKey, String.valueOf(info.getTotalQuota()));
        }
    }

    /**
     * 获取客户项目配额信息列表，配额信息含客户ID、项目ID、配额内发送的报警短信数、总报警短信配额
     * @return
     */
    private List<QuotaInfo> getCustomerQuotaInfoList() {
        log.info("获取客户项目配额信息列表");
        List<QuotaInfo> infoList = new ArrayList<QuotaInfo>();
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ");
        sb.append(" prj.customer_id as customerID, ");
        sb.append(" prj.prj_id as projectID, ");
        sb.append(" info.sentQuota as sentQuota, ");
        sb.append(" prj.sms_count as totalQuota ");
        sb.append("FROM ");
        sb.append(" cloud_project prj ");
        sb.append("LEFT JOIN ( ");
        sb.append(" SELECT ");
        sb.append("     sms.sms_cust AS customerId, ");
        sb.append("     sms.sms_proj AS projectId, ");
        sb.append("     sum(sms.sms_sent) AS sentQuota ");
        sb.append(" FROM ");
        sb.append("     sys_sms sms ");
        sb.append(" GROUP BY ");
        sb.append("     sms.sms_cust, ");
        sb.append("     sms.sms_proj ");
        sb.append(") AS info ON prj.customer_id = info.customerId ");
        sb.append("AND prj.prj_id = info.projectId ");
        sb.append("WHERE ");
        sb.append(" prj.customer_id <> '' AND prj.prj_id <> ''");
        Query query = ecmcContactDao.createSQLNativeQuery(sb.toString());
        List<Object[]> resultList = query.getResultList();
        for (Object[] objs : resultList) {
            String customerID = objs[0].toString();
            String projectID = objs[1].toString();
            int sentQuota = objs[2]==null?0:Integer.parseInt(objs[2].toString());
            int totalQuota = objs[3]==null?0:Integer.parseInt(objs[3].toString());//to avoid ClassCastException: BigDecimal cannot be cast to Integer

            QuotaInfo info = new QuotaInfo();
            info.setCustomerID(customerID);
            info.setProjectID(projectID);
            info.setSentQuota(sentQuota);
            info.setTotalQuota(totalQuota);

            infoList.add(info);
        }
        return infoList;
    }

    /**
     * 清除缓存中已有的短信配额信息
     */
    private void beforeResync() throws Exception {
        log.info("同步配额缓存前置操作");
        String key = RedisKey.SMS_QUOTA+"*";

        Set<String> idSet = jedisUtil.keys(key);
        for (String id : idSet) {
            jedisUtil.delete(id);
        }
        idSet.clear();
    }

    /**
     * 查询所有运维报警联系人
     * @Author: duanbinbin
     * @return
     *<li>Date: 2016年5月3日</li>
     */
	@Override
	public List<EcmcAlarmContact> getAllAlarmContacts() {
		log.info("查询所有运维报警联系人");
		List<EcmcAlarmContact> conList = new ArrayList<EcmcAlarmContact>();
		StringBuffer hql = new StringBuffer();
		hql.append(" SELECT ")
          .append("   con.mc_id, con.mc_name, con.mc_smsnotify, con.mc_mailnotify ")
          .append(" FROM ")
          .append("   ecmc_contact con ");
        Query query = ecmcAlarmContactDao.createSQLNativeQuery(hql.toString(), null);
        if(null == query){
        	return conList;
        }
        List list = query.getResultList();
        for(int i = 0;i <list.size();i++){
        	Object[] obj = (Object[]) list.get(i);
        	EcmcAlarmContact con = new EcmcAlarmContact();
        	con.setContactId(String.valueOf(obj[0]));
        	con.setContactName(String.valueOf(obj[1]));
        	
        	String contactMethod = "";
            String isSmsNotify = String.valueOf(obj[2]);
            String isMailNotify = String.valueOf(obj[3]);
            contactMethod = getContactMethod(isSmsNotify, isMailNotify);
            con.setContactMethod(contactMethod);
            
            conList.add(con);
        }
		return conList;
	}
	/**
	 * 同步ECMC报警对象和触发条件缓存
	 * @Author: duanbinbin
	 *<li>Date: 2017年3月9日</li>
	 */
    @Override
    public void syncEcmcMonitor(){
        //1.获取alarmObject和alarmTrigger的key，并删除
        //2.读取数据库，完成缓存更新操作，注意数据量大的情况下的内存问题，可以考虑分页
        log.info("同步ECMC监控报警对象和触发条件开始");
        this.syncAlarmObject();
        this.syncAlarmTrigger();
    }

    private void syncAlarmObject() {
        try {
            Set<String> keys = jedisUtil.keys(RedisKey.ECMC_ALARM_OBJECT+"*");
            for(String key : keys){
                jedisUtil.delete(key);
            }
            keys.clear();
            StringBuilder sb = new StringBuilder();
            sb.append(" from BaseEcmcAlarmObject ");
            QueryMap qm = new QueryMap();
            int START_PAGE_NUMBER = 1;
            boolean hasNextPage = false;
            do{
                qm.setCURRENT_ROWS_SIZE(PAGE_SIZE);
                qm.setPageNum(START_PAGE_NUMBER);
                Page page = ecmcAlarmObjectDao.pagedQuery(sb.toString(), qm);
                List<BaseEcmcAlarmObject> aoList = (List<BaseEcmcAlarmObject>) page.getResult();
                for(BaseEcmcAlarmObject ao : aoList){
                    syncAlarmObject(ao);
                }
                hasNextPage = START_PAGE_NUMBER < page.getTotalPageCount();
                START_PAGE_NUMBER ++ ;
            }while(hasNextPage);

            log.info("同步ECMC报警对象成功");
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }

    }

    private void syncAlarmObject(BaseEcmcAlarmObject ao) throws Exception {
        String objectJSON = JSONObject.toJSONString(ao);
        try {
            jedisUtil.set(RedisKey.ECMC_ALARM_OBJECT + ao.getId(), objectJSON);
        } catch (Exception e) {
            throw e;
        }
    }

    private void syncAlarmTrigger() {
        try {
            Set<String> keys = jedisUtil.keys(RedisKey.ECMC_ALARM_TRIGGER + "*");
            for(String key : keys){
                jedisUtil.delete(key);
            }
            keys.clear();
            StringBuilder sb = new StringBuilder();
            sb.append(" from BaseEcmcAlarmTrigger ");
            QueryMap qm = new QueryMap();
            int START_PAGE_NUMBER = 1;
            boolean hasNextPage = false;
            do{
                qm.setCURRENT_ROWS_SIZE(PAGE_SIZE);
                qm.setPageNum(START_PAGE_NUMBER);
                Page page = ecmcAlarmTriggerDao.pagedQuery(sb.toString(), qm);
                List<BaseEcmcAlarmTrigger> atList = (List<BaseEcmcAlarmTrigger>) page.getResult();
                for(BaseEcmcAlarmTrigger at : atList){
                    syncAlarmTrigger(at);
                }
                hasNextPage = START_PAGE_NUMBER < page.getTotalPageCount();
                START_PAGE_NUMBER ++ ;
            }while(hasNextPage);

            log.info("同步ECMC监控报警触发条件成功");
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
    }

    private void syncAlarmTrigger(BaseEcmcAlarmTrigger at) throws Exception {
        String objectJSON = JSONObject.toJSONString(at);
        try {
            jedisUtil.set(RedisKey.ECMC_ALARM_TRIGGER + at.getId(), objectJSON);
        } catch (Exception e) {
            throw e;
        }
    }

    @DELETE
	@Override
	public List<Map> getAllAlarmHost(String ruleid,String monitorType) {
		List<BaseCloudVm> list =ecmcAlarmRuleDao.getAllListAlarmObject(ruleid,"0","1");
		List<Map> listMap=new ArrayList<>();
		for(BaseCloudVm vm :list){
			Map map=new HashMap<>();
			BeanUtils.beanToMap(map, vm);
			listMap.add(map);
		}
		
		return listMap;
	}
	/**
	 * 查询出规则下所有未添加的报警对象ID
	 * @Author: duanbinbin
	 * @param ruleId
	 * @param monitorType
	 * @return
	 *<li>Date: 2017年3月9日</li>
	 */
	@Override
	public List<String> getAllObjsToAdd(String ruleId,String monitorType) {
		List<String> idList = new ArrayList<String>();
		StringBuffer sql = new StringBuffer("");
		if (monitorType.equals(RedisNodeIdConstant.ECMC_MONITOR_VM_NODEID)) {
			sql.append(" SELECT vm.vm_id ");
			sql.append(" FROM cloud_vm vm ");
			sql.append(" WHERE vm.is_deleted = '0' AND vm.is_visable = '1' AND vm.vm_id NOT IN (");
		}else if(monitorType.equals(RedisNodeIdConstant.ECMC_MONITOR_TYPE_CLOUDDATA)){
			sql.append(" SELECT rds.rds_id ");
			sql.append(" FROM cloud_rdsinstance rds ");
			sql.append(" WHERE rds.is_deleted = '0' AND rds.is_visible = '1' AND rds.rds_id NOT IN (");
		}else if(monitorType.equals(RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDCOMMON)){
			sql.append(" SELECT ld.pool_id ");
			sql.append(" FROM cloud_ldpool ld ");
			sql.append(" WHERE ld.is_visible = '1' AND ld.mode = '0' AND ld.pool_id NOT IN (");
		}else if(monitorType.equals(RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDMASTER)){
			sql.append(" SELECT ld.pool_id ");
			sql.append(" FROM cloud_ldpool ld ");
			sql.append(" WHERE ld.is_visible = '1' AND ld.mode = '1' AND ld.pool_id NOT IN (");
		}else{
			return idList;
		}
		sql.append(" SELECT eao.ao_objectid FROM ecmc_alarmobject eao ");
		sql.append(" WHERE eao.ao_alarmruleid = ? AND eao.ao_type= ? ");
		sql.append(")");
		Query query = ecmcAlarmRuleDao.createSQLNativeQuery(sql.toString(), ruleId,monitorType);
        List list = query.getResultList();
        if(list.isEmpty()){
        	return idList;
        }
        for (int i = 0; i < list.size(); i++) {
            String objectId = (String) list.get(i);
            idList.add(objectId);
        }
		return idList;
	}
	/**
     * 删除负载均衡资源所做处理
     * @Author: duanbinbin
     * @param poolId
     *<li>Date: 2017年3月6日</li>
     */
    @Override
    public void clearPoolMsgAfterDeletePool(String poolId){
    	this.cleanAlarmDataAfterDeletingObject(poolId);
    	mongoTemplate.remove(new org.springframework.data.mongodb.core.query.Query(
    			Criteria.where("poolId").is(poolId)), MongoCollectionName.MONITOR_LD_POOL_MEMBER_EXP);
    }

	@Override
	public void clearExpAfterDeleteMember(String memId) {
    	mongoTemplate.remove(new org.springframework.data.mongodb.core.query.Query(
    			Criteria.where("memberId").is(memId)), MongoCollectionName.MONITOR_LD_POOL_MEMBER_EXP);
	}

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
	 *<li>Date: 2017年3月21日</li>
	 */
	@Override
	public void doAfterUnbundHealth(String poolId, String healthId) {
		org.springframework.data.mongodb.core.query.Query query=new org.springframework.data.mongodb.core.query.Query();
		query.addCriteria(Criteria.where("poolId").is(poolId));
		query.addCriteria(Criteria.where("healthId").is(healthId));
		mongoTemplate.updateMulti(query, new Update().set("isRepair", "0"), 	//修改所有符合条件的记录
        		 MongoCollectionName.MONITOR_LD_POOL_MEMBER_EXP);
	}

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
