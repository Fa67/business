package com.eayun.virtualization.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.eayun.eayunstack.model.VpnIpSecPolicy;

@Entity
@Table(name = "cloud_vpnipsecpolicy")
public class BaseCloudVpnIpSecPolicy implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2374523964672991962L;

    private String ipSecId;                     //ipsecId
    private String ipSecName;                   //ipsec名称
    private String prjId;                       //项目id
    private String transform;                   //传输协议
    private String authAlgorithm;               //认证算法
    private String encapsulation;               //封装模式
    private String encryption;                  //加密算法
    private Long lifetimeValue;                 //生命周期(秒)
    private String dhAlgorithm;                 //DH算法
    @Id
    @Column(name = "ipsec_id", unique = true, nullable = false, length = 100)
    public String getIpSecId() {
        return ipSecId;
    }
    public void setIpSecId(String ipSecId) {
        this.ipSecId = ipSecId;
    }
    @Column(name = "ipsec_name", length = 50)
    public String getIpSecName() {
        return ipSecName;
    }
    public void setIpSecName(String ipSecName) {
        this.ipSecName = ipSecName;
    }
    @Column(name = "prj_id", length = 100)
    public String getPrjId() {
        return prjId;
    }
    public void setPrjId(String prjId) {
        this.prjId = prjId;
    }
    @Column(name = "transform_protocol", length = 10)
    public String getTransform() {
        return transform;
    }
    public void setTransform(String transform) {
        this.transform = transform;
    }
    @Column(name = "auth_algorithm", length = 10)
    public String getAuthAlgorithm() {
        return authAlgorithm;
    }
    public void setAuthAlgorithm(String authAlgorithm) {
        this.authAlgorithm = authAlgorithm;
    }
    @Column(name = "encapsulation_mode", length = 10)
    public String getEncapsulation() {
        return encapsulation;
    }
    public void setEncapsulation(String encapsulation) {
        this.encapsulation = encapsulation;
    }
    @Column(name = "encryption_algorithm", length = 10)
    public String getEncryption() {
        return encryption;
    }
    public void setEncryption(String encryption) {
        this.encryption = encryption;
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
    
    public BaseCloudVpnIpSecPolicy(){}
    
    public BaseCloudVpnIpSecPolicy(String datacenterId, VpnIpSecPolicy ipsecPolicy) {
        this.ipSecName = ipsecPolicy.getName();
        this.prjId = ipsecPolicy.getTenant_id();
        this.transform = ipsecPolicy.getTransform_protocol();
        this.authAlgorithm = ipsecPolicy.getAuth_algorithm();
        this.encapsulation = ipsecPolicy.getEncapsulation_mode();
        this.encryption = ipsecPolicy.getEncryption_algorithm();
        this.lifetimeValue = Long.valueOf(ipsecPolicy.getLifetime().getValue());
        this.dhAlgorithm = ipsecPolicy.getPfs();
    }
    
}
