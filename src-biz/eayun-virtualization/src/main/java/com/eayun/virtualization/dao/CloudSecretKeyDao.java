package com.eayun.virtualization.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.virtualization.model.BaseCloudSecretKey;

public interface CloudSecretKeyDao extends IRepository<BaseCloudSecretKey, String> {

    @Query("from BaseCloudSecretKey where dcId = ?  and prjId = ? ")
    public List<BaseCloudSecretKey> getBaseCloudSecretKeyList(String dcId,String prjId);
    
    @Query("select count(*) from BaseCloudSecretKey where prjId = ? ")
    int countSecretKeyByPrjId(String prjId);
    
}
