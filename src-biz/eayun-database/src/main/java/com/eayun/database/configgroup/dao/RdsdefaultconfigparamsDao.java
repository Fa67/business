package com.eayun.database.configgroup.dao;

import com.eayun.common.dao.IRepository;
import com.eayun.common.exception.AppException;
import com.eayun.database.configgroup.model.configfile.CloudRdsconfigfile;
import com.eayun.database.configgroup.model.configfile.CloudRdsdefaultconfigparams;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Administrator on 2017/2/24.
 */
@Repository
public interface RdsdefaultconfigparamsDao extends IRepository<CloudRdsdefaultconfigparams, String> {

    @Query("from CloudRdsdefaultconfigparams where version = ?1 and isStatic = '0'")
    public List<CloudRdsdefaultconfigparams> queryDefaultParamValueByVersionName(String versionName) throws Exception ;

}
