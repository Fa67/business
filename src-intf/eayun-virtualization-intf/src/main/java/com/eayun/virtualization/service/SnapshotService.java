package com.eayun.virtualization.service;

import java.util.List;

import com.eayun.charge.bean.ResourceCheckBean;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.virtualization.model.BaseCloudSnapshot;
import com.eayun.virtualization.model.CloudOrderSnapshot;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.model.CloudSnapshot;

public interface SnapshotService {

	
	//获取备份列表
	public Page getSnapshotList(Page page, String prjId, String dcId, String snapName,String isDeleted,
			QueryMap queryMap)throws Exception;
	
	//购买云硬盘备份
	public String buySnapshot(CloudOrderSnapshot orderSnap,SessionUserInfo sessionUser)throws Exception;
	
	//新增备份的方法
	public void addVolumeBack(String orderNo,SessionUserInfo sessionUser)throws AppException;
	
    //删除备份的方法
	public boolean deleteSnapshot(CloudSnapshot snap,SessionUserInfo sessionUser)throws AppException;
	
    //更新备份的方法
	public boolean updateSnapshot(CloudSnapshot snap)throws AppException;
	
    //验证备份重名
	public boolean getSnapByName(CloudSnapshot snap)throws AppException;
	
    //获取指定云硬盘创建的备份
	public List<CloudSnapshot> getSnapListByVolId(String volId)throws Exception;
	
    //获取指定项目下备份数量
	public int countSnapshotByPrjId(String prjId)throws AppException;
	
	//删除数据库备份方法
	public boolean deleteSnap (CloudSnapshot cloudSnapshot);
	
	//更新数据库备份方法
	public boolean updateSnap (CloudSnapshot cloudSnapshot);
	
	//根据云硬盘id删除所有根据此云主机创建的备份
	public void deleteAllSnaps(String volId,String isDeleted,SessionUserInfo user)throws AppException;
	
	//回滚云硬盘
	public void rollBackVolume(CloudSnapshot snapshot,SessionUserInfo sessionUser)throws AppException;
	
	//更改备份状态  如余额不足等
	public void modifyStateForSnap(String snapId, String chargeState,boolean isClose) throws Exception;
	
    //获取指定备份的名称
	String getSnapshotNameById(String id) throws Exception;
	
	//从回收站恢复云硬盘备份
	public boolean recoverSnapshot(String snapId,SessionUserInfo sessionUser)throws Exception;
	
	//根据条件查询云硬盘备份
	public List<CloudSnapshot> getSnapshotsBySome(String prjId,String isDeleted,String payType,String chargeState)throws Exception;
	
	//查询指定项目下的备份配额和配额使用情况
	public CloudProject queryProjectQuotaAndUsed(String prjId);
	
	//创建失败调用接口
	public void snapOrderFail(BaseCloudSnapshot snap,CloudOrderSnapshot snapOrder);
	
	//订单完成接口
	public void snapOrderSuccess (CloudOrderSnapshot orderSnap,BaseCloudSnapshot baseSnapshot) throws Exception;
	
	//查询回收站中的备份列表
	public Page getRecycleSnapList(Page page, ParamsMap map,SessionUserInfo sessionUser, QueryMap queryMap)throws Exception;
	
	//根据订单号查询cloudOrderSnapshot
	public CloudOrderSnapshot queryCloudOrderByOrderNo(String orderNo)throws Exception;
	
    //根据备份Id查询备份
	public CloudSnapshot getSnapshotById(String snapId);
	
	//根据资源id查询指定资源是否存在
    public ResourceCheckBean  isExistsByResourceId(String resourceId);
	
	
    /**
     * 查询已在回收站过期的云硬盘备份列表
     * 
     * @author zhouhaitao
     * @param seconds
     * @return
     */
    public List<CloudSnapshot> queryRecycleSnapshotList(long seconds);
	



}
