package com.eayun.common.constant;

/**
 * 付费类型枚举
 * @Filename: PayType.java
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
public class PayType {
	/**
     *Comment for <code>PAYBEFORE</code>
     *预付费
     */
    public static final String PAYBEFORE = "1";
    /**
     *Comment for <code>PAYAFTER</code>
     *后付费
     */
    public static final String PAYAFTER  = "2";
    
    /**
	 * 获取订单付费类型中文名称
	 * @param value 类型值
	 * @return 类型中文名称
	 */
	public static String getName(String value) {
		switch (value) {
		case PAYBEFORE:
			return "预付费";
		case PAYAFTER:
			return "后付费";
		default:
			return "";
		}
	}
}
