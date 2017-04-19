package com.eayun.database.instance.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.eayun.database.instance.model.CloudRDSInstance;
import com.eayun.datacenter.model.BaseDcDataCenter;

public interface CloudRDSInstanceService {

	 long size(String groupKey);
	
	 boolean push(String groupKey,String value);

	 String pop(String groupKey);

	 boolean syncRDSInstanceInBuild(CloudRDSInstance cloudRDSInstance) throws Exception;

	 JSONObject get(JSONObject valueJson) throws Exception;

	 void updateRdsInstance(CloudRDSInstance cloudRDSInstance, String status, boolean isConfig);

	 void deleteRdsInstance(CloudRDSInstance cloudRDSInstance) throws Exception ;
	
	 void resizeRdsInstanceVolume(CloudRDSInstance cloudRDSInstance, String status) throws Exception ;

	 void upgradeSuccess(CloudRDSInstance cloudRDSInstance, String status) throws Exception;

	 void rebootSuccessForDetach(CloudRDSInstance cloudRDSInstance) throws Exception;
	
	 void synchData(BaseDcDataCenter dataCenter, String prjId) throws Exception;
	 
	 /**
	  * 根据项目id获取未删除的实例列表
	  * 用于计划任务获取磁盘使用率
	  * @Author: duanbinbin
	  * @param projectId
	  * @return
	  *<li>Date: 2017年3月7日</li>
	  */
	 public List<CloudRDSInstance> getRDSListByPrjId(String projectId);

	void detachReplicaSuccess(CloudRDSInstance cloudRDSInstance, String status) throws Exception ;

	void deleteBuildInstance(CloudRDSInstance cloudRDSInstance) throws Exception;
}
