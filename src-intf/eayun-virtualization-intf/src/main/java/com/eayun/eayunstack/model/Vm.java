package com.eayun.eayunstack.model;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public class Vm {
	private String id;
	private String name;
	private String hostId;
	private String tenant_id;
	private String user_id;
	private String created;
	private String updated;
	private String progress;
	private String key_name;
	private String accessIPv4;
	private String accessIPv6;
	private String status;
	private String config_drive;

	private SecurityGroup[] security_groups;
	private Flavor flavor;
	private Image image;

	private List<String> volumes_attached;
	private JSONObject addresses;

	private String diskConfig;
	private String availability_zone;
	private String host;
	private String hypervisor_hostname;
	private String instance_name;
	private String power_state;
	private String vm_state;
	private String launched_at;
	private String portId;
	private String selfPortId;
	private String selfIp;
	private String ip;

	private List<Volume> volumes;

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

	public String getHostId() {
		return hostId;
	}

	public void setHostId(String hostId) {
		this.hostId = hostId;
	}

	public String getTenant_id() {
		return tenant_id;
	}

	public void setTenant_id(String tenant_id) {
		this.tenant_id = tenant_id;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public String getUpdated() {
		return updated;
	}

	public void setUpdated(String updated) {
		this.updated = updated;
	}

	public String getProgress() {
		return progress;
	}

	public void setProgress(String progress) {
		this.progress = progress;
	}

	public String getKey_name() {
		return key_name;
	}

	public void setKey_name(String key_name) {
		this.key_name = key_name;
	}

	public String getAccessIPv4() {
		return accessIPv4;
	}

	public void setAccessIPv4(String accessIPv4) {
		this.accessIPv4 = accessIPv4;
	}

	public String getAccessIPv6() {
		return accessIPv6;
	}

	public void setAccessIPv6(String accessIPv6) {
		this.accessIPv6 = accessIPv6;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getConfig_drive() {
		return config_drive;
	}

	public void setConfig_drive(String config_drive) {
		this.config_drive = config_drive;
	}

	public SecurityGroup[] getSecurity_groups() {
		return security_groups;
	}

	public void setSecurity_groups(SecurityGroup[] security_groups) {
		this.security_groups = security_groups;
	}

	public Flavor getFlavor() {
		return flavor;
	}

	public void setFlavor(Flavor flavor) {
		this.flavor = flavor;
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public List<String> getVolumes_attached() {
		return volumes_attached;
	}

	public void setVolumes_attached(List<String> volumes_attached) {
		this.volumes_attached = volumes_attached;
	}

	public JSONObject getAddresses() {
		return addresses;
	}

	public void setAddresses(JSONObject addresses) {
		this.addresses = addresses;
	}

	public String getDiskConfig() {
		return diskConfig;
	}

	public void setDiskConfig(String diskConfig) {
		this.diskConfig = diskConfig;
	}

	public String getAvailability_zone() {
		return availability_zone;
	}

	public void setAvailability_zone(String availability_zone) {
		this.availability_zone = availability_zone;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getHypervisor_hostname() {
		return hypervisor_hostname;
	}

	public void setHypervisor_hostname(String hypervisor_hostname) {
		this.hypervisor_hostname = hypervisor_hostname;
	}

	public String getInstance_name() {
		return instance_name;
	}

	public void setInstance_name(String instance_name) {
		this.instance_name = instance_name;
	}

	public String getPower_state() {
		return power_state;
	}

	public void setPower_state(String power_state) {
		this.power_state = power_state;
	}

	public String getVm_state() {
		return vm_state;
	}

	public void setVm_state(String vm_state) {
		this.vm_state = vm_state;
	}

	public String getLaunched_at() {
		return launched_at;
	}

	public void setLaunched_at(String launched_at) {
		this.launched_at = launched_at;
	}

	public List<Volume> getVolumes() {
		return volumes;
	}

	public void setVolumes(List<Volume> volumes) {
		this.volumes = volumes;
	}

	public String getPortId() {
		return portId;
	}

	public void setPortId(String portId) {
		this.portId = portId;
	}

	public String getSelfPortId() {
		return selfPortId;
	}

	public String getSelfIp() {
		return selfIp;
	}

	public void setSelfPortId(String selfPortId) {
		this.selfPortId = selfPortId;
	}

	public void setSelfIp(String selfIp) {
		this.selfIp = selfIp;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
}
