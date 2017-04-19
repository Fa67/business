package com.eayun.virtualization.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MapUtils;
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
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.eayunstack.model.Rule;
import com.eayun.log.service.LogService;
import com.eayun.virtualization.model.CloudVm;
import com.eayun.virtualization.service.SecurityGroupRuleService;

@Controller
@RequestMapping("/cloud/grouprule")
@Scope("prototype")
public class SecurityGroupRuleController extends BaseController{
	private static final Logger log = LoggerFactory
			.getLogger(SecurityGroupRuleController.class);

	@Autowired
	private LogService logService;
	@Autowired
	private SecurityGroupRuleService securityGroupRuleService;
	/**
	 * 创建安全组
	 * @param request
	 * @param cloudHostVm
	 * @param page
	 * @param reJson
	 * @return
	 * @throws AppException
	 */
	@RequestMapping(value = "/addRule", method = RequestMethod.POST)
	@ResponseBody
	public String addRule(HttpServletRequest request, @RequestBody Map map) throws Exception{
		log.info("创建安全组规则");
		Rule resultData= null;
		
		boolean fag=true;
		String dcId=map.get("dcId").toString();
		String prjId=map.get("prjId").toString();
		String originalSgId=map.get("originalSgId").toString();
		try {
			//执行创建操作
			resultData=securityGroupRuleService.createRule(request,dcId, prjId,originalSgId, map);
			logService.addLog("创建安全组规则", ConstantClazz.LOG_TYPE_GROUPRULE, null, prjId,
					ConstantClazz.LOG_STATU_SUCCESS, null);
			
		}catch (Exception e) {
			 fag=false;
			logService.addLog("创建安全组规则", ConstantClazz.LOG_TYPE_GROUPRULE, null, prjId,
					ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}
		return JSONObject.toJSONString(fag);
	}
	/**
	 * 删除安全组规则
	 * @param request
	 * @param datacenterId
	 * @param projectId
	 * @param idStr
	 * @param delJson
	 * @return
	 * @throws AppException
	 */
	@RequestMapping(value = "/deleteGroupRule", method = RequestMethod.POST)
	@ResponseBody
	public String  deleteGroupRule(HttpServletRequest request,String datacenterId,String projectId,String id,@RequestBody Map map) throws Exception{
		boolean flag = false;
		String dcId=map.get("dcId").toString();
		String prjId = map.get("prjId").toString();
		String sgrId = map.get("sgrId").toString();
		log.info("删除安全组规则开始");
		try {
			flag = securityGroupRuleService.deleteGroupRule(dcId, sgrId);
			logService.addLog("删除安全组规则",ConstantClazz.LOG_TYPE_GROUPRULE,  null, prjId,
					ConstantClazz.LOG_STATU_SUCCESS, null);
		}catch (Exception e) {
			logService.addLog("删除安全组规则",ConstantClazz.LOG_TYPE_GROUPRULE,  null, prjId,
					ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}
		return JSONObject.toJSONString(flag);
	}
	
	/**
	 * 获取安全组下的云主机列表
	 * 
	 * 
	 * */
	@RequestMapping("/queryCloudHostList")
	@ResponseBody
	public Page getSecurityGroupCloudHost(@RequestBody Map<String, Object> Maprequest)throws AppException{
		
		QueryMap queryMap = new QueryMap();
		
		
		int pageNo=MapUtils.getIntValue(Maprequest, "pageNumber");
		queryMap.setPageNum(pageNo);
		queryMap.setCURRENT_ROWS_SIZE(5);
		Map<String,Object> map=(Map<String,Object>)Maprequest.get("params");
		String sgid=MapUtils.getString(map, "sgId");
		return securityGroupRuleService.querySecurityGroupCloudHostList(sgid, queryMap);
		
	}
	
	/**
	 * 获取当前项目下待添加的云主机
	 * */
	@RequestMapping("/getaddcloudhostlist")
	@ResponseBody
	public Object getAddSecurityGroupCloudHostList(@RequestBody Map<String, Object> Maprequest)throws AppException{
		//Map<String,Object> map=(Map<String,Object>)Maprequest.get("params");
	 	String sgid=MapUtils.getString(Maprequest, "sgId");
	 	String prjid=MapUtils.getString(Maprequest, "projectId");
		String sgname=MapUtils.getString(Maprequest, "sgname");
		//String cusorg=MapUtils.getString(Maprequest, "cusorg");
	 	EayunResponseJson resp=new EayunResponseJson();
	 	resp.setData(securityGroupRuleService.getaddSecurityGroupCloudHostList(sgid, prjid,sgname,null));
		
		return resp;
	}
	
	/**
	 * 添加云主机到安全组
	 * */
	@RequestMapping("/securitygroupaddcloudHost")
	@ResponseBody
	public Object securityGroupAddCloudHost(@RequestBody Map<String, Object> Maprequest)throws AppException{
		EayunResponseJson resp=new EayunResponseJson();
		List<CloudVm> cloudvm1=new ArrayList<CloudVm>();
		try { 	
		List map=(List) Maprequest.get("cloudhostlist");
		 	 Map<String,Object> data=null;
		 	List<CloudVm> cloudvm=new ArrayList<CloudVm>();
		 	String sgId=null;
			String sgname=null;
		 	 for(int i=0;i<map.size();i++){
		 		CloudVm vm=new CloudVm();
		 		  data=(Map<String, Object>) map.get(i);
		 		  sgId=data.get("sgid").toString();
		 		  sgname=data.get("sgname").toString();
		 		  vm.setPrjId(data.get("prjid").toString());
		 		  vm.setVmId(data.get("vmid").toString());
		 		 vm.setDcId(data.get("dcid").toString());
		 		 vm.setVmName(data.get("vmname").toString());
		 		 cloudvm.add(vm);
		 		cloudvm1.add(vm);
		 	 }
		 
		 		securityGroupRuleService.securityGroupsAddCloudHost(cloudvm, sgId, sgname);
		 		resp.setRespCode(ConstantClazz.SUCCESS_CODE);
		 		for(int j=0;j<cloudvm.size();j++){
		 			logService.addLog("加入安全组", ConstantClazz.LOG_TYPE_HOST, cloudvm.get(j).getVmName(), cloudvm.get(j).getPrjId(),
							ConstantClazz.LOG_STATU_SUCCESS, null);
		 		}
		 		
			} catch (Exception e) {
				for(int j=0;j<cloudvm1.size();j++){
		 			logService.addLog("加入安全组", ConstantClazz.LOG_TYPE_HOST, cloudvm1.get(j).getVmName(), cloudvm1.get(j).getPrjId(),
							ConstantClazz.LOG_STATU_ERROR, e);
		 		}
				log.error(e.toString(),e);
				resp.setRespCode(ConstantClazz.ERROR_CODE);
				throw e;
			}
		 	
		return resp;
		
	}
	
	
	/**
	 * 移除安全组的云主机
	 * */
	@RequestMapping("/securityGroupRemoveCloudHost")
	@ResponseBody
	public Object securityGroupRemoveCloudHost(@RequestBody Map<String, Object> Maprequest)throws AppException{
		EayunResponseJson resp=new EayunResponseJson();
		String sgid=MapUtils.getString(Maprequest, "sgId");
	 	String prjid=MapUtils.getString(Maprequest, "prjid");
		String sgname=MapUtils.getString(Maprequest, "sgname");
		String vmid=MapUtils.getString(Maprequest, "vmid");
		String dcid=MapUtils.getString(Maprequest, "dcid");
		String vmname=MapUtils.getString(Maprequest, "vmname");
		
		
	 	 Map<String,Object> data=null;
	 
	 	CloudVm vm=new CloudVm();
	 	vm.setDcId(dcid);
		vm.setVmId(vmid);
	 	vm.setPrjId(prjid);
	 	vm.setVmName(vmname);
	 
	 	try {
	 		securityGroupRuleService.securityGroupsRemoveCloudHost(vm, sgid, sgname);
	 		resp.setRespCode(ConstantClazz.SUCCESS_CODE);
	 		logService.addLog("移除安全组", ConstantClazz.LOG_TYPE_HOST, vm.getVmName(), vm.getPrjId(),
					ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (Exception e) {
			logService.addLog("移除安全组", ConstantClazz.LOG_TYPE_HOST, vm.getVmName(), vm.getPrjId(),
					ConstantClazz.LOG_STATU_ERROR, e);
			log.error(e.toString(),e);
			resp.setRespCode(ConstantClazz.ERROR_CODE);
			throw e;
		}
		return resp;
		
	}
}
