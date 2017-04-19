package com.eayun.monitor.thread;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.RedisNodeIdConstant;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.DateUtil;
import com.eayun.mail.service.MailService;
import com.eayun.monitor.bean.LdPoolMonitorDetail;
import com.eayun.monitor.bean.MonitorAlarmUtil;
import com.eayun.monitor.bean.MonitorMngData;
import com.eayun.monitor.ecmcservice.EcmcAlarmService;
import com.eayun.monitor.ecmcservice.EcmcMonitorAlarmService;
import com.eayun.monitor.model.EcmcAlarmContact;
import com.eayun.monitor.model.EcmcAlarmMessage;
import com.eayun.monitor.model.EcmcAlarmObject;
import com.eayun.monitor.model.EcmcAlarmTrigger;
import com.eayun.monitor.model.EcmcContact;
import com.eayun.monitor.model.EcmcMonitorAlarmItem;
import com.eayun.sms.service.SMSService;
import com.mongodb.BasicDBObject;

public class EcmcStatusCalculateThread implements Runnable {
	
	private static final Logger log = LoggerFactory.getLogger(EcmcStatusCalculateThread.class);

    private EcmcMonitorAlarmItem    ecmcMonitorAlarmItem;
    private EcmcMonitorAlarmService ecmcMonitorAlarmService;
    private EcmcAlarmService        ecmcAlarmService;
    private MongoTemplate       mongoTemplate;
    private JedisUtil           jedisUtil;
    private Date                currentTime;
    private SMSService          smsService;
    private MailService         mailService;

    public EcmcStatusCalculateThread(EcmcMonitorAlarmItem ecmcMonitorAlarmItem,
			EcmcMonitorAlarmService ecmcMonitorAlarmService,
			EcmcAlarmService ecmcAlarmService, MongoTemplate mongoTemplate,
			JedisUtil jedisUtil, Date currentTime, SMSService smsService,
			MailService mailService) {
    	this.ecmcMonitorAlarmItem = ecmcMonitorAlarmItem;
        this.ecmcMonitorAlarmService = ecmcMonitorAlarmService;
        this.ecmcAlarmService = ecmcAlarmService;
        this.mongoTemplate = mongoTemplate;
        this.jedisUtil = jedisUtil;
        this.currentTime = currentTime;
        this.smsService = smsService;
        this.mailService = mailService;
	}

