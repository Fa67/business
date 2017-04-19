package com.eayun.physical.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eayun.common.dao.IRepository;
import com.eayun.physical.model.BaseDcServer;
import com.eayun.physical.model.DcServerModel;

public interface ServerDao extends IRepository<BaseDcServer, String>{

    @Query("from BaseDcServer t where t.datacenterId =:datacenterId")
    public List<BaseDcServer> getListByDataCenter(@Param("datacenterId") String datacenterId);
    
    @Query("from DcServerModel where id<>-1 order by creDate  ")
    public List<DcServerModel> queryByServerModel();
    @Query("from DcServerModel where id = ?")
    public DcServerModel queryByServerModelId(String serverid);
    @Modifying
    @Query("delete from BaseDcServer where id = ?")
    public void delete(String serverid);
    
    @Query("from BaseDcServer where id = ?")
    public BaseDcServer queryById(String serverid);
    
    
    
    
    /**
     * 2016-04-12
     * **/
    
    @Query(" select count(*) from BaseDcServer where datacenterId=?")
    public int getcountserver(String id);
    
    @Query("from BaseDcServer  where name=? and datacenterId = ?")
    public List<BaseDcServer> querybyid(String name,String id);
    
    @Query("from BaseDcServer  where name=? and datacenterId = ? and id != ?")
    public List<BaseDcServer> querybyid(String name,String dcid,String id);
    
}
