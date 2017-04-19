package com.eayun.virtualization.ecmcvo;

import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.BoolUtil;
import com.eayun.virtualization.model.BaseCloudLdMember;

public class CloudLdmemberVoe extends BaseCloudLdMember {
	
	private static final long serialVersionUID = 6380442807677170322L;
	
	private String isDeleting = "not";
	private String poolName;
	private String projectName;
	private String adminStateupStr;
	private String dcName;
	private String cusId;
	private String cusName;
	private String vmName;//云主机名称
	
	public CloudLdmemberVoe(){}
	
	public CloudLdmemberVoe(BaseCloudLdMember baseMember, String dcName, String prjName, String cusId, String cusName, String vmName){
		super();
		BeanUtils.copyPropertiesByModel(this, baseMember);
		this.dcName = dcName;
		this.projectName = prjName;
		this.cusId = cusId;
		this.cusName = cusName;
		this.vmName = vmName;
		this.setAdminStateupStr(BoolUtil.bool2Str(baseMember.getAdminStateup()));
	}
	
	public String getAdminStateupStr() {
		return adminStateupStr;
	}

	public void setAdminStateupStr(String adminStateupStr) {
		this.adminStateupStr = adminStateupStr;
	}

	public String getPoolName() {
		return poolName;
	}

	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getIsDeleting() {
		return isDeleting;
	}

	public void setIsDeleting(String isDeleting) {
		this.isDeleting = isDeleting;
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

	public String getVmName() {
		return vmName;
	}

	public void setVmName(String vmName) {
		this.vmName = vmName;
	}
	
}
