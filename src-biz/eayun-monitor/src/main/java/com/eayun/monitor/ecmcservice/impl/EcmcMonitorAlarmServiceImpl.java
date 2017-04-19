package com.eayun.monitor.ecmcservice.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.RedisNodeIdConstant;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.constant.RedisKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.monitor.bean.EcmcAlarmMsgExcel;
import com.eayun.monitor.bean.MonitorAlarmUtil;
import com.eayun.monitor.dao.EcmcAlarmMessageDao;
import com.eayun.monitor.dao.EcmcAlarmTriggerDao;
import com.eayun.monitor.dao.EcmcMonitorAlarmItemDao;
import com.eayun.monitor.ecmcservice.EcmcMonitorAlarmService;
import com.eayun.monitor.model.BaseEcmcAlarmMessage;
import com.eayun.monitor.model.BaseEcmcAlarmObject;
import com.eayun.monitor.model.BaseEcmcAlarmTrigger;
import com.eayun.monitor.model.BaseEcmcMonitorAlarmItem;
import com.eayun.monitor.model.EcmcAlarmMessage;
import com.eayun.monitor.model.EcmcAlarmObject;
import com.eayun.monitor.model.EcmcAlarmTrigger;
import com.eayun.monitor.model.EcmcMonitorAlarmItem;

@Service
@Transactional
public class EcmcMonitorAlarmServiceImpl implements EcmcMonitorAlarmService {
	
	private static final Logger   log = LoggerFactory.getLogger(EcmcMonitorAlarmServiceImpl.class);

	@Autowired
	private EcmcMonitorAlarmItemDao ecmcMonitorAlarmItemDao;
	@Autowired
    private EcmcAlarmTriggerDao ecmcAlarmTriggerDao;
	@Autowired
    private EcmcAlarmMessageDao ecmcAlarmMessageDao;
	@Autowired
    private JedisUtil jedisUtil;
	
	@Autowired
    private MongoTemplate mongoTemplate;
	
	private static final Logger logger = LoggerFactory.getLogger(EcmcMonitorAlarmServiceImpl.class);
	
