package com.eayun.virtualization.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;



/**
 * 云硬盘类型          
 * @Filename: BaseCloudVolumeType.java
 * @Description: 
 * @Author: chengxiaodong
 *<li>Date: 2017年02月16日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Entity
@Table(name = "cloud_volumetype")
public class BaseCloudVolumeType implements java.io.Serializable {

private static final long serialVersionUID = -1116765119289877206L;
	
    private String id;    	   //主键ID
	private String typeId;     //云硬盘类型ID
	private String typeName;   //云硬盘类型名称
	private Date updateTime;   //更新时间
	private String volumeType; //云硬盘类型（1普通型 2性能型 3超高性能型）
	private String dcId;       //数据中心id
	private int maxSize;       //允许单块云硬盘最大容量
	private int maxIops;       //允许最大iops数
	private int maxThroughput; //允许最大吞吐量
	private String isUse;      //0未启用   1已启用 2已停用
	private String qosId;      //qosId
	
	
	
	@Id
	@Column(name = "id", unique = true, nullable = false, length = 100)
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Column(name = "type_id", length = 100)
	public String getTypeId() {
		return typeId;
	}
	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}
	@Column(name = "type_name", length = 100)
	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "update_time", length = 19)
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	@Column(name = "volume_type", length = 1)
	public String getVolumeType() {
		return volumeType;
	}
	public void setVolumeType(String volumeType) {
		this.volumeType = volumeType;
	}
	@Column(name = "dc_id", length = 100)
	public String getDcId() {
		return dcId;
	}
	public void setDcId(String dcId) {
		this.dcId = dcId;
	}
	@Column(name = "max_size", length = 20)
	public int getMaxSize() {
		return maxSize;
	}
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}
	@Column(name = "max_iops", length = 20)
	public int getMaxIops() {
		return maxIops;
	}
	public void setMaxIops(int maxIops) {
		this.maxIops = maxIops;
	}
	@Column(name = "max_throughput", length = 20)
	public int getMaxThroughput() {
		return maxThroughput;
	}
	public void setMaxThroughput(int maxThroughput) {
		this.maxThroughput = maxThroughput;
	}
	@Column(name = "is_use", length = 1)
	public String getIsUse() {
		return isUse;
	}
	public void setIsUse(String isUse) {
		this.isUse = isUse;
	}
	@Column(name = "qos_id", length = 100)
	public String getQosId() {
		return qosId;
	}
	public void setQosId(String qosId) {
		this.qosId = qosId;
	}
	
	
	
	public BaseCloudVolumeType(String id, String typeId, String typeName,
			Date updateTime, String volumeType, String dcId, int maxSize,
			int maxIops, int maxThroughput, String isUse, String qosId) {
		super();
		this.id = id;
		this.typeId = typeId;
		this.typeName = typeName;
		this.updateTime = updateTime;
		this.volumeType = volumeType;
		this.dcId = dcId;
		this.maxSize = maxSize;
		this.maxIops = maxIops;
		this.maxThroughput = maxThroughput;
		this.isUse = isUse;
		this.qosId = qosId;
	}
	public BaseCloudVolumeType() {
		super();
	}
	
	
	
	
	

}
