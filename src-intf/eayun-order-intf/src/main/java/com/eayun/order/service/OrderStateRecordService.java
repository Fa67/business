package com.eayun.order.service;

import com.eayun.order.model.BaseOrderStateRecord;

/**
 *                       
 * @Filename: OrderStateRecordSerivce.java
 * @Description: 订单状态变更记录服务接口
 * @Version: 1.0
 * @Author: bo.zeng
 * @Email: bo.zeng@eayun.com
 * @History:<br>
 *<li>Date: 2016年7月27日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public interface OrderStateRecordService {
	
	/**
	 * 添加订单状态变更记录
	 * @param records 记录实体
	 */
	public void addOrderStateRecord(BaseOrderStateRecord record);
	

}
