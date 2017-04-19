package com.eayun.common.constant;

/**
 *                       
 * @Filename: ThirdPartType.java
 * @Description: 第三方支付类型
 * @Version: 1.0
 * @Author: bo.zeng
 * @Email: bo.zeng@eayun.com
 * @History:<br>
 *<li>Date: 2016年8月5日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class ThirdPartType {
	
    /**
     *Comment for <code>ALIPAY</code>
     *支付宝
     */
    public static final String ALIPAY = "0";
    
    /**
	 * 获取第三方支付类型中文名称
	 * @param value 类型值
	 * @return 类型中文名称
	 */
	public static String getName(String value) {
		switch (value) {
		case ALIPAY:
			return "支付宝";
		default:
			return "";
		}
	}

}
