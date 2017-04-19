package com.eayun.common.constant;

public class UnitNatureType {
	
	
	
	/**
     *Comment for <code>PAYBEFORE</code>
     * 个人
     */
    public static final String FORMAL = "1";
    /**
     *Comment for <code>PAYAFTER</code>
     *2企业、
     */
    public static final String  COOPERATION  = "2";
    
    /**
     *Comment for <code>PAYAFTER</code>
     *3政府机关
     */
    public static final String  TEST  = "3";
    
    /**
     *Comment for <code>PAYAFTER</code>
     *4事业单位
     */
    public static final String  USE  = "4";
    /**
     *Comment for <code>PAYAFTER</code>
     *5社会群体
     */
    public static final String  OTHER  = "5";
    
    
    /**
     * 6军队
     * */
    public static final String JUN="6";
    
    /**
	 * 
	 * @param value 类型值
	 * @return 类型中文名称
	 */
	public static String getName(String value) {
		switch (value) {
		case FORMAL:
			return "个人";
		case COOPERATION:
			return "企业";
		case TEST:
			return "政府机关";
		case USE:
			return "事业单位";
		case OTHER:
			return "社会群体";
		case JUN:
			return "军队";
		default:
			return "";
		}
	}

}
