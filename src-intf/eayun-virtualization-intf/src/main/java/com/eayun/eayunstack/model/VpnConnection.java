package com.eayun.eayunstack.model;

public class VpnConnection {
    private String id;
    private String name;
    private String status;
    private String tenant_id;
    private String peer_id;
    private String[] peer_cidrs;
    private String peer_address;
    private String psk;
    private String initiator;
    private String mtu;
    private Dpd dpd;
    private String vpnservice_id;
    private String ikepolicy_id;
    private String ipsecpolicy_id;
    private String admin_state_up;
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getTenant_id() {
        return tenant_id;
    }
    public void setTenant_id(String tenant_id) {
        this.tenant_id = tenant_id;
    }
    public String getPeer_id() {
        return peer_id;
    }
    public void setPeer_id(String peer_id) {
        this.peer_id = peer_id;
    }
    public String[] getPeer_cidrs() {
        return peer_cidrs;
    }
    public void setPeer_cidrs(String[] peer_cidrs) {
        this.peer_cidrs = peer_cidrs;
    }
    public String getPeer_address() {
        return peer_address;
    }
    public void setPeer_address(String peer_address) {
        this.peer_address = peer_address;
    }
    public String getPsk() {
        return psk;
    }
    public void setPsk(String psk) {
        this.psk = psk;
    }
    public String getInitiator() {
        return initiator;
    }
    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }
    public String getMtu() {
        return mtu;
    }
    public void setMtu(String mtu) {
        this.mtu = mtu;
    }
    public Dpd getDpd() {
        return dpd;
    }
    public void setDpd(Dpd dpd) {
        this.dpd = dpd;
    }
    public String getVpnservice_id() {
        return vpnservice_id;
    }
    public void setVpnservice_id(String vpnservice_id) {
        this.vpnservice_id = vpnservice_id;
    }
    public String getIkepolicy_id() {
        return ikepolicy_id;
    }
    public void setIkepolicy_id(String ikepolicy_id) {
        this.ikepolicy_id = ikepolicy_id;
    }
    public String getIpsecpolicy_id() {
        return ipsecpolicy_id;
    }
    public void setIpsecpolicy_id(String ipsecpolicy_id) {
        this.ipsecpolicy_id = ipsecpolicy_id;
    }
    public String getAdmin_state_up() {
        return admin_state_up;
    }
    public void setAdmin_state_up(String admin_state_up) {
        this.admin_state_up = admin_state_up;
    }
}
