package com.eayun.monitor.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "ecmc_contact")
public class BaseEcmcContact implements Serializable {

	private static final long serialVersionUID = 361783045506338335L;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "mc_id", unique = true, nullable = false, length = 32)
	private String id;
	@Column(name = "mc_name", length = 64)
	private String name;
	@Column(name = "mc_phone", length = 11)
	private String phone;
	@Column(name = "mc_email", length = 100)
	private String email;
	@Column(name = "mc_smsnotify", length = 1)
	private String smsNotify;
	@Column(name = "mc_mailnotify", length = 1)
	private String mailNotify;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSmsNotify() {
		return smsNotify;
	}

	public void setSmsNotify(String smsNotify) {
		this.smsNotify = smsNotify;
	}

	public String getMailNotify() {
		return mailNotify;
	}

	public void setMailNotify(String mailNotify) {
		this.mailNotify = mailNotify;
	}

}
