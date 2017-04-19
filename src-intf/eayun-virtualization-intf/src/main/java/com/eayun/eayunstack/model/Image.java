package com.eayun.eayunstack.model;


public class Image {
	private String container_format;
	private String min_ram;//最小内存
	private String instance_type_id;
	private String updated_at;
	private String file;
	private String owner;//所在的项目Id号
	private String id;
	private String size;
	private String user_id;
	private String image_type;
	private String self;
	private String disk_format;//镜像格式
	private String instance_type_ephemeral_gb;
	private String base_image_ref;
	private String network_allocated;
	private String schema;
	private String status;
	private String[] tags;
	private String instance_type_root_gb;
	private String instance_type_name;
	private String visibility;
	private String instance_type_rxtx_factor;
	private String min_disk;//最小硬盘
	private String instance_type_vcpus;
	private String instance_uuid;
	private String instance_type_memory_mb;
	private String instance_type_swap;
	private String name;
	private String created_at;
	private String instance_type_flavorid;
	private String owner_id;
	private Link[] links;
	private String sysTypeName;//镜像选择的系统名称；
	private String dateCenterName;//镜像所在的数据中心名称；
	private String imageSource="true";
	private boolean isPublic;
	private boolean isProtected;
	private String checkSum; 
	public String getDateCenterName() {
		return dateCenterName;
	}
	public void setDateCenterName(String dateCenterName) {
		this.dateCenterName = dateCenterName;
	}
	public String getSysTypeName() {
		return sysTypeName;
	}
	public void setSysTypeName(String sysTypeName) {
		this.sysTypeName = sysTypeName;
	}
	public String getContainer_format() {
		return container_format;
	}
	public void setContainer_format(String container_format) {
		this.container_format = container_format;
	}
	public String getMin_ram() {
		return min_ram;
	}
	public void setMin_ram(String min_ram) {
		this.min_ram = min_ram;
	}
	public String getInstance_type_id() {
		return instance_type_id;
	}
	public void setInstance_type_id(String instance_type_id) {
		this.instance_type_id = instance_type_id;
	}
	public String getUpdated_at() {
		return updated_at;
	}
	public void setUpdated_at(String updated_at) {
		this.updated_at = updated_at;
	}
	public String getFile() {
		return file;
	}
	public void setFile(String file) {
		this.file = file;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getUser_id() {
		return user_id;
	}
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}
	public String getImage_type() {
		return image_type;
	}
	public void setImage_type(String image_type) {
		this.image_type = image_type;
	}
	public String getSelf() {
		return self;
	}
	public void setSelf(String self) {
		this.self = self;
	}
	public String getDisk_format() {
		return disk_format;
	}
	public void setDisk_format(String disk_format) {
		this.disk_format = disk_format;
	}
	public String getInstance_type_ephemeral_gb() {
		return instance_type_ephemeral_gb;
	}
	public void setInstance_type_ephemeral_gb(String instance_type_ephemeral_gb) {
		this.instance_type_ephemeral_gb = instance_type_ephemeral_gb;
	}
	public String getBase_image_ref() {
		return base_image_ref;
	}
	public void setBase_image_ref(String base_image_ref) {
		this.base_image_ref = base_image_ref;
	}
	public String getNetwork_allocated() {
		return network_allocated;
	}
	public void setNetwork_allocated(String network_allocated) {
		this.network_allocated = network_allocated;
	}
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String[] getTags() {
		return tags;
	}
	public void setTags(String[] tags) {
		this.tags = tags;
	}
	public String getInstance_type_root_gb() {
		return instance_type_root_gb;
	}
	public void setInstance_type_root_gb(String instance_type_root_gb) {
		this.instance_type_root_gb = instance_type_root_gb;
	}
	public String getInstance_type_name() {
		return instance_type_name;
	}
	public void setInstance_type_name(String instance_type_name) {
		this.instance_type_name = instance_type_name;
	}
	public String getVisibility() {
		return visibility;
	}
	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}
	public String getInstance_type_rxtx_factor() {
		return instance_type_rxtx_factor;
	}
	public void setInstance_type_rxtx_factor(String instance_type_rxtx_factor) {
		this.instance_type_rxtx_factor = instance_type_rxtx_factor;
	}
	public String getMin_disk() {
		return min_disk;
	}
	public void setMin_disk(String min_disk) {
		this.min_disk = min_disk;
	}
	public String getInstance_type_vcpus() {
		return instance_type_vcpus;
	}
	public void setInstance_type_vcpus(String instance_type_vcpus) {
		this.instance_type_vcpus = instance_type_vcpus;
	}
	public String getInstance_uuid() {
		return instance_uuid;
	}
	public void setInstance_uuid(String instance_uuid) {
		this.instance_uuid = instance_uuid;
	}
	public String getInstance_type_memory_mb() {
		return instance_type_memory_mb;
	}
	public void setInstance_type_memory_mb(String instance_type_memory_mb) {
		this.instance_type_memory_mb = instance_type_memory_mb;
	}
	public String getInstance_type_swap() {
		return instance_type_swap;
	}
	public void setInstance_type_swap(String instance_type_swap) {
		this.instance_type_swap = instance_type_swap;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCreated_at() {
		return created_at;
	}
	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}
	public String getInstance_type_flavorid() {
		return instance_type_flavorid;
	}
	public void setInstance_type_flavorid(String instance_type_flavorid) {
		this.instance_type_flavorid = instance_type_flavorid;
	}
	public Link[] getLinks() {
		return links;
	}
	public void setLinks(Link[] links) {
		this.links = links;
	}
	public String getOwner_id() {
		return owner_id;
	}
	public void setOwner_id(String owner_id) {
		this.owner_id = owner_id;
	}
	public String getImageSource() {
		return imageSource;
	}
	public void setImageSource(String imageSource) {
		this.imageSource = imageSource;
	}
	public boolean isPublic() {
		return isPublic;
	}
	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}
	public boolean isProtected() {
		return isProtected;
	}
	public void setProtected(boolean isProtected) {
		this.isProtected = isProtected;
	}
	public String getCheckSum() {
		return checkSum;
	}
	public void setCheckSum(String checkSum) {
		this.checkSum = checkSum;
	}
	
}
