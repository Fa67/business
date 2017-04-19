package com.eayun.database.configgroup.dao;

import com.eayun.common.dao.IRepository;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.database.configgroup.model.configfile.CloudRdsconfigfile;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Administrator on 2017/2/24.
 */
@Repository
public interface RdsconfigfileDao extends IRepository<CloudRdsconfigfile, String> {

    @Query("from CloudRdsconfigfile where configDatacenterid = ?1 and configName = ?2 and configType = '1'")
    public List<CloudRdsconfigfile> queryByDcWithName(String dcId, String fileName) throws Exception ;

    @Query("from CloudRdsconfigfile where((configType = '1') or (configType = '2' and configProjectid = ?1)) and configVersion = ?2 order by configType asc, configDate desc ")
    public List<CloudRdsconfigfile> queryConfigFileByPrjAndVersion(String projectId, String version) throws Exception ;

    @Modifying @Query("update CloudRdsconfigfile set configType = '0' where configId = ?1")
    public int updateDefaultConfigFileState(String configId) throws Exception ;

    @Query("from CloudRdsconfigfile where configDatacenterid = ?1 and configProjectid = ?2 and configName = ?3")
    public List<CloudRdsconfigfile> queryCusSelfConfigFileByFilename(String dcId,String projectId, String fileName) throws Exception ;

}