package com.eayun.virtualization.model;

import java.util.Date;

public class EcmcCloudNetwork extends BaseCloudNetwork{	
	
    private static final long serialVersionUID = 4549575350353921017L;
    private String subnetnum;       //子网数量		
	private String prjName;
	private String dcName;
	private String isDeleting;//是否正在删除
	private String isAdminStateup;
	
	public EcmcCloudNetwork(String netId,String netName,Long subNum,String netStatus,String adminStateup,Date createTime,String isShared, String dcId,String dcName){
	    super();
	    this.setNetId(netId);
	    this.setNetName(netName);
	    this.setSubnetnum(subNum.toString());
	    this.setNetStatus(netStatus);
	    this.setAdminStateup(adminStateup);
	    this.setCreateTime(createTime);
	    this.setIsShared(isShared);
	    this.setDcId(dcId);
	    this.setDcName(dcName);
	}
	
	public EcmcCloudNetwork(String subnetnum, String prjName) {
		super();
		this.subnetnum = subnetnum;
		this.prjName = prjName;
	}

	public EcmcCloudNetwork() {
		super();
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

	public String getSubnetnum() {
		return subnetnum;
	}

	public void setSubnetnum(String subnetnum) {
		this.subnetnum = subnetnum;
	}

	public String getIsDeleting() {
		return isDeleting;
	}

	public void setIsDeleting(String isDeleting) {
		this.isDeleting = isDeleting;
	}

	public String getIsAdminStateup() {
		return isAdminStateup;
	}

	public void setIsAdminStateup(String isAdminStateup) {
		this.isAdminStateup = isAdminStateup;
	}
	
	
	
}
