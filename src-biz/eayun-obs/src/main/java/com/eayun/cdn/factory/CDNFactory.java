package com.eayun.cdn.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.eayun.cdn.intf.CDN;

/**
 * CDN工厂实现类
 */
public class CDNFactory extends AbstractCDNFactory {
    private static final Logger log = LoggerFactory.getLogger(CDNFactory.class);
    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends CDN> T createCDN(Class<T> c) {
        CDN cdn = null;
        WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
        try {
            //修改为通过Spring获取Bean，并实现自动注入，可以不用在CDN的具体实现中对依赖的jedisUtil和mongoTemplate进行getInstance获取单例，
            //但是对于计划任务来讲，由于无法实现自动注入，所以需要在Job中通过Spring获取指定的CDN实现类的实例，即指定applicationContext.getBean(UpYunCDN.class)
//            cdn = (T)Class.forName(c.getName()).newInstance();
            cdn = context.getBean(c);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return (T)cdn;
    }
}
