package com.eayun.work.service;

import java.util.Date;
import java.util.List;

import com.eayun.work.model.BaseNoSendSms;

public interface NoSendSmsService {

	public void delete(String smsId);

	public List<BaseNoSendSms> find(String string, List<Date> values);
	
}
