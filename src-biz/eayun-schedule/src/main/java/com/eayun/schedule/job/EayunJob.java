package com.eayun.schedule.job;

import java.lang.reflect.Method;
import java.util.Date;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.ecmcschedule.model.BaseEcmcScheduleStatistics;
import com.eayun.ecmcschedule.model.EcmcScheduleLog;
import com.eayun.ecmcschedule.service.EcmcScheduleLogService;
import com.eayun.ecmcschedule.service.EcmcScheduleStatisticsService;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class EayunJob extends BaseQuartzJobBean {

	@SuppressWarnings("unused")
    private EcmcScheduleLogService ecmcScheduleLogService;

	private EcmcScheduleStatisticsService ecmcScheduleStatisticsService;
	
	private MongoTemplate mongoTemplate; 

	private ApplicationContext context;

	@Override
	protected void executeInternal(JobExecutionContext ctx) throws JobExecutionException {
		context = getApplicationContext(ctx);
		Exception exception = null;
		if (context != null) {
			mongoTemplate = (MongoTemplate)context.getBean("mongoTemplate");
			// 获取bean名称和方法名
			JobDetail jobDetail = ctx.getJobDetail();
			String beanName = jobDetail.getKey().getGroup(); // Bean和方法名
			String methodName = jobDetail.getDescription();

			boolean success = false;
			String errorMsg = null;
			Date startDate = new Date();
			long startTime = startDate.getTime();
			try {
				// 从spring中获取Bean实例
				Object object = context.getBean(beanName);
				if (object != null) {
					// 执行方法
					JobDataMap dataMap = jobDetail.getJobDataMap();
					Method[] methods = object.getClass().getDeclaredMethods();
					Method excMethod = null;
					for (Method method : methods) {
						if (method.getName().equals(methodName)) {
							excMethod = method;
							break;
						}
					}
					if (excMethod != null) {
						Class<?>[] paramsClass = excMethod.getParameterTypes();
						excMethod.setAccessible(true);
						if (paramsClass.length == 0) {
							excMethod.invoke(object, new Object[] {});
							success = true;
						} else if (paramsClass.length == 1 && paramsClass[0].equals(JobDataMap.class)
								&& dataMap != null) {
							excMethod.invoke(object, new Object[] { dataMap });
							success = true;
						} else {
							errorMsg = "The parameter mismatched!";
						}
					} else {
						errorMsg = "The Method [" + methodName + "] is not defined in [" + beanName + "]";
					}
				}
			} catch (Exception e) {
				errorMsg = e.getMessage();
				success = false;
				exception = e;
			}
			long endTime = new Date().getTime();
			EcmcScheduleLog ecmcScheduleLog = new EcmcScheduleLog();
			ecmcScheduleLog.setBeanName(beanName);
			ecmcScheduleLog.setMethodName(methodName);
			// 当手动触发时，该字段为空
			ecmcScheduleLog.setDescription(ctx.getTrigger().getDescription());
			ecmcScheduleLog.setTriggerName(jobDetail.getKey().getName());
			ecmcScheduleLog.setJobStartTime(startDate);
			ecmcScheduleLog.setTakeTime(endTime - startTime);
			// 执行类型，0：自动执行；1：手动执行
			ecmcScheduleLog.setExcType(ctx.getTrigger().getKey().getName().startsWith("MT_") ? "1" : "0");
//			ecmcScheduleLog.setSuccess(success);
			ecmcScheduleLog.setErrorMsg(errorMsg);
			// 添加任务执行日志
			if(mongoTemplate != null){
				mongoTemplate.insert(ecmcScheduleLog, MongoCollectionName.LOG_SCHEDULE);
			}

			// 添加任务统计数据
			ecmcScheduleStatisticsService = context.getBean(EcmcScheduleStatisticsService.class);
			BaseEcmcScheduleStatistics scheduleStatistics = ecmcScheduleStatisticsService
					.getTriggerNameAndDate(jobDetail.getKey().getName(), startDate);
			if (scheduleStatistics == null) {
				// 如果当天没有该任务的统计数据，则新建
				scheduleStatistics = new BaseEcmcScheduleStatistics(jobDetail.getKey().getName(), startDate);
			}
			scheduleStatistics.setTotalCount(scheduleStatistics.getTotalCount() + 1);
			scheduleStatistics.setSucCount(scheduleStatistics.getSucCount() + (success ? 1 : 0));
			scheduleStatistics.setFalCount(scheduleStatistics.getFalCount() + (success ? 0 : 1));
			ecmcScheduleStatisticsService.add(scheduleStatistics);
		}
		if (exception != null) {
			throw new JobExecutionException(exception);
		}
	}

}
