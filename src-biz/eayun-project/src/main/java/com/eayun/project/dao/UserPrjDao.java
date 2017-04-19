package com.eayun.project.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.customer.model.BaseUserPrj;

public interface UserPrjDao extends IRepository<BaseUserPrj, String> {

    @Query("from BaseUserPrj t where userId=? order by userprjSort ")
    public List<BaseUserPrj> getListByUserId(String userId);
}
