package com.eayun.cdn.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.eayun.common.util.ObsUtil;

/**
 * @Filename: CDNConstant.java
 * @Description: CDN常量类
 * @Version: 1.0
 * @Author: fan.zhang
 * @Email: fan.zhang@eayun.com
 * @History:<br>
 * <li>Date: 2016年6月17日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
public class CDNConstant {
    public static String ACCESS_TOKEN = "0831d3cc-0bd0-446b-80d3-438112faaa5f";
    public static String BUCKET = "https://console.upyun.com/uapi/buckets/";
    public static String BUCKET_INFO = "https://console.upyun.com/uapi/buckets/info/";
    public static String BUCKET_DOMAINS = "https://console.upyun.com/uapi/buckets/domains/";
    public static String BUCKET_VISIBLE = "https://console.upyun.com/uapi/buckets/visible/";

    public static String CDN_SOURCE = "https://api.upyun.com/v2/buckets/cdn/source/";
    public static String CDN_CACHE = "https://api.upyun.com/v2/buckets/cdn/cache/";
    public static String CDN_STATISTICS = "https://api.upyun.com/v2/statistics/";
    public static String CDN_COMMONDATA = "https://api.upyun.com/flow/common_data/";
    public static String CDN_POSTFIX = ".b0.aicdn.com";
    public static String CDN_CERTIFICATE="https://console.upyun.com/uapi/https/certificate/manager/";

    public static String CDN_PURGE = "https://api.upyun.com/purge/";
    
    public static int CDN_ERROR_COUNT = 3;
    
    public static String CDN_SYSERROR_CODE = "21000";
    private static Object lock = new Object();
    
	private static InputStream dbInputStream = null;
	private static final String fileName = "db.properties";
	private static  Map<String, String> cdnMap = null;
	
	private static final Logger log = LoggerFactory.getLogger(CDNConstant.class);
    
  //CDN服务提供商
    public enum cdnProvider{
		UpYun,
		ChinaNetCenter
	}
    //CDN操作类型
    public enum operationCdn{
    	CREATE_BUCKETS,			//创建空间
    	CONFIG_SOURCE,			//配置回源
    	CONFIG_CACHE,			//配置缓存规则
    	BIND_DOMAIN,			//绑定加速域名
    	QUERY_BANDWIDTH,		//查询流量
    	REFRESH_CACHE,			//刷新缓存
    	CONFIG_VISIBLE,			//设置空间外链状态
    	QUERY_BACKSOURCE,		//回源流量
    	OPEN_HTTPS				//开启https
	}
    public static String getCdnCertificateId(){
    	if(cdnMap==null||cdnMap.get("cdnCertificateId")==null){
    		 synchronized (lock) {
    	 	        if(cdnMap==null||cdnMap.get("cdnCertificateId")==null){
    	 	        	cdnMap = CDNConstant.findCdnMap();
    	             }
    	 		}
    	}
    	
 		return cdnMap.get("cdnCertificateId");
    }
	//添加eayunObsHost到配置文件
	private static Map<String, String> findCdnMap() {
	    synchronized (lock) {
	        if(null == cdnMap){
	        	cdnMap = new HashMap<String,String>();
                Resource re = new ClassPathResource(fileName);
                try {
                    dbInputStream = re.getInputStream();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
                Properties p = new Properties();
                try {
                    p.load(dbInputStream);
                    cdnMap.put("cdnCertificateId", p.getProperty("cdn.certificate.id"));
                } catch (IOException e1) {
                    log.error(e1.getMessage(), e1);
                }
            
            }
		}
		
		return cdnMap;
	}
}