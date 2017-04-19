package com.eayun.work.ecmcservice;

import com.eayun.ecmcuser.model.EcmcSysUser;
import com.eayun.work.model.BaseWorkOpinion;
import com.eayun.work.model.WorkOpinion;
import com.eayun.work.model.Workorder;

public interface EcmcWorkMailService {
	/**
	 * 新建，受理，处理，解决，完结，取消时发送邮件短信
	 * @param workorder
	 * @param workOpinion
	 * @throws Exception
	 */
	public void sendMailAndSms(Workorder workorder,WorkOpinion workOpinion) throws Exception;
	/**
	 * 编辑是发送邮件
	 * @param workorder
	 * @param oldWorkorder
	 * @throws Exception
	 */
	public void sendEmailMessageForEdit(Workorder workorder, Workorder oldWorkorder, BaseWorkOpinion baseWorkOpinion, EcmcSysUser ecmcUser) throws Exception;
}
