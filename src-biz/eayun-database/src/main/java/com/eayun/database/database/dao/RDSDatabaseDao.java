package com.eayun.database.database.dao;

import org.springframework.stereotype.Repository;

import com.eayun.common.dao.IRepository;
import com.eayun.database.database.model.BaseCloudRDSDatabase;

@Repository
public interface RDSDatabaseDao extends IRepository<BaseCloudRDSDatabase, String> {

}
