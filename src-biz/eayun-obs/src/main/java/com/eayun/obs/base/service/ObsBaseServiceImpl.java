package com.eayun.obs.base.service;

import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.httpclient.HttpClientFactory;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.HmacSHA1Util;
import com.eayun.common.util.ObsUtil;
import com.eayun.obs.model.ObsAccessBean;
import com.eayun.obs.model.ObsResultBean;
@Service
@Transactional
public class ObsBaseServiceImpl implements ObsBaseService {
	 private static final Logger log = LoggerFactory.getLogger(ObsBaseServiceImpl.class);
	/**
	 * 对象存储rest请求的获取列表方法
	 * @param ObsAccessBean
	 * @return json
	 * @throws Exception
	 */
	public ObsResultBean get(ObsAccessBean obsBean) throws Exception {
       HttpClient httpclient = HttpClientFactory.getHttpClient(obsBean.isHttp());
       HttpGet get = new HttpGet(obsBean.getUrl());
       log.info("getMethod the current URL is:" + obsBean.getUrl());
       get.addHeader("Authorization", "AWS "+obsBean.getAccessKey()+":"+obsBean.getHmacSHA1());
       get.addHeader("Host", obsBean.getHost());
       get.addHeader("Date", obsBean.getRFC2822Date());
       log.info("getMethod Authorization: AWS "+obsBean.getAccessKey()+":"+obsBean.getHmacSHA1());
       log.info("getMethod Host: "+obsBean.getHost());
       log.info("getMethod Date: "+obsBean.getRFC2822Date());
       HttpResponse res = httpclient.execute(get);
       log.info("getMethod HttpResponse=" + res);
       int code = res.getStatusLine().getStatusCode();
       
       String resData = EntityUtils.toString(res.getEntity(),"utf-8");
       JSONObject resultJson = new JSONObject();
       ObsResultBean resultBean = new ObsResultBean();
       resultBean.setCode(code+"");
       resultBean.setResData(resData);
       resultJson.put("code", code);
       resultJson.put("resData", resData);
       log.info("getMethod The response Data=" + resultJson.toJSONString());
	return resultBean;
	}
	
	/**
	 * 对象存储rest请求的创建方法
	 * @param ObsAccessBean
	 * @return json
	 * @throws Exception
	 */
	public JSONObject put(ObsAccessBean obsBean) throws Exception {
		HttpClient httpclient = HttpClientFactory.getHttpClient(obsBean.isHttp());
		HttpPut put = new HttpPut(obsBean.getUrl());
		log.info("putMethod The current URL is:" + obsBean.getUrl());
		put.addHeader("Authorization", "AWS " + obsBean.getAccessKey() + ":" + obsBean.getHmacSHA1());
		put.addHeader("Host", obsBean.getPutHeaderHost());
		put.addHeader("Date", obsBean.getRFC2822Date());
		log.info("putMethod Authorization: AWS "+obsBean.getAccessKey()+":"+obsBean.getHmacSHA1());
	    log.info("putMethod Host: "+obsBean.getHost());
	    log.info("putMethod Date: "+obsBean.getRFC2822Date());
        HttpResponse res = httpclient.execute(put);
        log.info("putMethod HttpResponse=" + res);
        int code = res.getStatusLine().getStatusCode();
        String resData = EntityUtils.toString(res.getEntity(),"utf-8");
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("code", code);
        jsonObj.put("resData", resData);
        log.info("putMethod The response Data=" + jsonObj.toJSONString());
		return jsonObj;
	}
	
	/**
	 * 删除   例:bucket 
	 * @param 
	 * @param 
	 * @param 
	 * @return json
	 * @throws Exception
	 */
	public JSONObject deleteBucket(ObsAccessBean obsBean) throws Exception {
		HttpClient httpclient = HttpClientFactory.getHttpClient(obsBean.isHttp());
		HttpDelete delete = new HttpDelete(obsBean.getUrl());
		log.info("deleteBucketMehtod The current URL is:" + obsBean.getUrl());
		delete.addHeader("Authorization", "AWS " + obsBean.getAccessKey() + ":" + obsBean.getHmacSHA1());
		delete.addHeader("Host", obsBean.getHost());
		delete.addHeader("Date", obsBean.getRFC2822Date());
		delete.setHeader("Content-Type", "application/octet-stream");
		log.info("deleteBucketMehtod Authorization: AWS "+obsBean.getAccessKey()+":"+obsBean.getHmacSHA1());
	    log.info("deleteBucketMehtod Host: "+obsBean.getHost());
	    log.info("deleteBucketMehtod Date: "+obsBean.getRFC2822Date());
	    log.info("deleteBucketMehtod Content-Type: "+obsBean.getRFC2822Date());
	    
		HttpResponse res = httpclient.execute(delete);
		log.info("deleteBucketMehtod HttpResponse=" + res);
		int code = res.getStatusLine().getStatusCode();

		JSONObject resJson = new JSONObject();
		HttpEntity entity = res.getEntity();
		String resData = null;
		if (null == entity) {
			resJson.put("resData", entity);
		} else {
			resData = EntityUtils.toString(res.getEntity());
		}
		resJson.put("code", code);
		resJson.put("resData", resData);
		log.info("deleteBucketMehtod The response Data=" + resJson.toJSONString());
		return resJson;
	}
	
