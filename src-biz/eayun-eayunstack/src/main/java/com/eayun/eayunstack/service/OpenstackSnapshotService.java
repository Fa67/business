package com.eayun.eayunstack.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.Snapshot;
import com.eayun.virtualization.model.BaseCloudSnapshot;

public interface OpenstackSnapshotService extends
		OpenstackBaseService<Snapshot> {

	public boolean bind(String datacenterId, String projectId, String id,
			String vmId) throws AppException;

	public boolean debind(String datacenterId, String projectId, String id,
			String vmId) throws AppException;
	
	public JSONObject get (String dcId,String prjId,String fwId) throws Exception;
	
	/**                                                        
	 * 查询底层 数据中心的云资源                             
	 * -------------------                                   
	 * @author zhouhaitao                                    
	 * @param dataCenter                                     
	 * @return                                               
	 */                                                      
	public List<BaseCloudSnapshot> getStackList (BaseDcDataCenter dataCenter,String prjId);

}