	@Override
    public void run() {
		log.info("运维报警状态计算线程启动");
		String alarmObjectId = ecmcMonitorAlarmItem.getObjectId();
		String triggerId = ecmcMonitorAlarmItem.getTriggerId();
		String objType = ecmcMonitorAlarmItem.getObjType();		//监控项类型
		
		EcmcAlarmObject ecmcAlarmObject = ecmcAlarmService.getEcmcObjByObjId(alarmObjectId);
		if(null == ecmcAlarmObject || null == ecmcAlarmObject.getId() || ecmcAlarmObject.getIsDeleted()){
			log.error("没有查询到报警对象的信息，此次线程结束,monitorAlarmItem:"+JSONObject.toJSONString(ecmcMonitorAlarmItem));
        	return;
        }
		EcmcAlarmTrigger ecmcAlarmTrigger = ecmcAlarmService.getEcmcAlarmTriggerByTriId(triggerId);
		if(null == ecmcAlarmTrigger || null == ecmcAlarmTrigger.getId()){
			log.error("没有查询到触发条件的信息，此次线程结束,monitorAlarmItem:"+JSONObject.toJSONString(ecmcMonitorAlarmItem));
        	return;
        }
		
		String zbNodeId = ecmcAlarmTrigger.getZb();
		MonitorMngData monitorMngData = getZbMngDataInfo(zbNodeId);//指标信息
		
		//检查最新一条监控指标是否满足触发条件
		boolean isOverTrigger = checkLatestAlarmItemStatus(ecmcAlarmObject, ecmcAlarmTrigger, zbNodeId, currentTime);
		BasicDBObject jsonStatus = createStatusForMongo(alarmObjectId, objType, triggerId, currentTime, isOverTrigger);
		//将最新的监控项状态信息存入mongo中
		mongoTemplate.insert(jsonStatus, MongoCollectionName.ECMC_MONITOR_ALARM_ITEM);
		String statusId = jsonStatus.getString("_id");
		
		int durations = ecmcAlarmTrigger.getLastTime();//触发条件规定的持续时间，单位：s
		Date baselineStartTime = new Date(currentTime.getTime() - durations * 1000);//由当前时间向前推一段持续时间段
		Date itemModifiedTime = ecmcMonitorAlarmItem.getModifiedTime();//报警监控项的上次修改时间
		
		if (baselineStartTime.getTime() < itemModifiedTime.getTime()) {
			//上次修改时间距此时不超过触发条件持续时间，则直接选择不报警
            return;
        } else {
        	//判断是否应该报警,即在持续时间内指标数据全都违反了阈值规定
        	boolean needToAlarm = isNeedToAlarm(alarmObjectId,objType , triggerId, durations, baselineStartTime,currentTime,statusId);
        	if(needToAlarm){
        		List<EcmcAlarmContact> ecmcContactList = ecmcAlarmService.getEcmcConsByRuleId(ecmcAlarmObject.getAlarmRuleId());
        		if (ecmcMonitorAlarmItem.getIsNotified().equals("0")) {//需要报警 且 未报警
        			try {
						EcmcAlarmMessage ecmcAlarmMessage = produceEcmcAlarmMessage(ecmcMonitorAlarmItem, monitorMngData, ecmcAlarmObject, ecmcAlarmTrigger);
						notifyAlarmContacts(ecmcContactList, monitorMngData, ecmcAlarmObject, ecmcAlarmTrigger, ecmcAlarmMessage);//通知联系人
        			} catch (Exception e) {
        				log.error("ecmc-AlarmMessage",e);
                        throw new AppException("发送运维报警信息异常");
					}
        			ecmcMonitorAlarmItem.setIsNotified("1");//发送一次报警信息后状态置为已报警
        			ecmcMonitorAlarmItem.setModifiedTime(currentTime);//发送一次报警信息则修改一次最新修改时间
        		}else{
                    long intervals = currentTime.getTime()-itemModifiedTime.getTime();
                    // 需要报警，但已产生过报警；若连续24h持续触发重新报警
                    if(intervals%(1000*60*60*24)==0){
                    	try {
                            EcmcAlarmMessage EcmcAlarmMsg = ecmcAlarmService.getLatestEcmcAlarmMsgByItemId(ecmcMonitorAlarmItem.getId());
                            notifyAlarmContacts(ecmcContactList, monitorMngData, ecmcAlarmObject, ecmcAlarmTrigger, EcmcAlarmMsg);
                        } catch (Exception e) {
                            log.error("ecmc-AlarmMessage24h",e);
                            throw new AppException("发送运维报警信息异常");
                        }
                    }
        		}
        	}else{
        		//如果不需要报警，且已产生报警，则将是否产生报警状态置为“未报警”，下次再遇到需要报警的情况，还会产生报警信息。
                if (ecmcMonitorAlarmItem.getIsNotified().equals("1")) {
                	ecmcMonitorAlarmItem.setIsNotified("0");
                }
        	}
        	//更新此项监控报警项信息，改变的只有两个字段：最新一次修改时间和是否报警标示
        	ecmcMonitorAlarmService.updateEcmcMonitorAlarmItem(ecmcMonitorAlarmItem);
        }
	}
	/**
	 * 组织报警邮件、短信模板，并发送
	 * @Author: duanbinbin
	 * @param ecmcContactList
	 * @param monitorMngData	指标信息
	 * @param ecmcAlarmObject
	 * @param ecmcAlarmTrigger
	 * @param ecmcAlarmMessage
	 * @return
	 * @throws Exception
	 *<li>Date: 2017年3月3日</li>
	 */
	private boolean notifyAlarmContacts(List<EcmcAlarmContact> ecmcContactList, MonitorMngData monitorMngData,
			EcmcAlarmObject ecmcAlarmObject, EcmcAlarmTrigger ecmcAlarmTrigger, EcmcAlarmMessage ecmcAlarmMessage) throws Exception {
		log.info("发送运维报警信息和邮件至运维报警联系人");
		List<String> mails = new ArrayList<String>();
		List<String> mobiles = new ArrayList<String>();
		
		for (EcmcAlarmContact ecmcAlarmContact : ecmcContactList) {
			String contactMethod = ecmcAlarmContact.getContactMethod();
			EcmcContact ecmcContact = ecmcAlarmService.getEcmcContactById(ecmcAlarmContact.getContactId());
			//TODO 如果增加短信包，则需要此时对contact进行某些判断，判断是否应该发送
			if (contactMethod.equals("短信 邮件")) {
				mails.add(ecmcContact.getEmail());
				mobiles.add(ecmcContact.getPhone());
			} else if (contactMethod.equals("短信")) {
				mobiles.add(ecmcContact.getPhone());
			} else if (contactMethod.equals("邮件")) {
				mails.add(ecmcContact.getEmail());
			}
		}
		
		String objType = ecmcAlarmObject.getAoType();
		String monitorName = MonitorAlarmUtil.getMonitorNameByType(objType);
		
		String title = "易云"+monitorName+"报警提醒";
		StringBuffer smsContent = new StringBuffer();
		smsContent.append(ecmcAlarmObject.getDcName());
		smsContent.append("数据中心，客户");
		smsContent.append(ecmcAlarmObject.getCusName());
		smsContent.append("在易云租用的"+monitorName);
		smsContent.append(ecmcAlarmObject.getObjName());
		smsContent.append("发生报警，")
		.append(monitorMngData.getName())
		.append(ecmcAlarmTrigger.getOperator())
		.append(ecmcAlarmTrigger.getThreshold())
		.append(ecmcAlarmTrigger.getUnit())
		.append("持续" + getLastTime(ecmcAlarmTrigger.getLastTime()));
		
		String mailContent = getMailContent(ecmcAlarmObject, ecmcAlarmMessage);
		if(mails.size()>0){
			mailService.send(title, mailContent, mails);
		}
		if(mobiles.size()>0){
			smsService.send(smsContent.toString(), mobiles);
		}
		return true;
	
	}
	/**
	 * 组织邮件发送模板
	 * @param ecmcAlarmObject
	 * @param ecmcAlarmMessage
	 * @return
	 */
	private String getMailContent(EcmcAlarmObject ecmcAlarmObject, EcmcAlarmMessage ecmcAlarmMessage) {
		//模板确定
		String objType = ecmcAlarmObject.getAoType();
		EcmcAlarmMailTemplate template = EcmcAlarmMailTemplate.getInstance();
        String dcName = "";
        dcName = ecmcAlarmObject.getDcName();
        String alarmTime = "";
        alarmTime = DateUtil.dateToString(ecmcAlarmMessage.getTime());
        String objTypeName = MonitorAlarmUtil.getMonitorNameByType(objType);
        
        String strContent=String.valueOf(template.getMailHtml());
        strContent=strContent.replace("{objType}", objTypeName+"名称");
        strContent=strContent.replace("{dcName}", dcName);
        strContent=strContent.replace("{objName}", ecmcAlarmObject.getObjName());
        strContent=strContent.replace("{detail}", ecmcAlarmMessage.getDetail());
        strContent=strContent.replace("{alarmTime}", alarmTime);
        return strContent;
    }
	
