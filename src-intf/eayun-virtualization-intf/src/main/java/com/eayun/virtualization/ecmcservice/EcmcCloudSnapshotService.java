package com.eayun.virtualization.ecmcservice;

import java.util.List;

import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.ecmcuser.model.BaseEcmcSysUser;
import com.eayun.virtualization.model.BaseCloudSnapshot;
import com.eayun.virtualization.model.BaseCloudVolume;
import com.eayun.virtualization.model.CloudSnapshot;
import com.eayun.virtualization.model.CloudVolume;

public interface EcmcCloudSnapshotService {
	
	
	//根据项目查询备份使用量
	public int countSnapshotByPrjId(String prjId)throws Exception;
	
	//查询备份列表
	public Page getSnapshotList(Page page, String prjId, String dcId, String queryName,String queryType,
			QueryMap queryMap, String isDeleted)throws Exception;
	//创建快照
	public BaseCloudSnapshot addSnapshot(CloudSnapshot snap)throws AppException;
	//删除备份
	public boolean deleteSnapshot(CloudSnapshot snap, BaseEcmcSysUser user)throws AppException;
	//基于快照创建云硬盘
	public BaseCloudVolume addVolumeBySnapshot(CloudVolume vol, String createName)throws AppException;
	//编辑备份
	public boolean updateSnapshot(CloudSnapshot snap)throws AppException;
	//验证重名
	public boolean getSnapByName(CloudSnapshot snap)throws AppException;
	//查询指定云硬盘下的备份列表
	public List<CloudSnapshot> getSnapListByVolId(String volId)throws Exception;

	public boolean deleteSnap (CloudSnapshot cloudSnapshot);
	
	public boolean updateSnap (CloudSnapshot cloudSnapshot);

	/**
	 * 回滚云硬盘
	 * @param snapshot
	 * @param sessionUser
	 */
	public void rollBackVolume(CloudSnapshot snapshot) throws AppException;
	/**
	 * 根据云硬盘id删除所有根据此云硬盘创建的备份
	 * @author liuzhuangzhuang
	 * @param volId
	 * @param isDeleted
	 * @param user
	 * @throws AppException
	 */
	public void deleteAllSnaps(String volId, String cusId, String isDeleted, BaseEcmcSysUser user)throws AppException;
	/**
	 * 查询回收站中的备份列表
	 * @author liuzhuangzhuang
	 * @param page
	 * @param map
	 * @param user
	 * @param queryMap
	 * @return
	 */
	public Page getRecycleSnapList(Page page, ParamsMap map, BaseEcmcSysUser user, QueryMap queryMap);

	/**
	 * 根据项目查询备份使用量(数量)包括订单中待创建占用的配额数
	 * @param prjId
	 * @return
	 * @throws Exception
	 */
	public int getUsedSnapshotCount(String prjId)throws Exception;
	/**
	 * 根据项目查询备份使用量(容量)包括订单中待创建占用的配额数
	 * @param prjId
	 * @return
	 * @throws Exception
	 */
	public int getUsedSnapshotCapacity(String prjId)throws Exception;
}
