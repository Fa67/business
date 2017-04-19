package com.eayun.virtualization.model;

import java.math.BigDecimal;

public class CloudVpn extends BaseCloudVpnConn {

    /**
     * 
     */
    private static final long serialVersionUID = -4955384727987175293L;
    
    private String dcId;                            //数据中心id
    private String dcName;                          //数据中心名称
    private String vpnStatusStr;                    //vpn状态转义
    
    private String vpnServiceName;                  //vpn基础服务名字
    private String vpnServiceStatus;                //vpn基础服务状态
    private String routeId;                         //本端路由id
    private String subnetId;                        //本端子网id
    
    private String ikeName;                         //ike策略的名称
    private String ikeVersion;                      //版本
    private String ikeAuthAlgorithm;                //认证算法
    private String ikeEncryption;                   //加密算法
    private String ikeNegotiation;                  //协商模式
    private Long ikeLifetimeValue;                  //生存周期(秒)
    private String ikeDhAlgorithm;                  //DH算法
    
    private String ipSecName;                       //ipsec名称
    private String ipSecTransform;                  //传输协议
    private String ipSecAuthAlgorithm;              //认证算法
    private String ipSecEncapsulation;              //封装模式
    private String ipSecEncryption;                 //加密算法
    private Long ipSecLifetimeValue;                //生命周期(秒)
    private String ipSecDhAlgorithm;                //DH算法
    
    private String networkId;                       //本端网络id
    private String networkName;                     //本端网络名称
    private String gatewayIp;                       //路由网关ip
    private String subnetName;                      //本端子网名称
    private String subnetCidr;                      //本端子网网段
    
    private String payTypeStr;                      //付款方式转义
    private BigDecimal price;                       //价格
    private String orderNo;                         //订单编号
    private int buyCycle;                           //购买周期
    
    private String cusId;                           
    private String cusName;
    private String prjName;
    
    private int count;                              //记录资源同步次数
    
    public String getDcId() {
        return dcId;
    }
    public void setDcId(String dcId) {
        this.dcId = dcId;
    }
    public String getDcName() {
        return dcName;
    }
    public void setDcName(String dcName) {
        this.dcName = dcName;
    }
    public String getVpnStatusStr() {
        return vpnStatusStr;
    }
    public void setVpnStatusStr(String vpnStatusStr) {
        this.vpnStatusStr = vpnStatusStr;
    }
    public String getVpnServiceName() {
        return vpnServiceName;
    }
    public void setVpnServiceName(String vpnServiceName) {
        this.vpnServiceName = vpnServiceName;
    }
    public String getVpnServiceStatus() {
        return vpnServiceStatus;
    }
    public void setVpnServiceStatus(String vpnServiceStatus) {
        this.vpnServiceStatus = vpnServiceStatus;
    }
    public String getRouteId() {
        return routeId;
    }
    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }
    public String getSubnetId() {
        return subnetId;
    }
    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }
    public String getIkeName() {
        return ikeName;
    }
    public void setIkeName(String ikeName) {
        this.ikeName = ikeName;
    }
    public String getIkeVersion() {
        return ikeVersion;
    }
    public void setIkeVersion(String ikeVersion) {
        this.ikeVersion = ikeVersion;
    }
    public String getIkeAuthAlgorithm() {
        return ikeAuthAlgorithm;
    }
    public String getCusId() {
        return cusId;
    }
    public void setCusId(String cusId) {
        this.cusId = cusId;
    }
    public String getCusName() {
        return cusName;
    }
    public void setCusName(String cusName) {
        this.cusName = cusName;
    }
    public void setIkeAuthAlgorithm(String ikeAuthAlgorithm) {
        this.ikeAuthAlgorithm = ikeAuthAlgorithm;
    }
    public String getIkeEncryption() {
        return ikeEncryption;
    }
    public void setIkeEncryption(String ikeEncryption) {
        this.ikeEncryption = ikeEncryption;
    }
    public String getIkeNegotiation() {
        return ikeNegotiation;
    }
    public void setIkeNegotiation(String ikeNegotiation) {
        this.ikeNegotiation = ikeNegotiation;
    }
    public Long getIkeLifetimeValue() {
        return ikeLifetimeValue;
    }
    public void setIkeLifetimeValue(Long ikeLifetimeValue) {
        this.ikeLifetimeValue = ikeLifetimeValue;
    }
    public String getIkeDhAlgorithm() {
        return ikeDhAlgorithm;
    }
    public void setIkeDhAlgorithm(String ikeDhAlgorithm) {
        this.ikeDhAlgorithm = ikeDhAlgorithm;
    }
    public String getIpSecName() {
        return ipSecName;
    }
    public void setIpSecName(String ipSecName) {
        this.ipSecName = ipSecName;
    }
    public String getIpSecTransform() {
        return ipSecTransform;
    }
    public void setIpSecTransform(String ipSecTransform) {
        this.ipSecTransform = ipSecTransform;
    }
    public String getIpSecAuthAlgorithm() {
        return ipSecAuthAlgorithm;
    }
    public void setIpSecAuthAlgorithm(String ipSecAuthAlgorithm) {
        this.ipSecAuthAlgorithm = ipSecAuthAlgorithm;
    }
    public String getIpSecEncapsulation() {
        return ipSecEncapsulation;
    }
    public void setIpSecEncapsulation(String ipSecEncapsulation) {
        this.ipSecEncapsulation = ipSecEncapsulation;
    }
    public String getIpSecEncryption() {
        return ipSecEncryption;
    }
    public void setIpSecEncryption(String ipSecEncryption) {
        this.ipSecEncryption = ipSecEncryption;
    }
    public Long getIpSecLifetimeValue() {
        return ipSecLifetimeValue;
    }
    public void setIpSecLifetimeValue(Long ipSecLifetimeValue) {
        this.ipSecLifetimeValue = ipSecLifetimeValue;
    }
    public String getIpSecDhAlgorithm() {
        return ipSecDhAlgorithm;
    }
    public void setIpSecDhAlgorithm(String ipSecDhAlgorithm) {
        this.ipSecDhAlgorithm = ipSecDhAlgorithm;
    }
    public String getNetworkId() {
        return networkId;
    }
    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }
    public String getNetworkName() {
        return networkName;
    }
    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }
    public String getGatewayIp() {
        return gatewayIp;
    }
    public void setGatewayIp(String gatewayIp) {
        this.gatewayIp = gatewayIp;
    }
    public String getSubnetName() {
        return subnetName;
    }
    public void setSubnetName(String subnetName) {
        this.subnetName = subnetName;
    }
    public String getSubnetCidr() {
        return subnetCidr;
    }
    public void setSubnetCidr(String subnetCidr) {
        this.subnetCidr = subnetCidr;
    }
    public String getPayTypeStr() {
        return payTypeStr;
    }
    public void setPayTypeStr(String payTypeStr) {
        this.payTypeStr = payTypeStr;
    }
    public BigDecimal getPrice() {
        return price;
    }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    public String getOrderNo() {
        return orderNo;
    }
    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
    public int getBuyCycle() {
        return buyCycle;
    }
    public void setBuyCycle(int buyCycle) {
        this.buyCycle = buyCycle;
    }
	public String getPrjName() {
		return prjName;
	}
	public void setPrjName(String prjName) {
		this.prjName = prjName;
	}
    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }
    
}
