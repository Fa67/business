package com.eayun.virtualization.controller;

import java.util.ArrayList;
import java.util.Date;
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
import com.eayun.log.service.LogService;
import com.eayun.notice.model.MessageExpireRenewResourcesModel;
import com.eayun.notice.model.MessageOrderResourceNotice;
import com.eayun.notice.model.MessagePayAsYouGoResourcesStopModel;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.virtualization.model.BaseCloudSecurityGroup;
import com.eayun.virtualization.model.CloudSecurityGroup;
import com.eayun.virtualization.model.CloudSecurityGroupRule;
import com.eayun.virtualization.service.SecurityGroupService;

@Controller
@RequestMapping("/cloud/securitygroup")
@Scope("prototype")
public class SecurityGroupController extends BaseController{
	private static final Logger log = LoggerFactory
			.getLogger(SecurityGroupController.class);

	@Autowired
	private LogService logService;
	
	@Autowired
	private SecurityGroupService securityGroupService;

	
	
	
	
	/**
     * 根据prjId、dcId、sgId获取Entity
     * @author liyanchao
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value= "/getGroup" , method = RequestMethod.POST)
    @ResponseBody
    public String getGroup(HttpServletRequest request,@RequestBody Map map){
    	BaseCloudSecurityGroup group=new BaseCloudSecurityGroup();
    	try{
    		String dcId="";
    		String prjId="";
    		String sgId="";
    		dcId=map.get("dcId").toString();
    		prjId=map.get("prjId").toString();
    		sgId=map.get("sgId").toString();
    		group=securityGroupService.getGroup(dcId,prjId,sgId);
    	
    	}catch(AppException e){
    		throw e;
    	}
		return JSONObject.toJSONString(group);
    	
    }
    /**
     * 验证重名 创建
     * @author liyanchao
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value= "/getGroupByName" , method = RequestMethod.POST)
    @ResponseBody
    public String getGroupById(HttpServletRequest request,@RequestBody CloudSecurityGroup cloudGroup){
		boolean isTrue=false;
    	try{
    		isTrue=securityGroupService.getGroupByName(cloudGroup.getPrjId(),cloudGroup.getSgId(),cloudGroup.getSgName());
    	}catch(AppException e){
    		throw e;
    	}
		return JSONObject.toJSONString(isTrue);
    	
    }
    /**
     * 验证重名 编辑
     * @author liyanchao
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value= "/getGroupById" , method = RequestMethod.POST)
    @ResponseBody
    public String getGroupById(HttpServletRequest request,@RequestBody Map map){
		boolean isTrue=false;
    	try{
    		Map project = (Map)map.get("project");
    		String dcId="";
    		String prjId="";
    		String sgName="";
    		String sgId="";
    		//用于创建时判断重名
    		if(null!=project){
    			dcId=project.get("dcId").toString();
    			prjId=project.get("projectId").toString();
        		sgName=map.get("name").toString();
    		}else{
    			//用于编辑时时判断重名
    			dcId=map.get("dcId").toString();
    			prjId=map.get("prjId").toString();
    			sgName=map.get("name").toString();
        		sgId=map.get("sgId").toString();
    		}
    		
    		isTrue=securityGroupService.getGroupById(prjId,sgId,sgName);
    	}catch(AppException e){
    		throw e;
    	}
		return JSONObject.toJSONString(isTrue);
    	
    }
	/**
	 * @param request
	 * @param Map
	 * @param page
	 * @return
	 */
	@RequestMapping(value = "/getSecurityGroupList", method = RequestMethod.POST)
	@ResponseBody
	public String getSecurityGroupList(HttpServletRequest request, Page page, @RequestBody ParamsMap map)
			throws Exception {
		String groupName="";
		try {
			 String prjId = map.getParams().get("prjId").toString();
			 String dcId=map.getParams().get("dcId").toString();
			 
			 if(map.getParams().containsKey("name")){
				 groupName=map.getParams().get("name").toString();
			 }
			int pageSize = map.getPageSize();
			int pageNumber = map.getPageNumber();

			QueryMap queryMap = new QueryMap();
			queryMap.setPageNum(pageNumber);
			queryMap.setCURRENT_ROWS_SIZE(pageSize);
			
			page = securityGroupService.getSecurityGroupList(page, prjId, dcId,
					groupName, queryMap);
			
		} catch (Exception e) {
			
			throw e;
		}
		return JSONObject.toJSONString(page);

	}

