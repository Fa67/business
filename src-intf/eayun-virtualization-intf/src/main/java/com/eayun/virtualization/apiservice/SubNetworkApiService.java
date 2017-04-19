package com.eayun.virtualization.apiservice;

import com.eayun.virtualization.model.BaseCloudSubNetWork;

public interface SubNetworkApiService {
    
    /**
     * 根据子网id获取子网对象
     * @author gaoxiang
     * @param subNetId
     * @return
     */
    public BaseCloudSubNetWork getSubNetworkById(String subNetId);
    
    /**
     * 判断受管子网是否绑定路由。count > 0 :已绑定，count = 0 : 未绑定。
     * @author gaoxiang
     * @param subnetId
     * @return
     */
    public int getSubBindRouteCount(String subnetId);
}
