package com.eayun.common.constant;

public class recordupdateType {
	
	  /**
     * 
     * 
    2初审通过、3初审未通过、4复审通过、5复审未通过、6管局审核、7管局未通过、8备案成功
     * */
    public static final String A="2";
    public static final String B="3";
    public static final String C="4";
    public static final String D="5";
    public static final String E="6";
    public static final String F="7";
    public static final String G="8";
    
    /**
	 * 
	 * @param value 类型值
	 * @return 类型中文名称
	 */
	public static String getName(String value) {
		switch (value) {
		case A:
			return "初审通过";
		case B:
			return "初审未通过";
		case C:
			return "复审通过";
		case D:
			return "复审未通过";
		case E:
			return "上报管局";
		
		case F:
			return "管局未通过";
		case G:
			return "管局通过";
		
		default:
			return "";
			
		}
		
		
	}

}
