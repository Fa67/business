package com.eayun.virtualization.service;

import com.eayun.virtualization.model.BaseCloudOrderLdPool;
import com.eayun.virtualization.model.CloudOrderLdPool;

public interface CloudOrderLdPoolService {
	/**
	 * 负载均衡器订单数据入库
	 * @param orderPool
	 */
	public BaseCloudOrderLdPool save(BaseCloudOrderLdPool orderPool);
	/**
	 * 负载均衡器订单数据更新入库
	 * @param orderPool
	 */
	public boolean update(String orderNo, String resourceId);
	/**
	 * 通过订单编号查询负载均衡器订单数据
	 * @param orderNo
	 * @return
	 */
	public CloudOrderLdPool getOrderLdPoolByOrderNo(String orderNo);
}
