package com.eayun.project.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.util.BeanUtils;
import com.eayun.customer.model.BaseUserPrj;
import com.eayun.customer.model.User;
import com.eayun.customer.model.UserPrj;
import com.eayun.customer.serivce.UserService;
import com.eayun.project.dao.UserPrjDao;
import com.eayun.project.service.ProjectService;
import com.eayun.project.service.UserPrjService;
import com.eayun.virtualization.model.CloudProject;

@Service
@Transactional
public class UserPrjServiceImpl implements UserPrjService {
    private static final Logger log = LoggerFactory.getLogger(UserPrjServiceImpl.class);
    
    @Autowired
    private UserPrjDao userPrjDao;
    
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserService userService;

    @SuppressWarnings("unchecked")
    @Override
    public List<UserPrj> getListByUserId(String userId) {
        log.info("得到指定用户下的项目列表");
        
        StringBuffer strb = new StringBuffer();
        strb.append("from BaseUserPrj where userId = ?");

        List<BaseUserPrj> BaseUpList = userPrjDao.find(strb.toString(), userId);
        List<UserPrj> upList = new ArrayList<UserPrj>();
        for (BaseUserPrj baseRp : BaseUpList) {
            UserPrj up = new UserPrj();
            BeanUtils.copyPropertiesByModel(up, baseRp);
            upList.add(up);
        }
        return upList;
    }

    @Override
    public void setUserProjects(String userId, List<String> projectIds) {
        log.info("给用户设置项目");
        
        StringBuffer sb = new StringBuffer();
        sb.append("delete BaseUserPrj where userId = ?");
        userPrjDao.executeUpdate(sb.toString(), userId);

        for (int i = 0; i < projectIds.size(); i++) {
            String projectId = projectIds.get(i);
            BaseUserPrj userPrj = new BaseUserPrj();
            userPrj.setUserId(userId);
            userPrj.setProjectId(projectId);
            userPrj.setUserprjSort(0);
            userPrjDao.saveEntity(userPrj);
        }
    }

    @Override
    public void deleteByUser(String userId) {
        log.info("删除用户与项目的关联");
        StringBuffer sb = new StringBuffer();
        sb.append("delete BaseUserPrj where userId = ?");
        userPrjDao.executeUpdate(sb.toString(), userId);
    }
    
    @Override
    public List<CloudProject> getProjectListByUserId(String userId) {
        log.info("根据用户ID查询所能查看项目列表");
        
        List<CloudProject> CloudProject = new ArrayList<CloudProject>();
        User user = userService.findUserById(userId);
        if(user.getIsAdmin()){
            CloudProject = projectService.getProjectListByCustomer(user.getCusId());
        }else{
            CloudProject = projectService.getProjectListByUser(user.getCusId(),userId);
        }
        return CloudProject;
    }
}
