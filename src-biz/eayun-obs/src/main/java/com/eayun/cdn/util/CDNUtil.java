package com.eayun.cdn.util;

import com.eayun.cdn.factory.CDNFactory;
import com.eayun.cdn.impl.UpYunCDN;
import com.eayun.cdn.intf.CDN;
import org.springframework.stereotype.Component;

/**
 * @Filename: CDNUtil.java
 * @Description: CDN工具类，用于获取指定厂商CDN实例
 * @Version: 1.0
 * @Author: fan.zhang
 * @Email: fan.zhang@eayun.com
 * @History:<br>
 * <li>Date: 2016年6月17日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
@Component
public class CDNUtil {
    public static CDN getUpYunCDN(){
        CDNFactory cdnFactory = new CDNFactory();
        CDN upYunCDN = cdnFactory.createCDN(UpYunCDN.class);
        return upYunCDN;
    }
}
