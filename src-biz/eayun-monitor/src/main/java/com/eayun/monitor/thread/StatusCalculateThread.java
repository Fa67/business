package com.eayun.monitor.thread;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.mail.service.MailService;
import com.eayun.monitor.bean.LdPoolMonitorDetail;
import com.eayun.monitor.bean.MonitorAlarmUtil;
import com.eayun.monitor.bean.MonitorZB;
import com.eayun.monitor.model.AlarmContact;
import com.eayun.monitor.model.AlarmMessage;
import com.eayun.monitor.model.AlarmObject;
import com.eayun.monitor.model.AlarmTrigger;
import com.eayun.monitor.model.Contact;
import com.eayun.monitor.model.MonitorAlarmItem;
import com.eayun.monitor.service.AlarmService;
import com.eayun.monitor.service.MonitorAlarmService;
import com.eayun.sms.service.SMSService;
import com.mongodb.BasicDBObject;

public class StatusCalculateThread implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(StatusCalculateThread.class);

    private MonitorAlarmItem    monitorAlarmItem;
    private MonitorAlarmService monitorAlarmService;
    private AlarmService        alarmService;
    private MongoTemplate       mongoTemplate;
    private JedisUtil           jedisUtil;
    private Date                currentTime;
    private SMSService          smsService;
    private MailService         mailService;

    public StatusCalculateThread(MonitorAlarmItem monitorAlarmItem,
                                 MonitorAlarmService monitorAlarmService,
                                 AlarmService alarmService, MongoTemplate mongoTemplate, JedisUtil jedisUtil,
                                 Date currentTime, SMSService smsService, MailService mailService) {
        this.monitorAlarmItem = monitorAlarmItem;
        this.monitorAlarmService = monitorAlarmService;
        this.alarmService = alarmService;
        this.mongoTemplate = mongoTemplate;
        this.jedisUtil = jedisUtil;
        this.currentTime = currentTime;
        this.smsService = smsService;
        this.mailService = mailService;
    }

    @Override
    public void run() {
        log.info("状态计算线程启动");

        String objectId = monitorAlarmItem.getObjectId();
        String triggerId = monitorAlarmItem.getTriggerId();
        String monitorType = monitorAlarmItem.getMonitorType();	//监控项类型

        //状态计算线程的对象应当在缓存中取得，因为多线程，读库有压力
        AlarmObject alarmObj = alarmService.getAlarmObject(objectId);
        if(null == alarmObj || null == alarmObj.getId() || alarmObj.getIsDeleted()){
        	log.error("没有查询到报警对象的信息，此次线程结束,monitorAlarmItem:"+JSONObject.toJSONString(monitorAlarmItem));
        	return;
        }
        AlarmTrigger alarmTrigger = alarmService.getAlarmTrigger(triggerId);
        if(null == alarmTrigger || null == alarmTrigger.getId()){
        	log.error("没有查询到触发条件的信息，此次线程结束,monitorAlarmItem:"+JSONObject.toJSONString(monitorAlarmItem));
        	return;
        }

        //这里的指标是nodeId，需要进行转义
        String zb = alarmTrigger.getZb();
        MonitorZB monitorZB = getZbInfo(zb);
        String zbNodeId = monitorZB.getNodeId();

        //判断最新一条指标数据是否满足了触发条件
        boolean isTriggerMet = checkTriggerMet(alarmObj, alarmTrigger, zbNodeId, currentTime);
        //将最新一条是否满足情况计入报警监控项mongo集合
        BasicDBObject jsonStatus = createStatusForMongo(objectId, triggerId,monitorType, currentTime, isTriggerMet);
        
        mongoTemplate.insert(jsonStatus, MongoCollectionName.MONITOR_ALARM_ITEM);
        String statusId = jsonStatus.getString("_id");
        
        int durations = alarmTrigger.getLastTime();//持续时间，单位：s
        Date baselineStartTime = new Date(currentTime.getTime() - durations * 1000);
        Date itemModifiedTime = monitorAlarmItem.getModifiedTime();
        if (baselineStartTime.getTime() < itemModifiedTime.getTime()) {
            return;
        } else {
            boolean needToAlarm = isNeedToAlarm(objectId, triggerId, durations, baselineStartTime,currentTime,statusId);
            if (needToAlarm) {
                /* XXX
                 * 如果上一个线程要update监控报警项的状态，但是数据库还没写入完毕
                 * 而下一个线程开始访问monitorAlarmItem的isNotified状态，由于数据还没变更过来
                 * 就会产生多余的一条报警信息——该如何处理多线程读写冲突？
                 * ——这个情况只要保证1min之内计算完就不会出现，出现这个情况是DEBUG状态，执行慢。
                 */
                //获得报警规则ID
                String alarmRuleId = alarmObj.getAlarmRuleId();
                //根据报警规则ID查询报警联系人
                List<AlarmContact> contactList = alarmService.getAlarmContactListByRuleId(alarmRuleId);
                //查询项目的配额及客户id信息
                Map<String,String> customerInfo = alarmService.getCusInfoByPrjId(alarmObj.getPrjId());
                if (monitorAlarmItem.getIsNotified().equals("0")) {
                    //如果当前未通知报警联系人，则产生报警信息并通知报警联系人
                    try {
                        AlarmMessage alarmMsg = produceAlarmMessage(monitorAlarmItem, monitorZB, alarmObj, alarmTrigger,monitorType);
                        notifyAlarmContacts(contactList, monitorZB, alarmObj, alarmTrigger, alarmMsg, customerInfo);
                    } catch (Exception e) {
                        log.error("",e);
                        throw new AppException("发送报警信息异常");
                    }
                    monitorAlarmItem.setIsNotified("1");
                    monitorAlarmItem.setModifiedTime(currentTime);
                }else{
                    //如果当前需要报警且已通知报警联系人：用监控报警项上次修改时间做判断即可
                    long intervals = currentTime.getTime()-itemModifiedTime.getTime();
                    // 连续24h持续触发重新报警
                    if(intervals%(1000*60*60*24)==0){
                        try {
                            AlarmMessage alarmMsg = alarmService.getLatestAlarmMessageByMonitorAlarmItemId(monitorAlarmItem.getId());
                            notifyAlarmContacts(contactList, monitorZB, alarmObj, alarmTrigger, alarmMsg, customerInfo);
                        } catch (Exception e) {
                            log.error("",e);
                            throw new AppException("发送报警信息异常");
                        }
                    }
                }
            } else {
                //如果不需要报警，且已产生报警，则将是否产生报警状态置为“未报警”，下次再遇到需要报警的情况，还会产生报警信息。
                if (monitorAlarmItem.getIsNotified().equals("1")) {
                    monitorAlarmItem.setIsNotified("0");
                }
            }
            monitorAlarmService.updateMonitorAlarmItem(monitorAlarmItem);
        }
    }
    /**
     * 添加一条监控报警项mongo
     * @Author: duanbinbin
     * @param objectId
     * @param triggerId
     * @param monitorType
     * @param currentTime
     * @param isTriggerMet
     * @return
     *<li>Date: 2017年3月3日</li>
     */
    private BasicDBObject createStatusForMongo(String objectId, String triggerId,
    		String monitorType,Date currentTime, boolean isTriggerMet) {
    	BasicDBObject json = new BasicDBObject();
        json.put("objectId", objectId);
        json.put("monitorType", monitorType);
        json.put("triggerId", triggerId);
        json.put("time", currentTime);
        json.put("isTriggerMet", isTriggerMet?"1":"0");
        return json;
    }

    /**
     * 产生一条报警信息，保存到数据库中
     * @param monitorAlarmItem
     * @param monitorZB
     * @param alarmObj
     * @param alarmTrigger
     * @return
     * @throws Exception
     */
    private AlarmMessage produceAlarmMessage(MonitorAlarmItem monitorAlarmItem, MonitorZB monitorZB,
    		AlarmObject alarmObj, AlarmTrigger alarmTrigger , String monitorType) throws Exception {
        log.info("产生报警信息开始");
        AlarmMessage alarmMsg = new AlarmMessage();
        alarmMsg.setVmId(alarmObj.getVmId());
        alarmMsg.setMonitorType(monitorType);
        alarmMsg.setAlarmType(MonitorAlarmUtil.getAlarmTypeByZb(monitorZB.getNodeId()));
        
        StringBuffer detail = new StringBuffer();
        detail.append(monitorZB.getName())
        .append(alarmTrigger.getOperator()).append(alarmTrigger.getThreshold())
        .append(alarmTrigger.getUnit())
        .append("已持续" + getLastTime(alarmTrigger.getLastTime()));
        alarmMsg.setDetail(detail.toString());
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStr = sdf.format(new Date());
        Date time = null;
        try {
            time = sdf.parse(timeStr);
        } catch (ParseException e) {
            throw new AppException(e.getMessage());
        }
        alarmMsg.setTime(time);
        alarmMsg.setAlarmTime(timeStr);
        
        alarmMsg.setMonitorAlarmItemId(monitorAlarmItem.getId());
        alarmMsg.setAlarmRuleId(alarmObj.getAlarmRuleId());
        
        //这个状态只有在报警信息前台展现时可以操作更改为已处理，其他地方操作不了
        alarmMsg.setIsProcessed("0");
        //将报警信息保存入库
        monitorAlarmService.addAlarmMessage(alarmMsg);
        return alarmMsg;
    }
    /**
     * 组织邮件、短信模板并发送
     * @Author: duanbinbin
     * @param contactList
     * @param monitorZB
     * @param alarmObj
     * @param alarmTrigger
     * @param alarmMsg
     * @param customerInfo
     * @return
     * @throws Exception
     *<li>Date: 2017年3月3日</li>
     */
    private boolean notifyAlarmContacts(List<AlarmContact> contactList, MonitorZB monitorZB,AlarmObject alarmObj, 
    		AlarmTrigger alarmTrigger, AlarmMessage alarmMsg, Map<String, String> customerInfo) throws Exception {
        log.info("发送报警信息至报警联系人");
        List<String> mails = new ArrayList<String>();
        List<String> mobiles = new ArrayList<String>();
        for (AlarmContact alarmContact : contactList) {
            String contactMethod = alarmContact.getContactMethod();
            Contact contact = alarmService.getContactById(alarmContact.getContactId());
            //TODO 如果增加短信包，则需要此时对contact进行某些判断，判断是否应该发送
            if (contactMethod.equals("短信 邮件")) {
                mails.add(contact.getEmail());
                mobiles.add(contact.getPhone());
            } else if (contactMethod.equals("短信")) {
                mobiles.add(contact.getPhone());
            } else if (contactMethod.equals("邮件")) {
                mails.add(contact.getEmail());
            }
        }
        
        String title = "易云报警提醒";
        String monitorName = MonitorAlarmUtil.getMonitorNameByType(alarmObj.getMonitorType());
        StringBuffer smsContent = new StringBuffer();
        smsContent.append("尊敬的客户：您在易云租用的"+monitorName)
        	.append(alarmObj.getVmName())
            .append("发生报警，")
            .append(monitorZB.getName())
            .append(alarmTrigger.getOperator())
            .append(alarmTrigger.getThreshold())
            .append(alarmTrigger.getUnit())
            .append("持续" + getLastTime(alarmTrigger.getLastTime()))
            .append("，请登录管理控制台处理。");
        
        String mailContent = getMailContent(monitorZB, alarmObj, alarmTrigger, alarmMsg);
        
        if(mails.size()>0){
            mailService.send(title, mailContent, mails);
        }

        String customerID = customerInfo.get("customerId");
        String projectID = customerInfo.get("projectId");
        String sentStr = jedisUtil.get(RedisKey.SMS_QUOTA_SENT +customerID+":"+projectID);
        String totalStr = jedisUtil.get(RedisKey.SMS_QUOTA_TOTAL +customerID+":"+projectID);
        int sentCount = 0;
        int totalCount = 0;
        if(!(null == sentStr || "".equals(sentStr))){
            sentCount = Integer.valueOf(sentStr);
        }
        if(!(null == totalStr || "".equals(totalStr))){
            totalCount = Integer.valueOf(totalStr);
        }
        if(sentCount< totalCount && mobiles.size()>0){
            smsService.send(smsContent.toString(), mobiles, customerID, projectID, ConstantClazz.SMS_BIZ_MONITOR);
        }else{
            smsService.save(smsContent.toString(), mobiles, "100", customerID, projectID, ConstantClazz.SMS_BIZ_MONITOR);
        }
        return true;
        
    }
    /**
     * 组织报警邮件模板
     * @Author: duanbinbin
     * @param monitorZB
     * @param alarmObj
     * @param alarmTrigger
     * @param alarmMsg
     * @return
     *<li>Date: 2017年3月3日</li>
     */
    private String getMailContent(MonitorZB monitorZB, AlarmObject alarmObj, AlarmTrigger alarmTrigger, AlarmMessage alarmMsg) {
        AlarmMailTemplate template = AlarmMailTemplate.getInstance();
        String dcName = alarmObj.getDcName();
        String strContent=String.valueOf(template.getMailHtml());
        /*
         * 这里的账户是用户还是其他？该如何去取得——是联系人的名字！而不是客户。
         * 由于是先组织contactList统一发送，所以没办法区分具体contact名称，所以跟路兴商量后，邮件中不体现具体联系人名称。
         */
        String monitorName = MonitorAlarmUtil.getMonitorNameByType(alarmObj.getMonitorType());
        strContent=strContent.replace("{monitorName}", monitorName);
        strContent=strContent.replace("{dcName}", dcName);
        strContent=strContent.replace("{vmName}", alarmObj.getVmName());
        strContent=strContent.replace("{detail}", alarmMsg.getDetail());
        strContent=strContent.replace("{alarmTime}", alarmMsg.getAlarmTime());
        return strContent;
    }

    private String getLastTime(int lastTime) {
        if (lastTime >= 60 && lastTime < 3600) {
            return lastTime / 60 + "分钟";
        } else {
            return lastTime / 3600 + "小时";
        }
    }
    /**
     * 判断当前是否需要报警。即是否已经持续满足了触发条件
     * @Author: duanbinbin
     * @param objectId
     * @param triggerId
     * @param durations
     * @param baselineStartTime
     * @param currentTime
     * @param statusId
     * @return
     *<li>Date: 2017年3月3日</li>
     */
    private boolean isNeedToAlarm(String objectId, String triggerId, int durations, Date baselineStartTime, Date currentTime,String statusId) {
    	
    	for(int i = 0; i < 500;i++){
    		JSONObject jsonById = mongoTemplate.findById(statusId, JSONObject.class, MongoCollectionName.MONITOR_ALARM_ITEM);
        	if(null != jsonById  && !jsonById.isEmpty()){
        		log.info("ECSC_NEW_ITEM_YES：最新监控报警项查询成功,statusId:"+statusId+",NO."+i);
        		break;
        	}
        	log.error("ECSC_NEW_ITEM_NO：最新监控报警项没有及时查询出,statusId:"+statusId+",NO."+i+",objectId:"+objectId+",triggerId:"+triggerId);
        	try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				log.error(e.toString());
			}
    	}
    	
        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("objectId").is(objectId),Criteria.where("triggerId").is(triggerId), 
            Criteria.where("time").gt(baselineStartTime), Criteria.where("time").lte(currentTime));
        List<JSONObject> resultList = mongoTemplate.find(new Query(criatira), JSONObject.class,MongoCollectionName.MONITOR_ALARM_ITEM);
        if(resultList.size()<(durations/60)){
            return false;
        }else{
            List<Integer> statusList = new ArrayList<Integer>();
            for (JSONObject json : resultList) {
                int itemStatus = json.getIntValue("isTriggerMet");
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
     * 确认最新一条指标数据是否满足了触发条件
     * @Author: duanbinbin
     * @param alarmObj
     * @param alarmTrigger
     * @param monitorZB
     * @param currentTime
     * @return
     *<li>Date: 2017年3月3日</li>
     */
    private boolean checkTriggerMet(AlarmObject alarmObj, AlarmTrigger alarmTrigger,
    		String zbNodeId, Date currentTime) {
        boolean isTriggerMet = false;
        float threshold = alarmTrigger.getThreshold();
        String operator = alarmTrigger.getOperator();
        //判断是否为负载均衡监控项
        if(MonitorAlarmUtil.checkArray(alarmObj.getMonitorType(), MonitorAlarmUtil.LDPOOL_MONITOR_ZB_AGGS)){
        	LdPoolMonitorDetail ldpoolDetail = getLdpoolMonitorValue(alarmObj.getVmId(), zbNodeId, currentTime);
        	double value = 0;
        	if(MonitorAlarmUtil.checkArray(zbNodeId,MonitorAlarmUtil.LDPOOL_MONITOR_MEMBER_AGGS)&&ldpoolDetail.getMember()>0){
    			value = ldpoolDetail.getExpMemberRatio();
    		}else if(MonitorAlarmUtil.checkArray(zbNodeId,MonitorAlarmUtil.LDPOOL_MONITOR_MASTER_AGGS)
    				&&ldpoolDetail.getMasterMember()>0&&ldpoolDetail.getMember()>0){
    			value = ldpoolDetail.getExpMasterRatio();
    		}else if(MonitorAlarmUtil.checkArray(zbNodeId,MonitorAlarmUtil.LDPOOL_MONITOR_SALVE_AGGS)
    				&&ldpoolDetail.getSlaveMember()>0&&ldpoolDetail.getMember()>0){
    			value = ldpoolDetail.getExpSalveRatio();
    		}else{
    			return isTriggerMet;
    		}
	        if (">".equals(operator)) {
	            isTriggerMet = value > threshold ? true : false;
	        } else if ("<".equals(operator)) {
	            isTriggerMet = value < threshold ? true : false;
	        } else if ("=".equals(operator)) {
	            isTriggerMet = value == threshold ? true : false;
	        }
	        return isTriggerMet;
        }
        double zbValue = getZBValue(alarmObj.getVmId(), zbNodeId, currentTime);
        if (">".equals(operator)) {
            isTriggerMet = zbValue > threshold ? true : false;
        } else if ("<".equals(operator)) {
            isTriggerMet = zbValue < threshold ? true : false;
        } else if ("=".equals(operator)) {
            isTriggerMet = zbValue == threshold ? true : false;
        }
        return isTriggerMet;
    }
    private MonitorZB getZbInfo(String zb) {
        MonitorZB monitorZB = new MonitorZB();
        try {
            String monitorItemStr = jedisUtil.get(RedisKey.SYS_DATA_TREE + zb);
            JSONObject monitorItemJSON = JSONObject.parseObject(monitorItemStr);
            monitorZB.setNodeId(zb);
            monitorZB.setName(monitorItemJSON.getString("nodeName"));
            monitorZB.setNameEN(monitorItemJSON.getString("nodeNameEn"));
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
        return monitorZB;
    }
    /**
     * 云主机（数据库）获取最新的一条指标数据
     * @Author: duanbinbin
     * @param vmId
     * @param zbNodeId
     * @param currentTime
     * @return
     *<li>Date: 2017年3月3日</li>
     */
    private double getZBValue(String vmId, String zbNodeId, Date currentTime) {
        //在中间结果集中查询当前报警对象，当前时间前一分钟之内的的指定指标值
        double zbValue = 0;
        Date oneMinAgo = new Date(currentTime.getTime() - 1 * 60 * 1000);
        String collectionName = MonitorAlarmUtil.getMongoByZb(zbNodeId);
        Sort sort = new Sort(Direction.DESC, "timestamp");
        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("vm_id").is(vmId),	//只有云主机和云数据库的集合数据在这里查询，因此key仍为vm_id
            Criteria.where("timestamp").gte(oneMinAgo), Criteria.where("timestamp")
                .lte(currentTime));
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
}
