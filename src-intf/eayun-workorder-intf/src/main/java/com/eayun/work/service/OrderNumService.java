package com.eayun.work.service;
/**
 * 
 * @author 陈鹏飞
 *
 */
public interface OrderNumService {
	/**
	 * 获取流水号
	 * @param prefix--流水号类型--例如 工单--SJ
	 * @param format--日子类型-yyyyMMdd
	 * @param length--长度
	 * @return zd_20140917_00000001
	 */
	public String getOrderNum(String prefix,String format,int length);
}
