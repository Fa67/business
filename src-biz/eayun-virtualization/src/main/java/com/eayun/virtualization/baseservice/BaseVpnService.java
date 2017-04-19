package com.eayun.virtualization.baseservice;

import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.virtualization.dao.CloudVpnDao;

@Transactional
@Service
public class BaseVpnService {
	
    @Autowired
    private CloudVpnDao vpnDao;
    
    @SuppressWarnings("rawtypes")
	public boolean checkVpnOrderExist(String vpnId){
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT vpn_id FROM cloudorder_vpn vpn ")
                .append("LEFT JOIN order_info oi ON vpn.order_no = oi.order_no ")
                .append("WHERE vpn.vpn_id =? ")
                .append("AND (oi.order_state IN ('1' , '2')) ")
                .append("AND (vpn.order_type IN ('1' , '2')) ");
        Query query = vpnDao.createSQLNativeQuery(sb.toString(), vpnId);
        List resultList = query.getResultList();
        return resultList != null && resultList.size()>0 ;
    }
}
