package com.eayun.datacenter.ecmcvoe;


import com.eayun.datacenter.model.BaseDcDataCenter;

public class DcDataCenterVOE extends BaseDcDataCenter {

	/**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 4264464002095646712L;
    private String cabinetNum;
	private String serverNum;
	private String switchNum;
	private String storageNum;
	private String firewallNum;
	private String emptyCapa;
	private String projectName;
	private int alarmCount;//报警信息数量
	
	public String getEmptyCapa() {
		return emptyCapa;
	}
	public void setEmptyCapa(String emptyCapa) {
		this.emptyCapa = emptyCapa;
	}
	public String getCabinetNum() {
		return cabinetNum;
	}
	public void setCabinetNum(String cabinetNum) {
		this.cabinetNum = cabinetNum;
	}
	public String getServerNum() {
		return serverNum;
	}
	public void setServerNum(String serverNum) {
		this.serverNum = serverNum;
	}
	public String getSwitchNum() {
		return switchNum;
	}
	public void setSwitchNum(String switchNum) {
		this.switchNum = switchNum;
	}
	public String getStorageNum() {
		return storageNum;
	}
	public void setStorageNum(String storageNum) {
		this.storageNum = storageNum;
	}
	public String getFirewallNum() {
		return firewallNum;
	}
	public void setFirewallNum(String firewallNum) {
		this.firewallNum = firewallNum;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public int getAlarmCount() {
		return alarmCount;
	}
	public void setAlarmCount(int alarmCount) {
		this.alarmCount = alarmCount;
	}
	
	public DcDataCenterVOE(BaseDcDataCenter item) {
		setId(item.getId());
		setName(item.getName());
		setDcType(item.getDcType());
		setDcAddress(item.getDcAddress());
		setVCenterUsername(item.getVCenterUsername());
		setVCenterPassword(item.getVCenterPassword());
		setCabinetCapacity(item.getCabinetCapacity());
		setEcManagenodeOutnetip(item.getEcManagenodeOutnetip());
		setEcManagenodeAuth(item.getEcManagenodeAuth());
		setOsManagenodeInnetip(item.getOsManagenodeInnetip());
		setOsManagenodeInnetAuth(item.getOsManagenodeInnetAuth());
		setCreUser(item.getCreUser());
		setCreDate(item.getCreDate());
		//------以前不要求显示的，现在有可能会显示
		setOsAdminProjectId(item.getOsAdminProjectId());
		setOsKeystoneRegion(item.getOsKeystoneRegion());
		setOsCommonRegion(item.getOsCommonRegion());
		setDcDesc(item.getDcDesc());
		setNagiosUser(item.getNagiosUser());
		setNagiosPassword(item.getNagiosPassword());
		setQuotaCpu(item.getQuotaCpu());
		setQuotaMemory(item.getQuotaMemory());
		setQuotaNatwork(item.getQuotaNatwork());
		setQuotaDisk(item.getQuotaDisk());
		setNagiosIp(item.getNagiosIp());
		setCpuAllocationRatio(item.getCpuAllocationRatio());
		setDiskAllocationRatio(item.getDiskAllocationRatio());
		setRamAllocationRatio(item.getRamAllocationRatio());
	}
	public DcDataCenterVOE() {
	}
}