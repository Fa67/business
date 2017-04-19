package com.eayun.ecmcauthority.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 * 角色 权限 关联表
* @Author:fangjun.yang
* @Date:2016年3月1日
*/
@Entity
@Table(name = "ecmc_sys_roleauth")
public class BaseEcmcSysRoleAuth implements Serializable{

	private static final long serialVersionUID = -8084407695969148019L;

	@Id
	@GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "id", unique = true, nullable = false, length = 32)
	private String id;			//ID
	
	@Column(name = "role_id", unique = true, nullable = false, length = 32)
	private String roleId;		//角色ID
	
	@Column(name = "auth_id", unique = true, nullable = false, length = 32)
	private String authId;		//权限ID

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getAuthId() {
		return authId;
	}

	public void setAuthId(String authId) {
		this.authId = authId;
	}
	
	
}

