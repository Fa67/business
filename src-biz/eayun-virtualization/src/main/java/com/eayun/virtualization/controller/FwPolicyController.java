package com.eayun.virtualization.controller;

import java.util.HashMap;
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
import com.eayun.common.model.EayunResponseJson;
import com.eayun.log.service.LogService;
import com.eayun.virtualization.model.BaseCloudFwPolicy;
import com.eayun.virtualization.model.CloudFwPolicy;
import com.eayun.virtualization.service.FwPolicyService;

@Controller
@RequestMapping("/safety/fwPolicy")
@Scope("prototype")
public class FwPolicyController extends BaseController {
	private static final Logger log = LoggerFactory.getLogger(FwPolicyController.class);
	@Autowired
	private FwPolicyService fwpService;
	@Autowired
	private LogService logService;

	/**
	 * 查询防火墙策略列表
	 * 
	 * @author Chengxiaodong
	 * @param request
	 * @param datacenterId
	 * @param projectId
	 * @param name
	 * @param page
	 * @return
	 */

	@RequestMapping(value = "/getFwpList", method = RequestMethod.POST)
	@ResponseBody
	public String getFwpList(HttpServletRequest request, Page page, @RequestBody ParamsMap map) {
		try {
			log.info("查询防火墙策略列表开始");
			String prjId = map.getParams().get("prjId").toString();
			String dcId = map.getParams().get("dcId").toString();
			String fwpName = map.getParams().get("name").toString();
			int pageSize = map.getPageSize();
			int pageNumber = map.getPageNumber();

			QueryMap queryMap = new QueryMap();
			queryMap.setPageNum(pageNumber);
			queryMap.setCURRENT_ROWS_SIZE(pageSize);
			page = fwpService.getFwpList(page, prjId, dcId, fwpName, queryMap);
		} catch (AppException e) {
			throw e;
		}
		return JSONObject.toJSONString(page);

	}

	/**
	 * 查询prjId查询防火墙策略
	 * 
	 * @author Chengxiaodong
	 * @param request
	 * @param datacenterId
	 * @param projectId
	 * @param name
	 * @param page
	 * @return
	 */

	@RequestMapping(value = "/getFwpListByPrjId", method = RequestMethod.POST)
	@ResponseBody
	public String getFwpListByPrjId(HttpServletRequest request, Page page, @RequestBody Map map) {
		List<CloudFwPolicy> listFwp = null;
		try {
			log.info("查询防火墙列表开始");
			String prjId = map.get("prjId").toString();
			String dcId = map.get("dcId").toString();
			listFwp = fwpService.getFwpListByPrjId(dcId, prjId);
		} catch (AppException e) {
			throw e;
		}
		return JSONObject.toJSONString(listFwp);

	}

	/**
	 * 检查重名
	 * 
	 * @author Chengxiaodong
	 * @param request
	 * @param datacenterId
	 * @param projectId
	 * @param name
	 * @return
	 */

	@RequestMapping(value = "/getFwpByName", method = RequestMethod.POST)
	@ResponseBody
	public String getFwpByName(HttpServletRequest request, Page page, @RequestBody Map map) {
		boolean isTrue = false;
		try {
			log.info("验证防火墙策略重名开始");
			isTrue = fwpService.getFwpByName(map);
		} catch (AppException e) {
			throw e;
		}
		return JSONObject.toJSONString(isTrue);

	}

