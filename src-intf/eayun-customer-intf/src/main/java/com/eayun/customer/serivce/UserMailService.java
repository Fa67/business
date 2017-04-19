package com.eayun.customer.serivce;

public interface UserMailService {

    /**
     * 发送验证邮件
     * @param userId
     * @param email
     * @param imgCode
     * @param rightIdCode
     */
    public void sendEmail(String userId, String account , String email , String imgCode , String rightIdCode);
    /**
     * 重新发送验证邮件
     * @param userId
     * @param email
     */
    public void againSendEmail(String userId , String account , String email);
}
