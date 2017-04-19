package com.eayun.virtualization.controller;

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
import com.eayun.virtualization.model.CloudFireWall;
import com.eayun.virtualization.service.FireWallService;

@Controller
@RequestMapping("/safety/firewall")
@Scope("prototype")
public class FireWallController extends BaseController {
	private static final Logger log = LoggerFactory.getLogger(FireWallController.class);
	@Autowired
	private FireWallService fireWallService;
	@Autowired
	private LogService logService;

	/**
	 * 查询防火墙列表
	 * 
	 * @author Chengxiaodong
	 * @param request
	 * @param datacenterId
	 * @param projectId
	 * @param name
	 * @param page
	 * @return
	 */

	@RequestMapping(value = "/getFireWallList", method = RequestMethod.POST)
	@ResponseBody
	public String getFireWallList(HttpServletRequest request, Page page, @RequestBody ParamsMap map) {
		try {
			log.info("查询防火墙列表开始");
			String prjId = map.getParams().get("prjId").toString();
			String dcId = map.getParams().get("dcId").toString();
			String fireName = map.getParams().get("name").toString();
			int pageSize = map.getPageSize();
			int pageNumber = map.getPageNumber();

			QueryMap queryMap = new QueryMap();
			queryMap.setPageNum(pageNumber);
			queryMap.setCURRENT_ROWS_SIZE(pageSize);
			page = fireWallService.getFireWallList(page, prjId, dcId, fireName, queryMap);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return JSONObject.toJSONString(page);

	}

	/**
	 * 创建防火墙
	 * 
	 * @author Chengxiaodong
	 * @param request
	 * @param map
	 * @return
	 */
	@RequestMapping(value = "/addFireWall", method = RequestMethod.POST)
	@ResponseBody
	public String addFireWall(HttpServletRequest request, @RequestBody Map map) {
		BaseCloudFireWall fireWall = null;

		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession()
				.getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		String createName = sessionUser.getUserName();
		Map project = (Map) map.get("project");
		String dcId = project.get("dcId").toString();
		String prjId = project.get("projectId").toString();
		String fireWallName = map.get("name").toString();
		String policyId = map.get("fwpId").toString();
		try {
			log.info("创建防火墙开始");
			fireWall = fireWallService.addFireWall(dcId, prjId, createName, fireWallName, policyId);
			logService.addLog("创建防火墙", ConstantClazz.LOG_TYPE_FIREWALL, fireWallName, prjId,
					ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (AppException e) {
			logService.addLog("创建防火墙", ConstantClazz.LOG_TYPE_FIREWALL, fireWallName, prjId,
					ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}
		return JSONObject.toJSONString(fireWall);
	}

	/**
	 * 验证重名
	 * 
	 * @author Chengxiaodong
	 * @param request
	 * @param map
	 * @return
	 */
	@RequestMapping(value = "/getFireWallByName", method = RequestMethod.POST)
	@ResponseBody
	public String getFireWallByName(HttpServletRequest request, @RequestBody Map map) {
		boolean isTrue = false;
		try {
			log.info("验证防火墙重名开始");
			isTrue = fireWallService.getFireWallByName(map);
		} catch (AppException e) {
			throw e;
		}
		return JSONObject.toJSONString(isTrue);

	}

	/**
	 * 删除防火墙
	 * 
	 * @author Chengxiaodong
	 * @param request
	 * @param map
	 * @return
	 */
	@RequestMapping(value = "/deleteFireWall", method = RequestMethod.POST)
	@ResponseBody
	public String deleteFireWall(HttpServletRequest request, @RequestBody CloudFireWall fw) {
		boolean isTrue = false;
		try {
			log.info("删除防火墙开始");
			isTrue = fireWallService.deleteFireWall(fw);
			logService.addLog("删除防火墙", ConstantClazz.LOG_TYPE_FIREWALL, fw.getFwName(), fw.getPrjId(),
					ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (AppException e) {
			logService.addLog("删除防火墙", ConstantClazz.LOG_TYPE_FIREWALL, fw.getFwName(), fw.getPrjId(),
					ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}

		return JSONObject.toJSONString(isTrue);

	}

	/**
	 * 编辑防火墙
	 * 
	 * @author chengxiaodong
	 * @param request
	 * @param map
	 * @return
	 */
	@RequestMapping(value = "/updateFireWall", method = RequestMethod.POST)
	@ResponseBody
	public String updateFireWall(HttpServletRequest request, @RequestBody CloudFireWall fw) throws AppException {
		boolean isTrue = false;
		try {
			log.info("编辑防火墙开始");
			isTrue = fireWallService.updateFireWall(fw);
			logService.addLog("编辑防火墙", ConstantClazz.LOG_TYPE_FIREWALL, fw.getFwName(), fw.getPrjId(),
					ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (AppException e) {
			logService.addLog("编辑防火墙", ConstantClazz.LOG_TYPE_FIREWALL, fw.getFwName(), fw.getPrjId(),
					ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}
		return JSONObject.toJSONString(isTrue);

	}

	/**
	 * 创建防火墙及策略和规则
	 * 
	 * @param request
	 * @param map
	 * @return
	 * @throws AppException
	 */
	@RequestMapping(value = "/createFireWall")
	@ResponseBody
	public String createFwAndFwpAndFwr(HttpServletRequest request, @RequestBody Map<String, Object> map)
			throws AppException {
		BaseCloudFireWall fireWall = null;
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession()
				.getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		String createName = "";
		if (sessionUser != null && sessionUser.getUserName() != null) {
			createName = sessionUser.getUserName();
		} else {
			return JSONObject.toJSONString("登录过期!");
		}
		Map<String, String> project = (Map<String, String>)map.get("project");
		try {
			log.info("创建防火墙及策略和规则开始");
			fireWall = fireWallService.createFwAndFwpAndFwR(map, createName);
			logService.addLog("创建防火墙", ConstantClazz.LOG_TYPE_FIREWALL, map.get("name").toString(), project.get("projectId").toString(),
					ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (Exception e) {
			logService.addLog("创建防火墙", ConstantClazz.LOG_TYPE_FIREWALL, map.get("name").toString(), project.get("projectId").toString(),
					ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}
		return JSONObject.toJSONString(fireWall);
	}
	/**
	 * 删除防火墙同时删除策略和规则
	 * @param map {fwName,fwId,fwpId,dcId,prjId}
	 * @return
	 * @throws AppException
	 */
	@RequestMapping(value = "/deleteFwAndFwpAndFwr")
	@ResponseBody
	public String deleteFwAndFwpAndFwr( @RequestBody Map<String, String> map) throws AppException{
		boolean isTrue = false;
		try {
			log.info("删除防火墙开始");
			isTrue = fireWallService.deleteFwAndFwpAndFwr(map);
			logService.addLog("删除防火墙", ConstantClazz.LOG_TYPE_FIREWALL, map.get("fwName"), map.get("prjId"),
					ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (AppException e) {
			logService.addLog("删除防火墙", ConstantClazz.LOG_TYPE_FIREWALL, map.get("fwName"), map.get("prjId"),
					ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}
		return JSONObject.toJSONString(isTrue);
	}
	/**
	 * 根据防火墙ID 获取防火墙
	 * @param fwId
	 * @return
	 * @throws AppException
	 */
	@RequestMapping(value = "/queryFwById")
	@ResponseBody
	public String queryFwById(@RequestBody String fwId) throws AppException{
		return JSONObject.toJSONString(fireWallService.getFwById(fwId));
	}
	/**
	 * 编辑防火墙名称或者描述
	 * @param fw
	 * @return
	 * @throws AppException
	 */
	@RequestMapping(value = "/editFwNameorDesc")
	@ResponseBody
	public String updateFwNameorDesc(@RequestBody CloudFireWall fw) throws AppException{
		boolean isTrue = false;
		try {
			log.info("编辑防火墙名称或者描述");
			isTrue = fireWallService.updateFwNameorDesc(fw);
			logService.addLog("编辑防火墙", ConstantClazz.LOG_TYPE_FIREWALL, fw.getFwName(), fw.getPrjId(),
					ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (AppException e) {
			logService.addLog("编辑防火墙", ConstantClazz.LOG_TYPE_FIREWALL, fw.getFwName(), fw.getPrjId(),
					ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}
		return JSONObject.toJSONString(isTrue);
	}
	
}
