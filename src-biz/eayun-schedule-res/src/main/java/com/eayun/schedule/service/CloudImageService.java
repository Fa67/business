package com.eayun.schedule.service;


import com.alibaba.fastjson.JSONObject;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.virtualization.model.CloudImage;

public interface CloudImageService {
	
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
	 * 删除自定义镜像
	 * @param cloudImage
	 * @return
	 */
	public boolean deleteImage(CloudImage cloudImage)throws Exception;
	
	/**
	 * 修改自定义镜像信息
	 * @param cloudImage
	 * @return
	 */
	public boolean  updateImage(CloudImage cloudImage)throws Exception;
	
	/**
	 * 同步底层数据中心下的镜像
	 * -----------------
	 * @param dataCenter
	 * @param prjId
	 */
	public void synchData(BaseDcDataCenter dataCenter) throws Exception;
	
}
