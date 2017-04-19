package com.eayun.cdn.bean;

import java.io.Serializable;

/**
 * @Filename: CacheRule.java
 * @Description: CDN缓存规则实体类
 * @Version: 1.0
 * @Author: fan.zhang
 * @Email: fan.zhang@eayun.com
 * @History:<br>
 * <li>Date: 2016年6月17日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
public class CacheRule implements Serializable {

    /**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -728235199372753448L;
    /**资源uri。e.g: /*.jpg, /js/*.js*/
    private String uri;
    /**缓存时间（time to live，存活时间）。e.g: 3600s. 单位：s*/
    private long ttl;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }
}
