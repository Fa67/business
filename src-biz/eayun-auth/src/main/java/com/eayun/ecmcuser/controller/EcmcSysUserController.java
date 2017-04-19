package com.eayun.ecmcuser.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.StringUtil;
import com.eayun.ecmcauthority.model.BaseEcmcSysAuthority;
import com.eayun.ecmcauthority.service.EcmcSysAuthorityService;
import com.eayun.ecmcmenu.service.EcmcSysMenuService;
import com.eayun.ecmcrole.model.EcmcSysRole;
import com.eayun.ecmcrole.service.EcmcSysRoleService;
import com.eayun.ecmcuser.model.BaseEcmcSysUser;
import com.eayun.ecmcuser.model.EcmcSysUser;
import com.eayun.ecmcuser.service.EcmcSysUserService;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.log.ecmcsevice.EcmcLogService;

/**
 * 用户登录
 * 
 * @author zengbo
 *
 */
@Controller
@RequestMapping("/ecmc/system/user")
@Scope("prototype")
public class EcmcSysUserController {
    private static final Logger     log                 = LoggerFactory.getLogger(EcmcSysUserController.class);

    private final static String     LOG_TYPE_USER       = "用户";

    private final static String     LOG_OPT_ADD         = "创建用户";

    private final static String     LOG_OPT_MODIFY      = "修改用户";

    private final static String     LOG_OPT_DELETE      = "删除用户";

    private final static String     LOG_OPT_LOGIN       = "登录";

    private final static String     LOG_OPT_LOGOUT      = "退出";

    private final static String     LOG_OPT_CHANGE_PASS = "修改密码";

    @Autowired
    private EcmcSysUserService      ecmcSysUserService;
    @Autowired
    private EcmcSysRoleService      ecmcSysRoleService;
    @Autowired
    private EcmcSysAuthorityService ecmcSysAuthorityService;
    @Autowired
    private EcmcSysMenuService      ecmcSysMenuService;

    @Autowired
    private EcmcLogService          ecmcLogService;

    @RequestMapping(value = "/login")
    @ResponseBody
    public Object login(HttpServletRequest request, @RequestBody Map<String, Object> requestMap) throws Exception {
        log.info("登陆开始……");
        // 校验验证码
        String codenum = MapUtils.getString(requestMap, "codenum");
        String correctNum = null;
        HttpSession session = request.getSession();
        String loginJson = (String) session.getAttribute("login");
        if (!StringUtil.isEmpty(loginJson)) {
            JSONObject jsonObj = JSONObject.parseObject(loginJson);
            correctNum = jsonObj.getString("code");
        }
        String userAccount = MapUtils.getString(requestMap, "userName");
        String userPasswd = MapUtils.getString(requestMap, "userPasswd");
        String passKey = ObjectUtils.toString(request.getSession().getAttribute(ConstantClazz.PASSWORD_SESSION));
        request.getSession().removeAttribute(ConstantClazz.PASSWORD_SESSION);
        EayunResponseJson reJson = ecmcSysUserService.login(codenum, correctNum, userAccount, userPasswd, passKey);
        BaseEcmcSysUser user = (BaseEcmcSysUser) reJson.getData();
        if (user != null) {
            // 将用户信息存入session
            EcmcSessionUtil.setUser(user);
            // 将用户角色信息存入session
            List<EcmcSysRole> userRoles = ecmcSysRoleService.findRolesByUserId(user.getId());
            EcmcSessionUtil.setUserRoles(userRoles);
            List<String> userRoleIds = new ArrayList<String>();
            for (EcmcSysRole ecmcSysRole : userRoles) {
                userRoleIds.add(ecmcSysRole.getId());
            }
            if (userRoleIds.size() > 0) {
                // 将用户权限信息存入session
                List<BaseEcmcSysAuthority> ecmcSysAuthorityList = ecmcSysAuthorityService.getSysAuthorityListByRoleIds(userRoleIds);
                EcmcSessionUtil.setUserAuths(ecmcSysAuthorityList);
                // 将用户菜单信息存入session
                EcmcSessionUtil.setUserMenus(ecmcSysMenuService.getMenuGridyByRoleIds(userRoleIds));
            }
            ecmcLogService.addLog(LOG_OPT_LOGIN, LOG_TYPE_USER, userAccount, null, 1, user.getId(), null);
        } else {
            ecmcLogService.addLog(LOG_OPT_LOGIN, LOG_TYPE_USER, userAccount, null, 0, null, null);
        }
        return reJson;
    }

