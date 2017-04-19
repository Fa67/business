package com.eayun.common.constant;


/**
 * 计费周期枚举
 * @Filename: BillingCycleType.java
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
public class BillingCycleType {

	/**
	 * Comment for <code>HOUR</code> 按小时
	 */
	public static final String HOUR = "0";
	/**
	 * Comment for <code>DAY</code> 按天
	 */
	public static final String DAY = "1";
	/**
	 * Comment for <code>OTHER</code> 其他
	 */
	public static final String OTHER = "2";

	/**
	 * 获取订单计费周期类型中文名称
	 * @param value 类型值
	 * @return 类型中文名称
	 */
	public static String getName(String value) {
		switch (value) {
		case HOUR:
			return "小时";
		case DAY:
			return "天";
		case OTHER:
			return "其他";
		default:
			return "";
		}
	}

}
