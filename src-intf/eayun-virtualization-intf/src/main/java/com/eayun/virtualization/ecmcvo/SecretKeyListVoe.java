package com.eayun.virtualization.ecmcvo;

import java.util.Date;

public class SecretKeyListVoe {
	
	private String skid;
	private String skname;
	private String prjname;
	private String cusorg;
	private String dcname;
	private Date sktime;
	private int vmCount;
	
	public int getVmCount() {
		return vmCount;
	}
	public void setVmCount(int vmCount) {
		this.vmCount = vmCount;
	}
	public String getSkid() {
		return skid;
	}
	public void setSkid(String skid) {
		this.skid = skid;
	}
	public String getSkname() {
		return skname;
	}
	public void setSkname(String skname) {
		this.skname = skname;
	}
	public String getPrjname() {
		return prjname;
	}
	public void setPrjname(String prjname) {
		this.prjname = prjname;
	}
	public String getCusorg() {
		return cusorg;
	}
	public void setCusorg(String cusorg) {
		this.cusorg = cusorg;
	}
	public String getDcname() {
		return dcname;
	}
	public void setDcname(String dcname) {
		this.dcname = dcname;
	}
	public Date getSktime() {
		return sktime;
	}
	public void setSktime(Date sktime) {
		this.sktime = sktime;
	}
	
	
	

}
