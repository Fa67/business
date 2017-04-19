

package com.eayun.ecmcschedule.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.tools.DictUtil;
import com.eayun.customer.filter.SystemConfig;
import com.eayun.ecmcschedule.model.EcmcScheduleInfo;
import com.eayun.ecmcschedule.model.ScheduleLostJobMongoInfo;
import com.eayun.ecmcschedule.service.EcmcScheduleInfoService;
import com.eayun.ecmcuser.model.EcmcSysUser;
import com.eayun.ecmcuser.service.EcmcSysUserService;
import com.eayun.mail.service.MailService;
import com.eayun.sms.service.SMSService;
import com.eayun.sys.model.SysDataTree;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
public class ScheduleLostJobListenerTask {
	
	@Autowired
	private JedisUtil 				jedisUtil ;
	@Autowired
	private MongoTemplate 			mongoTemplate ;
	@Autowired
	private EcmcScheduleInfoService ecmcScheduleInfoService ;
	@Autowired
	private EcmcSysUserService 		ecmcSysUserService ;
	@Autowired
	private MailService 			mailService ;
	@Autowired
	private SMSService 				smsService ;
	
	//设置key的超时时间(默认单位是:秒)
	private static final Integer INCREMENT_ASYNC_TIME 	  = 59 ;
	//日期格式化模板
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS") ;
	//邮件和短信提醒内容(规则和内容格式还未明确)
	private static final String  SMS_AND_MAIL_SEND_TITLE  = "计划任务漏跑信息提醒" ;
	private static final String  SMS_AND_MAIL_SEND_TABLE_CONTENT  = "&nbsp;&nbsp;&nbsp;&nbsp;检测到当前系统中新出现如下漏跑的计划任务项，请及时查看处理！" ;
	private static final String  SMS_AND_MAIL_SEND_TR_CONTENT  = "<tr><th>计划任务漏跑项名称</th></tr>" ;
	//常量标识:第一次漏跑时间点
	private static final String  FIRST_TIME = "firstTime" ;
	//常量标识:最新一次漏跑时间点
	private static final String  END_TIME   = "endTime" ;
	//常量标识:任务名称
	private static final String  JOB_NAME = "jobName" ;
	//常量标识:获取所有管理员邮箱信息
	private static final Integer MAIL 		= 0 ;
	//常量标识:获取所有管理员手机号码信息
	private static final Integer SMS  		= 1 ;
	//数据字典中,被监测计划任务名称根节点的信息
	private static final String MONITOR_SCHEDULED_TASK_ROOT_KEY = "0014" ;
	//发送邮件和短信的约束,即每次只是提醒最新的计划任务信息,连续的不再提示
	private static List<String> STORE_SCHEDULE_LOST_TASK_INFORMATION_FOR_SMS_AND_MAIL = null ;
	//邮件发送模板位置
	private static final String SCHEDULE_ALERT_MAIL_HTML_MODEL_LOCATION = "/ecsc_sys_mail.html" ;
	//邮件发送模板加载配置项
	private static Map<String, String> urlMap = null;
	private static final Logger log = LoggerFactory.getLogger(ScheduleLostJobListenerTask.class) ;
	