    /**
     * 登出
     * @return
     */
    @RequestMapping(value = "/logout")
    @ResponseBody
    public Object logout() {
        EayunResponseJson reJson = new EayunResponseJson();
        try {
            ecmcLogService.addLog(LOG_OPT_LOGOUT, LOG_TYPE_USER, EcmcSessionUtil.getUser() != null ? EcmcSessionUtil.getUser().getName() : null, null, 1, EcmcSessionUtil.getUser() != null ? EcmcSessionUtil.getUser().getId() : null, null);
            EcmcSessionUtil.setUser(null);
            EcmcSessionUtil.setUserAuths(null);
            EcmcSessionUtil.setUserMenus(null);
            EcmcSessionUtil.setUserRoles(null);
            reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            reJson.setRespCode(ConstantClazz.ERROR_CODE);
        }
        return reJson;
    }

    /**
     * 查询所有用户
     * 
     * @return
     */
    @RequestMapping(value = "/finduserlist")
    @ResponseBody
    public Object findUserList(@RequestBody ParamsMap paramsMap) {
        log.info("根据部门查询所有用户");
        QueryMap queryMap = new QueryMap();
        Map<String, Object> params = paramsMap.getParams();
        queryMap.setPageNum(paramsMap.getPageNumber() == null ? 1 : paramsMap.getPageNumber());
        if (paramsMap.getPageSize() != null) {
            queryMap.setCURRENT_ROWS_SIZE(paramsMap.getPageSize());
        }
        String departId = MapUtils.getString(params, "departId");
        if (!StringUtil.isEmpty(departId)) {
            return ecmcSysUserService.findPageUserByDepartId(departId, queryMap);
        }
        return null;
    }

    /**
     * 查询用户详细信息
     * 
     * @return
     */
    @RequestMapping(value = "/getuserdetail")
    @ResponseBody
    public Object getUserDetail(@RequestBody Map<String, Object> requestMap) {
        log.info("用户详细信息");
        EayunResponseJson reJson = new EayunResponseJson();
        reJson.setRespCode(ConstantClazz.ERROR_CODE);
        String userId = MapUtils.getString(requestMap, "userId");
        if (!StringUtil.isEmpty(userId)) {
            EcmcSysUser ecmcSysUser = ecmcSysUserService.findUserById(userId);
            if(ecmcSysUser != null){
                reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
                reJson.setData(ecmcSysUser);
            }else{
                reJson.setMessage("找不到用户");
            }
        }
        return reJson;
    }

    /**
     * 创建用户
     * 
     * @return
     */
    @RequestMapping(value = "/createuser")
    @ResponseBody
    public Object createUser(@RequestBody Map<String, Object> requestMap) {
        log.info("创建用户");
        EayunResponseJson reJson = new EayunResponseJson();
        reJson.setRespCode(ConstantClazz.ERROR_CODE);
        try {
            // 验证用户Account是否重复
            if (ecmcSysUserService.checkUserAccount(MapUtils.getString(requestMap, "account"), null)) {
                BaseEcmcSysUser user = ecmcSysUserService.addUser(requestMap);
                reJson.setData(user);
                reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
                ecmcLogService.addLog(LOG_OPT_ADD, LOG_TYPE_USER, user.getAccount(), null, 1, user.getId(), null);
            } else {
                log.info("用户Account验证未通过！");
                reJson.setMessage("用户Account已经存在！");
                ecmcLogService.addLog(LOG_OPT_ADD, LOG_TYPE_USER, (String)requestMap.get("account"), null, 0, null, null);
            }
        } catch (Exception e) {
            log.info("创建用户时异常：{}", e.getMessage());
            ecmcLogService.addLog(LOG_OPT_ADD, LOG_TYPE_USER, (String)requestMap.get("account"), null, 0, null, e);
        }
        return reJson;
    }

