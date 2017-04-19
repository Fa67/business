package com.eayun.work.service;

import java.util.List;

import com.eayun.ecmcuser.model.EcmcSysUser;
import com.eayun.work.model.WorkOpinion;
import com.eayun.work.model.Workorder;

public interface WorkMailService {
    /**
     * 新增工单发送邮件
     * @param workorder
     * @param userId
     */
    public void addWorkSendMail (Workorder workorder,String userId) throws Exception;
    /**
     * 修改工单状态发送邮件
     * @param newWorkorder
     * @param oldWorkorder
     * @param userId
     */
    public void updateWorkorderForFalg(Workorder newWorkorder,Workorder oldWorkorder,String userId) throws Exception;
    /**
     * 投诉发送邮件
     * @param userId
     */
    public void updWorkForFcSendMail(Workorder workorder,String userId) throws Exception;
    /**
     * 新增回复发送邮件
     * @param workorder
     * @param userId
     */
    public void addWorkOpinionSendMail(WorkOpinion workOpinion, Workorder workorder, String userId) throws Exception;
    /**
     * 评价工单
     * @param workorder
     * @param userId
     */
    public void updWorkEcscFalgSendMail(Workorder workorder, String userId) throws Exception;
    /**
     * 获取ecmc对应权限的用户信息
     * @return
     */
    public List<EcmcSysUser> getEcmcUserList(String permission);
    /**
     * 根据id获取ecmc用户信息
     * @param userId
     * @return
     */
    public EcmcSysUser findEcmcUserByUserId(String userId);
    
}
