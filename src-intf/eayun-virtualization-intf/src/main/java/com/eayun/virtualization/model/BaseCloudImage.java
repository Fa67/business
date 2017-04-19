package com.eayun.virtualization.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.StringUtils;

import com.eayun.eayunstack.model.Image;

@Entity
@Table(name = "cloud_image")
public class BaseCloudImage implements java.io.Serializable {
	
	private static final long serialVersionUID = -1116765119289877206L;
	
	
	private String imageId;
	private String imageName;
	private BigDecimal imageSize;	//镜像大小
	private String imageStatus;		//状态
	private Character imageIspublic;	//镜像类型。0：未知；1：公共镜像；2：自定义镜像
	private String fromVmId;	//来源云主机id
	private Date createdTime;	//创建时间
	private Date updatedTime;	//修改时间
	private Date deletedTime;	//删除时间
	private Character isDeleted;//是否删除。0：未删除。1：已删除
	private String diskFormat;	//镜像硬盘格式
	private String containerFormat;//镜像容器格式
	private String checkSum;	//限制连接数
	private String ownerId;		//（项目id）
	private Long minDisk;		//最小硬盘
	private Long minRam;		//最小内存
	private Character isProtected;//是否受保护：0：未受保护；1：受保护
	private String osType;		//操作系统类型
	private String sysType;		//操作系统
	private String imageDescription;//描述
	private String prjId;		//项目id
	private String dcId;		//数据中心id
	private String createName;	//创建人姓名
	private Long minCpu; 		//最小cpu
	private String imageUrl;	//镜像地址
	private String reserve1;
	private String reserve2;
	private String reserve3;
	private String reserve4;
	
	private Integer maxCpu;		//最大支持CPU（核）
	private Integer maxRam;		//最大支持内存（GB）
	
