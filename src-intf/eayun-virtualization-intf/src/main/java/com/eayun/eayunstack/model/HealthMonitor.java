package com.eayun.eayunstack.model;

public class HealthMonitor {
	private String id;
	private String tenant_id;
	private String type;// 监控类型
	private String delay;// 延迟
	private String timeout;// 超时
	private String max_retries;// 最大超时次数
	private String http_method;
	private String url_path;
	private String expected_codes;
	private boolean admin_state_up;
	private String status;

	private String name;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDelay() {
		return delay;
	}

	public void setDelay(String delay) {
		this.delay = delay;
	}

	public String getTimeout() {
		return timeout;
	}

	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}

	public String getMax_retries() {
		return max_retries;
	}

	public void setMax_retries(String max_retries) {
		this.max_retries = max_retries;
	}

	public String getHttp_method() {
		return http_method;
	}

	public void setHttp_method(String http_method) {
		this.http_method = http_method;
	}

	public String getUrl_path() {
		return url_path;
	}

	public void setUrl_path(String url_path) {
		this.url_path = url_path;
	}

	public String getExpected_codes() {
		return expected_codes;
	}

	public void setExpected_codes(String expected_codes) {
		this.expected_codes = expected_codes;
	}

	public boolean isAdmin_state_up() {
		return admin_state_up;
	}

	public void setAdmin_state_up(boolean admin_state_up) {
		this.admin_state_up = admin_state_up;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

}
