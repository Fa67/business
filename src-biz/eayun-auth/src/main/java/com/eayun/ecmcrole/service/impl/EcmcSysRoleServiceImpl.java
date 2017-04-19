package com.eayun.ecmcrole.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.exception.AppException;
import com.eayun.common.util.BeanUtils;
import com.eayun.ecmcauthority.dao.EcmcSysAuthorityDao;
import com.eayun.ecmcauthority.dao.EcmcSysRoleAuthDao;
import com.eayun.ecmcauthority.model.BaseEcmcSysRoleAuth;
import com.eayun.ecmcmenu.dao.EcmcSysMenuDao;
import com.eayun.ecmcmenu.dao.EcmcSysRoleMenuDao;
import com.eayun.ecmcmenu.model.BaseEcmcSysRoleMenu;
import com.eayun.ecmcrole.dao.EcmcSysRoleDao;
import com.eayun.ecmcrole.model.BaseEcmcSysRole;
import com.eayun.ecmcrole.model.EcmcSysRole;
import com.eayun.ecmcrole.service.EcmcSysRoleService;
import com.eayun.ecmcuser.dao.EcmcSysUserRoleDao;
import com.eayun.ecmcuser.model.BaseEcmcSysUser;
import com.eayun.ecmcuser.model.BaseEcmcSysUserRole;
import com.eayun.ecmcuser.util.EcmcSessionUtil;

@Service
@Transactional
public class EcmcSysRoleServiceImpl implements EcmcSysRoleService {

    private static final Logger log = LoggerFactory.getLogger(EcmcSysRoleServiceImpl.class);

    @Autowired
    private EcmcSysRoleDao      ecmcRoleDao;

    @Autowired
    private EcmcSysAuthorityDao ecmcSysAuthorityDao;

    @Autowired
    private EcmcSysMenuDao      ecmcSysMenuDao;

    @Autowired
    private EcmcSysUserRoleDao  ecmcSysUserRoleDao;

    @Autowired
    private EcmcSysRoleMenuDao  ecmcSysRoleMenuDao;

    @Autowired
    private EcmcSysRoleAuthDao  ecmcSysRoleAuthDao;

    @Override
    public EcmcSysRole addRole(EcmcSysRole role) throws AppException {
        log.info("添加角色");
        role.setCreateTime(new Date());
        BaseEcmcSysRole baseRole = new BaseEcmcSysRole();
        try {
            BeanUtils.copyProperties(baseRole, role);
            BaseEcmcSysUser createUser = EcmcSessionUtil.getUser();
            if (createUser != null) {
                baseRole.setCreatedBy(createUser.getId());
            }
            baseRole = ecmcRoleDao.save(baseRole);
            BeanUtils.copyProperties(role, baseRole);
            return role;
        } catch (Exception e) {
            log.error("添加角色失败", e);
        }
        return null;
    }

    @Override
    public void delRole(String roleId) throws AppException {
        log.info("删除角色");
        ecmcRoleDao.delete(roleId);
        //删除角色与用户的关联关系
        ecmcSysUserRoleDao.deleteByRoleId(roleId);
        //同步删除角色与权限的关联关系
        ecmcSysRoleAuthDao.deleteSysRoleAuthByRoleId(roleId);
        //同步删除角色菜单关系
        ecmcSysRoleMenuDao.deleteByRoleId(roleId);
    }

    public boolean hasUserByRoleId(String roleId) {
        if (StringUtils.isEmpty(roleId)) {
            return false;
        }
        return ecmcSysUserRoleDao.countByRoleId(roleId) > 0 ? true : false;
    }

    @Override
    public boolean updateRole(EcmcSysRole role) throws AppException {
        log.info("更新角色");
        try {
            BaseEcmcSysRole baseRole = ecmcRoleDao.findOne(role.getId());
            String name = role.getName();
            baseRole.setName(StringUtils.isEmpty(name) ? baseRole.getName() : name);
            baseRole.setDescription(role.getDescription());
            Boolean enableFlag = role.isEnableFlag();
            baseRole.setEnableFlag(enableFlag == null ? baseRole.isEnableFlag() : enableFlag);
            ecmcRoleDao.saveOrUpdate(baseRole);
            return true;
        } catch (Exception e) {
            log.error("更新角色失败", e);
        }
        return false;
    }

    @Override
    public boolean checkRoleName(String roleName) throws AppException {
        log.info("查询角色名称是否存在");
        BaseEcmcSysRole role = ecmcRoleDao.findRoleByName(roleName);
        return role != null ? true : false;
    }

    @Override
    public EcmcSysRole findRoleById(String roleId) throws AppException {
        log.info("根据ID查询角色");
        //查询角色
        BaseEcmcSysRole baseRole = ecmcRoleDao.findOne(roleId);
        EcmcSysRole role = new EcmcSysRole();
        BeanUtils.copyPropertiesByModel(role, baseRole);
        //根据角色查询菜单
        role.setMenus(ecmcSysMenuDao.findByRoleId(roleId));
        //根据角色查询权限
        role.setAuthorities(ecmcSysAuthorityDao.findByRoleId(roleId));
        return role;
    }

