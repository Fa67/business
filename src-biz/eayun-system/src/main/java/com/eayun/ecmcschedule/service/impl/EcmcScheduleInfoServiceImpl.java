package com.eayun.ecmcschedule.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Trigger.TriggerState;
import org.quartz.impl.jdbcjobstore.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.ecmcschedule.dao.EcmcScheduleInfoDao;
import com.eayun.ecmcschedule.model.BaseEcmcScheduleInfo;
import com.eayun.ecmcschedule.model.EcmcScheduleInfo;
import com.eayun.ecmcschedule.service.EcmcScheduleInfoService;
import com.eayun.ecmcuser.model.BaseEcmcSysUser;
import com.eayun.ecmcuser.model.EcmcSysUser;
import com.eayun.ecmcuser.service.EcmcSysUserService;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.schedule.service.ScheduleService;

@Service
@Transactional
public class EcmcScheduleInfoServiceImpl implements EcmcScheduleInfoService {
	
	@Autowired
	private EcmcScheduleInfoDao ecmcScheduleInfoDao;
//	@Autowired
	private ScheduleService scheduleService;
	@Autowired
	private EcmcSysUserService ecmcSysUserService;

	@Override
	public void add(EcmcScheduleInfo scheduleInfo) throws Exception {
		try {
			scheduleService.addTask(scheduleInfo);
			BaseEcmcScheduleInfo baseScheduleInfo = new BaseEcmcScheduleInfo();
			BeanUtils.copyPropertiesByModel(baseScheduleInfo, scheduleInfo);
			baseScheduleInfo.setCreateTime(new Date());
			BaseEcmcSysUser createUser = EcmcSessionUtil.getUser();
            if (createUser != null) {
            	baseScheduleInfo.setCreateBy(createUser.getId());
            }
			ecmcScheduleInfoDao.save(baseScheduleInfo);
		} catch (Exception e) {
			throw e;
		}
	}
	
	@SuppressWarnings("unchecked")
    @Override
	public List<EcmcScheduleInfo> getTaskList(String queryName, String state) {
		try {
			List<String> params = new ArrayList<>();
			StringBuffer sqlBuff = new StringBuffer();
			sqlBuff.append("select t.trigger_name as triggerName, ");
			sqlBuff.append("j.job_group as beanName, ");
			sqlBuff.append("j.description as description, ");
			sqlBuff.append("t.trigger_state as taskState, ");
			sqlBuff.append("c.cron_expression as cronExpression, ");
			sqlBuff.append("t.prev_fire_time as prevFireTime, ");
			sqlBuff.append("t.next_fire_time as nextFireTime, ");
			sqlBuff.append("t.description as triggerDesc, ");
			sqlBuff.append("t.job_name as jobName ");
			sqlBuff.append("from QRTZ_TRIGGERS t ");
			sqlBuff.append("left join QRTZ_JOB_DETAILS j on t.job_name = j.job_name and t.job_group = j.job_group and t.sched_name = j.sched_name ");
			sqlBuff.append("left join QRTZ_CRON_TRIGGERS c on t.trigger_name = c.trigger_name and t.trigger_group = c.trigger_group and t.sched_name = c.sched_name ");
			sqlBuff.append("where 1=1 ");
			if (!StringUtil.isEmpty(queryName)) {
				sqlBuff.append("and ( t.trigger_name like ? escape '/' or t.job_name like ? escape '/') ");
				params.add("%" + escapeSpecialChar(queryName) + "%");
				params.add("%" + escapeSpecialChar(queryName) + "%");
			}
			if (!StringUtil.isEmpty(state)) {
				sqlBuff.append("and t.trigger_state = ? ");
				params.add(state);
			}
			sqlBuff.append("and  t.trigger_name not like 'MT_%' ");		//排除手动执行时生成的临时触发器
			sqlBuff.append("order by t.job_name ");
			List<Object[]>  pageResult = ecmcScheduleInfoDao.createSQLNativeQuery(sqlBuff.toString(), params.toArray()).getResultList();
			List<EcmcScheduleInfo> resultList = new ArrayList<EcmcScheduleInfo>();
			if (pageResult != null && pageResult.size() > 0) {
				for (Object[] objects : pageResult) {
					EcmcScheduleInfo scheduleInfo = new EcmcScheduleInfo();
					scheduleInfo.setTriggerName(ObjectUtils.toString(objects[0], null));
					scheduleInfo.setBeanName(ObjectUtils.toString(objects[1], null));
					scheduleInfo.setMethodName(ObjectUtils.toString(objects[2], null));
					scheduleInfo.setTaskState(ObjectUtils.toString(objects[3], null));
					scheduleInfo.setCronExpression(ObjectUtils.toString(objects[4], null));
					scheduleInfo.setPreExcTime(stringToDate(ObjectUtils.toString(objects[5], null)));
					scheduleInfo.setNextExcTime(stringToDate(ObjectUtils.toString(objects[6], null)));
					scheduleInfo.setTaskDesc(ObjectUtils.toString(objects[7], null));
					scheduleInfo.setJobName(ObjectUtils.toString(objects[8], null));
					resultList.add(scheduleInfo);
				}
			}
			return resultList;
		} catch (Exception e) {
			throw e;
		}
	}
	
	@Override
	public BaseEcmcScheduleInfo getByTriggerName(String triggerName) {
		return ecmcScheduleInfoDao.findByTriggerName(triggerName);
	}
	
	@Override
	public List<String> getAllTaskId() {
		return ecmcScheduleInfoDao.findAllTaskId();
	}
	
	@Override
	public void update(EcmcScheduleInfo scheduleInfo, String oldBeanName) throws Exception {
		try {
			scheduleService.modifyTask(scheduleInfo, oldBeanName);
			BaseEcmcScheduleInfo baseScheduleInfo = new BaseEcmcScheduleInfo();
			BeanUtils.copyPropertiesByModel(baseScheduleInfo, scheduleInfo);
			ecmcScheduleInfoDao.saveOrUpdate(baseScheduleInfo);
		} catch (Exception e) {
			throw e;
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
	
	private Date stringToDate(String dateStr){
		if(StringUtils.equals(dateStr, "-1")){
			return null;
		}
		return DateUtil.timestampToDate(dateStr);
	}

	@Override
	public void deleteTask(String taskId, String beanName) throws Exception {
		try {
			ecmcScheduleInfoDao.delete(taskId);
			scheduleService.deleteTask(taskId, beanName);
		} catch (Exception e) {
			throw e;
		}
		
	}

	@Override
	public EcmcScheduleInfo getTask(String taskId, String beanName) throws Exception {
		try {
			BaseEcmcScheduleInfo baseScheduleInfo = ecmcScheduleInfoDao.findByTriggerName(taskId);
			EcmcSysUser ecmcSysUser = null;
			EcmcScheduleInfo scheduleInfo = scheduleService.getTask(taskId, beanName);
			if(baseScheduleInfo!=null){
				ecmcSysUser = ecmcSysUserService.findUserById(baseScheduleInfo.getCreateBy());
				scheduleInfo.setCreateTime(baseScheduleInfo.getCreateTime());
				scheduleInfo.setCreateBy(baseScheduleInfo.getCreateBy());
	            scheduleInfo.setTaskName(baseScheduleInfo.getTaskName());
	            if(ecmcSysUser != null){
	                scheduleInfo.setCreateUserName(ecmcSysUser.getName());
	            }
			}

			return scheduleInfo;
		} catch (Exception e) {
			throw e;
		}
	}
}