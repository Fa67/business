package com.eayun.monitor.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.RedisNodeIdConstant;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.redis.JedisUtil;
import com.eayun.monitor.bean.AlarmMsgExcel;
import com.eayun.monitor.bean.MonitorAlarmUtil;
import com.eayun.monitor.dao.AlarmMessageDao;
import com.eayun.monitor.dao.MonitorAlarmItemDao;
import com.eayun.monitor.model.AlarmMessage;
import com.eayun.monitor.model.AlarmObject;
import com.eayun.monitor.model.AlarmTrigger;
import com.eayun.monitor.model.BaseAlarmMessage;
import com.eayun.monitor.model.BaseAlarmObject;
import com.eayun.monitor.model.BaseMonitorAlarmItem;
import com.eayun.monitor.model.MonitorAlarmItem;
import com.eayun.monitor.service.AlarmService;
import com.eayun.monitor.service.MonitorAlarmService;

@Service
@Transactional
public class MonitorAlarmServiceImpl implements MonitorAlarmService {
    
    private static final Logger   log = LoggerFactory.getLogger(MonitorAlarmServiceImpl.class);
    @Autowired
    private MonitorAlarmItemDao monitorAlarmItemDao;
    @Autowired
    private AlarmMessageDao alarmMsgDao;
    @Autowired
    private AlarmService alarmService;//XXX alarmservice中引入了monitoralarmservice,同时这里也注入了alarmservice，为什么没有提示循环依赖?
    
    @Autowired
    private JedisUtil jedisUtil;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Override
    public void addMonitorAlarmItemByAlarmRule(String alarmRuleId) {
        String queryAlmObj = " from BaseAlarmObject where alarmRuleId=?";
        List<BaseAlarmObject> almObjList = monitorAlarmItemDao.find(queryAlmObj, alarmRuleId);
        for (BaseAlarmObject baseAlarmObject : almObjList) {
            AlarmObject alarmObject = new AlarmObject();
            BeanUtils.copyProperties(baseAlarmObject, alarmObject);
            addMonitorAlarmItemByAlarmObject(alarmObject);
        }
    }
    
    @Override
    public void updateMonitorAlarmItemByAlarmRule(String alarmRuleId) {
        log.info("更新监控报警项开始");
        
        //1.根据报警规则找到所有的监控报警项，全部删除。
        deleteMonitorAlarmItemByAlarmRule(alarmRuleId);
        
        //2.根据报警规则找到所有的报警对象，重新添加监控报警项
        addMonitorAlarmItemByAlarmRule(alarmRuleId);
    }
    
