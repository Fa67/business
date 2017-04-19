package com.eayun.physical.ecmcvoe;

import com.eayun.physical.model.BaseDcSwitch;

public class DcSwitchVOE extends BaseDcSwitch{

	/**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 283464630552082280L;
    private String dataCenterName;
	private String cabinetName;
	private String state;
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getDataCenterName() {
		return dataCenterName;
	}
	public void setDataCenterName(String dataCenterName) {
		this.dataCenterName = dataCenterName;
	}
	public String getCabinetName() {
		return cabinetName;
	}
	public void setCabinetName(String cabinetName) {
		this.cabinetName = cabinetName;
	}
	
	public DcSwitchVOE(BaseDcSwitch item){
		setId(item.getId());
		setName(item.getName());
		setCabinetId(item.getCabinetId());
		setCreDate(item.getCreDate());
		setCreUser(item.getCreUser());
		setDataCenterId(item.getDataCenterId());
		setForwardFunc(item.getForwardFunc());
		setInterfaceModel(item.getInterfaceModel());
		setIpAddress(item.getIpAddress());
		setPortCapacity(item.getPortCapacity());
		setResponPerson(item.getResponPerson());
		setSpec(item.getSpec());
		setSwitchModel(item.getSwitchModel());
		setMemo(item.getMemo());
		setResponPersonMobile(item.getResponPersonMobile());
	}
	
	public DcSwitchVOE(){
		
	}
}
