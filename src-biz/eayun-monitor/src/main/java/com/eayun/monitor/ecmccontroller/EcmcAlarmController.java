package com.eayun.monitor.ecmccontroller;

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
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.RedisNodeIdConstant;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.tools.ExportDataToExcel;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.monitor.bean.EcmcAlarmMsgExcel;
import com.eayun.monitor.bean.EcmcConGroupBy;
import com.eayun.monitor.bean.MonitorMngData;
import com.eayun.monitor.ecmcservice.EcmcAlarmService;
import com.eayun.monitor.ecmcservice.EcmcMonitorAlarmService;
import com.eayun.monitor.model.EcmcAlarmContact;
import com.eayun.monitor.model.EcmcAlarmObject;
import com.eayun.monitor.model.EcmcAlarmRule;

@Controller
@RequestMapping("/ecmc/monitor/alarm")
public class EcmcAlarmController {

	private static final Logger log = LoggerFactory.getLogger(EcmcAlarmController.class);
	
	@Autowired
    private EcmcAlarmService ecmcAlarmService;
    @Autowired
    private EcmcMonitorAlarmService ecmcMonitorAlarmService;
    @Autowired
    private JedisUtil jedisUtil;
    
    @Autowired
    private EcmcLogService ecmcLogService;
    
    @RequestMapping("/getecmcpagerulelist")
    @ResponseBody
    public String getEcmcPageRuleList (HttpServletRequest request , Page page , @RequestBody ParamsMap map) {
        log.info("查询运维人员创建的所有报警规则开始");
        String name = map.getParams().get("name").toString();
        String monitorItem = map.getParams().get("monitorItem").toString();
        int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        page = ecmcAlarmService.getEcmcPageRule(page , queryMap , name , monitorItem);
        return JSONObject.toJSONString(page);
    }
    
