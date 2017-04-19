package com.eayun.physical.ecmcvoe;

import com.eayun.physical.model.BaseDcStorage;

public class DcStorageVOE extends BaseDcStorage {

	/**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 195396943964460503L;
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
	
	public DcStorageVOE(BaseDcStorage item) {
		setId(item.getId());
		setName(item.getName());
		setCabinetId(item.getCabinetId());
		setDataCenterId(item.getDataCenterId());
		setDataRate(item.getDataRate());
		setMemo(item.getMemo());
		setRaidSupport(item.getRaidSupport());
		setResponPerson(item.getResponPerson());
		setResponPersonMobile(item.getResponPersonMobile());
		setSpec(item.getSpec());
		setStorage_id(item.getStorage_id());
		setStorageModel(item.getStorageModel());
		setStorageUnit(item.getStorageUnit());
		setStorageValue(item.getStorageValue());
		setCache(item.getCache());
		setCreDate(item.getCreDate());
		setCreUser(item.getCreUser());
	}
	
}