    /**
     * 修改用户
     * 
     * @return
     */
    @RequestMapping(value = "/modifyuser")
    @ResponseBody
    public Object modifyUser(@RequestBody Map<String, Object> requestMap) {
        log.info("更改用户信息");
        EayunResponseJson reJson = new EayunResponseJson();
        reJson.setRespCode(ConstantClazz.ERROR_CODE);
        try {
            reJson.setData(ecmcSysUserService.updateUser(requestMap));
            reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
            ecmcLogService.addLog(LOG_OPT_MODIFY, LOG_TYPE_USER, (String)requestMap.get("account"), null, 1, (String)requestMap.get("id"), null);
        } catch (Exception e) {
            log.info("修改用户时发生异常：{}", e.getMessage());
            ecmcLogService.addLog(LOG_OPT_MODIFY, LOG_TYPE_USER, (String)requestMap.get("account"), null, 0, (String)requestMap.get("id"), e);
        }
        return reJson;
    }

    /**
     * 删除用户
     * 
     * @return
     */
    @RequestMapping(value = "/deleteuser")
    @ResponseBody
    public Object deleteUser(@RequestBody Map<String, Object> requestMap) {
        log.info("删除用户");
        String userId = MapUtils.getString(requestMap, "userId");
        EayunResponseJson reJson = new EayunResponseJson();
        reJson.setRespCode(ConstantClazz.ERROR_CODE);
        if (!StringUtil.isEmpty(userId)) {
            //判断删除的是否是当前登录用户自己
            if (EcmcSessionUtil.getUser() != null && !userId.equals(EcmcSessionUtil.getUser().getId())) {
                if(ecmcSysUserService.hasUnfinishedWorkOrder(userId)){
                    reJson.setMessage("该用户名有未完成的工单");
                }else{
                    try {
                        ecmcSysUserService.delUser(userId);
                        reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
                    } catch (Exception e) {
                        ecmcLogService.addLog(LOG_OPT_DELETE, LOG_TYPE_USER, null, null, 0, userId, e);
                        throw new AppException("删除用户失败", e);
                    }
                }
            } else {
                reJson.setMessage("不能删除自己");
            }
        }
        if(ConstantClazz.SUCCESS_CODE.equals(reJson.getRespCode())){
            ecmcLogService.addLog(LOG_OPT_DELETE, LOG_TYPE_USER, null, null, 1, userId, null);
        }else{
            ecmcLogService.addLog(LOG_OPT_DELETE, LOG_TYPE_USER, null, null, 0, userId, null);
        }
        return reJson;
    }

    /**
     * 校验用户Account
     * 
     * @return
     */
    @RequestMapping(value = "/checkaccount")
    @ResponseBody
    public Object checkAccount(@RequestBody Map<String, Object> requestMap) {
        log.info("校验用户Account是否可用（判断重复）");
        EayunResponseJson reJson = new EayunResponseJson();
        reJson.setRespCode(ConstantClazz.ERROR_CODE);
        String userAccount = MapUtils.getString(requestMap, "account");
        String id = MapUtils.getString(requestMap, "id");
        if (!StringUtil.isEmpty(userAccount) && ecmcSysUserService.checkUserAccount(userAccount, id)) {
            reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        }
        return reJson;
    }

