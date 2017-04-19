package com.eayun.eayunstack.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.datacenter.model.DcDataCenter;
import com.eayun.eayunstack.model.EayunStackRole;
import com.eayun.eayunstack.model.EayunStackUser;
import com.eayun.eayunstack.model.Role;
import com.eayun.eayunstack.model.Tenant;
import com.eayun.eayunstack.model.User;
import com.eayun.eayunstack.service.OpenstackTenantService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
import com.eayun.virtualization.model.BaseCloudProject;

@Service
public class OpenstackTenantServiceImpl extends
		OpenstackBaseServiceImpl<Tenant> implements OpenstackTenantService {
	private static final Logger log = LoggerFactory
			.getLogger(OpenstackTenantServiceImpl.class);

	public List<Tenant> list(String datacenterId, String projectId)
			throws AppException {
		List<Tenant> list = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.TENANT_URI);
		List<JSONObject> result = restService.list(restTokenBean,
				OpenstackUriConstant.TENANT_DATA_NAMES);
		if (result != null && result.size() > 0) {
			list = new ArrayList<Tenant>();
			for (JSONObject jsonObject : result) {
				list.add(restService.json2bean(jsonObject, Tenant.class));
			}
		}
		return list;
	}

	public List<Tenant> listAll(String datacenterId) throws AppException {
		List<Tenant> list = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.IDENTITY_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.TENANT_URI);
		List<JSONObject> result = restService.list(restTokenBean,
				OpenstackUriConstant.TENANT_DATA_NAMES);
		if (result != null && result.size() > 0) {
			list = new ArrayList<Tenant>();
			for (JSONObject jsonObject : result) {
				list.add(restService.json2bean(jsonObject, Tenant.class));
			}
		}
		return list;
	}

	public boolean checkLinked(DcDataCenter datacenter) throws AppException {
		boolean initResult = true;
		try {
			RestTokenBean restTokenBean = getRestTokenBean(datacenter.getId(),
					OpenstackUriConstant.IDENTITY_SERVICE_URI);
			restService.getToken(restTokenBean);
		} catch (Exception e) {
			log.error(e.toString(), e);
			initResult = false;
		}
		return initResult;
	}

	public boolean checkLinked(String datacenterId) throws AppException {
		boolean initResult = true;
		try {
			RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
					OpenstackUriConstant.IDENTITY_SERVICE_URI);
			restService.getToken(restTokenBean);
		} catch (Exception e) {
			log.error(e.toString(), e);
			initResult = false;
		}
		return initResult;
	}

	public Tenant create(String datacenterId, String projectId, JSONObject data)
			throws AppException {
		Tenant tenant = null;
		EayunStackRole role = null;
		EayunStackUser user = null;
		BaseDcDataCenter datacenter = dataCenterService.getById(datacenterId);

		RestTokenBean restTokenBean = getRestTokenBean(datacenter.getId(), projectId,
				OpenstackUriConstant.IDENTITY_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.TENANT_URI);
		JSONObject result = restService.create(restTokenBean,
				OpenstackUriConstant.TENANT_DATA_NAME, data);
		tenant = restService.json2bean(result, Tenant.class);

		// 获取虚拟化平台角色
		role = getRole(datacenter, projectId, "admin");
		// 获取指定数据中心下面管理员
		user = getUserByName(datacenter, projectId,
				datacenter.getVCenterUsername());
		String uri = "/tenants/" + tenant.getId() + "/users/" + user.getId()
				+ "/roles/OS-KSADM/" + role.getId();

		// 项目绑定到管理员
		restTokenBean.setUrl(uri);
		restService.extend(restTokenBean, "role");

		return tenant;
	}

	/**
	 * 未同步的项目添加当前数据中心的管理账户的admin权限
	 * 
	 * @param datacenter
	 * @param projectId
	 * @return
	 * @throws AppException
	 */
	public JSONObject updateRoleByTenant(DcDataCenter datacenter,
			String projectId) throws AppException {
		JSONObject json = null;
		EayunStackRole role = null;
		EayunStackUser user = null;

		// 获取虚拟化平台角色
		role = getRole(datacenter, datacenter.getOsAdminProjectId(), "admin");

		// 获取指定数据中心下面管理员
		user = getUserByName(datacenter, datacenter.getOsAdminProjectId(),
				datacenter.getVCenterUsername());
		String uri1 = "/tenants/" + projectId + "/users/" + user.getId()
				+ "/roles";
		String uri = "/tenants/" + projectId + "/users/" + user.getId()
				+ "/roles/OS-KSADM/" + role.getId();

		RestTokenBean restTokenBean = getRestTokenBean(datacenter.getId(), projectId,
				OpenstackUriConstant.IDENTITY_SERVICE_URI);
		restTokenBean.setUrl(uri1);

		List<JSONObject> roles = restService.list(restTokenBean, "roles");
		int index = 0;
		if (null != roles) {
			for (JSONObject roleJson : roles) {
				if ("admin".equals(roleJson.getString("name"))) {
					index++;
				}
			}
		}
		// 项目绑定到管理员
		if (index == 0) {
			restTokenBean.setUrl(uri);
			json = restService.extend(restTokenBean, "role");
		}

		return json;
	}

	/**
	 * 修改项目
	 * 
	 * @param datacenterId
	 *            ：数据中心id
	 * @param projectId
	 *            ：主项目id
	 * @param id
	 *            ：项目id
	 * @param data
	 *            ：修改的数据
	 * @return
	 * @throws AppException
	 */
	public Tenant edit(String datacenterId, String projectId, String id,
			JSONObject data) throws AppException {
		Tenant resultData = null;

		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.IDENTITY_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.TENANT_URI + "/" + id);
		// 执行具体业务操作，并获取返回结果
		JSONObject result = restService.update(restTokenBean,
				OpenstackUriConstant.TENANT_DATA_NAME, data);
		if (result != null) {
			resultData = restService.json2bean(result, Tenant.class);
		}
		return resultData;
	}

	/**
	 * 修改项目配额--cpu
	 * 
	 * @param datacenterId
	 *            ：数据中心id
	 * @param projectId
	 *            ：主项目id
	 * @param id
	 * @param data
	 * @return
	 * @throws AppException
	 */
	public JSONObject editQuota(String datacenterId, String projectId,
			String id, JSONObject data) throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		restTokenBean.setUrl("/os-quota-sets/" + id);
		JSONObject result = restService
				.update(restTokenBean, "quota_set", data);
		return result;
	}

	/**
	 * 修改项目配额--云硬盘，云快照，云硬盘和快照大小
	 * 
	 * @param datacenterId
	 *            ：数据中心id
	 * @param projectId
	 *            ：主项目id
	 * @param id
	 * @param data
	 * @return
	 * @throws AppException
	 */
	public JSONObject editQuotaVolumes(String datacenterId, String projectId,
			String id, JSONObject data) throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.VOLUME_SERVICE_URI);
		restTokenBean.setUrl("/os-quota-sets/" + id);
		JSONObject result = restService
				.update(restTokenBean, "quota_set", data);
		return result;
	}

	/**
	 * 修改项目配额--网络
	 * 
	 * @param datacenterId
	 *            ：数据中心id
	 * @param projectId
	 *            ：主项目id
	 * @param id
	 * @param data
	 * @return
	 * @throws AppException
	 */
	public JSONObject editQuotaNetwork(String datacenterId, String projectId,
			String id, JSONObject data) throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl("/v2.0/quotas/" + id);
		JSONObject result = restService.update(restTokenBean, "quota", data);
		return result;
	}

	/**
	 * 获取项目的配额
	 * 
	 * @param datacenterId
	 *            ：数据中心id
	 * @param projectId
	 *            ：主项目id
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public JSONObject getQuota(String datacenterId, String projectId, String id)
			throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		restTokenBean.setUrl("/os-quota-sets/" + projectId);
		JSONObject result = restService.get(restTokenBean, "quota_set");
		return result;
	}

	/**
	 * 获取项目配额--云硬盘，云硬盘快照
	 * 
	 * @param datacenterId
	 *            ：数据中心id
	 * @param projectId
	 *            ：主项目id
	 * @param id
	 * @param data
	 * @return
	 * @throws AppException
	 */
	public JSONObject getQuotaVm(String datacenterId, String projectId)
			throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.VOLUME_SERVICE_URI);
		restTokenBean.setUrl("/os-quota-sets/" + projectId);
		JSONObject result = restService.get(restTokenBean, "quota_set");
		return result;
	}

	/**
	 * 获取项目配额--网络数量、子网数量、浮动ip数量
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @return
	 * @throws AppException
	 */
	public JSONObject getNetworkQuota(String datacenterId, String projectId)
			throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl("/v2.0/quotas/" + projectId);
		JSONObject result = restService.get(restTokenBean, "quota");
		return result;
	}

	/**
	 * （获取Cores,FloatingIp,Instances,RAM,SecurityGroups）
	 * 
	 * @param datacenterId
	 *            :数据中心id
	 * @param id
	 *            ：项目id
	 * @param dataString
	 *            ：要获取资源的标志
	 * @return
	 * @throws AppException
	 */
	public JSONObject getSpecifyQuota(String datacenterId, String projectId,
			String dataString) throws AppException {
		JSONObject json = new JSONObject();
		JSONObject result = null;

		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		restTokenBean.setUrl("/limits");
		result = restService.get(restTokenBean, "limits");
		JSONObject absolute = result.getJSONObject("absolute");
		if ("totalCoresUsed".equals(dataString)) {// 已经使用的Cpu
			int i = absolute.getIntValue(dataString);
			json.put(dataString, i);
		} else if ("totalFloatingIpsUsed".equals(dataString)) {// 浮动IP
			int i = absolute.getIntValue(dataString);
			json.put(dataString, i);
		} else if ("totalInstancesUsed".equals(dataString)) {// 云主机
			int i = absolute.getIntValue(dataString);
			json.put(dataString, i);
		} else if ("totalRAMUsed".equals(dataString)) {// 内存
			float i = absolute.getFloatValue(dataString);
			json.put(dataString, i);
		} else if ("totalSecurityGroupsUsed".equals(dataString)) {// 安全组
			int i = absolute.getIntValue(dataString);
			json.put(dataString, i);
		}
		return json;
	}

	/**
	 * （获取Volumes,Gigabytes）
	 * 
	 * @param datacenterId
	 *            :数据中心id
	 * @param id
	 *            ：项目id
	 * @param dataString
	 *            ：要获取资源的标志
	 * @return
	 * @throws AppException
	 */
	public JSONObject getSpecifyVolumev2(String datacenterId, String projectId,
			String dataString) throws AppException {
		JSONObject json = new JSONObject();
		JSONObject result = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.VOLUME_SERVICE_URI);
		restTokenBean.setUrl("/limits");
		result = restService.get(restTokenBean, "limits");
		JSONObject absolute = result.getJSONObject("absolute");
		if ("totalVolumesUsed".equals(dataString)) {// 块已使用
			int i = absolute.getIntValue(dataString);
			json.put(dataString, i);
		} else if ("totalGigabytesUsed".equals(dataString)) {// 容量已使用
			int i = absolute.getIntValue(dataString);
			json.put(dataString, i);
		} else if ("totalSnapshotsUsed".equals(dataString)) {// 云快照
			int i = absolute.getIntValue(dataString);
			json.put(dataString, i);
		}
		return json;
	}

	/**
	 * 删除指定id的租户
	 * 
	 * @param datacenterId
	 *            ：数据中心id
	 * @param projectId
	 *            ：主项目id
	 * @param id
	 *            ：项目id
	 * @return
	 * @throws AppException
	 */
	public boolean delete(String datacenterId, String projectId, String id)
			throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.IDENTITY_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.TENANT_URI + "/" + id);
		return restService.delete(restTokenBean);
	}

	private EayunStackRole getRole(BaseDcDataCenter datacenter, String projectId,
			String name) throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenter.getId(), projectId,
				OpenstackUriConstant.IDENTITY_SERVICE_URI);
		restTokenBean.setUrl("/OS-KSADM/roles/");
		List<JSONObject> result = restService.list(restTokenBean, "roles");
		for (JSONObject jsonObject : result) {
			EayunStackRole role = restService.json2bean(jsonObject, EayunStackRole.class);
			if (name != null && name.equals(role.getName())) {
				return role;
			}
		}
		return null;
	}

	/**
	 * openstack平台获取用户详情功能
	 * 
	 * @return
	 * @throws AppException
	 */
	private EayunStackUser getUserByName(BaseDcDataCenter datacenter, String projectId,
			String name) throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenter.getId(), projectId,
				OpenstackUriConstant.IDENTITY_SERVICE_URI);
		restTokenBean.setUrl("/users?name=" + name);
		JSONObject result = null;
		result = restService.get(restTokenBean, "user");
		if (result != null) {
			return restService.json2bean(result, EayunStackUser.class);
		}

		return null;
	}
	
	/**                                                                                                         
	 * 获取底层数据中心下的项目及配额信息                                                               
	 * -----------------                                                                                      
	 * @author zhouhaitao                                                                                     
	 * @param dataCenter                                                                                      
	 *                                                                                                        
	 * @return                                                                                                
	 *                                                                                                        
	 */                                                                                                       
	public List<BaseCloudProject> getStackList(BaseDcDataCenter dataCenter) {                                  
		List <BaseCloudProject> list = new ArrayList<BaseCloudProject>();                                         
		RestTokenBean restTokenBean = getRestTokenBean(dataCenter.getId(),                                              
				OpenstackUriConstant.IDENTITY_SERVICE_URI);                                                          
		restTokenBean.setUrl(OpenstackUriConstant.TENANT_URI);                                           
		List<JSONObject> result = restService.list(restTokenBean,OpenstackUriConstant.TENANT_DATA_NAMES);
                                                                                                            
		if (result != null && result.size() > 0) {                                                              
			for (JSONObject jsonObject : result) {                                                                
				Tenant data = restService.json2bean(jsonObject,                                             
						Tenant.class);                                                                            
				 BaseCloudProject ccn=new BaseCloudProject(data,dataCenter.getId());                                 
				 if(!"admin".equalsIgnoreCase(ccn.getPrjName())&&
							!"services".equalsIgnoreCase(ccn.getPrjName())){
							getProjectQuota(dataCenter, ccn);
							list.add(ccn);
						}
				}                                                                                                   
			}                                                                                                     
		return list;                                                                                            
	} 
	
	/**
	 * 获取项目配额信息
	 * 
	 * @param cloudProject
	 * @return
	 * @throws Exception
	 */
	private void getProjectQuota(BaseDcDataCenter dataCenter,
			BaseCloudProject cloudProject) throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(dataCenter.getId(),                                              
				OpenstackUriConstant.COMPUTE_SERVICE_URI);                                                          
		restTokenBean.setUrl("/os-quota-sets/"+ cloudProject.getProjectId());  
		JSONObject computeJson = restService.get(restTokenBean,"quota_set");

		restTokenBean = getRestTokenBean(dataCenter.getId(),                                              
				OpenstackUriConstant.VOLUME_SERVICE_URI);                                                          
		restTokenBean.setUrl("/os-quota-sets/"+ cloudProject.getProjectId());
		JSONObject volumeJson = restService.get(restTokenBean, "quota_set");

		restTokenBean = getRestTokenBean(dataCenter.getId(),                                              
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl("/v2.0/quotas/" + cloudProject.getProjectId());
		JSONObject networkJson = restService.get(restTokenBean, "quota");

		if (computeJson != null) {
			cloudProject.setMemory(computeJson.getIntValue("ram") / 1024);// 内存大小
			cloudProject.setCpuCount(computeJson.getIntValue("cores"));// cpu核数
			cloudProject.setHostCount(computeJson.getIntValue("instances"));// 每个项目对应的云主机数量
		}
		if (null != volumeJson) {
			cloudProject.setDiskCapacity(volumeJson.getIntValue("gigabytes"));// 云硬盘容量
			cloudProject.setDiskCount(volumeJson.getIntValue("volumes"));// 云硬盘数量
			cloudProject.setDiskSnapshot(volumeJson.getIntValue("backups"));// 云硬盘备份数量
			cloudProject.setSnapshotSize(volumeJson.getIntValue("backup_gigabytes"));// 云硬盘备份容量
		}
		if (null != networkJson) {
			cloudProject.setNetWork(networkJson.getIntValue("network"));// 网络数量
			cloudProject.setSubnetCount(networkJson.getIntValue("subnet"));// 子网数量
			cloudProject.setOuterIP(networkJson.getIntValue("floatingip"));// 浮动ip数量
			cloudProject.setRouteCount(networkJson.getIntValue("router"));// 路由数量
			cloudProject.setSafeGroup(networkJson.getIntValue("security_group"));// 安全组数量
			cloudProject.setQuotaPool(networkJson.getIntValue("pool"));// 负载均衡数量
			cloudProject.setCountVpn(networkJson.getIntValue("vpnservice"));// VPN数量
			cloudProject.setPortMappingCount(networkJson.getIntValue("portmapping"));// 端口映射数量
		}
	}
	

	public Role getRole(BaseDcDataCenter dataCenter,String name) throws Exception{
		RestTokenBean restTokenBean = getRestTokenBean(dataCenter.getId(),                                              
				OpenstackUriConstant.IDENTITY_SERVICE_URI);                                                          
		restTokenBean.setUrl("/OS-KSADM/roles/");
		List<JSONObject> result = restService.list(restTokenBean,"roles");
		if(null!=result){
			for (JSONObject jsonObject : result) {
				Role role = restService.json2bean(jsonObject, Role.class);
				if (name != null && name.equals(role.getName())) {
					return role;
				}
			}
		}
		return null;
	}
	
	public User getUserByName (BaseDcDataCenter dataCenter,String name) throws Exception{
		RestTokenBean restTokenBean = getRestTokenBean(dataCenter.getId(),                                              
				OpenstackUriConstant.IDENTITY_SERVICE_URI); 
		restTokenBean.setUrl("/users?name=" + name);
		JSONObject result = restService.get(restTokenBean,"user");
		if (result != null) {
			return restService.json2bean(result, User.class);
		}
		return null;
	}
	
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
			String projectId) throws Exception {
		JSONObject json = null;
		RestTokenBean restTokenBean = getRestTokenBean(dataCenter.getId(),                                              
				OpenstackUriConstant.IDENTITY_SERVICE_URI);
		String uri1 = "/tenants/" + projectId + "/users/" + user.getId()
				+ "/roles";
		String uri = "/tenants/" + projectId + "/users/" + user.getId()
				+ "/roles/OS-KSADM/" + role.getId();
		restTokenBean.setUrl(uri1);
		List<JSONObject> roles = restService.list(restTokenBean, "roles");
		int index = 0;
		if (null != roles) {
			for (JSONObject roleJson : roles) {
				if ("admin".equals(roleJson.getString("name"))) {
					index++;
				}
			}
		}
		// 项目绑定到管理员
		if (index == 0) {
			restTokenBean.setUrl(uri);
			json = restService.extend(restTokenBean, "role");
		}
		return json;
	}
	
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
	public JSONObject editTroveQuota(String dcId,String prjId,JSONObject data){
		RestTokenBean restTokenBean = getRestTokenBean(dcId,
				OpenstackUriConstant.TROVE_SERVICE_URI);
		restTokenBean.setUrl("/mgmt/quotas/" + prjId);
		JSONObject result = restService.update(restTokenBean, "quotas", data);
		return result;
	}
}
