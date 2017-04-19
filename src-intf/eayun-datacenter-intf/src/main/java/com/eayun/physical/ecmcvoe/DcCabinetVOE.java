package com.eayun.physical.ecmcvoe;
import com.eayun.physical.model.BaseDcCabinet;

public class DcCabinetVOE extends BaseDcCabinet {

	/**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 9158956574901708668L;
    private String emptyCapa;
	private String dataCenterName;

	public String getDataCenterName() {
		return dataCenterName;
	}

	public void setDataCenterName(String dataCenterName) {
		this.dataCenterName = dataCenterName;
	}

	public String getEmptyCapa() {
		return emptyCapa;
	}

	public void setEmptyCapa(String emptyCapa) {
		this.emptyCapa = emptyCapa;
	}
	public DcCabinetVOE(BaseDcCabinet item){
		setCabinetId(item.getCabinetId());
		setDataCenterId(item.getDataCenterId());
		setId(item.getId());
		setMemo(item.getMemo());
		setName(item.getName());
		setTotalCapacity(item.getTotalCapacity());
		setUsedCapacity(item.getUsedCapacity());
		setCreUser(item.getCreUser());
		setCreDate(item.getCreDate());
	}
}
