package com.eayun.customer.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.customer.bean.BasePhoneVerify;

public interface PhoneVerifyDao extends IRepository<BasePhoneVerify, String> {

    @Query(" from BasePhoneVerify where userId = ? and phone = ? and is_newphone = ? order by sendTime desc")
    public List<BasePhoneVerify> findByUserAndPh(String userId , String phone , String isNew);
}
