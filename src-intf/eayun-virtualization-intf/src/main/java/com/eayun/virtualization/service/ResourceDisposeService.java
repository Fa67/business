package com.eayun.virtualization.service;

import java.util.Date;
import java.util.List;

import com.eayun.virtualization.bean.AboutToExpire;

public interface ResourceDisposeService {
	/**
	 * 云资源到期处理
	 * @throws Exception
	 */
	public void resourceExpiration() throws Exception;
	
	/**
	 * 云资源过期处理
	 * @throws Exception
	 */
	public void resourceExceed() throws Exception;
	/**
	 * 云资源欠费处理(在保留时长内)
	 * @throws Exception
	 */
	public void inRententionTime(String cusId) throws Exception;
	/**
	 * 云资源欠费超时处理(超过保留时长)
	 * @throws Exception
	 */
	public void outRententionTime(String cusId,Date time) throws Exception;
	/**
	 * 根据到期时间获取资源信息
	 * @param cusId
	 * @param prjId
	 * @param threeDay
	 * @return
	 * @throws Exception
	 */
	public List<AboutToExpire> getExpireResources(String cusId,Date threeDay,String chargeState) throws Exception;
	/**
	 * 获取即将被停用的资源数量 后付费,服务状态为正常变余额不足
	 * @param cusId
	 * @return
	 * @throws Exception
	 */
	public int getWillStopResourceCount(String cusId,Date date);
	/**
	 * 发送按需付费资源停用（未停用）消息  达到信用额度时发送
	 * @param cusId
	 */
	public void sendMessageForReachCreditlimit(String cusId,Date time) throws Exception;
}
