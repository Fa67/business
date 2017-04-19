package com.eayun.ecmcschedule.dao;

import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.ecmcschedule.model.BaseEcmcScheduleStatistics;

public interface EcmcScheduleStatisticsDao extends IRepository<BaseEcmcScheduleStatistics, String> {
	
	@Query("from BaseEcmcScheduleStatistics t where t.triggerName = ? and t.statisticsDate = ?")
	public BaseEcmcScheduleStatistics getTriggerNameAndDate(String triggerName, String date);

}
