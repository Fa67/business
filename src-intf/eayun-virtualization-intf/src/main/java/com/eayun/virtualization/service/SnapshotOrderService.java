package com.eayun.virtualization.service;

import com.eayun.virtualization.model.CloudOrderSnapshot;

public interface SnapshotOrderService {
	//根据订单号查询CloudOrderSnapshot
	public CloudOrderSnapshot getSnapOrderByOrderNo(String orderNo)throws Exception;
	
	//新增CloudOrderSnapshot
	public void addOrderSnapshot(CloudOrderSnapshot orderSnapshot)throws Exception;

	//订单完成将资源id存入cloudOrderSnapshot
	public boolean updateOrderResources(String orderNo, String resourceJson)throws Exception;
	
	//根据订单号查询详细CloudOrderSnapshot
	public CloudOrderSnapshot getOrderByOrderNo(String orderNo);
}
