package com.eayun.database.configgroup.dao;

import com.eayun.common.dao.IRepository;
import com.eayun.database.configgroup.model.datastore.CloudDatastore;
import org.springframework.stereotype.Repository;

/**
 * Created by Administrator on 2017/2/24.
 */
@Repository
public interface DatastoreDao extends IRepository<CloudDatastore, String> {
}
