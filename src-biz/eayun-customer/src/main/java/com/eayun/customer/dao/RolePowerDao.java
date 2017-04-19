package com.eayun.customer.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.customer.model.BaseRolePower;

public interface RolePowerDao extends IRepository<BaseRolePower, String>{
    
    @Query("from BaseRolePower t where roleId=? ")
    public List<BaseRolePower> getListByRoleId(String roleId);
    
    @Query("from BaseRolePower t where roleId = ? and powerId = ?")
    public List<BaseRolePower> getByRolePower(String roleId , String powerId);

}
