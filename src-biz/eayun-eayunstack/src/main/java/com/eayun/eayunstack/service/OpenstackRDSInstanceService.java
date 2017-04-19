package com.eayun.eayunstack.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.database.instance.model.CloudRDSInstance;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.RDSInstance;
import com.eayun.eayunstack.util.RestTokenBean;

public interface OpenstackRDSInstanceService extends OpenstackBaseService<RDSInstance>{
	
	List<RDSInstance> list(RestTokenBean restTokenBean, String url) throws AppException ;
	
	List<RDSInstance> list(String datacenterId, String projectId) throws AppException ;
	
	List<RDSInstance> listAll(String datacenterId) throws AppException ;
	
	String createRdsInstance(CloudRDSInstance cloudRdsInstance, RDSInstance rdsInstance) throws AppException ;
	
	void resizeVolume(String datacenterId, String projectId, String id, int size) throws AppException ;
	
	void resizeFlavor(String datacenterId, String projectId, String id, String flavorId) throws AppException ;
	
	void restartRdsInstance(String datacenterId, String projectId, String id) throws AppException ;
	
	void detachReplica(String datacenterId, String projectId, String id) throws AppException ;
	
	void edit(String datacenterId, String projectId, String id, String name) throws AppException ;
	
	List<RDSInstance> getStackList(BaseDcDataCenter dataCenter,String projectId) throws AppException ;

	JSONObject get(String datacenterId, String projectId, String id) throws Exception;

	JSONObject resetStatus(String datacenterId, String id) throws  Exception;
}
