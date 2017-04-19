package com.eayun.virtualization.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestBody;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.virtualization.model.BaseCloudSecurityGroup;

/**
 * SecurityGroupService
 * 
 * @Filename: SecurityGroupService.java
 * @Description:
 * @Version: 1.0
 * @Author: liyanchao
 * @Email: yanchao.li@eayun.com
 * @History:<br> <li>Date: 2015年10月16日</li> <li>Version: 1.0</li> <li>Content:
 *               create</li>
 * 
 */
public interface SecurityGroupService {
	/**
	 * 查询安全组列表
	 * 
	 * @param page
	 * @param prjId
	 * @param dcId
	 * @param queryMap
	 * @return
	 */
	public Page getSecurityGroupList(Page page, String prjId, String dcId,
			String name, QueryMap queryMap);

	/**
	 * 创建安全组
	 * 
	 * @param page
	 * @param prjId
	 * @param dcId
	 * @param queryMap
	 * @return BaseCloudSecurityGroup
	 */
	@SuppressWarnings("rawtypes")
    public BaseCloudSecurityGroup addSecurityGroup(HttpServletRequest request,
			@RequestBody Map map);

	@SuppressWarnings("rawtypes")
    public BaseCloudSecurityGroup updateSecurityGroup(
			HttpServletRequest request, @RequestBody Map map);

	// 删除安全组方法；
	public boolean deleteGroup(String dcId, String prjId, String groupId);

	/**
	 * 根据安全组id查寻其所有本地库的规则
	 * 
	 * @param dcId
	 * @param prjId
	 * @param groupId
	 * @return 返回指定安全组id的规则列表
	 */
	public Page getRules(Page page, String dcId, String prjId, String sgId,
			QueryMap queryMap);

	@SuppressWarnings("rawtypes")
    public List<BaseCloudSecurityGroup> getGroupsByProjectId(
			HttpServletRequest request, @RequestBody Map map);

	// 用于判断重名--创建时
	public boolean getGroupByName(String dcId, String sgId, String sgName);
	// 用于判断重名--编辑时
	public boolean getGroupById(String dcId, String sgId, String sgName);
	/*根据数据中心、项目、安全组Id获取实体*/
	public BaseCloudSecurityGroup getGroup(String dcId, String prjId, String sgId);
	/*
	 *根据prjId查询个数 
	 */
	public int getCountByPrjId(String prjId);
	
	
	
	/**
	 * 添加默认安全组
	 * */
	
	public void addDefault3389SecurityGroup(String prjId,String dcId)throws AppException;
	public void addDefault22SecurityGroup(String prjId,String dcId)throws AppException;
}
