package com.eayun.monitor.dao;

import java.util.List;

import com.eayun.monitor.model.BaseEcmcAlarmTrigger;
import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.monitor.model.BaseEcmcAlarmRule;
import com.eayun.virtualization.model.BaseCloudVm;
import org.springframework.data.repository.query.Param;

public interface EcmcAlarmRuleDao extends IRepository<BaseEcmcAlarmRule, String> {
	  
    @Query(" from BaseCloudVm  where vmId not in(select aoObjectId from BaseEcmcAlarmObject where alarmRuleId=? ) and isDeleted=? and isVisable=?")
  
    public List<BaseCloudVm> getAllListAlarmObject(String ruleid,String dele,String isv);


    @Query("from BaseEcmcAlarmRule where monitorItem = :monitorItem ")
    public List<BaseEcmcAlarmRule> getAllListByRuleType(@Param(value = "monitorItem") String monitorItem);


   
}
