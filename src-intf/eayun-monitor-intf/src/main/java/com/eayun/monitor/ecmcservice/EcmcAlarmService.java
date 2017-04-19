package com.eayun.monitor.ecmcservice;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.monitor.bean.EcmcConGroupBy;
import com.eayun.monitor.bean.MonitorMngData;
import com.eayun.monitor.model.EcmcAlarmContact;
import com.eayun.monitor.model.EcmcAlarmMessage;
import com.eayun.monitor.model.EcmcAlarmObject;
import com.eayun.monitor.model.EcmcAlarmRule;
import com.eayun.monitor.model.EcmcAlarmTrigger;
import com.eayun.monitor.model.EcmcContact;

public interface EcmcAlarmService {

	public Page getEcmcPageMsg(Page page, QueryMap queryMap, String queryType, String queryName,
			String amType ,String isProcessed ,String dcName ) throws AppException;

	public int getEcmcUnMsgCount(String cusId, String prjId);

	public Page getEcmcPageRule(Page page, QueryMap queryMap, String name,
			String monitorItem);

	public EcmcAlarmRule copyEcmcAlarmRule(EcmcAlarmRule ecmcAlarmRule);

	public boolean deleteEcmcAlarmRule(String ruleId);

	@SuppressWarnings("rawtypes")
    public EcmcAlarmRule addEcmcAlarmRule(EcmcAlarmRule ecmcAlarmRule,
			List<Map> triggerConditionList);

	public List<MonitorMngData> getEcmcItemList(String parentId);

	public List<MonitorMngData> getZbListByItem(String parentId);

	public List<MonitorMngData> getEcmcTriggerOperator(String parentId);

	public List<MonitorMngData> getEcmcTriggerTimes(String parentId);

	public EcmcAlarmRule getEcmcRuleById(String alarmRuleId);

	@SuppressWarnings("rawtypes")
    public EcmcAlarmRule updateEcmcRule(EcmcAlarmRule ecmcAlarmRule,
			List<Map> triggerConditionList);

	public Page getEcmcObjPageInRule(String ruleId,String monitorType,QueryMap queryMap);
	
	public  List<EcmcAlarmObject> getEcmcObjListInRule(String ruleId, String monitorType);

	public List<EcmcAlarmObject> getEcmcObjListOutRule(String ruleId,
			String cusId, String prjId,String monitorType);

	@SuppressWarnings("rawtypes")
    public void addEcmcObjToRule(String ruleId, List<Map> ecmcObjectList,String monitorType);
	
    public void addEcmcObjAllToRule(String ruleId, List<String> objsIdList, String monitorType);

	public void removeEcmcObjById(String id);

	public List<EcmcAlarmContact> getEcmcConsByRuleId(String ruleId);

	public List<EcmcConGroupBy> getAllConsGroupBy();

	public void addEcmcConsToRule(String ruleId, List<String> contactIds);

	public void removeEcmcConFromRule(String alarmContactId);

	public JSONObject getJsonForEcmcRuleParams(String ruleId);
	
	public EcmcAlarmObject getEcmcObjByObjId(String alarmObjectId);
	
	public EcmcAlarmTrigger getEcmcAlarmTriggerByTriId(String triggerId);
	
	public void cleanAlarmDataAfterDeletingObject(String objectId);

	public EcmcContact getEcmcContactById(String contactId);

	public EcmcAlarmMessage getLatestEcmcAlarmMsgByItemId(String itemId);

	public void resyncSmsQuotaCache() throws Exception;

	public List<EcmcAlarmContact> getAllAlarmContacts();
	
	public List<Map> getAllAlarmHost(String ruleid,String monitorType); 

    void syncEcmcMonitor();
    
    /**
	 * 一键添加报警对象时，首先需要查询出该规则下可添加的所有报警对象资源ID
	 * @Author: duanbinbin
	 * @param ruleid		规则id
	 * @param monitorType	监控项类型
	 * @return
	 *<li>Date: 2017年3月9日</li>
	 */
    public List<String> getAllObjsToAdd(String ruleId,String monitorType); 
    
    /**
	 * 删除负载均衡资源时，需对报警相关所做操作
	 * 清除该负载均衡的所有报警信息，同时清除该负载均衡下的所有异常记录
	 * @Author: duanbinbin
	 * @param poolId
	 *<li>Date: 2017年3月6日</li>
	 */
	public void clearPoolMsgAfterDeletePool(String poolId);
	
	/**
	 * 删除成员时，清除该成员产生的所有异常记录
	 * @Author: duanbinbin
	 * @param poolId
	 *<li>Date: 2017年3月6日</li>
	 */
	public void clearExpAfterDeleteMember(String memId);
	
	/**
	 * 删除健康检查时，清除该健康检查下的所有异常记录
	 * @Author: duanbinbin
	 * @param poolId
	 *<li>Date: 2017年3月6日</li>
	 */
	public void clearExpAfterDeleteHealth(String healthId);
	
	/**
	 * 解绑健康检查与负载均衡关系时，将该健康检查在该负载均衡下产生的所有异常记录是否需要修复状态都改为“否”
	 * @Author: duanbinbin
	 * @param poolId
	 *<li>Date: 2017年3月6日</li>
	 */
	public void doAfterUnbundHealth(String poolId ,String healthId);
	
	/**
	 * 彻底删除资源时清除指标的记录
	 * @Author: duanbinbin
	 * @param resourceType
	 * @param resourceId
	 *<li>Date: 2017年3月14日</li>
	 */
	public void deleteMonitorByResource(String resourceType, String resourceId);
}
