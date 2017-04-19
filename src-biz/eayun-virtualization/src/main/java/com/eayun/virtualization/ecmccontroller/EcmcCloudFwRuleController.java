package com.eayun.virtualization.ecmccontroller;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.eayunstack.model.FirewallRule;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.virtualization.ecmcservice.EcmcCloudFireWallPoliyService;
import com.eayun.virtualization.ecmcservice.EcmcCloudFwRuleService;
import com.eayun.virtualization.model.BaseCloudFwRule;
import com.eayun.virtualization.model.CloudFwPolicy;
import com.eayun.virtualization.model.CloudFwRule;


/**
 * @author jingang.liu@eayun.com to beijing
 * @date 2016年4月12日
 */
@Controller
@RequestMapping("/ecmc/virtual/cloudfwrule")
public class EcmcCloudFwRuleController {

	private static final Log log = LogFactory.getLog(EcmcCloudFwRuleController.class);
	
	@Autowired
	private EcmcCloudFwRuleService cloudfwruleservice;
	@Autowired
    private EcmcLogService logServer;
	/**创建策略*/
	@Autowired
	private EcmcCloudFireWallPoliyService poliyservice;
	/**
	 * 用于校验规则名称是否已经存在
	 * @param request
	 * @param fwrName
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/checkFwRuleName")
	@ResponseBody
	public Object checkFwRuleName(HttpServletRequest request,@RequestBody Map<String, String> map) throws AppException{
		log.info("校验规则名称是否已经存在");
		String fwrName = map.get("fwrName");
		String datacenterId = map.get("datacenterId");
		String projectId = map.get("projectId");
		String id = map.get("id");
		String fwrNamex = "";
		EayunResponseJson res = new EayunResponseJson();
		try {
			if(null!=fwrName){
				fwrNamex = java.net.URLDecoder.decode(fwrName,"UTF-8");
			}
			res.setRespCode(ConstantClazz.SUCCESS_CODE);
			res.setData(cloudfwruleservice.checkFwRuleName(datacenterId, projectId, fwrNamex, id));
		} catch (UnsupportedEncodingException e) {
		    log.error(e.toString(),e);
			res.setRespCode(ConstantClazz.ERROR_CODE);
			res.setMessage("error.openstack.EcmcCloudFwRule:"+e.getMessage());
		} catch (AppException e){
			throw e;
		}
		return res;
	}
	
	/**
	 * 查询所有防火墙规则的信息，以列表形式在页面展示
	 * @param request
	 * @param page
	 * @param mapparems
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/list")
	@ResponseBody
	public Object list(HttpServletRequest request,Page page,@RequestBody ParamsMap mapparems) throws AppException{
		log.info("查询所有防火墙规则的信息，以列表形式在页面展示");
		String prjName = mapparems.getParams().get("prjName") == null ? null : mapparems.getParams().get("prjName").toString();
		String name = mapparems.getParams().get("name") == null ? null : mapparems.getParams().get("name").toString();
		String datacenterId = mapparems.getParams().get("datacenterId") == null ? null : mapparems.getParams().get("datacenterId").toString();
		String cusOrg = mapparems.getParams().get("cusOrg") == null ? null : mapparems.getParams().get("cusOrg").toString();
		String fwpId = mapparems.getParams().get("fwpId") == null ? null : mapparems.getParams().get("fwpId").toString();
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(mapparems.getPageNumber());
        queryMap.setCURRENT_ROWS_SIZE(mapparems.getPageSize());
		page = cloudfwruleservice.list(page,datacenterId, prjName, name,cusOrg, queryMap,fwpId);
		return page;
	}
	
	/**
	 * 查询所有防火墙规则的信息，以下拉列表形式在页面展示
	 * @param projectId
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/listForPolicy")
	@ResponseBody
	public Object listForPolicy(@RequestBody Map<String, String> map) throws AppException{
		log.info("查询所有防火墙规则的信息，以下拉列表形式在页面展示");
		EayunResponseJson res = new EayunResponseJson();
		try {
			res.setRespCode(ConstantClazz.SUCCESS_CODE);
			res.setData(cloudfwruleservice.listForPolicy(map.get("projectId"),map.get("datacenterId")));
		} catch (AppException e) {
			res.setRespCode(ConstantClazz.ERROR_CODE);
			res.setMessage("ecmc.virtual.cloudfwpoliy:"+e.getMessage());
			throw e;
		}
		return res;
	}
	
	/**
	 * 查询指定id的防火墙规则的信息
	 * @param id
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/getOne")
	@ResponseBody
	public Object getById(@RequestBody Map<String, String> map) throws AppException{
		log.info("查询指定id的防火墙规则的信息");
		EayunResponseJson res = new EayunResponseJson();
		try {
			res.setRespCode(ConstantClazz.SUCCESS_CODE);
			res.setData(cloudfwruleservice.getById(map.get("id")));
		} catch (AppException e) {
			res.setRespCode(ConstantClazz.ERROR_CODE);
			res.setMessage("ecmc.virtual.cloudfwpoliy:"+e.getMessage());
			throw e;
		}
		return res;
	}
	
	/**
	 * 创建防火墙规则
	 * @param parmes
	 * @return
	 */
	@RequestMapping("/createfwRule")
	@ResponseBody
	public Object create(@RequestBody Map<String, String> parmes) throws AppException{
		log.info("创建防火墙规则");
		EayunResponseJson res = new EayunResponseJson();
		FirewallRule result = new FirewallRule();
		try {
			result = cloudfwruleservice.createFwRuleToPoliy(parmes);
			if (result!=null) {
				res.setRespCode(ConstantClazz.SUCCESS_CODE);
				res.setData(result);
				logServer.addLog("创建防火墙规则",ConstantClazz.LOG_TYPE_FIRERULE, parmes.get("name"),parmes.get("projectId"), 1,result.getId(),null);
			}else{
				res.setRespCode(ConstantClazz.ERROR_CODE);
				res.setMessage("创建防火墙失败");
				logServer.addLog("创建防火墙规则",ConstantClazz.LOG_TYPE_FIRERULE, parmes.get("name"),parmes.get("projectId"), 0,null,null);
			}
		} catch (Exception e) {
			res.setRespCode(ConstantClazz.ERROR_CODE);
			res.setMessage("创建防火墙失败");
			logServer.addLog("创建防火墙规则",ConstantClazz.LOG_TYPE_FIRERULE, parmes.get("name"),parmes.get("projectId"), 0,null,e);
			throw e;
		}
		return res;
	}
	/**
	 * 修改防火墙规则
	 * @param parmes
	 * @return
	 */
	@RequestMapping("/updatefwRule")
	@ResponseBody
	public Object update(@RequestBody Map<String, String> parmes)throws AppException{
		log.info("修改防火墙规则");
		EayunResponseJson res = new EayunResponseJson();
		try {
			FirewallRule result = cloudfwruleservice.updateCloudFwRule(parmes);
			logServer.addLog("修改防火墙规则",ConstantClazz.LOG_TYPE_FIRERULE, parmes.get("name"),parmes.get("projectId"), 1,parmes.get("id"),null);
			if (result!=null) {
				res.setRespCode(ConstantClazz.SUCCESS_CODE);
				res.setData(result);
			}else{
				res.setRespCode(ConstantClazz.ERROR_CODE);
				res.setMessage("修改规则失败");
				logServer.addLog("修改防火墙规则",ConstantClazz.LOG_TYPE_FIRERULE, parmes.get("name"),parmes.get("projectId"), 0,parmes.get("id"),null);
			}
		} catch (Exception e) {
			res.setRespCode(ConstantClazz.ERROR_CODE);
			res.setMessage("修改规则失败");
			logServer.addLog("修改防火墙规则",ConstantClazz.LOG_TYPE_FIRERULE, parmes.get("name"),parmes.get("projectId"), 0,parmes.get("id"),e);
			throw e;
		}
		
		return res;
	}
	/**
	 * 通过datacenterId, projectId, id删除防火墙规则
	 * @param parmes
	 * @return
	 */
	@RequestMapping("/deletefwRule")
	@ResponseBody
	public Object delete(@RequestBody Map<String, String> parmes)throws AppException{
		log.info("删除防火墙规则");
		EayunResponseJson res = new EayunResponseJson();
		String datacenterId = parmes.get("datacenterId");
		String projectId = parmes.get("projectId");
		String id = parmes.get("id");
		String fwpId = parmes.get("fwpId");
		BaseCloudFwRule fireWallRule = cloudfwruleservice.getById(id);
		try {
			cloudfwruleservice.deleteFwRuletoPolicy(datacenterId, projectId, id,fwpId);
			res.setRespCode(ConstantClazz.SUCCESS_CODE);
			res.setData("防火墙规则删除成功");
			logServer.addLog("删除防火墙规则",ConstantClazz.LOG_TYPE_FIRERULE, fireWallRule.getFwrName(),fireWallRule.getPrjId(), 1,fireWallRule.getFwrId(),null);
		} catch (Exception e) {
			res.setRespCode(ConstantClazz.ERROR_CODE);
			res.setData("防火墙规则删除失败");
			logServer.addLog("删除防火墙规则",ConstantClazz.LOG_TYPE_FIRERULE, fireWallRule.getFwrName(),fireWallRule.getPrjId(), 0,fireWallRule.getFwrId(),e);
			throw e;
		}
		return res;
	}
	/**
	 * 禁用规则or启用规则
	 * @param parmes
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/updateIsEnabled")
	@ResponseBody
	public Object updateIsEnabled(@RequestBody Map<String, String> parmes)throws AppException{
		log.info("防火墙规则禁用启用");
		EayunResponseJson res = new EayunResponseJson();
		try {
			FirewallRule result = cloudfwruleservice.isEnabled(parmes);
			res.setRespCode(ConstantClazz.SUCCESS_CODE);
			if(result.isEnabled()){
				res.setData("防火墙规则启用成功");
			}else{
				res.setData("防火墙规则禁用成功");
			}
			logServer.addLog("防火墙规则禁用启用",ConstantClazz.LOG_TYPE_FIRERULE, result.getName(),parmes.get("prjId"), 1,result.getId(),null);
		} catch (Exception e) {
			res.setRespCode(ConstantClazz.ERROR_CODE);
			res.setMessage("禁用规则or启用规则失败");
			logServer.addLog("防火墙规则禁用启用",ConstantClazz.LOG_TYPE_FIRERULE, parmes.get("fwrName"),parmes.get("prjId"), 0,parmes.get("fwrId"),e);
			throw e;
		}
		return res;
	}
}