	/**
	 * Spring配置计划任务,用来监测ECSC系统中所有在运行的计划任务执行情况(漏跑)
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Scheduled(cron="0 0/1 * * * ? ")
	public void scheduleLostTask() throws Exception {
		log.info("Spring监测计划任务开始执行");
		boolean isCheckAll = false ;
		List<String> allMonitorScheduledTaskName = new ArrayList<>() ;
		List<SysDataTree> allMonitorScheduledTask = DictUtil
				.getDataTreeByParentId(MONITOR_SCHEDULED_TASK_ROOT_KEY) ;
		if(allMonitorScheduledTask != null && allMonitorScheduledTask.size() != 0){
			for (SysDataTree sysDataTree : allMonitorScheduledTask){
				allMonitorScheduledTaskName.add(sysDataTree.getPara2().trim()) ;
			}
		}else{
			isCheckAll = true ;
		}
		Date nowDate = new Date() ;//检测时间
		Long nowDateTime = nowDate.getTime() ;//检测时间的毫秒数表示

		//increase方法为原子操作,控制集群环境同一时间只有一台机器可以执行此操作
		if (jedisUtil.increase (RedisKey.SCHEDULE_LOST_IS_CHECKED) == 1) {
			//设置超时时间
			jedisUtil.expireKey(RedisKey.SCHEDULE_LOST_IS_CHECKED, INCREMENT_ASYNC_TIME) ;
			//获取所有在运行的计划任务信息,此Service接口事先已经定义
			List<EcmcScheduleInfo> scheduleInfos = ecmcScheduleInfoService.getTaskList(null, null) ;

			//当前这一轮任务所检测到的所有计划任务漏跑信息
			List<String> currentLostTaskInformation = new ArrayList<>() ;

			for (EcmcScheduleInfo info : scheduleInfos) {
				//只监测我们在数据字典中配置好的计划任务监测项
				info.setJobName(info.getJobName().trim());
				if (isCheckAll || allMonitorScheduledTaskName.indexOf(info.getJobName()) != -1) {
					//计划任务名称
					String leakRunJobName = info.getJobName() ;
					//计划任务漏跑检测规则:当前时间大于等于下次执行时间加任务相隔时间差的和,则断定该计划任务出现了漏跑的情况
					//容错时间设置为一分钟,考虑到机器之间的时间差异等问题


					Date nextRunTime = info.getNextExcTime() ;
					if (nowDateTime >= (nextRunTime.getTime() + 60000)){
						log.info("计划任务：" + leakRunJobName + " 下次执行时间为：" + format.format(nextRunTime));
						log.info("当前时间: " + format.format(nowDate) + " ,检测到计划任务: '" + leakRunJobName + "' 有漏跑情况出现,请相关人员注意.【上次应该执行的时间："+format.format(info.getNextExcTime())+"】");


						currentLostTaskInformation.add(leakRunJobName) ;	//添加漏跑计划任务到事先指定的集合中
						//计划任务当前检测时刻总漏跑次数(计算规则为:当前时间减去下一次执行时间然后向下取整)
						int leakRunTime = ((int) Math.floor((nowDateTime - info.getNextExcTime().getTime()) / 
									getTaskCycleTime(info.getCronExpression(), nowDate)) + 1) ;

						//返回第一次和最新一次漏跑任务时间点信息的Map集合
						Map<String, Object> rs = willStoreToRedisLostContent(info, leakRunTime, nowDate) ;

						//对应于Mongodb中计划任务漏跑信息的存储实体类型
						ScheduleLostJobMongoInfo jobMongoInfo = new ScheduleLostJobMongoInfo(
								leakRunJobName,
								nowDate,
								info.getCronExpression(),
								getTaskCycleTime(info.getCronExpression(), nowDate),
								String.valueOf(rs.get(FIRST_TIME)),
								String.valueOf(rs.get(END_TIME)),
								leakRunTime) ;

						//先删除该时间段内上一个检测时间点查询的日志信息,从新保存新生成的

						mongoTemplate.remove(Query.query(new Criteria().andOperator(
									where(FIRST_TIME).is(String.valueOf(rs.get(FIRST_TIME))) ,
									where(JOB_NAME).is(leakRunJobName)
								)) ,
								ScheduleLostJobMongoInfo.class ,
								MongoCollectionName.SCHEDULE_LOST_JOB ) ;
						mongoTemplate.insert(jobMongoInfo, MongoCollectionName.SCHEDULE_LOST_JOB);

					}
				}
			}
			String storeScheduleLostFromRedis = jedisUtil.get(RedisKey.REDIS_KEY_STORE_SCHEDULE_LOST_INFORMATION) ;
			if(    storeScheduleLostFromRedis == null){
				STORE_SCHEDULE_LOST_TASK_INFORMATION_FOR_SMS_AND_MAIL = new ArrayList<>() ;
			}else{
				STORE_SCHEDULE_LOST_TASK_INFORMATION_FOR_SMS_AND_MAIL =
						JSONObject.parseObject(storeScheduleLostFromRedis, List.class) ;
			}
			//约定:如果有多条计划任务漏跑信息的话,则一次发送
			List<String> distinctContent = compareResult(STORE_SCHEDULE_LOST_TASK_INFORMATION_FOR_SMS_AND_MAIL,
					currentLostTaskInformation) ;
			if (distinctContent != null && distinctContent.size() != 0){
				log.info("OK,NOW IS NOTICE : " + distinctContent);
				//need notice.
				if (urlMap == null) {
					SystemConfig xml = new SystemConfig();
					urlMap = xml.findNodeMap();
				}
				//MAIL
				log.info("-----------------------------------Send Mail-----------------------------------");
				String strContent = initScheduleLostTaskAlertMailHtml() ;
				strContent = strContent.replace("{tablecontent}", SMS_AND_MAIL_SEND_TABLE_CONTENT);
				strContent = strContent.replace("{trcontent}", SMS_AND_MAIL_SEND_TR_CONTENT);
				strContent = strContent.replace("{userName}", "管理员");
				strContent = strContent.replace("{imgUrl}", urlMap.get("imgUrl"));
				strContent = strContent.replace("{ecscUrl}", urlMap.get("ecscUrl"));
				StringBuilder builder = new StringBuilder() ;
				for (String sName : distinctContent){
					builder.append("<tr><td>" + sName + "</td></tr>") ;
				}
				strContent = strContent.replace("{content}", builder.toString());
				mailService.send(SMS_AND_MAIL_SEND_TITLE, strContent, getSMSOrMailSenderInfosByAdminUser(MAIL));
				//SMS
//				log.info("-----------------------------------Send SMS-----------------------------------");
//				smsService.send(SMS_AND_MAIL_SEND_TITLE + " - 管理员您好，检测到当前有如下计划任务出现了漏跑的情况：" +
//						distinctContent.toString() + "，请及时查看处理！",  getSMSOrMailSenderInfosByAdminUser(SMS)) ;
			}
			jedisUtil.set(RedisKey.REDIS_KEY_STORE_SCHEDULE_LOST_INFORMATION, JSONObject.toJSONString(currentLostTaskInformation));
		}
		log.info("Spring监测计划任务执行完毕");
	}
	
	/**
	 * 根据传入的CRON表达式求出时间间隔
	 * @param cronExpression 时间表达式
	 * @return
	 */
	private Long getTaskCycleTime(String cronExpression, Date passDate){
		try {
			CronExpression cron = new CronExpression(cronExpression) ;
			Date from = null , end = null ;
			from = cron.getNextValidTimeAfter(passDate) ;
			end =  cron.getNextValidTimeAfter(from) ;
			return end.getTime() - from.getTime() ;
		} catch (ParseException e) {
			return 0L ;
		}
	}
	
