package com.eayun.datacenter.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.datacenter.model.BaseDcDataCenter;

public interface DataCenterDao extends IRepository<BaseDcDataCenter, String> {
    
    @Query(" from BaseDcDataCenter t where 1=1")
    public List<BaseDcDataCenter> getAllList();
    
    
    @Query(" from BaseDcDataCenter t where id=? ")
    public BaseDcDataCenter getdatacenterbyid(String id);
    
    
    
    @Query(" from BaseDcDataCenter t where name=?")
    public List<BaseDcDataCenter> checkNameExist(String name);
    
    @Query(" from BaseDcDataCenter t where name=? and id<>?")
    public List<BaseDcDataCenter> checkNameExist(String name,String id);
    
    @Query(" from BaseDcDataCenter t where dcAddress=?")
    public List<BaseDcDataCenter> queryip(String ip);
    
    @Modifying
    @Query( "delete  from BaseDcDataCenter  where id=?")
    public void deletedatacenter(String dataCenterId);
    
    
    @Query("select name from BaseDcDataCenter where id=?")
    public String getdatacenterName(String id);

    @Query("select apiDcCode from BaseDcDataCenter where id=?")
    public String getApiCodeById(String id);

    @Query(" from BaseDcDataCenter t where name=? ")
    public BaseDcDataCenter getdatacenterbyname(String name);
    
    @Query("select provinces from BaseDcDataCenter where id=?")
    public String getProvinces(String id);

}
