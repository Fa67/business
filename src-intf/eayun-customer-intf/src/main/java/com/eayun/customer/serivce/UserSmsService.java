package com.eayun.customer.serivce;

public interface UserSmsService {
    /**
     * 给所填写手机发送短信
     * @param phone
     * @param userId
     */
    public void sendValidSms(String phone , String userId , String type);
    /**
     * 修改邮箱时给用户发送短信
     * @param userId
     */
    public void sendSmsForMail(String userId);
}
