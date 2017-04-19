package com.eayun.common.constant;

/**
 * 订单类型枚举
 * @Filename: OrderStateType.java
 * @Description: 
 * @Version: 1.0
 * @Author: bo.zeng
 * @Email: bo.zeng@eayun.com
 * @History:<br>
 *<li>Date: 2016年8月5日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class OrderStateType {

	/**
	 * Comment for <code>TO_BE_PAID</code> 待支付
	 */
	public static final String TO_BE_PAID = "1";

	/**
	 * Comment for <code>BUILDING_RESOURCE</code> 处理中
	 */
	public static final String BUILDING_RESOURCE = "2";

	/**
	 * Comment for <code>CANCELED_BY_RESOURCE</code> 处理失败-已取消
	 */
	public static final String CANCELED_BY_RESOURCE = "3";

	/**
	 * Comment for <code>COMPLETE</code> 已完成
	 */
	public static final String COMPLETE = "4";

	/**
	 * Comment for <code>CANCELED</code> 已取消
	 */
	public static final String CANCELED = "5";

	/**
	 * 获取订单状态中文名称
	 * @param value 类型值
	 * @return 类型中文名称
	 */
	public static String getName(String value) {
		switch (value) {
		case BUILDING_RESOURCE:
			return "处理中";
		case CANCELED:
			return "已取消";
		case CANCELED_BY_RESOURCE:
			return "处理失败-已取消";
		case COMPLETE:
			return "已完成";
		case TO_BE_PAID:
			return "待支付";
		default:
			return "";
		}
	}

}
