package com.eayun.monitor.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * ECMC邮件模板
 *                       
 * @Filename: EcmcAlarmMailTemplate.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2016年5月10日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class ApiAlarmMailTemplate {
    private static final Logger log = LoggerFactory.getLogger(ApiAlarmMailTemplate.class);

	private static Map<String,String> urlMap = null;
    private static StringBuffer mailHtml = null;
    private static ApiAlarmMailTemplate template = null;
    public static InputStream htmlInputStream = null;
    public static InputStream dbInputStream = null;

    private ApiAlarmMailTemplate(){
    }
    
    public static ApiAlarmMailTemplate getInstance() {
        if (template == null) {    
            template = new ApiAlarmMailTemplate();
        }    
       return template;  
    }  
    
    /**
     * 获取报警邮件内容
     * @return
     */
    public StringBuffer getMailHtml() {
        getUrlMap();
        if(mailHtml== null){
            initMailHtml();
        }
        return mailHtml;
    }
    
    private void getUrlMap() {
        if(urlMap==null){
            urlMap = findNodeMap();
        }
    }
    
    public Map<String, String> findNodeMap() {
        Properties p  =   new  Properties();
        Map<String,String> map = new HashMap<String,String>();
        try {
            p.load(dbInputStream);
            map.put("imgUrl",  p.getProperty("imgUrl"));
            map.put("ecscUrl", p.getProperty("ecscUrl"));
            map.put("ecmcUrl", p.getProperty("ecmcUrl"));
        } catch (IOException e1) {
            log.error(e1.getMessage(),e1);
        }
        return map;
    }

    private void initMailHtml(){
        mailHtml= new StringBuffer();
        try {  
            if(htmlInputStream==null){
                throw new NullPointerException();
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(htmlInputStream,"utf-8"));  
            String line = ""; 
            String mail="";
            while ((line = br.readLine()) != null) {  
                mail += line; 
            }
            mail=mail.replace("{imgUrl}", urlMap.get("imgUrl"));
            mail=mail.replace("{ecscUrl}", urlMap.get("ecscUrl"));
            mailHtml.append(mail);
            br.close(); 
        } catch (IOException ie) {  
            log.error(ie.getMessage(),ie); 
        }  
    }
}