	/**
	 * 获取第一次和最新一次计划任务漏跑的时刻
	 * @param scheduleInfo
	 * @param lostCount
	 * @return	Map集合,包含第一次和最新一次计划任务漏跑的时刻信息
	 * @throws Exception
	 */
	private Map<String, Object> willStoreToRedisLostContent(
			EcmcScheduleInfo scheduleInfo, Integer lostCount, Date passDate) throws Exception {
		Map<String, Object> result = new HashMap<>() ;
		//首次漏跑时间点
		result.put(FIRST_TIME, format.format(scheduleInfo.getNextExcTime())) ;
		//最新一次漏跑时间点
		if (lostCount > 1){
			result.put(END_TIME, format.format(new Date(
					scheduleInfo.getNextExcTime().getTime() + 
						(lostCount - 1) * getTaskCycleTime(scheduleInfo.getCronExpression(),
								passDate)
			))) ;
		}else{
			result.put(END_TIME, format.format(scheduleInfo.getNextExcTime())) ;
		}
		return result ;
	}
	
	/**
	 * 根据标识获取全部管理员的邮箱信息或者手机号码信息
	 * @param sendType
	 * @return
	 */
	private List<String> getSMSOrMailSenderInfosByAdminUser(Integer sendType){
		List<EcmcSysUser> allAdmins = ecmcSysUserService.findAllAdminUsers() ;
		List<String> rs = new ArrayList<>() ;
		if (sendType == MAIL){
			for (EcmcSysUser user : allAdmins){
				rs.add(user.getMail()) ;
			}
		}
		if (sendType == SMS){
			for (EcmcSysUser user : allAdmins){
				rs.add(user.getTel()) ;
			}
		}
		return rs ;
	}
	/**
	 * 比较上下两次漏跑计划任务列表信息的差异,这个差异则为需要进行管理员提醒的
	 * @param prev	上一次检测到的漏跑任务集合信息
	 * @param next	下一次检测到的漏跑任务集合信息
	 * @return		差异内容,类型为一个集合
	 */
	private List<String> compareResult(List<String> prev, List<String> next){
		if(prev == null) {prev = new ArrayList<String>() ;}
		if(next == null) {prev = new ArrayList<String>() ;}
		List<String> rs = new ArrayList<>() ;
		for (String eleString : next){
			if(prev.indexOf(eleString) == -1) {
				//新出现的漏跑计划任务
				rs.add(eleString) ;
			}
		}
		return rs ;
	}
	/**
	 * 获取发送邮件模板内容
	 * @return 模板内容字符串
	 * @throws Exception
	 */
	private String initScheduleLostTaskAlertMailHtml() throws Exception {
		StringBuffer scheduleLostTaskAlertMailHtml = null;
		InputStream emailInput = ScheduleLostJobListenerTask.class.
				getResourceAsStream(SCHEDULE_ALERT_MAIL_HTML_MODEL_LOCATION);
		BufferedReader br = null;
		try {
			scheduleLostTaskAlertMailHtml = new StringBuffer();
			br = new BufferedReader(new InputStreamReader(emailInput, "utf-8"));
			String line = "";
			String mail = "";
			while ((line = br.readLine()) != null) {
				mail += line;
			}
			scheduleLostTaskAlertMailHtml.append(mail);
		} finally {
			if (br != null) {
				br.close();
			}
		}
		return scheduleLostTaskAlertMailHtml.toString() ;
	}
	}