	@Override
	public List<EcmcAlarmMsgExcel> getEcmcAlarmMsgExcel() {
		log.info("获取运维所有报警信息用于导出excel");
		StringBuffer hql = new StringBuffer();
		hql.append(" SELECT msg.am_monitortype,");
		hql.append(" msg.am_detail,msg.am_time,msg.am_isprocessed,");
        hql.append(" CASE WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECMC_MONITOR_VM_NODEID+"' THEN vm.vm_name ")
        	.append(" WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECMC_MONITOR_TYPE_CLOUDDATA+"' THEN rds.rds_name ")
        	.append(" WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDCOMMON+"' OR ")
        	.append(" msg.am_monitortype = '"+RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDMASTER+"' THEN ld.pool_name ")
        	.append(" WHEN msg.am_monitortype = '"+RedisNodeIdConstant.ECMC_MONITOR_TYPE_API+"' THEN '——' ")
        	.append(" END AS objName, ");
        hql.append(" dc.dc_name AS dcName, ");
        hql.append(" cus.cus_org, ");
        hql.append(" prj.prj_name ");
        hql.append(" FROM ecmc_alarmmessage msg ");
        hql.append(" LEFT JOIN dc_datacenter dc ON msg.dc_id = dc.id ");
        hql.append(" LEFT JOIN sys_selfcustomer cus ON msg.cus_id = cus.cus_id ");
        hql.append(" LEFT JOIN cloud_project prj ON msg.prj_id = prj.prj_id ");
        
        hql.append(" LEFT JOIN cloud_vm vm ON vm.vm_id = msg.am_objectid AND msg.am_monitortype = '"+RedisNodeIdConstant.ECMC_MONITOR_VM_NODEID+"' ");
        hql.append(" LEFT JOIN cloud_rdsinstance rds ON rds.rds_id = msg.am_objectid AND msg.am_monitortype = '"+RedisNodeIdConstant.ECMC_MONITOR_TYPE_CLOUDDATA+"' ");
        hql.append(" LEFT JOIN cloud_ldpool ld ON ld.pool_id = msg.am_objectid AND ( msg.am_monitortype = '"+RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDCOMMON+"' OR ")
           .append(" msg.am_monitortype = '"+RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDMASTER+"' )");
		String sql = hql.toString();
        javax.persistence.Query query = ecmcMonitorAlarmItemDao.createSQLNativeQuery(sql, null);
		List<EcmcAlarmMsgExcel> msgList = new ArrayList<EcmcAlarmMsgExcel>();
		List list = query.getResultList();
        for (int i = 0; i < list.size(); i++) {
        	Object[] obj = (Object[]) list.get(i);
        	EcmcAlarmMsgExcel msgExcel = new EcmcAlarmMsgExcel();
        	String monitorName = this.getMonitorItemByNodeID(String.valueOf(obj[0]));
        	msgExcel.setType(monitorName);
        	msgExcel.setAlarmDetail(String.valueOf(obj[1]));
        	Date time = (Date)obj[2];
        	msgExcel.setAlarmTime(DateUtil.dateToStringTwo(time));
        	String sign = "未处理";
        	if(String.valueOf(obj[3]).equals("1")){
        		sign = "已处理";
        	}
        	msgExcel.setAlarmSign(sign);
        	msgExcel.setObjName(String.valueOf(obj[4]));
        	msgExcel.setDcName(null == obj[5]?"——":String.valueOf(obj[5]));
        	msgExcel.setCusName(null == obj[6]?"——":String.valueOf(obj[6]));
        	msgExcel.setProjectName(null == obj[7]?"——":String.valueOf(obj[7]));
        	msgList.add(msgExcel);
        }
		return msgList;
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
	
	@Override
	public boolean removeAlarmMsgByIds(List<String> checkedIds) {
		log.info("批量消除报警信息");
		SimpleDateFormat isNeedNotifyTimeFormat = new SimpleDateFormat("yyyyMMddHHmm") ;
		for (String id : checkedIds) {
            BaseEcmcAlarmMessage msg = ecmcAlarmMessageDao.findOne(id);
			//将状态设置为已解决再重新保存到数据库系统之中
            msg.setIsProcessed("1");
            ecmcAlarmMessageDao.saveOrUpdate(msg);
			//针对API消除报警，只有消除掉对应Redis中的报警标识才能达到消除报警的作用
			//删除对应维度信息与报警规则ID的缓存标识
			if ("0010003003".equals(msg.getMonitorType())){
				String redisKeyString = RedisKey.API_MONITORINGALARM_ISNEEDNOTIFY + msg.getCusId() + ":" + msg.getIp()    + ":" + msg.getDcId()  + ":" + msg.getAm_alarmtriggerid() ;
				try {
					jedisUtil.delete(redisKeyString);
				}catch (Exception e){
					log.error(e.getMessage(), e);
				}
			}
            //更新报警信息之后，需要对应将产生报警信息的监控报警项的是否已报警置为0，同时修改上次修改时间。
            String monitorAlarmItemId = msg.getMonitorAlarmItemId();
            if(monitorAlarmItemId!=null){
                BaseEcmcMonitorAlarmItem alarmItem = ecmcMonitorAlarmItemDao.findOne(monitorAlarmItemId);
                if(alarmItem!=null){
                	alarmItem.setIsNotified("0");
                	alarmItem.setModifiedTime(new Date());
                	ecmcMonitorAlarmItemDao.saveOrUpdate(alarmItem);
                }
            }
        }
        return true;
	}
	/**
	 * 更新运维监控报警项对象信息
	 * @param ecmcMonitorAlarmItem
	 */
	@Override
    public void updateEcmcMonitorAlarmItem(EcmcMonitorAlarmItem ecmcMonitorAlarmItem) {
        BaseEcmcMonitorAlarmItem baseEcmcMonAlarmItem = new BaseEcmcMonitorAlarmItem();
        BeanUtils.copyPropertiesByModel(baseEcmcMonAlarmItem, ecmcMonitorAlarmItem);
        ecmcMonitorAlarmItemDao.saveOrUpdate(baseEcmcMonAlarmItem);
    }

	/**
	 * 根据运维报警对象删除监控报警项
	 * mysql及mongo数据
	 * @param alarmObject
	 */
	@Override
	public void deleteMonAlarmItemByAlarmObject(EcmcAlarmObject alarmObject) {
		log.info("删除对象的监控报警项");
        String hql = " delete BaseEcmcMonitorAlarmItem where objectId= ? and alarmRuleId = ? ";
        ecmcMonitorAlarmItemDao.executeUpdate(hql, alarmObject.getId(),alarmObject.getAlarmRuleId());
        
        mongoTemplate.remove(new org.springframework.data.mongodb.core.query.Query
        		(Criteria.where("alarmObjectId").is(alarmObject.getId())), MongoCollectionName.ECMC_MONITOR_ALARM_ITEM);
	}

	/**
	 * 根据运维报警规则id删除监控报警项
	 * @param ruleId
	 */
	@Override
	public void deleteMonAlarmItemByRuleId(String ruleId) {
		String hql = " delete BaseEcmcMonitorAlarmItem where alarmRuleId=?";
		ecmcMonitorAlarmItemDao.executeUpdate(hql, ruleId);
	}
	/**
	 * 根据运维报警规则id更新其监控报警项
	 */
	@Override
	public void updateEcmcMonItemByRuleId(String ruleId) {
		//1.根据报警规则找到所有的监控报警项，全部删除。
		this.deleteMonAlarmItemByRuleId(ruleId);
        
        //2.根据报警规则的新触发条件和报警对象，重新设置监控报警项
		this.addEcmcMonItemByRuleId(ruleId);
	}
	/**
	 * 根据运维报警规则id查询所有报警对象，并添加监控报警项
	 * @param ruleId
	 */
	@SuppressWarnings("unchecked")
	public void addEcmcMonItemByRuleId(String ruleId) {
        String queryAlmObj = " from BaseEcmcAlarmObject where alarmRuleId = ?";
        List<BaseEcmcAlarmObject> almObjList = ecmcMonitorAlarmItemDao.find(queryAlmObj, ruleId);
        for (BaseEcmcAlarmObject baseAlarmObject : almObjList) {
            EcmcAlarmObject alarmObject = new EcmcAlarmObject();
            BeanUtils.copyPropertiesByModel(alarmObject, baseAlarmObject);
            
            this.addEcmcMonItemByObject(alarmObject);
        }
    }
	/**
	 * 添加报警对象的监控报警项
	 * @param alarmObject
	 * @throws AppException
	 */
	@Override
	public void addEcmcMonItemByObject(EcmcAlarmObject alarmObject) throws AppException{
        List<EcmcAlarmTrigger> triggerList = getEcmcTriggerListByRuleId(alarmObject.getAlarmRuleId());
        for (EcmcAlarmTrigger alarmTrigger : triggerList) {
            BaseEcmcMonitorAlarmItem ecmcMonItem = new BaseEcmcMonitorAlarmItem();
            
            ecmcMonItem.setObjectId(alarmObject.getId());
            ecmcMonItem.setObjType(alarmObject.getAoType());
            ecmcMonItem.setTriggerId(alarmTrigger.getId());
            ecmcMonItem.setAlarmRuleId(alarmObject.getAlarmRuleId());
            ecmcMonItem.setIsNotified("0");
            ecmcMonItem.setModifiedTime(new Date());
            
            ecmcMonitorAlarmItemDao.saveEntity(ecmcMonItem);
        }
    }

	/**
	 * 批量保存报警规则产生的报警信息，为了不会出现报警信息部分茶如失败的情况，故将所有的操作放到一个事务之中
	 * @param alarmMessages
     */
	@Override
	public void saveAlarmMessages(Map<String, List<BaseEcmcAlarmMessage>> alarmMessages) {
		try {
			for (Map.Entry<String, List<BaseEcmcAlarmMessage>> entity : alarmMessages.entrySet()){
				for (BaseEcmcAlarmMessage message : entity.getValue()){
					EcmcAlarmMessage ecmcAlarmMessage = new EcmcAlarmMessage();
					BeanUtils.copyProperties(ecmcAlarmMessage, message);
					addEcmcAlarmMessage(ecmcAlarmMessage);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * 根据运维报警规则id查询触发条件
	 * @param ruleId
	 * @return
	 * @throws AppException
	 */
	@SuppressWarnings("unchecked")
	public List<EcmcAlarmTrigger> getEcmcTriggerListByRuleId(String ruleId) throws AppException {
        List<EcmcAlarmTrigger> ecmcTriggerList = new ArrayList<EcmcAlarmTrigger>();
        StringBuffer sb = new StringBuffer();
        sb.append(" from BaseEcmcAlarmTrigger where alarmRuleId = ?");
        List<BaseEcmcAlarmTrigger> baseEcmcTriggerList = ecmcAlarmTriggerDao.find(sb.toString(),ruleId);
        for (BaseEcmcAlarmTrigger baseEcmcTrigger : baseEcmcTriggerList) {
        	EcmcAlarmTrigger alarmTrigger = new EcmcAlarmTrigger();
            BeanUtils.copyPropertiesByModel(alarmTrigger, baseEcmcTrigger);
            ecmcTriggerList.add(alarmTrigger);
        }
        return ecmcTriggerList;
    }

	/**
	 * 查询所有运维监控报警项
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<EcmcMonitorAlarmItem> getAllEcmcMonAlarmItemList() {
		List<EcmcMonitorAlarmItem> ecmcMonitorAlarmItemList = new ArrayList<EcmcMonitorAlarmItem>();
		String hql = " from BaseEcmcMonitorAlarmItem ";
		List<BaseEcmcMonitorAlarmItem> baseMonitorAlarmItemList = ecmcMonitorAlarmItemDao.find(hql);
		for(BaseEcmcMonitorAlarmItem baseMonitorAlarmItem : baseMonitorAlarmItemList){
			EcmcMonitorAlarmItem ecmcMonitorAlarmItem = new EcmcMonitorAlarmItem();
			BeanUtils.copyPropertiesByModel(ecmcMonitorAlarmItem, baseMonitorAlarmItem);
			ecmcMonitorAlarmItemList.add(ecmcMonitorAlarmItem);
		}
		return ecmcMonitorAlarmItemList;
	}
	/**
	 * 添加报警信息对象记录
	 * @param ecmcAlarmMessage
	 */
	@Override
    public void addEcmcAlarmMessage(EcmcAlarmMessage ecmcAlarmMessage) {
        BaseEcmcAlarmMessage BaseecmcAlarmMsg = new BaseEcmcAlarmMessage();
        BeanUtils.copyPropertiesByModel(BaseecmcAlarmMsg, ecmcAlarmMessage);
        ecmcAlarmMessageDao.saveEntity(BaseecmcAlarmMsg);
    }
}
