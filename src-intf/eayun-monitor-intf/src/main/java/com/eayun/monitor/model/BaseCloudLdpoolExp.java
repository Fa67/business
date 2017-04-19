package com.eayun.monitor.model;

import java.util.Date;

/**
 * 负载均衡成员异常情况实体类，存入mongo中
 * 赋值时除普通模式时role不赋值外，所有字段必须赋值
 * 
 * @Filename: BaseCloudLdpoolExp.java
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
public class BaseCloudLdpoolExp{
	
    private Date timestamp;				//当前记录时间
    
    private Date expTime;				//成员异常时间（如获取不到则记为当前记录的时间）
    
    private Date realTime;				//数据实际插入时间
	
    private String poolId;				//负载均衡Id
    
    private String mode;				//负载均衡模式
    
    private String projectId;			//项目Id
    
    private String memberId;			//成员Id
    
    private String role;				//角色
    
    private String healthId;			//健康检查Id
    
    private String expDetails;			//健康检查事件
    
    private String isRepair;			//是否需要修复,1:是；0：否(默认是1)
    
    private Long port;					//成员端口
    
    private String vmIp;				//成员受管子网IP
    
    private String vmId;				//成员对应主机ID

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Date getExpTime() {
		return expTime;
	}

	public void setExpTime(Date expTime) {
		this.expTime = expTime;
	}

	public String getPoolId() {
		return poolId;
	}

	public void setPoolId(String poolId) {
		this.poolId = poolId;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getHealthId() {
		return healthId;
	}

	public void setHealthId(String healthId) {
		this.healthId = healthId;
	}

	public String getExpDetails() {
		return expDetails;
	}

	public void setExpDetails(String expDetails) {
		this.expDetails = expDetails;
	}

	public String getIsRepair() {
		return isRepair;
	}

	public void setIsRepair(String isRepair) {
		this.isRepair = isRepair;
	}

	public Long getPort() {
		return port;
	}

	public void setPort(Long port) {
		this.port = port;
	}

	public String getVmIp() {
		return vmIp;
	}

	public void setVmIp(String vmIp) {
		this.vmIp = vmIp;
	}

	public String getVmId() {
		return vmId;
	}

	public void setVmId(String vmId) {
		this.vmId = vmId;
	}

	public Date getRealTime() {
		return realTime;
	}

	public void setRealTime(Date realTime) {
		this.realTime = realTime;
	}
    
}