	/**
	 * @param request
	 * @param page
	 * @param Map
	 * @return 
	 */
	@RequestMapping(value = "/addSecurityGroup", method = RequestMethod.POST)
	@ResponseBody
	public String addSecurityGroup(HttpServletRequest request, Page page, @RequestBody Map map)
			throws Exception {
		BaseCloudSecurityGroup securityGroup = new BaseCloudSecurityGroup();
		String name = map.get("name").toString();
		String prjId = ((Map)map.get("project")).get("projectId").toString();
		log.info("创建安全组");
		try {
			securityGroup = securityGroupService.addSecurityGroup(request,map);
			logService.addLog("创建安全组", ConstantClazz.LOG_TYPE_GROUP, name, prjId,
					ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (Exception e) {
			logService.addLog("创建安全组", ConstantClazz.LOG_TYPE_GROUP, name, prjId,
					ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}
		

		return JSONObject.toJSONString(securityGroup);

	}
	/**
	 * @param request
	 * @param name
	 * @param dcId
	 * @param prjId
	 * @param page
	 * @param ParamsMap
	 * @return 
	 */
	@RequestMapping(value = "/updateSecurityGroup", method = RequestMethod.POST)
	@ResponseBody
	public String updateSecurityGroup(HttpServletRequest request, @RequestBody Map map)
			throws Exception {
		BaseCloudSecurityGroup securityGroup = null;
		String name = map.get("sgName").toString();
		String prjId = map.get("prjId").toString();
		log.info("更新安全组");
		try {
			//判断操作是否成功
			 securityGroup = securityGroupService.updateSecurityGroup(request, map);
			
			logService.addLog("更新安全组", ConstantClazz.LOG_TYPE_GROUP, name, prjId,
					ConstantClazz.LOG_STATU_SUCCESS, null);
		}catch (Exception e) {
			logService.addLog("更新安全组", ConstantClazz.LOG_TYPE_GROUP, name, prjId,
					ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}
		return JSONObject.toJSON(securityGroup).toString();
	}
	
	/**
	 * 删除安全组功能；
	 * @param request
	 * @param datacenterId
	 * @param projectId
	 * @param idStr
	 * @param delJson
	 * @return
	 * @throws AppException
	 */
	@RequestMapping(value = "/deleteGroup" , method = RequestMethod.POST)
	@ResponseBody
	public String  deleteGroup(HttpServletRequest request, @RequestBody Map map) throws Exception{
		boolean flag = false;
		String dcId=map.get("dcId").toString();
		String prjId = map.get("prjId").toString();
		String groupId = map.get("sgId").toString();
		String sgName = map.get("sgName").toString();
		log.info("删除安全组开始");
		try {
			flag=securityGroupService.deleteGroup(dcId,prjId,groupId);
			logService.addLog("删除安全组", ConstantClazz.LOG_TYPE_GROUP, sgName, prjId,
					ConstantClazz.LOG_STATU_SUCCESS, null);
		}catch (Exception e) {
			logService.addLog("删除安全组", ConstantClazz.LOG_TYPE_GROUP, sgName, prjId,
					ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}
		return JSONObject.toJSONString(flag);
	}
	/**
	 * 根据安全组ID查找安全组规则
	 * @param request
	 * @param datacenterId
	 * @param projectId
	 * @param securitygroupId
	 * @param reJson
	 * @return 返回数据库中指定安全组的所有规则
	 * @throws AppException
	 */
	@RequestMapping(value = "/getRules" , method = RequestMethod.POST)
	@ResponseBody
	public String getRules(HttpServletRequest request, Page page, @RequestBody ParamsMap map) throws Exception{
		List<CloudSecurityGroupRule> listGroupRule= null;
		try {
			String dcId=map.getParams().get("dcId").toString();
			String prjId = map.getParams().get("prjId").toString();
			String sgId=map.getParams().get("sgId").toString();
			int pageSize = map.getPageSize();
			int pageNumber = map.getPageNumber();

			QueryMap queryMap = new QueryMap();
			queryMap.setPageNum(pageNumber);
			queryMap.setCURRENT_ROWS_SIZE(5);
			
			page=securityGroupService.getRules(page, dcId, prjId, sgId, queryMap);
			
		}catch (Exception e) {
			
			throw e;
		}
		return JSONObject.toJSONString(page);
	}
	
	/**
	 * 查找当前项目下的所有安全组集合
	 * @param request
	 * @return
	 * @throws AppException
	 */
	@RequestMapping(value = "/getGroupsByProjectId" , method = RequestMethod.POST)
	@ResponseBody
	public Object getGroupsByProjectId(HttpServletRequest request,@RequestBody Map map) throws Exception{
		List<BaseCloudSecurityGroup> list = new ArrayList<BaseCloudSecurityGroup>();
		try {
			
			list = securityGroupService.getGroupsByProjectId(request,map);
			
		}catch (Exception e) {
			
			throw e;
		}
		return list;
	}
	
	
	
}
