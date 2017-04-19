package com.eayun.virtualization.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "cloudorder_vpn")
public class BaseCloudOrderVpn implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1380535669475997120L;
    private String ordervpnId;
    private String orderNo;
    private String orderType;
    private int buyCycle;
    private String payType;
    private BigDecimal price;

    private String vpnId;
    private String vpnserviceId;
    private String ikeId;
    private String ipsecId;

    private String vpnName;
    private String routeId;
    private String subnetId;
    private String networkId;

    private String peerAddress;
    private String peerId;
    private String peerCidrs;

    private Long mtu;
    private String dpdAction;
    private Long dpdInterval;
    private Long dpdTimeout;
    private String pskKey;
    private String initiator;

    private String ikeEncryption;
    private String ikeVersion;
    private String ikeAuth;
    private String ikeNegotiation;
    private Long ikeLifetime;
    private String ikeDh;

    private String ipsecEncryption;
    private String ipsecProtocol;
    private String ipsecAuth;
    private String ipsecEncapsulation;
    private Long ipsecLifetime;
    private String ipsecDh;

    private String createName;
    private Date createTime;
    private String dcId;
    private String prjId;
    private String cusId;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "ordervpn_id", length = 32)
    public String getOrdervpnId() {
        return ordervpnId;
    }
    public void setOrdervpnId(String ordervpnId) {
        this.ordervpnId = ordervpnId;
    }
    @Column(name = "order_no", length = 18)
    public String getOrderNo() {
        return orderNo;
    }
    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
    @Column(name = "order_type", length = 1)
    public String getOrderType() {
        return orderType;
    }
    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }
    @Column(name = "buy_cycle")
    public int getBuyCycle() {
        return buyCycle;
    }
    public void setBuyCycle(int buyCycle) {
        this.buyCycle = buyCycle;
    }
    @Column(name = "pay_type", length = 1)
    public String getPayType() {
        return payType;
    }
    public void setPayType(String payType) {
        this.payType = payType;
    }
    @Column(name = "price")
    public BigDecimal getPrice() {
        return price;
    }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    @Column(name = "vpn_id", length = 100)
    public String getVpnId() {
        return vpnId;
    }
    public void setVpnId(String vpnId) {
        this.vpnId = vpnId;
    }
    @Column(name = "vpnservice_id", length = 100)
    public String getVpnserviceId() {
        return vpnserviceId;
    }
    public void setVpnserviceId(String vpnserviceId) {
        this.vpnserviceId = vpnserviceId;
    }
    @Column(name = "ike_id", length = 100)
    public String getIkeId() {
        return ikeId;
    }
    public void setIkeId(String ikeId) {
        this.ikeId = ikeId;
    }
    @Column(name = "ipsec_id", length = 100)
    public String getIpsecId() {
        return ipsecId;
    }
    public void setIpsecId(String ipsecId) {
        this.ipsecId = ipsecId;
    }
    @Column(name = "vpn_name", length = 50)
    public String getVpnName() {
        return vpnName;
    }
    public void setVpnName(String vpnName) {
        this.vpnName = vpnName;
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
    @Column(name = "network_id", length = 100)
    public String getNetworkId() {
        return networkId;
    }
    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }
    @Column(name = "peer_address", length = 100)
    public String getPeerAddress() {
        return peerAddress;
    }
    public void setPeerAddress(String peerAddress) {
        this.peerAddress = peerAddress;
    }
    @Column(name = "peer_id", length = 200)
    public String getPeerId() {
        return peerId;
    }
    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }
    @Column(name = "peer_cidrs", length = 2000)
    public String getPeerCidrs() {
        return peerCidrs;
    }
    public void setPeerCidrs(String peerCidrs) {
        this.peerCidrs = peerCidrs;
    }
    @Column(name = "mtu", length = 9)
    public Long getMtu() {
        return mtu;
    }
    public void setMtu(Long mtu) {
        this.mtu = mtu;
    }
    @Column(name = "dpd_action", length = 50)
    public String getDpdAction() {
        return dpdAction;
    }
    public void setDpdAction(String dpdAction) {
        this.dpdAction = dpdAction;
    }
    @Column(name = "dpd_interval", length = 9)
    public Long getDpdInterval() {
        return dpdInterval;
    }
    public void setDpdInterval(Long dpdInterval) {
        this.dpdInterval = dpdInterval;
    }
    @Column(name = "dpd_timeout", length = 9)
    public Long getDpdTimeout() {
        return dpdTimeout;
    }
    public void setDpdTimeout(Long dpdTimeout) {
        this.dpdTimeout = dpdTimeout;
    }
    @Column(name = "psk_key", length = 80)
    public String getPskKey() {
        return pskKey;
    }
    public void setPskKey(String pskKey) {
        this.pskKey = pskKey;
    }
    @Column(name = "initiator", length = 20)
    public String getInitiator() {
        return initiator;
    }
    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }
    @Column(name = "ike_encryption", length = 10)
    public String getIkeEncryption() {
        return ikeEncryption;
    }
    public void setIkeEncryption(String ikeEncryption) {
        this.ikeEncryption = ikeEncryption;
    }
    @Column(name = "ike_version", length = 10)
    public String getIkeVersion() {
        return ikeVersion;
    }
    public void setIkeVersion(String ikeVersion) {
        this.ikeVersion = ikeVersion;
    }
    @Column(name = "ike_auth", length = 10)
    public String getIkeAuth() {
        return ikeAuth;
    }
    public void setIkeAuth(String ikeAuth) {
        this.ikeAuth = ikeAuth;
    }
    @Column(name = "ike_negotiation", length = 10)
    public String getIkeNegotiation() {
        return ikeNegotiation;
    }
    public void setIkeNegotiation(String ikeNegotiation) {
        this.ikeNegotiation = ikeNegotiation;
    }
    @Column(name = "ike_lifetime", length = 9)
    public Long getIkeLifetime() {
        return ikeLifetime;
    }
    public void setIkeLifetime(Long ikeLifetime) {
        this.ikeLifetime = ikeLifetime;
    }
    @Column(name = "ike_dh", length = 10)
    public String getIkeDh() {
        return ikeDh;
    }
    public void setIkeDh(String ikeDh) {
        this.ikeDh = ikeDh;
    }
    @Column(name = "ipsec_encryption", length = 10)
    public String getIpsecEncryption() {
        return ipsecEncryption;
    }
    public void setIpsecEncryption(String ipsecEncryption) {
        this.ipsecEncryption = ipsecEncryption;
    }
    @Column(name = "ipsec_protocol", length = 10)
    public String getIpsecProtocol() {
        return ipsecProtocol;
    }
    public void setIpsecProtocol(String ipsecProtocol) {
        this.ipsecProtocol = ipsecProtocol;
    }
    @Column(name = "ipsec_auth", length = 10)
    public String getIpsecAuth() {
        return ipsecAuth;
    }
    public void setIpsecAuth(String ipsecAuth) {
        this.ipsecAuth = ipsecAuth;
    }
    @Column(name = "ipsec_encapsulation", length = 10)
    public String getIpsecEncapsulation() {
        return ipsecEncapsulation;
    }
    public void setIpsecEncapsulation(String ipsecEncapsulation) {
        this.ipsecEncapsulation = ipsecEncapsulation;
    }
    @Column(name = "ipsec_lifetime", length = 9)
    public Long getIpsecLifetime() {
        return ipsecLifetime;
    }
    public void setIpsecLifetime(Long ipsecLifetime) {
        this.ipsecLifetime = ipsecLifetime;
    }
    @Column(name = "ipsec_dh", length = 10)
    public String getIpsecDh() {
        return ipsecDh;
    }
    public void setIpsecDh(String ipsecDh) {
        this.ipsecDh = ipsecDh;
    }
    @Column(name = "create_name", length = 100)
    public String getCreateName() {
        return createName;
    }
    public void setCreateName(String createName) {
        this.createName = createName;
    }
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_time", length = 19)
    public Date getCreateTime() {
        return createTime;
    }
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    @Column(name = "dc_id", length = 100)
    public String getDcId() {
        return dcId;
    }
    public void setDcId(String dcId) {
        this.dcId = dcId;
    }
    @Column(name = "prj_id", length = 100)
    public String getPrjId() {
        return prjId;
    }
    public void setPrjId(String prjId) {
        this.prjId = prjId;
    }
    @Column(name = "cus_id", length = 32)
    public String getCusId() {
        return cusId;
    }
    public void setCusId(String cusId) {
        this.cusId = cusId;
    }

}
