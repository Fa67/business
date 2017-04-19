package com.eayun.virtualization.service;

import com.eayun.virtualization.model.BaseCloudOrderVpn;
import com.eayun.virtualization.model.CloudOrderVpn;

public interface CloudOrderVpnService {

    public BaseCloudOrderVpn save(BaseCloudOrderVpn orderVpn);
    
    public boolean update(String orderNo, String resourceId);
    
    public CloudOrderVpn getOrderVpnByOrderNo(String orderNo);
}
