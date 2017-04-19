package com.eayun.work.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.work.dao.NoSendSmsDao;
import com.eayun.work.model.BaseNoSendSms;
import com.eayun.work.service.NoSendSmsService;
@Service
@Transactional
public class NoSendSmsServiceImpl implements NoSendSmsService {
	
	@Autowired
	private NoSendSmsDao noSendSmsDao;
	@SuppressWarnings("unchecked")
	@Override
	public List<BaseNoSendSms> find(String hql, List<Date> values) {
		List<BaseNoSendSms> smsList=noSendSmsDao.find(hql, values.toArray());
		return smsList;
	}

	@Override
	public void delete(String smsId) {
		noSendSmsDao.delete(smsId);
	}

}
