package com.eayun.ecmcuser.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.ConstantClazz;
import com.eayun.common.constant.EcmcRoleIds;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.AuthUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.DesUtil;
import com.eayun.common.util.MD5;
import com.eayun.common.util.StringUtil;
import com.eayun.ecmcauthority.dao.EcmcSysRoleAuthDao;
import com.eayun.ecmcdepartment.dao.EcmcSysDepartmentDao;
import com.eayun.ecmcdepartment.model.BaseEcmcSysDepartment;
import com.eayun.ecmcrole.dao.EcmcSysRoleDao;
import com.eayun.ecmcrole.model.EcmcSysRole;
import com.eayun.ecmcuser.dao.EcmcSysUserDao;
import com.eayun.ecmcuser.dao.EcmcSysUserRoleDao;
import com.eayun.ecmcuser.model.BaseEcmcSysUser;
import com.eayun.ecmcuser.model.BaseEcmcSysUserRole;
import com.eayun.ecmcuser.model.EcmcSysUser;
import com.eayun.ecmcuser.service.EcmcSysUserService;
import com.eayun.ecmcuser.util.EcmcSessionUtil;

/**
 * @author zengbo
 *
 */
@Service
@Transactional
public class EcmcSysUserServiceImpl implements EcmcSysUserService {

    private static final Logger  log = LoggerFactory.getLogger(EcmcSysUserServiceImpl.class);
    @Autowired
    private EcmcSysUserDao       ecmcUserDao;
    @Autowired
    private EcmcSysUserRoleDao   ecmcUserRoleDao;
    @Autowired
    private EcmcSysRoleDao       ecmcRoleDao;
    @Autowired
    private EcmcSysDepartmentDao ecmcDepartmentDao;
    @Autowired
    private EcmcSysRoleAuthDao   ecmcSysRoleAuthDao;

