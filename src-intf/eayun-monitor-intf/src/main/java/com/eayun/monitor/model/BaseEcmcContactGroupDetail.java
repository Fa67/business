package com.eayun.monitor.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "ecmc_contactgroupdetail")
public class BaseEcmcContactGroupDetail implements Serializable {

	private static final long serialVersionUID = -5096116926748594705L;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "mgd_id", unique = true, nullable = false, length = 32)
	private String id;
	@Column(name = "mgd_groupid", length = 32)
	private String groupId;
	@Column(name = "mgd_contactid", length = 32)
	private String contactId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

}
