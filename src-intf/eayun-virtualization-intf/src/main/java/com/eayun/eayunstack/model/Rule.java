package com.eayun.eayunstack.model;

import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;

public class Rule {
	private String id;// 自己ID
	private String direction;// egress出口、ingress入口
	private String ethertype;// IP4或者IP6
	private String port_range_max;// 端口最大值
	private String port_range_min;// 端口最小值
	private String protocol;// IP协议 tcp udp等
	private String remote_ip_prefix;// 远程 CIDR
	private String security_group_id;// 所在的安全组ID
	private String tenant_id;// 所在的项目ID
	private String remote_group_id;// 远程选择安全组时 显示的值是安全组的name 用id标记；
	private String remote_group_name;// 设置指定的那个安全组的名称；
	private String icmp;// 设置指定的那个安全组的名称；
	public String getIcmp() {
		return icmp;
	}

	public void setIcmp(String icmp) {
		this.icmp = icmp;
	}

	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	private Date creat_time;// 设置规则的创建时间；

	public Date getCreat_time() {
		return creat_time;
	}

	public void setCreat_time(Date creat_time) {
		this.creat_time = creat_time;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getEthertype() {
		return ethertype;
	}

	public void setEthertype(String ethertype) {
		this.ethertype = ethertype;
	}

	public String getPort_range_max() {
		return port_range_max;
	}

	public void setPort_range_max(String port_range_max) {
		this.port_range_max = port_range_max;
	}

	public String getPort_range_min() {
		return port_range_min;
	}

	public void setPort_range_min(String port_range_min) {
		this.port_range_min = port_range_min;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getRemote_ip_prefix() {
		return remote_ip_prefix;
	}

	public void setRemote_ip_prefix(String remote_ip_prefix) {
		this.remote_ip_prefix = remote_ip_prefix;
	}

	public String getSecurity_group_id() {
		return security_group_id;
	}

	public void setSecurity_group_id(String security_group_id) {
		this.security_group_id = security_group_id;
	}

	public String getTenant_id() {
		return tenant_id;
	}

	public void setTenant_id(String tenant_id) {
		this.tenant_id = tenant_id;
	}

	public String getRemote_group_id() {
		return remote_group_id;
	}

	public void setRemote_group_id(String remote_group_id) {
		this.remote_group_id = remote_group_id;
	}

	public String getRemote_group_name() {
		return remote_group_name;
	}

	public void setRemote_group_name(String remote_group_name) {
		this.remote_group_name = remote_group_name;
	}
}
