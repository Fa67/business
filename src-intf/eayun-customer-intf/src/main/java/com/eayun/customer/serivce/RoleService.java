package com.eayun.customer.serivce;

import java.util.List;

import com.eayun.customer.model.Role;

public interface RoleService {

    /**
     * 检测指定客户下角色名是否存在
     * 
     * @param customerId
     * @param roleName
     * @param roleId 新增时验证，roleId为null，修改时验证，不为null
     * @return
     */
    public boolean checkRoleName(String customerId, String roleName, String roleId);

    /**
     * 查询角色
     * @param role
     * @return
     */
    public List<Role> getListByCustomer(String customerId);

    /**
     * 根据roleId查询指定角色
     * @param roleId
     * @return
     */
    public Role findRoleById(String roleId);

    /**
     * 添加角色
     * @param role
     * @return
     */
    public Role addRole(Role role);

    /**
     * 修改角色
     * @param role
     * @return
     */
    public Role updateRole(Role role);

    /**
     * 删除角色，先校验是否有用户关联此角色，需要级联删除RolePower
     * 
     * @param role
     * @return
     */
    public void deleteRole(String roleId);
}
