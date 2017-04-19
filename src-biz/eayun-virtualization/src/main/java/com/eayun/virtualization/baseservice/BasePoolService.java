package com.eayun.virtualization.baseservice;

import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.virtualization.dao.CloudLdPoolDao;

@Transactional
@Service
public class BasePoolService {

	@Autowired
	private CloudLdPoolDao poolDao;

	@SuppressWarnings("rawtypes")
	public boolean checkLbOrderExist(String lbId) {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT lb.pool_id FROM cloudorder_ldpool lb ")
				.append("LEFT JOIN order_info oi ON lb.order_no = oi.order_no ").append("WHERE lb.pool_id =? ")
				.append("AND (oi.order_state IN ('1' , '2')) ").append("AND (lb.order_type IN ('1' , '2')) ");
		Query query = poolDao.createSQLNativeQuery(sb.toString(), lbId);
		List resultList = query.getResultList();
		return resultList != null && resultList.size() > 0;
	}
}
