package com.eayun.virtualization.ecmcvo;

import java.util.Date;
import java.util.List;

import com.eayun.virtualization.model.BaseCloudSecretKey;

public class CloudSecretKeyVoe extends BaseCloudSecretKey {
	
	private List<SecretKeyVm> svm;
	
	private String prjname;
	
	private String cusorg;
	
	private  String dcname;
	
	



	public String getDcname() {
		return dcname;
	}

	public void setDcname(String dcname) {
		this.dcname = dcname;
	}

	public String getCusorg() {
		return cusorg;
	}

	public void setCusorg(String cusorg) {
		this.cusorg = cusorg;
	}

	public String getPrjname() {
		return prjname;
	}

	public void setPrjname(String prjname) {
		this.prjname = prjname;
	}

	public List<SecretKeyVm> getSvm() {
		return svm;
	}

	public void setSvm(List<SecretKeyVm> svm) {
		this.svm = svm;
	}

	
	
	
	

}
