package com.eayun.monitor.bean;

/**
 * 负载均衡资源监控列表展示model
 *                       
 * @Filename: LdPoolIndicator.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2017年3月2日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class LdPoolIndicator extends LdPoolMonitorDetail {
    
    private String ldPoolName;  //负载均衡名称
    
    private String netId;    	//所属网络id
    
    private String netName;  	//所属网络名称
    
    private String dcName;  	//数据中心名称
    
    private String prjName;  	//项目名称
    
    private String cusName;  	//客户名称
    
    private String vmIp;     	//受管子网IP
    
    private String floatIp;    	//公网IP
    
    private String config;     	//配置
    
    private String healthMonitor;    //当前生效健康检查名称
    
    private int expMemberDiff;       //不活跃节点百分比变化
    
    private int expMasterDiff;      //不活跃主节点百分比变化
    
    private int expSalveDiff;      //不活跃从节点百分比变化


	public String getLdPoolName() {
		return ldPoolName;
	}

	public void setLdPoolName(String ldPoolName) {
		this.ldPoolName = ldPoolName;
	}


	public String getNetId() {
		return netId;
	}

	public void setNetId(String netId) {
		this.netId = netId;
	}

	public String getNetName() {
		return netName;
	}

	public void setNetName(String netName) {
		this.netName = netName;
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

	public String getCusName() {
		return cusName;
	}

	public void setCusName(String cusName) {
		this.cusName = cusName;
	}

	public String getVmIp() {
		return vmIp;
	}

	public void setVmIp(String vmIp) {
		this.vmIp = vmIp;
	}

	public String getFloatIp() {
		return floatIp;
	}

	public void setFloatIp(String floatIp) {
		this.floatIp = floatIp;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public String getHealthMonitor() {
		return healthMonitor;
	}

	public void setHealthMonitor(String healthMonitor) {
		this.healthMonitor = healthMonitor;
	}

	public int getExpMemberDiff() {
		return expMemberDiff;
	}

	public void setExpMemberDiff(int expMemberDiff) {
		this.expMemberDiff = expMemberDiff;
	}

	public int getExpMasterDiff() {
		return expMasterDiff;
	}

	public void setExpMasterDiff(int expMasterDiff) {
		this.expMasterDiff = expMasterDiff;
	}

	public int getExpSalveDiff() {
		return expSalveDiff;
	}

	public void setExpSalveDiff(int expSalveDiff) {
		this.expSalveDiff = expSalveDiff;
	}
    
    
}