	/**
	 * 删除策略
	 * 
	 * @author Chengxiaodong
	 * @param request
	 * @param map
	 * @return
	 */
	@RequestMapping(value = "/deleteFwp", method = RequestMethod.POST)
	@ResponseBody
	public String deleteFwp(HttpServletRequest request, @RequestBody CloudFwPolicy fwp) {
		boolean isTrue = false;
		try {
			log.info("删除防火墙开始");
			isTrue = fwpService.deleteFwp(fwp);
			logService.addLog("删除防火墙策略", ConstantClazz.LOG_TYPE_FIREPOLICY, fwp.getFwpName(), fwp.getPrjId(),
					ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (AppException e) {
			logService.addLog("删除防火墙策略", ConstantClazz.LOG_TYPE_FIREPOLICY, fwp.getFwpName(), fwp.getPrjId(),
					ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}

		return JSONObject.toJSONString(isTrue);

	}

	/**
	 * 创建防火墙策略
	 * 
	 * @author Chengxiaodong
	 * @param request
	 * @param map
	 * @return
	 */
	@RequestMapping(value = "/addFwPolicy", method = RequestMethod.POST)
	@ResponseBody
	public String addFwPolicy(HttpServletRequest request, @RequestBody Map map) {
		BaseCloudFwPolicy fwp = null;
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession()
				.getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		String createName = sessionUser.getUserName();
		Map project = (Map) map.get("project");
		String dcId = project.get("dcId").toString();
		String prjId = project.get("projectId").toString();
		String fwpName = map.get("name").toString();
		try {
			log.info("创建防火墙规则开始");
			fwp = fwpService.addFwPolicy(dcId, prjId, createName, fwpName);
			logService.addLog("创建防火墙策略", ConstantClazz.LOG_TYPE_FIREPOLICY, fwpName, prjId,
					ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (AppException e) {
			logService.addLog("创建防火墙策略", ConstantClazz.LOG_TYPE_FIREPOLICY, fwpName, prjId,
					ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}
		return JSONObject.toJSONString(fwp);
	}

	/**
	 * 编辑防火墙策略
	 * 
	 * @author chengxiaodong
	 * @param request
	 * @param map
	 * @return
	 */
	@RequestMapping(value = "/updateFwPolicy", method = RequestMethod.POST)
	@ResponseBody
	public String updateFwPolicy(HttpServletRequest request, @RequestBody CloudFwPolicy fwp) throws AppException {
		boolean isTrue = false;
		try {
			log.info("编辑防火墙策略开始");
			isTrue = fwpService.updateFwPolicy(fwp);
			logService.addLog("编辑防火墙策略", ConstantClazz.LOG_TYPE_FIREPOLICY, fwp.getFwpName(), fwp.getPrjId(),
					ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (AppException e) {
			logService.addLog("编辑防火墙策略", ConstantClazz.LOG_TYPE_FIREPOLICY, fwp.getFwpName(), fwp.getPrjId(),
					ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}
		return JSONObject.toJSONString(isTrue);

	}

	/**
	 * 编辑防火墙策略规则
	 * 
	 * @author chengxiaodong
	 * @param request
	 * @param map
	 * @return
	 */
	@RequestMapping(value = "/toDoFwRule", method = RequestMethod.POST)
	@ResponseBody
	public String toDoFwRule(HttpServletRequest request, @RequestBody CloudFwPolicy fwp) throws AppException {
		boolean isTrue = false;
		try {
			log.info("管理防火墙策略规则");
			isTrue = fwpService.toDoFwRule(fwp);
			logService.addLog("编辑防火墙策略规则", ConstantClazz.LOG_TYPE_FIREPOLICY, fwp.getFwpName(), fwp.getPrjId(),
					ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (AppException e) {
			logService.addLog("编辑防火墙策略规则", ConstantClazz.LOG_TYPE_FIREPOLICY, fwp.getFwpName(), fwp.getPrjId(),
					ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}
		return JSONObject.toJSONString(isTrue);

	}

	/**
	 * 跟椐策略ID查詢规则
	 * @param fwpId
	 * @return
	 * @throws AppException
	 */
	@RequestMapping(value = "/getRuleByfwpId")
	@ResponseBody
	public String getRuleByfwpId(@RequestBody String fwpId)throws AppException{
		return JSONObject.toJSONString(fwpService.getRuleByFwpId(fwpId));
	}
	/**
     * 调整防火墙策略规则的优先级
     * @param request
     * @param fwp
     * @param local
     * @param target
     * @param reference
     * @return
     * @throws AppException
     */
    @RequestMapping(value= "/updateRuleSequence")
    @ResponseBody
    public Object updateFwR(HttpServletRequest request,@RequestBody Map<String, String> prames)throws AppException{
    	EayunResponseJson res = new EayunResponseJson();
    	log.info("调整防火墙策略规则的优先级");
    	boolean isTrue=false;
    	BaseCloudFwPolicy fwp = fwpService.getFwpById(prames.get("fwpId").toString());
    	try{
    		String local = prames.get("local").toString();
    		String target = prames.get("target").toString();
    		String reference = prames.get("reference").toString();
    		isTrue = fwpService.updateRuleSequence(fwp, local, target, reference);
    		res.setRespCode(ConstantClazz.SUCCESS_CODE);
   	 		res.setData(isTrue);
   	 		logService.addLog("调整优先级",ConstantClazz.LOG_TYPE_FIRERULE, prames.get("fwrName").toString(),fwp.getPrjId(), ConstantClazz.LOG_STATU_SUCCESS,null);
    	}catch(AppException e){
    		logService.addLog("调整优先级",ConstantClazz.LOG_TYPE_FIRERULE, prames.get("fwrName").toString(),fwp.getPrjId(), ConstantClazz.LOG_STATU_ERROR,e);
    		throw e;
    	}
    	return res;
    }
	
}
