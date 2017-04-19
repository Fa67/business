package com.eayun.ecmcschedule.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections.MapUtils;
import org.quartz.CronExpression;
import org.quartz.JobDataMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eayun.common.ConstantClazz;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.BeanUtils;
import com.eayun.ecmcschedule.model.EcmcScheduleInfo;
import com.eayun.ecmcschedule.service.EcmcScheduleInfoService;
import com.eayun.schedule.service.ScheduleService;

@Controller
@RequestMapping("/ecmc/system/schedule")
@Scope("prototype")
public class EcmcScheduleInfoController {

//	@Autowired
	private ScheduleService scheduleService;
	@Autowired
	private EcmcScheduleInfoService ecmcScheduleInfoService;

	/**
	 * 添加任务
	 * 
	 * @param requstMap
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/addtask")
	@ResponseBody
	public Object addTask(@RequestBody Map<String, Object> requstMap) throws Exception {
		try {
			EayunResponseJson reJson = new EayunResponseJson();
			JobDataMap jobDataMap = new JobDataMap();
			List<Map<String, String>> dataList = (List<Map<String, String>>) MapUtils.getObject(requstMap, "params");
			if (dataList != null && dataList.size() > 0) {
				for (Map<String, String> map : dataList) {
					if(map.get("key")!=null){
						jobDataMap.put(map.get("key"), map.get("value"));
					}
				}
			}
			requstMap.put("dataMap", jobDataMap);
			
			EcmcScheduleInfo scheduleInfo = new EcmcScheduleInfo();
			BeanUtils.mapToBean(scheduleInfo, requstMap);
			scheduleInfo.setTriggerName(getUUID());
			ecmcScheduleInfoService.add(scheduleInfo);
			
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			return reJson;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 删除任务
	 * 
	 * @param requstMap
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/deletetask")
	@ResponseBody
	public Object deleteTask(@RequestBody Map<String, Object> requstMap) throws Exception {
		try {
			EayunResponseJson reJson = new EayunResponseJson();
			String taskId = MapUtils.getString(requstMap, "taskId");
			String beanName = MapUtils.getString(requstMap, "beanName");
			ecmcScheduleInfoService.deleteTask(taskId, beanName);
			scheduleService.deleteTask(taskId, beanName);
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			return reJson;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 获取任务信息
	 * 
	 * @param requstMap
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/gettask")
	@ResponseBody
	public Object getTask(@RequestBody Map<String, Object> requstMap) throws Exception {
		try {
			EayunResponseJson reJson = new EayunResponseJson();
			String taskId = MapUtils.getString(requstMap, "taskId");
			String beanName = MapUtils.getString(requstMap, "beanName");
			EcmcScheduleInfo scheduleInfo = ecmcScheduleInfoService.getTask(taskId, beanName);
			reJson.setData(scheduleInfo);
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			return reJson;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 获取任务列表
	 * 
	 * @param paramsMap
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/gettasklist")
	@ResponseBody
	public Object getTaskList(@RequestBody Map<String, Object> requstMap) throws Exception {
		try {
			String queryName = MapUtils.getString(requstMap, "queryName");
			String state = MapUtils.getString(requstMap, "state");
			return ecmcScheduleInfoService.getTaskList(queryName, state);
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 修改任务
	 * 
	 * @param requstMap
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
    @RequestMapping(value = "/modifytask")
	@ResponseBody
	public Object modifyTask(@RequestBody Map<String, Object> requstMap) throws Exception {
		try {
			EayunResponseJson reJson = new EayunResponseJson();
			
			JobDataMap jobDataMap = new JobDataMap();
			List<Map<String, String>> dataList = (List<Map<String, String>>) MapUtils.getObject(requstMap, "params");
			if (dataList != null && dataList.size() > 0) {
				for (Map<String, String> map : dataList) {
					if(map.get("key")!=null){
						jobDataMap.put(map.get("key"), map.get("value"));
					}
				}
			}
			requstMap.put("dataMap", jobDataMap);
			
			EcmcScheduleInfo scheduleInfo = new EcmcScheduleInfo();
			BeanUtils.mapToBean(scheduleInfo, requstMap);
			scheduleInfo.setTriggerName(getUUID());
			scheduleInfo.setTriggerName(scheduleInfo.getTaskId());
			String oldBeanName = MapUtils.getString(requstMap, "oldBeanName");
			ecmcScheduleInfoService.update(scheduleInfo, oldBeanName);
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			return reJson;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 恢复任务
	 * 
	 * @param requstMap
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/resumetask")
	@ResponseBody
	public Object resumeTask(@RequestBody Map<String, Object> requstMap) throws Exception {
		try {
			EayunResponseJson reJson = new EayunResponseJson();
			String taskId = MapUtils.getString(requstMap, "taskId");
			scheduleService.resumeTask(taskId);
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			return reJson;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 暂停任务
	 * 
	 * @param requstMap
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/pausetask")
	@ResponseBody
	public Object pauseTask(@RequestBody Map<String, Object> requstMap) throws Exception {
		try {
			EayunResponseJson reJson = new EayunResponseJson();
			String taskId = MapUtils.getString(requstMap, "taskId");
			scheduleService.pauseTask(taskId);
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			return reJson;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 触发任务
	 * 
	 * @param requstMap
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/triggertask")
	@ResponseBody
	public Object triggerTask(@RequestBody Map<String, Object> requstMap) throws Exception {
		try {
			EayunResponseJson reJson = new EayunResponseJson();
			String taskId = MapUtils.getString(requstMap, "taskId");
			String beanName = MapUtils.getString(requstMap, "beanName");
			scheduleService.triggerTask(taskId, beanName);
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			return reJson;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * 获取全部任务ID
	 * 
	 * @param requstMap
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/getalltaskid")
	@ResponseBody
	public Object getAllTaskId(@RequestBody Map<String, Object> requstMap) throws Exception {
		try {
			EayunResponseJson reJson = new EayunResponseJson();
			List<String> ids = ecmcScheduleInfoService.getAllTaskId();
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			reJson.setData(ids);
			return reJson;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 验证触发时间
	 * 
	 * @param requstMap
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/checkcronexpression")
	@ResponseBody
	public Object checkCronExpression(@RequestBody Map<String, Object> requstMap) throws Exception {
		try {
			EayunResponseJson reJson = new EayunResponseJson();
			String cronExpression = MapUtils.getString(requstMap, "cronExpression");
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			reJson.setData(CronExpression.isValidExpression(cronExpression));
			return reJson;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 验证Bean是否存在
	 * 
	 * @param requstMap
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/checkbeanname")
	@ResponseBody
	public Object checkBeanName(@RequestBody Map<String, Object> requstMap) throws Exception {
		try {
			EayunResponseJson reJson = new EayunResponseJson();
			String beanName = MapUtils.getString(requstMap, "beanName");
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			reJson.setData(scheduleService.checkBeanAndMethod(beanName, null));
			return reJson;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 验证Bean中的方法名
	 * 
	 * @param requstMap
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/checkmethodname")
	@ResponseBody
	public Object checkMethodName(@RequestBody Map<String, Object> requstMap) throws Exception {
		try {
			EayunResponseJson reJson = new EayunResponseJson();
			String beanName = MapUtils.getString(requstMap, "beanName");
			String methodName = MapUtils.getString(requstMap, "methodName");
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			reJson.setData(scheduleService.checkBeanAndMethod(beanName, methodName));
			return reJson;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * 获取任务名称
	 * 
	 * @param requstMap
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/gettaskname")
	@ResponseBody
	public Object getTaskName(@RequestBody Map<String, Object> requstMap) throws Exception {
		try {
			EayunResponseJson reJson = new EayunResponseJson();
			String taskId = MapUtils.getString(requstMap, "taskId");
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			reJson.setData(ecmcScheduleInfoService.getByTriggerName(taskId));
			return reJson;
		} catch (Exception e) {
			throw e;
		}
	}

	protected String getUUID() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString().replace("-", "");
	}

}
