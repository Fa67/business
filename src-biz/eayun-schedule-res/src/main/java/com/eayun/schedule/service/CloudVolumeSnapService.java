package com.eayun.schedule.service;


import com.alibaba.fastjson.JSONObject;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.virtualization.model.CloudSnapshot;

public interface CloudVolumeSnapService {
	
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
	 * 删除云硬盘备份
	 * @param cloudVm
	 * @return
	 */
	public boolean deleteVolSnap(CloudSnapshot cloudSnp);
	
	/**
	 * 修改云硬盘备份信息
	 * @param cloudVm
	 * @return
	 */
	public boolean  updateVolSnap(CloudSnapshot cloudSnp);
	
	
	
	/**
	 * 修改云硬盘备份信息
	 * @param cloudVm
	 * @return
	 * @新增使用
	 */
	public boolean  syncSnapshotInBuild(CloudSnapshot cloudSnapshot);
	
	/**
	 * 同步底层项目下的云硬盘备份
	 * -----------------
	 * @param dataCenter
	 * @param prjId
	 */
	public void synchData(BaseDcDataCenter dataCenter) throws Exception;
	
}
