package com.eayun.database.configgroup.dao;

import com.eayun.common.dao.IRepository;
import com.eayun.common.exception.AppException;
import com.eayun.database.configgroup.model.configfile.CloudRdsconfigparams;
import com.eayun.database.configgroup.model.configfile.CloudRdsdefaultconfigparams;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Administrator on 2017/2/24.
 */
@Repository
public interface RdsconfigparamsDao extends IRepository<CloudRdsconfigparams, String> {

}
