package com.eayun.customer.serivce;

import com.eayun.customer.bean.MailVerify;

public interface MailVerifyService {

    /**
     * 添加邮箱验证记录
     * 用户点击邮箱验证发送邮件时启动
     * @param mailVerify
     * @return
     */
    public MailVerify addMailVerify(MailVerify mailVerify);
    /**
     * 根据ID查询邮箱验证记录
     * @param verifyId
     * @return
     */
    public MailVerify findById(String verifyId);
    /**
     * 点击验证通过时返回修改邮箱验证记录
     * @param mailVerify
     * @return
     */
    public void updateByVerify(MailVerify mailVerify);
    
}
