package com.eayun.common.constant;

public class DutyCertificateType {
	
	
	

	 public static final String SFZ="1";
	 
	 
	 public static final String HZ="2";
	 
	 public static final String TBZ="3";
	 
	 
	 public static final String JGZ="4";
	 
	 
	 /**
		 * 
		 * @param value 类型值
		 * @return 类型中文名称
		 */
		public static String getName(String value) {
			switch (value) {
			case SFZ:
				return "身份证";
			case HZ:
				return "护照";
			case TBZ:
				return "台胞证";
			case JGZ:
				return "军官证";
			
			default:
				return "";
				
			}
			
		}
			

}
