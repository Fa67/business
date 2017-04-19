package com.eayun.cdn.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.net.util.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.alibaba.fastjson.JSONObject;
import com.eayun.cdn.bean.DNSRecord;
import com.eayun.cdn.util.DNSConstant;
import com.eayun.common.constant.MongoCollectionName;

@Component
public class ALiDNS {
	
	private static final String ISO8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	
	private static final String ENCODING = "UTF-8";
	
	private static final String ALGORITHM = "HmacSHA1";
	
	private static final String HTTP_METHOD = "GET";
	
	private static final String SEPARATOR = "&";
	
	@Autowired
    private MongoTemplate mongoTemplate;

	/*public static void main(String[] args) {
		try {
			DNSRecord record = new DNSRecord();
            record.setDomainName("elbarco.cn");	//域名eayun.com
            record.setSubDomain("wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww.file");		//bucketName.file
            record.setRecordType("CNAME");
            record.setRecordLine("default");
            record.setValue("elbarco.cn");
            record.setTtl(600);
            Map<String, String> parameters = new HashMap<String, String>();
			parameters = getCreateParams(record);
			String url = getUrl(parameters);
			System.out.println(url);
			
			HttpClient c = HttpClients.createDefault();
	        HttpGet get = new HttpGet(url);
	        HttpResponse resp = c.execute(get);
	        HttpEntity respEntity = resp.getEntity();
	        String respData = EntityUtils.toString(respEntity, "UTF-8");
	        System.out.print(respData);
	        System.out.println();
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
	private String getUrl(Map<String, String> parameters) throws Exception {
		// 对参数进行排序，注意严格区分大小写
        String[] sortedKeys = parameters.keySet().toArray(new String[]{});
        Arrays.sort(sortedKeys);
        
        // 生成stringToSign字符串
        StringBuilder stringToSign = new StringBuilder();
        stringToSign.append(HTTP_METHOD).append(SEPARATOR);
        stringToSign.append(percentEncode("/")).append(SEPARATOR);

        StringBuilder canonicalizedQueryString = new StringBuilder();
        for(String key : sortedKeys) {
            // 这里注意对key和value进行编码
            canonicalizedQueryString.append("&")
            .append(percentEncode(key)).append("=")
            .append(percentEncode(parameters.get(key)));
        }
        // 这里注意对canonicalizedQueryString进行编码
        stringToSign.append(percentEncode(canonicalizedQueryString.toString().substring(1)));
        
        
        String sk = DNSConstant.getAccessKeySecret()+"&";

        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(new SecretKeySpec(sk.getBytes(ENCODING), ALGORITHM));
        byte[] signData = mac.doFinal(stringToSign.toString().getBytes(ENCODING));
        //生成签名
        String signature = new String(Base64.encodeBase64(signData));
        
        //将签名也加进参数内重新排序
        parameters.put("Signature", signature);
        String[] sortKeys = parameters.keySet().toArray(new String[]{});
        Arrays.sort(sortKeys);
        StringBuilder queryString = new StringBuilder();
        for(String key : sortKeys) {
        	queryString.append("&")
            .append(percentEncode(key)).append("=")
            .append(percentEncode(parameters.get(key)));
        }
        String url = DNSConstant.ALI_DOMAIN_RECORD + queryString.toString();
		return url;
	}
	//公共参数
	private Map<String, String> getPublicParams(){
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("Version", "2015-01-09");
        parameters.put("AccessKeyId", DNSConstant.getAccessKeyID());
        parameters.put("Timestamp", formatIso8601Date(new Date()));
        parameters.put("SignatureMethod", "HMAC-SHA1");
        parameters.put("SignatureVersion", "1.0");
        parameters.put("SignatureNonce", UUID.randomUUID().toString());
        parameters.put("Format", "JSON");
		return parameters;
	}
	/*添加记录参数*/
    private Map<String, String> getCreateParams(DNSRecord record) {
        Map<String, String> parameters = getPublicParams();
        parameters.put("Action", DNSConstant.ALI_DOMAIN_RECORD_CREATE);
        
        parameters.put("DomainName", record.getDomainName());
        parameters.put("RR", record.getSubDomain());
        parameters.put("Type", record.getRecordType());
        parameters.put("Value", record.getValue());
        parameters.put("TTL", String.valueOf(record.getTtl()));
        parameters.put("Line", record.getRecordLine());
        return parameters;
    }
    /*删除记录参数*/
	private Map<String, String> getRemoveParams(String recordId) {
        Map<String, String> parameters = getPublicParams();
        parameters.put("Action", DNSConstant.ALI_DOMAIN_RECORD_REMOVE);
        
        parameters.put("RecordId", recordId);
        return parameters;
    }
	/*设置记录状态参数*/
	private Map<String, String> getSetStatusParams(String recordId , boolean status) {
	    Map<String, String> parameters = getPublicParams();
	    parameters.put("Action", DNSConstant.ALI_DOMAIN_RECORD_STATUS);
	    
	    parameters.put("RecordId", recordId);
	    if(status){
	    	parameters.put("Status", "Enable");
	    }else{
	    	parameters.put("Status", "Disable");
	    }
	    return parameters;
	}
	/*获取记录列表*/
    @SuppressWarnings("unused")
    private Map<String, String> getRecordsParams(String domainName) {
        Map<String, String> parameters = getPublicParams();
        parameters.put("Action", DNSConstant.ALI_DOMAIN_RECORD_RECORDS);
        
        parameters.put("DomainName", domainName);
        return parameters;
    }
    /*获取记录信息83588446*/
    @SuppressWarnings("unused")
    private Map<String, String> getRecordInfoParams(String RecordId) {
        Map<String, String> parameters = getPublicParams();
        parameters.put("Action", DNSConstant.ALI_DOMAIN_RECORD_RECORDINFO);
        
        parameters.put("RecordId", RecordId);
        return parameters;
    }
    /* 删除主机记录对应的解析记录参数*/
    @SuppressWarnings("unused")
    private Map<String, String> getDeleteSubParams(String domainName,String RR) {
        Map<String, String> parameters = getPublicParams();
        parameters.put("Action", DNSConstant.ALI_DOMAIN_RECORD_DELETESUB);
        
        parameters.put("DomainName", domainName);
        parameters.put("RR", RR);
        return parameters;
    }
    /*获取域名的解析操作日志参数*/
    @SuppressWarnings("unused")
    private Map<String, String> getRecordLogsParams(String domainName) {
        Map<String, String> parameters = getPublicParams();
        parameters.put("Action", DNSConstant.ALI_DOMAIN_RECORD_LOG);
        
        parameters.put("DomainName", domainName);
        return parameters;
    }
    private String formatIso8601Date(Date date) {
        SimpleDateFormat df = new SimpleDateFormat(ISO8601_DATE_FORMAT);
        df.setTimeZone(new SimpleTimeZone(0, "GMT"));
        return df.format(date);
    }
    private String percentEncode(String value) throws UnsupportedEncodingException{
        return value != null ?
                URLEncoder.encode(value, ENCODING).replace("+", "%20")
                .replace("*", "%2A").replace("%7E", "~")
                : null;
    }
    /**
     * 添加解析记录
     * @Author: duanbinbin
     * @param record
     * @return
     * @throws Exception
     *<li>Date: 2016年8月8日</li>
     */
	public String createRecord(DNSRecord record) throws Exception {
		Assert.notNull(record, "记录不能为空");
		Map<String, String> parameters = getCreateParams(record);
		String url = getUrl(parameters);
		
		String respData = doGet(url);
		doLog(record, url, respData);
		return respData;
	}

