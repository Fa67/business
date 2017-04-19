package com.eayun.cdn.intf;

import java.util.Date;

import com.eayun.cdn.bean.CDNConfig;

/**
 * CDN接口
 */
public interface CDN {
    String createDomain(CDNConfig config);

    /**
     * 适用UpYun的删除加速域名
     * @param domainId
     * @param domain
     * @return
     */
    String deleteDomain(String domainId, String domain);

    String enableDomain(String domainId);

    String disableDomain(String domainId);

    String getDomainConfiguration(String domainId);

    String getStatistics(String domainId, Date from, Date to) ;

    String purgeFiles(String[] fileUrls);
    
    String getBackSource(String domainId,Date from , Date to) throws Exception;
}
