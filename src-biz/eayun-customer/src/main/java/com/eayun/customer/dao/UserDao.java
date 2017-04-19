package com.eayun.customer.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.customer.model.BaseUser;

public interface UserDao extends IRepository<BaseUser, String>{

    @Query("select count(*) from BaseUser t where t.userPhone = ?")
    public int getCountByPhone(String newPhone);
    
    @Query("select count(*) from BaseUser t where t.userEmail = ?")
    public int getCountByMail(String newMail);
    
    @Query("from BaseUser where cusId = ? and isAdmin = '1'")
    public BaseUser findAdminByCusId(String cusId);
    @Query("from BaseUser where cusId = ? ")
    public List<BaseUser> getUsersByCusId(String cusId);
}
