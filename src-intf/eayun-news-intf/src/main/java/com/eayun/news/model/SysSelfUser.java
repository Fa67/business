package com.eayun.news.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "sys_selfuser")
public class SysSelfUser implements java.io.Serializable {
	
	private static final long serialVersionUID = 8396272820854892269L;

	@Id
	@GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name="user_id", length=32)
	private String userId;   //id

    @Column(name="user_account", length=32)
	private String userAccount;   //消息id
    
    @Column(name="cus_id", length=32)
	private String cusId;   //客户id
    
    @Column(name="is_admin", length=1)
	private String isAdmin;   //是否管理员  1：是  2：不是

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserAccount() {
		return userAccount;
	}

	public void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
	}

	public String getCusId() {
		return cusId;
	}

	public void setCusId(String cusId) {
		this.cusId = cusId;
	}

	public String getIsAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(String isAdmin) {
		this.isAdmin = isAdmin;
	}
    
}