    @Override
    public void deleteMonitorAlarmItemByAlarmRule(String alarmRuleId) {
        String hql = " delete BaseMonitorAlarmItem where alarmRuleId=?";
        monitorAlarmItemDao.executeUpdate(hql, alarmRuleId);
    }
    /**
     * 清除报警对象的监控项MySQL及mongo数据
     * @Author: duanbinbin
     * @param alarmObject
     *<li>Date: 2017年3月17日</li>
     */
    @Override
    public void deleteMonitorAlarmItemByAlarmObject(AlarmObject alarmObject) {
        log.info("删除监控报警项开始");
        String hql = " delete BaseMonitorAlarmItem where objectId=? and alarmRuleId=?";
        monitorAlarmItemDao.executeUpdate(hql,  alarmObject.getId(),alarmObject.getAlarmRuleId());
        
        mongoTemplate.remove(new org.springframework.data.mongodb.core.query.Query
        		(Criteria.where("objectId").is(alarmObject.getId())), MongoCollectionName.MONITOR_ALARM_ITEM);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<MonitorAlarmItem> getMonitorAlarmItemList() {
        List<MonitorAlarmItem> itemList = new ArrayList<MonitorAlarmItem>();
        String sql = " from BaseMonitorAlarmItem ";
        List<BaseMonitorAlarmItem> resultList = monitorAlarmItemDao.find(sql);
        for (BaseMonitorAlarmItem baseMonitorAlarmItem : resultList) {
            MonitorAlarmItem item = new MonitorAlarmItem();
            BeanUtils.copyProperties(baseMonitorAlarmItem, item);
            itemList.add(item);
        }
        return itemList;
    }
    
    /**
     * 当为报警规则添加一条报警对象时，需要在监控报警项表中插入该报警对象所属报警规则的触发条件数目的数据。
     * 监控报警项用于后面计算报警对象是否应当产生报警信息。
     * @param alarmObject
     * @throws AppException
     */
    @Override
    public void addMonitorAlarmItemByAlarmObject(AlarmObject alarmObject) throws AppException{
        log.info("添加监控报警项开始");
        List<AlarmTrigger> triggerList = alarmService.getAlarmTriggerListByRuleId(alarmObject.getAlarmRuleId());
        for (AlarmTrigger alarmTrigger : triggerList) {
            BaseMonitorAlarmItem monitorAlarmItem = new BaseMonitorAlarmItem();
            monitorAlarmItem.setObjectId(alarmObject.getId());
            monitorAlarmItem.setTriggerId(alarmTrigger.getId());
            monitorAlarmItem.setAlarmRuleId(alarmObject.getAlarmRuleId());
            monitorAlarmItem.setIsNotified("0");
            monitorAlarmItem.setModifiedTime(new Date());
            monitorAlarmItem.setMonitorType(alarmObject.getMonitorType());
            
            monitorAlarmItemDao.saveEntity(monitorAlarmItem);
        }
    }

    @Override
    public void updateMonitorAlarmItem(MonitorAlarmItem monitorAlarmItem) {
        log.info("更新监控报警项");
        BaseMonitorAlarmItem baseItem = new BaseMonitorAlarmItem();
        BeanUtils.copyProperties(monitorAlarmItem, baseItem);
        monitorAlarmItemDao.saveOrUpdate(baseItem);
    }

    @Override
    public void addAlarmMessage(AlarmMessage alarmMsg) {
        BaseAlarmMessage baseMsg = new BaseAlarmMessage();
        BeanUtils.copyProperties(alarmMsg, baseMsg);
        alarmMsgDao.saveEntity(baseMsg);
        BeanUtils.copyProperties(baseMsg, alarmMsg);
    }

    @Override
    public boolean eraseAlarmMsgByIds(List<String> checkedIds) {
        // TODO 消除报警时要将产生该报警信息的监控报警项是否已报警置为0 则需要调整BaseAlarmMessage的表结构，冗余一下monitorAlarmItem的id
        for (String id : checkedIds) {
            BaseAlarmMessage msg = alarmMsgDao.findOne(id);
            msg.setIsProcessed("1");
            alarmMsgDao.saveOrUpdate(msg);
            
            //更新报警信息之后，需要对应将产生报警信息的监控报警项的是否已报警置为0，同时修改上次修改时间。
            String monitorAlarmItemId = msg.getMonitorAlarmItemId();
            if(monitorAlarmItemId!=null){
                BaseMonitorAlarmItem baseMonitorAlarmItem = monitorAlarmItemDao.findOne(monitorAlarmItemId);
                if(baseMonitorAlarmItem!=null){
                    baseMonitorAlarmItem.setIsNotified("0");
                    baseMonitorAlarmItem.setModifiedTime(new Date());
                    monitorAlarmItemDao.saveOrUpdate(baseMonitorAlarmItem);
                }
            }
        }
        return true;
    }
/**
 * @Author: duanbinbin
 * @param sessionUser
 * @return
 *<li>Date: 2017年3月2日</li>
 */
    @Override
    public List<AlarmMsgExcel> getAlarmMessagesByUserIdForExcel(SessionUserInfo sessionUser) {
        log.info("获取当前用户可见的报警信息列表用以导出Excel");
        String userId = sessionUser.getIsAdmin()?sessionUser.getCusId():sessionUser.getUserId();
        List<Object> paramList = new ArrayList<Object>();
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ")
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
        sb.append("ORDER BY msg.am_time DESC");
        Query query = alarmMsgDao.createSQLNativeQuery(sb.toString(), paramList.toArray());
        List resultList = (List) query.getResultList();
        List<AlarmMsgExcel> csvList = new ArrayList<AlarmMsgExcel>();
        for(int i=0; i<resultList.size(); i++){
            Object[] objs = (Object[]) resultList.get(i);
            AlarmMsgExcel msg = new AlarmMsgExcel();
            msg.setVmName(String.valueOf(objs[0]));
            msg.setProjectName(String.valueOf(objs[1]));
            
            String alarmTypeName = this.getMonitorItemByNodeID(String.valueOf(objs[2]));
            msg.setAlarmType(alarmTypeName);
            String monitorName = this.getMonitorItemByNodeID(String.valueOf(objs[3]));
            msg.setMonitorType(monitorName);
            msg.setAlarmDetail(String.valueOf(objs[4]));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timeStr = sdf.format(objs[5]);
            msg.setAlarmTime(timeStr);
            String isProcessed = String.valueOf(objs[6]);
            String alarmSign = isProcessed.equals("0")?"未处理":"已处理";
            msg.setAlarmSign(alarmSign);
            
            csvList.add(msg);
        }
        return csvList;
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

}
