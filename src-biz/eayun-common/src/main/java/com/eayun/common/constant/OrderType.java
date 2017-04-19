package com.eayun.common.constant;


/**
 * 订单类型枚举
 * @Filename: OrderType.java
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
public class OrderType {
	
	/**
	 *Comment for <code>NEW</code>
	 *新购
	 */
	public static final String NEW = "0";
	
	/**
	 *Comment for <code>RENEW</code>
	 *续费
	 */
	public static final String RENEW   = "1";

	/**
	 *Comment for <code>UPGRADE</code>
	 *升级
	 */
	public static final String UPGRADE = "2";
	
	/**
	 * 获取订单类型中文名称
	 * @param value 类型值
	 * @return 类型中文名称
	 */
	public static String getName(String value) {
		switch (value) {
		case NEW:
			return "新购";
		case RENEW:
			return "续费";
		case UPGRADE:
			return "升级";
		default:
			return "";
		}
	}
}
