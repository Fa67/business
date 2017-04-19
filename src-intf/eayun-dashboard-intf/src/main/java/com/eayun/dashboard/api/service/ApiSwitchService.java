package com.eayun.dashboard.api.service;

public interface ApiSwitchService {

	/**
	 * 查询数据中心下API开关的开通状态
	 * @param dcId
	 * @return
	 */
	public boolean getApiSwitch(String dcId);

	/**
	 * 开启/关闭API开关操作
	 * @param cusId
	 * @param operation
	 * @param code
	 * @param dcId
	 */
	public void operationApiSwitch(String cusId, String operation, String code, String dcId, String currentPhone);

	/**
	 * 开启/关闭API开关操作发送验证码
	 * @param dcId
	 */
	public void getCodeForApiSwitch(String dcId,String currentPhone,String operation);

	/**
	 * 查询API最高权限的手机号码
	 * ①、首先将完整信息存入session
	 * ②、前台显示****
	 * @return
	 */
	public String getApiSwitchPhone();

	/**
	 * 修改手机号码时发送验证码
	 * @param type
	 */
	public void sendApiPhoneCode(String type,String currentPhone,String newPhone);

	/**
	 * 校验验证码是否正确
	 * @param code
	 * @param currentPhone
	 * @return
	 */
	public boolean verifyApiPhoneCode(String code, String currentPhone);

	/**
	 * 修改最高权限手机号码
	 * @param code
	 * @param newPhone
	 * @return
	 */
	public boolean editApiSwitchPhone(String code, String newPhone);	
}
