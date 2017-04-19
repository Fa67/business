package com.eayun.datacenter.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "dc_datacenter")
public class BaseDcDataCenter implements java.io.Serializable {

    private static final long serialVersionUID = 8697781184581281173L;
    private String            id;                                      //主键
    private String            name;                                    //数据中心名称
    private String            dcType;                                  //支持的虚拟化平台(默认是openstack)
    private String            dcAddress;                               //数据中心地址IP，对应IPURL
    private String            VCenterUsername;                         //数据中心管理员帐号
    private String            VCenterPassword;                         //数据中心管理员密码
    private BigDecimal        cabinetCapacity;                         //可容机柜数量
    private String            ecManagenodeOutnetip;                    //EayunCloud管理节点外网IP
    private String            ecManagenodeAuth;                        //EayunCloud管理节点外网鉴权信息
    private String            osManagenodeInnetip;                     //OpenStack管理节点内网IP
    private String            osManagenodeInnetAuth;                   //EayunCloud管理节点内网鉴权信息
    private String            osAdminProjectId;                        //OpenStack管理项目的ID
    private String            creUser;                                 //创建人ID
    private Timestamp         creDate;                                 //创建日期
    private String            osKeystoneRegion;                        //OpenStack的keystone  region
    private String            osCommonRegion;                          //OpenStack的common region
    private String            dcDesc;                                  //数据中心描述

    private String            nagiosUser;                              //
    private String            nagiosPassword;                          //
    private String            quotaCpu;                                //
    private String            quotaMemory;                             //
    private String            quotaNatwork;                            //
    private String            quotaDisk;                               //
    private String            nagiosIp;                                //

    private Float             cpuAllocationRatio;                      //cpu超配比
    private Float             diskAllocationRatio;                     //硬盘超配比
    private Float             ramAllocationRatio;                      //内存超配比
    
    private String 			  dcDns;								   //dns地址
    
    private Boolean			  apiStatus;							   //API开关状态
    
    private String			  apiDcCode;							   //API代号
    private String			  commonRegionUrlType;					   //commonRegion对应的URL类型
    
    private String            provinces;                               //分布地点

    @Id
    @Column(name = "ID", unique = true, nullable = false, length = 50)
    public String getId() {
        return this.id;
    }
    @Column(name = "DC_DNS", length = 100)
    public String getDcDns() {
		return dcDns;
	}

	public void setDcDns(String dcDns) {
		this.dcDns = dcDns;
	}

	public void setId(String id) {
        this.id = id;
    }

