package com.eayun.accesskey.service;

public interface AkSmsService {
    /**
     * 给所填写手机发送短信
     * @param phone
     * @param userId
     */
    public void sendValidSms(String phone , String userId);
  
}
