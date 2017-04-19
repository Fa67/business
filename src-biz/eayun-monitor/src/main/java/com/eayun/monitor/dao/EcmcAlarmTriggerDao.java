package com.eayun.monitor.dao;

import com.eayun.common.dao.IRepository;
import com.eayun.monitor.model.BaseEcmcAlarmTrigger;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EcmcAlarmTriggerDao extends IRepository<BaseEcmcAlarmTrigger, String> {


    @Query("from BaseEcmcAlarmTrigger where zb in (:ids)")
    public List<BaseEcmcAlarmTrigger> getAllListByTriggerType(@Param(value = "ids") List<String> ids);

    @Query("from BaseEcmcAlarmTrigger where alarmRuleId = :alarmRuleId ")
    public List<BaseEcmcAlarmTrigger> getAllListByRuleId(@Param(value = "alarmRuleId") String alarmRuleId);

}
