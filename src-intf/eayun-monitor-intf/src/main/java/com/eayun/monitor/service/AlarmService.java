package com.eayun.monitor.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.monitor.bean.ContactGroupAvailable;
import com.eayun.monitor.bean.MonitorItem;
import com.eayun.monitor.bean.MonitorMngData;
import com.eayun.monitor.bean.MonitorZB;
import com.eayun.monitor.model.*;

public interface AlarmService {

    public Page getPagedAlarmRuleList(String cusId, String name, String monitorItemID, Page page, QueryMap queryMap) throws AppException;

    /**
     * 隐藏项目信息，改为使用数据中心名称查询
     * @param sessionUser
     * @param vmName
     * @param prjName
     * @param alarmType
     * @param processedSign
     * @param page
     * @param queryMap
     * @return
     * @throws AppException
     */
    public Page getPagedAlarmMsgList(SessionUserInfo sessionUser, String vmName, String dcName, String alarmType,String monitorType, String processedSign, Page page, QueryMap queryMap) throws AppException;
    
    public AlarmRule getAlarmRuleById(String cusId, String alarmRuleId) throws AppException;
    
    public List<MonitorItem> getMonitorItemList() throws AppException;

    public List<MonitorZB> getMonitorZBList(String monitorItemNodeId) throws AppException;

    public List<MonitorZB> getMonitorZBListByRuleId(String alarmRuleId) throws AppException;
    
    @SuppressWarnings("rawtypes")
    public AlarmRule addAlarmRule(AlarmRule alarmRule, List<Map> triggerConditionList) throws AppException;

    public boolean deleteAlarmRule(AlarmRule alarmRule) throws AppException;

    public boolean copyAlarmRule(AlarmRule alarmRule) throws AppException;

    public AlarmTrigger addAlarmTrigger(AlarmTrigger trigger) throws AppException;

    public boolean checkRuleName(String cusId, String alarmRuleName) throws AppException;

    public List<AlarmTrigger> getAlarmTriggerListByRuleId(String alarmRuleId) throws AppException;

    @SuppressWarnings("rawtypes")
    public AlarmRule updateAlarmRule(AlarmRule alarmRule, List<Map> triggerConditionList) throws AppException;

    public boolean deleteAllTriggersByRuleId(String id) throws AppException;

    public List<AlarmObject> getAlarmObjectListByRuleId(String alarmRuleId, String monitorType) throws AppException;

    public List<AlarmObject> getAvailableAlarmObjectsByCustomer(String cusId, String alarmRuleId, String monitorType) throws AppException;
    
    public AlarmObject addAlarmObject(AlarmObject alarmObject) throws AppException;

    public boolean deleteAlarmObject(AlarmObject alarmObject) throws AppException;

    public List<AlarmContact> getAlarmContactListByRuleId(String alarmRuleId) throws AppException;

    public List<ContactGroupAvailable> getAvailableContactGroupsList(String cusId) throws AppException;

    public AlarmContact addAlarmContact(AlarmContact alarmContact) throws AppException;

    public boolean deleteAlarmContact(AlarmContact alarmContact) throws AppException;

    public boolean deleteAllObjectsByRuleId(String alarmRuleId) throws AppException;

    public boolean deleteAllContactsByRuleId(String alarmRuleId) throws AppException;

    @SuppressWarnings("rawtypes")
    public void addAlarmObject(String alarmRuleId, List<Map> alarmObjectList, String monitorType) throws AppException;

    @SuppressWarnings("rawtypes")
    public void addAlarmContact(String alarmRuleId, List<Map> alarmContactList) throws AppException;

    public JSONObject getAlarmRuleParams(String cusId, String alarmRuleId) throws AppException;

    public AlarmObject getAlarmObject(String objectId);

    public AlarmTrigger getAlarmTrigger(String triggerId);
    
    public boolean cleanAlarmDataAfterDeletingVM(String resourceId);

    public Contact getContactById(String contactId) ;

    public List<AlarmContact> getAllAlarmContactList(String cusId);

    public int getUnhandledAlarmMsgNumberByCusId(SessionUserInfo sessionUser);

    /**
     * 对客户隐藏项目信息，因此内部已改为查询数据中心名称
     * @param sessionUser
     * @return
     */
    public List<String> getPrjNamesBySession(SessionUserInfo sessionUser);

    public void getUnhandledAlarmMsgList(SessionUserInfo sessionUser, JSONObject object, String prjId);

    /**
     * 监控报警时，根据项目Id查询客户的项目短信配额
     * @Author: duanbinbin
     * @param prjId
     * @return
     *<li>Date: 2017年3月3日</li>
     */
    public Map<String, String> getCusInfoByPrjId(String prjId);

    public AlarmMessage getLatestAlarmMessageByMonitorAlarmItemId(String id);

    /**
     * 更新ECSC监控报警缓存，主要更新alarmObject和alarmTrigger
     */
    void syncEcscMonitor();

    /**
     * 根据监控项类型查询报警类型列表
     * @Author: duanbinbin
     * @param monitorType
     * @return
     *<li>Date: 2017年3月2日</li>
     */
	public List<MonitorMngData> getAlarmTypeByMonitor(String monitorType);
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
	 * 资源类型为云主机、云数据库、负载均衡
	 * @Author: duanbinbin
	 * @param resourceType
	 * @param resourceId
	 *<li>Date: 2017年3月14日</li>
	 */
	public void deleteMonitorByResource(String resourceType, String resourceId);
}
