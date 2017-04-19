package com.eayun.eayunstack.model;

public class Vpn {
    private String id;
    private String name;
    private String status;
    private String tenant_id;
    private String router_id;
    private String subnet_id;
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
    public String getRouter_id() {
        return router_id;
    }
    public void setRouter_id(String router_id) {
        this.router_id = router_id;
    }
    public String getSubnet_id() {
        return subnet_id;
    }
    public void setSubnet_id(String subnet_id) {
        this.subnet_id = subnet_id;
    }
    public String getAdmin_state_up() {
        return admin_state_up;
    }
    public void setAdmin_state_up(String admin_state_up) {
        this.admin_state_up = admin_state_up;
    }
    
}
