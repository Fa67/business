package com.eayun.common.service;

import com.eayun.common.model.IpInfo;

/**
 * 获得ip信息，通过调用taobao的api，结果缓存在redis中，超时1天
 *                       
 * @Filename: IpService.java
 * @Description: 
 * @Version: 1.0
 * @Author: chenhao
 * @Email: hao.chen@eayun.com
 * @History:<br>
 *<li>Date: 2016年8月5日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public interface IpService {
    
    /**
     * 得到Ip信息
     * 
     * @param ip
     * @return
     */
    public IpInfo getIp(String ip);

}
