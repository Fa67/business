package com.eayun.database.database.service;


public interface CloudDatabaseService {

    /**
     * 同步底层数据中心下的数据库资源
     * @author gaoxiang
     * @date 2017-02-15
     * @param datacenterId
     * @param projectId
     * @param instanceId
     * @throws Exception
     */
    public void synchData(String datacenterId, String projectId, String instanceId) throws Exception;
}
