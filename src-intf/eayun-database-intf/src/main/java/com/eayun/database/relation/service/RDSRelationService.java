package com.eayun.database.relation.service;

import java.util.List;

import com.eayun.database.relation.model.CloudRDSRelation;

/**
 * 
 * @filename: RDSRelationService.java
 * @description:
 * @version: 1.0
 * @author: gaoxiang
 * @email: xiang.gao@eayun.com
 * @history: <br>
 * <li>Date: 2017年2月21日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
public interface RDSRelationService {
    /**
     * 获取关系实体列表
     * @param accountId
     * @param databaseId
     * @return
     */
    public List<CloudRDSRelation> getRDSRelationList(String accountId, String databaseId);
    /**
     * 添加关系记录
     * @param accountId
     * @param dbIdList
     */
    public void addRDSRelations (String accountId, List<String> dbIdList);
    /**
     * 删除关系记录
     * @param accountId
     * @param databaseId
     * @return
     * accountId和databaseId传入都为空或null，返回false
     * 两者都非空或null，删除数据库中的关系数据，返回true
     * 只有一个非空或null，删除数据库中与之相关的多条记录，返回true
     */
    public boolean deleteRDSRelations (String accountId, String databaseId);
}