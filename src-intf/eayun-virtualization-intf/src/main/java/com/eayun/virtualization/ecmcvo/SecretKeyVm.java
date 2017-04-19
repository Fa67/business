package com.eayun.virtualization.ecmcvo;

public class SecretKeyVm {

	private String skId;
	private String vmId;
	private String status;
	private String osType;//操作系统
	
	private String vmip;
	private String netip;
	private String floip;
	private boolean isremoveVm;
	private boolean isroute;
	
	
	public boolean isIsroute() {
		return isroute;
	}

	public void setIsroute(boolean isroute) {
		this.isroute = isroute;
	}

	public String getSkId() {
		return skId;
	}

	public void setSkId(String skId) {
		this.skId = skId;
	}

	public boolean isIsremoveVm() {
		return isremoveVm;
	}

	public void setIsremoveVm(boolean isremoveVm) {
		this.isremoveVm = isremoveVm;
	}

	public String getVmip() {
		return vmip;
	}

	public void setVmip(String vmip) {
		this.vmip = vmip;
	}

	public String getNetip() {
		return netip;
	}

	public void setNetip(String netip) {
		this.netip = netip;
	}

	public String getFloip() {
		return floip;
	}

	public void setFloip(String floip) {
		this.floip = floip;
	}

	private String vmname;
	
	public String getIp() {
		return vmip;
	}

	public void setIp(String vmip) {
		this.vmip = vmip;
	}

	public String getVmname() {
		return vmname;
	}

	public void setVmname(String vmname) {
		this.vmname = vmname;
	}

	private int cpus;	//cpu配置
	private int rams;	//内存配置
	private int sysdisks;	//硬盘配置
	private int datedisk;
	private String chargeState;//资费状态
	
	private String netIp;

	public String getVmId() {
		return vmId;
	}

	public void setVmId(String vmId) {
		this.vmId = vmId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getOsType() {
		return osType;
	}

	public void setOsType(String osType) {
		this.osType = osType;
	}

	public int getCpus() {
		return cpus;
	}

	public void setCpus(int cpus) {
		this.cpus = cpus;
	}

	public int getRams() {
		return rams;
	}

	public void setRams(int rams) {
		this.rams = rams;
	}



	public int getSysdisks() {
		return sysdisks;
	}

	public void setSysdisks(int sysdisks) {
		this.sysdisks = sysdisks;
	}

	public int getDatedisk() {
		return datedisk;
	}

	public void setDatedisk(int datedisk) {
		this.datedisk = datedisk;
	}

	public String getNetIp() {
		return netIp;
	}

	public void setNetIp(String netIp) {
		this.netIp = netIp;
	}

    public String getChargeState() {
        return chargeState;
    }

    public void setChargeState(String chargeState) {
        this.chargeState = chargeState;
    }
	
	
	
	
	
}
