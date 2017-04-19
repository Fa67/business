package com.eayun.common.util;

import com.eayun.common.constant.PayType;

public class CloudResourceUtil {
	
	/**------------计费模式---------------- */
	/** 计费模式-包年包月（先付费）*/
	public static final String CHANRGE_BY_MONTHLY = "包年包月";
	/** 计费模式-按需（后付费）*/
	public static final String CHARGE_BY_ONDEMAND = "按需付费";
	
	
	/**------------计费资源状态---------------- */
	/** 计费资源状态-正常*/
	public final static String CLOUD_CHARGESTATE_NORMAL = "正常";
	/** 计费资源状态-余额不足*/
	public final static String CLOUD_CHARGESTATE_NSF = "余额不足";
	/** 计费资源状态-已到期 */
	public final static String CLOUD_CHARGESTATE_EXPIRED = "已到期";
	
	/** 计费资源状态-正常*/
	public final static String CLOUD_CHARGESTATE_NORMAL_CODE = "0";
	/** 计费资源状态-余额不足*/
	public final static String CLOUD_CHARGESTATE_NSF_CODE = "1";
	/** 计费资源状态-已到期 */
	public final static String CLOUD_CHARGESTATE_EXPIRED_CODE = "2";
	/** 计费资源状态-已处理 */
	public final static String CLOUD_CHARGESTATE_EXPIRED_ED_CODE = "3";
	
	/**
	 * 云资源计费模式转义
	 * 
	 * @param payType
	 * @return
	 */
	public static String escapePayType(String payType){
		String type = null;
		if(PayType.PAYBEFORE.equals(payType)){
			type = CHANRGE_BY_MONTHLY;
		}
		else if(PayType.PAYAFTER.equals(payType)){
			type = CHARGE_BY_ONDEMAND;
		}
		return type;
	}
	
	/**
	 * 云资源计费状态转义；正常时，显示资源状态
	 * 
	 * @author zhouhaitao
	 * @param chargeState
	 * 			计费状态（0,1,2,3）
	 * @param resourceStatus
	 * 			云资源状态
	 * @return
	 */
	public static String escapseChargeState(String chargeState){
		String status = null ;
		if(CLOUD_CHARGESTATE_NORMAL_CODE.equals(chargeState)){
			status = CLOUD_CHARGESTATE_NORMAL;
		}
		else if(CLOUD_CHARGESTATE_NSF_CODE.equals(chargeState)){
			status = CLOUD_CHARGESTATE_NSF;
		}
		else if(CLOUD_CHARGESTATE_EXPIRED_CODE.equals(chargeState)||CLOUD_CHARGESTATE_EXPIRED_ED_CODE.equals(chargeState)){
			status = CLOUD_CHARGESTATE_EXPIRED;
		}
		return status;
	}
	
	/**
	 * <p>处理云主机注入的Linux下的密码</p>
	 * ------------------
	 * @author zhouhaitao
	 * @param pwd
	 * @return
	 */
	public static String handleLinuxPwd(String pwd){
		if(null!=pwd&&pwd.contains("'"))
			pwd = pwd.replaceAll("'", "''");
		pwd = "'"+pwd+"'";
		return pwd;
	}
}
