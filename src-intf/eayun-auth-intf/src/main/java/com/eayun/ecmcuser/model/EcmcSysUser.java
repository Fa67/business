package com.eayun.ecmcuser.model;

import java.util.ArrayList;

import com.eayun.common.util.BeanUtils;
import com.eayun.ecmcrole.model.EcmcSysRole;

public class EcmcSysUser extends BaseEcmcSysUser {

	private static final long serialVersionUID = 1L;
	
	private String departName;																													//部门名称
	private ArrayList<EcmcSysRole> roles = new ArrayList<EcmcSysRole>();		//用户角色
	
	public EcmcSysUser(){super();}
	
	public EcmcSysUser(BaseEcmcSysUser base){
	    super();
	    BeanUtils.copyPropertiesByModel(this, base);
	}

	public String getDepartName() {
		return departName;
	}

	public void setDepartName(String departName) {
		this.departName = departName;
	}

	public ArrayList<EcmcSysRole> getRoles() {
		return roles;
	}

	public void setRoles(ArrayList<EcmcSysRole> roles) {
		this.roles = roles;
	}
	
}
