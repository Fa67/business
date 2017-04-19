/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.virtualization.model;


/**
 *                       
 * @Filename: EcmcCloudSubNetwork.java
 * @Description: 
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2016年4月1日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class EcmcCloudSubNetwork extends BaseCloudSubNetWork{
    
    private String prjName;
    private String netName;
    private String subnetId;//路由用到 勿注释
    private String ipversion;//路由用到 勿注释
    private String gatewayip;//路由用到 勿注释
    private String isDeleting;//是否正在删除，路由用到 勿注释
    private String netId;
    private String dcId;
    
    public EcmcCloudSubNetwork(){}
    
    public EcmcCloudSubNetwork(String subnetId, String subnetName, String ipVersion, String netName, String cidr, String gatewayIp, String pooldata, String isForbiddengw){
        super();
        this.setSubnetId(subnetId);
        this.setSubnetName(subnetName);
        this.setIpVersion(ipVersion);
        this.setNetName(netName);
        this.setCidr(cidr);
        this.setGatewayIp(gatewayIp);
        this.setPooldata(pooldata);
        this.setIsForbiddengw(isForbiddengw);
    }
    
    public EcmcCloudSubNetwork(String subnetId, String subnetName, String ipVersion, String netId, String netName, String cidr, String gatewayIp, String pooldata, String isForbiddengw){
        this(subnetId, subnetName, ipVersion, netName, cidr, gatewayIp, pooldata, isForbiddengw);
        this.setNetId(netId);
    }
    
    public String getPrjName() {
        return prjName;
    }
    public void setPrjName(String prjName) {
        this.prjName = prjName;
    }
    public String getNetName() {
        return netName;
    }
    public void setNetName(String netName) {
        this.netName = netName;
    }
    public String getSubnetId() {
        return subnetId;
    }
    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }
    public String getIpversion() {
        return ipversion;
    }
    public void setIpversion(String ipversion) {
        this.ipversion = ipversion;
    }
    public String getGatewayip() {
        return gatewayip;
    }
    public void setGatewayip(String gatewayip) {
        this.gatewayip = gatewayip;
    }
    public String getIsDeleting() {
        return isDeleting;
    }
    public void setIsDeleting(String isDeleting) {
        this.isDeleting = isDeleting;
    }
    public String getNetId() {
        return netId;
    }
    public void setNetId(String netId) {
        this.netId = netId;
    }
    public String getDcId() {
        return dcId;
    }
    public void setDcId(String dcId) {
        this.dcId = dcId;
    }

}