	public String deleteAccessKey(ObsAccessBean obsBean) throws Exception{
		HttpClient httpclient = HttpClientFactory.getHttpClient(obsBean.isHttp());
		HttpDelete delete = new HttpDelete(obsBean.getUrl());
		log.info("deleteAccessKeyMethod The current URL is:" + obsBean.getUrl());
		delete.addHeader("Authorization", "AWS " + obsBean.getAccessKey() + ":" + obsBean.getHmacSHA1());
		delete.addHeader("Host", obsBean.getHost());
		delete.addHeader("Date", obsBean.getRFC2822Date());
		log.info("deleteAccessKeyMethod Authorization: AWS "+obsBean.getAccessKey()+":"+obsBean.getHmacSHA1());
	    log.info("deleteAccessKeyMethod Host: "+obsBean.getHost());
	    log.info("deleteAccessKeyMethod Date: "+obsBean.getRFC2822Date());
        HttpResponse res = httpclient.execute(delete);
        String resData = EntityUtils.toString(res.getEntity(),"utf-8");
        log.info("deleteAccessKeyMethod The response Data=" + resData);
        return resData;
	}
	@Override
	public void setBucketCORS(String accessKey ,String secretKey ,String bucketName) throws Exception {
		String str = new String("<CORSConfiguration><CORSRule><AllowedMethod>POST</AllowedMethod><AllowedMethod>GET</AllowedMethod><AllowedMethod>HEAD</AllowedMethod><AllowedMethod>PUT</AllowedMethod><AllowedMethod>DELETE</AllowedMethod><AllowedOrigin>*</AllowedOrigin><AllowedHeader>authorization</AllowedHeader><AllowedHeader>content-type</AllowedHeader><AllowedHeader>x-amz-date</AllowedHeader><AllowedHeader>x-amz-user-agent</AllowedHeader><MaxAgeSeconds>1</MaxAgeSeconds></CORSRule></CORSConfiguration>");
        String date = DateUtil.getRFC2822Date(new Date());
        String header=ObsUtil.getRequestHeader();
        String host=ObsUtil.getEayunObsHost();
        String url = header + bucketName + "."+host+"/?cors";
        String signature = ObsUtil.getSignature("PUT", "", "application/xml", date, "/" + bucketName + "/?cors", "");
		
        String hmacSHA1 = HmacSHA1Util.getEncrypt(signature, secretKey);
        
        HttpClient httpclient = HttpClientFactory.getHttpClient("http://".equals(header));
        
        HttpPut put = new HttpPut(url);
        log.info("setBucketCORSMehtod The current URL is: "+url);
        put.addHeader("Authorization", "AWS "+accessKey+":"+hmacSHA1);
        put.addHeader("Host", bucketName + "."+host);
        put.addHeader("Date", date);
        put.setHeader("Content-Type", "application/xml");
        log.info("setBucketCORSMehtod Authorization: AWS "+accessKey+":"+hmacSHA1);
        log.info("setBucketCORSMehtod Host: "+bucketName + "."+host);
        log.info("setBucketCORSMehtod Date: "+date);
        log.info("setBucketCORSMehtod Content-Type: application/xml");
        HttpEntity entity = new StringEntity(str,ContentType.APPLICATION_XML);
        put.setEntity(entity);
        HttpResponse res = httpclient.execute(put);
        log.info("setBucketCORSMehtod code:" + res.getStatusLine().getStatusCode());
        log.info("setBucketCORSMehtod put:" + put.getEntity().toString());
        log.info("setBucketCORSMehtod res:" + res);
        log.info("setBucketCORSMehtod end");
	}
}
