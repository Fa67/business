package com.eayun.eayunstack.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.BackUp;
import com.eayun.eayunstack.model.QosAssociation;
import com.eayun.eayunstack.model.Restore;
import com.eayun.eayunstack.model.Vm;
import com.eayun.eayunstack.model.Volume;
import com.eayun.eayunstack.model.VolumeQos;
import com.eayun.eayunstack.model.VolumeType;
import com.eayun.eayunstack.service.OpenstackVolumeService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
import com.eayun.virtualization.model.BaseCloudProject;
import com.eayun.virtualization.model.CloudProject;

@Service
@SuppressWarnings("unused")
public class OpenstackVolumeServiceImpl extends
		OpenstackBaseServiceImpl<Volume> implements OpenstackVolumeService{
	private static final Logger log = LoggerFactory
			.getLogger(OpenstackVolumeServiceImpl.class);

	private void initData(Volume data, JSONObject object, List<Vm> list) {
		data.setTenant_id(object.getString("os-vol-tenant-attr:tenant_id"));
		data.setMigstat(object.getString("os-vol-mig-status-attr:migstat"));
		data.setHost(object.getString("os-vol-host-attr:host"));
		data.setName_id(object.getString("os-vol-mig-status-attr:name_id"));
		if (data.getAttachments() != null && data.getAttachments().length > 0) {
			// 转义并设置云硬盘所挂载的云主机名称
			String vmId = data.getAttachments()[0].getServer_id();
			if (list != null && list.size() > 0) {
				for (Vm vm : list) {
					if (vm.getId().equals(vmId)) {
						data.getAttachments()[0].setVm_name(vm.getName());
					}
				}
			}
		}
	}

	private List<Volume> list(RestTokenBean restTokenBean, String url,
			List<Vm> vmList) throws AppException {
		List<Volume> list = null;
		restTokenBean.setUrl(url);

		List<JSONObject> result = restService.list(restTokenBean,
				OpenstackUriConstant.DISK_DATA_NAMES);
		if (result != null && result.size() > 0) {
			for (JSONObject jsonObject : result) {
				if (list == null) {
					list = new ArrayList<Volume>();
				}
				Volume volume = restService.json2bean(jsonObject, Volume.class);
				initData(volume, jsonObject, vmList);
				volume.setCreated_at(volume.getCreated_at().replace('T', ' ')
						.substring(0, 19));
				list.add(volume);
			}
		}
		return list;
	}

	public List<Volume> list(String datacenterId, String projectId)
			throws AppException {

		if (projectId == null || projectId.equals("")) {
			return null;
		}
		// 查询云主机列表 从OpenStack中获取数据
		List<Vm> vmList = this.vmList(datacenterId, projectId);

		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.VOLUME_SERVICE_URI);

		return list(restTokenBean, OpenstackUriConstant.DISK_URI, vmList);

	}

	public List<Volume> listAll(String datacenterId) throws AppException {
		List<Volume> list = null;

		List<CloudProject> projectList = projectService
				.getProjectListByDataCenter(datacenterId);
		if (projectList != null && projectList.size() > 0) {
			if (list == null) {
				list = new ArrayList<Volume>();
			}
			RestTokenBean restTokenBean = null;
			for (BaseCloudProject cloudProject : projectList) {
				if (restTokenBean == null) {
					restTokenBean = getRestTokenBean(datacenterId,
							cloudProject.getProjectId(),
							OpenstackUriConstant.COMPUTE_SERVICE_URI);
				} else {
					restTokenBean.setTenantId(cloudProject.getProjectId());
				}
				List<Vm> vmList = this.vmList(datacenterId,
						cloudProject.getProjectId());

				list.addAll(list(restTokenBean, OpenstackUriConstant.DISK_URI,
						vmList));
			}
		}

		return list;
	}

	public Volume getById(String datacenterId, String projectId, String id)
			throws AppException {
		Volume result = null;
		List<Vm> vmList = this.vmList(datacenterId, projectId);
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.VOLUME_SERVICE_URI);

		JSONObject json = restService.getById(restTokenBean, "/volumes/",
				OpenstackUriConstant.DISK_DATA_NAME, id);

		if (json != null) {
			// 转换成java对象
			result = restService.json2bean(json, Volume.class);
			result.setCreated_at(result.getCreated_at().replace('T', ' ')
					.substring(0, 19));
			// 名称特殊的key，调用initData方法做数据初始化
			initData(result, json, vmList);
		}

		return result;
	}
	
	
	
	
	/**
	 * 更改指定云硬盘的类型
	 */
	public boolean retype(String dcId, String prjId, String volId,
			JSONObject data) throws AppException {
		Volume volume = null;
		RestTokenBean restTokenBean = getRestTokenBean(dcId, prjId,"volumev2");
		restTokenBean.setUrl("/volumes/"+volId+"/action");
		this.restService.post(restTokenBean, data);
		return true;	
	}
	
	
	/**
	 * 获取指定数据中心下的volumetype
	 * @author chengxiaodong
	 */
	public List<VolumeType> getVolumeTypes(String datacenterId) throws AppException {
		List<VolumeType> volumetypes=null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				"volumev2");
		restTokenBean.setUrl("/types");
		List<JSONObject> result = this.restService.list(restTokenBean, "volume_types");
		if (result != null && result.size() > 0) {
			if (volumetypes == null) {
				volumetypes = new ArrayList<VolumeType>();
			}
			for (JSONObject jsonObject : result) {
				VolumeType volumeType = restService.json2bean(jsonObject, VolumeType.class);
				volumeType.setIs_public(jsonObject.getString("os-volume-type-access:is_public"));
				volumetypes.add(volumeType);	
			}
		}
		return volumetypes;
		
	}
	
	
	/**
	 * 获取指定数据中心下的volumeqos
	 * @author chengxiaodong
	 */
	public List<VolumeQos> getAllVolumeQos(String datacenterId) throws AppException {
		List<VolumeQos> volumeQoss=null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				"volumev2");
		restTokenBean.setUrl("/qos-specs");
		List<JSONObject> result = this.restService.list(restTokenBean, "qos_specs");
		if (result != null && result.size() > 0) {
			if (volumeQoss == null) {
				volumeQoss = new ArrayList<VolumeQos>();
			}
			for (JSONObject jsonObject : result) {
				VolumeQos volumeQos = restService.json2bean(jsonObject, VolumeQos.class);
				volumeQoss.add(volumeQos);	
			}
		}
		return volumeQoss;
		
	}
	
	
	
	/**
	 * 获取指定qos与volumetype的关联关系
	 * @author chengxiaodong
	 */
	public List<QosAssociation> getAllAssociationsForQoS(String datacenterId,
			String qosId) {
		List<QosAssociation> qosAssociations=null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				"volumev2");
		restTokenBean.setUrl("/qos-specs/"+qosId+"/associations");
		List<JSONObject> result = this.restService.list(restTokenBean, "qos_associations");
		if (result != null && result.size() > 0) {
			if (qosAssociations == null) {
				qosAssociations = new ArrayList<QosAssociation>();
			}
			for (JSONObject jsonObject : result) {
				QosAssociation qosAssociation = restService.json2bean(jsonObject, QosAssociation.class);
				qosAssociations.add(qosAssociation);	
			}
		}
		return qosAssociations;
	}
	
	
	/**
	 * 更改指定qos的参数
	 * @author chengxiaodong
	 */
	public boolean setQosKeys(String datacenterId, String qosId, JSONObject data)
			throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, "volumev2");
		restTokenBean.setUrl("/qos-specs/"+qosId);
		this.restService.put(restTokenBean, data);
		return true;
	}
	
	
	
	
	/**
	 * 云硬盘扩容
	 * @param datacenterId
	 * @param projectId
	 * @param volId
	 * @param data
	 * @return
	 * @throws AppException
	 * @author chengxiaodong
	 */
	public boolean extendVolume(String datacenterId, String projectId,String volId, JSONObject data)
			throws AppException {
		Volume volume = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				"volumev2");
		restTokenBean.setUrl("/volumes/"+volId+"/action");
		this.restService.post(restTokenBean, data);
		return true;
	}
	
	/**
	 * 创建云硬盘备份
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 * @return
	 * @author chengxiaodong
	 * @throws AppException
	 */
	@Override
	public BackUp createBackUps(String datacenterId, String projectId,
			JSONObject data) throws AppException {
		BackUp backUp = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				"volumev2");
		restTokenBean.setUrl("/backups");
		JSONObject result = this.restService.create(restTokenBean, "backup",data);
		backUp = (BackUp) this.restService.json2bean(result, BackUp.class);
		return backUp;
	} 
	
	/**
	 * 删除备份
	 * @param datacenterId
	 * @param projectId
	 * @param id
	 * @return
	 * @author chengxiaodong
	 * @throws AppException
	 */
	@Override
	public boolean deleteBackUps(String datacenterId, String projectId,
			String id) throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				"volumev2");
		restTokenBean.setUrl("/backups/" + id);
		return this.restService.delete(restTokenBean);
	}
	
	
	/**
	 * 回滚云硬盘
	 * @param datacenterId
	 * @param projectId
	 * @param snapId
	 * @param volId
	 * @param data
	 * @return
	 * @author chengxiaodong
	 * @throws AppException
	 */
	@Override
	public Restore restoreVolume(String datacenterId, String projectId,
			String snapId, JSONObject data) throws AppException {
		Restore restore=null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				"volumev2");
		restTokenBean.setUrl("/backups/"+snapId+"/restore");
		JSONObject result = this.restService.create(restTokenBean, "restore",
				data);

		restore = (Restore) this.restService.json2bean(result, Restore.class);
		return restore;
	}
	


	/**
	 * 查询当前项目下所有的云硬盘备份
	 * @param datacenterId
	 * @param projectId
	 * @return
	 * @author chengxiaodong
	 * @throws AppException
	 */
	@Override
	public List<BackUp> getAllBackUps(String datacenterId) throws AppException {
		List<BackUp> backUps=null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				"volumev2");
		restTokenBean.setUrl("/backups/detail");
		List<JSONObject> result = this.restService.list(restTokenBean, "backups");
		if (result != null && result.size() > 0) {
			if (backUps == null) {
				backUps = new ArrayList<BackUp>();
			}
			for (JSONObject jsonObject : result) {
				BackUp backUp = restService.json2bean(jsonObject, BackUp.class);
				backUps.add(backUp);	
			}
		}
		return backUps;
	}
	
	/**
	 * 查询指定云硬盘备份的信息
	 * @param datacenterId
	 * @param projectId
	 * @return
	 * @author chengxiaodong
	 * @throws AppException
	 */
	@Override
	public BackUp getBackUp(String datacenterId, String projectId,String snapId) throws AppException {
		BackUp backUp = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				"volumev2");
		restTokenBean.setUrl("/backups/"+snapId);
		JSONObject result = this.restService.get(restTokenBean, "backup");
		backUp = (BackUp) this.restService.json2bean(result, BackUp.class);
		return backUp;
	}
	
	/**
	 * 更改指定云硬盘状态到预期状态
	 * @param datacenterId
	 * @param projectId
	 * @return
	 * @author chengxiaodong
	 * @throws AppException
	 */
	@Override
	public boolean resetVolStatus(String datacenterId, String projectId,String volId,JSONObject data) throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,"volumev2");
		restTokenBean.setUrl("/volumes/"+volId+"/action");
		JSONObject result = this.restService.post(restTokenBean, data);
		return true;
	}
	
	
	

	public Volume create(String datacenterId, String projectId, JSONObject data)
			throws AppException {
		Volume volume = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,OpenstackUriConstant.VOLUME_SERVICE_URI);
		restTokenBean.setUrl("/volumes");
		JSONObject result = restService.create(restTokenBean,
				OpenstackUriConstant.DISK_DATA_NAME, data);
		volume = restService.json2bean(result, Volume.class);

		return volume;
	}

	public boolean delete(String datacenterId, String projectId, String id)
			throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.VOLUME_SERVICE_URI);
		restTokenBean.setUrl("/volumes/" + id);

		return restService.delete(restTokenBean);
	}

	public Volume update(String datacenterId, String projectId,
			JSONObject data, String id) throws AppException {
		Volume volume = null;

		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.VOLUME_SERVICE_URI);
		restTokenBean.setUrl("/volumes/" + id);

		JSONObject result = restService.update(restTokenBean,
				OpenstackUriConstant.DISK_DATA_NAME, data);
		volume = restService.json2bean(result, Volume.class);
		volume.setCreated_at(volume.getCreated_at().replace('T', ' ')
				.substring(0, 19));

		return volume;
	}

	public boolean bind(String datacenterId, String projectId, String id,
			String vmId) throws AppException {

		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("volumeId", id);
		data.put("volumeAttachment", temp);

		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		restTokenBean.setUrl("/servers/" + vmId + "/os-volume_attachments");
		JSONObject result = restService.create(restTokenBean, null, data);
		return true;
	}

	public boolean debind(String datacenterId, String projectId, String id,
			String vmId) throws AppException {
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("volumeId", id);
		data.put("volumeAttachment", temp);

		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		restTokenBean.setUrl("/servers/" + vmId + "/os-volume_attachments/"
				+ id);
		boolean result = restService.delete(restTokenBean);
		return result;
	}

	// 查询云主机列表
	public List<Vm> vmList(String datacenterId, String projectId)
			throws AppException {
		List<Vm> vmList = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.VM_DETAIL_URI);

		List<JSONObject> vmResult = restService.list(restTokenBean,
				OpenstackUriConstant.VM_DATA_NAMES);
		if (vmResult != null && vmResult.size() > 0) {
			if (vmList == null) {
				vmList = new ArrayList<Vm>();
			}
			for (JSONObject jsonObject : vmResult) {
				if (jsonObject.getString("image") != null && !jsonObject.getString("image").equals("")) {
					Vm vm = restService.json2bean(jsonObject, Vm.class);
					vmList.add(vm);
				}
			}
		}
		return vmList;
	}
	
	
	public JSONObject get (String dcId,String prjId,String volId) throws Exception{
		RestTokenBean restTokenBean = getRestTokenBean(dcId, prjId,
				OpenstackUriConstant.VOLUME_SERVICE_URI);
		return restService.getJSONById(restTokenBean, "/volumes/", volId);
	}
	
	/**                                                                                                         
	 * 获取底层项目的云硬盘                                                               
	 * -----------------                                                                                      
	 * @author zhouhaitao                                                                                     
	 * @param dataCenter                                                                                      
	 *                                                                                                        
	 * @return                                                                                                
	 * @throws Exception 
	 *                                                                                                        
	 */                                                                                                       
	public List<JSONObject> getStackList(BaseDcDataCenter dataCenter,String prjId){                                  
		RestTokenBean restTokenBean = getRestTokenBean(dataCenter.getId(), prjId,                                             
				OpenstackUriConstant.VOLUME_SERVICE_URI);                                                          
		restTokenBean.setUrl(OpenstackUriConstant.DISK_URI);                                           
		List<JSONObject> result = restService.list(restTokenBean,OpenstackUriConstant.DISK_DATA_NAMES);
        return result ;                                                                                                    
	}
	
	public <T> T json2bean(JSONObject jSONObject, Class<T> clazz){
		return restService.json2bean(jSONObject, clazz);
	}

	

	

	

}