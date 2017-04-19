package com.eayun.virtualization.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.util.BeanUtils;
import com.eayun.virtualization.dao.CloudOrderLdPoolDao;
import com.eayun.virtualization.model.BaseCloudOrderLdPool;
import com.eayun.virtualization.model.CloudOrderLdPool;
import com.eayun.virtualization.service.CloudOrderLdPoolService;
@Service
@Transactional
public class CloudOrderLdPoolServiceImpl implements CloudOrderLdPoolService {
    private static final Logger log = LoggerFactory.getLogger(CloudOrderLdPoolServiceImpl.class);
	@Autowired
	private CloudOrderLdPoolDao cloudOrderLdPoolDao;
	
	@Override
	public BaseCloudOrderLdPool save(BaseCloudOrderLdPool orderPool) {
		return cloudOrderLdPoolDao.save(orderPool);
	}
	
	@Override
    public boolean update(String resourceId, String orderNo) {
        boolean flag = false;
        StringBuffer hql = new StringBuffer();
        try {
            hql.append("update BaseCloudOrderLdPool set poolId = ? where orderNo = ?");
            cloudOrderLdPoolDao.executeUpdate(hql.toString(), new Object[]{resourceId, orderNo});
        } catch (Exception e) {
            flag = false;
            log.error(e.toString(),e);
            throw e;
        }
        return flag;
    }
	
	@Override
	public CloudOrderLdPool getOrderLdPoolByOrderNo(String orderNo) {
		StringBuffer hql = new StringBuffer();
		hql.append("from BaseCloudOrderLdPool orderPool where orderPool.orderNo = ?");
		BaseCloudOrderLdPool orderPool = (BaseCloudOrderLdPool)cloudOrderLdPoolDao.findUnique(hql.toString(), orderNo);
		CloudOrderLdPool cloudOrderPool = new CloudOrderLdPool();
		BeanUtils.copyPropertiesByModel(cloudOrderPool, orderPool);
		return cloudOrderPool;
	}
}
