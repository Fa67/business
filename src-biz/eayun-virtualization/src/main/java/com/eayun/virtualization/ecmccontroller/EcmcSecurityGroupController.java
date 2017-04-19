package com.eayun.virtualization.ecmccontroller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.BeanUtils;
import com.eayun.datacenter.service.DataCenterService;
import com.eayun.eayunstack.model.Tenant;
import com.eayun.eayunstack.service.OpenstackSecurityGroupService;
import com.eayun.eayunstack.service.OpenstackTenantService;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.virtualization.ecmcservice.EcmcCloudSecurityGroupService;
import com.eayun.virtualization.model.BaseCloudSecurityGroup;
import com.eayun.virtualization.model.BaseCloudVm;
import com.eayun.virtualization.model.BaseCloudVmSgroup;
import com.eayun.virtualization.model.CloudSecurityGroup;

/**
 * 
 * @Author yangfangjun
 * @Date 2016年4月12日
 */
@Controller
@RequestMapping("/ecmc/virtual/securitygroup")
@Scope("prototype")
public class EcmcSecurityGroupController {

	private final static Logger log = LoggerFactory.getLogger(EcmcSecurityGroupController.class);

	@Autowired
	private EcmcCloudSecurityGroupService ecmcCloudSecurityGroupService;
	
	@Autowired
    private EcmcLogService logServer;



	/**
	 * 用于校验安全组名称是否已经存在
	 * 
	 * @param request
	 * @param page
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/checksecuritygroupname")
	@ResponseBody
	public boolean checkSecurityGroupName(@RequestBody Map<String, String> params) throws AppException {
		log.info("校验安全组名称开始");
		return ecmcCloudSecurityGroupService.checkSecurityGroupName(params.get("dcId"), params.get("prjId"),
				params.get("sgName"), params.get("sgId"));
	}

	/**
	 * 创建安全组
	 * 
	 * @param baseCloudSecurityGroup
	 * @return
	 * @throws Exception 
	 * @throws AppException
	 */
	@RequestMapping("/createsecuritygroup")
	@ResponseBody
	public Object createSecurityGroup(@RequestBody CloudSecurityGroup baseCloudSecurityGroup) throws Exception {
		log.info("创建安全组开始");
		// 设置创建人
		baseCloudSecurityGroup.setCreateName(EcmcSessionUtil.getUser().getAccount());
		EayunResponseJson respJson = new EayunResponseJson();
		BaseCloudSecurityGroup securityGroup = new BaseCloudSecurityGroup();
		try {
			Object result = ecmcCloudSecurityGroupService.addSecurityGroup(baseCloudSecurityGroup.getDcId(), baseCloudSecurityGroup.getPrjId(), baseCloudSecurityGroup.getSgName(), baseCloudSecurityGroup.getSgDescription(), baseCloudSecurityGroup.getCreateName());
			if(result != null){
				BeanUtils.copyProperties(securityGroup, result);
				respJson.setRespCode(ConstantClazz.SUCCESS_CODE);
				respJson.setData(result);
				logServer.addLog("创建安全组",ConstantClazz.LOG_TYPE_GROUP, baseCloudSecurityGroup.getSgName(),securityGroup.getPrjId(), 1,securityGroup.getSgId(),null);
			}
		}catch(AppException e){
		    log.error(e.getMessage(),e);
		    respJson.setRespCode(ConstantClazz.ERROR_CODE);
			logServer.addLog("创建安全组",ConstantClazz.LOG_TYPE_GROUP, baseCloudSecurityGroup.getSgName(),baseCloudSecurityGroup.getPrjId(), 0,null,e);
			throw e;
			
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
		    respJson.setRespCode(ConstantClazz.ERROR_CODE);
			logServer.addLog("创建安全组",ConstantClazz.LOG_TYPE_GROUP, baseCloudSecurityGroup.getSgName(),baseCloudSecurityGroup.getPrjId(), 0,null,e);
			throw e;
		}
		
		return respJson;
	}

