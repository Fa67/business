package com.eayun.monitor.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 报警邮件模板
 *                       
 * @Filename: AlarmMailTemplate.java
 * @Description: 
 * @Version: 1.0
 * @Author: fan.zhang
 * @Email: fan.zhang@eayun.com
 * @History:<br>
 *<li>Date: 2016年1月4日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class AlarmMailTemplate{
    private static final Logger log = LoggerFactory.getLogger(AlarmMailTemplate.class);
    private static Map<String,String> urlMap = null;
    private static StringBuffer mailHtml = null;
    private static AlarmMailTemplate template = null;
    public static InputStream htmlInputStream = null;
    public static InputStream dbInputStream = null;
    
    private AlarmMailTemplate(){
    }
    
    public static AlarmMailTemplate getInstance() {  
        if (template == null) {    
            template = new AlarmMailTemplate();  
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
