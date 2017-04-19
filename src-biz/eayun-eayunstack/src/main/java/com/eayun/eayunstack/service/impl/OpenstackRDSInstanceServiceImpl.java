package com.eayun.eayunstack.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.StringUtil;
import com.eayun.database.instance.model.CloudRDSInstance;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.Port;
import com.eayun.eayunstack.model.RDSInstance;
import com.eayun.eayunstack.service.OpenstackRDSInstanceService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
import com.eayun.virtualization.model.BaseCloudProject;
import com.eayun.virtualization.model.CloudProject;

@Service
public class OpenstackRDSInstanceServiceImpl extends OpenstackBaseServiceImpl<RDSInstance>
		implements OpenstackRDSInstanceService{
	
	private static final Logger log = LoggerFactory
			.getLogger(OpenstackVmServiceImpl.class);

	@Override
	public RDSInstance getById(String datacenterId, String projectId, String id) throws AppException {
		
		log.info("查询数据库实例详情");
		RDSInstance result = null;
		
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId, 
				OpenstackUriConstant.TROVE_SERVICE_URI);
		
		JSONObject jsonResult = restService.getById(restTokenBean, 
				"/mgmt" + OpenstackUriConstant.RDS_INSTANCES_URI + "/", 
				OpenstackUriConstant.RDS_DATA_NAME, id);
		if(jsonResult != null){
			result = restService.json2bean(jsonResult, RDSInstance.class);
			if(jsonResult.getJSONObject("server") != null
					&& jsonResult.getJSONObject("server").getString("id") != null){
				String vmId = jsonResult.getJSONObject("server").getString("id");
				result.setVmId(vmId);
			}
		}
		return result;
	}
	
	@Override
	public RDSInstance create(String datacenterId, String projectId, JSONObject data) throws AppException {
		return null;
	}

	/**
	 * 删除云数据库实例
	 * @param datacenterId -- 数据中心ID
	 * @param projectId  -- 项目ID
	 * @param id -- 云数据库实例ID
	 * @return
	 * @throws AppException
	 */
	@Override
	public boolean delete(String datacenterId, String projectId, String id) throws AppException {
		
		log.info("删除云数据库实例");
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.TROVE_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.RDS_INSTANCES_URI + "/" + id);
		// 执行具体业务操作，并获取返回结果
		return restService.delete(restTokenBean);
	}

	@Override
	public RDSInstance update(String datacenterId, String projectId, JSONObject data, String id) throws AppException {
		return null;
	}

	@Override
	public List<RDSInstance> list(RestTokenBean restTokenBean, String url) 
			throws AppException {
		
		List<RDSInstance> list = null;
		// 获取云数据库实例信息
		restTokenBean.setUrl(url);
		List<JSONObject> result = restService.list(restTokenBean,
				OpenstackUriConstant.RDS_DATA_NAMES);
		
		if (result != null && result.size() > 0) {
			for (JSONObject jsonObject : result) {
				if (list == null) {
					list = new ArrayList<RDSInstance>();
				}
				// json转换为java对象
				RDSInstance rdsInstance = restService.json2bean(jsonObject, RDSInstance.class);
				// 控制云主机创建时间不显示TZ字母
				list.add(rdsInstance);
			}
		}
		return list;
	}

	/**
	 * 获取指定客户的某个数据中心下的所有云数据库实例
	 * @param datacenterId  -- 数据中心
	 * @param projectId  -- 项目ID
	 * @return
	 */
	@Override
	public List<RDSInstance> list(String datacenterId, String projectId) 
			throws AppException {
		// 初始化opentack平台连接
		if (projectId == null || projectId.equals("")) {
			return null;
		}
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.TROVE_SERVICE_URI);
		return list(restTokenBean, OpenstackUriConstant.RDS_INSTANCES_URI);
	}

	/**
	 * 获取指定数据中心下的云数据库实例
	 * @param datacenterId -- 数据中心ID
	 * @return
	 */
	@Override
	public List<RDSInstance> listAll(String datacenterId) 
			throws AppException {
		
		List<RDSInstance> list = null;

		List<CloudProject> projectList = projectService
				.getProjectListByDataCenter(datacenterId);

		// 项目列表非空并且长度大于0时
		if (projectList != null && projectList.size() > 0) {
			if (list == null) {
				list = new ArrayList<RDSInstance>();
			}
			RestTokenBean restTokenBean = null;
			for (BaseCloudProject cloudProject : projectList) {
				if (restTokenBean == null) {
					restTokenBean = getRestTokenBean(datacenterId,
							cloudProject.getProjectId(),
							OpenstackUriConstant.TROVE_SERVICE_URI);
				} else {
					restTokenBean.setTenantId(cloudProject.getProjectId());
				}

				List<RDSInstance> rdsInstanceList = list(restTokenBean,
						OpenstackUriConstant.RDS_INSTANCES_URI);
				if (rdsInstanceList != null && rdsInstanceList.size() > 0) {
					list.addAll(rdsInstanceList);
				}
			}
		}

		return list;
	}

	@Override
	public String createRdsInstance(CloudRDSInstance cloudRdsInstance, RDSInstance rdsInstance) 
			throws AppException {
		log.info("创建数据库实例");
		
		String errMsg  = null;
		JSONObject object = new JSONObject();
		JSONObject port = new JSONObject();
		RDSInstance rdsBean = new RDSInstance();
		try{
				RestTokenBean restTokenBean = getRestTokenBean(cloudRdsInstance.getDcId(), cloudRdsInstance.getPrjId(),
					OpenstackUriConstant.TROVE_SERVICE_URI);

				Port portData = null;
				object.put("name", cloudRdsInstance.getRdsName());
				object.put("flavorRef", cloudRdsInstance.getFlavorId());
				if(!StringUtils.isEmpty(cloudRdsInstance.getSubnetId())){
					portData = createPort(cloudRdsInstance.getDcId(),cloudRdsInstance.getTorvePrjId(),
							cloudRdsInstance.getNetId(),cloudRdsInstance.getSubnetId(),new String []{cloudRdsInstance.getTroveSecurityGroupId()});
					port.put("port-id", portData.getId());
					port.put("net-id", cloudRdsInstance.getNetId());
				}
				
				JSONObject[] nets = {port};
				object.put("nics", nets);
				
				// 数据盘大小和类型
				JSONObject  volume = new JSONObject();
				volume.put("size", cloudRdsInstance.getVolumeSize());
				volume.put("type", cloudRdsInstance.getVolumeType());
				object.put("volume", volume);
				
				// 数据库类型和版本
				JSONObject  datastore = new JSONObject();
				datastore.put("type", cloudRdsInstance.getType());
				datastore.put("version", cloudRdsInstance.getVersion());
				object.put("datastore", datastore);
				
				// 配置组
				object.put("configuration",cloudRdsInstance.getConfigId());
				
				if(!StringUtil.isEmpty(cloudRdsInstance.getBackupId())){
					JSONObject backup = new JSONObject();
					backup.put("backupRef", cloudRdsInstance.getBackupId());
					object.put("restorePoint", backup);
				}
				
				if(!StringUtil.isEmpty(cloudRdsInstance.getMasterId())){
					object.put("replica_of", cloudRdsInstance.getMasterId());
				}
				
				JSONObject data = new JSONObject();
				data.put("instance", object);
				
				restTokenBean.setUrl(OpenstackUriConstant.RDS_INSTANCES_URI);
				JSONObject result = restService.create(restTokenBean, 
						OpenstackUriConstant.RDS_DATA_NAME, data);
				log.info("创建返回云数据库实例结果："+result);
				
				if (result != null) {
					rdsBean = restService.json2bean(result, RDSInstance.class);
					rdsBean.setIp(new String []{portData.getFixed_ips()[0].getIp_address()});
					rdsBean.setPortId(portData.getId());
					BeanUtils.copyPropertiesByModel(rdsInstance, rdsBean);
				}
			
		}catch(AppException e){
		    log.error(e.getMessage(),e);
		    errMsg = e.getMessage();
			throw e;
		}
		catch(Exception e){
		    log.error(e.getMessage(),e);
		    errMsg = e.getMessage();
			throw new AppException(e.getMessage());
		}
		return errMsg;
	}

	/**
	 * 创建子网对应的端口
	 * @param dcId
	 * @param prjId
	 * @param netId
	 * @param subnetId
	 * @param sgIds
	 * @return
     * @throws AppException
     */
	private Port createPort(String dcId,String prjId,String netId,String subnetId,String[] sgIds)
			throws AppException {
		Port port = null;
		JSONObject data  = new JSONObject ();
		JSONObject portJson  = new JSONObject ();
		JSONObject subnet  = new JSONObject ();
		subnet.put("subnet_id",subnetId );
		JSONObject subnets [] ={subnet};
		portJson.put("fixed_ips",subnets );
		portJson.put("network_id",netId );
		portJson.put("admin_state_up","true" );
		portJson.put("security_groups",sgIds );
		data.put("port", portJson);
		
		RestTokenBean restTokenBean = getRestTokenBean(dcId, prjId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.PORT_URI);
		JSONObject result = restService.create(restTokenBean,
				OpenstackUriConstant.PORT_DATA_NAME, data);
		
		if(result!=null){
			port = restService.json2bean(result, Port.class);
		}
		return port ;
	}

	/**
	 * 调整云数据库实例对应的数据盘的大小
	 * @param datacenterId  -- 数据中心ID
	 * @param projectId  -- 项目ID
	 * @param id  -- 云数据库实例ID
	 * @param volumeSize -- 调整后的数据盘大小
	 * @return
	 */
	@Override
	public void resizeVolume(String datacenterId, String projectId, String id, int volumeSize) 
			throws AppException {
		
		log.info("调整云数据库实例存储容量");
		JSONObject data = new JSONObject();
		JSONObject volume = new JSONObject();
		JSONObject size = new JSONObject();
		size.put("size", volumeSize);
		volume.put("volume", size);
		data.put("resize", volume);
		
		operate(datacenterId, projectId, id, data);
	}
	
	/**
	 * 调整云数据库实例对应的云主机规格（CPU和内存）
	 * @param datacenterId  -- 数据中心ID
	 * @param projectId  -- 项目ID
	 * @param id  -- 云数据库实例ID
	 * @param flavorId  -- 调整后的云主机规格ID
	 * @return
	 * @throws AppException
	 */
	@Override
	public void resizeFlavor(String datacenterId, String projectId, String id, String flavorId) throws AppException {
		
		log.info("调整云数据库实例规格");
		JSONObject data = new JSONObject();
		JSONObject flavorRef = new JSONObject();
		flavorRef.put("flavorRef", flavorId);
		data.put("resize", flavorRef);
		
		operate(datacenterId, projectId, id, data);
	}
	
	private JSONObject operate(String datacenterId, String projectId,
			String id, JSONObject data) throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.TROVE_SERVICE_URI);
		restTokenBean
				.setUrl(OpenstackUriConstant.RDS_INSTANCES_URI + "/" + id + "/action");
		// 执行具体业务操作，并获取返回结果
		JSONObject result = restService.operate(restTokenBean, data);

		return result;
	}

	/**
	 * 重启云数据库实例
	 * @param datacenterId  -- 数据中心ID
	 * @param projectId  -- 项目ID
	 * @param id  -- 云数据库实例ID
	 * @return
	 * @throws AppException
	 */
	@Override
	public void restartRdsInstance(String datacenterId, String projectId, String id) throws AppException {
		
		log.info("重启云数据库实例");
		JSONObject data = new JSONObject();
		data.put("restart", new JSONObject());
		operate(datacenterId, projectId, id, data);
	}

	/**
	 * 从库提升为主库操作
	 * @param datacenterId  -- 数据中心ID
	 * @param projectId  -- 项目ID
	 * @param id  -- 云数据库实例ID
	 * @return
	 * @throws AppException
	 */
	@Override
	public void detachReplica(String datacenterId, String projectId, String id) throws AppException {
		
		log.info("提升从库为主库");
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.TROVE_SERVICE_URI);
		
		restTokenBean.setUrl(OpenstackUriConstant.RDS_INSTANCES_URI + "/" + id);
		
		JSONObject arg = new JSONObject();
		JSONObject instance = new JSONObject();
		instance.put("replica_of", "null");
		instance.put("slave_of", "null");
		
		arg.put("instance", instance);
		
		restService.patch(restTokenBean, arg.toJSONString());
	}

	/**
	 * 修改云数据库实例名称
	 * 
	 * @param datacenterId  -- 数据中心ID
	 * @param projectId  -- 项目ID
	 * @param id  -- 云数据库实例ID
	 * @param name  -- 修改后的名称
	 * @return
	 * @throws AppException
	 */
	@Override
	public void edit(String datacenterId, String projectId, String id, String name) throws AppException {
		
		log.info("修改云数据库实例的名称");
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.TROVE_SERVICE_URI);
		
		restTokenBean.setUrl(OpenstackUriConstant.RDS_INSTANCES_URI + "/" + id);
		
		JSONObject arg = new JSONObject();
		JSONObject instance = new JSONObject();
		instance.put("name", name);
		arg.put("instance", instance);
		restService.patch(restTokenBean, arg.toJSONString());
	}

	/**
	 * 用于数据中心云数据库实例同步
	 * @param dataCenter  -- 数据中心
	 * @param projectId  -- 项目ID
	 * @return
	 * @throws AppException
	 */
	@Override
	public List<RDSInstance> getStackList(BaseDcDataCenter dataCenter, String projectId) throws AppException {
		return this.list(dataCenter.getId(), projectId);
	}

	@Override
	public JSONObject get(String datacenterId, String projectId, String id) throws Exception {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.TROVE_SERVICE_URI);
		JSONObject json = restService.getJSONById(restTokenBean,OpenstackUriConstant.RDS_INSTANCES_URI + "/",id);

		return json;
	}

	/**
	 * 重置数据库实例的状态
	 * @param datacenterId -- 数据中心ID
	 * @param projectId -- 租户ID
	 * @param id -- 实例ID
	 * @throws Exception
     */
	@Override
	public JSONObject resetStatus(String datacenterId, String id) throws Exception {
		log.info("重置云数据库实例状态");
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.TROVE_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.RDS_INSTANCES_URI + "/" + id + "/action");
		JSONObject data = new JSONObject();
		data.put("reset_status", new JSONObject());
		JSONObject result = restService.operate(restTokenBean, data);
		return result;
	}
}
