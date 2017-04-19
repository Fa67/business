package com.eayun.virtualization.ecmccontroller;

import java.util.Map;

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
import com.eayun.eayunstack.model.Firewall;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.virtualization.ecmcservice.EcmcCloudFireWallService;
import com.eayun.virtualization.model.CloudFireWall;

/**
 * @author jingang.liu@eayun.com to beijing
 * @date 2016年4月12日
 */
@Controller
@RequestMapping("/ecmc/virtual/cloudfirewall")
public class EcmcCloudFireWallController {

	private static final Log log = LogFactory.getLog(EcmcCloudFireWallController.class);
	@Autowired
    private EcmcLogService logServer;
	@Autowired
	private EcmcCloudFireWallService firewallservice;
	
	/**
	 * 校验防火墙名称是否已经存在
	 * @param fwName
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/checkFirewallName")
	@ResponseBody
	public Object checkFirewallName(@RequestBody Map<String, String> map) throws AppException{
		EayunResponseJson res = new EayunResponseJson();
		boolean isExist = false;
		String fwName = map.get("fwName");
		String datacenterId = map.get("datacenterId");
		String projectId = map.get("projectId");
		String fwId = map.get("fwId");
		try {
			if(fwName!=null && !"".equals(fwName)){
				isExist = firewallservice.checkName(fwName,datacenterId,projectId,fwId);
				res.setRespCode(ConstantClazz.SUCCESS_CODE);
				res.setData(isExist);
			}
		}catch (AppException e) {
			throw e;
		}
		catch (Exception e) {
			log.error(e, e);
			throw new AppException("error.ecmc.cloud.cloudfirewall", e);
		}
		return res;
	}
	/**
	 * 防火墙列表展示
	 * @param mapparems
	 * @param page
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/list")
	@ResponseBody
	public Object list(@RequestBody ParamsMap mapparems,Page page) throws AppException{
		String prjName = mapparems.getParams().get("prjName") == null ? null : mapparems.getParams().get("prjName").toString();
		String name = mapparems.getParams().get("name") == null ? null : mapparems.getParams().get("name").toString();
		String datacenterId = mapparems.getParams().get("datacenterId") == null ? null : mapparems.getParams().get("datacenterId").toString();
		String cusOrg = mapparems.getParams().get("cusOrg") == null ? null : mapparems.getParams().get("cusOrg").toString();
		QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(mapparems.getPageNumber());
        queryMap.setCURRENT_ROWS_SIZE(mapparems.getPageSize());
		page = firewallservice.list(page, datacenterId, prjName, name,cusOrg, queryMap);
		return page;
	}
	/**
	 * 根据ID获取防火墙信息
	 * @param id
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/getById")
	@ResponseBody
	public Object getById(@RequestBody Map<String, String> map) throws AppException{
		EayunResponseJson res = new EayunResponseJson();
		try {
			res.setRespCode(ConstantClazz.SUCCESS_CODE);
			res.setData(firewallservice.getFWById(map.get("id")));
		} catch (AppException e) {
			res.setRespCode(ConstantClazz.ERROR_CODE);
			res.setMessage("ecmc.virtual.cloudfirewall:"+e.getMessage());
			throw e;
		}
		return res;
	}
	/**
	 * 根据ID获取防火墙信息包含客户名称，关联项目
	 * @param map
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/getFwByIdDetail")
	@ResponseBody
	public Object getFwByIdDetail(@RequestBody Map<String, String> map) throws AppException{
		EayunResponseJson res = new EayunResponseJson();
		try {
			res.setRespCode(ConstantClazz.SUCCESS_CODE);
			res.setData(firewallservice.getFwByIdDetail(map.get("id")));
		} catch (AppException e) {
			res.setRespCode(ConstantClazz.ERROR_CODE);
			res.setMessage("ecmc.virtual.cloudfirewall:"+e.getMessage());
			throw e;
		}
		return res;
	}
	/**
	 * 创建防火墙
	 * @param parmes
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/createFireWall")
	@ResponseBody
	public Object createFireWall(@RequestBody Map<String, String> parmes) throws AppException{
		EayunResponseJson res = new EayunResponseJson();
		Firewall fireWall = new Firewall();
		try {
			fireWall = firewallservice.create(parmes);
			res.setRespCode(ConstantClazz.SUCCESS_CODE);
			res.setData(fireWall);
			logServer.addLog("创建防火墙",ConstantClazz.LOG_TYPE_FIREWALL, parmes.get("name"),parmes.get("projectId"), 1,fireWall.getId(),null);
			
		} catch (AppException e) {
			logServer.addLog("创建防火墙",ConstantClazz.LOG_TYPE_FIREWALL, parmes.get("name"),parmes.get("projectId"), 0,fireWall.getId(),e);
			res.setRespCode(ConstantClazz.ERROR_CODE);
			res.setMessage("ecmc.virtual.cloudfirewall:"+e.getMessage());
			throw e;
		}
		return res;
	}
	
	/**
	 * 修改防火墙
	 * @param parmes
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/updateFireWall")
	@ResponseBody
	public Object updateFireWall(@RequestBody Map<String, String> parmes) throws AppException{
		EayunResponseJson res = new EayunResponseJson();
		try {
			res.setRespCode(ConstantClazz.SUCCESS_CODE);
			res.setData(firewallservice.update(parmes));
			logServer.addLog("修改防火墙",ConstantClazz.LOG_TYPE_FIREWALL, parmes.get("name"),parmes.get("projectId"), 1,parmes.get("id"),null);
		} catch (AppException e) {
			logServer.addLog("修改防火墙",ConstantClazz.LOG_TYPE_FIREWALL, parmes.get("name"),parmes.get("projectId"), 0,parmes.get("id"),e);
			res.setRespCode(ConstantClazz.ERROR_CODE);
			res.setMessage("ecmc.virtual.cloudfirewall:"+e.getMessage());
			throw e;
		}
		return res;
	}
	
	/**
	 * 删除防火墙
	 * @param parmes
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/deleteFireWall")
	@ResponseBody
	public Object deleteFireWall(@RequestBody Map<String, String> parmes) throws AppException{
		String datacenterId = parmes.get("datacenterId");
		String projectId = parmes.get("projectId");
		String id = parmes.get("id"); 
		EayunResponseJson res = new EayunResponseJson();
		CloudFireWall  fireWall = firewallservice.getFWById(id);
		try {
			res.setRespCode(ConstantClazz.SUCCESS_CODE);
			res.setData(firewallservice.delete(datacenterId,projectId,id));
			logServer.addLog("删除防火墙",ConstantClazz.LOG_TYPE_FIREWALL, fireWall.getFwName(),projectId, 1,id,null);
		} catch (AppException e) {
			logServer.addLog("删除防火墙",ConstantClazz.LOG_TYPE_FIREWALL, fireWall.getFwName(),projectId, 0,id,e);
			res.setRespCode(ConstantClazz.ERROR_CODE);
			res.setMessage("ecmc.virtual.cloudfirewall:"+e.getMessage());
			throw e;
		}
		return res;
	}
	/**
	 * 获取没有防火墙的项目
	 * @param request
	 * @param datacenterId
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/projects")
	@ResponseBody
	public Object projects(@RequestBody Map<String, String> map)throws AppException{
		EayunResponseJson res = new EayunResponseJson();
		try {
			res.setRespCode(ConstantClazz.SUCCESS_CODE);
			res.setData(firewallservice.projects(map.get("datacenterId")));
		} catch (AppException e) {
			res.setRespCode(ConstantClazz.ERROR_CODE);
			res.setMessage("ecmc.virtual.cloudfirewall:"+e.getMessage());
			throw e;
		}
		return res;
	}
	/**
	 * 创建防火墙
	 * 同时创建策略和规则
	 * @param parmes
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/createFwAndPoliyAndRule")
	@ResponseBody
	public Object createFPR(@RequestBody Map<String, String> parmes) throws AppException{
		EayunResponseJson res = new EayunResponseJson();
		Firewall fireWall = new Firewall();
		try {
			fireWall = firewallservice.createFwAndFwpAndRule(parmes);
			res.setRespCode(ConstantClazz.SUCCESS_CODE);
			res.setData(fireWall);
			logServer.addLog("创建防火墙",ConstantClazz.LOG_TYPE_FIREWALL, parmes.get("name"),parmes.get("projectId"), 1,fireWall.getId(),null);
			
		} catch (AppException e) {
			logServer.addLog("创建防火墙",ConstantClazz.LOG_TYPE_FIREWALL, parmes.get("name"),parmes.get("projectId"), 0,fireWall.getId(),e);
			res.setRespCode(ConstantClazz.ERROR_CODE);
			res.setMessage("ecmc.virtual.cloudfirewall:"+e.getMessage());
			throw e;
		}
		return res;
	}
	/**
	 * 删除防火墙
	 * 同时删除策略和规则
	 * @param parmes
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/deleteFwAndPoliyAndRule")
	@ResponseBody
	public Object deleteFPR(@RequestBody Map<String, String> parmes) throws AppException{
		String id = parmes.get("id"); 
		String projectId = parmes.get("projectId");
		EayunResponseJson res = new EayunResponseJson();
		CloudFireWall  fireWall = firewallservice.getFWById(id);
		try {
			res.setRespCode(ConstantClazz.SUCCESS_CODE);
			res.setData(firewallservice.deleteFwAndFwpAndRule(parmes));
			logServer.addLog("删除防火墙",ConstantClazz.LOG_TYPE_FIREWALL, fireWall.getFwName(),projectId, 1,id,null);
		} catch (AppException e) {
			logServer.addLog("删除防火墙",ConstantClazz.LOG_TYPE_FIREWALL, fireWall.getFwName(),projectId, 0,id,e);
			res.setRespCode(ConstantClazz.ERROR_CODE);
			res.setMessage("ecmc.virtual.cloudfirewall:"+e.getMessage());
			throw e;
		}
		return res;
	}
}
