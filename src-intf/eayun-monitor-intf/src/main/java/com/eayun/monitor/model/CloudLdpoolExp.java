package com.eayun.monitor.model;

/**
 * 负载均衡成员异常情况扩展类
 *                       
 * @Filename: CloudLdpoolExp.java
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
public class CloudLdpoolExp extends BaseCloudLdpoolExp {
	
	private String poolName;		//负载均衡名称
	
	private String memberName;		//成员名称
	
	private String floatIp;			//公网IP地址
	
	private String healthName;		//健康检查名称
	
	private String roleName;		//角色名称
	
	private String dcName;			//数据中心名称
	
	private String cusName;			//客户名称
	
	private String prjName;			//项目名称

	public String getPoolName() {
		return poolName;
	}

	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}

	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}

	public String getFloatIp() {
		return floatIp;
	}

	public void setFloatIp(String floatIp) {
		this.floatIp = floatIp;
	}

	public String getHealthName() {
		return healthName;
	}

	public void setHealthName(String healthName) {
		this.healthName = healthName;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getDcName() {
		return dcName;
	}

	public void setDcName(String dcName) {
		this.dcName = dcName;
	}

	public String getCusName() {
		return cusName;
	}

	public void setCusName(String cusName) {
		this.cusName = cusName;
	}

	public String getPrjName() {
		return prjName;
	}

	public void setPrjName(String prjName) {
		this.prjName = prjName;
	}

	
}
