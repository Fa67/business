package com.eayun.eayunstack.model;

public class VpnIkePolicy {
    private String id;
    private String name;
    private String ike_version;
    private String tenant_id;
    private String auth_algorithm;
    private String encryption_algorithm;
    private String phase1_negotiation_mode;
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
    public String getIke_version() {
        return ike_version;
    }
    public void setIke_version(String ike_version) {
        this.ike_version = ike_version;
    }
    public String getTenant_id() {
        return tenant_id;
    }
    public void setTenant_id(String tenant_id) {
        this.tenant_id = tenant_id;
    }
    public String getAuth_algorithm() {
        return auth_algorithm;
    }
    public void setAuth_algorithm(String auth_algorithm) {
        this.auth_algorithm = auth_algorithm;
    }
    public String getEncryption_algorithm() {
        return encryption_algorithm;
    }
    public void setEncryption_algorithm(String encryption_algorithm) {
        this.encryption_algorithm = encryption_algorithm;
    }
    public String getPhase1_negotiation_mode() {
        return phase1_negotiation_mode;
    }
    public void setPhase1_negotiation_mode(String phase1_negotiation_mode) {
        this.phase1_negotiation_mode = phase1_negotiation_mode;
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
