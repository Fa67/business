package com.eayun.database.instance.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.database.instance.model.CloudOrderRDSInstance;
import com.eayun.database.instance.model.CloudRDSInstance;

/**
 * ECMC云数据库实例相关操作的service接口
 *                       
 * @Filename: EcmcCloudRDSInstanceService.java
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
public interface EcmcCloudRDSInstanceService {
	
	public int getMasterCountByPrjId(String prjId);
	
	public int getSlaveCountByPrjId(String prjId);
	
	/**
	 * @author zhouhaitao
	 * --------------------------<br>
	 * @desc:
	 * 统计数据中心的RDS使用情况 <br>
	 * 		CPU：CPU的已使用量<br>
	 * 		Ram: 内存的已使用量<br>
	 * 		Volume:硬盘容量的已使用量<br>
	 * 		Instances:已创建的RDS实力数量<br>
	 * 
	 * @param dcId 数据中心ID
	 * @return
	 */
	public Map<String,String> getRDSInstanceUsedInfoByDcId(String dcId);
	/**
	 * 查询数据库实例列表
	 * @author gaoxiang
	 * @param page
	 * @param paramsMap
	 * @return
	 * @throws Exception
	 */
	public Page getList(Page page, ParamsMap paramsMap) throws Exception;
	/**
	 * 获取数据库实例详情
	 * @author gaoxiang
	 * @param instanceId
	 * @return
	 */
	public CloudRDSInstance getInstanceById (String instanceId);
	/**
	 * 重启数据库实例
	 * @param instance
	 * @throws AppException
	 */
	public void restart (CloudRDSInstance instance) throws AppException;
	/**
	 * 校验主库或从库是否超过配额，如果超过，则返回超配信息，正常发则返回null
	 * @param cloudOrder
	 * @return
	 */
	public String checkInstanceQuota(CloudOrderRDSInstance cloudOrder);
	/**
	 * 从库升级为主库
	 * @param cloudRDSInstance
	 * @throws AppException
	 */
	public void detachReplica(CloudRDSInstance cloudRDSInstance) throws AppException;
	/**
	 * 删除数据库实例
	 * @param cloudRdsInstance
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	public EayunResponseJson deleteRdsInstance(CloudRDSInstance cloudRdsInstance, String userName) throws Exception;
	/**
	 * 校验重名
	 * @param rdsId
	 * @param rdsName
	 * @param prjId
	 * @return true:不重名,false:重名
	 */
	public boolean checkRdsNameExist(String rdsId, String rdsName, String prjId);
	/**
	 * 修改数据库实例名称或描述信息
	 * @param cloudRDSInstance
	 * @throws AppException
	 */
	public void modifyRdsInstance(CloudRDSInstance cloudRDSInstance) throws AppException;
	/**
	 * 获取配置文件列表
	 * @param prjId
	 * @param versionId
	 * @return
	 */
	public EayunResponseJson getConfigList(String prjId, String versionId);
	/**
	 * 修改数据库实例的配置文件
	 * @param cloudRDSInstance
	 * @throws AppException
	 */
	public EayunResponseJson modifyRdsInstanceConfiguraion(CloudRDSInstance cloudRDSInstance) throws AppException;
	/**
	 * 获取数据库版本列表
	 * @return
	 */
	public List<JSONObject> getAllDBVersion ();
	
	public int getRdsInstanceCountByNetId (String netId);
    public int getRdsInstanceToBeCreatedByNetId (String netId);
    
    public int getRdsInstanceCountBySubnet (String subnetId);
    
    public int getRdsInstanceToBeCreatedBySubnet (String subnetId);

	int getRdsInstanceCountByPrjId(String prjId);
}
