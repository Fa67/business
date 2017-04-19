package com.eayun.work.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Map;

import com.eayun.customer.filter.SystemConfig;


public class ReadMailHtml{

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
    
   
    public static void initEcmcMailHtml()throws Exception{
        ecmcMailHtml= new StringBuffer();
        String fileUrl = Thread.currentThread().getContextClassLoader().getResource("").toURI().getPath();
        File file = new File(fileUrl,"ecmc_mail.html");
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
        mail=mail.replace("{ecmcUrl}", urlMap.get("ecmcUrl"));
        ecmcMailHtml.append(mail);
        br.close();
    }
   
}
