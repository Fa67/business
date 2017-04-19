package com.eayun.common.util;

public class BoolUtil {

	private static final String YES = "是";
	private static final String NO = "否";

	public static Character bool2char(boolean b) {
		if (b) {
			return new Character('1');
		} else {
			return new Character('0');
		}
	}

	public static String bool2Str(boolean b) {
		if (b) {
			return YES;
		} else {
			return NO;
		}
	}
	
	public static String bool2Str(char b) {
		if (b == '1') {
			return YES;
		} else {
			return NO;
		}
	}

}
