package com.eayun.project.service;

import java.util.List;

import com.eayun.customer.model.UserPrj;
import com.eayun.virtualization.model.CloudProject;

public interface UserPrjService {
    
    /**
     * 查询用户项目
     * 
     * @param userPrj
     * @return
     */
    public List<UserPrj> getListByUserId(String userId);

    /**
     * 设置用户的项目,全删全增
     * 
     * @param userPrj
     * @return
     */
    public void setUserProjects(String userId, List<String> projectIds);
    /**
     * 删除该用户的所有项目关联记录
     * @param userId
     */
    public void deleteByUser(String userId);

    /**
     * 根据用户ID查询所能查看项目列表
     * @param userId
     * @return
     */
    List<CloudProject> getProjectListByUserId(String userId);
}
