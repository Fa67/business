package com.eayun.obs.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "cdn_bucket")
public class BaseCdnBucket implements Serializable{

	/**
	 *Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "id", unique = true, nullable = false, length = 36)
	private String id;
	
	@Column(name = "cdn_provider", length = 36)
	private String cdnProvider;				//CDN供应商，enum<UpYun, ChinaNetCenter>
	
	@Column(name = "cus_id", length = 36)
	private String cusId;					//客户id
	
	@Column(name = "bucket_name", length = 64)
	private String bucketName;				//bucket名称
	
	@Column(name = "close_time")
	private Date closeTime;					//上次关闭时间
	
	@Column(name = "delete_time")
	private Date deleteTime;				//删除时间
	
	@Column(name = "is_opencdn", length = 10)
	private String isOpencdn;				//是否开通CDN	未开通：0	已开通：1

	
	@Column(name = "cdn_status", length = 10)
	private String cdnStatus;				//CDN状态		未加速：0	设置中：1	已加速：2

	
	@Column(name = "cdn_path", length = 255)
	private String cdnPath;					//CDN加速地址
	
	@Column(name = "domain_id", length = 36)
	private String domainId;				//加速域名（UpYun为我们自己生成的不重复的字符串）
	
	@Column(name = "record_id", length = 36)
	private String recordId;				//DNS记录id
	
	@Column(name = "is_delete", length = 10)
	private String isDelete;				//是否删除		0：未删除	1：已删除


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCdnProvider() {
		return cdnProvider;
	}

	public void setCdnProvider(String cdnProvider) {
		this.cdnProvider = cdnProvider;
	}

	public String getCusId() {
		return cusId;
	}

	public void setCusId(String cusId) {
		this.cusId = cusId;
	}

	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

	public Date getCloseTime() {
		return closeTime;
	}

	public void setCloseTime(Date closeTime) {
		this.closeTime = closeTime;
	}

	public Date getDeleteTime() {
		return deleteTime;
	}

	public void setDeleteTime(Date deleteTime) {
		this.deleteTime = deleteTime;
	}

	public String getIsOpencdn() {
		return isOpencdn;
	}

	public void setIsOpencdn(String isOpencdn) {
		this.isOpencdn = isOpencdn;
	}

	public String getCdnStatus() {
		return cdnStatus;
	}

	public void setCdnStatus(String cdnStatus) {
		this.cdnStatus = cdnStatus;
	}

	public String getCdnPath() {
		return cdnPath;
	}

	public void setCdnPath(String cdnPath) {
		this.cdnPath = cdnPath;
	}

	public String getDomainId() {
		return domainId;
	}

	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}

	public String getRecordId() {
		return recordId;
	}

	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}

	public String getIsDelete() {
		return isDelete;
	}

	public void setIsDelete(String isDelete) {
		this.isDelete = isDelete;
	}
	
	
}
