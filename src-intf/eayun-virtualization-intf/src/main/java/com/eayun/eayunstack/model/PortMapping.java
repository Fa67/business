package com.eayun.eayunstack.model;

public class PortMapping {
    private String id;
    private String tenant_id;
    private String router_id;
    private String router_port;
    private String destination_ip;
    private String destination_port;
    private String name;
    private String admin_state_up;
    private String status;
    private String protocol;
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
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
    public String getRouter_port() {
        return router_port;
    }
    public void setRouter_port(String router_port) {
        this.router_port = router_port;
    }
    public String getDestination_ip() {
        return destination_ip;
    }
    public void setDestination_ip(String destination_ip) {
        this.destination_ip = destination_ip;
    }
    public String getDestination_port() {
        return destination_port;
    }
    public void setDestination_port(String destination_port) {
        this.destination_port = destination_port;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getAdmin_state_up() {
        return admin_state_up;
    }
    public void setAdmin_state_up(String admin_state_up) {
        this.admin_state_up = admin_state_up;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getProtocol() {
        return protocol;
    }
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    
}
