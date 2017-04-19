package com.eayun.eayunstack.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.datacenter.model.DcDataCenter;
import com.eayun.eayunstack.model.Role;
import com.eayun.eayunstack.model.Tenant;
import com.eayun.eayunstack.model.User;
import com.eayun.virtualization.model.BaseCloudProject;

public interface OpenstackTenantService {
	
	public List<Tenant> list(String datacenterId, String projectId)
			throws AppException;
	
	public List<Tenant> listAll(String datacenterId) throws AppException;
	
	public boolean checkLinked(DcDataCenter datacenter) throws AppException;
	
	public boolean checkLinked(String datacenterId) throws AppException;
	
	public Tenant create(String datacenterId, String projectId, JSONObject data)
			throws AppException;
	
	public JSONObject updateRoleByTenant(DcDataCenter datacenter,
			String projectId) throws AppException;
	
	public Tenant edit(String datacenterId, String projectId, String id,
			JSONObject data) throws AppException;
	
	public JSONObject editQuota(String datacenterId, String projectId,
			String id, JSONObject data) throws AppException;
	
	public JSONObject editQuotaVolumes(String datacenterId, String projectId,
			String id, JSONObject data) throws AppException;
	
	public JSONObject editQuotaNetwork(String datacenterId, String projectId,
			String id, JSONObject data) throws AppException;
	
	public JSONObject getQuota(String datacenterId, String projectId, String id)
			throws AppException;
	
	public JSONObject getQuotaVm(String datacenterId, String projectId)
			throws AppException;
	
	public JSONObject getNetworkQuota(String datacenterId, String projectId)
			throws AppException;
	
	public JSONObject getSpecifyQuota(String datacenterId, String projectId,
			String dataString) throws AppException;
	
	public JSONObject getSpecifyVolumev2(String datacenterId, String projectId,
			String dataString) throws AppException ;
	
	public boolean delete(String datacenterId, String projectId, String id)
			throws AppException;
	
	/**                                                        
	 * 查询底层 数据中心的云资源                             
	 * -------------------                                   
	 * @author zhouhaitao                                    
	 * @param dataCenter                                     
	 * @return                                               
	 */                                                      
	public List<BaseCloudProject> getStackList (BaseDcDataCenter dataCenter);
	
	public Role getRole(BaseDcDataCenter dataCenter,String name) throws Exception;
	
	public User getUserByName (BaseDcDataCenter dataCenter,String name) throws Exception;
	
	/**
	 * 将同步的项目数据赋予当前数据中心所对应的管理者的"admin"权限
	 * 
	 * @param role
	 * @param user
	 * @param projectId
	 * @return
	 * @throws AppException
	 */
	public JSONObject bindAdminToDcmanager(BaseDcDataCenter dataCenter,Role role, User user,
			String projectId) throws Exception ;


	/**
	 * @author zhouhaitao<br>
	 * @since RDS v1.0
	 * ---------------------------------<br>
	 * @desc
	 * 修改RDS相关的项目配额（instances  backups   volumes）
	 * 
	 * @param dcId		数据中心ID
	 * @param prjId		所修改的项目ID
	 * @param data		具体参数
	 * 
	 * @return			返回修改后的结果
	 */
	public JSONObject editTroveQuota(String dcId,String prjId,JSONObject data);
}
