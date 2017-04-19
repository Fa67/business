package com.eayun.virtualization.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 * @author jingang.liu@eayun.com to beijing
 * @date 2016年4月13日
 */
@Entity
@Table(name = "cloud_outip")
public class BaseCloudOutIp implements java.io.Serializable{

	/**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -15370219025206222L;
    private String ipId;						//ipid
	private String dcId;						//数据中心ID
	private String netId;						//网络ID
	private String subnetId;					//子网ID
	private String usedType;					//使用类型
	private String ipVersion;					//IP版本（4或6）
	private String ipAddress;					//IP地址
	
	private String createName;					//创建者
	private String createTime;					//创建时间
	public BaseCloudOutIp() {
	}
	public BaseCloudOutIp(String ipId,String dcId,String netId,String subnetId,String usedType,
			String ipVersion,String ipAddress,String createName,String createTime){
		this.ipId = ipId;
		this.dcId = dcId;
		this.netId = netId;
		this.subnetId = subnetId;
		this.usedType = usedType;
		this.ipVersion = ipVersion;
		this.ipAddress = ipAddress;
		this.createName = createName;
		this.createTime = createTime;		
	}

	@Id
	@GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "ip_id", unique = true, nullable = false, length = 100)
	public String getIpId() {
		return ipId;
	}

	public void setIpId(String ipId) {
		this.ipId = ipId;
	}

	@Column(name = "dc_id", length = 100)
	public String getDcId() {
		return dcId;
	}

	public void setDcId(String dcId) {
		this.dcId = dcId;
	}

	@Column(name = "net_id", length = 100)
	public String getNetId() {
		return netId;
	}

	public void setNetId(String netId) {
		this.netId = netId;
	}

	@Column(name = "subnet_id", length = 100)
	public String getSubnetId() {
		return subnetId;
	}

	public void setSubnetId(String subnetId) {
		this.subnetId = subnetId;
	}

	@Column(name = "used_type", length = 100)
	public String getUsedType() {
		return usedType;
	}

	public void setUsedType(String usedType) {
		this.usedType = usedType;
	}

	@Column(name = "ip_version", length = 1)
	public String getIpVersion() {
		return ipVersion;
	}

	public void setIpVersion(String ipVersion) {
		this.ipVersion = ipVersion;
	}

	@Column(name = "ip_address", length = 100)
	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	@Column(name = "create_name", length = 100)
	public String getCreateName() {
		return createName;
	}

	public void setCreateName(String createName) {
		this.createName = createName;
	}

	@Column(name = "create_time", length = 100)
	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	
	
	
}
