package com.eayun.database.account.dao;

import org.springframework.stereotype.Repository;

import com.eayun.common.dao.IRepository;
import com.eayun.database.account.model.BaseCloudRDSAccount;

@Repository
public interface RDSAccountDao extends IRepository<BaseCloudRDSAccount, String> {

}
