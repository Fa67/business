package com.eayun.work.ecmcservice.impl;

import com.eayun.customer.filter.SystemConfig;

import java.io.*;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class EcmcReadMailHtml{
    
    private static Map<String,String> urlMap = null;
    
    private static StringBuffer ecscMailHtml = null;
    private static StringBuffer ecmcMailHtml = null;
    
    public static Map<String,String> getUrlMap(){
    	if(urlMap==null){
	    	SystemConfig xml = new SystemConfig();
	        urlMap=xml.findNodeMap();
    	}
        return urlMap;
    } 
  /**
   * 加载ecsc的mail页面
   * @return
   */
    public static StringBuffer getEcscMailHtml() throws Exception{
        getUrlMap();
        if(ecscMailHtml==null){
        	initEcscMailHtml();
        }
        return ecscMailHtml;
    }
    public static void initEcscMailHtml() throws Exception{
    	ecscMailHtml= new StringBuffer();
    	String fileUrl = Thread.currentThread().getContextClassLoader().getResource("").toURI().getPath();
    	File file = new File(fileUrl,"mail.html");
		if (!file.exists() || file.isDirectory()){
			throw new FileNotFoundException();
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"utf-8"));
		String line = "";
		String mail="";
		while ((line = br.readLine()) != null) {
			mail += line;
		}
		mail=mail.replace("{imgUrl}", urlMap.get("imgUrl"));
		mail=mail.replace("{ecscUrl}", urlMap.get("ecscUrl"));
		ecscMailHtml.append(mail);
		br.close();
    }
    /**
     * 加载ecmc的mail页面
     * @return
     */
    public static StringBuffer getEcmcMailHtml() throws Exception{
    	getUrlMap();
    	if(ecmcMailHtml==null){
    		initEcmcMailHtml();
    	}
    	return ecmcMailHtml;
    }
    public static void initEcmcMailHtml() throws Exception {
    	ecmcMailHtml= new StringBuffer();
		Resource config = new ClassPathResource("ecmc_mail.html");
		if(config.getInputStream()==null){
			throw new Exception("计划任务邮件模板读取失败。");
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(config.getInputStream(),"utf-8"));
		String line = "";
		String mail="";
		while ((line = br.readLine()) != null) {
			mail += line;
		}
		mail=mail.replace("{imgUrl}", urlMap.get("imgUrl"));
		mail=mail.replace("{ecmcUrl}", urlMap.get("ecmcUrl"));
		ecmcMailHtml.append(mail);
		br.close();
    }
}
