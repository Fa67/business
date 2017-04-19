package com.eayun.ecmcschedule.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.ecmcschedule.model.BaseEcmcScheduleInfo;

public interface EcmcScheduleInfoDao extends IRepository<BaseEcmcScheduleInfo, String> {

	@Query("from BaseEcmcScheduleInfo where triggerName=?")
	public BaseEcmcScheduleInfo  findByTriggerName(String triggerName);

	@Query("select triggerName from BaseEcmcScheduleInfo")
	public List<String> findAllTaskId();
}
