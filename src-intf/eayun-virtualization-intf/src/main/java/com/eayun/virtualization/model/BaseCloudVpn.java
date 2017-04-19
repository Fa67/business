package com.eayun.virtualization.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.eayun.eayunstack.model.Vpn;

@Entity
@Table(name = "cloud_vpnservice")
public class BaseCloudVpn implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1067448678299799137L;

    private String vpnServiceId;            //vpnId
    private String vpnServiceName;          //vpn基础服务名字
    private String vpnServiceStatus;        //vpn基础服务状态
    private String prjId;                   //项目id
    private String networkId;               //本端网络id
    private String routeId;                 //本端路由id
    private String subnetId;                //本端子网id
    @Id
    @Column(name = "vpnservice_id", unique = true, nullable = false, length = 100)
    public String getVpnServiceId() {
        return vpnServiceId;
    }
    public void setVpnServiceId(String vpnServiceId) {
        this.vpnServiceId = vpnServiceId;
    }
    @Column(name = "vpnservice_name", length = 50)
    public String getVpnServiceName() {
        return vpnServiceName;
    }
    public void setVpnServiceName(String vpnServiceName) {
        this.vpnServiceName = vpnServiceName;
    }
    @Column(name = "vpn_status", length = 10)
    public String getVpnServiceStatus() {
        return vpnServiceStatus;
    }
    public void setVpnServiceStatus(String vpnServiceStatus) {
        this.vpnServiceStatus = vpnServiceStatus;
    }
    @Column(name = "prj_id", length = 100)
    public String getPrjId() {
        return prjId;
    }
    public void setPrjId(String prjId) {
        this.prjId = prjId;
    }
    @Column(name = "network_id", length = 100)
    public String getNetworkId() {
        return networkId;
    }
    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }
    @Column(name = "route_id", length = 100)
    public String getRouteId() {
        return routeId;
    }
    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }
    @Column(name = "subnet_id", length = 100)
    public String getSubnetId() {
        return subnetId;
    }
    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }
    
    public BaseCloudVpn(){}
    
    public BaseCloudVpn(String datacenterId, Vpn vpn) {
        this.vpnServiceName = vpn.getName();
        this.vpnServiceStatus = vpn.getStatus();
        this.prjId = vpn.getTenant_id();
        this.routeId = vpn.getRouter_id();
        this.subnetId = vpn.getSubnet_id();
    }
}
