package com.eayun.virtualization.baseservice;

import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.virtualization.dao.CloudNetWorkDao;

@Transactional
@Service
public class BaseNetworkService {
	
	@Autowired
	private CloudNetWorkDao netWorkDao;
	
	@SuppressWarnings("rawtypes")
    public boolean checkNetworkOrderExist(String netId) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT net.net_id FROM cloudorder_network net ")
                .append("LEFT JOIN order_info oi ON net.order_no = oi.order_no  ")
                .append("WHERE net.net_id =? ")
                .append("AND (oi.order_state IN ('1' , '2')) ")
                .append("AND (net.order_type IN ('1' , '2')) ");
        Query query = netWorkDao.createSQLNativeQuery(sb.toString(), new Object[]{netId});
        List resultList = query.getResultList();
        return resultList != null && resultList.size()>0 ;
    }
}
