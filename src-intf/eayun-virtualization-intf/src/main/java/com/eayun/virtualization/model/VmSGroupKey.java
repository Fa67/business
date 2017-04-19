package com.eayun.virtualization.model;

public class VmSGroupKey implements java.io.Serializable {
	
	private static final long serialVersionUID = -11167651198987706L;
	private String vmId;
	private String sgId;

	public String getVmId() {
		return vmId;
	}
	public void setVmId(String vmId) {
		this.vmId = vmId;
	}
	public String getSgId() {
		return sgId;
	}
	public void setSgId(String sgId) {
		this.sgId = sgId;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof VmSGroupKey){
			VmSGroupKey key =(VmSGroupKey)obj;
			if(this.sgId.equals(key.getSgId())&&this.vmId.equals(key.getVmId())){
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.sgId.hashCode();
	}

}
