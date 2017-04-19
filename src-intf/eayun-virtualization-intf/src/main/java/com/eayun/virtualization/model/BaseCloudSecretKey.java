package com.eayun.virtualization.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "cloud_secretkey")
public class BaseCloudSecretKey implements Serializable {
    
	@Id
	@Column(name = "secretkey_id", unique = true, nullable = false, length = 32)
	private String secretkeyId;//id
	
	@Column(name = "fingerprint",length = 200)
	private String fingerPrint;//指纹信息
	
	@Column(name = "secretkey_name",length = 200)
	private String secretkeyName;//秘钥名称
	
	@Column(name="secretkey_desc",length=1000)
	private String secretkeyDesc;//描述
	
	@Column(name="create_time")
	private Date createTime;//创建时间 
	
	@Column(name="prj_id",length = 32)
	private String prjId;//项目ID
	
	
	@Column(name="dc_id",length = 32)
	private String dcId;//数据中心ID
	
	@Column(name="public_key",length = 2000)
	private String publicKey;


	public String getSecretkeyId() {
		return secretkeyId;
	}


	public void setSecretkeyId(String secretkeyId) {
		this.secretkeyId = secretkeyId;
	}


	public String getSecretkeyName() {
		return secretkeyName;
	}


	public void setSecretkeyName(String secretkeyName) {
		this.secretkeyName = secretkeyName;
	}


	public String getSecretkeyDesc() {
		return secretkeyDesc;
	}


	public void setSecretkeyDesc(String secretkeyDesc) {
		this.secretkeyDesc = secretkeyDesc;
	}


	public Date getCreateTime() {
		return createTime;
	}


	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}


	public String getPrjId() {
		return prjId;
	}


	public void setPrjId(String prjId) {
		this.prjId = prjId;
	}


	public String getDcId() {
		return dcId;
	}


	public void setDcId(String dcId) {
		this.dcId = dcId;
	}


	
	public BaseCloudSecretKey(String secretkeyId, String fingerPrint, String secretkeyName, String secretkeyDesc,
			Date createTime, String prjId, String dcId, String publicKey) {
		super();
		this.secretkeyId = secretkeyId;
		this.fingerPrint = fingerPrint;
		this.secretkeyName = secretkeyName;
		this.secretkeyDesc = secretkeyDesc;
		this.createTime = createTime;
		this.prjId = prjId;
		this.dcId = dcId;
		this.publicKey = publicKey;
	}


	public String getFingerPrint() {
		return fingerPrint;
	}


	public void setFingerPrint(String fingerPrint) {
		this.fingerPrint = fingerPrint;
	}


	public String getPublicKey() {
		return publicKey;
	}


	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}


	public BaseCloudSecretKey(){};
	
	
	

}
