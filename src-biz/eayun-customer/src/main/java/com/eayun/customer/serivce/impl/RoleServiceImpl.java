package com.eayun.customer.serivce.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.exception.AppException;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.StringUtil;
import com.eayun.customer.dao.RoleDao;
import com.eayun.customer.model.BaseRole;
import com.eayun.customer.model.Role;
import com.eayun.customer.serivce.PowerService;
import com.eayun.customer.serivce.RolePowerService;
import com.eayun.customer.serivce.RoleService;
import com.eayun.customer.serivce.UserService;

@Service
@Transactional
@SuppressWarnings("unchecked")
public class RoleServiceImpl implements RoleService {
    private static final Logger log = LoggerFactory.getLogger(RoleServiceImpl.class);

    @Autowired
    private RoleDao             roleDao;
    @Autowired
    private UserService         userService;
    @Autowired
    private RolePowerService rolePowerService;
    @Autowired
    private PowerService powerService;
    @Override
    public List<Role> getListByCustomer(String customerId) {
        log.info("得到指定客户下角色列表");

        StringBuffer strb = new StringBuffer();
        strb.append("from BaseRole where cusId = ? order by roleName");
        List<BaseRole> baseRoleList = roleDao.find(strb.toString(), customerId);

        List<Role> roleList = new ArrayList<Role>();
        for (BaseRole baseRole : baseRoleList) {
            Role sysrole = new Role();
            BeanUtils.copyPropertiesByModel(sysrole, baseRole);
            roleList.add(sysrole);
        }
        return roleList;
    }

    @Override
    public Role findRoleById(String roleId) {
        log.info("根据id得到角色");

        BaseRole baseRole = roleDao.findOne(roleId);
        Role role = new Role();
        BeanUtils.copyPropertiesByModel(role, baseRole);
        return role;
    }

    @Override
    public Role addRole(Role role) {
        log.info("添加角色");

        BaseRole baseRole = new BaseRole();
        BeanUtils.copyPropertiesByModel(baseRole, role);
        roleDao.saveEntity(baseRole);
        BeanUtils.copyPropertiesByModel(role, baseRole);
        /** 此处业务逻辑及数据已更改：2015-12-03*/
        /*List<Power> powerlist = powerService.getDefaultList();//添加默认权限
        List<String> powerIds = new ArrayList<String>();
        for(Power power : powerlist){
            powerIds.add(power.getPowerId());
        }
        rolePowerService.setRolePower(role.getRoleId(), powerIds);*/
        return role;
    }

    @Override
    public Role updateRole(Role role) {
        log.info("修改角色");

        BaseRole baseRole = new BaseRole();
        BeanUtils.copyPropertiesByModel(baseRole, role);
        roleDao.saveOrUpdate(baseRole);
        BeanUtils.copyPropertiesByModel(role, baseRole);
        return role;
    }

    @Override
    public void deleteRole(String roleId) {
        log.info("删除角色");
        long count = userService.getCountByRole(roleId);
        if (count > 0) {
            throw new AppException("有" + count + "个用户关联了此角色，不允许删除！");
        }
        rolePowerService.deleteByRole(roleId);

        StringBuffer sb = new StringBuffer();
        sb.append("delete BaseRole where roleId = ?");
        roleDao.executeUpdate(sb.toString(), roleId);
    }

    @Override
    public boolean checkRoleName(String customerId, String roleName, String roleId) {
        log.info("验证角色名重复");

        StringBuffer sb = new StringBuffer();
        sb.append(" from BaseRole where cusId = ?  and roleName = ? ");
        List<String> param = new ArrayList<String>();
        param.add(customerId);
        param.add(roleName);
        if (!StringUtil.isEmpty(roleId)) {
            sb.append(" and roleId <> ? ");
            param.add(roleId);
        }

        List<BaseRole> role = roleDao.find(sb.toString(), param.toArray());
        if (role.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }
}
