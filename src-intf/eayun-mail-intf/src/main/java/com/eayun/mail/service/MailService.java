package com.eayun.mail.service;

import java.util.List;

public interface MailService {

	/**
	 * 发送邮件
	 * 
	 * @param title
	 *            邮件标题
	 * @param context
	 *            邮件正文
	 * @param links
	 *            联系人列表
	 * @return
	 * @throws Exception
	 */
	public boolean send(String title, String context, List<String> links) throws Exception;
}
