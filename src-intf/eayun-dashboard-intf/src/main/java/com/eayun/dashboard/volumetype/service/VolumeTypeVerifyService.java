package com.eayun.dashboard.volumetype.service;

import com.eayun.dashboard.api.model.BaseApiSwitchPhone;

public interface VolumeTypeVerifyService {

	
	
	/**
	 * 查询云硬盘类型最高权限的手机号码
	 * ①、首先将完整信息存入session
	 * ②、前台显示****
	 * @return
	 */
	public BaseApiSwitchPhone getVolumeTypePhone();
	
	

	/**
	 * 绑定手机号时发送验证码
	 * @param type
	 * @param currentPhone
	 * @param newPhone
	 */
	public void sendVolumeTypePhoneCode(String type, String volumePhone,
			String newPhone);



	/**
	 * 修改手机号
	 * @param code
	 * @param newPhone
	 * @return
	 */
	public boolean editVolumeTypePhone(String code, String newPhone);



	/**
	 * 修改联系人
	 * @param newName
	 * @return
	 */
	public boolean editVolumeTypePerson(String newName);
	
	
	/**
	 * 获取云硬盘分类限速验证码
	 * @param dcId
	 * @param currentPhone
	 */
	public void getCodeForVolumeType(String volumePhone);



	/**
	 * 校验最高权限手机号验证码正误
	 * @param code
	 * @return
	 */
	public boolean checkVolumePhoneCode(String code,String volumePhone);



	/**
	 * 查询是否成功获取了操作权限
	 * @param volumePhone
	 * @return
	 */
	public boolean checkVolumePhone(String volumePhone);


	
}
