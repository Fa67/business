package com.eayun.customer.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.customer.model.BasePower;


public interface PowerDao extends IRepository<BasePower, String>{

    @Query(" from BasePower where powerRoute = ? ")
    public List<BasePower> findByRoute(String route);
    
    @Query("select count(powerId) from BasePower where powerRoute = ? and powerId <> ?")
    public int getByRouteAndId(String route , String powerId);
}
