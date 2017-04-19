package com.eayun.virtualization.model;



public class CloudVolume extends BaseCloudVolume {
	
	private static final long serialVersionUID = 1L;
	private String vmName;
	private String prjName;
	private String createTimeForDis;//前端显示的时间格式
	private String dcName;
	private String snapNum;
	private String bootForDis;
	private String isDeleting;
	private String statusForDis;
	private int count ;
	private String vmStatus;
	private String cusId;//客户Id
	private String cusOrg;//客户组织
	private String isDeSnaps;//是否删除备份
	private int volNumber;//批量创建数量
	
	private String orderNo;//订单编号
	private int cycleCount;//剩余天数
	private String volType;//云硬盘类型
	private String volumeTypeAs;//类型中文名称
	private int maxSize;//单块硬盘最大容量
	
	
	
	
	public String getVmStatus() {
		return vmStatus;
	}
	public void setVmStatus(String vmStatus) {
		this.vmStatus = vmStatus;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public String getVmName() {
		return vmName;
	}
	public void setVmName(String vmName) {
		this.vmName = vmName;
	}
	public String getPrjName() {
		return prjName;
	}
	public void setPrjName(String prjName) {
		this.prjName = prjName;
	}
	public String getCreateTimeForDis() {
		return createTimeForDis;
	}
	public void setCreateTimeForDis(String createTimeForDis) {
		this.createTimeForDis = createTimeForDis;
	}
	public String getBootForDis() {
		return bootForDis;
	}
	public void setBootForDis(String bootForDis) {
		this.bootForDis = bootForDis;
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
	public String getSnapNum() {
		return snapNum;
	}
	public void setSnapNum(String snapNum) {
		this.snapNum = snapNum;
	}
	public String getStatusForDis() {
		return statusForDis;
	}
	public void setStatusForDis(String statusForDis) {
		this.statusForDis = statusForDis;
	}
	public String getCusId() {
		return cusId;
	}
	public void setCusId(String cusId) {
		this.cusId = cusId;
	}
	public String getCusOrg() {
		return cusOrg;
	}
	public void setCusOrg(String cusOrg) {
		this.cusOrg = cusOrg;
	}
	public String getIsDeSnaps() {
		return isDeSnaps;
	}
	public void setIsDeSnaps(String isDeSnaps) {
		this.isDeSnaps = isDeSnaps;
	}
	public int getVolNumber() {
		return volNumber;
	}
	public void setVolNumber(int volNumber) {
		this.volNumber = volNumber;
	}
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public int getCycleCount() {
		return cycleCount;
	}
	public void setCycleCount(int cycleCount) {
		this.cycleCount = cycleCount;
	}
	public String getVolType() {
		return volType;
	}
	public void setVolType(String volType) {
		this.volType = volType;
	}
	public String getVolumeTypeAs() {
		return volumeTypeAs;
	}
	public void setVolumeTypeAs(String volumeTypeAs) {
		this.volumeTypeAs = volumeTypeAs;
	}
	public int getMaxSize() {
		return maxSize;
	}
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	
	
	
	
}
	
	
	
	
	
