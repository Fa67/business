package com.eayun.virtualization.ecmcservice;

import java.util.List;
import java.util.Map;

import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.ecmcuser.model.BaseEcmcSysUser;
import com.eayun.virtualization.model.BaseCloudVolume;
import com.eayun.virtualization.model.CloudVolume;

public interface EcmcCloudVolumeService {
	
	
	
	//根据项目查询云硬盘使用量
	public int getCountByPrjId(String prjId);
	
	//查询云硬盘列表
	public Page getVolumeList(Page page, String prjId, String dcId,
			String queryName, String queryType, QueryMap queryMap) throws Exception;
	
	//查询云硬盘详情
	public CloudVolume getVolumeById(String volId)throws Exception;
	
	//验证重名
	public boolean getVolumeByName(Map<String, String> map)throws Exception;

	//创建云硬盘
	public BaseCloudVolume createVolume(String dcId, String prjId,
			String createName, String from, String volName, int size,
			String description)throws AppException;
	
	public List<CloudVolume> getUnBindDisk(String prjId);
	
	//删除云硬盘
	public boolean deleteVolume(CloudVolume vol, BaseEcmcSysUser user)throws AppException;

	//更新云硬盘
	public boolean updateVolume(CloudVolume vol)throws AppException;

	//挂载云硬盘
	public boolean bindVolume(String dcId, String prjId, String vmId,
			String volId)throws AppException;

	//解绑云硬盘
	public boolean debindVolume(String dcId, String prjId, String vmId,
			String volId)throws AppException;

	//查询指定云主机下挂载的云硬盘
	public List<CloudVolume> queryVolumesByVm(String vmId)throws Exception;

	//查询指定云主机下挂载的云硬盘数量
	public int getCountByVnId(String vmId)throws Exception;

	//创建系统盘
	public void insertVolumeDB(BaseCloudVolume cloudVolume);
	
	//删除云主机时删除系统盘
	public void deleteVolumeByVm(String vmId,String deleteUser) throws Exception;
	
	
	public boolean updateVol(CloudVolume cloudVolume);
	
	//解绑指定云主机下所有数据盘
	public boolean debindVolsByVmId(String vmId) throws AppException;
	/**
	 * 查询当前登录用户下回收站的volume
	 * @author liuzhuangzhuang
	 * @param page
	 * @param map
	 * @param user
	 * @param queryMap
	 * @return
	 */
	public Page getRecycleVolList(Page page, ParamsMap map, BaseEcmcSysUser user, QueryMap queryMap); 
	
	/**
	 * 根据项目查询云硬盘使用量(数量)，包括订单中待创建的配额数
	 * @param prjId
	 * @return
	 */
	public int getUsedVolumeCountByPrjId(String prjId);
	/**
	 * 根据项目查询云硬盘使用量(容量)，包括订单中待创建的配额数
	 * @param prjId
	 * @return
	 */
	public int getUsedVolumeCapacityByPrjId(String prjId);
}
