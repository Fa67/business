package com.eayun.database.configgroup.dao;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.dao.IRepository;
import com.eayun.common.exception.AppException;
import com.eayun.database.configgroup.model.datastore.CloudDatastoreVersion;
import org.springframework.stereotype.Repository;

/**
 * Created by Administrator on 2017/2/24.
 */
@Repository
public interface DatastoreVersionDao extends IRepository<CloudDatastoreVersion, String> {
}
