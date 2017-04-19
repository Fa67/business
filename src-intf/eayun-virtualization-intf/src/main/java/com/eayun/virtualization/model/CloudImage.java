package com.eayun.virtualization.model;

public class CloudImage extends BaseCloudImage {

	private static final long serialVersionUID = 1L;
	
	private String sysTypeName;//操作系统名称
	private int vmNum;//创建云主机数量
	private String createTimeForDis;//前端显示的时间格式
	private String isDeleting;//用于控制前端是否可以删除
	private String prjName;//项目名称
	private int count;
	private String statusForDis;	//状态显示
	
	private String dcName;		//数据中心名称
	private String cusOrg;		//所属客户
	private String osTypeName;	//操作系统类型名称
	private String createType;	//创建方式：1：文件方式；2：地址方式
	private String sysTypeEn;	//镜像类型的英文名称
	private String sourceType;	//自定义镜像来源镜像 1 是公共镜像 3是市场镜像
	private int sourceMaxCpu;	//源镜像的最大CPU
	private int sourceMaxRam;	//源镜像的最大内存
	
	private String professionName;//业务类别中文名称
	private String sourceName;//来源镜像名称
	
	
	
	public String getSysTypeName() {
		return sysTypeName;
	}
	public void setSysTypeName(String sysTypeName) {
		this.sysTypeName = sysTypeName;
	}
	public int getVmNum() {
		return vmNum;
	}
	public void setVmNum(int vmNum) {
		this.vmNum = vmNum;
	}
	public String getCreateTimeForDis() {
		return createTimeForDis;
	}
	public void setCreateTimeForDis(String createTimeForDis) {
		this.createTimeForDis = createTimeForDis;
	}
	public String getIsDeleting() {
		return isDeleting;
	}
	public void setIsDeleting(String isDeleting) {
		this.isDeleting = isDeleting;
	}
	public String getPrjName() {
		return prjName;
	}
	public void setPrjName(String prjName) {
		this.prjName = prjName;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public String getStatusForDis() {
		return statusForDis;
	}
	public void setStatusForDis(String statusForDis) {
		this.statusForDis = statusForDis;
	}
	public String getDcName() {
		return dcName;
	}
	public void setDcName(String dcName) {
		this.dcName = dcName;
	}
	public String getCusOrg() {
		return cusOrg;
	}
	public void setCusOrg(String cusOrg) {
		this.cusOrg = cusOrg;
	}
	public String getOsTypeName() {
		return osTypeName;
	}
	public void setOsTypeName(String osTypeName) {
		this.osTypeName = osTypeName;
	}
	public String getCreateType() {
		return createType;
	}
	public void setCreateType(String createType) {
		this.createType = createType;
	}
	public String getSysTypeEn() {
		return sysTypeEn;
	}
	public void setSysTypeEn(String sysTypeEn) {
		this.sysTypeEn = sysTypeEn;
	}
	public String getProfessionName() {
		return professionName;
	}
	public void setProfessionName(String professionName) {
		this.professionName = professionName;
	}
	public String getSourceType() {
		return sourceType;
	}
	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}
	public int getSourceMaxCpu() {
		return sourceMaxCpu;
	}
	public void setSourceMaxCpu(int sourceMaxCpu) {
		this.sourceMaxCpu = sourceMaxCpu;
	}
	public int getSourceMaxRam() {
		return sourceMaxRam;
	}
	public void setSourceMaxRam(int sourceMaxRam) {
		this.sourceMaxRam = sourceMaxRam;
	}
	public String getSourceName() {
		return sourceName;
	}
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}	
	
}
