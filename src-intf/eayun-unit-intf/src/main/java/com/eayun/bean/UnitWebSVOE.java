package com.eayun.bean;

import java.util.List;

import com.eayun.unit.model.BaseUnitInfo;
import com.eayun.unit.model.BaseWebSiteInfo;
import com.eayun.unit.model.WebSiteIP;

public class UnitWebSVOE extends BaseUnitInfo{
	
	private List<WebSiteIP> webs;
	
	private String UnitNaturestr;
	private String applyId;
	private String certificateTypestr;
	
	private String dutyCertificateTypestr;
	
	private Integer status;
	private Integer returnType;
	private  Integer recordType;
	public List<WebSiteIP> getWebs() {
		return webs;
	}
	public void setWebs(List<WebSiteIP> webs) {
		this.webs = webs;
	}
	public String getUnitNaturestr() {
		return UnitNaturestr;
	}
	public void setUnitNaturestr(String unitNaturestr) {
		UnitNaturestr = unitNaturestr;
	}
	public String getApplyId() {
		return applyId;
	}
	public void setApplyId(String applyId) {
		this.applyId = applyId;
	}
	public String getCertificateTypestr() {
		return certificateTypestr;
	}
	public void setCertificateTypestr(String certificateTypestr) {
		this.certificateTypestr = certificateTypestr;
	}
	public String getDutyCertificateTypestr() {
		return dutyCertificateTypestr;
	}
	public void setDutyCertificateTypestr(String dutyCertificateTypestr) {
		this.dutyCertificateTypestr = dutyCertificateTypestr;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getReturnType() {
		return returnType;
	}
	public void setReturnType(Integer returnType) {
		this.returnType = returnType;
	}
	public Integer getRecordType() {
		return recordType;
	}
	public void setRecordType(Integer recordType) {
		this.recordType = recordType;
	}
	public UnitWebSVOE(List<WebSiteIP> webs, String unitNaturestr, String applyId, String certificateTypestr,
			String dutyCertificateTypestr, Integer status, Integer returnType, Integer recordType) {
		this.webs = webs;
		UnitNaturestr = unitNaturestr;
		this.applyId = applyId;
		this.certificateTypestr = certificateTypestr;
		this.dutyCertificateTypestr = dutyCertificateTypestr;
		this.status = status;
		this.returnType = returnType;
		this.recordType = recordType;
	}
	
	public UnitWebSVOE(){
		
	}
	
}
