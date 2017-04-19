package com.eayun.virtualization.service;

import java.util.List;
import java.util.Map;

import com.eayun.common.model.EayunResponseJson;
import com.eayun.virtualization.model.BaseCloudSubNetWork;
import com.eayun.virtualization.model.CloudSubNetWork;

public interface SubNetWorkService {
	/**
	 * 获取相同项目下所有内部网络的子网络
	 * @param datacenterId  数据中心id
	 * @param projectId  项目id
	 * @return
	 */
	public List<CloudSubNetWork> getInnerNetList(String dcId,String prjId);
	public BaseCloudSubNetWork getSubNetworkById(String subNetId);
	public void saveOrUpdate(BaseCloudSubNetWork subNetWork);
	/**
	 * 获取路由绑定的子网
	 * @param datacenterId
	 * @param routeid
	 * @return
	 */
	public List<CloudSubNetWork> getSubnetList(String dcId,String prjId,String routeId);
	/**
	 * 获取项目下所有内网的子网列表信息,用于资源池创建中的下拉框
	 * @param datacenterId  数据中心id
	 * @param projectId  项目id
	 * 
	 * @return
	 * @throws AppException
	 */
	public List<BaseCloudSubNetWork> querySubnetList(String datacenterId,String projectId);
	//------------------
	/**
	 * 验证子网地址重名
	 * @param netId
	 * @param netName
	 * @param dcId
	 * @return
	 */
	public boolean checkCidr(String netId,String cidr);
	/**
	 * 验证子网重名
	 * @param netId
	 * @param netName
	 * @param dcId
	 * @return
	 */
    public boolean checkSubNetName(String subnetId,String subNetName,String dcId);
    /**
     * 添加子网
     * @param cloudSubNetWork
     * @return
     */
    public CloudSubNetWork addSubNetWork(CloudSubNetWork cloudSubNetWork);
    /**
     * 编辑子网
     * @param cloudSubNetWork
     * @return
     */
    public CloudSubNetWork updateSubNetWork(CloudSubNetWork cloudSubNetWork);
    /**
     * 删除子网
     * @param cloudSubNetWork
     * @return
     */
    public boolean daleteCloudSubNet(CloudSubNetWork cloudSubNetWork);
    /**
     * 获取指定网络下的子网
     * @param netId
     * @return
     */
    public List<CloudSubNetWork> getSubNetListById(String netId,String prjId,String dcId);
    /**
     * 
     * @param prjId
     * @return
     */
    public int findSubNetCountByPrjId(String prjId);
    
    /**
     * 获取子网DNS
     * @param subnetId
     * @return
     */
    public String getSubnetDNS(String subnetId);
    /**
     * 获取绑定路由的受管子网 或 自管子网列表
     * @author gaoxiang
     * @param netId			私有网络id
     * @param subnetType	子网类型:1是受管子网,0是自管子网
     * @return
     */
    public List<BaseCloudSubNetWork> getSubNetListByType(String netId, String subnetType);
    /**
     * 获取受管子网列表
     * @author gaoxiang
     * @param netId
     * @return
     */
    public List<BaseCloudSubNetWork> getManagedSubnetList(String netId);
    /**
     * 检验是否存在被订单中云主机调用的受管子网
     * @author gaoxiang
     * @param subnetId
     * @return
     */
    public boolean checkSubnetExistForVmInOrder(String subnetId);
    /**
     * 通过子网id获取对应的私有网络id
     * @author gaoxiang
     * @param subnetId
     * @return
     */
    public BaseCloudSubNetWork getNetIdBySubnetId(String subnetId);
    /**
     * 判断子网是否允许删除
     * @param params
     * @return
     */
	public EayunResponseJson checkForDel(Map<String, String> map);
}