	/**
	 * 检测前一分钟对象的报警监控项下的某一指标的数据是否超出触发条件的阈值
	 * @param ecmcAlarmObject
	 * @param ecmcAlarmTrigger
	 * @param monitorMngData
	 * @param currentTime
	 * @return
	 */
	private boolean checkLatestAlarmItemStatus(EcmcAlarmObject ecmcAlarmObject, EcmcAlarmTrigger ecmcAlarmTrigger,
			String zbNodeId, Date currentTime) {
		boolean isOverTrigger = false;
		float threshold = ecmcAlarmTrigger.getThreshold();
        String operator = ecmcAlarmTrigger.getOperator();
        
		//判断是否为负载均衡监控项，负载均衡指标存储和判断时稍有不同，分母为0则判定一定为不满足条件
        if(MonitorAlarmUtil.checkArray(ecmcAlarmObject.getAoType(), MonitorAlarmUtil.LDPOOL_MONITOR_ZB_AGGS)){
        	LdPoolMonitorDetail ldpoolDetail = getLdpoolMonitorValue(ecmcAlarmObject.getAoObjectId(), zbNodeId, currentTime);
        	double value = 0;
        	if(MonitorAlarmUtil.checkArray(zbNodeId,MonitorAlarmUtil.LDPOOL_MONITOR_MEMBER_AGGS)&&ldpoolDetail.getMember()>0){//不活跃成员指标
    			value = ldpoolDetail.getExpMemberRatio();
    		}else if(MonitorAlarmUtil.checkArray(zbNodeId,MonitorAlarmUtil.LDPOOL_MONITOR_MASTER_AGGS)		//不活跃主节点指标
    				&&ldpoolDetail.getMasterMember()>0&&ldpoolDetail.getMember()>0){
    			value = ldpoolDetail.getExpMasterRatio();
    		}else if(MonitorAlarmUtil.checkArray(zbNodeId,MonitorAlarmUtil.LDPOOL_MONITOR_SALVE_AGGS)		//不活跃从节点指标
    				&&ldpoolDetail.getSlaveMember()>0&&ldpoolDetail.getMember()>0){
    			value = ldpoolDetail.getExpSalveRatio();
    		}else{
    			return isOverTrigger;
    		}
	        if (">".equals(operator)) {
	        	isOverTrigger = value > threshold ? true : false;
	        } else if ("<".equals(operator)) {
	        	isOverTrigger = value < threshold ? true : false;
	        } else if ("=".equals(operator)) {
	        	isOverTrigger = value == threshold ? true : false;
	        }
	        return isOverTrigger;
        }
		double zbValue = getZBValue(ecmcAlarmObject.getAoObjectId(), ecmcAlarmObject.getAoType(), zbNodeId, currentTime);
        
        if (">".equals(operator)) {
        	isOverTrigger = zbValue > threshold ? true : false;
        } else if ("<".equals(operator)) {
        	isOverTrigger = zbValue < threshold ? true : false;
        } else if ("=".equals(operator)) {
        	isOverTrigger = zbValue == threshold ? true : false;
        }
		return isOverTrigger;
	}
	/**
	 * 查询云主机（云数据库）前一分钟的某一指标的记录数据
	 * @param objId
	 * @param zbEnName
	 * @param currentTime
	 * @return
	 */
	private double getZBValue(String objId,String objType, String zbNodeId, Date currentTime) {
        double zbValue = 0;
        Date oneMinAgo = new Date(currentTime.getTime() - 1 * 60 * 1000);
        String collectionName = MonitorAlarmUtil.getMongoByZb(zbNodeId);
        Sort sort = new Sort(Direction.DESC, "timestamp");
        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("vm_id").is(objId),
            Criteria.where("timestamp").gte(oneMinAgo), Criteria.where("timestamp").lte(currentTime));
        
