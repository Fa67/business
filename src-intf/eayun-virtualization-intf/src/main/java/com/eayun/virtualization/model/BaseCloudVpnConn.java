package com.eayun.virtualization.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.StringUtils;

import com.eayun.eayunstack.model.VpnConnection;

@Entity
@Table(name = "cloud_vpnconn")
public class BaseCloudVpnConn implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 110638178395709405L;

    private String vpnId;                       //vpn id
    private String vpnName;                     //vpn 名称
    private String vpnStatus;                   //vpn 状态
    private String dcId;                        //数据中心id
    private String prjId;                       //项目id
    private String peerAddress;                 //对端网关
    private String peerId;                      //对端路由id
    private String peerCidrs;                   //对端网段
    private String pskKey;                      //与共享密钥
    private Long mtu;                           //最大传输单元
    private String dpdAction;                   //失效处理
    private Long dpdInterval;                   //检测时间(秒)
    private Long dpdTimeout;                    //超时(秒)
    private String initiator;                   //发起状态
    private String vpnserviceId;
    private String ipsecId;
    private String ikeId;
    private Date createTime;                    //创建时间
    private String payType;                     //付款方式
    private String chargeState;                 //计费状态
    private Date endTime;                       //到期时间
    private String isVisible;                   //是否展现给用户看：1展示，0隐藏
    @Id
    @Column(name = "vpn_id", unique = true, nullable = false, length = 100)
    public String getVpnId() {
        return vpnId;
    }
    public void setVpnId(String vpnId) {
        this.vpnId = vpnId;
    }
    @Column(name = "vpn_name", length = 50)
    public String getVpnName() {
        return vpnName;
    }
    public void setVpnName(String vpnName) {
        this.vpnName = vpnName;
    }
    @Column(name = "vpn_status", length = 50)
    public String getVpnStatus() {
        return vpnStatus;
    }
    public void setVpnStatus(String vpnStatus) {
        this.vpnStatus = vpnStatus;
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
    @Column(name = "psk_key", length = 80)
    public String getPskKey() {
        return pskKey;
    }
    public void setPskKey(String pskKey) {
        this.pskKey = pskKey;
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
    @Column(name = "initiator", length = 20)
    public String getInitiator() {
        return initiator;
    }
    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }
    @Column(name = "vpnservice_id", length = 100)
    public String getVpnserviceId() {
        return vpnserviceId;
    }
    public void setVpnserviceId(String vpnserviceId) {
        this.vpnserviceId = vpnserviceId;
    }
    @Column(name = "ipsec_id", length = 100)
    public String getIpsecId() {
        return ipsecId;
    }
    public void setIpsecId(String ipsecId) {
        this.ipsecId = ipsecId;
    }
    @Column(name = "ike_id", length = 100)
    public String getIkeId() {
        return ikeId;
    }
    public void setIkeId(String ikeId) {
        this.ikeId = ikeId;
    }
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_time", length = 19)
    public Date getCreateTime() {
        return createTime;
    }
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    @Column(name = "pay_type", length = 1)
    public String getPayType() {
        return payType;
    }
    public void setPayType(String payType) {
        this.payType = payType;
    }
    @Column(name = "charge_state", length = 1)
    public String getChargeState() {
        return chargeState;
    }
    public void setChargeState(String chargeState) {
        this.chargeState = chargeState;
    }
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_time", length = 19)
    public Date getEndTime() {
        return endTime;
    }
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    @Column(name = "is_visible", length = 1)
    public String getIsVisible() {
        return isVisible;
    }
    public void setIsVisible(String isVisible) {
        this.isVisible = isVisible;
    }
    public BaseCloudVpnConn(){}
    
    public BaseCloudVpnConn(VpnConnection vpnConnection, String dcId) {
        if (null != vpnConnection) {
            this.vpnId = vpnConnection.getId();
            this.vpnName = vpnConnection.getName();
            this.vpnStatus = vpnConnection.getStatus();
            this.prjId = vpnConnection.getTenant_id();
            this.dcId = dcId;
            this.peerId = vpnConnection.getPeer_id();
            this.peerCidrs = StringUtils.join(vpnConnection.getPeer_cidrs(), ",");
            this.peerAddress = vpnConnection.getPeer_address();
            this.pskKey = vpnConnection.getPsk();
            this.initiator = vpnConnection.getInitiator();
            this.mtu = Long.valueOf(vpnConnection.getMtu());
            this.dpdAction = vpnConnection.getDpd().getAction();
            this.dpdInterval = Long.valueOf(vpnConnection.getDpd().getInterval());
            this.dpdTimeout = Long.valueOf(vpnConnection.getDpd().getTimeout());
            this.vpnserviceId = vpnConnection.getVpnservice_id();
            this.ikeId = vpnConnection.getIkepolicy_id();
            this.ipsecId = vpnConnection.getIpsecpolicy_id();
        }
    }
}
