package com.eayun.database.account.service;

public interface CloudDBUserService {

    /**
     * @author gaoxiang
     * @date 2017-02-15
     * @param datacenterId
     * @param projectId
     * @param instanceId
     * @throws Exception
     */
    public void synchData(String datacenterId, String projectId, String instanceId) throws Exception;
}
