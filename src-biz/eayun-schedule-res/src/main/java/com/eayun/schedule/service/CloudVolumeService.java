package com.eayun.schedule.service;


import com.alibaba.fastjson.JSONObject;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.virtualization.model.BaseCloudVolume;
import com.eayun.virtualization.model.CloudVolume;

public interface CloudVolumeService {
	
	/**
	 * 从消息队列取出对应key的的其中一条
	 * @param groupKey
	 * @return
	 */
	public String pop(String groupKey);
	
	/**
	 * 查询当前队列的长度
	 * @param groupKey
	 * @return
	 */
	public long size (String groupKey);
	
	/**
	 * 重新放入队列
	 * @param groupKey
	 * @param value
	 * @return
	 */
	public	boolean push(String groupKey,String value);
	
	/**
	 * 获取底层指定ID的资源<br>
	 * ------------------
	 * @author zhouhaitao
	 * 
	 * @param value
	 * @param json
	 * 
	 * @return
	 */
	public JSONObject get(JSONObject valueJson) throws Exception;
	
	/**
	 * 修改云硬盘信息
	 * @param cloudVm
	 * @return
	 */
	public boolean  updateVol(CloudVolume cloudVol);
	
	/**
	 * 同步底层项目下的云硬盘
	 * -----------------
	 * @param dataCenter
	 * @param prjId
	 */
	public void synchData(BaseDcDataCenter dataCenter,String prjId) throws Exception;
	
	/**
	 * 删除底层不存在的云硬盘
	 * @param cloudVolume
	 * @return
	 */
	public boolean deleteVol(CloudVolume cloudVolume);
	
	/**
	 * 删除底层不存在的云硬盘（只有状态）
	 * @param cloudVolume
	 * @return
	 */
	public boolean updateDeteleStatus(BaseCloudVolume cloudVolume);
	
	/**
	 * 刷新订单中云硬盘状态
	 * @param cloudVolume
	 * @return
	 * @throws Exception
	 */
	public boolean syncVolumeInBuild(CloudVolume cloudVolume) throws Exception;
	
	/**
	 * 刷新订单中备份创建云硬盘状态
	 * @param cloudVolume
	 * @return
	 * @throws Exception
	 */
	public boolean syncVolumeByBackUpInBuild(CloudVolume cloudVolume) throws Exception;

	/**
	 * 刷新挂载状态
	 * 如果云硬盘不为in-use状态
	 * 将数据库中的vm_id设为null
	 * @param cloudVolume
	 */
	public boolean updateBindVol(CloudVolume cloudVolume);
	
	
	/**
	 * 各项目下的云硬盘retype
	 * -----------------
	 * @param dataCenter
	 * @param prjId
	 */
	public void synchVolumeRetype(BaseDcDataCenter dataCenter,String prjId) throws Exception;
	
}
