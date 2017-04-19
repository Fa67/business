package com.eayun.virtualization.service;

import com.eayun.virtualization.model.BaseCloudOrderNetWork;
import com.eayun.virtualization.model.CloudOrderNetWork;

public interface CloudOrderNetWorkService {
	/**
	 * 私有网络订单数据入库
	 * @author gaoxiang
	 * @param orderNetWork
	 */
	public BaseCloudOrderNetWork save(BaseCloudOrderNetWork orderNetWork);
	/**
	 * 私有网络订单数据更新入库
	 * @author gaoxiang
	 * @param orderNo
	 * @param resourceId
	 * @return
	 */
	public boolean update(String orderNo, String resourceId);
	/**
	 * 通过订单编号查询私有网络订单数据
	 * @author gaoxiang
	 * @param orderNo
	 * @return
	 */
	public CloudOrderNetWork getOrderNetWorkByOrderNo(String orderNo);
}