    @RequestMapping("/copyecmcalarmrule")
    @ResponseBody
    public String copyEcmcAlarmRule (HttpServletRequest request , @RequestBody EcmcAlarmRule ecmcAlarmRule) {
        log.info("复制一份报警规则开始");
        JSONObject json = new JSONObject();
        EcmcAlarmRule ecmcCopymRule;
        try {
        	ecmcCopymRule = ecmcAlarmService.copyEcmcAlarmRule(ecmcAlarmRule);
        	json.put("respCode", ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("复制报警规则", ConstantClazz.LOG_TYPE_MONITOR, ecmcCopymRule.getName(), null, 1, ecmcCopymRule.getId(), null);
		} catch (Exception e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("复制报警规则", ConstantClazz.LOG_TYPE_MONITOR, null, null, 0, null, e);
			log.error(e.getMessage(),e);
		}
        
        return json.toJSONString();
    }
    
    @RequestMapping("/deleteecmcalarmrule")
    @ResponseBody
    public String deleteEcmcAlarmRule (HttpServletRequest request , @RequestBody Map map) {
        log.info("删除报警规则开始");
        JSONObject json = new JSONObject();
        String ruleId = null == map.get("ruleId")?"":map.get("ruleId").toString();
        String ruleName = null == map.get("ruleName")?"":map.get("ruleName").toString();
        try {
			ecmcAlarmService.deleteEcmcAlarmRule(ruleId);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("删除报警规则", ConstantClazz.LOG_TYPE_MONITOR, ruleName, null, 1, ruleId, null);
		} catch (Exception e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("删除报警规则", ConstantClazz.LOG_TYPE_MONITOR, ruleName, null, 0, ruleId, e);
			log.error(e.getMessage(),e);
		}
        return json.toJSONString();
    }
    
    @SuppressWarnings("unchecked")
	@RequestMapping("/addecmcalarmrule")
    @ResponseBody
    public String addEcmcAlarmRule (HttpServletRequest request , @RequestBody Map map) {
        log.info("创建报警规则开始");
        JSONObject json = new JSONObject();
        Map ecmcRuleModel = (Map) map.get("ecmcRuleModel");
        EcmcAlarmRule ecmcAlarmRule = new EcmcAlarmRule();
        ecmcAlarmRule.setName(ecmcRuleModel.get("name").toString());
        ecmcAlarmRule.setModifyTime(new Date());
        ecmcAlarmRule.setMonitorItem(ecmcRuleModel.get("monitorItem").toString());
        List<Map> triggerConditionList = (List<Map>) map.get("triggerArray");
        
        try {
			ecmcAlarmRule = ecmcAlarmService.addEcmcAlarmRule(ecmcAlarmRule, triggerConditionList);
			json.put("data", ecmcAlarmRule);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("创建报警规则", ConstantClazz.LOG_TYPE_MONITOR, ecmcRuleModel.get("name").toString(), null, 1, ecmcAlarmRule.getId(), null);
		} catch (Exception e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("创建报警规则", ConstantClazz.LOG_TYPE_MONITOR, ecmcRuleModel.get("name").toString(), null, 0, null, e);
			log.error(e.getMessage(),e);
		}
        return json.toJSONString();
    }
    
    @RequestMapping("/getecmcitemlist")
    @ResponseBody
    public String getEcmcItemList (HttpServletRequest request) {
        log.info("获取所有运维监控项类型开始");
        JSONObject json = new JSONObject();
        String parentId = RedisNodeIdConstant.ECMC_MONITOR_TYPE_NODE_ID;
		try {
			List<MonitorMngData> MonitorMngList = ecmcAlarmService.getEcmcItemList(parentId);
			json.put("data", MonitorMngList);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
        } catch (Exception e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
		}
		return json.toJSONString();
    }
    
    @RequestMapping("/getzblistbyitem")
    @ResponseBody
    public String getZbListByItem (HttpServletRequest request , @RequestBody Map map) {
        log.info("获取监控项下所有监控指标开始");
        JSONObject json = new JSONObject();
        String parentId = map.get("parentId").toString();
		try {
			List<MonitorMngData> MonitorMngList = ecmcAlarmService.getZbListByItem(parentId);
			json.put("data", MonitorMngList);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
        } catch (Exception e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
		}
		return json.toJSONString();
    }
    
    @RequestMapping("/getecmctriggeroperator")
    @ResponseBody
    public String getEcmcTriggerOperator (HttpServletRequest request) {
        log.info("获取触发条件操作符开始");
        JSONObject json = new JSONObject();
        String parentId = RedisNodeIdConstant.ECMC_TRIGGER_OPER_NODE_ID;
		try {
			List<MonitorMngData> MonitorMngList = ecmcAlarmService.getEcmcTriggerOperator(parentId);
			json.put("data", MonitorMngList);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
        } catch (Exception e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
		}
		return json.toJSONString();
    }
    
    @RequestMapping("/getecmctriggertimes")
    @ResponseBody
    public String getEcmcTriggerTimes (HttpServletRequest request) {
        log.info("获取触发条件持续时间开始");
        JSONObject json = new JSONObject();
        String parentId = RedisNodeIdConstant.ECMC_TRIGGER_TIME_NODE_ID;
		try {
			List<MonitorMngData> MonitorMngList = ecmcAlarmService.getEcmcTriggerTimes(parentId);
			json.put("data", MonitorMngList);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
        } catch (Exception e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
		}
		return json.toJSONString();
    }
    
    @RequestMapping("/getecmcrulebyid")
    @ResponseBody
    public String getEcmcRuleById (HttpServletRequest request , @RequestBody Map map) {
        log.info("根据id获取规则开始");
        JSONObject json = new JSONObject();
        String alarmRuleId = map.get("alarmRuleId").toString();
        try {
			EcmcAlarmRule ecmcAlarmRule = ecmcAlarmService.getEcmcRuleById(alarmRuleId);
			json.put("data", ecmcAlarmRule);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
		}
        return json.toJSONString();
    }
    
    @RequestMapping("/updateecmcrule")
    @ResponseBody
    public String updateEcmcRule (HttpServletRequest request , @RequestBody Map map) {
        log.info("编辑运维规则开始");
        JSONObject json = new JSONObject();
        Map ecmcRuleModel = (Map) map.get("ecmcRuleModel");
        EcmcAlarmRule ecmcAlarmRule = new EcmcAlarmRule();
        ecmcAlarmRule.setId(ecmcRuleModel.get("id").toString());
        ecmcAlarmRule.setName(ecmcRuleModel.get("name").toString());
        ecmcAlarmRule.setModifyTime(new Date());
        ecmcAlarmRule.setMonitorItem(ecmcRuleModel.get("monitorItem").toString());
        List<Map> triggerConditionList = (List<Map>) map.get("triggerArray");
        
        try {
			ecmcAlarmRule = ecmcAlarmService.updateEcmcRule(ecmcAlarmRule, triggerConditionList);
			json.put("data", ecmcAlarmRule);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("修改报警规则", ConstantClazz.LOG_TYPE_MONITOR, ecmcRuleModel.get("name").toString(), null, 1, ecmcRuleModel.get("id").toString(), null);
		} catch (Exception e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("修改报警规则", ConstantClazz.LOG_TYPE_MONITOR, ecmcRuleModel.get("name").toString(), null, 0, ecmcRuleModel.get("id").toString(), e);
			log.error(e.getMessage(),e);
		}
        return json.toJSONString();
    }
    /**
     * 查询某运维报警规则下的运维报警对象分页列表(区分监控项类型)
     * @Author: duanbinbin
     * @param request
     * @param map
     * @return
     *<li>Date: 2017年3月2日</li>
     */
    @RequestMapping("/getecmcobjpageinrule")
    @ResponseBody
    public String getEcmcObjPageInRule (HttpServletRequest request , @RequestBody ParamsMap map) {
        log.info("查询某运维报警规则下的运维报警对象分页列表开始");
        String ruleId = map.getParams().get("ruleId").toString();
        String monitorType = map.getParams().get("monitorType").toString();
        int pageNumber = map.getPageNumber();
        Page page=null;
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(5);
        try {
			 page = ecmcAlarmService.getEcmcObjPageInRule(ruleId,monitorType,queryMap);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
        return JSONObject.toJSONString(page);
    }
    /**
     * 查询某运维报警规则下的运维报警对象列表（区分监控项类型）
     * @Author: duanbinbin
     * @param request
     * @param map
     * @return
     *<li>Date: 2017年3月2日</li>
     */
    @RequestMapping("/getecmcobjlistinrule")
    @ResponseBody
    public String getEcmcObjListInRule (HttpServletRequest request , @RequestBody Map map) {
        log.info("查询某运维报警规则下的运维报警对象列表开始");
        JSONObject json = new JSONObject();
        String ruleId = map.get("ruleId").toString();
        String monitorType = map.get("monitorType").toString();
        try {
			List<EcmcAlarmObject> ecmcObjectList = ecmcAlarmService.getEcmcObjListInRule(ruleId,monitorType);
			json.put("data", ecmcObjectList);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
		}
        return json.toJSONString();
    }
    
    /**
     * 查询不在某运维报警规则下的运维报警对象列表(区分监控项类型)
     * @Author: duanbinbin
     * @param request
     * @param map
     * @return
     *<li>Date: 2017年3月2日</li>
     */
    @RequestMapping("/getecmcobjlistoutrule")
    @ResponseBody
    public String getEcmcObjListOutRule (HttpServletRequest request , @RequestBody Map map) {
        log.info("查询不在某运维报警规则下的运维报警对象列表开始");
        JSONObject json = new JSONObject();
        String ruleId = map.get("ruleId").toString();
        String cusId = map.get("cusId").toString();
        String prjId = map.get("prjId").toString();
        String monitorType = map.get("monitorItem").toString();
        try {
			List<EcmcAlarmObject> ecmcObjectList = ecmcAlarmService.getEcmcObjListOutRule(ruleId,cusId,prjId,monitorType);
			json.put("data", ecmcObjectList);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
		}
        return json.toJSONString();
    }
    
    /**
     * 批量添加报警对象（区分监控项类别）
     * @Author: duanbinbin
     * @param request
     * @param map
     * @return
     *<li>Date: 2017年3月2日</li>
     */
    @RequestMapping("/addecmcobjtorule")
    @ResponseBody
    public String addEcmcObjToRule (HttpServletRequest request , @RequestBody Map map) {
        log.info("批量添加运维报警对象关联到某运维报警规则开始");
        JSONObject json = new JSONObject();
        String ruleName = null == map.get("ruleName")?"":map.get("ruleName").toString();
        String ruleId = null == map.get("ruleId")?"":map.get("ruleId").toString();
        String monitorType = null == map.get("monitorType")?"":map.get("monitorType").toString();
        List<Map> ecmcObjectList = (List<Map>) map.get("selectedAlarmObject");
        try {
			ecmcAlarmService.addEcmcObjToRule(ruleId,ecmcObjectList,monitorType);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("关联报警对象", ConstantClazz.LOG_TYPE_MONITOR, ruleName, null, 1, ruleId, null);
		} catch (Exception e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("关联报警对象", ConstantClazz.LOG_TYPE_MONITOR, ruleName, null, 0, ruleId, e);
			log.error(e.getMessage(),e);
		}
        return json.toJSONString();
    }
    /**
     * 一键添加报警对象（区分监控项类别）
     * @Author: duanbinbin
     * @param request
     * @param map
     * @return
     *<li>Date: 2017年3月2日</li>
     */
    @RequestMapping("/addAllecmcobjtorule")
    @ResponseBody
    public String addAllEcmcObjToRule (HttpServletRequest request , @RequestBody Map map) {
        log.info("批量添加运维报警对象关联到某运维报警规则开始");
        JSONObject json = new JSONObject();
        String ruleName = null == map.get("ruleName")?"":map.get("ruleName").toString();
        String ruleId = null == map.get("ruleId")?"":map.get("ruleId").toString();
        String monitorType = null == map.get("monitorType")?"":map.get("monitorType").toString();
       
        try {
        	List<String> objsIdList =ecmcAlarmService.getAllObjsToAdd(ruleId,monitorType);
			ecmcAlarmService.addEcmcObjAllToRule(ruleId, objsIdList,monitorType);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
			json.put("message", objsIdList.size());
			ecmcLogService.addLog("关联报警对象", ConstantClazz.LOG_TYPE_MONITOR, ruleName, null, 1, ruleId, null);
		} catch (Exception e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("关联报警对象", ConstantClazz.LOG_TYPE_MONITOR, ruleName, null, 0, ruleId, e);
			log.error(e.getMessage(),e);
		}
        return json.toJSONString();
    }
    
    
    
    @RequestMapping("/removeecmcobjfromrule")
    @ResponseBody
    public String removeEcmcObjFromRule (HttpServletRequest request , @RequestBody Map map) {
        log.info("从某运维报警规则下删除某运维报警对象开始");
        JSONObject json = new JSONObject();
        String ruleName = null == map.get("ruleName")?"":map.get("ruleName").toString();
        String ruleId = null == map.get("ruleId")?"":map.get("ruleId").toString();
        String id = map.get("id").toString();
        try {
			ecmcAlarmService.removeEcmcObjById(id);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("移除报警对象", ConstantClazz.LOG_TYPE_MONITOR, ruleName, null, 1, ruleId, null);
		} catch (Exception e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("移除报警对象", ConstantClazz.LOG_TYPE_MONITOR, ruleName, null, 0, ruleId, e);
			log.error(e.getMessage(),e);
		}
        return json.toJSONString();
    }
    
    @RequestMapping("/getecmcconsbyrule")
    @ResponseBody
    public String getEcmcConsByRule (HttpServletRequest request , @RequestBody Map map) {
        log.info("查询某报警规则下关联的联系人开始");
        JSONObject json = new JSONObject();
        String ruleId = map.get("ruleId").toString();
        try {
			List<EcmcAlarmContact> ecmcContactList = ecmcAlarmService.getEcmcConsByRuleId(ruleId);
			json.put("data", ecmcContactList);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
		}
        return json.toJSONString();
    }
    
    @RequestMapping("/getallconsgroupby")
    @ResponseBody
    public String getAllConsGroupBy (HttpServletRequest request) {
        log.info("按组别查询所有运维报警联系人开始");
        JSONObject json = new JSONObject();
        try {
			List<EcmcConGroupBy> ecmcConGroupByList = ecmcAlarmService.getAllConsGroupBy();
			json.put("data", ecmcConGroupByList);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
		}
        return json.toJSONString();
    }
    
    @RequestMapping("/getallalarmcontacts")
    @ResponseBody
    public String getAllAlarmContacts (HttpServletRequest request) {
        log.info("查询所有运维报警联系人开始");
        JSONObject json = new JSONObject();
        try {
			List<EcmcAlarmContact> ecmcConList = ecmcAlarmService.getAllAlarmContacts();
			json.put("data", ecmcConList);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
		}
        return json.toJSONString();
    }
    
    @RequestMapping("/addecmcconstorule")
    @ResponseBody
    public String addEcmcConsToRule (HttpServletRequest request , @RequestBody Map map) {
        log.info("批量添加报警联系人到某报警规则开始");
        JSONObject json = new JSONObject();
        String ruleId = null == map.get("ruleId")?"":map.get("ruleId").toString();
        String ruleName = null == map.get("ruleName")?"":map.get("ruleName").toString();
        List<String> contactIds =  (List<String>)map.get("contactIds");
        try {
			ecmcAlarmService.addEcmcConsToRule(ruleId,contactIds);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("关联报警联系人", ConstantClazz.LOG_TYPE_MONITOR, ruleName, null, 1, ruleId, null);
		} catch (Exception e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("关联报警联系人", ConstantClazz.LOG_TYPE_MONITOR, ruleName, null, 0, ruleId, e);
			log.error(e.getMessage(),e);
		}
        return json.toJSONString();
    }
    
    @RequestMapping("/removeecmcconfromrule")
    @ResponseBody
    public String removeEcmcConFromRule (HttpServletRequest request , @RequestBody Map map) {
        log.info("从某运维报警规则下解绑某运维联系人开始");
        JSONObject json = new JSONObject();
        String ruleName = null == map.get("ruleName")?"":map.get("ruleName").toString();
        String ruleId = null == map.get("ruleId")?"":map.get("ruleId").toString();
        String alarmContactId = map.get("alarmContactId").toString();
        try {
			ecmcAlarmService.removeEcmcConFromRule(alarmContactId);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("移除报警联系人", ConstantClazz.LOG_TYPE_MONITOR, ruleName, null, 1, ruleId, null);
		} catch (Exception e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("移除报警联系人", ConstantClazz.LOG_TYPE_MONITOR, ruleName, null, 0, ruleId, e);
			log.error(e.getMessage(),e);
		}
        return json.toJSONString();
    }
    
    @RequestMapping("/getjsonforecmcruleparams")
    @ResponseBody
    public String getJsonForEcmcRuleParams (HttpServletRequest request , @RequestBody Map map) {
        log.info("查询编辑报警规则时需要查询出触发条件等参数开始");
        String ruleId = map.get("ruleId").toString();
        JSONObject json = new JSONObject();
        json = ecmcAlarmService.getJsonForEcmcRuleParams(ruleId);
        return json.toJSONString();
    }
    
    @RequestMapping("/exportmsgexcel")
    @ResponseBody
    public String exportMsgExcel (HttpServletRequest request, HttpServletResponse response, String browser) {
        log.info("导出所有运维报警信息excel开始");
        List<EcmcAlarmMsgExcel> list = ecmcMonitorAlarmService.getEcmcAlarmMsgExcel();
        ExportDataToExcel<EcmcAlarmMsgExcel> excel = new ExportDataToExcel<EcmcAlarmMsgExcel>();
        response.setContentType("application/vnd.ms-excel");
        
        try {
			String fileName = "";
			if("Firefox".equals(browser)){
			    fileName = new String("报警信息.xls".getBytes(), "iso-8859-1");
			}else{
			    fileName = URLEncoder.encode("报警信息.xls", "UTF-8") ;
			}
			response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
			if(list.size()==0){
				EcmcAlarmMsgExcel msgExcel = new EcmcAlarmMsgExcel();
				list.add(msgExcel);
			}
			excel.exportData(list, response.getOutputStream(), "报警信息");
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
		}
        return null;
    }
    
    @RequestMapping("/getecmcpagemsglist")
    @ResponseBody
    public String getEcmcPageMsgList (HttpServletRequest request , Page page , @RequestBody ParamsMap map) {
        log.info("查询所有的运维报警信息开始");
        String queryType = map.getParams().get("queryType").toString();
        String queryName = map.getParams().get("queryName").toString();
        String amType = map.getParams().get("amType").toString();
        String isProcessed = map.getParams().get("isProcessed").toString();
        String dcName = map.getParams().get("dcName").toString();
        
        int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        
        page = ecmcAlarmService.getEcmcPageMsg(page, queryMap, queryType, queryName, amType, isProcessed, dcName);
        
        return JSONObject.toJSONString(page);
    }
    
    @SuppressWarnings("unchecked")
	@RequestMapping("/eraseecmcmsgbyids")
    @ResponseBody
    public String eraseEcmcMsgByIds (HttpServletRequest request , @RequestBody Map map) {
        log.info("批量消除报警开始");
        JSONObject json = new JSONObject();
        boolean isSuccess = false;
        List<String> checkedIds =  (List<String>)map.get("checkedIds");
        try {
			isSuccess = ecmcMonitorAlarmService.removeAlarmMsgByIds(checkedIds);
			for(int i = 0; i < checkedIds.size();i++){
				ecmcLogService.addLog("消除报警", "报警信息", null, null, 1, checkedIds.get(i), null);
			}
		} catch (Exception e) {
			for(int i = 0; i < checkedIds.size();i++){
				ecmcLogService.addLog("消除报警", "报警信息", null, null, 0, checkedIds.get(i), e);
			}
			log.error(e.getMessage(),e);
		}
        json.put("respCode", isSuccess?ConstantClazz.SUCCESS_CODE:ConstantClazz.ERROR_CODE);
        return json.toJSONString();
    }
    
    @RequestMapping("/getecmcunmsgcount")
    @ResponseBody
    public String getEcmcUnhandledAlarmMsgCount (HttpServletRequest request , @RequestBody Map map) {
        log.info("查询未处理的报警信息数量开始");
        String cusId = null == map.get("cusId")?"":map.get("cusId").toString();
        String prjId = null == map.get("prjId")?"":map.get("prjId").toString();
        JSONObject json = new JSONObject();
        try {
			int count = ecmcAlarmService.getEcmcUnMsgCount(cusId,prjId);
			json.put("count", count);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
		}
        return json.toJSONString();
    }

    @RequestMapping("/resyncsmsquotacache")
    @ResponseBody
    public String resyncSmsQuotaCache(HttpServletRequest request){
        log.info("同步报警短信配额开始");
        JSONObject json = new JSONObject();
        try {
            ecmcAlarmService.resyncSmsQuotaCache();
            json.put("respCode",ConstantClazz.SUCCESS_CODE);
        }catch(Exception e){
            json.put("respCode",ConstantClazz.ERROR_CODE);
            log.error(e.getMessage(),e);
        }
        return json.toJSONString();
    }

    @RequestMapping("/syncEcmcMonitor")
    @ResponseBody
    public String syncEcscMonitor (HttpServletRequest request) {
        log.info("同步ECMC监控报警缓存开始");
        EayunResponseJson json = new EayunResponseJson();
        try {
            ecmcAlarmService.syncEcmcMonitor();
            json.setRespCode(ConstantClazz.SUCCESS_CODE);
        } catch (Exception e) {
            json.setRespCode(ConstantClazz.ERROR_CODE);
            json.setMessage(e.toString());
            log.error(e.toString(),e);
        }
        return JSONObject.toJSONString(json);
    }
}