    @SuppressWarnings("unchecked")
    @Override
    @Transactional
    public EcmcSysUser addUser(Map<String, Object> map) throws AppException {
        log.info("添加用户");
        BaseEcmcSysUser baseUser = new BaseEcmcSysUser();
        try {
            BeanUtils.mapToBean(baseUser, map);
            baseUser.setCreateTime(new Date()); // 设置创建时间
            // 生成密码干扰串
            int salt = (int) ((Math.random() * 9 + 1) * 100000);
            String userSalt = String.valueOf(salt);
            baseUser.setSalt(userSalt);
            // 用户密码加密
            String saltPassword = AuthUtil.createSaltPassword(baseUser.getPassword(), userSalt);
            baseUser.setPassword(saltPassword);
            //创建者
            BaseEcmcSysUser createUser = EcmcSessionUtil.getUser();
            if (createUser != null) {
                baseUser.setCreatedBy(createUser.getId());
            }
            // 用户信息入库
            baseUser = ecmcUserDao.save(baseUser);
            // 设置用户权限
            ArrayList<String> roles = (ArrayList<String>) map.get("roles");
            if (roles != null && roles.size() > 0) {
                List<BaseEcmcSysUserRole> userRoles = getUserRoles(baseUser.getId(), roles);
                ecmcUserRoleDao.save(userRoles);
            }
            EcmcSysUser user = new EcmcSysUser();
            BeanUtils.copyProperties(user, baseUser);
            return user;
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return null;
    }

    @Override
    public void delUser(String userId) throws AppException {
        log.info("删除用户");
        ecmcUserRoleDao.delUserRoleByUserId(userId);
        ecmcUserDao.delete(userId);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public EcmcSysUser updateUser(Map map) throws AppException {
        log.info("更新用户");
        BaseEcmcSysUser baseUser = ecmcUserDao.findOne(MapUtils.getString(map, "id"));

        try {

            if (baseUser != null) {
                String departId = MapUtils.getString(map, "departId");
                baseUser.setDepartId(StringUtil.isEmpty(departId) ? baseUser.getDepartId() : departId);
                String mail = MapUtils.getString(map, "mail");
                baseUser.setMail(StringUtil.isEmpty(mail) ? baseUser.getMail() : mail);
                String name = MapUtils.getString(map, "name");
                baseUser.setName(StringUtil.isEmpty(name) ? baseUser.getName() : name);
                //				String sex = MapUtils.getString(map, "sex");
                //				user.setSex(StringUtil.isEmpty(sex) ? user.getSex() : sex.charAt(0));
                String tel = MapUtils.getString(map, "tel");
                baseUser.setTel(StringUtil.isEmpty(tel) ? baseUser.getTel() : tel);
                Boolean enableFlag = MapUtils.getBoolean(map, "enableFlag");
                baseUser.setEnableFlag(enableFlag == null ? baseUser.isEnableFlag() : enableFlag);
                String password = MapUtils.getString(map, "password");
                if (!StringUtil.isEmpty(password)) {
                    int salt = (int) ((Math.random() * 9 + 1) * 100000);
                    String userSalt = String.valueOf(salt);
                    baseUser.setSalt(userSalt);
                    String saltPassword = AuthUtil.createSaltPassword(password, userSalt);
                    baseUser.setPassword(saltPassword);
                }
                // 更新用户信息
                ecmcUserDao.merge(baseUser);
                // 重新设置用户权限
                ecmcUserRoleDao.delUserRoleByUserId(baseUser.getId());
                ArrayList<String> roles = (ArrayList<String>) map.get("roles");
                if (roles != null && roles.size() > 0) {
                    List<BaseEcmcSysUserRole> userRoles = getUserRoles(baseUser.getId(), roles);
                    ecmcUserRoleDao.save(userRoles);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        EcmcSysUser user = new EcmcSysUser();
        BeanUtils.copyPropertiesByModel(user, baseUser);
        return user;
    }

    @Override
    public boolean checkUserAccount(String userAccount, String id) throws AppException {
        log.info("查询用户Account是否存在");
        BaseEcmcSysUser user = ecmcUserDao.findUserByAccount(userAccount, id);
        return user != null ? false : true;
    }

    @Override
    public List<EcmcSysUser> findUserByDepartmentId(String departmentId) throws AppException {
        log.info("根据部门ID查询用户");
        List<EcmcSysUser> users = new ArrayList<EcmcSysUser>();
        try {
            List<BaseEcmcSysUser> baseUsers = ecmcUserDao.findUserByDepartmentId(departmentId);
            if (baseUsers != null && baseUsers.size() > 0) {
                for (BaseEcmcSysUser baseUser : baseUsers) {
                    EcmcSysUser user = new EcmcSysUser();
                    BeanUtils.copyProperties(user, baseUser);
                    users.add(user);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return users;
    }

    @Override
    public EcmcSysUser findUserById(String userId) throws AppException {
        log.info("根据ID查询用户");
        // 查询用户信息
        BaseEcmcSysUser baseUser = ecmcUserDao.findOne(userId);
        if (baseUser == null) {
            return null;
        }
        EcmcSysUser ecmcUser = new EcmcSysUser();
        BeanUtils.copyPropertiesByModel(ecmcUser, baseUser);
        // 查询用户部门信息
        BaseEcmcSysDepartment baseDepartment = ecmcDepartmentDao.findOne(ecmcUser.getDepartId());
        ecmcUser.setDepartName(baseDepartment.getName());
        // 查询用户权限
        List<String> userIds = new ArrayList<String>();
        userIds.add(ecmcUser.getId());
        List<Map<String, Object>> userRolesMap = ecmcRoleDao.findRolesByUserIds(userIds);
        for (Map<String, Object> roleMap : userRolesMap) {
            EcmcSysRole ecmcRole = new EcmcSysRole();
            BeanUtils.mapToBean(ecmcRole, roleMap);
            ecmcUser.getRoles().add(ecmcRole);
        }
        return ecmcUser;
    }

    /**
     * 生成用户角色对象数组
     * @param userId		用户ID
     * @param roleIds		角色ID数组
     * @return
     */
    public List<BaseEcmcSysUserRole> getUserRoles(String userId, List<String> roleIds) {
        List<BaseEcmcSysUserRole> userRoles = new ArrayList<BaseEcmcSysUserRole>();
        for (String roleId : roleIds) {
            BaseEcmcSysUserRole userRole = new BaseEcmcSysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoles.add(userRole);
        }
        return userRoles;
    }

    @Override
    public EcmcSysUser findUserByName(String userName) throws AppException {
        log.info("根据用户名称查询用户");
        try {
            BaseEcmcSysUser baseUser = ecmcUserDao.findUserByName(userName);
            EcmcSysUser user = new EcmcSysUser();
            BeanUtils.copyProperties(user, baseUser);
            return user;
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Page findPageUserByDepartId(String departId, QueryMap queryMap) throws AppException {
        log.info("根据部门ID查询分页用户");
        ArrayList<String> paramsList = new ArrayList<String>();
        String hql = "select new map(u.id as id, u.account as account, u.name as name, u.tel as tel, u.mail as mail, u.enableFlag as enableFlag, u.departId as departId, d.name as departName, u.sex as sex)" + " from BaseEcmcSysUser as u, BaseEcmcSysDepartment as d where u.departId=d.id and u.departId = ? order by u.createTime desc";
        paramsList.add(departId);
        Page pageUser = ecmcUserDao.pagedQuery(hql, queryMap, paramsList.toArray());
        List<Map<String, Object>> userDataMap = (List<Map<String, Object>>) pageUser.getResult();
        if (userDataMap != null && userDataMap.size() > 0) {
            Map<String, EcmcSysUser> userMap = new LinkedHashMap<String, EcmcSysUser>();
            // 获得用户集合id
            List<String> userIds = new ArrayList<String>();
            for (Map<String, Object> user : userDataMap) {
                EcmcSysUser ecmcUser = new EcmcSysUser();
                BeanUtils.mapToBean(ecmcUser, user);
                userIds.add(ecmcUser.getId());
                userMap.put(ecmcUser.getId(), ecmcUser);
            }
            // 通过id获取角色集合
            List<Map<String, Object>> userRolesMap = ecmcRoleDao.findRolesByUserIds(userIds);
            for (Map<String, Object> roleMap : userRolesMap) {
                EcmcSysRole ecmcRole = new EcmcSysRole();
                BeanUtils.mapToBean(ecmcRole, roleMap);
                userMap.get(MapUtils.getString(roleMap, "userId")).getRoles().add(ecmcRole);
            }
            pageUser.setResult(userMap.values());
        }
        return pageUser;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<EcmcSysUser> findUserByPermission(String userName, String permission) {
        List<EcmcSysUser> userList = new ArrayList<EcmcSysUser>();
        StringBuffer sqlBuffer = new StringBuffer();
        List<String> params = new ArrayList<String>();
        sqlBuffer.append("SELECT DISTINCT u.id, u.account, u.name, u.tel, u.mail, u.depart_id, u.createtime, u.sex ");
        sqlBuffer.append("FROM ecmc_sys_user u ");
        sqlBuffer.append("LEFT JOIN ecmc_sys_userrole ur ON u.id = ur.user_id ");
        sqlBuffer.append("LEFT JOIN ecmc_sys_role r ON ur.role_id = r.id ");
        sqlBuffer.append("LEFT JOIN ecmc_sys_roleauth ra ON r.id = ra.role_id ");
        sqlBuffer.append("LEFT JOIN ecmc_sys_authority a ON ra.auth_id = a.id ");
        sqlBuffer.append("WHERE a.enableflag = 1 ");
        if (!StringUtil.isEmpty(userName)) {
            sqlBuffer.append("AND u.name LIKE ?");
            params.add("%" + userName + "%");
        }
        sqlBuffer.append("AND a.permission LIKE ?");
        params.add("%" + permission + "%");
        List<Object[]> resultList = ecmcUserDao.createSQLNativeQuery(sqlBuffer.toString(), params.toArray()).getResultList();
        if (resultList != null && resultList.size() > 0) {
            for (Object[] objects : resultList) {
                EcmcSysUser user = new EcmcSysUser();
                user.setId(ObjectUtils.toString(objects[0], null));
                user.setAccount(ObjectUtils.toString(objects[1], null));
                user.setName(ObjectUtils.toString(objects[2], null));
                user.setTel(ObjectUtils.toString(objects[3], null));
                user.setMail(ObjectUtils.toString(objects[4], null));
                user.setDepartId(ObjectUtils.toString(objects[5], null));
                user.setCreateTime(DateUtil.stringToDate(ObjectUtils.toString(objects[6], null)));
                user.setSex(ObjectUtils.toString(objects[7], "0").charAt(0));
                userList.add(user);
            }
        }
        return userList;
    }

    @SuppressWarnings("unused")
    private String escapeSpecialChar(String str) {
        if (StringUtils.isNotBlank(str)) {
            String[] specialChars = { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|", "%", "/", ":" };
            for (String key : specialChars) {
                if (str.contains(key)) {
                    str = str.replace(key, "/" + key);
                }
            }
        }
        return str;
    }

    @Override
    public boolean hasUserByRoleId(String roleId) {
        List<BaseEcmcSysUserRole> roleUserList = ecmcUserRoleDao.findUserRoleByRoleId(roleId);
        if (roleUserList != null && roleUserList.size() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public EayunResponseJson changePass(String origPwd, String newPwd, String passKey, EcmcSysUser user) {
        EayunResponseJson reJson = new EayunResponseJson();
        BaseEcmcSysUser baseUser = new BaseEcmcSysUser();
        BeanUtils.copyPropertiesByModel(baseUser, user);
        reJson.setRespCode(ConstantClazz.ERROR_CODE);
        if (!StringUtil.isEmpty(passKey)) {
            if (!StringUtil.isEmpty(origPwd) && !StringUtil.isEmpty(newPwd)) {
                origPwd = DesUtil.strDec(origPwd, passKey, null, null);
                newPwd = DesUtil.strDec(newPwd, passKey, null, null);
                if (baseUser != null && !StringUtil.isEmpty(baseUser.getId())) {
                    baseUser = ecmcUserDao.findOne(baseUser.getId());
                    origPwd = AuthUtil.createSaltPassword(origPwd, baseUser.getSalt());
                    if (StringUtils.equals(origPwd, baseUser.getPassword())) {
                        // 生成密码干扰串
                        int salt = (int) ((Math.random() * 9 + 1) * 100000);
                        String userSalt = String.valueOf(salt);
                        baseUser.setSalt(userSalt);
                        // 用户密码加密
                        String saltPassword = AuthUtil.createSaltPassword(newPwd, userSalt);
                        baseUser.setPassword(saltPassword);
                        // 用户信息入库
                        baseUser = ecmcUserDao.save(baseUser);
                        reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
                        reJson.setMessage("修改密码成功！");
                        reJson.setData(baseUser);
                    } else {
                        reJson.setMessage("旧密码错误！");
                    }
                } else {
                    reJson.setMessage("会话用户为空！");
                }
            } else {
                reJson.setMessage("新密码或者旧密码不能为空！");
            }
        } else {
            reJson.setMessage("页面超时，请重新刷新页面！");
        }
        return reJson;
    }

    @Override
    public EayunResponseJson login(String codenum, String correctNum, String userAccount, String userPasswd, String passKey) {
        EayunResponseJson reJson = new EayunResponseJson();
        reJson.setRespCode(ConstantClazz.ERROR_CODE);
        if (correctNum == null) { // 验证码超时
            reJson.setMessage("验证码已超时，请刷新验证码!");
        } else if ("".equals(codenum)) { // 用户输入的验证码为空
            reJson.setMessage("请输入图片验证码!");
        } else if (!codenum.equalsIgnoreCase(correctNum)) { // 验证码错误
            reJson.setMessage("图片验证码错误!");
        } else { // 验证用户信息
            if (userAccount == null || StringUtils.equals(userAccount, "") || userPasswd == null || StringUtils.equals(userPasswd, "")) { // 用户名或者密码为空
                reJson.setMessage("用户名或密码不能为空！");
            } else { // 验证用户密码正确性
                BaseEcmcSysUser user = ecmcUserDao.findUserByAccount(userAccount);
                if (user == null) { // 未查询到用户
                    reJson.setMessage("用户名或密码不正确！");
                } else if (!user.isEnableFlag()) {
                    reJson.setMessage("该用户已禁用！");
                } else {
                    userPasswd = DesUtil.strDec(userPasswd, passKey, null, null);
                    MD5 md5 = new MD5();
                    String salt = user.getSalt();
                    String pass = md5.getMD5ofStr(userPasswd);
                    if (!StringUtils.equals(md5.getMD5ofStr(pass + salt), user.getPassword())) { // 用户密码错误
                        reJson.setMessage("用户名或密码不正确！");
                    } else if (!hasRoles(user.getId())) {
                        reJson.setMessage("该账户未分配角色！");
                    } else if(!hasAuths(user.getId())){
                        reJson.setMessage("该账户分配了角色，但角色不具备任何权限！");
                    } else { // 登陆成功
                        reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
                        reJson.setMessage("登陆成功！");
                        reJson.setData(user);
                    }
                }
            }
        }
        return reJson;
    }

    protected boolean hasRoles(String userId) {
        if (StringUtils.isBlank(userId)) {
            return false;
        }
        return ecmcUserRoleDao.countByUserId(userId) > 0 ? true : false;
    }

    protected boolean hasAuths(String userId) {
        if(StringUtils.isBlank(userId)){
            return false;
        }
        return ecmcSysRoleAuthDao.countAuthByRoleIds(ecmcUserRoleDao.findRoleIdsByUserId(userId)) > 0 ? true : false;
    }

    public boolean hasUnfinishedWorkOrder(String workHeadUser) throws AppException {
        if (StringUtils.isBlank(workHeadUser)) {
            return false;
        }
        String sql = "select count(wo.workid) from workorder wo where wo.flag < 3 and wo.head_user = ?";
        int count = ((Number) ecmcUserDao.createSQLNativeQuery(sql, new Object[] { workHeadUser }).getSingleResult()).intValue();
        return count > 0 ? true : false;
    }

    /**
	 * 查询ECMC系统下所有“管理员”角色的用户信息
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<EcmcSysUser> findAllAdminUsers() {
		List<EcmcSysUser> userList = new ArrayList<EcmcSysUser>();
        StringBuffer sqlBuffer = new StringBuffer();
        List<String> params = new ArrayList<String>();
        sqlBuffer.append("SELECT DISTINCT u.id, u.account, u.name, u.tel, u.mail, u.depart_id, u.createtime, u.sex ");
        sqlBuffer.append("FROM ecmc_sys_user u ");
        sqlBuffer.append("LEFT JOIN ecmc_sys_userrole ur ON u.id = ur.user_id ");
        sqlBuffer.append("LEFT JOIN ecmc_sys_role r ON ur.role_id = r.id AND r.id = '"+ EcmcRoleIds.ADMIN +"' ");
        sqlBuffer.append("LEFT JOIN ecmc_sys_roleauth ra ON r.id = ra.role_id ");
        sqlBuffer.append("LEFT JOIN ecmc_sys_authority a ON ra.auth_id = a.id ");
        sqlBuffer.append("WHERE a.enableflag = 1 ");
        List<Object[]> resultList = ecmcUserDao.createSQLNativeQuery(sqlBuffer.toString(), 
        		params.toArray()).getResultList();
        if (resultList != null && resultList.size() > 0) {
            for (Object[] objects : resultList) {
                EcmcSysUser user = new EcmcSysUser();
                user.setId(ObjectUtils.toString(objects[0], null));
                user.setAccount(ObjectUtils.toString(objects[1], null));
                user.setName(ObjectUtils.toString(objects[2], null));
                user.setTel(ObjectUtils.toString(objects[3], null));
                user.setMail(ObjectUtils.toString(objects[4], null));
                user.setDepartId(ObjectUtils.toString(objects[5], null));
                user.setCreateTime(DateUtil.stringToDate(ObjectUtils.toString(objects[6], null)));
                user.setSex(ObjectUtils.toString(objects[7], "0").charAt(0));
                userList.add(user);
            }
        }
        return userList;
	}
}