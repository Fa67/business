package com.eayun.virtualization.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.util.BeanUtils;
import com.eayun.virtualization.dao.CloudOrderVpnDao;
import com.eayun.virtualization.model.BaseCloudOrderVpn;
import com.eayun.virtualization.model.CloudOrderVpn;
import com.eayun.virtualization.service.CloudOrderVpnService;
@Service
@Transactional
public class CloudOrderVpnServiceImpl implements CloudOrderVpnService {
    private static final Logger log = LoggerFactory.getLogger(CloudOrderVpnServiceImpl.class);
    @Autowired
    private CloudOrderVpnDao cloudOrderVpnDao;
    @Override
    public BaseCloudOrderVpn save(BaseCloudOrderVpn orderVpn) {
        return cloudOrderVpnDao.save(orderVpn);
    }
    @Override
    public boolean update(String orderNo, String resourceId) {
        boolean flag = false;
        StringBuffer hql = new StringBuffer();
        try {
            hql.append("update BaseCloudOrderVpn set vpnId = ? where orderNo = ?");
            cloudOrderVpnDao.executeUpdate(hql.toString(), new Object[]{resourceId, orderNo});
        } catch (Exception e) {
            flag = false;
            log.error(e.toString(),e);
            throw e;
        }
        return flag;
    }
    @Override
    public CloudOrderVpn getOrderVpnByOrderNo(String orderNo) {
        StringBuffer hql = new StringBuffer();
        hql.append("from BaseCloudOrderVpn orderVpn where orderVpn.orderNo = ?");
        BaseCloudOrderVpn orderPool = (BaseCloudOrderVpn)cloudOrderVpnDao.findUnique(hql.toString(), orderNo);
        CloudOrderVpn cloudOrderVpn = new CloudOrderVpn();
        BeanUtils.copyPropertiesByModel(cloudOrderVpn, orderPool);
        return cloudOrderVpn;
    }
}
