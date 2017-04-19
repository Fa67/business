package com.eayun.cdn.bean;

import java.util.List;

/**
 * @Filename: CDNConfig.java
 * @Description: CDN回源及缓存规则配置
 * @Version: 1.0
 * @Author: fan.zhang
 * @Email: fan.zhang@eayun.com
 * @History:<br>
 * <li>Date: 2016年6月17日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
public class CDNConfig {

    private String bucketName;
    /**加速域名，提供开启CDN加速的bucket使用。e.g: bucketname.file.eayun.com*/
    private String domain;
    /**源站域名（又叫做回源地址）。e.g: bucketname.eos.eayun.com, bucketname.obs.eayun.com:9090*/
    private String origin;
    /**缓存规则列表*/
    private List<CacheRule> cacheRuleList;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public List<CacheRule> getCacheRuleList() {
        return cacheRuleList;
    }

    public void setCacheRuleList(List<CacheRule> cacheRuleList) {
        this.cacheRuleList = cacheRuleList;
    }
}
