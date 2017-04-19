package com.eayun.monitor.apiservice.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.monitor.apiservice.EcmcMonitorAlarmApiService;
import com.eayun.monitor.baseservice.BaseEcmcMonitorAlarmService;
import com.eayun.monitor.dao.EcmcMonitorAlarmItemDao;
import com.eayun.monitor.model.BaseEcmcMonitorAlarmItem;
import com.eayun.monitor.model.EcmcAlarmObject;


@Service
@Transactional
public class EcmcMonitorAlarmApiServiceImpl extends BaseEcmcMonitorAlarmService implements EcmcMonitorAlarmApiService {

	@Autowired
	private EcmcMonitorAlarmItemDao ecmcMonitorAlarmItemDao;

	/**
	 * 根据运维报警对象删除监控报警项
	 * @param alarmObject
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void deleteMonAlarmItemByAlarmObject(EcmcAlarmObject alarmObject) {
        String sql = " from BaseEcmcMonitorAlarmItem where objectId= ? and alarmRuleId = ? ";
        List<BaseEcmcMonitorAlarmItem> ecmcItemList = ecmcMonitorAlarmItemDao.find(sql, alarmObject.getId(),alarmObject.getAlarmRuleId());
        for (BaseEcmcMonitorAlarmItem baseEcmcMonitorAlarmItem : ecmcItemList) {
        	ecmcMonitorAlarmItemDao.delete(baseEcmcMonitorAlarmItem);
        }
		
	}

}
