package com.eayun.ecmcuser.util;

import java.util.List;

import javax.servlet.http.HttpSession;

import com.eayun.common.util.SessionUtil;
import com.eayun.ecmcauthority.model.BaseEcmcSysAuthority;
import com.eayun.ecmcmenu.model.EcmcSysMenuTreeGrid;
import com.eayun.ecmcrole.model.BaseEcmcSysRole;
import com.eayun.ecmcrole.model.EcmcSysRole;
import com.eayun.ecmcuser.model.BaseEcmcSysUser;

/**
 * Ecmc Session 会话工具
 * 
 * 管理ecmc session存取
 * 
 * @author zhujun
 * @date 2016年4月7日
 *
 */
public final class EcmcSessionUtil {

	private final static String USER_SESSION_KEY = "ecmc.user";
	private final static String ROLE_SESSION_KEY = "ecmc.user.role";
	private final static String AUTH_SESSION_KEY = "ecmc.user.auth";
	private final static String MENU_SESSION_KEY = "ecmc.user.menu";
	
	private EcmcSessionUtil() {
	}
	
	private static HttpSession getSession() {
		return SessionUtil.getSession();
	}
	
	
	public static void setUser(BaseEcmcSysUser user) {
		getSession().setAttribute(USER_SESSION_KEY, user);
	}
	
	/**
	 * 获取会话中的 用户
	 * @author zhujun
	 * @date 2016年4月7日
	 *
	 * @return
	 */
	public static BaseEcmcSysUser getUser() {
		HttpSession session = getSession();
		if(null != session){
			return (BaseEcmcSysUser)session.getAttribute(USER_SESSION_KEY);
		}
		return null;
	}
	
	public static void setUserRoles(List<EcmcSysRole> roles) {
		getSession().setAttribute(ROLE_SESSION_KEY, roles);
	}
	
	/**
	 * 获取会话中的 用户的角色
	 * @author zhujun
	 * @date 2016年4月7日
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
    public static List<BaseEcmcSysRole> getUserRoles() {
		return (List<BaseEcmcSysRole>)getSession().getAttribute(ROLE_SESSION_KEY);
	}
	
	
	public static void setUserAuths(List<BaseEcmcSysAuthority> auths) {
		getSession().setAttribute(AUTH_SESSION_KEY, auths);
	}
	
	/**
	 * 获取会话中的 用户的权限
	 * @author zhujun
	 * @date 2016年4月7日
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
    public static List<BaseEcmcSysAuthority> getUserAuths() {
		return (List<BaseEcmcSysAuthority>)getSession().getAttribute(AUTH_SESSION_KEY);
	}
	
	
	public static void setUserMenus(List<EcmcSysMenuTreeGrid> menus) {
		getSession().setAttribute(MENU_SESSION_KEY, menus);
	}
	
	/**
	 * 获取会话中的 用户的菜单
	 * @author zhujun
	 * @date 2016年4月7日
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
    public static List<EcmcSysMenuTreeGrid> getUserMenus() {
		return (List<EcmcSysMenuTreeGrid>)getSession().getAttribute(MENU_SESSION_KEY);
	}

	
}
