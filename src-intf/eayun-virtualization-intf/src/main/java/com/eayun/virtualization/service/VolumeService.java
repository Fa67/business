package com.eayun.virtualization.service;

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
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.virtualization.model.BaseCloudVolume;
import com.eayun.virtualization.model.CloudOrderVm;
import com.eayun.virtualization.model.CloudOrderVolume;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.model.CloudVolume;

public interface VolumeService {
    //查询云硬盘列表
	public Page getVolumeList(Page page, String projectId,String datacenterId,String queryName,String queryType,String isDeleted,String volStatus,QueryMap queryMap)throws Exception;

	//创建云硬盘
	public List<BaseCloudVolume> addVolume(CloudOrderVolume orderVolume)throws AppException;

	//删除云硬盘
	public boolean deleteVolume(CloudVolume vol,SessionUserInfo user)throws AppException;

	//更新云硬盘
	public boolean updateVolume(CloudVolume vol)throws AppException;

	//查看云硬盘详情
	public CloudVolume getVolumeById(String dcId, String prjId, String volId)throws Exception;

	//挂载云硬盘
	public boolean bindVolume(CloudVolume vol)throws AppException;

	//解绑云硬盘
	public boolean debindVolume(CloudVolume vol)throws AppException;

	//验证云硬盘重名
	public boolean getVolumeByName(CloudVolume cloudVolume)throws Exception;
	
	//查询指定云主机下挂载的云硬盘个数
	public int getCountByVnId(String vmId)throws AppException;
	
	//查询指定云主机下挂载的云硬盘
	public List<CloudVolume> queryVolumesByVm(String vmId)throws AppException;
	
	//查询指定项目下云硬盘的数量
	public int CountVolByPrjId(String prjId)throws AppException;
	
	//更新云硬盘数据库方法
	public boolean updateVol(CloudVolume cloudVolume);

	public List<BaseCloudVolume> getStackList(BaseDcDataCenter dataCenter,String prjId) throws Exception ;
	
	public void insertVolumeDB(BaseCloudVolume cloudVolume);
	
	public void deleteVolumeByVm(String vmId,String deleteUser);

	//获取指定项目下可被挂载的云硬盘
	public List<CloudVolume> getUnUsedVolumeList(String prjId, String dcId)throws Exception;

	//购买云硬盘
	public String buyVolumes(CloudOrderVolume orderVolume,SessionUserInfo sessionUser)throws Exception;

	//获取指定云硬盘名称
	String getVolumeNameById(String id) throws Exception;
	
	//修改资源状态
	public void	modifyStateForVol(String volId,String chargeState,boolean isDebind)throws AppException;
	
	//修改资源状态
	public void modifyStateForVol(String volId,String chargeState,Date endTime,boolean isDebind)throws Exception;
	
	//扩容生成订单
	public String extendVolume(CloudOrderVolume orderVolume,SessionUserInfo sessionUser)throws Exception;
	
	//扩容接口
	public boolean largeVolume(CloudOrderVolume orderVol)throws  Exception;
	
	//根据云主机id 解绑其下的数据盘
	public boolean debindVolsByVmId(String vmId)throws AppException;
	
	//从回收站恢复云硬盘
	public boolean recoverVolume(String volId,SessionUserInfo sessionUser)throws AppException;
	
	//查询指定云硬盘是否有为处理的续费或扩容的订单
	public boolean checkVolOrderExsit(String volId)throws Exception;
	
	//查询配额
	public String checkVolumeQuota (CloudOrderVolume orderVolume)throws Exception;
	
	//查询某条云主机的系统盘
	public CloudVolume getOsVolumeByVmId(String vmId)throws Exception;
	
	//综合查询云硬盘
    public List<CloudVolume> getVolumesBySome(Date endTime,String chargeState,String isDeleted,boolean isInRecycle,String payType,String prjId,String volBootable)throws Exception;
    
    //云硬盘失败，删除整单
    public void volOrderFail(List<BaseCloudVolume>  list,CloudOrderVolume volOrder);
    
    //云硬盘创建成功，将本订单所有的云硬盘置为可见
    public void modifyVolVisableByOrder (String orderNo);
    
    //订单完成后调用
    public void volOrderSuccess (CloudOrderVolume orderVolume,List<BaseCloudVolume> volList) throws Exception;
    
    //查询指定项目下的云硬盘配额和配额使用量
    public CloudProject queryProjectQuotaAndUsed(String prjId);
    /**
     * 云硬盘续费，提交按钮校验当前主机是否有未完成订单，以及当前账户金额是否充足与提交订单	
     */
    public JSONObject renewVolumeOrderConfirm(Map<String ,String> map,String userId,String userName,String cusId)throws Exception;

	//查询回收站中的云硬盘列表
	public Page getRecycleVolList(Page page,ParamsMap map,SessionUserInfo sessionUser, QueryMap queryMap)throws Exception;
	
	//根据备份创建云硬盘
	public boolean addVolumeBySnapshot(CloudOrderVolume volumeOrder)throws AppException;

	//根据订单编号查询订单信息
	public CloudOrderVolume queryCloudOrderByOrderNo(String orderNo);

	//根据云硬盘ID查询云硬盘的计费队列需要的信息
	public CloudVolume queryVolChargeById(String volId);
	
	//查询该订单对应的升级或续费资源是否存在
	public boolean isExistsByOrderNo(String orderNo);
	
	//根据资源id查询指定资源是否存在
	public ResourceCheckBean  isExistsByResourceId(String resourceId);
	
	/**
	 * 查询已在回收站过期的云硬盘列表
	 * 
	 * @author zhouhaitao
	 * @param seconds
	 * @return
	 */
	public List<CloudVolume> queryRecycleVolumeList(long seconds);
	
	/**
	 * 刷新云主机对应的数据盘状态
	 * @param vmId 云主机ID
	 */
	public void deleteDataVolumeByVm(String vmId);
	
	/**
	 * 购买云主机时同时购买数据盘
	 * @param volOrder
	 * @return
	 * @throws AppException
	 */
	public List<BaseCloudVolume> addVmsAndVolumes(CloudOrderVolume volOrder,List<BaseCloudVolume> dataVolumeList)throws AppException;
	
	
	/**
	 * 购买云主机时同时也购买数据盘出错，删除云硬盘
	 * @param vol
	 * @return
	 * @throws Exception
	 */
	public void volumeFailedHandler(CloudOrderVm order);
	
	/**
	 * 后付费发送计费信息
	 * @param orderVolume
	 * @param vol
	 */
	public void volStartCharge (CloudOrderVolume orderVolume ,BaseCloudVolume vol);

	/**
	 * 查询指定云主机的系统盘信息
	 * @param vmId
	 * @return
	 */
	public CloudVolume getSysVolumeByVmId(String vmId);
}
