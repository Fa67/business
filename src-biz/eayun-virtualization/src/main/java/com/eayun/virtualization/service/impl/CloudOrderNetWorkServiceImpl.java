package com.eayun.virtualization.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.util.BeanUtils;
import com.eayun.virtualization.dao.CloudOrderNetWorkDao;
import com.eayun.virtualization.model.BaseCloudOrderNetWork;
import com.eayun.virtualization.model.CloudOrderNetWork;
import com.eayun.virtualization.service.CloudOrderNetWorkService;
@Service
@Transactional
public class CloudOrderNetWorkServiceImpl implements CloudOrderNetWorkService {
    private static final Logger log = LoggerFactory.getLogger(CloudOrderNetWorkServiceImpl.class);
	@Autowired
	private CloudOrderNetWorkDao orderNetWorkDao;
	
	@Override
	public BaseCloudOrderNetWork save(BaseCloudOrderNetWork orderNetWork) {
		return orderNetWorkDao.save(orderNetWork);
	}
	
	@Override
	public boolean update(String orderNo, String resourceId) {
	    boolean flag = false;
	    StringBuffer hql = new StringBuffer();
	    try {
	        hql.append("update BaseCloudOrderNetWork set netId = ? where orderNo = ?");
	        orderNetWorkDao.executeUpdate(hql.toString(), new Object[]{resourceId, orderNo});
	    } catch (Exception e) {
	        flag = false;
	        log.error(e.toString(),e);
	        throw e;
	    }
	    return flag;
	}
	
	@Override
	public CloudOrderNetWork getOrderNetWorkByOrderNo(String orderNo) {
		StringBuffer hql = new StringBuffer();
		hql.append("from BaseCloudOrderNetWork orderNetWork where orderNetWork.orderNo = ?");
		BaseCloudOrderNetWork orderNetWork = (BaseCloudOrderNetWork)orderNetWorkDao.findUnique(hql.toString(), orderNo);
		CloudOrderNetWork cloudOrderNetWork = new CloudOrderNetWork();
		BeanUtils.copyPropertiesByModel(cloudOrderNetWork, orderNetWork);
		return cloudOrderNetWork;
	}
	
}
