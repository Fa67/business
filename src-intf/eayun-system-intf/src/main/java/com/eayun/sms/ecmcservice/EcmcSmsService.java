package com.eayun.sms.ecmcservice;

import java.util.Date;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.sms.model.SMS;

public interface EcmcSmsService {
	/**
	 * @author gaoxiang
	 * @param page
	 * @param beginTime
	 * @param endTime
	 * @param queryMap
	 * @return
	 * @throws Exception
	 */
	public Page getSmsList(Page page, Date beginTime, Date endTime,
			String mobile, String status, QueryMap queryMap) throws Exception;

	public boolean createSms(SMS sms) throws Exception;

	public boolean resendSms(String id) throws Exception;
}
