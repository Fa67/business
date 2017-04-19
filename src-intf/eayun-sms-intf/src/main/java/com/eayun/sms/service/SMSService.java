package com.eayun.sms.service;

import java.util.List;

public interface SMSService {

    /**
     * 短信发送简单接口
     *
     * @param content
     * @param mobiles
     * @return
     * @throws Exception
     */
    public boolean send(String content, List<String> mobiles) throws Exception;

    /**
     * 短信发送接口-含客户、项目及业务来源信息
     *
     * @param content
     * @param mobiles
     * @param customerID
     * @param projectID
     * @param biz        -- 枚举项，如<code>ConstantClazz.SMS_BIZ_MONITOR</code>
     * @return
     * @throws Exception
     */
    public boolean send(String content, List<String> mobiles, String customerID, String projectID, String biz) throws Exception;

    /**
     * 将短信保存至数据库
     *
     * @param content
     * @param mobiles
     * @param customerID
     * @param projectID
     * @param biz
     * @return
     * @throws Exception
     */
    public boolean save(String content, List<String> mobiles, String status, String customerID, String projectID, String biz) throws Exception;
}
