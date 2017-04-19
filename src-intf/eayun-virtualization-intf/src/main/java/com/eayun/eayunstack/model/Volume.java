package com.eayun.eayunstack.model;

import java.util.List;

import com.eayun.eayunstack.model.InnerVolume;
import com.eayun.eayunstack.model.Link;
import com.eayun.eayunstack.model.MetaData;
import com.eayun.eayunstack.model.Snapshot;


public class Volume {
	private String status;
	private String user_id;
	private InnerVolume[] attachments;
	private Link[] links;
	private String availability_zone;
	private boolean bootable;
	private String encrypted;
	private String created_at;
	private String description;
	private String tenant_id;
	private String volume_type;
	private String name;
	private String host;
	private String source_volid;
	private String snapshot_id;
	private String image_id;
	private String name_id;
	private MetaData metadata;
	private String id;
	private String migstat;
	private String size;
	private List<Snapshot> snapshots;
	
	// 云数据库实例查询使用
	private String used;
	
	public List<Snapshot> getSnapshots() {
		return snapshots;
	}
	public void setSnapshots(List<Snapshot> snapshots) {
		this.snapshots = snapshots;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getUser_id() {
		return user_id;
	}
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}
	public InnerVolume[] getAttachments() {
		return attachments;
	}
	public void setAttachments(InnerVolume[] attachments) {
		this.attachments = attachments;
	}
	public Link[] getLinks() {
		return links;
	}
	public void setLinks(Link[] links) {
		this.links = links;
	}
	public String getAvailability_zone() {
		return availability_zone;
	}
	public void setAvailability_zone(String availability_zone) {
		this.availability_zone = availability_zone;
	}
	public boolean getBootable() {
		return bootable;
	}
	public void setBootable(boolean bootable) {
		this.bootable = bootable;
	}
	public String getEncrypted() {
		return encrypted;
	}
	public void setEncrypted(String encrypted) {
		this.encrypted = encrypted;
	}
	public String getCreated_at() {
		return created_at;
	}
	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getTenant_id() {
		return tenant_id;
	}
	public void setTenant_id(String tenant_id) {
		this.tenant_id = tenant_id;
	}
	public String getVolume_type() {
		return volume_type;
	}
	public void setVolume_type(String volume_type) {
		this.volume_type = volume_type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getSource_volid() {
		return source_volid;
	}
	public void setSource_volid(String source_volid) {
		this.source_volid = source_volid;
	}
	public String getSnapshot_id() {
		return snapshot_id;
	}
	public void setSnapshot_id(String snapshot_id) {
		this.snapshot_id = snapshot_id;
	}
	public String getName_id() {
		return name_id;
	}
	public void setName_id(String name_id) {
		this.name_id = name_id;
	}
	public MetaData getMetadata() {
		return metadata;
	}
	public void setMetadata(MetaData metadata) {
		this.metadata = metadata;
	}
	public String getMigstat() {
		return migstat;
	}
	public void setMigstat(String migstat) {
		this.migstat = migstat;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getImage_id() {
		return image_id;
	}
	public void setImage_id(String image_id) {
		this.image_id = image_id;
	}
	public String getUsed() {
		return used;
	}
	public void setUsed(String used) {
		this.used = used;
	}
	
}
