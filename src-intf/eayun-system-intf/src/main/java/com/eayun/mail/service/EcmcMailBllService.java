package com.eayun.mail.service;

import java.util.List;

import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.support.Page;
import com.eayun.sys.model.SysDataTree;

public interface EcmcMailBllService {
	/**
	 * 查询邮件分页
	 * @param page
	 * @param paramsMap
	 * @return
	 */
	public Page getMailList(Page page,ParamsMap paramsMap);
	/**
	 * 获取邮件状态
	 * @return
	 */
	public List<SysDataTree> getMailStatusList();
	/**
	 * 重新发送邮件
	 * @param mailId
	 * @param userMailList
	 * @return
	 */
	public boolean sendMailByUser(String mailId,List<String> userMailList)throws Exception;
}
