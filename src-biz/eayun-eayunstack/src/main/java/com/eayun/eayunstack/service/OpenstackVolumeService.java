package com.eayun.eayunstack.service;

import java.util.List;

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

public interface OpenstackVolumeService extends OpenstackBaseService<Volume> {

	public boolean bind(String datacenterId, String projectId, String id,
			String vmId) throws AppException;

	public boolean debind(String datacenterId, String projectId, String id,
			String vmId) throws AppException;
	public boolean extendVolume(String datacenterId, String projectId, String volId,
			JSONObject data) throws AppException;
	public BackUp createBackUps(String datacenterId, String projectId, JSONObject data)
			throws AppException;
	public boolean deleteBackUps(String datacenterId, String projectId, String id)
			throws AppException;
	public Restore restoreVolume(String datacenterId, String projectId,String snapId,JSONObject data)
			throws AppException;
	public List<BackUp> getAllBackUps(String datacenterId) throws AppException;
	public BackUp getBackUp(String datacenterId, String projectId,String snapId) throws AppException;
	public boolean resetVolStatus(String datacenterId, String projectId,String volId,JSONObject data) throws AppException;
	public List<Vm> vmList(String datacenterId, String projectId)
			throws AppException;
	
	public JSONObject get (String dcId,String prjId,String volId) throws Exception;

    public List<VolumeType> getVolumeTypes(String datacenterId)throws AppException;
	
	public List<VolumeQos> getAllVolumeQos(String datacenterId)throws AppException;
	
	public List<QosAssociation> getAllAssociationsForQoS(String datacenterId,String qosId)throws AppException;
	
	public boolean setQosKeys(String datacenterId,String qosId,JSONObject data)throws AppException;
	
	public boolean retype(String dcId,String prjId,String volId,JSONObject data)throws AppException;
	
	/**                                                        
	 * 查询底层 数据中心的云资源                             
	 * -------------------                                   
	 * @author zhouhaitao                                    
	 * @param dataCenter                                     
	 * @return                                               
	 */                                                      
	public List<JSONObject> getStackList (BaseDcDataCenter dataCenter,String prjId);
	
	public <T> T json2bean(JSONObject jSONObject, Class<T> clazz);

}
