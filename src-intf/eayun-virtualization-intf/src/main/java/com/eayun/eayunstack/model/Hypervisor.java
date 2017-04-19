package com.eayun.eayunstack.model;

public class Hypervisor {
	private String cpu_info;
	private String current_workload;
	private String disk_available_least;
	private String free_disk_gb;
	private String free_ram_mb;
	private String host_ip;
	private String hypervisor_hostname;
	private String hypervisor_type;
	private String hypervisor_version;
	private String id;
	private String local_gb;
	private String local_gb_used;
	private String memory_mb;
	private String memory_mb_used;
	private String running_vms;
	private Service service;
	private String state;
	private String status;
	private String vcpus;
	private String vcpus_used;
	
	public String getCpu_info() {
		return cpu_info;
	}
	public void setCpu_info(String cpu_info) {
		this.cpu_info = cpu_info;
	}
	public String getCurrent_workload() {
		return current_workload;
	}
	public void setCurrent_workload(String current_workload) {
		this.current_workload = current_workload;
	}
	public String getDisk_available_least() {
		return disk_available_least;
	}
	public void setDisk_available_least(String disk_available_least) {
		this.disk_available_least = disk_available_least;
	}
	public String getFree_disk_gb() {
		return free_disk_gb;
	}
	public void setFree_disk_gb(String free_disk_gb) {
		this.free_disk_gb = free_disk_gb;
	}
	public String getFree_ram_mb() {
		return free_ram_mb;
	}
	public void setFree_ram_mb(String free_ram_mb) {
		this.free_ram_mb = free_ram_mb;
	}
	public String getHypervisor_hostname() {
		return hypervisor_hostname;
	}
	public void setHypervisor_hostname(String hypervisor_hostname) {
		this.hypervisor_hostname = hypervisor_hostname;
	}
	public String getHypervisor_type() {
		return hypervisor_type;
	}
	public void setHypervisor_type(String hypervisor_type) {
		this.hypervisor_type = hypervisor_type;
	}
	public String getHypervisor_version() {
		return hypervisor_version;
	}
	public void setHypervisor_version(String hypervisor_version) {
		this.hypervisor_version = hypervisor_version;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getLocal_gb() {
		return local_gb;
	}
	public void setLocal_gb(String local_gb) {
		this.local_gb = local_gb;
	}
	public String getLocal_gb_used() {
		return local_gb_used;
	}
	public void setLocal_gb_used(String local_gb_used) {
		this.local_gb_used = local_gb_used;
	}
	public String getMemory_mb() {
		return memory_mb;
	}
	public void setMemory_mb(String memory_mb) {
		this.memory_mb = memory_mb;
	}
	public String getMemory_mb_used() {
		return memory_mb_used;
	}
	public void setMemory_mb_used(String memory_mb_used) {
		this.memory_mb_used = memory_mb_used;
	}
	public String getRunning_vms() {
		return running_vms;
	}
	public void setRunning_vms(String running_vms) {
		this.running_vms = running_vms;
	}
	public String getVcpus() {
		return vcpus;
	}
	public void setVcpus(String vcpus) {
		this.vcpus = vcpus;
	}
	public String getVcpus_used() {
		return vcpus_used;
	}
	public void setVcpus_used(String vcpus_used) {
		this.vcpus_used = vcpus_used;
	}
	public Service getService() {
		return service;
	}
	public void setService(Service service) {
		this.service = service;
	}
	public String getHost_ip() {
		return host_ip;
	}
	public void setHost_ip(String host_ip) {
		this.host_ip = host_ip;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
}
