package com.eayun.database.instance.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.eayun.charge.bean.ResourceCheckBean;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.database.instance.model.BaseCloudRDSInstance;
import com.eayun.database.instance.model.CloudOrderRDSInstance;
import com.eayun.database.instance.model.CloudRDSInstance;
import com.eayun.price.bean.ParamBean;
import com.eayun.price.bean.PriceDetails;

/**
 * ECSC云数据库实例相关操作service接口
 *                       
 * @Filename: EcscRDSInstanceService.java
 * @Description: 
 * @Version: 1.0
 * @Author: LiuZhuangzhuang
 * @Email: zhuangzhuang.liu@eayun.com
 * @History:<br>
 *<li>Date: 2017年2月22日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public interface RDSInstanceService {
	
	int getMasterCountByPrjId(String prjId);
	
	int getSlaveCountByPrjId(String prjId);
	
	String buyRDSInstance(CloudOrderRDSInstance cloudOrder, SessionUserInfo user) throws Exception;
	
	void createRdsInstance(CloudOrderRDSInstance cloudOrder) throws AppException;
	
	void createFailHandler(CloudOrderRDSInstance cloudOrder, BaseCloudRDSInstance succRdsInstance) 
			throws Exception;
	
	void enableRootAndlog(CloudOrderRDSInstance cloudOrder, BaseCloudRDSInstance succRdsInstance) 
			throws Exception;
	
	void createSuccessRDSInstance(CloudOrderRDSInstance cloudOrder, BaseCloudRDSInstance succRdsInstance) throws Exception;
	
	JSONObject renewRdsOrderConfirm(Map<String ,String> map, SessionUserInfo sessionUser)throws Exception;
	
	void setRdsInstanceStatusToBackup(String masterId) ;
	
	EayunResponseJson deleteRdsInstance(CloudRDSInstance cloudRdsInstance, SessionUserInfo sessionUser)
			throws Exception;

	Page getList(Page page, ParamsMap map, SessionUserInfo sessionUser, QueryMap queryMap) throws Exception;

	CloudRDSInstance getRdsById(String rdsId) throws Exception;
	
	void modifyStateForRdsInstance(String rdsId, String resourceState, Date endTime, boolean isRestict, boolean isResumable) throws Exception;
	
	CloudRDSInstance findRDSInstanceByRdsId(String rdsId);
	
	void modifyRdsInstance(CloudRDSInstance cloudRDSInstance) throws AppException;
	
	void restart(CloudRDSInstance cloudRDSInstance) throws AppException;
	
	void detachReplica(CloudRDSInstance cloudRDSInstance) throws AppException;
	
	String resizeRdsInstance(CloudOrderRDSInstance cloudOrder ,SessionUserInfo user) throws Exception;
	
	void upgradeFailHandler(CloudRDSInstance rdsInstance, boolean isRollBack) throws Exception ;
	
	void upgradeSuccessHandler(CloudRDSInstance cloudRdsInstance) throws Exception ;

	EayunResponseJson modifyRdsInstanceConfiguraion(CloudRDSInstance cloudRDSInstance) throws AppException ;
	
	void rdsInstanceAttachConfiguration(CloudRDSInstance cloudRDSInstance) throws AppException ;

	boolean checkRdsNameExist(String rdsId, String rdsName, String prjId);

	void resizeRdsInstanceVolume(CloudRDSInstance cloudRDSInstance, boolean isNeedRollback) throws Exception;
	
	ResourceCheckBean isExistsByResourceId(String rdsId);
	
	boolean isMasterRdsInstance(String rdsId);
	
	List<CloudRDSInstance> queryNormalRdsInstance(String prjId,Date endTime,String state, String payType,String cusState) throws Exception;

	PriceDetails getPriceDetails(ParamBean paramBean);

	List<Map<String, String>> getVersionList(String dcId);
	
	EayunResponseJson getConfigList(String prjId, String versionId);
	
	void resizeRdsInstance(CloudOrderRDSInstance order) throws Exception ;
	
	String checkInstanceQuota(CloudOrderRDSInstance cloudOrder);
	
	CloudRDSInstance queryRdsInstanceChargeById(String rdsId);
	
	boolean checkRdsInstanceOrderExsit(String rdsId,boolean isResize,boolean isRenew) ;
	
	public CloudRDSInstance getInstanceByRdsId (String rdsId);

	CloudOrderRDSInstance getInstanceByOrderNo(String orderNo);
	
	public int getRdsInstanceCountByNetId (String netId);
	
	public int getRdsInstanceToBeCreatedByNetId (String netId);
	
	public int getRdsInstanceCountBySubnet (String subnetId);
	
	public int getRdsInstanceToBeCreatedBySubnet (String subnetId);

	Map<String,Integer> getStandardByRdsId(String rdsId);

	void updateRdsInstanceStatus(List<CloudRDSInstance> rdsList, String status);
}
