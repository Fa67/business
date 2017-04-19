package com.eayun.physical.ecmcvoe;

import com.eayun.physical.model.BaseDcServer;

public class DcServerVOE extends BaseDcServer{
	
	/**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -1574182458933612279L;
    private int vmNumber; //云主机数量
	private String state; //所属机柜位置
	private String deleteFlag;//是否可删除
	private String dcServerModelName;//服务器型号名称
	private String datacenterName;//数据中心名称
	private String cabinetName;//机柜名称
	

	public String getCabinetName() {
		return cabinetName;
	}

	public void setCabinetName(String cabinetName) {
		this.cabinetName = cabinetName;
	}

	public String getDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(String deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public int getVmNumber() {
		return vmNumber;
	}

	public void setVmNumber(int vmNumber) {
		this.vmNumber = vmNumber;
	}

	public String getDcServerModelName() {
		return dcServerModelName;
	}

	public String getDatacenterName() {
		return datacenterName;
	}

	public void setDatacenterName(String datacenterName) {
		this.datacenterName = datacenterName;
	}

	public void setDcServerModelName(String dcServerModelName) {
		this.dcServerModelName = dcServerModelName;
	}

	public DcServerVOE(){
		
	}
	
	public DcServerVOE(BaseDcServer item){
		setId(item.getId());
		setCabinetId(item.getCabinetId());
		setCpu(item.getCpu());
		setDatacenterId(item.getDatacenterId());
		setDiskCapacity(item.getDiskCapacity());
		setIsComputenode(item.getIsComputenode());
		setIsMonitor(item.getIsMonitor());
		setMemo(item.getMemo());
		setMemory(item.getMemory());
		setName(item.getName());
		setServerId(item.getServerId());
		setServerInnetIp(item.getServerInnetIp());
		setServerModelId(item.getServerModelId());
		setServerOutnetIp(item.getServerOutnetIp());
		setServerUses(item.getServerUses());
		setSpec(item.getSpec());
		setCreDate(item.getCreDate());
		setCreUser(item.getCreUser());
		setResponPerson(item.getResponPerson());
		setResponPersonMobile(item.getResponPersonMobile());
		setNodeId(item.getNodeId());
	}
}
