package com.eayun.common.constant;

public class CustomerType {
	
	/**
     *Comment for <code>PAYBEFORE</code>
     *正式客户
     */
    public static final String FORMAL = "0";
    /**
     *Comment for <code>PAYAFTER</code>
     *合作客户
     */
    public static final String  COOPERATION  = "1";
    
    /**
     *Comment for <code>PAYAFTER</code>
     *测试客户
     */
    public static final String  TEST  = "2";
    
    /**
     *Comment for <code>PAYAFTER</code>
     *公司自用客户
     */
    public static final String  USE  = "3";
    /**
     *Comment for <code>PAYAFTER</code>
     *公司自用客户
     */
    public static final String  OTHER  = "4";
    
    
    
    
    
    /**
	 * 获取订单付费类型中文名称
	 * @param value 类型值
	 * @return 类型中文名称
	 */
	public static String getName(String value) {
		switch (value) {
		case FORMAL:
			return "正式客户";
		case COOPERATION:
			return "合作客户";
		case TEST:
			return "测试客户";
		case USE:
			return "公司自用客户";
		case OTHER:
			return "其它";
		default:
			return "";
		}
	}

}
