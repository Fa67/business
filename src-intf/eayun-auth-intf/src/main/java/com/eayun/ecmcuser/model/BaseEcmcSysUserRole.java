package com.eayun.ecmcuser.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 * @author zengbo 用户表映射实体类
 *
 */
@Entity
@Table(name = "ecmc_sys_userrole")
public class BaseEcmcSysUserRole implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "id", unique = true, nullable = false, length = 32)
	private String id;																				//ID
	
	@Column(name = "user_id", length = 32)
	private String userId;																		//用户ID
	
	@Column(name = "role_id", length = 32)
	private String roleId;																		//角色ID

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}
	
	

}
