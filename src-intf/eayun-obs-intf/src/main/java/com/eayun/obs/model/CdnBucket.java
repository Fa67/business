package com.eayun.obs.model;

public class CdnBucket extends BaseCdnBucket {

	/**
	 *Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;
	
	private String cusOrg;		//所属组织
	
	private String cusCpname;	//公司中文名称
	
	private String eosPath;		//源地址，bucketName+"eos.eayun.com"
	
	private String bucketPath;	//加速地址，bucketName+"file.eayun.com"
	
	private long cdnFlow;		//下载流量（如一个月内的下载流量）
	
	private String cdnFlowStr;	//下载流量显示
	
	private long backsource;		//回源流量（如一个月内的下载流量）
	
	private String backsourceStr;	//回源流量显示
	
	private long cdnHreqs;	//https请求数
	private String cdnHreqsStr;//https请求数（用于显示，加上单位）
	
	private long cdnDreqs;	//动态请求数
	private String cdnDreqsStr;//动态请求数（用于显示，加上单位）
	
	

	public long getCdnHreqs() {
		return cdnHreqs;
	}

	public void setCdnHreqs(long cdnHreqs) {
		this.cdnHreqs = cdnHreqs;
	}

	public String getCdnHreqsStr() {
		return cdnHreqsStr;
	}

	public void setCdnHreqsStr(String cdnHreqsStr) {
		this.cdnHreqsStr = cdnHreqsStr;
	}

	public long getCdnDreqs() {
		return cdnDreqs;
	}

	public void setCdnDreqs(long cdnDreqs) {
		this.cdnDreqs = cdnDreqs;
	}

	public String getCdnDreqsStr() {
		return cdnDreqsStr;
	}

	public void setCdnDreqsStr(String cdnDreqsStr) {
		this.cdnDreqsStr = cdnDreqsStr;
	}

	public String getCusOrg() {
		return cusOrg;
	}

	public void setCusOrg(String cusOrg) {
		this.cusOrg = cusOrg;
	}

	public String getCusCpname() {
		return cusCpname;
	}

	public void setCusCpname(String cusCpname) {
		this.cusCpname = cusCpname;
	}

	public String getEosPath() {
		return eosPath;
	}

	public void setEosPath(String eosPath) {
		this.eosPath = eosPath;
	}

	public String getBucketPath() {
		return bucketPath;
	}

	public void setBucketPath(String bucketPath) {
		this.bucketPath = bucketPath;
	}

	public long getCdnFlow() {
		return cdnFlow;
	}

	public void setCdnFlow(long cdnFlow) {
		this.cdnFlow = cdnFlow;
	}

	public String getCdnFlowStr() {
		return cdnFlowStr;
	}

	public void setCdnFlowStr(String cdnFlowStr) {
		this.cdnFlowStr = cdnFlowStr;
	}

	public long getBacksource() {
		return backsource;
	}

	public void setBacksource(long backsource) {
		this.backsource = backsource;
	}

	public String getBacksourceStr() {
		return backsourceStr;
	}

	public void setBacksourceStr(String backsourceStr) {
		this.backsourceStr = backsourceStr;
	}

	
}
