package com.eayun.customer.serivce.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eayun.customer.serivce.DubboInitService;
import com.eayun.mail.service.MailService;
import com.eayun.sms.service.SMSService;

/**
 * Dubbo服务初始化，单例，不提供任何方法
 *                       
 * @Filename: DubboInitServiceImpl.java
 * @Description: 
 * @Version: 1.0
 * @Author: chenhao
 * @Email: hao.chen@eayun.com
 * @History:<br>
 *<li>Date: 2015年11月30日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Service
public class DubboInitServiceImpl implements DubboInitService {

    @Autowired
    private MailService mailService;

    @Autowired
    private SMSService  smsService;
}