	private String sysDetail;  //系统详情
	private String sourceId;   //源镜像id
	private Character isUse;   //0:未启用 1：启用 2：已停用
	private Long sysdiskSize;  //系统盘大小
	private String provider;    //提供商
	private String integratedSoftware; //包含软件
	private String professionType;  //业务类型
	private String marketimageDepict; //市场镜像描述
	
	
	@Id
	@Column(name = "image_id", unique = true, nullable = false, length = 100)
	public String getImageId() {
		return this.imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	@Column(name = "image_name", length = 100)
	public String getImageName() {
		return this.imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	@Column(name = "image_size", precision = 30, scale = 0)
	public BigDecimal getImageSize() {
		return this.imageSize;
	}

	public void setImageSize(BigDecimal imageSize) {
		this.imageSize = imageSize;
	}

	@Column(name = "image_status", length = 30)
	public String getImageStatus() {
		return this.imageStatus;
	}

	public void setImageStatus(String imageStatus) {
		this.imageStatus = imageStatus;
	}

	@Column(name = "image_ispublic", length = 1)
	public Character getImageIspublic() {
		return this.imageIspublic;
	}

	public void setImageIspublic(Character imageIspublic) {
		this.imageIspublic = imageIspublic;
	}

	@Column(name = "from_vmid", length = 100)
	public String getFromVmId() {
		return this.fromVmId;
	}

	public void setFromVmId(String fromVmId) {
		this.fromVmId = fromVmId;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_time", length = 19)
	public Date getCreatedTime() {
		return this.createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_time", length = 19)
	public Date getUpdatedTime() {
		return this.updatedTime;
	}

	public void setUpdatedTime(Date updatedTime) {
		this.updatedTime = updatedTime;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "deleted_time", length = 19)
	public Date getDeletedTime() {
		return this.deletedTime;
	}

	public void setDeletedTime(Date deletedTime) {
		this.deletedTime = deletedTime;
	}

	@Column(name = "is_deleted", length = 1)
	public Character getIsDeleted() {
		return this.isDeleted;
	}

	public void setIsDeleted(Character isDeleted) {
		this.isDeleted = isDeleted;
	}

	@Column(name = "disk_format", length = 20)
	public String getDiskFormat() {
		return this.diskFormat;
	}

	public void setDiskFormat(String diskFormat) {
		this.diskFormat = diskFormat;
	}

	@Column(name = "container_format", length = 20)
	public String getContainerFormat() {
		return this.containerFormat;
	}

	public void setContainerFormat(String containerFormat) {
		this.containerFormat = containerFormat;
	}

	@Column(name = "check_sum", precision = 8, scale = 0)
	public String getCheckSum() {
		return this.checkSum;
	}

	public void setCheckSum(String checkSum) {
		this.checkSum = checkSum;
	}

	@Column(name = "owner_id", length = 100)
	public String getOwnerId() {
		return this.ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	@Column(name = "min_disk", precision = 10, scale = 0)
	public Long getMinDisk() {
		return this.minDisk;
	}

	public void setMinDisk(Long minDisk) {
		this.minDisk = minDisk;
	}

	@Column(name = "min_ram", precision = 10, scale = 0)
	public Long getMinRam() {
		return this.minRam;
	}

	public void setMinRam(Long minRam) {
		this.minRam = minRam;
	}

	@Column(name = "is_protected", length = 1)
	public Character getIsProtected() {
		return this.isProtected;
	}

	public void setIsProtected(Character isProtected) {
		this.isProtected = isProtected;
	}

	@Column(name = "os_type", length = 100)
	public String getOsType() {
		return this.osType;
	}

	public void setOsType(String osType) {
		this.osType = osType;
	}

	@Column(name = "sys_type", length = 100)
	public String getSysType() {
		return this.sysType;
	}

	public void setSysType(String sysType) {
		this.sysType = sysType;
	}

	@Column(name = "image_description", length = 1000)
	public String getImageDescription() {
		return this.imageDescription;
	}

	public void setImageDescription(String imageDescription) {
		this.imageDescription = imageDescription;
	}

	@Column(name = "prj_id", length = 100)
	public String getPrjId() {
		return this.prjId;
	}

	public void setPrjId(String prjId) {
		this.prjId = prjId;
	}

	@Column(name = "dc_id", length = 100)
	public String getDcId() {
		return this.dcId;
	}

	public void setDcId(String dcId) {
		this.dcId = dcId;
	}

	@Column(name = "create_name", length = 100)
	public String getCreateName() {
		return this.createName;
	}

	public void setCreateName(String createName) {
		this.createName = createName;
	}

	@Column(name = "image_url", length = 300)
	public String getImageUrl() {
		return this.imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	@Column(name = "min_cpu", precision = 10, scale = 0)
	public Long getMinCpu() {
		return this.minCpu;
	}

	public void setMinCpu(Long minCpu) {
		this.minCpu = minCpu;
	}
	
	@Column(name = "reserve1", length = 100)
	public String getReserve1() {
		return this.reserve1;
	}

	public void setReserve1(String reserve1) {
		this.reserve1 = reserve1;
	}

	@Column(name = "reserve2", length = 100)
	public String getReserve2() {
		return this.reserve2;
	}

	public void setReserve2(String reserve2) {
		this.reserve2 = reserve2;
	}

	@Column(name = "reserve3", length = 100)
	public String getReserve3() {
		return this.reserve3;
	}

	public void setReserve3(String reserve3) {
		this.reserve3 = reserve3;
	}

	@Column(name = "reserve4", length = 100)
	public String getReserve4() {
		return this.reserve4;
	}

	public void setReserve4(String reserve4) {
		this.reserve4 = reserve4;
	}

	@Column(name = "max_cpu", length = 5)
	public Integer getMaxCpu() {
		return maxCpu;
	}

	public void setMaxCpu(Integer maxCpu) {
		this.maxCpu = maxCpu;
	}

	@Column(name = "max_ram", length = 10)
	public Integer getMaxRam() {
		return maxRam;
	}

	public void setMaxRam(Integer maxRam) {
		this.maxRam = maxRam;
	}
	
	@Column(name = "sys_detail", length = 100)
	public String getSysDetail() {
		return sysDetail;
	}

	public void setSysDetail(String sysDetail) {
		this.sysDetail = sysDetail;
	}

	@Column(name = "source_id", length = 100)
	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	@Column(name = "is_use", length = 1)
	public Character getIsUse() {
		return isUse;
	}

	public void setIsUse(Character isUse) {
		this.isUse = isUse;
	}

	@Column(name = "sysdisk_size", precision = 10, scale = 0)
	public Long getSysdiskSize() {
		return sysdiskSize;
	}

	public void setSysdiskSize(Long sysdiskSize) {
		this.sysdiskSize = sysdiskSize;
	}

	@Column(name = "provider", length = 200)
	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	@Column(name = "integrated_software", length = 800)
	public String getIntegratedSoftware() {
		return integratedSoftware;
	}

	public void setIntegratedSoftware(String integratedSoftware) {
		this.integratedSoftware = integratedSoftware;
	}

	@Column(name = "profession_type", length = 100)
	public String getProfessionType() {
		return professionType;
	}

	public void setProfessionType(String professionType) {
		this.professionType = professionType;
	}
	
	@Column(name = "marketimage_depict")
	public String getMarketimageDepict() {
		return marketimageDepict;
	}

	public void setMarketimageDepict(String marketimageDepict) {
		this.marketimageDepict = marketimageDepict;
	}

	public BaseCloudImage (){}
	
	public BaseCloudImage(Image image,String dcId) throws Exception{
		if(null!=image){
			this.imageId = image.getId();
			this.imageName = image.getName();
			this.imageStatus = image.getStatus()!=null?image.getStatus().toUpperCase():"";
			this.diskFormat = image.getDisk_format();
			this.containerFormat = image.getContainer_format();
			if(!StringUtils.isEmpty(image.getSize())){
				this.imageSize = new BigDecimal(image.getSize());
			}
			if("snapshot".equals(image.getImage_type())){
				this.imageIspublic='2';
			}
			else if("public".equals(image.getVisibility())){
				this.imageIspublic='1';
			}
			else if("private".equals(image.getVisibility())){
				this.imageIspublic='0';
			}
			this.checkSum = image.getCheckSum();
			this.ownerId = image.getOwner_id();
			if(!StringUtils.isEmpty(image.getMin_disk()))
				this.minDisk = Long.parseLong(image.getMin_disk());
			if(!StringUtils.isEmpty(image.getMin_ram()))
				this.minRam = Long.parseLong(image.getMin_ram());
			this.isProtected = (image.isProtected())?'1':'0';
			this.prjId = image.getOwner_id();
			this.dcId = dcId;
			this.fromVmId=image.getInstance_uuid();
		}
	}

}
