package com.eayun.balancechange.service;

/**
 * 账户余额变动后对后付费资源的处理Service
 *
 * @Filename: PostpayResHandlerService.java
 * @Description:
 * @Version: 1.0
 * @Author: zhangfan
 * @Email: fan.zhang@eayun.com
 * @History:<br> <li>Date: 2016年8月11日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
public interface PostpayResHandlerService {

    /**
     * 找到客户下服务受限的后付费资源，并调用资源处理接口恢复资源使用
     * @param cusId
     */
    void recoverPostPayResource(String cusId);

    /**
     * 找到客户下所有后付费资源，并将其资源的计费状态修改为正常（正常->正常，余额不足->正常）
     * @param cusId
     */
    void modifyResourceStatus(String cusId);
}