        List<JSONObject> jsonList = mongoTemplate.find(new Query(criatira).with(sort),
            JSONObject.class, collectionName);
        if (jsonList.size() > 0) {
            zbValue = jsonList.get(0).getDouble("counter_volume");
        }
        return zbValue;
	}
	/**
     * 最新的负载均衡指标数据
     * @Author: duanbinbin
     * @param vmId
     * @param zbNodeId
     * @param currentTime
     * @return
     *<li>Date: 2017年3月3日</li>
     */
    private LdPoolMonitorDetail getLdpoolMonitorValue(String ldPoolId, String zbNodeId, Date currentTime) {
    	LdPoolMonitorDetail ldpoolDetail = new LdPoolMonitorDetail();
        Date oneMinAgo = new Date(currentTime.getTime() - 1 * 60 * 1000);
        Sort sort = new Sort(Direction.DESC, "timestamp");
        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("ldPoolId").is(ldPoolId),
            Criteria.where("timestamp").gte(oneMinAgo), Criteria.where("timestamp")
                .lte(currentTime));
        List<JSONObject> jsonList = mongoTemplate.find(new Query(criatira).with(sort),
            JSONObject.class, MongoCollectionName.MONITOR_LD_POOL_DETAIL);
        if (jsonList.size() > 0) {
        	JSONObject json = jsonList.get(0);
        	ldpoolDetail = JSONObject.toJavaObject(json, LdPoolMonitorDetail.class);
        }
        return ldpoolDetail;
    }
    /**
     * 监控报警项状态（mongo）
     * @param objectId
     * @param triggerId
     * @param currentTime
     * @param isTriggerMet
     * @return
     */
    private BasicDBObject createStatusForMongo(String alarmObjectId,String objType, String triggerId,
            Date currentTime, boolean isOverTrigger) {
    	BasicDBObject json = new BasicDBObject();
		json.put("alarmObjectId", alarmObjectId);
		json.put("objType", objType);
		json.put("triggerId", triggerId);
		json.put("time", currentTime);
		json.put("isOverTrigger", isOverTrigger?"1":"0");
		return json;
    }
    /**
     * 判断是否应该报警,即在持续时间内指标数据全都违反了阈值规定
     * @param alarmObjectId
     * @param objType
     * @param triggerId
     * @param durations
     * @param baselineStartTime
     * @param currentTime
     * @return
     */
    private boolean isNeedToAlarm(String alarmObjectId, String objType, String triggerId, int durations, Date baselineStartTime, Date currentTime,String statusId) {
    	for(int i = 0; i < 500;i++){
    		JSONObject jsonById = mongoTemplate.findById(statusId, JSONObject.class, MongoCollectionName.ECMC_MONITOR_ALARM_ITEM);
        	if(null != jsonById  && !jsonById.isEmpty()){
        		log.info("ECMC_NEW_ITEM_YES：最新监控报警项查询成功,statusId:"+statusId+",NO."+i);
        		break;
        	}
        	log.error("ECMC_NEW_ITEM_NO：最新监控报警项没有及时查询出,statusId:"+statusId+",NO."+i+",alarmObjectId:"+alarmObjectId+",triggerId:"+triggerId);
        	try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				log.error(e.toString());
			}
    	}
    	Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("alarmObjectId").is(alarmObjectId),Criteria.where("triggerId").is(triggerId), 
        		Criteria.where("objType").is(objType), Criteria.where("time").gt(baselineStartTime), Criteria.where("time").lte(currentTime));
        
        List<JSONObject> resultList = mongoTemplate.find(new Query(criatira), JSONObject.class,MongoCollectionName.ECMC_MONITOR_ALARM_ITEM);
        if(resultList.size()<(durations/60)){
            return false;
        }else{
            List<Integer> statusList = new ArrayList<Integer>();
            for (JSONObject json : resultList) {
                int itemStatus = json.getIntValue("isOverTrigger");
                statusList.add(itemStatus);
            }
            if(statusList.isEmpty()){
                return false;
            }else{
                int flag = 1;
                for (int i = 0; i < statusList.size(); i++) {
                    flag *= statusList.get(i);
                }
                return flag == 1 ? true : false;
            }
        }
    }
    /**
     * 产生运维报警信息
     * @param ecmcMonitorAlarmItem
     * @param monitorMngData	//指标信息
     * @param ecmcAlarmObject
     * @param ecmcAlarmTrigger
     * @return
     * @throws Exception
     */
    private EcmcAlarmMessage produceEcmcAlarmMessage(EcmcMonitorAlarmItem ecmcMonitorAlarmItem, MonitorMngData monitorMngData,
    		EcmcAlarmObject ecmcAlarmObject, EcmcAlarmTrigger ecmcAlarmTrigger) throws Exception {
		log.info("产生运维报警信息开始");
		EcmcAlarmMessage ecmcAlarmMessage = new EcmcAlarmMessage();
		ecmcAlarmMessage.setObjId(ecmcAlarmObject.getAoObjectId());
		
		ecmcAlarmMessage.setMonitorType(ecmcMonitorAlarmItem.getObjType());		//RDS1.0&网络1.3版本由原来的存储中文改为监控项nodeId
		String AlarmType = MonitorAlarmUtil.getAlarmTypeByZb(monitorMngData.getNodeId());
		ecmcAlarmMessage.setAlarmType(AlarmType);			//RDS1.0&网络1.3版本由原来的存储中文改为报警类型nodeId
		
		StringBuffer detail = new StringBuffer();
		detail.append(monitorMngData.getName())
		.append(ecmcAlarmTrigger.getOperator()).append(ecmcAlarmTrigger.getThreshold())
		.append(ecmcAlarmTrigger.getUnit())
		.append("已持续" + getLastTime(ecmcAlarmTrigger.getLastTime()));
		
		ecmcAlarmMessage.setDetail(detail.toString());
		ecmcAlarmMessage.setTime(new Date());
		ecmcAlarmMessage.setMonitorAlarmItemId(ecmcMonitorAlarmItem.getId());
		ecmcAlarmMessage.setAlarmRuleId(ecmcAlarmObject.getAlarmRuleId());
		ecmcAlarmMessage.setCusId(ecmcAlarmObject.getCusId());
		ecmcAlarmMessage.setPrjId(ecmcAlarmObject.getPrjId());
		ecmcAlarmMessage.setDcId(ecmcAlarmObject.getDcId());
		ecmcAlarmMessage.setObjName(ecmcAlarmObject.getObjName());
		
		//这个状态只有在报警信息前台展现时可以操作更改为已处理，其他地方操作不了
		ecmcAlarmMessage.setIsProcessed("0");
		ecmcMonitorAlarmService.addEcmcAlarmMessage(ecmcAlarmMessage);
		
		return ecmcAlarmMessage;
	}
    private String getLastTime(int lastTime) {
        if (lastTime >= 60 && lastTime < 3600) {
            return lastTime / 60 + "分钟";
        } else {
            return lastTime / 3600 + "小时";
        }
    }
    /**
     * 根据指标nodeId获取指标信息
     * @param zb
     * @return
     */
    private MonitorMngData getZbMngDataInfo(String zbNodeId) {
    	MonitorMngData monitorMngData = new MonitorMngData();
        try {
            String monitorItemStr = jedisUtil.get(RedisKey.SYS_DATA_TREE + zbNodeId);
            JSONObject monitorItemJSON = JSONObject.parseObject(monitorItemStr);
            monitorMngData.setNodeId(zbNodeId);
            monitorMngData.setName(monitorItemJSON.getString("nodeName"));
            monitorMngData.setNameEN(monitorItemJSON.getString("nodeNameEn"));
            monitorMngData.setParam1(monitorItemJSON.getString("para1"));//指标单位
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
        return monitorMngData;
    }
}
