package com.eayun.schedule.service;


import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.virtualization.model.BaseCloudVolumeType;
import com.eayun.virtualization.model.CloudVolumeType;

public interface CloudVolumeTypeService {
	
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
	 * 删除云硬盘类型
	 * @param 
	 * @return
	 */
	public boolean deleteVolumeType(CloudVolumeType cloudVolumeType);
	
	/**
	 * 修改云硬盘类型信息
	 * @param 
	 * @return
	 */
	public boolean  updateVolumeType(CloudVolumeType cloudVolumeType);
	
	
	
	
	/**
	 * 同步底层数据中心下的云硬盘类型
	 * -----------------
	 * @param dataCenter
	 * @param 
	 */
	public void synchData(BaseDcDataCenter dataCenter) throws Exception;
	
	
	/**
	 * 获取指定数据中心下有效云硬盘类型
	 */
	public List<BaseCloudVolumeType> getVolumeTypesByDcId(String dcId)throws Exception;
	
}
