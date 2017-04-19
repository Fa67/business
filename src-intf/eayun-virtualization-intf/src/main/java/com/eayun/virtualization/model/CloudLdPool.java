package com.eayun.virtualization.model;

import java.math.BigDecimal;
import java.util.List;

import com.eayun.common.util.BeanUtils;

public class CloudLdPool extends BaseCloudLdPool {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String subnetName;
	private String projectName;
	private String vipName;
	private String isDeleting = "not";
	private String countNum ;//资源池绑定监控数量
	private String adminStateupStr;
	private String cidr ;
	private int count ;
	private String statusForPool;
	private String tagName;
	private String lbMethodCn;
	private Long connectionLimit;
	private Long vipPort;
	private String subnetIp;
	private String floatIp;
	private String floatId;
	private boolean isCheckMonitor;
	private String monitorStatus;
	private String dcName;
	private String prjName;
	private String netName;
	private String subnetCidr;
	private List<CloudLdMember> members;
	private List<String> monitors;
	private String portId;
	/* 用户中心改版计费相关 */
    private String payTypeStr;
    private String orderNo;
    private int buyCycle;
    private BigDecimal price;
    
    private String cusId;
    private String monitor;
    
    
    

	public String getMonitor() {
		return monitor;
	}

	public void setMonitor(String monitor) {
		this.monitor = monitor;
	}

	public String getCusId() {
        return cusId;
    }

    public void setCusId(String cusId) {
        this.cusId = cusId;
    }

    public String getCusOrg() {
		return cusOrg;
	}

	public void setCusOrg(String cusOrg) {
		this.cusOrg = cusOrg;
	}

	private String cusOrg;
	
	public CloudLdPool(){
		super();
	}
	
	public CloudLdPool(BaseCloudLdPool base){
		BeanUtils.copyPropertiesByModel(this, base);
	}
	
	public String getLbMethodCn() {
		return lbMethodCn;
	}
	public void setLbMethodCn(String lbMethodCn) {
		this.lbMethodCn = lbMethodCn;
	}
	public String getTagName() {
		return tagName;
	}
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
	public String getStatusForPool() {
		return statusForPool;
	}
	public void setStatusForPool(String statusForPool) {
		this.statusForPool = statusForPool;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public String getCidr() {
		return cidr;
	}
	public void setCidr(String cidr) {
		this.cidr = cidr;
	}
	public String getSubnetName() {
		return subnetName;
	}
	public void setSubnetName(String subnetName) {
		this.subnetName = subnetName;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public String getVipName() {
		return vipName;
	}
	public void setVipName(String vipName) {
		this.vipName = vipName;
	}
	public String getIsDeleting() {
		return isDeleting;
	}
	public void setIsDeleting(String isDeleting) {
		this.isDeleting = isDeleting;
	}
	public String getCountNum() {
		return countNum;
	}
	public void setCountNum(String countNum) {
		this.countNum = countNum;
	}
	public String getAdminStateupStr() {
		return adminStateupStr;
	}
	public void setAdminStateupStr(String adminStateupStr) {
		this.adminStateupStr = adminStateupStr;
	}
	public Long getConnectionLimit() {
		return connectionLimit;
	}
	public void setConnectionLimit(Long connectionLimit) {
		this.connectionLimit = connectionLimit;
	}
	public Long getVipPort() {
		return vipPort;
	}
	public void setVipPort(Long vipPort) {
		this.vipPort = vipPort;
	}
	public String getSubnetIp() {
		return subnetIp;
	}
	public void setSubnetIp(String subnetIp) {
		this.subnetIp = subnetIp;
	}
	public String getFloatIp() {
		return floatIp;
	}
	public void setFloatIp(String floatIp) {
		this.floatIp = floatIp;
	}
	public boolean isCheckMonitor() {
		return isCheckMonitor;
	}
	public void setCheckMonitor(boolean isCheckMonitor) {
		this.isCheckMonitor = isCheckMonitor;
	}
	public String getMonitorStatus() {
		return monitorStatus;
	}
	public void setMonitorStatus(String monitorStatus) {
		this.monitorStatus = monitorStatus;
	}
	public String getDcName() {
		return dcName;
	}
	public void setDcName(String dcName) {
		this.dcName = dcName;
	}
	public String getPrjName() {
		return prjName;
	}
	public void setPrjName(String prjName) {
		this.prjName = prjName;
	}
	public String getNetName() {
		return netName;
	}
	public void setNetName(String netName) {
		this.netName = netName;
	}
	public String getSubnetCidr() {
		return subnetCidr;
	}
	public void setSubnetCidr(String subnetCidr) {
		this.subnetCidr = subnetCidr;
	}
	public List<CloudLdMember> getMembers() {
		return members;
	}
	public void setMembers(List<CloudLdMember> members) {
		this.members = members;
	}
	public List<String> getMonitors() {
		return monitors;
	}
	public void setMonitors(List<String> monitors) {
		this.monitors = monitors;
	}
	public String getFloatId() {
		return floatId;
	}
	public void setFloatId(String floatId) {
		this.floatId = floatId;
	}
	public String getPortId() {
		return portId;
	}
	public void setPortId(String portId) {
		this.portId = portId;
	}
	public String getPayTypeStr() {
		return payTypeStr;
	}
	public void setPayTypeStr(String payTypeStr) {
		this.payTypeStr = payTypeStr;
	}
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public int getBuyCycle() {
		return buyCycle;
	}
	public void setBuyCycle(int buyCycle) {
		this.buyCycle = buyCycle;
	}
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
}
