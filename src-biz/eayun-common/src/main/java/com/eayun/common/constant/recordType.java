package com.eayun.common.constant;

public class recordType {
public static final String ONEUNIT="1";
public static final String NEWWEB="2";
public static final String NEWIMPL="3";
public static final String UPUNIT="4";
    
    /**
	 * 
	 * @param value 类型值
	 * @return 类型中文名称1 首次备案、2 新增网站、3 新增接入、4 变更备案
	 */
	public static String getName(String value) {
		switch (value) {
		case ONEUNIT:
			return "首次备案";
		case NEWWEB:
			return "新增网站";
		case NEWIMPL:
			return "新增接入";
		case UPUNIT:
			return "变更备案";
		default:
			return "";
			
		}
		}

}
