package com.eayun.virtualization.baseservice;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.virtualization.dao.CloudOrderFloatIpDao;

@Transactional
@Service
public class BaseFloatIpService {
	
    @Autowired
    private CloudOrderFloatIpDao cloudOrderFloatIpDao;
	
	@SuppressWarnings("rawtypes")
	public boolean checkFloatIpOrderExist(String floId) {
		StringBuilder sql = new StringBuilder("select of.flo_id from cloudorder_floatip of");
		sql.append(" left join order_info oi on oi.order_no =of.order_no");
		sql.append(" where of.flo_id=? and of.order_type='1' and oi.order_state in ('1','2')");
		javax.persistence.Query query = cloudOrderFloatIpDao.createSQLNativeQuery(sql.toString(),
				new Object[] { floId });
		List resultList = query.getResultList();
		return resultList != null && resultList.size() > 0;
	}
	
	
	
	
	

	
}
