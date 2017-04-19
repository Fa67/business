package com.eayun.common.service;

/**
 * 序列号service
 *                       
 * @Filename: SerialNumService.java
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
public interface SerialNumService {

    /**
     * @param prefix 前缀
     * @param suffixLength 后缀长度
     * @return
     */
    public String getSerialNum(String prefix, int suffixLength);

}
