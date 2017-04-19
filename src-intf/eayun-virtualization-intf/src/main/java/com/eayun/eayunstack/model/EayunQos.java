package com.eayun.eayunstack.model;

public class EayunQos {
	private String id;
	private String name;
	private String tenant_id;
	private String rate;
	private String description;
	private String direction;
	private String default_queue_id;
	private String target_type ;
	private String target_id;
	private EayunQosQueue[] qos_queues;
	private EayunQosFilter[] unattached_filters;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTenant_id() {
		return tenant_id;
	}

	public void setTenant_id(String tenant_id) {
		this.tenant_id = tenant_id;
	}

	public String getRate() {
		return rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getDefault_queue_id() {
		return default_queue_id;
	}

	public void setDefault_queue_id(String default_queue_id) {
		this.default_queue_id = default_queue_id;
	}

	public EayunQosQueue[] getQos_queues() {
		return qos_queues;
	}

	public void setQos_queues(EayunQosQueue[] qos_queues) {
		this.qos_queues = qos_queues;
	}

	public EayunQosFilter[] getUnattached_filters() {
		return unattached_filters;
	}

	public void setUnattached_filters(EayunQosFilter[] unattached_filters) {
		this.unattached_filters = unattached_filters;
	}

	public String getTarget_type() {
		return target_type;
	}

	public void setTarget_type(String target_type) {
		this.target_type = target_type;
	}

	public String getTarget_id() {
		return target_id;
	}

	public void setTarget_id(String target_id) {
		this.target_id = target_id;
	}
	
}
