package com.eayun.virtualization.ecmccontroller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.eayunstack.model.Rule;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.virtualization.ecmcservice.EcmcCloudSecurityGroupRuleService;
import com.eayun.virtualization.ecmcservice.EcmcCloudSecurityGroupService;
import com.eayun.virtualization.model.BaseCloudSecurityGroupRule;
import com.eayun.virtualization.model.CloudVm;

/**
 * 
 * @Author yangfangjun
 * @Date 2016年4月12日
 */
@Controller
@RequestMapping("/ecmc/virtual/securitygrouprule")
@Scope("prototype")
public class EcmcSecurityGroupRuleController {

	private final static Logger log = LoggerFactory.getLogger(EcmcSecurityGroupRuleController.class);
	@Autowired
    private EcmcLogService logServer;
	@Autowired
	private EcmcCloudSecurityGroupRuleService cloudSecurityGroupRuleService;
	
	@Autowired
	private EcmcCloudSecurityGroupService ecmcCloudSecurityGroupService;
	
	/**
	 * 删除单个安全规则
	 * 
	 * @param params
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/deleterulebyid")
	@ResponseBody
	public Object deleteRuleById(@RequestBody Map<String, String> params) throws AppException {
		EayunResponseJson delJson = new EayunResponseJson();
		BaseCloudSecurityGroupRule  rule =cloudSecurityGroupRuleService.getBaseGroupRuleBySgrId(params.get("id"));
		Map map=ecmcCloudSecurityGroupService.getBaseCloudSecurityGroupById(rule.getSgId());
		try {
			if (cloudSecurityGroupRuleService.deleteSecurityGroupRule(params.get("datacenterId"), params.get("id"))) {
				logServer.addLog("删除安全组规则",ConstantClazz.LOG_TYPE_GROUPRULE, map.get("sgName").toString(),rule.getPrjId(), 1,rule.getSgrId(),null);
				// 标记为删除成功
				delJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			}
		} catch (AppException e) {
			logServer.addLog("删除安全组规则",ConstantClazz.LOG_TYPE_GROUPRULE, map.get("sgName").toString(),rule.getPrjId(), 0,null,e);
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			logServer.addLog("删除安全组规则",ConstantClazz.LOG_TYPE_GROUPRULE, map.get("sgName").toString(),rule.getPrjId(), 0,null,e);
			throw new AppException("error.globe.system", e);
		}
		return delJson;
	}

	/**
	 * 列表功能
	 * 
	 * @param params
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/getstacksecuritygroupsbyprojectid")
	@ResponseBody
	public Object getStackSecurityGroupsByProjectId(@RequestBody Map<String, String> params) throws AppException {
		return cloudSecurityGroupRuleService.findStackSecurityGroupsListByProjectId(params.get("datacenterId"),
				params.get("projectId"));
	}

	/**
	 * 查找当前项目下的所有安全组集合
	 * 
	 * @param params
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/getcloudsecuritygroupsbyprojectid")
	@ResponseBody
	public Object getCloudSecurityGroupsByProjectId(@RequestBody Map<String, String> params) throws AppException {
		try {
			return cloudSecurityGroupRuleService.getCloudSecurityGroupsByProjectId(params.get("datacenterId"),
					params.get("projectId"));
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new AppException("error.globe.system", e);
		}
	}

	/**
	 * 创建安全组
	 * @param params
	 * @param reJson
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/createrule")
	@ResponseBody
	public Object createRule(@RequestBody Map<String, String> params,EayunResponseJson reJson) throws AppException{
		Rule resultData = new Rule();
		Map map=ecmcCloudSecurityGroupService.getBaseCloudSecurityGroupById(params.get("securityGroupId"));
		try {
			//执行创建操作
			resultData=cloudSecurityGroupRuleService.createRule(params);
			if(resultData==null){
				reJson.setRespCode(ConstantClazz.ERROR_CODE);
				reJson.setData(resultData);
			}else{
				//设置成功状态码	
			
				logServer.addLog("创建安全组规则",ConstantClazz.LOG_TYPE_GROUPRULE, map.get("sgName").toString(),params.get("projectId"), 1,resultData.getId(),null);
				reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
				//设置返回数据
				reJson.setData(resultData);
			}
		} catch (AppException e) {
			//logServer.addLog("创建安全组规则",ConstantClazz.LOG_TYPE_GROUPRULE, null,params.get("projectId"), 1,resultData.getId(),e);
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			if(resultData!=null){
			    logServer.addLog("创建安全组规则",ConstantClazz.LOG_TYPE_GROUPRULE, map.get("sgName").toString(),params.get("projectId"), 1,resultData.getId(),e);
			}
			throw new AppException("error.openstack.message", e);
		}
		return reJson;
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
		return cloudSecurityGroupRuleService.querySecurityGroupCloudHostList(sgid, queryMap);
		
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
		String cusorg=MapUtils.getString(Maprequest, "cusorg");
	 	EayunResponseJson resp=new EayunResponseJson();
	 	resp.setData(cloudSecurityGroupRuleService.getaddSecurityGroupCloudHostList(sgid, prjid,sgname,cusorg));
		
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
		 	 
		 
		 		
		 		
		 		 cloudSecurityGroupRuleService.securityGroupsAddCloudHost(cloudvm, sgId, sgname);
		 		resp.setRespCode(ConstantClazz.SUCCESS_CODE);
		 		for(int j=0;j<cloudvm.size();j++){
		 			
		 			logServer.addLog("加入安全组", ConstantClazz.LOG_TYPE_HOST, cloudvm.get(j).getVmName(), cloudvm.get(j).getPrjId(), 1, cloudvm.get(j).getVmId(), null);
		 		}
		 		
			} catch (Exception e) {
				for(int j=0;j<cloudvm1.size();j++){
					logServer.addLog("加入安全组", ConstantClazz.LOG_TYPE_HOST, cloudvm1.get(j).getVmName(), cloudvm1.get(j).getPrjId(), 1, cloudvm1.get(j).getVmId(), e);
		 		}
				log.error(e.toString(),e);
				resp.setRespCode(ConstantClazz.ERROR_CODE);
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
	 		 cloudSecurityGroupRuleService.securityGroupsRemoveCloudHost(vm, sgid, sgname);
	 		resp.setRespCode(ConstantClazz.SUCCESS_CODE);
	 		logServer.addLog("移除安全组", ConstantClazz.LOG_TYPE_HOST,vm.getVmName(), vm.getPrjId(), 1, vm.getVmId(), null);
	 		
		} catch (Exception e) {
		    log.error(e.toString(),e);
			resp.setRespCode(ConstantClazz.ERROR_CODE);
			logServer.addLog("移除安全组", ConstantClazz.LOG_TYPE_HOST,vm.getVmName(), vm.getPrjId(), 1, vm.getVmId(), e);
		}
		return resp;
		
	}
}
