package com.eayun.cdn.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * @Filename: DNSConstant.java
 * @Description: DNS常量类
 * @Version: 1.0
 * @Author: fan.zhang
 * @Email: fan.zhang@eayun.com
 * @History:<br>
 * <li>Date: 2016年6月17日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
public class DNSConstant {
    private static Object lock = new Object();
    public static String LOGIN_TOKEN = "14528,85c43feb6d8978901ab82374898a979a";
    public static String DOMAIN_GRADE = "DP_Free";//在域名信息中可获取，这里因为eayun.com是DP_Free，新免费用户，则声明于此。
    public static String PUBLIC_PARAMS = "login_token=" + LOGIN_TOKEN + "&format=json";

    public static String DOMAIN_CREATE = "https://dnsapi.cn/Domain.Create";
    public static String DOMAIN_LIST = "https://dnsapi.cn/Domain.List";
    public static String DOMAIN_INFO = "https://dnsapi.cn/Domain.Info";
    public static String DOMAIN_ALIAS_LIST = "https://dnsapi.cn/Domainalias.List";

    public static String DOMAIN_RECORD_TYPE = "https://dnsapi.cn/Record.Type";
    public static String DOMAIN_RECORD_LINE = "https://dnsapi.cn/Record.Line";
    public static String DOMAIN_RECORD_LIST = "https://dnsapi.cn/Record.List";
    public static String DOMAIN_RECORD_INFO = "https://dnsapi.cn/Record.Info";
    public static String DOMAIN_RECORD_STATUS = "https://dnsapi.cn/Record.Status/";
    public static String DOMAIN_RECORD_CREATE = "https://dnsapi.cn/Record.Create";
    public static String DOMAIN_RECORD_MODIFY = "https://dnsapi.cn/Record.Modify";
    public static String DOMAIN_RECORD_REMOVE = "https://dnsapi.cn/Record.Remove";

    public static String EAYUN_DOMAIN_NAME = "eayun.com";
    public static String EAYUN_DOMAIN_ID = "14455314";

    //CDN操作类型
    public static enum operationDns{
    	CREATE_RECORD,			//创建记录
    	CONFIG_RECORD,			//设置记录状态
    	DELETE_RECORD,			//删除记录
	}
    
    //阿里
    public static String ALI_DOMAIN_RECORD = "http://alidns.aliyuncs.com/?";
    public static String ALI_DOMAIN_RECORD_CREATE = "AddDomainRecord";
    public static String ALI_DOMAIN_RECORD_STATUS = "SetDomainRecordStatus";
    public static String ALI_DOMAIN_RECORD_REMOVE = "DeleteDomainRecord";
    public static String ALI_DOMAIN_RECORD_RECORDS = "DescribeDomainRecords";
    public static String ALI_DOMAIN_RECORD_RECORDINFO = "DescribeDomainRecordInfo";
    public static String ALI_DOMAIN_RECORD_DELETESUB = "DeleteSubDomainRecords";
    public static String ALI_DOMAIN_RECORD_LOG = "DescribeRecordLogs";
    
    
    private static InputStream dbInputStream = null;
	private static final String fileName = "db.properties";
	private static  Map<String, String> obsNodeMap = null;
	
	public static String getAccessKeyID (){
	    synchronized (lock) {
	        if(null==obsNodeMap||null==obsNodeMap.get("ACCESS_KEY_ID")){
		        obsNodeMap = DNSConstant.findOBSNodeMap();
            }
		}
		return obsNodeMap.get("ACCESS_KEY_ID");
	}
	
	public static String getAccessKeySecret (){
	    synchronized (lock) {
	        if(null==obsNodeMap||null==obsNodeMap.get("ACCESS_KEY_SECRET")){
		        obsNodeMap = DNSConstant.findOBSNodeMap();
		    }
		}
		return obsNodeMap.get("ACCESS_KEY_SECRET");
	}
	private static Map<String, String> findOBSNodeMap() {
	    synchronized (lock) {
	        if(null == obsNodeMap){
		        obsNodeMap = new HashMap<String,String>();
	            Resource re = new ClassPathResource(fileName);
	            try {
	                dbInputStream = re.getInputStream();
	            } catch (IOException e) {
	            }
	            Properties p = new Properties();
	            try {
	                p.load(dbInputStream);
	                obsNodeMap.put("ACCESS_KEY_ID", p.getProperty("alidns.access.key.id"));
	                obsNodeMap.put("ACCESS_KEY_SECRET", p.getProperty("alidns.access.key.secret"));
	            } catch (IOException e1) {
	            }
		    }
		}
		
		return obsNodeMap;
	}
}
