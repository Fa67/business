package com.eayun.virtualization.ecmcvo;

import com.eayun.common.util.BeanUtils;
import com.eayun.virtualization.model.BaseCloudLdPool;

public class CloudLdpoolVoe extends BaseCloudLdPool {

	private static final long serialVersionUID = -4113779022332916485L;
	private String subnetName;
	private String projectName;
	private String vipName;
	
	private String isDeleting = "not";
	private String countNum ;//资源池绑定监控数量
	private String adminStateupStr;
	
	private String dcName;
	private String cusId;
	private String cusName;
	private String cusOrg;//客户组织名称
	private String floatId;//浮动IP的ID
	private String lbMethodName;
	
	public CloudLdpoolVoe(){
		super();
	}
	public CloudLdpoolVoe(BaseCloudLdPool base, String dcName, String prjName, String cusId, String cusName){
		super();
		BeanUtils.copyPropertiesByModel(this, base);
		this.dcName = dcName;
		this.projectName = prjName;
		this.cusId = cusId;
		this.cusName = cusName;
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

	public String getAdminStateupStr() {
		return adminStateupStr;
	}

	public void setAdminStateupStr(String adminStateupStr) {
		this.adminStateupStr = adminStateupStr;
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
	public String getDcName() {
		return dcName;
	}
	public void setDcName(String dcName) {
		this.dcName = dcName;
	}
	public String getCusId() {
		return cusId;
	}
	public void setCusId(String cusId) {
		this.cusId = cusId;
	}
	public String getCusName() {
		return cusName;
	}
	public void setCusName(String cusName) {
		this.cusName = cusName;
	}
	public String getFloatId() {
		return floatId;
	}
	public void setFloatId(String floatId) {
		this.floatId = floatId;
	}
	public String getLbMethodName() {
		return lbMethodName;
	}
	public void setLbMethodName(String lbMethod) {
		//  ROUND_ROBIN对应"轮询"；LEAST_CONNECTIONS对应“最小连接数”；SOURCE_IP对应“源地址”
		this.lbMethodName = "ROUND_ROBIN".equals(lbMethod) ? "轮询" : 
			"LEAST_CONNECTIONS".equals(lbMethod) ? "最小连接数" :
				"SOURCE_IP".equals(lbMethod) ? "源地址" : "";
	}
	public String getCusOrg() {
		return cusOrg;
	}
	public void setCusOrg(String cusOrg) {
		this.cusOrg = cusOrg;
	}
	
}
