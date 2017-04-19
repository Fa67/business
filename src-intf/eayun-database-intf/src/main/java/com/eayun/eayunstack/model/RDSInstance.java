package com.eayun.eayunstack.model;

/**
 * 云数据库实例 --底层数据映射
 *                       
 * @Filename: RdsInstance.java
 * @Description: 
 * @Version: 1.0
 * @Author: LiuZhuangzhuang
 * @Email: zhuangzhuang.liu@eayun.com
 * @History:<br>
 *<li>Date: 2017年2月21日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class RDSInstance {
	
	private String id;
	private Volume volume;
	private Flavor flavor;
	private String [] ip;
	private Datastore datastore;
	private String status;
	private String name;
	private String created;
	private String updated;
	private Link [] links;
	private String vmId;
	private Configuration configuration;
	private ReplicaOf replica_of;  // 创建数据库实例返回的数据
	private ReplicaOf [] replicas; // 查询主库时返回的数据
	private String portId;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Volume getVolume() {
		return volume;
	}
	public void setVolume(Volume volume) {
		this.volume = volume;
	}
	public Flavor getFlavor() {
		return flavor;
	}
	public void setFlavor(Flavor flavor) {
		this.flavor = flavor;
	}
	public String[] getIp() {
		return ip;
	}
	public void setIp(String[] ip) {
		this.ip = ip;
	}
	public Datastore getDatastore() {
		return datastore;
	}
	public void setDatastore(Datastore datastore) {
		this.datastore = datastore;
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
	public String getVmId() {
		return vmId;
	}
	public void setVmId(String vmId) {
		this.vmId = vmId;
	}
	public Configuration getConfiguration() {
		return configuration;
	}
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
	public Link[] getLinks() {
		return links;
	}
	public void setLinks(Link[] links) {
		this.links = links;
	}
	public ReplicaOf getReplica_of() {
		return replica_of;
	}
	public void setReplica_of(ReplicaOf replica_of) {
		this.replica_of = replica_of;
	}
	public ReplicaOf[] getReplicas() {
		return replicas;
	}
	public void setReplicas(ReplicaOf[] replicas) {
		this.replicas = replicas;
	}
	public String getPortId() {
		return portId;
	}
	public void setPortId(String portId) {
		this.portId = portId;
	}
	
}
