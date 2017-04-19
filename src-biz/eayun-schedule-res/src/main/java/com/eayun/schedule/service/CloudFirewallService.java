package com.eayun.schedule.service;


import com.alibaba.fastjson.JSONObject;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.virtualization.model.CloudFireWall;

public interface CloudFirewallService {
	
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
	 * 修改防火墙信息
	 * @param cloudVm
	 * @return
	 */
	public boolean  updateFw(CloudFireWall cloudFw) throws Exception;
	
	/**
	 * 同步底层数据中心下的防火墙
	 * -----------------
	 * @param dataCenter
	 * @param prjId
	 */
	public void synchData(BaseDcDataCenter dataCenter) throws Exception;
	
	/**
	 * 删除底层不存在的防火墙
	 * @param cloudFw
	 * @return
	 */
	public boolean deleteFw (CloudFireWall cloudFw);
	
}
