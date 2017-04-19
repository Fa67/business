package com.eayun.physical.ecmcvoe;

import com.eayun.physical.model.BaseDcFirewall;


public class DcFirewallVOE extends BaseDcFirewall {

	/**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -6616565419619566798L;
    private String dataCenterName;
	private String cabinetName;
	private String state;
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
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	
	public DcFirewallVOE(BaseDcFirewall item) {
		setId(item.getId());
		setName(item.getName());
		setFirewallModel(item.getFirewallModel());
		setIpAddress(item.getIpAddress());
		setNetThroughput(item.getNetThroughput());
		setDataCenterId(item.getDataCenterId());
		setCabinetId(item.getCabinetId());
		setConcurrentConn(item.getConcurrentConn());
		setMemo(item.getMemo());
		setResponPerson(item.getResponPerson());
		setResponPersonMobile(item.getResponPersonMobile());
		setCreUser(item.getCreUser());
		setCreDate(item.getCreDate());
		setSpec(item.getSpec());
		setFirewallId(item.getFirewallId());
	}
}