	/**
	 * 查询安全组
	 * 
	 * @param paramsMap
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/querysecuritygroup")
	@ResponseBody
	public Object querySecurityGroup(@RequestBody ParamsMap paramsMap) throws AppException {
		log.info("查询安全组开始");
		QueryMap queryMap = new QueryMap();
		queryMap.setPageNum(paramsMap.getPageNumber());
		queryMap.setCURRENT_ROWS_SIZE(paramsMap.getPageSize());
		
		
		
		return ecmcCloudSecurityGroupService.getSecurityGroupList((String) paramsMap.getParams().get("dcId"),
				(String) paramsMap.getParams().get("prjName"), (String) paramsMap.getParams().get("cusOrg"), (String) paramsMap.getParams().get("name"), queryMap);
	}

	/**
	 * 未关联云主机的安全组列表
	 * 
	 * @param request
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/listallgroups")
	@ResponseBody
	public Object listAllGroups(@RequestBody Map<String, String> params) throws AppException {
		return ecmcCloudSecurityGroupService.listAllGroups(params.get("datacenterId"), params.get("projectId"));
	}

	/**
	 * 删除安全组
	 * 
	 * @param params
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/deletesecuritygroup")
	@ResponseBody
	public Object deleteSecurityGroup(@RequestBody Map<String, String> params) throws AppException {
		log.info("删除安全组开始");
		EayunResponseJson delJson = new EayunResponseJson();
		BaseCloudSecurityGroup  baseGroup = ecmcCloudSecurityGroupService.getGroupBySgId(params.get("id"));
	List<BaseCloudVmSgroup>  vmList= ecmcCloudSecurityGroupService.getVmByPrjId(baseGroup.getSgId());
	if(vmList.size()>0){
		delJson.setRespCode(ConstantClazz.ERROR_CODE);
		delJson.setMessage("无法删除，请移除安全组下的云主机后操作");
		return delJson;
	}
		
		try {
			if (ecmcCloudSecurityGroupService.deleteSecurityGroup(params.get("datacenterId"), params.get("id"))) {
				// 标记为删除成功
				delJson.setRespCode(ConstantClazz.SUCCESS_CODE);
				logServer.addLog("删除安全组",ConstantClazz.LOG_TYPE_GROUP, baseGroup.getSgName(),baseGroup.getPrjId(), 1,baseGroup.getSgId(),null);
			}
		} catch (AppException e) {
		    log.error(e.getMessage(),e);
		    logServer.addLog("删除安全组",ConstantClazz.LOG_TYPE_GROUP, baseGroup.getSgName(),baseGroup.getPrjId(), 0,baseGroup.getSgId(),e);
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			logServer.addLog("删除安全组",ConstantClazz.LOG_TYPE_GROUP, baseGroup.getSgName(),baseGroup.getPrjId(), 0,baseGroup.getSgId(),e);
			throw new AppException("error.globe.system", e);
		}
		return delJson;
	}
	
	/**
	 * 编辑安全组名称
	 * 
	 * @param request
	 * @param datacenterId
	 * @param projectId
	 * @param id
	 * @param name
	 * @param description
	 * @param reJson
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/updatesecuritygroup")
	@ResponseBody
	public Object updateSecurityGroup(@RequestBody CloudSecurityGroup cloudSecurityGroup) throws AppException {
		log.info("修改安全组开始");
		EayunResponseJson reJson = new EayunResponseJson();
		BaseCloudSecurityGroup  baseGroup = ecmcCloudSecurityGroupService.getGroupBySgId(cloudSecurityGroup.getSgId());
		try {
			// 判断操作是否成功
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			reJson.setData(ecmcCloudSecurityGroupService.updateSecurityGroup(cloudSecurityGroup));
			logServer.addLog("修改安全组",ConstantClazz.LOG_TYPE_GROUP, baseGroup.getSgName(),baseGroup.getPrjId(), 1,baseGroup.getSgId(),null);
			return reJson;
		} catch (AppException e) {
		    log.error(e.getMessage(),e);
		    logServer.addLog("修改安全组",ConstantClazz.LOG_TYPE_GROUP, baseGroup.getSgName(),baseGroup.getPrjId(), 0,baseGroup.getSgId(),e);
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			logServer.addLog("修改安全组",ConstantClazz.LOG_TYPE_GROUP, baseGroup.getSgName(),baseGroup.getPrjId(), 0,baseGroup.getSgId(),e);
			throw new AppException("error.globe.system", e);
		}
	}

	/**
	 * 根据安全组ID查找本地安全组
	 * 
	 * @param params
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/getsecuritygroupbyid")
	@ResponseBody
	public Object getSecurityGroupById(@RequestBody Map<String, String> params) throws AppException {
		log.info("获取安全组详情开始");
		try {
			return ecmcCloudSecurityGroupService.getBaseCloudSecurityGroupById(params.get("sgId"));
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new AppException("error.openstack.message", e);
		}
	}

	/**
	 * 根据安全组ID查找安全组规则
	 * @param paramsMap
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/getrulesbysgid")
	@ResponseBody
	public Object getRulesBySgId(@RequestBody ParamsMap paramsMap) throws AppException {
		log.info("查询安全组规则开始");
		try {
			Map<String, Object> params = paramsMap.getParams();
			QueryMap queryMap = new QueryMap();
			queryMap.setPageNum(paramsMap.getPageNumber());
			queryMap.setCURRENT_ROWS_SIZE(5);
			return ecmcCloudSecurityGroupService.getSecurityGroupRulesBySgId((String)params.get("dcId"), (String)params.get("prjId"), (String)params.get("sgId"), queryMap);
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new AppException("error.openstack.message", e);
		}
	}
	

	/**
	 * 修改线上默认安全组
	 * */
	
	
	@RequestMapping("/updateecscsecuritygroup")
	@ResponseBody
	public void updateEcscSecurityGroup(HttpServletResponse resp){
		Object obj=ecmcCloudSecurityGroupService.updateEcscSecurityGroup();
		
		try {
			
			//resp.getOutputStream();
			 String data = obj.toString();
			 
			OutputStream outputStream = resp.getOutputStream();//获取OutputStream输出流
			resp.setHeader("content-type", "text/html;charset=UTF-8");//通过设置响应头控制浏览器以UTF-8的编码显示数据
			 byte[] dataByteArr = data.getBytes("UTF-8");//将字符转换成字节数组，指定以UTF-8编码进行转换
			 outputStream.write(dataByteArr);//使用OutputStream流向客户端输出字节数组
		} catch (IOException e) {
		    log.error(e.getMessage(),e);
		}
		//return ecmcCloudSecurityGroupService;
}
	
}