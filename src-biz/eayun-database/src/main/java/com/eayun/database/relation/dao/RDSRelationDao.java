package com.eayun.database.relation.dao;

import org.springframework.stereotype.Repository;

import com.eayun.common.dao.IRepository;
import com.eayun.database.relation.model.BaseCloudRDSRelation;

@Repository
public interface RDSRelationDao extends IRepository<BaseCloudRDSRelation, String> {

}
