package com.eayun.schedule.service.impl;

import java.lang.reflect.Method;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobPersistenceException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.jdbcjobstore.Constants;
import org.springframework.stereotype.Service;

import com.eayun.common.util.DateUtil;
import com.eayun.ecmcschedule.model.EcmcScheduleInfo;
import com.eayun.schedule.ScheduleStartup;
import com.eayun.schedule.job.EayunJob;
import com.eayun.schedule.service.ScheduleService;

@Service
public class ScheduleServiceImpl implements ScheduleService {

//	@Autowired
	private Scheduler scheduler;

	@Override
	public void addTask(EcmcScheduleInfo scheduleInfo) throws Exception {
		try {
			JobDetail jobDetail = JobBuilder.newJob(EayunJob.class).withIdentity(scheduleInfo.getTriggerName(), scheduleInfo.getBeanName())
					.withDescription(scheduleInfo.getMethodName()).usingJobData(scheduleInfo.getDataMap()).build();
			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleInfo.getCronExpression());
			CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(scheduleInfo.getTriggerName())
					.withDescription(scheduleInfo.getTaskDesc()).withSchedule(scheduleBuilder).build();
			scheduler.scheduleJob(jobDetail, cronTrigger);
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public void pauseTask(String taskId) throws Exception {
		try {
			TriggerKey triggerKey = new TriggerKey(taskId);
			scheduler.pauseTrigger(triggerKey);
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public void resumeTask(String taskId) throws Exception {
		try {
			TriggerKey triggerKey = new TriggerKey(taskId);
			scheduler.resumeTrigger(triggerKey);
		} catch (Exception e) {
			throw e;
		}
	}

	

	@Override
	public void deleteTask(String taskId, String beanName) throws SchedulerException {
		try {
			JobKey jobKey = JobKey.jobKey(taskId, beanName);
			scheduler.pauseJob(jobKey);
			scheduler.deleteJob(jobKey);
		} catch (JobPersistenceException e) {
			throw e;
		}
	}

	@Override
	public void modifyTask(EcmcScheduleInfo scheduleInfo, String oldBeanName) throws Exception {
		try {
			// 先删除Job
			deleteTask(scheduleInfo.getTaskId(), oldBeanName);
			// 再创建Job
			addTask(scheduleInfo);
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public EcmcScheduleInfo getTask(String taskId, String beanName) throws Exception {
		try {
			JobKey jobKey = JobKey.jobKey(taskId, beanName);
			TriggerKey triggerKey = TriggerKey.triggerKey(taskId);
			JobDetail jobDetail = scheduler.getJobDetail(jobKey);
			CronTrigger trigger = (CronTrigger)scheduler.getTrigger(triggerKey);
			
			
			EcmcScheduleInfo scheduleInfo = new EcmcScheduleInfo();
			scheduleInfo.setTaskId(taskId);
			scheduleInfo.setBeanName(beanName);
			scheduleInfo.setMethodName(jobDetail.getDescription());
			scheduleInfo.setTaskState(formatTaskState(scheduler.getTriggerState(triggerKey)));
			scheduleInfo.setPreExcTime(trigger.getPreviousFireTime());
			scheduleInfo.setNextExcTime(trigger.getNextFireTime());
			scheduleInfo.setCronExpression(trigger.getCronExpression());
			scheduleInfo.setDataMap(jobDetail.getJobDataMap());
			scheduleInfo.setTaskDesc(trigger.getDescription());
			scheduleInfo.setTriggerName(taskId);
			
			return scheduleInfo;
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public void triggerTask(String taskId, String beanName) throws Exception {
		try {
			JobKey jobKey = JobKey.jobKey(taskId, beanName);
			scheduler.triggerJob(jobKey);
		} catch (JobPersistenceException e) {
			throw e;
		}
	}
	
	@Override
	public boolean checkBeanAndMethod(String beanName, String methodName) {
		if(beanName == null){
			return false;
		}
		if(methodName == null){	//只验证Bean
			try {
				return ScheduleStartup.context.containsBean(beanName);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}else{	//验证Bean和方法
			try {
				if(ScheduleStartup.context.containsBean(beanName)){
					Object object = ScheduleStartup.context.getBean(beanName);
//					Method method = object.getClass().getDeclaredMethod(methodName, new Class[] {});
//					Method methodWithParams = object.getClass().getDeclaredMethod(methodName, new Class[] {JobDataMap.class});
					Method[] methods = object.getClass().getDeclaredMethods();
					for(Method method : methods){
						if(method.getName().equals(methodName)){
							return true;
						}
					}
//					object.getClass().getDeclaredMethod(methodName, new Class[] {JobDataMap.class}).setAccessible(true);
//					return true;
				}
				return false;
			} catch (Exception e) {
//				e.printStackTrace();
				return false;
			}
		}
	}

	protected TriggerState getTaskState(String state) {
		// NONE：Trigger已经完成，且不会在执行，或者找不到该触发器，或者Trigger已经被删除
		// NORMAL：正常状态
		// PAUSED：暂停状态
		// COMPLETE：触发器完成，但是任务可能还正在执行中
		// BLOCKED：线程阻塞状态
		// ERROR：出现错误
		if (state.equals(Constants.STATE_DELETED)) {
			return TriggerState.NONE;
			// return "删除";
		}
		if (state.equals(Constants.STATE_COMPLETE)) {
			return TriggerState.COMPLETE;
			// return "完成";
		}
		if (state.equals(Constants.STATE_PAUSED)) {
			return TriggerState.PAUSED;
			// return "暂停";
		}
		if (state.equals(Constants.STATE_PAUSED_BLOCKED)) {
			return TriggerState.PAUSED;
			// return "暂停";
		}
		if (state.equals(Constants.STATE_ERROR)) {
			return TriggerState.ERROR;
			// return "错误";
		}
		if (state.equals(Constants.STATE_BLOCKED)) {
			return TriggerState.BLOCKED;
			// return "阻塞";
		}
		return TriggerState.NORMAL;
		// return "正常";
	}

	protected String formatTaskState(TriggerState triggerState) {
		if (triggerState == TriggerState.NONE) {
			return "删除";
		} else if (triggerState == TriggerState.COMPLETE) {
			return "完成";
		} else if (triggerState == TriggerState.PAUSED) {
			return "暂停";
		} else if (triggerState == TriggerState.ERROR) {
			return "错误";
		} else if (triggerState == TriggerState.BLOCKED) {
			return "阻塞";
		} else {
			return "正常";
		}
	}

	@SuppressWarnings("unused")
    private String escapeSpecialChar(String str) {
		if (StringUtils.isNotBlank(str)) {
			String[] specialChars = { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|", "%" };
			for (String key : specialChars) {
				if (str.contains(key)) {
					str = str.replace(key, "/" + key);
				}
			}
		}
		return str;
	}
	
	@SuppressWarnings("unused")
    private Date stringToDate(String dateStr){
		if(StringUtils.equals(dateStr, "-1")){
			return null;
		}
		return DateUtil.timestampToDate(dateStr);
	}

}
