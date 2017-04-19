package com.eayun.virtualization.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.eayun.eayunstack.model.VpnIkePolicy;

@Entity
@Table(name = "cloud_vpnikepolicy")
public class BaseCloudVpnIkePolicy implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3819182192225583464L;

    private String ikeId;                   //ike策略的id
    private String ikeName;                 //ike策略的名称
    private String ikeVersion;              //版本
    private String prjId;                   //项目id
    private String authAlgorithm;           //认证算法
    private String encryption;              //加密算法
    private String negotiation;             //协商模式
    private Long lifetimeValue;             //生存周期(秒)
    private String dhAlgorithm;             //DH算法
    @Id
    @Column(name = "ike_id", unique = true, nullable = false, length = 100)
    public String getIkeId() {
        return ikeId;
    }
    public void setIkeId(String ikeId) {
        this.ikeId = ikeId;
    }
    @Column(name = "ike_name", length = 50)
    public String getIkeName() {
        return ikeName;
    }
    public void setIkeName(String ikeName) {
        this.ikeName = ikeName;
    }
    @Column(name = "ike_version", length = 10)
    public String getIkeVersion() {
        return ikeVersion;
    }
    public void setIkeVersion(String ikeVersion) {
        this.ikeVersion = ikeVersion;
    }
    @Column(name = "prj_id", length = 100)
    public String getPrjId() {
        return prjId;
    }
    public void setPrjId(String prjId) {
        this.prjId = prjId;
    }
    @Column(name = "auth_algorithm", length = 10)
    public String getAuthAlgorithm() {
        return authAlgorithm;
    }
    public void setAuthAlgorithm(String authAlgorithm) {
        this.authAlgorithm = authAlgorithm;
    }
    @Column(name = "encryption_algorithm", length = 10)
    public String getEncryption() {
        return encryption;
    }
    public void setEncryption(String encryption) {
        this.encryption = encryption;
    }
    @Column(name = "negotiation_mode", length = 10)
    public String getNegotiation() {
        return negotiation;
    }
    public void setNegotiation(String negotiation) {
        this.negotiation = negotiation;
    }
    @Column(name = "lifetime_value", length = 9)
    public Long getLifetimeValue() {
        return lifetimeValue;
    }
    public void setLifetimeValue(Long lifetimeValue) {
        this.lifetimeValue = lifetimeValue;
    }
    @Column(name = "dh_algorithm", length = 10)
    public String getDhAlgorithm() {
        return dhAlgorithm;
    }
    public void setDhAlgorithm(String dhAlgorithm) {
        this.dhAlgorithm = dhAlgorithm;
    }
    
    public BaseCloudVpnIkePolicy(){}
    
    public BaseCloudVpnIkePolicy(String datacenterId, VpnIkePolicy ikePolicy) {
        this.ikeName = ikePolicy.getName();
        this.ikeVersion = ikePolicy.getIke_version();
        this.prjId = ikePolicy.getTenant_id();
        this.authAlgorithm = ikePolicy.getAuth_algorithm();
        this.encryption = ikePolicy.getEncryption_algorithm();
        this.negotiation = ikePolicy.getPhase1_negotiation_mode();
        this.lifetimeValue = Long.valueOf(ikePolicy.getLifetime().getValue());
        this.dhAlgorithm = ikePolicy.getPfs();
    }
    
}
