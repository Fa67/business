package com.eayun.eayunstack.model;

public class VpnIpSecPolicy {
    private String id;
    private String name;
    private String tenant_id;
    private String transform_protocol;
    private String auth_algorithm;
    private String encapsulation_mode;
    private String encryption_algorithm;
    private Lifetime lifetime;
    private String pfs;
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
    public String getTenant_id() {
        return tenant_id;
    }
    public void setTenant_id(String tenant_id) {
        this.tenant_id = tenant_id;
    }
    public String getTransform_protocol() {
        return transform_protocol;
    }
    public void setTransform_protocol(String transform_protocol) {
        this.transform_protocol = transform_protocol;
    }
    public String getAuth_algorithm() {
        return auth_algorithm;
    }
    public void setAuth_algorithm(String auth_algorithm) {
        this.auth_algorithm = auth_algorithm;
    }
    public String getEncapsulation_mode() {
        return encapsulation_mode;
    }
    public void setEncapsulation_mode(String encapsulation_mode) {
        this.encapsulation_mode = encapsulation_mode;
    }
    public String getEncryption_algorithm() {
        return encryption_algorithm;
    }
    public void setEncryption_algorithm(String encryption_algorithm) {
        this.encryption_algorithm = encryption_algorithm;
    }
    public Lifetime getLifetime() {
        return lifetime;
    }
    public void setLifetime(Lifetime lifetime) {
        this.lifetime = lifetime;
    }
    public String getPfs() {
        return pfs;
    }
    public void setPfs(String pfs) {
        this.pfs = pfs;
    }
    
}
