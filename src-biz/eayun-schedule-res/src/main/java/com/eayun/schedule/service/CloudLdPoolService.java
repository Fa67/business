package com.eayun.schedule.service;


import com.alibaba.fastjson.JSONObject;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.virtualization.model.CloudLdPool;

public interface CloudLdPoolService {
	
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
	 * 修改负载均衡资源池信息
	 * @param cloudVm
	 * @return
	 */
	public boolean  updateLdp(CloudLdPool cloudLdp) throws Exception;
	
	/**
	 * 同步底层数据中心下的负载均衡资源池
	 * -----------------
	 * @param dataCenter
	 * @param prjId
	 */
	public void synchData(BaseDcDataCenter dataCenter) throws Exception;
	
	/**
	 * 删除底层不存在的负载均衡的资源池
	 * @param cloudPool
	 * @return
	 */
	public boolean deletePool (CloudLdPool cloudPool);
}
