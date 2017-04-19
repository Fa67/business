package com.eayun.common.constant;

public class UnitStatusType {
	
	  /**
     * 
     * 
     * 1等待初审、2初审通过、3初审未通过、4复审通过、5复审未通过、6管局审核、7管局未通过、8备案成功
     * */
    public static final String A="1";
    public static final String B="2";
    public static final String C="3";
    public static final String D="4";
    public static final String E="5";
    public static final String F="6";
    public static final String G="7";
    public static final String H="8";
    
    /**
	 * 
	 * @param value 类型值
	 * @return 类型中文名称
	 */
	public static String getName(String value) {
		switch (value) {
		case A:
			return "等待初审";
		case B:
			return "初审通过";
		case C:
			return "初审未通过";
		case D:
			return "复审通过";
		case E:
			return "复审未通过";
		case F:
			return "管局审核";
		case G:
			return "管局未通过";
		case H:
			return "备案成功";
		default:
			return "";
			
		}
		
		
	}
	
}
	

