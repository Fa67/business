package com.eayun.virtualization.service;

import com.eayun.virtualization.model.CloudOrderVolume;

public interface VolumeOrderService {
	public CloudOrderVolume getVolOrderByOrderNo(String orderNo)throws Exception;

	public void addOrderVolume(CloudOrderVolume orderVolume)throws Exception;
	
	//回调该方法设置资源ids
	public boolean updateOrderResources(String orderNo,String resourceJson)throws Exception;

	// 根据订单编号查询云硬盘订单信息
	public CloudOrderVolume getByOrder(String orderNo);



}
