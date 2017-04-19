package com.eayun.common.constant;

public class UnitcertificateType {
	
	 public static final String GONGSHANG="1";
	 
	 
	 public static final String ZUZIJIGOU="2";
	 
	 
	 /**
		 * 
		 * @param value 类型值
		 * @return 类型中文名称
		 */
		public static String getName(String value) {
			switch (value) {
			case GONGSHANG:
				return "工商营业执照";
			case ZUZIJIGOU:
				return "组织机构代码";
			
			default:
				return "";
				
			}
			
		}
			
			
}
