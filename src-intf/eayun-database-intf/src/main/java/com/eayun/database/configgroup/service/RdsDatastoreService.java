package com.eayun.database.configgroup.service;

/**
 * Created by Administrator on 2017/2/24.
 */

import com.amazonaws.services.opsworks.model.App;
import com.eayun.database.configgroup.model.configfile.CloudRdsconfigfile;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.datacenter.model.DcDataCenter;

import java.util.List;

/**
 * 处理 Datastore 数据库版本信息的Service
 */
public interface RdsDatastoreService {

    /**
     * 从Stack底层同步Datastore数据到Cloud数据表中，包括自动生成默认配置文件信息
     * @throws Exception
     */
    public void syncDatastoreDataFromStackToCloud(List<CloudRdsconfigfile> cloudRdsconfigfiles) throws Exception ;

    /**
     * 将提供的静态默认数据库配置参数保存到对应的数据库表中
     * @throws Exception
     */
    public void syncDefaultMySqlParamsFromStaticFileToDatabase() throws Exception ;

    /**
     * 同步Datastore数据
     * @return
     * @throws Exception
     */
    public List<CloudRdsconfigfile> syncDatastoreDatas(BaseDcDataCenter dcDataCenter) throws Exception ;

}
