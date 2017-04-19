package com.eayun.datacenter.model;


public class DcDataCenter extends BaseDcDataCenter{

	private static final long serialVersionUID = 3545078651296542734L;
	
	//数据中心下的资源存在数量
	private int serverCount;//物理服务器数量
	private int prjCount;//项目数量
	private int imageCount;//镜像数量
	private int firewallCount;//防火墙数量
	
	//数据中心下的资源的配额数量
	private int vmCountQuato;//云主机配额数量
	private int volumeCountQuato;//云硬盘配额数量
	private int dataCapacityQuato;//云硬盘和云硬盘快照的配额容量
	private int volSnapshotCountQuato;//云硬盘备份配额量
	private int networkCountQuato;//网络配额数量
	private int subnetCountQuato;//子网配额数量
	private int poolCountQuato;//负载均衡配额数量 
	private int safeGroupCountQuato;//安全组配额数量
	private int floatIpCountQuato;//外网ip配额数量
	private int cpuQuato;//CPU配额量
	private int memoryQuato;//内存配额量
	private int bandWidthQuato;//网络带宽配额量
	
	//数据中心下资源的已使用量
	private int usedVolumeCapacity;//已使用的云硬盘容量
	private int usedVolSnapshotSum;//已使用的云硬盘备份容量
	private int usedCpuCount;//已使用的CPU核数
	private float usedMemoryCount;//已使用的内存量（GB）
	private int usedFloatIpCount;//已使用的外网IP数量
	private int allotFloatIpCount;//已分配的外网IP数量
	private int usedRouteCount;//已使用的带宽数量
	private int usedRoute;//已使用的路由占用
	private int usedDHCP;//DHCP占用
	private int usedTotalRDSInstance;	//已使用的RDS实例数量
	
	private String  resourceType; //排序资源
	private String  sortType;//排序规则
	
	public int getServerCount() {
		return serverCount;
	}
	public void setServerCount(int serverCount) {
		this.serverCount = serverCount;
	}
	public int getPrjCount() {
		return prjCount;
	}
	public void setPrjCount(int prjCount) {
		this.prjCount = prjCount;
	}
	public int getImageCount() {
		return imageCount;
	}
	public void setImageCount(int imageCount) {
		this.imageCount = imageCount;
	}
	public int getFirewallCount() {
		return firewallCount;
	}
	public void setFirewallCount(int firewallCount) {
		this.firewallCount = firewallCount;
	}
	public int getVmCountQuato() {
		return vmCountQuato;
	}
	public void setVmCountQuato(int vmCountQuato) {
		this.vmCountQuato = vmCountQuato;
	}
	public int getVolumeCountQuato() {
		return volumeCountQuato;
	}
	public void setVolumeCountQuato(int volumeCountQuato) {
		this.volumeCountQuato = volumeCountQuato;
	}
	public int getDataCapacityQuato() {
		return dataCapacityQuato;
	}
	public void setDataCapacityQuato(int dataCapacityQuato) {
		this.dataCapacityQuato = dataCapacityQuato;
	}
	public int getVolSnapshotCountQuato() {
		return volSnapshotCountQuato;
	}
	public void setVolSnapshotCountQuato(int volSnapshotCountQuato) {
		this.volSnapshotCountQuato = volSnapshotCountQuato;
	}
	public int getFloatIpCountQuato() {
		return floatIpCountQuato;
	}
	public void setFloatIpCountQuato(int floatIpCountQuato) {
		this.floatIpCountQuato = floatIpCountQuato;
	}
	public int getCpuQuato() {
		return cpuQuato;
	}
	public void setCpuQuato(int cpuQuato) {
		this.cpuQuato = cpuQuato;
	}
	public int getMemoryQuato() {
		return memoryQuato;
	}
	public void setMemoryQuato(int memoryQuato) {
		this.memoryQuato = memoryQuato;
	}
	public int getUsedVolumeCapacity() {
		return usedVolumeCapacity;
	}
	public void setUsedVolumeCapacity(int usedVolumeCapacity) {
		this.usedVolumeCapacity = usedVolumeCapacity;
	}
	public int getUsedCpuCount() {
		return usedCpuCount;
	}
	public void setUsedCpuCount(int usedCpuCount) {
		this.usedCpuCount = usedCpuCount;
	}
	public float getUsedMemoryCount() {
		return usedMemoryCount;
	}
	public void setUsedMemoryCount(float usedMemoryCount) {
		this.usedMemoryCount = usedMemoryCount;
	}
	public int getUsedFloatIpCount() {
		return usedFloatIpCount;
	}
	public void setUsedFloatIpCount(int usedFloatIpCount) {
		this.usedFloatIpCount = usedFloatIpCount;
	}
	public int getAllotFloatIpCount() {
		return allotFloatIpCount;
	}
	public void setAllotFloatIpCount(int allotFloatIpCount) {
		this.allotFloatIpCount = allotFloatIpCount;
	}
	public int getNetworkCountQuato() {
		return networkCountQuato;
	}
	public void setNetworkCountQuato(int networkCountQuato) {
		this.networkCountQuato = networkCountQuato;
	}
	public int getSubnetCountQuato() {
		return subnetCountQuato;
	}
	public void setSubnetCountQuato(int subnetCountQuato) {
		this.subnetCountQuato = subnetCountQuato;
	}
	public int getPoolCountQuato() {
		return poolCountQuato;
	}
	public void setPoolCountQuato(int poolCountQuato) {
		this.poolCountQuato = poolCountQuato;
	}
	public int getSafeGroupCountQuato() {
		return safeGroupCountQuato;
	}
	public void setSafeGroupCountQuato(int safeGroupCountQuato) {
		this.safeGroupCountQuato = safeGroupCountQuato;
	}
	public int getBandWidthQuato() {
		return bandWidthQuato;
	}
	public void setBandWidthQuato(int bandWidthQuato) {
		this.bandWidthQuato = bandWidthQuato;
	}
	public String getResourceType() {
		return resourceType;
	}
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
	public String getSortType() {
		return sortType;
	}
	public void setSortType(String sortType) {
		this.sortType = sortType;
	}
	public int getUsedRouteCount() {
		return usedRouteCount;
	}
	public void setUsedRouteCount(int usedRouteCount) {
		this.usedRouteCount = usedRouteCount;
	}
	public int getUsedRoute() {
		return usedRoute;
	}
	public void setUsedRoute(int usedRoute) {
		this.usedRoute = usedRoute;
	}
	public int getUsedDHCP() {
		return usedDHCP;
	}
	public void setUsedDHCP(int usedDHCP) {
		this.usedDHCP = usedDHCP;
	}
	public int getUsedVolSnapshotSum() {
		return usedVolSnapshotSum;
	}
	public void setUsedVolSnapshotSum(int usedVolSnapshotSum) {
		this.usedVolSnapshotSum = usedVolSnapshotSum;
	}
	public int getUsedTotalRDSInstance() {
		return usedTotalRDSInstance;
	}
	public void setUsedTotalRDSInstance(int usedTotalRDSInstance) {
		this.usedTotalRDSInstance = usedTotalRDSInstance;
	}

}
