package com.eayun.monitor.ecmcservice;

import java.util.List;
import java.util.Map;

import com.eayun.monitor.bean.EcmcAlarmMsgExcel;
import com.eayun.monitor.model.BaseEcmcAlarmMessage;
import com.eayun.monitor.model.EcmcAlarmMessage;
import com.eayun.monitor.model.EcmcAlarmObject;
import com.eayun.monitor.model.EcmcMonitorAlarmItem;

public interface EcmcMonitorAlarmService {
	
	public List<EcmcAlarmMsgExcel> getEcmcAlarmMsgExcel();
	
	public boolean removeAlarmMsgByIds(List<String> checkedIds);
	
	/**
	 * 根据运维报警对象 删除监控报警项
	 * @Author: duanbinbin
	 * @param alarmObject
	 *<li>Date: 2017年3月9日</li>
	 */
	public void deleteMonAlarmItemByAlarmObject(EcmcAlarmObject alarmObject);

	/**
	 * 根据报警规则ID删除监控报警项
	 * @Author: duanbinbin
	 * @param ruleId
	 *<li>Date: 2017年3月9日</li>
	 */
	public void deleteMonAlarmItemByRuleId(String ruleId);

	public void updateEcmcMonItemByRuleId(String ruleId);
	
	public List<EcmcMonitorAlarmItem> getAllEcmcMonAlarmItemList();

	public void addEcmcAlarmMessage(EcmcAlarmMessage ecmcAlarmMessage);

	public void updateEcmcMonitorAlarmItem(EcmcMonitorAlarmItem ecmcMonitorAlarmItem);

	/**
	 * 添加单个对象的监控报警项
	 * @Author: duanbinbin
	 * @param alarmObject
	 *<li>Date: 2017年3月9日</li>
	 */
	public void addEcmcMonItemByObject(EcmcAlarmObject alarmObject);

	public void saveAlarmMessages(Map<String, List<BaseEcmcAlarmMessage>> alarmMessages) ;

}
