package com.eayun.virtualization.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.log.service.LogService;
import com.eayun.virtualization.model.BaseCloudFireWall;
import com.eayun.virtualization.model.BaseCloudFwRule;
import com.eayun.virtualization.model.BaseCloudVolume;
import com.eayun.virtualization.model.CloudFireWall;
import com.eayun.virtualization.model.CloudFwPolicy;
import com.eayun.virtualization.model.CloudFwRule;
import com.eayun.virtualization.model.CloudVolume;
import com.eayun.virtualization.service.FireWallRuleService;
import com.eayun.virtualization.service.FireWallService;
import com.eayun.virtualization.service.VolumeService;

@Controller
@RequestMapping("/safety/firewallrule")
@Scope("prototype")
public class FireWallRuleController extends BaseController {
	private static final Logger log = LoggerFactory.getLogger(FireWallRuleController.class);
	@Autowired
	private FireWallRuleService fireWallRuleService;
	@Autowired
	private LogService logService;

	/**
	 * 查询防火墙规则列表
	 * 
	 * @author Chengxiaodong
	 * @param request
	 * @param datacenterId
	 * @param projectId
	 * @param name
	 * @param page
	 * @return
	 */

	@RequestMapping(value = "/getFireWallRuleList", method = RequestMethod.POST)
	@ResponseBody
	public String getFireWallRuleList(HttpServletRequest request, Page page, @RequestBody ParamsMap map) {
		try {
			log.info("查询防火墙规则列表开始");
			String prjId = map.getParams().get("prjId").toString();
			String dcId = map.getParams().get("dcId").toString();
			String fireRuleName = map.getParams().get("name").toString();
			String fwpId = map.getParams().get("fwpId").toString();
			int pageSize = 10;
			int pageNumber = map.getPageNumber();

			QueryMap queryMap = new QueryMap();
			queryMap.setPageNum(pageNumber);
			queryMap.setCURRENT_ROWS_SIZE(pageSize);
			page = fireWallRuleService.getFireWallRuleList(page, prjId, dcId, fireRuleName, queryMap, fwpId);
		} catch (AppException e) {
			throw e;
		}
		return JSONObject.toJSONString(page);

	}

	/**
	 * 根据prjId查询防火墙规则
	 * 
	 * @author Chengxiaodong
	 * @param request
	 * @param page
	 * @param map
	 * @return
	 */
	@RequestMapping(value = "/getFwRulesByPrjId", method = RequestMethod.POST)
	@ResponseBody
	public String getFwRulesByPrjId(HttpServletRequest request, Page page, @RequestBody Map map) {
		List<CloudFwRule> fwrList = null;
		try {
			String dcId = map.get("dcId").toString();
			String prjId = map.get("prjId").toString();
			fwrList = fireWallRuleService.getFwRulesByPrjId(dcId, prjId);
		} catch (AppException e) {
			throw e;
		}
		return JSONObject.toJSONString(fwrList);

	}

	/**
	 * 根据指定防火墙策略查询防火墙规则
	 * 
	 * @author Chengxiaodong
	 * @param request
	 * @param page
	 * @param map
	 * @return
	 */

	@RequestMapping(value = "/getFwRulesByfwpId", method = RequestMethod.POST)
	@ResponseBody
	public String getFwRulesByfwpId(HttpServletRequest request, Page page, @RequestBody CloudFwPolicy fwp) {
		List<CloudFwRule> fwrList = null;
		try {
			fwrList = fireWallRuleService.getFwRulesByfwpId(fwp);
		} catch (AppException e) {
			throw e;
		}
		return JSONObject.toJSONString(fwrList);

	}

