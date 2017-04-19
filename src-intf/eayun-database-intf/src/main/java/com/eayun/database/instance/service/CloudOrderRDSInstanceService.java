package com.eayun.database.instance.service;

import java.util.Date;

import com.eayun.database.instance.model.CloudOrderRDSInstance;

/**
 * 云数据库实例订单的service接口
 *                       
 * @Filename: CloudOrderRDSInstanceService.java
 * @Description: 
 * @Version: 1.0
 * @Author: LiuZhuangzhuang
 * @Email: zhuangzhuang.liu@eayun.com
 * @History:<br>
 *<li>Date: 2017年2月22日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public interface CloudOrderRDSInstanceService {
	
	void saveOrUpdate(CloudOrderRDSInstance order);

	boolean modifyResourceForVisable(String rdsId, Date completeDate, String vmId);

	CloudOrderRDSInstance getRdsOrderByOrderNo(String orderNo);

	void udpateOrder(CloudOrderRDSInstance order);
}
