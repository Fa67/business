package com.eayun.accesskey.service.impl;

import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.accesskey.service.ObsService;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.HmacSHA1Util;
import com.eayun.common.util.ObsUtil;
@Service
@Transactional
public class ObsServiceImpl implements ObsService {

	@SuppressWarnings("unused")
    @Override
	public String put(String cusId, String ak,String sk) throws Exception {
//		 	String accessKey  = "8XHJDGE7265UBJP43DIT";
//	        String secretKey   = "jDfwCHMrFsz0IKwnYDm44dUNa0AzoWZU7i7ur7XH";
			String accessKey=ObsUtil.getAdminAccessKey();
			String secretKey=ObsUtil.getAdminSecretKey();
	        String date = DateUtil.getRFC2822Date(new Date());
	        
	        String url = "/admin/user";
	        String signature=ObsUtil.getSignature("PUT", "", "", date, "", url); 
//	        String signature = getSignature("PUT", "", "", date, "", url);
	        
	        String hmacSHA1 = HmacSHA1Util.getEncrypt(signature, secretKey);
	        String host=ObsUtil.getEayunObsHost();
	        String header=ObsUtil.getRequestHeader();
	        HttpClient httpclient = HttpClients.createDefault();
	        String strPut=header+host+url+"?key&uid="+cusId+"&access-key="+ak+"&secret-key="+sk;
	        HttpPut get = new HttpPut(strPut);
	        get.addHeader("Authorization", "AWS "+accessKey+":"+hmacSHA1);
	        get.addHeader("Host", host);
	        get.addHeader("Date", date);
	       
	        HttpResponse res = httpclient.execute(get);

	        int code = res.getStatusLine().getStatusCode();

	        String resData = EntityUtils.toString(res.getEntity(),"utf-8");
        
		return resData;
	}
	
	@Override
	public String delete(String acck) throws Exception {
//	 	String accessKey  = "8XHJDGE7265UBJP43DIT";
//        String secretKey   = "jDfwCHMrFsz0IKwnYDm44dUNa0AzoWZU7i7ur7XH";
		String accessKey=ObsUtil.getAdminAccessKey();
		String secretKey=ObsUtil.getAdminSecretKey();
        String date = DateUtil.getRFC2822Date(new Date());
        
        String url = "/admin/user";
        String signature=ObsUtil.getSignature("DELETE", "", "", date, "", url); 
//        String signature = getSignature("PUT", "", "", date, "", url);
        
        String hmacSHA1 = HmacSHA1Util.getEncrypt(signature, secretKey);
        String host=ObsUtil.getEayunObsHost();
        String header=ObsUtil.getRequestHeader();
        
        HttpClient httpclient = HttpClients.createDefault();
        HttpDelete get = new HttpDelete(header+host+url+"?key&access-key="+acck);
        get.addHeader("Authorization", "AWS "+accessKey+":"+hmacSHA1);
        get.addHeader("Host", host);
        get.addHeader("Date", date);
       
        HttpResponse res = httpclient.execute(get);

        String resData = EntityUtils.toString(res.getEntity(),"utf-8");
        
		return resData;
	}
	
}