	/**
	 * 删除防火墙规则
	 * 
	 * @author Chengxiaodong
	 * @param request
	 * @param fw
	 * @return
	 */
	@RequestMapping(value = "/deleteFwRule", method = RequestMethod.POST)
	@ResponseBody
	public String deleteFwRule(HttpServletRequest request, @RequestBody CloudFwRule fwr) {
		boolean isTrue = false;
		try {
			log.info("删除防火墙规则开始");
			isTrue = fireWallRuleService.deleteFwRule(fwr);
			logService.addLog("删除防火墙规则", ConstantClazz.LOG_TYPE_FIRERULE, fwr.getFwrName(), fwr.getPrjId(),
					ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (AppException e) {
			logService.addLog("删除防火墙规则", ConstantClazz.LOG_TYPE_FIRERULE, fwr.getFwrName(), fwr.getPrjId(),
					ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}

		return JSONObject.toJSONString(isTrue);

	}

	/**
	 * 创建防火墙
	 * 
	 * @author Chengxiaodong
	 * @param request
	 * @param map
	 * @return
	 */
	@RequestMapping(value = "/addFwRule.do", method = RequestMethod.POST)
	@ResponseBody
	public String addFwRule(HttpServletRequest request, @RequestBody Map map) {
		BaseCloudFwRule fwRule = null;
		Map project = (Map) map.get("project");
		try {
			log.info("创建防火墙规则开始");
			SessionUserInfo sessionUser = (SessionUserInfo) request.getSession()
					.getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
			String createName = sessionUser.getUserName();
			fwRule = fireWallRuleService.addFwRule(createName, map);
			logService.addLog("创建防火墙规则", ConstantClazz.LOG_TYPE_FIRERULE, map.get("name").toString(),
					project.get("projectId").toString(), ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (AppException e) {
			logService.addLog("创建防火墙规则", ConstantClazz.LOG_TYPE_FIRERULE, map.get("name").toString(),
					project.get("projectId").toString(), ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}
		return JSONObject.toJSONString(fwRule);
	}

	/**
	 * 验证重名
	 * 
	 * @author Chengxiaodong
	 * @param request
	 * @param map
	 * @return
	 */
	@RequestMapping(value = "/getFwRuleByName", method = RequestMethod.POST)
	@ResponseBody
	public String getFwRuleByName(HttpServletRequest request, @RequestBody Map map) {
		boolean isTrue = false;
		try {
			log.info("验证防火墙规则重名开始");
			isTrue = fireWallRuleService.getFwRuleByName(map);
		} catch (AppException e) {
			throw e;
		}
		return JSONObject.toJSONString(isTrue);

	}

	/**
	 * 更新防火墙规则
	 * 
	 * @author Chengxiaodong
	 * @param request
	 * @param fwr
	 * @return
	 * @throws AppException
	 */
	@RequestMapping(value = "/updateFwRule", method = RequestMethod.POST)
	@ResponseBody
	public String updateFwRule(HttpServletRequest request, @RequestBody CloudFwRule fwr) throws AppException {
		boolean isTrue = false;
		try {
			log.info("编辑防火墙规则开始");
			isTrue = fireWallRuleService.updateFwRule(fwr);
			logService.addLog("编辑防火墙规则", ConstantClazz.LOG_TYPE_FIRERULE, fwr.getFwrName(), fwr.getPrjId(),
					ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (AppException e) {
			logService.addLog("编辑防火墙规则", ConstantClazz.LOG_TYPE_FIRERULE, fwr.getFwrName(), fwr.getPrjId(),
					ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}
		return JSONObject.toJSONString(isTrue);

	}
	
	/**
	 * 防火墙规则禁用或启用
	 * @param fwr
	 * @return
	 * @throws AppException
	 */
	@RequestMapping(value = "/updateEnabled", method = RequestMethod.POST)
	@ResponseBody
	public String updateEnabled(@RequestBody CloudFwRule fwr) throws AppException{
		boolean isTrue = false;
		try {
			log.info("防火墙规则禁用或启用开始");
			isTrue = fireWallRuleService.updateIsEnabled(fwr);
			logService.addLog("防火墙规则禁用或启用", ConstantClazz.LOG_TYPE_FIRERULE, fwr.getFwrName(), fwr.getPrjId(),
					ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (AppException e) {
			logService.addLog("防火墙规则禁用或启用", ConstantClazz.LOG_TYPE_FIRERULE, fwr.getFwrName(), fwr.getPrjId(),
					ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}
		return JSONObject.toJSONString(isTrue);
	}
	/**
	 * 删除防火墙规则并解绑
	 * 
	 * @author Chengxiaodong
	 * @param request
	 * @param fw
	 * @return
	 */
	@RequestMapping(value = "/deleteFwRulePolicy", method = RequestMethod.POST)
	@ResponseBody
	public String deleteFwRulePolicy(HttpServletRequest request, @RequestBody CloudFwRule fwr) {
		boolean isTrue = false;
		try {
			log.info("防火墙规则解绑删除开始");
			isTrue = fireWallRuleService.deleteFwRuletoPolicy(fwr);
			logService.addLog("删除防火墙规则", ConstantClazz.LOG_TYPE_FIRERULE, fwr.getFwrName(), fwr.getPrjId(),
					ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (AppException e) {
			logService.addLog("删除防火墙规则", ConstantClazz.LOG_TYPE_FIRERULE, fwr.getFwrName(), fwr.getPrjId(),
					ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}

		return JSONObject.toJSONString(isTrue);

	}

}