	/**
	 * 删除解析记录
	 * @Author: duanbinbin
	 * @param recordId
	 * @return
	 * @throws Exception
	 *<li>Date: 2016年8月8日</li>
	 */
	public String removeRecord(String recordId) throws Exception {
		Map<String, String> parameters = getRemoveParams(recordId);
		String url = getUrl(parameters);
		
		String respData = doGet(url);
		return respData;
	}

	/**
	 * 设置解析记录状态
	 * @Author: duanbinbin
	 * @param recordId
	 * @param status
	 * @return
	 * @throws Exception
	 *<li>Date: 2016年8月8日</li>
	 */
	public String setRecordStatus(String recordId, boolean status) throws Exception {
		Map<String, String> parameters = getSetStatusParams(recordId,status);
		String url = getUrl(parameters);
		
		String respData = doGet(url);
		return respData;
	}
	private String doGet(String url) throws IOException {
		HttpClient c = HttpClients.createDefault();
        HttpGet get = new HttpGet(url);
        HttpResponse resp = c.execute(get);
        HttpEntity respEntity = resp.getEntity();
        String respData = EntityUtils.toString(respEntity, "UTF-8");
        System.out.print(respData);
        return respData;
    }
	/**
	 * 添加DNS日志
	 * @Author: duanbinbin
	 * @param record
	 * @param url
	 * @param respData
	 *<li>Date: 2016年8月8日</li>
	 */
	private void doLog(DNSRecord record, String url, String respData) {
        String subDomain = record.getSubDomain();
        String cname = record.getValue();
        String bucketName = subDomain.substring(0, subDomain.indexOf("."));
        String randomBucketName = cname.substring(0, cname.indexOf("."));
        JSONObject resp = JSONObject.parseObject(respData);
        addDNSLog(bucketName, randomBucketName, url, DNSConstant.operationDns.CREATE_RECORD, resp);
    }
	private void addDNSLog(String bucketName, String domainName, String url, Object operation, JSONObject responseBody) {
		JSONObject json = new JSONObject();
		json.put("bucketName", bucketName);
		json.put("domainName", domainName);
		json.put("URL", url);
		json.put("operation", operation);
		json.put("responseBody", responseBody);
		json.put("recordId", responseBody.getString("RecordId"));
		json.put("timestamp", new Date());
		String status = responseBody.getString("Code");
		if (null == status) {
		json.put("status", "1");
		} else {
		json.put("status", "0");
		}
		mongoTemplate.insert(json, MongoCollectionName.LOG_API_DNS);
	}
}
