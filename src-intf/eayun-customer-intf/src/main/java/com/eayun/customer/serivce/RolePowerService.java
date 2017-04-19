package com.eayun.customer.serivce;

import java.util.List;

import com.eayun.customer.model.RolePower;

public interface RolePowerService {
    /**
     * 查询角色权限
     * @param rolePower
     * @return
     */
    public List<RolePower> getListByRole(String roleId);


    /**
     * 设置角色权限,全删全增
     * 
     * @param rolePower
     * @return
     */
    public void setRolePower(String roleId,List<String> powerIds);
    /**
     * 删除一个角色的所有关联记录
     * @param roleId
     */
    public void deleteByRole(String roleId);
    /**
     * 删除一项功能权限的所有关联记录
     * @param powerId
     */
    public void deleteByPower(String powerId);
}
