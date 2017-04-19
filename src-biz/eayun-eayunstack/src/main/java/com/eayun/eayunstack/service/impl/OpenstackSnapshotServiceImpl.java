package com.eayun.eayunstack.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.Snapshot;
import com.eayun.eayunstack.service.OpenstackSnapshotService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
import com.eayun.virtualization.model.BaseCloudProject;
import com.eayun.virtualization.model.BaseCloudSnapshot;
import com.eayun.virtualization.model.CloudProject;

@Service
@SuppressWarnings("unused")
public class OpenstackSnapshotServiceImpl extends
		OpenstackBaseServiceImpl<Snapshot> implements OpenstackSnapshotService {
	private static final Logger log = LoggerFactory
			.getLogger(OpenstackSnapshotServiceImpl.class);

	private void initData(Snapshot data, JSONObject object) {
		data.setTenant_id(object.getString("os-vol-tenant-attr:tenant_id"));
	}

	private List<Snapshot> list(RestTokenBean restTokenBean, String url)
			throws AppException {
		List<Snapshot> list = null;
		restTokenBean.setUrl(url);
		List<JSONObject> result = restService.list(restTokenBean, "snapshots");

		if (result != null && result.size() > 0) {
			for (JSONObject jsonObject : result) {
				if (list == null) {
					list = new ArrayList<Snapshot>();
				}
				Snapshot snapshot = restService.json2bean(jsonObject,
						Snapshot.class);
				initData(snapshot, jsonObject);
				snapshot.setCreated_at(snapshot.getCreated_at()
						.replace('T', ' ').substring(0, 19));
				snapshot.setTenant_id(restTokenBean.getTenantId());
				list.add(snapshot);
			}
		}
		return list;
	}

	/**
	 * 获取指定数据中心的指定项目下的云资源列表
	 */
	public List<Snapshot> list(String datacenterId, String projectId)
			throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.VOLUME_SERVICE_URI);
		return list(restTokenBean, "/snapshots");
	}

	/**
	 * 获取指定数据中心下的所有项目的云资源总和的列表
	 */
	public List<Snapshot> listAll(String datacenterId) throws AppException {
		List<Snapshot> list = null;
		List<CloudProject> projectList = projectService
				.getProjectListByDataCenter(datacenterId);

		if (projectList != null && projectList.size() > 0) {
			if (list == null) {
				list = new ArrayList<Snapshot>();
			}
			RestTokenBean restTokenBean = null;
			for (BaseCloudProject cloudProject : projectList) {
				if (restTokenBean == null) {
					restTokenBean = getRestTokenBean(datacenterId,
							cloudProject.getProjectId(),
							OpenstackUriConstant.VOLUME_SERVICE_URI);
				} else {
					restTokenBean.setTenantId(cloudProject.getProjectId());
				}

				list.addAll(list(restTokenBean, "/snapshots"));
			}
		}

		return list;
	}

	public Snapshot getById(String datacenterId, String projectId, String id)
			throws AppException {
		Snapshot result = null;

		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.VOLUME_SERVICE_URI);
		JSONObject json = restService.getById(restTokenBean, "/volumes/",
				OpenstackUriConstant.DISK_DATA_NAME, id);
		if (json != null) {
			result = restService.json2bean(json, Snapshot.class);
			initData(result, json);
		}

		return result;
	}

	public Snapshot create(String datacenterId, String projectId,
			JSONObject data) throws AppException {
		Snapshot Snapshot = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.VOLUME_SERVICE_URI);
		restTokenBean.setUrl("/snapshots");

		JSONObject result = restService.create(restTokenBean, "snapshot", data);
		Snapshot = restService.json2bean(result, Snapshot.class);

		return Snapshot;
	}

	public boolean delete(String datacenterId, String projectId, String id)
			throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.VOLUME_SERVICE_URI);
		restTokenBean.setUrl("/snapshots/" + id);
		return restService.delete(restTokenBean);
	}

	public Snapshot update(String datacenterId, String projectId,
			JSONObject data, String id) throws AppException {
		Snapshot Snapshot = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.VOLUME_SERVICE_URI);
		restTokenBean.setUrl("/snapshots/" + id);
		JSONObject result = restService.update(restTokenBean, "snapshot", data);
		Snapshot = restService.json2bean(result, Snapshot.class);
		return Snapshot;
	}

	public boolean bind(String datacenterId, String projectId, String id,
			String vmId) throws AppException {
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("SnapshotId", id);
		data.put("SnapshotAttachment", temp);

		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		restTokenBean.setUrl("/servers/" + vmId + "/os-Snapshot_attachments");
		JSONObject result = restService.create(restTokenBean, null, data);
		return true;

	}

	public boolean debind(String datacenterId, String projectId, String id,
			String vmId) throws AppException {
		// 根据操作类型设置request body 对象
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("SnapshotId", id);
		data.put("SnapshotAttachment", temp);

		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		restTokenBean.setUrl("/servers/" + vmId + "/os-Snapshot_attachments/"
				+ id);

		boolean result = restService.delete(restTokenBean);
		return result;
	}
	
	public JSONObject get(String dcId,String prjId,String snapId) throws Exception{
		RestTokenBean restTokenBean = getRestTokenBean(dcId, prjId,
				OpenstackUriConstant.VOLUME_SERVICE_URI);
		return restService.getJSONById(restTokenBean, "/snapshots/", snapId);
	}
	
	/**                                                                                                         
	 * 获取底层数据中心下的云硬盘快照                                                               
	 * -----------------                                                                                      
	 * @author zhouhaitao                                                                                     
	 * @param dataCenter                                                                                      
	 *                                                                                                        
	 * @return                                                                                                
	 *                                                                                                        
	 */                                                                                                       
	public List<BaseCloudSnapshot> getStackList(BaseDcDataCenter dataCenter,String prjId) {                                  
		List <BaseCloudSnapshot> list = new ArrayList<BaseCloudSnapshot>();                                         
		RestTokenBean restTokenBean = getRestTokenBean(dataCenter.getId(), prjId,                                             
				OpenstackUriConstant.VOLUME_SERVICE_URI);                                                          
		restTokenBean.setUrl("/snapshots");                                           
		List<JSONObject> result = restService.list(restTokenBean,"snapshots");
                                                                                                            
		if (result != null && result.size() > 0) {                                                              
			for (JSONObject jsonObject : result) {                                                                
				 Snapshot data = restService.json2bean(jsonObject,                                             
						 Snapshot.class);                                                                            
				 BaseCloudSnapshot ccn=new BaseCloudSnapshot(data,dataCenter.getId(),prjId);                                 
					list.add(ccn);                                                                                    
				}                                                                                                   
			}                                                                                                     
		return list;                                                                                            
	}                                                                                                         
}