    @Override
    public List<EcmcSysRole> findAllRole() throws AppException {
        log.info("查询所有的角色");
        return baseRolesToSubs(ecmcRoleDao.findAllEcmcSysRole());
    }

    public List<EcmcSysRole> findRoleSelectList() throws AppException {
        log.info("查询角色下拉列表");
        return baseRolesToSubs(ecmcRoleDao.findAllEnableEcmcSysRole());
    }

    /**
     * baseModel转子类
     * @param baseList
     * @return
     * @throws AppException
     */
    protected List<EcmcSysRole> baseRolesToSubs(List<BaseEcmcSysRole> baseList) throws AppException {
        if (CollectionUtils.isEmpty(baseList)) {
            return Collections.<EcmcSysRole> emptyList();
        }
        List<EcmcSysRole> resultList = new ArrayList<EcmcSysRole>(baseList.size());
        for (int i = 0; i < baseList.size(); i++) {
            resultList.add(new EcmcSysRole(baseList.get(i)));
        }
        return resultList;
    }

    public void saveSysRoleMenuAndAuthority(String roleId, List<String> menuIds, List<String> authIds) throws Exception {
        log.info("保存角色菜单和权限");
        ecmcSysRoleMenuDao.deleteByRoleId(roleId);
        ecmcSysRoleAuthDao.deleteSysRoleAuthByRoleId(roleId);
        List<BaseEcmcSysRoleMenu> sysRoleMenus = new ArrayList<BaseEcmcSysRoleMenu>();
        if (menuIds != null && menuIds.size() > 0) {
            for (String menuId : menuIds) {
                BaseEcmcSysRoleMenu roleMenu = new BaseEcmcSysRoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                sysRoleMenus.add(roleMenu);
            }
        }
        saveSysRoleMenu(roleId, sysRoleMenus);

        List<BaseEcmcSysRoleAuth> sysRoleAuths = new ArrayList<BaseEcmcSysRoleAuth>();
        if (authIds != null && authIds.size() > 0) {
            for (String authId : authIds) {
                BaseEcmcSysRoleAuth roleAuth = new BaseEcmcSysRoleAuth();
                roleAuth.setRoleId(roleId);
                roleAuth.setAuthId(authId);
                sysRoleAuths.add(roleAuth);
            }
        }
        saveSysRoleAuth(roleId, sysRoleAuths);
    }

    protected void saveSysRoleMenu(String roleId, List<BaseEcmcSysRoleMenu> sysRoleMenus) throws Exception {
        log.info("保存角色菜单");
        if (StringUtils.isEmpty(roleId)) {
            throw new AppException("error.globe.system");
        }

        if (CollectionUtils.isNotEmpty(sysRoleMenus)) {
            ecmcSysRoleMenuDao.save(sysRoleMenus);
        }
    }

    protected void saveSysRoleAuth(String roleId, List<BaseEcmcSysRoleAuth> sysRoleAuths) throws Exception {
        log.info("修改角色权限");
        if (StringUtils.isEmpty(roleId)) {
            throw new AppException("error.globe.system");
        }

        if (CollectionUtils.isNotEmpty(sysRoleAuths)) {
            ecmcSysRoleAuthDao.save(sysRoleAuths);
        }
    }

    @Override
    public List<EcmcSysRole> findRolesByUserId(String userId) throws AppException {
        log.info("根据用户ID查询角色");
        Iterable<BaseEcmcSysUserRole> userRoleItr = ecmcSysUserRoleDao.findUserRoleByUserId(userId);
        Iterator<BaseEcmcSysUserRole> iterator = userRoleItr.iterator();
        List<EcmcSysRole> userRoles = new ArrayList<EcmcSysRole>();
        try {
            while (iterator.hasNext()) {
                BaseEcmcSysUserRole baseEcmcSysUserRole = iterator.next();
                BaseEcmcSysRole baseEcmcSysRole = ecmcRoleDao.findOne(baseEcmcSysUserRole.getRoleId());
                if(baseEcmcSysRole != null) {
                    EcmcSysRole role = new EcmcSysRole();
                    BeanUtils.copyProperties(role, baseEcmcSysRole);
                    userRoles.add(role);
                }
            }
        } catch (Exception e) {
            throw new AppException("查询用户角色失败", e);
        }
        return userRoles;
    }

    public boolean checkRoleName(String name, String roleId) throws AppException {
        log.info("验证角色名称是否重复");
        if (StringUtils.isEmpty(name)) {
            return false;
        }
        StringBuffer hql = new StringBuffer("select count(r.id) from BaseEcmcSysRole r where r.name = ?");
        List<Object> params = new ArrayList<Object>();
        params.add(name);
        if (StringUtils.isNotBlank(roleId)) {
            hql.append(" and r.id <> ?");
            params.add(roleId);
        }
        return (Long) ecmcRoleDao.findUnique(hql.toString(), params.toArray()) > 0 ? true : false;
    }

}