    @Column(name = "DC_NAME", length = 50)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "DC_TYPE", length = 50)
    public String getDcType() {
        return this.dcType;
    }

    public void setDcType(String dcType) {
        this.dcType = dcType;
    }

    @Column(name = "DC_ADDRESS", length = 50)
    public String getDcAddress() {
        return this.dcAddress;
    }

    public void setDcAddress(String dcAddress) {
        this.dcAddress = dcAddress;
    }

    @Column(name = "V_CENTER_USERNAME", length = 50)
    public String getVCenterUsername() {
        return this.VCenterUsername;
    }

    public void setVCenterUsername(String VCenterUsername) {
        this.VCenterUsername = VCenterUsername;
    }

    @Column(name = "V_CENTER_PASSWORD", length = 50)
    public String getVCenterPassword() {
        return this.VCenterPassword;
    }

    public void setVCenterPassword(String VCenterPassword) {
        this.VCenterPassword = VCenterPassword;
    }

    @Column(name = "CABINET_CAPACITY", precision = 22, scale = 0)
    public BigDecimal getCabinetCapacity() {
        return this.cabinetCapacity;
    }

    public void setCabinetCapacity(BigDecimal cabinetCapacity) {
        this.cabinetCapacity = cabinetCapacity;
    }

    @Column(name = "EC_MANAGENODE_OUTNETIP", length = 50)
    public String getEcManagenodeOutnetip() {
        return this.ecManagenodeOutnetip;
    }

    public void setEcManagenodeOutnetip(String ecManagenodeOutnetip) {
        this.ecManagenodeOutnetip = ecManagenodeOutnetip;
    }

    @Column(name = "EC_MANAGENODE_AUTH", length = 100)
    public String getEcManagenodeAuth() {
        return this.ecManagenodeAuth;
    }

    public void setEcManagenodeAuth(String ecManagenodeAuth) {
        this.ecManagenodeAuth = ecManagenodeAuth;
    }

    @Column(name = "OS_MANAGENODE_INNETIP", length = 50)
    public String getOsManagenodeInnetip() {
        return this.osManagenodeInnetip;
    }

    public void setOsManagenodeInnetip(String osManagenodeInnetip) {
        this.osManagenodeInnetip = osManagenodeInnetip;
    }

    @Column(name = "OS_MANAGENODE_INNET_AUTH", length = 100)
    public String getOsManagenodeInnetAuth() {
        return this.osManagenodeInnetAuth;
    }

    public void setOsManagenodeInnetAuth(String osManagenodeInnetAuth) {
        this.osManagenodeInnetAuth = osManagenodeInnetAuth;
    }

    @Column(name = "CRE_USER", length = 50)
    public String getCreUser() {
        return this.creUser;
    }

    public void setCreUser(String creUser) {
        this.creUser = creUser;
    }

    @Column(name = "CRE_DATE", length = 11)
    public Timestamp getCreDate() {
        return this.creDate;
    }

    public void setCreDate(Timestamp creDate) {
        this.creDate = creDate;
    }

    @Column(name = "OS_ADMIN_PROJECT_ID", length = 100)
    public String getOsAdminProjectId() {
        return osAdminProjectId;
    }

    public void setOsAdminProjectId(String osAdminProjectId) {
        this.osAdminProjectId = osAdminProjectId;
    }

    @Column(name = "OS_KEYSTONE_REGION", length = 100)
    public String getOsKeystoneRegion() {
        return osKeystoneRegion;
    }

    public void setOsKeystoneRegion(String osKeystoneRegion) {
        this.osKeystoneRegion = osKeystoneRegion;
    }

    @Column(name = "OS_COMMON_REGION", length = 100)
    public String getOsCommonRegion() {
        return osCommonRegion;
    }

    public void setOsCommonRegion(String osCommonRegion) {
        this.osCommonRegion = osCommonRegion;
    }

    @Column(name = "DC_DESC", length = 500)
    public String getDcDesc() {
        return dcDesc;
    }

    public void setDcDesc(String dcDesc) {
        this.dcDesc = dcDesc;
    }

    @Column(name = "NAGIOS_USER", length = 50)
    public String getNagiosUser() {
        return nagiosUser;
    }

    public void setNagiosUser(String nagiosUser) {
        this.nagiosUser = nagiosUser;
    }

    @Column(name = "NAGIOS_PASSWORD", length = 50)
    public String getNagiosPassword() {
        return nagiosPassword;
    }

    public void setNagiosPassword(String nagiosPassword) {
        this.nagiosPassword = nagiosPassword;
    }

    @Column(name = "QUOTA_CPU", length = 50)
    public String getQuotaCpu() {
        return quotaCpu;
    }

    public void setQuotaCpu(String quotaCpu) {
        this.quotaCpu = quotaCpu;
    }

    @Column(name = "QUOTA_MEMORY", length = 50)
    public String getQuotaMemory() {
        return quotaMemory;
    }

    public void setQuotaMemory(String quotaMemory) {
        this.quotaMemory = quotaMemory;
    }

    @Column(name = "QUOTA_NATWORK", length = 50)
    public String getQuotaNatwork() {
        return quotaNatwork;
    }

    public void setQuotaNatwork(String quotaNatwork) {
        this.quotaNatwork = quotaNatwork;
    }

    @Column(name = "QUOTA_DISK", length = 50)
    public String getQuotaDisk() {
        return quotaDisk;
    }

    public void setQuotaDisk(String quotaDisk) {
        this.quotaDisk = quotaDisk;
    }

    @Column(name = "NAGIOS_IP", length = 50)
    public String getNagiosIp() {
        return nagiosIp;
    }

    public void setNagiosIp(String nagiosIp) {
        this.nagiosIp = nagiosIp;
    }

    @Column(name = "CPU_ALLOCATION_RATIO", length = 50)
    public Float getCpuAllocationRatio() {
        return cpuAllocationRatio;
    }

    public void setCpuAllocationRatio(Float cpuAllocationRatio) {
        this.cpuAllocationRatio = cpuAllocationRatio;
    }

    @Column(name = "DISK_ALLOCATION_RATIO", length = 50)
    public Float getDiskAllocationRatio() {
        return diskAllocationRatio;
    }

    public void setDiskAllocationRatio(Float diskAllocationRatio) {
        this.diskAllocationRatio = diskAllocationRatio;
    }

    @Column(name = "RAM_ALLOCATION_RATIO", length = 50)
    public Float getRamAllocationRatio() {
        return ramAllocationRatio;
    }

    public void setRamAllocationRatio(Float ramAllocationRatio) {
        this.ramAllocationRatio = ramAllocationRatio;
    }
    
    @Column(name = "api_status")
	public Boolean getApiStatus() {
		return apiStatus;
	}
	public void setApiStatus(Boolean apiStatus) {
		this.apiStatus = apiStatus;
	}
	@Column(name = "api_dc_code")
	public String getApiDcCode() {
		return apiDcCode;
	}
	public void setApiDcCode(String apiDcCode) {
		this.apiDcCode = apiDcCode;
	}
	@Column(name = "common_region_url_type")
	public String getCommonRegionUrlType() {
		return commonRegionUrlType;
	}
	public void setCommonRegionUrlType(String commonRegionUrlType) {
		this.commonRegionUrlType = commonRegionUrlType;
	}
	@Column(name = "provinces")
    public String getProvinces() {
        return provinces;
    }
    public void setProvinces(String provinces) {
        this.provinces = provinces;
    }
	
    
}