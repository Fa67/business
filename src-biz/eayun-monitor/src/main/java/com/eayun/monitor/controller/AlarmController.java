package com.eayun.monitor.controller;

import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.eayun.common.model.EayunResponseJson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.tools.ExportDataToExcel;
import com.eayun.log.service.LogService;
import com.eayun.monitor.bean.AlarmMsgExcel;
import com.eayun.monitor.bean.ContactGroupAvailable;
import com.eayun.monitor.bean.MonitorItem;
import com.eayun.monitor.bean.MonitorMngData;
import com.eayun.monitor.bean.MonitorZB;
import com.eayun.monitor.model.AlarmContact;
import com.eayun.monitor.model.AlarmObject;
import com.eayun.monitor.model.AlarmRule;
import com.eayun.monitor.service.AlarmService;
import com.eayun.monitor.service.MonitorAlarmService;

@Controller
@RequestMapping("/monitor/alarm")
public class AlarmController {

    private static final Logger log = LoggerFactory.getLogger(AlarmController.class);
    
    @Autowired
    private AlarmService alarmService;
    @Autowired
    private MonitorAlarmService monitorService;
    @Autowired
    private JedisUtil jedisUtil;
    
    @Autowired
	private LogService logService;
    
    @RequestMapping(value = "/getAlarmRuleList", method = RequestMethod.POST)
    @ResponseBody
    public String getAlarmRuleList(HttpServletRequest request, Page page,@RequestBody ParamsMap map) throws Exception {
        log.info("报警规则列表查询开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        String name = map.getParams().get("name")==null?"":map.getParams().get("name").toString();
        String monitorItemID = map.getParams().get("monitorItemID")==null?"":map.getParams().get("monitorItemID").toString();
        int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        
        page = alarmService.getPagedAlarmRuleList(cusId, name, monitorItemID, page, queryMap);
        return JSONObject.toJSONString(page);
    }
    
    @RequestMapping(value = "/getAlarmMsgList", method = RequestMethod.POST)
    @ResponseBody
    public String getAlarmMsgList(HttpServletRequest request, Page page,@RequestBody ParamsMap map) throws Exception {
        log.info("报警信息列表查询开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String vmName = map.getParams().get("vmName")==null?"":map.getParams().get("vmName").toString();
        String dcName = map.getParams().get("dcName")==null?"":map.getParams().get("dcName").toString();
        String alarmType = map.getParams().get("alarmType")==null?"":map.getParams().get("alarmType").toString();
        String monitorType = map.getParams().get("monitorType")==null?"":map.getParams().get("monitorType").toString();
        String processedSign = map.getParams().get("processedSign")==null?"":map.getParams().get("processedSign").toString();
        int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        
        page = alarmService.getPagedAlarmMsgList(sessionUser, vmName, dcName, alarmType,monitorType, processedSign, page, queryMap);
        return JSONObject.toJSONString(page);
    }
    /**
     * 查询客户某一数据中心下未处理的三条报警信息
     * @Author: duanbinbin
     * @param request
     * @param map
     * @return
     * @throws Exception
     *<li>Date: 2017年3月7日</li>
     */
    @RequestMapping(value = "/getUnhandledAlarmMsgList", method = RequestMethod.POST)
    @ResponseBody
    public String getUnhandledAlarmMsgList(HttpServletRequest request, @RequestBody Map map) throws Exception {
        log.info("查询客户未处理的三条报警信息");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String prjId = map.get("prjId")==null?"":map.get("prjId").toString();
        JSONObject object = new JSONObject();
        try{
        	alarmService.getUnhandledAlarmMsgList(sessionUser,object,prjId);
        } catch (Exception e) {
        	log.error(e.toString(), e);
        }
        return JSONObject.toJSONString(object);
    }
    
    /**
     * 查询某一报警规则下的报警对象列表（区分监控项类别）
     * @Author: duanbinbin
     * @param request
     * @param map
     * @return
     * @throws Exception
     *<li>Date: 2017年3月2日</li>
     */
    @RequestMapping(value="/getAlarmObjectListByRuleId", method = RequestMethod.POST)
    @ResponseBody
    public String getAlarmObjectListByRuleId(HttpServletRequest request, @RequestBody Map map) throws Exception{
        log.info("报警对象列表查询开始");
        String alarmRuleId = map.get("alarmRuleId")==null?"":map.get("alarmRuleId").toString();
        String monitorType = map.get("monitorType")==null?"":map.get("monitorType").toString();
        List<AlarmObject> list = alarmService.getAlarmObjectListByRuleId(alarmRuleId,monitorType);
        return JSONObject.toJSONString(list);
    }
    
    @RequestMapping(value="/getAlarmContactListByRuleId", method = RequestMethod.POST)
    @ResponseBody
    public String getAlarmContactListByRuleId(HttpServletRequest request, @RequestBody Map map) throws Exception{
        log.info("报警联系人列表查询开始");
        String alarmRuleId = map.get("alarmRuleId")==null?"":map.get("alarmRuleId").toString();
        List<AlarmContact> list = alarmService.getAlarmContactListByRuleId(alarmRuleId);
        return JSONObject.toJSONString(list);
    }
    
    @RequestMapping(value="/getAllAlarmContactList", method = RequestMethod.POST)
    @ResponseBody
    public String getAllAlarmContactList(HttpServletRequest request) throws Exception{
        log.info("全部联系人列表查询开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        List<AlarmContact> list = alarmService.getAllAlarmContactList(cusId);
        return JSONObject.toJSONString(list);
    }
    /**
     * 查询某客户某项规则未添加的报警对象列表（区分监控项类型）
     * @Author: duanbinbin
     * @param request
     * @param map
     * @return
     * @throws Exception
     *<li>Date: 2017年3月2日</li>
     */
    @RequestMapping(value="/getAvailableAlarmObjectsByCustomer", method = RequestMethod.POST)
    @ResponseBody
    public String getAvailableAlarmObjectsByCustomer(HttpServletRequest request, @RequestBody Map map) throws Exception{
        log.info("查询某客户某项规则未添加的报警对象列表");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        String alarmRuleId = map.get("alarmRuleId")==null?"":map.get("alarmRuleId").toString();
        String monitorType = map.get("monitorType")==null?"":map.get("monitorType").toString();
        List<AlarmObject> list = alarmService.getAvailableAlarmObjectsByCustomer(cusId, alarmRuleId,monitorType);
        return JSONObject.toJSONString(list);
    }
    
    @RequestMapping(value="/getAvailableContactGroupsList", method = RequestMethod.POST)
    @ResponseBody
    public String getAvailableContactGroupsList(HttpServletRequest request, @RequestBody Map map) throws Exception{
        log.info("可选报警联系人列表查询开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        List<ContactGroupAvailable> list = alarmService.getAvailableContactGroupsList(cusId);
        return JSONObject.toJSONString(list);
    }
    
    @RequestMapping(value="/getMonitorItemList", method = RequestMethod.POST)
    @ResponseBody
    public String getMonitorItemList(HttpServletRequest request) throws Exception{
        log.info("监控项列表查询开始");
        List<MonitorItem> list = alarmService.getMonitorItemList();
        return JSONObject.toJSONString(list);
    }
    
    @RequestMapping(value="/getPrjNamesBySession", method = RequestMethod.POST)
    @ResponseBody
    public String getPrjNamesBySession(HttpServletRequest request) throws Exception{
        log.info("报警信息列表中所属项目查询开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        List<String> list = alarmService.getPrjNamesBySession(sessionUser);
        return JSONObject.toJSONString(list);
    }
    
    @RequestMapping(value="/getMonitorZBList", method = RequestMethod.POST)
    @ResponseBody
    public String getMonitorZBList(HttpServletRequest request, @RequestBody Map map) throws Exception{
        log.info("监控指标列表查询开始");
        String monitorItemNodeId = map.get("monitorItemNodeId")==null?"":map.get("monitorItemNodeId").toString();
        List<MonitorZB> list = alarmService.getMonitorZBList(monitorItemNodeId);
        return JSONObject.toJSONString(list);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @RequestMapping(value = "/addAlarmRule", method = RequestMethod.POST)
    @ResponseBody
    public String addAlarmRule(HttpServletRequest request, @RequestBody Map map) throws Exception{
        log.info("添加报警规则开始");
        //1.添加报警规则首先保存报警规则; 2.然后保存触发条件(如果有的话),将触发条件关联到该报警规则上
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        Map alarmRuleMap = (Map) map.get("alarmRuleModel");
        AlarmRule alarmRule = new AlarmRule();
        alarmRule.setCusId(cusId);
        alarmRule.setName(alarmRuleMap.get("name").toString());
        alarmRule.setModifyTime(new Date());
        alarmRule.setMonitorItem(alarmRuleMap.get("monitorItem").toString());
        List<Map> triggerConditionList = (List<Map>) map.get("triggerArray");
        
        try {
			alarmRule = alarmService.addAlarmRule(alarmRule,triggerConditionList);
			logService.addLog("创建报警规则", "报警规则", alarmRuleMap.get("name").toString(), null,ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (Exception e) {
			logService.addLog("创建报警规则", "报警规则", alarmRuleMap.get("name").toString(), null,ConstantClazz.LOG_STATU_ERROR, e);
			log.error(e.toString(), e);
		}
        
        return JSONObject.toJSONString(alarmRule);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @RequestMapping(value = "/editAlarmRule", method = RequestMethod.POST)
    @ResponseBody
    public String editAlarmRule(HttpServletRequest request, @RequestBody Map map) throws Exception{
        log.info("编辑报警规则开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        Map alarmRuleMap = (Map) map.get("alarmRuleModel");
        List<Map> triggerConditionList = (List<Map>) map.get("triggerArray");
        
        AlarmRule alarmRule = new AlarmRule();
        alarmRule.setId(alarmRuleMap.get("id").toString());
        alarmRule.setCusId(cusId);
        alarmRule.setName(alarmRuleMap.get("name").toString());
        alarmRule.setModifyTime(new Date());
        alarmRule.setMonitorItem(alarmRuleMap.get("monitorItem").toString());
        
        try {
			alarmRule = alarmService.updateAlarmRule(alarmRule,triggerConditionList);
			logService.addLog("修改报警规则", "报警规则", alarmRuleMap.get("name").toString(), null,ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (Exception e) {
			logService.addLog("修改报警规则", "报警规则", alarmRuleMap.get("name").toString(), null,ConstantClazz.LOG_STATU_ERROR, e);
			log.error(e.toString(), e);
		}
        
        return JSONObject.toJSONString(alarmRule);
    }
    
    @RequestMapping(value = "/deleteAlarmRule", method = RequestMethod.POST)
    @ResponseBody
    public String deleteAlarmRule(HttpServletRequest request, @RequestBody AlarmRule alarmRule) throws Exception{
        log.info("删除报警规则开始");
        boolean isDeleted = false;
		try {
			isDeleted = alarmService.deleteAlarmRule(alarmRule);
			logService.addLog("删除报警规则", "报警规则", alarmRule.getName(), null,ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (Exception e) {
			logService.addLog("删除报警规则", "报警规则", alarmRule.getName(), null,ConstantClazz.LOG_STATU_ERROR, e);
			log.error(e.toString(), e);
		}
        return JSONObject.toJSONString(isDeleted);
    }
    
    @RequestMapping(value = "/copyAlarmRule", method = RequestMethod.POST)
    @ResponseBody
    public String copyAlarmRule(HttpServletRequest request, @RequestBody AlarmRule alarmRule) throws Exception{
        log.info("复制报警规则开始");
        boolean isCopied = false;
        String newName = "复制_"+alarmRule.getName();
		try {
			isCopied = alarmService.copyAlarmRule(alarmRule);
			logService.addLog("复制报警规则", "报警规则", newName.length()<=20?newName:newName.substring(0, 20), null,ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (Exception e) {
			logService.addLog("复制报警规则", "报警规则", newName.length()<=20?newName:newName.substring(0, 20), null,ConstantClazz.LOG_STATU_ERROR, e);
			log.error(e.toString(), e);
		}
        return JSONObject.toJSONString(isCopied);
    }
    
    @RequestMapping(value = "/checkRuleName" , method = RequestMethod.POST)
    @ResponseBody
    public boolean checkRuleName(HttpServletRequest request, @RequestBody Map map) throws Exception {
        log.info("验证报警规则名称重复开始");
        String alarmRuleName = map.get("alarmRuleName").toString();
        SessionUserInfo sessionUserInfo = (SessionUserInfo) request.getSession().getAttribute(
            ConstantClazz.SYS_SESSION_USERINFO);
        return alarmService.checkRuleName(sessionUserInfo.getCusId(), alarmRuleName);
    }
    
    @RequestMapping(value = "/getAlarmRuleById" , method = RequestMethod.POST)
    @ResponseBody
    public String getAlarmRuleById(HttpServletRequest request, @RequestBody Map map) throws Exception {
        log.info("根据规则ID查询报警规则开始");
        String alarmRuleId = map.get("alarmRuleId").toString();
        SessionUserInfo sessionUserInfo = (SessionUserInfo) request.getSession().getAttribute(
            ConstantClazz.SYS_SESSION_USERINFO);
        AlarmRule alarmRule = alarmService.getAlarmRuleById(sessionUserInfo.getCusId(), alarmRuleId);
        return JSONObject.toJSONString(alarmRule);
    }
    
    @RequestMapping(value="/getAlarmRuleParamsForEdit", method = RequestMethod.POST)
    @ResponseBody
    public String getAlarmRuleParamsForEdit(HttpServletRequest request, @RequestBody Map map) throws Exception{
        log.info("为编辑报警规则页面准备参数");
        String alarmRuleId = map.get("alarmRuleId").toString();
        SessionUserInfo sessionUserInfo = (SessionUserInfo) request.getSession().getAttribute(
            ConstantClazz.SYS_SESSION_USERINFO);
        JSONObject json = alarmService.getAlarmRuleParams(sessionUserInfo.getCusId(), alarmRuleId);
        return json.toJSONString();
    }
    /**
     * 添加报警对象（区分监控项类型）
     * @Author: duanbinbin
     * @param request
     * @param map
     * @return
     * @throws Exception
     *<li>Date: 2017年3月2日</li>
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @RequestMapping(value="/addAlarmObject", method = RequestMethod.POST)
    @ResponseBody
    public String addAlarmObject(HttpServletRequest request, @RequestBody Map map) throws Exception{
        log.info("添加报警对象开始");
        String alarmRuleId = map.get("alarmRuleId")==null?"":map.get("alarmRuleId").toString();
        String alarmRuleName = map.get("alarmRuleName")==null?"":map.get("alarmRuleName").toString();
        String monitorType = map.get("monitorType")==null?"":map.get("monitorType").toString();
        List<Map> alarmObjectList = (List<Map>) map.get("selectedAlarmObject");
        try {
			alarmService.addAlarmObject(alarmRuleId, alarmObjectList,monitorType);
			logService.addLog("关联报警对象", "报警规则", alarmRuleName, null,ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (Exception e) {
			logService.addLog("关联报警对象", "报警规则", alarmRuleName, null,ConstantClazz.LOG_STATU_ERROR, e);
			log.error(e.toString(), e);
		}
        return JSONObject.toJSONString(true);
    }
    
    @RequestMapping(value = "/deleteAlarmObject", method = RequestMethod.POST)
    @ResponseBody
    public String deleteAlarmObject(HttpServletRequest request, @RequestBody AlarmObject alarmObject) throws Exception{
        log.info("删除报警对象开始");
        boolean isDeleted = false;
		try {
			isDeleted = alarmService.deleteAlarmObject(alarmObject);
			logService.addLog("移除报警对象", "报警规则", alarmObject.getAlarmRuleName(), null,ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (Exception e) {
			logService.addLog("移除报警对象", "报警规则", alarmObject.getAlarmRuleName(), null,ConstantClazz.LOG_STATU_ERROR, e);
			log.error(e.toString(), e);
		}
        return JSONObject.toJSONString(isDeleted);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @RequestMapping(value="/addAlarmContact", method = RequestMethod.POST)
    @ResponseBody
    public String addAlarmContact(HttpServletRequest request, @RequestBody Map map) throws Exception{
        log.info("添加报警联系人开始");
        String alarmRuleId = map.get("alarmRuleId")==null?"":map.get("alarmRuleId").toString();
        String alarmRuleName = map.get("alarmRuleName")==null?"":map.get("alarmRuleName").toString();
        List<Map> alarmContactList = (List<Map>) map.get("selectedAlarmContacts");
        try {
			alarmService.addAlarmContact(alarmRuleId, alarmContactList);
			logService.addLog("关联报警联系人", "报警规则", alarmRuleName, null,ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (Exception e) {
			logService.addLog("关联报警联系人", "报警规则", alarmRuleName, null,ConstantClazz.LOG_STATU_ERROR, e);
			log.error(e.toString(), e);
		}
        return JSONObject.toJSONString(true);
    }
    
    @RequestMapping(value = "/unbindContact", method = RequestMethod.POST)
    @ResponseBody
    public String unbindContact(HttpServletRequest request, @RequestBody AlarmContact alarmContact) throws Exception{//todo
        log.info("删除报警联系人开始");
        boolean isDeleted = false;
		try {
			isDeleted = alarmService.deleteAlarmContact(alarmContact);
			logService.addLog("移除报警联系人", "报警规则", alarmContact.getAlarmRuleName(), null,ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (Exception e) {
			logService.addLog("移除报警联系人", "报警规则", alarmContact.getAlarmRuleName(), null,ConstantClazz.LOG_STATU_ERROR, e);
			log.error(e.toString(), e);
		}
        return JSONObject.toJSONString(isDeleted);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @RequestMapping(value="/eraseAlarmMsgByIds", method = RequestMethod.POST)
    @ResponseBody
    public String eraseAlarmMsgByIds(HttpServletRequest request, @RequestBody Map map) throws Exception{
        log.info("消除报警开始");
        boolean isSucceed = false;
        List<String> checkedIds =  (List<String>)map.get("checkedIds");
        try {
			isSucceed = monitorService.eraseAlarmMsgByIds(checkedIds);
			logService.addLog("消除报警", "报警信息", null, null,ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (Exception e) {
			log.error(e.toString(),e);
			logService.addLog("消除报警", "报警信息", null, null,ConstantClazz.LOG_STATU_ERROR, e);
		}
        return JSONObject.toJSONString(isSucceed);
    }
    
    @RequestMapping(value="/export2Excel")
    @ResponseBody
    public String export2Excel(HttpServletRequest request, HttpServletResponse response, String browser) throws Exception{
        log.info("导出到Excel开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        try {
            List<AlarmMsgExcel> list = monitorService.getAlarmMessagesByUserIdForExcel(sessionUser);
            ExportDataToExcel<AlarmMsgExcel> excel = new ExportDataToExcel<AlarmMsgExcel>();
            response.setContentType("application/vnd.ms-excel");
            
            String fileName = "";
            if("Firefox".equals(browser)){
                fileName = new String("报警信息.xls".getBytes(), "iso-8859-1");
            }else{
                fileName = URLEncoder.encode("报警信息.xls", "UTF-8") ;
            }
            response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
            if(list.isEmpty()){
            	AlarmMsgExcel msgExcel = new AlarmMsgExcel();
            	list.add(msgExcel);
            }
            excel.exportData(list, response.getOutputStream(), "报警信息");
        } catch (Exception e) {
            throw e;
        }
        return null;
    }
    
    @RequestMapping(value="/getUnhandledAlarmMsgNumberByCusId")
    @ResponseBody
    public String getUnhandledAlarmMsgNumberByCusId(HttpServletRequest request) throws Exception{
        log.info("当前用户下报警信息数量查询开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        int number = alarmService.getUnhandledAlarmMsgNumberByCusId(sessionUser);
        return JSONObject.toJSONString(number);
    }

    @RequestMapping("/syncEcscMonitor")
    @ResponseBody
    public String syncEcscMonitor (HttpServletRequest request) {
        log.info("同步ECSC监控报警缓存开始");
        EayunResponseJson json = new EayunResponseJson();
        try {
            alarmService.syncEcscMonitor();
            json.setRespCode(ConstantClazz.SUCCESS_CODE);
        } catch (Exception e) {
            json.setRespCode(ConstantClazz.ERROR_CODE);
            json.setMessage(e.toString());
            log.error(e.toString(),e);
        }
        return JSONObject.toJSONString(json);
    }
    /**
     * 根据监控项类型查询报警类型
     * @Author: duanbinbin
     * @param request
     * @param map
     * @return
     * @throws Exception
     *<li>Date: 2017年3月2日</li>
     */
    @RequestMapping(value="/getalarmtypebymonitor", method = RequestMethod.POST)
    @ResponseBody
    public String getAlarmTypeByMonitor(HttpServletRequest request, @RequestBody Map map) throws Exception{
        log.info("根据监控项类型查询报警类型");
        String monitorType = map.get("monitorType")==null?"":map.get("monitorType").toString();
        List<MonitorMngData> list = alarmService.getAlarmTypeByMonitor(monitorType);
        return JSONObject.toJSONString(list);
    }
}