    @RequestMapping(value = "/finduserbypermission")
    @ResponseBody
    public Object findUserByPermission(@RequestBody Map<String, Object> requestMap) {
        EayunResponseJson reJson = new EayunResponseJson();
        reJson.setRespCode(ConstantClazz.ERROR_CODE);
        String permission = MapUtils.getString(requestMap, "permission");
        String userName = MapUtils.getString(requestMap, "userName");
        if (!StringUtil.isEmpty(permission)) {
            reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
            reJson.setData(ecmcSysUserService.findUserByPermission(userName, permission));
        }
        return reJson;
    }

    /**
     * 获取 会话信息
     * @return
     */
    @RequestMapping(value = "/getsessioninfo")
    @ResponseBody
    public Object getSessionInfo() {

        EayunResponseJson reJson = new EayunResponseJson();
		reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		Map<String, Object> sessionInfo = new HashMap<>();
		BaseEcmcSysUser user = EcmcSessionUtil.getUser();
		if (user != null) {
			user.setPassword(null);
		}
		sessionInfo.put("user", user);
		sessionInfo.put("roles", EcmcSessionUtil.getUserRoles());
		sessionInfo.put("uiPermissions", getUiPermissions(EcmcSessionUtil.getUserAuths()));
		
		// 菜单
		sessionInfo.put("menus", EcmcSessionUtil.getUserMenus());
		
		reJson.setData(sessionInfo);
		return reJson;
    }

    
    private Set<String> getUiPermissions(List<BaseEcmcSysAuthority> userAuths) {
		if (userAuths == null || userAuths.isEmpty()) {
			return null;
		}
		
		Set<String> uiPermissionSet = new HashSet<>();
		String[] permissionArray = null;
		for (BaseEcmcSysAuthority auth : userAuths) {
			if (auth != null && auth.getPermission() != null) {
				permissionArray = auth.getPermission().split(";");
				for (String permission : permissionArray) {
					permission = permission.trim();
					if (permission.startsWith("ui:")) {
						uiPermissionSet.add(permission);
					}
				}
			}
		}
    	
		return uiPermissionSet;
	}

	@RequestMapping("/changepass")
    @ResponseBody
    public Object changePass(HttpServletRequest request, @RequestBody Map<String, Object> requestMap) {
        String origPwd = MapUtils.getString(requestMap, "origPwd");
        String newPwd = MapUtils.getString(requestMap, "newPwd");
        String passKey = ObjectUtils.toString(request.getSession().getAttribute(ConstantClazz.PASSWORD_SESSION));
        request.getSession().removeAttribute(ConstantClazz.PASSWORD_SESSION);
        BaseEcmcSysUser baseUser = EcmcSessionUtil.getUser();
        EcmcSysUser user = new EcmcSysUser();
        BeanUtils.copyPropertiesByModel(user, baseUser);
        try {
            EayunResponseJson responseJson = ecmcSysUserService.changePass(origPwd, newPwd, passKey, user);
            if(ConstantClazz.SUCCESS_CODE.equals(responseJson.getRespCode())){
                ecmcLogService.addLog(LOG_OPT_CHANGE_PASS, LOG_TYPE_USER, user.getAccount(), null, 1, user.getId(), null);
            }else{
                ecmcLogService.addLog(LOG_OPT_CHANGE_PASS, LOG_TYPE_USER, user.getAccount(), null, 0, user.getId(), null);
            }
            return responseJson;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ecmcLogService.addLog(LOG_OPT_CHANGE_PASS, LOG_TYPE_USER, user.getAccount(), null, 0, user.getId(), e);
            throw e;
        }
    }

    @RequestMapping("/getpasskey")
    @ResponseBody
    public Object getPassKey(HttpServletRequest request) {
        int key = (int) ((Math.random() * 9 + 1) * 100000);
        String passKey = String.valueOf(key);
        request.getSession().setAttribute(ConstantClazz.PASSWORD_SESSION, passKey);
        return passKey;
    }
}
