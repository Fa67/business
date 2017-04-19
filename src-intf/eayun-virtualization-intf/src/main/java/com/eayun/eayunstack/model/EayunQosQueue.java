package com.eayun.eayunstack.model;

public class EayunQosQueue {
	private String id;
	private String rate;
	private int prio;
	private String qos_id;
	private int ceil;
	private String tenant_id;
	private EayunQosQueue[] subqueues;
	private EayunQosFilter[] attached_filters;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRate() {
		return rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}

	public int getPrio() {
		return prio;
	}

	public void setPrio(int prio) {
		this.prio = prio;
	}

	public String getQos_id() {
		return qos_id;
	}

	public void setQos_id(String qos_id) {
		this.qos_id = qos_id;
	}

	public int getCeil() {
		return ceil;
	}

	public void setCeil(int ceil) {
		this.ceil = ceil;
	}

	public String getTenant_id() {
		return tenant_id;
	}

	public void setTenant_id(String tenant_id) {
		this.tenant_id = tenant_id;
	}

	public EayunQosQueue[] getSubqueues() {
		return subqueues;
	}

	public void setSubqueues(EayunQosQueue[] subqueues) {
		this.subqueues = subqueues;
	}

	public EayunQosFilter[] getAttached_filters() {
		return attached_filters;
	}

	public void setAttached_filters(EayunQosFilter[] attached_filters) {
		this.attached_filters = attached_filters;
	}

}
