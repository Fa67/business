package com.eayun.unit.model;

import java.util.List;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2017年3月20日
 */
public class WebSiteIP extends BaseWebSiteInfo {

    private List<BaseWebDataCenterIp> ipList;

    public List<BaseWebDataCenterIp> getIpList() {
        return ipList;
    }

    public void setIpList(List<BaseWebDataCenterIp> ipList) {
        this.ipList = ipList;
    }
    
}
