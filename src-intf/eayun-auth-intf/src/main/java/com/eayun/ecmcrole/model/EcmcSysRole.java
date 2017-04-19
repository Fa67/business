package com.eayun.ecmcrole.model;

import java.util.ArrayList;
import java.util.List;

import com.eayun.common.util.BeanUtils;
import com.eayun.ecmcauthority.model.BaseEcmcSysAuthority;
import com.eayun.ecmcmenu.model.BaseEcmcSysMenu;

public class EcmcSysRole extends BaseEcmcSysRole {

	private static final long serialVersionUID = 1L;
	
	private String roleId;							//角色ID
	private String roleName;					//角色名称
	private List<BaseEcmcSysMenu> menus = new ArrayList<BaseEcmcSysMenu>();			//角色菜单
	private List<BaseEcmcSysAuthority> authorities = new ArrayList<BaseEcmcSysAuthority>();			//角色权限
	
	public EcmcSysRole(){
	    super();
	}
	
	public EcmcSysRole(BaseEcmcSysRole base){
	    super();
	    BeanUtils.copyPropertiesByModel(this, base);
	}
	
	public String getRoleId() {
		return roleId;
	}
	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}
	public String getRoleName() {
		return roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	public List<BaseEcmcSysMenu> getMenus() {
		return menus;
	}
	public void setMenus(List<BaseEcmcSysMenu> menus) {
		this.menus = menus;
	}
	public List<BaseEcmcSysAuthority> getAuthorities() {
		return authorities;
	}
	public void setAuthorities(List<BaseEcmcSysAuthority> authorities) {
		this.authorities = authorities;
	}
}
