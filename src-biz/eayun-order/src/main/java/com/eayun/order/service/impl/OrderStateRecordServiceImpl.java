package com.eayun.order.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.order.dao.OrderStateRecordDao;
import com.eayun.order.model.BaseOrderStateRecord;
import com.eayun.order.service.OrderStateRecordService;

@Service
@Transactional
public class OrderStateRecordServiceImpl implements OrderStateRecordService {
	
	@Autowired
	private OrderStateRecordDao recordDao;

	@Override
	public void addOrderStateRecord(BaseOrderStateRecord record) {
		recordDao.save(record);
	}

}
