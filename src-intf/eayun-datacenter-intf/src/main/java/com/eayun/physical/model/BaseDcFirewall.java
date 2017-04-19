package com.eayun.physical.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;


/**
 * DcFirewall entity. @author MyEclipse Persistence Tools
 */
@Entity
@Table(name="dc_firewall")

public class BaseDcFirewall  implements java.io.Serializable {

    /**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1556960210615754338L;
    // Fields    

     private String id;   //id
     private String name;  //防火墙名称
     private String firewallModel;//防火墙型号
     private String ipAddress;  //IP地址
     private String netThroughput;	//网络吞吐量
     private String dataCenterId;	//数据中心id
     private String cabinetId;	//机柜id
     private String concurrentConn;	//并发连接数
     private String memo;	//防火墙描述
     private String responPerson;	//责任人
     private String responPersonMobile;	//责任人联系电话
     private String creUser;	//创建人
     private String creDate;		//创建时间
     private Double spec;	//规格
     private String firewallId;	//防火墙id


    // Constructors

    /** default constructor */
    public BaseDcFirewall() {
    }

	/** minimal constructor */
    public BaseDcFirewall(String id) {
        this.id = id;
    }
    
    /** full constructor */
    public BaseDcFirewall(String id, String name, String firewallModel, String ipAddress, String netThroughput, String dataCenterId, String cabinetId, String concurrentConn, String memo, String responPerson, String responPersonMobile, String creUser, String creDate, Double spec, String firewallId) {
        this.id = id;
        this.name = name;
        this.firewallModel = firewallModel;
        this.ipAddress = ipAddress;
        this.netThroughput = netThroughput;
        this.dataCenterId = dataCenterId;
        this.cabinetId = cabinetId;
        this.concurrentConn = concurrentConn;
        this.memo = memo;
        this.responPerson = responPerson;
        this.responPersonMobile = responPersonMobile;
        this.creUser = creUser;
        this.creDate = creDate;
        this.spec = spec;
        this.firewallId = firewallId;
    }

   
    // Property accessors
    @GenericGenerator(name="generator", strategy="uuid.hex")
    @Id @GeneratedValue(generator="generator")
    @Column(name="ID", unique=true, nullable=false, length=50)

    public String getId() {
        return this.id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    @Column(name="NAME", length=100)

    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @Column(name="FIREWALL_MODEL", length=50)

    public String getFirewallModel() {
        return this.firewallModel;
    }
    
    public void setFirewallModel(String firewallModel) {
        this.firewallModel = firewallModel;
    }
    
    @Column(name="IP_ADDRESS", length=50)

    public String getIpAddress() {
        return this.ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    @Column(name="NET_THROUGHPUT", length=50)

    public String getNetThroughput() {
        return this.netThroughput;
    }
    
    public void setNetThroughput(String netThroughput) {
        this.netThroughput = netThroughput;
    }
    
    @Column(name="DATA_CENTER_ID", length=50)

    public String getDataCenterId() {
        return this.dataCenterId;
    }
    
    public void setDataCenterId(String dataCenterId) {
        this.dataCenterId = dataCenterId;
    }
    
    @Column(name="CABINET_ID", length=50)

    public String getCabinetId() {
        return this.cabinetId;
    }
    
    public void setCabinetId(String cabinetId) {
        this.cabinetId = cabinetId;
    }
    
    @Column(name="CONCURRENT_CONN", length=50)

    public String getConcurrentConn() {
        return this.concurrentConn;
    }
    
    public void setConcurrentConn(String concurrentConn) {
        this.concurrentConn = concurrentConn;
    }
    
    @Column(name="MEMO", length=500)

    public String getMemo() {
        return this.memo;
    }
    
    public void setMemo(String memo) {
        this.memo = memo;
    }
    
    @Column(name="RESPON_PERSON", length=50)

    public String getResponPerson() {
        return this.responPerson;
    }
    
    public void setResponPerson(String responPerson) {
        this.responPerson = responPerson;
    }
    
    @Column(name="RESPON_PERSON_MOBILE", length=50)

    public String getResponPersonMobile() {
        return this.responPersonMobile;
    }
    
    public void setResponPersonMobile(String responPersonMobile) {
        this.responPersonMobile = responPersonMobile;
    }
    
    @Column(name="CRE_USER", length=50)

    public String getCreUser() {
        return this.creUser;
    }
    
    public void setCreUser(String creUser) {
        this.creUser = creUser;
    }
    
    @Column(name="CRE_DATE", length=11)

    public String getCreDate() {
        return this.creDate;
    }
    
    public void setCreDate(String creDate) {
        this.creDate = creDate;
    }
    
    @Column(name="SPEC", precision=0)

    public Double getSpec() {
        return this.spec;
    }
    
    public void setSpec(Double spec) {
        this.spec = spec;
    }
    
    @Column(name="FIREWALL_ID", length=50)

    public String getFirewallId() {
        return this.firewallId;
    }
    
    public void setFirewallId(String firewallId) {
        this.firewallId = firewallId;
    }

}