package com.eayun.cdn.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.eayun.cdn.bean.CDNConfig;
import com.eayun.cdn.bean.CacheRule;
import com.eayun.cdn.bean.DNSRecord;
import com.eayun.cdn.intf.CDN;
import com.eayun.cdn.util.CDNConstant;
import com.eayun.cdn.util.DNSConstant;
import com.eayun.cdn.util.DNSUtil;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.ObsUtil;

/**
 * @Filename: UpYunCDN.java
 * @Description: UpYunCDN实例
 * @Version: 1.0
 * @Author: fan.zhang
 * @Email: fan.zhang@eayun.com
 * @History:<br> <li>Date: 2016年6月17日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
@Component
public class UpYunCDN implements CDN {
    private static final Logger log = LoggerFactory.getLogger(UpYunCDN.class);
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private JedisUtil jedisUtil;

    private static String ACCESS_TOKEN;

    /**
     * 创建CDN加速域名，并完成基本的回源和缓存规则配置
     *
     * @param config
     * @return
     */
    public String createDomain(CDNConfig config) {
        //如果入参CDN配置不合理，直接返回错误提示信息，结束
        if (config == null
                || config.getBucketName() == null
                || config.getOrigin() == null
                || config.getCacheRuleList() == null
                || config.getCacheRuleList().isEmpty()) {
            JSONObject json = new JSONObject();
            json.put("result", "false");
            json.put("message", "Invalid CDN configuration");
            return json.toJSONString();
        }

        JSONObject respData = new JSONObject();
        try {
            //1. 在UpYun平台创建一个bucket
            String bucketName = config.getBucketName();
            JSONObject c_result = createBucket(bucketName);
            if (!c_result.getBoolean("result")) {
                return c_result.toJSONString();
            }
            //2. 得到创建后的bucketname，为其配置回源地址
            String randomBucketName = c_result.getString("randomBucketName");
            JSONObject conf_src_result = configSource(config, randomBucketName);
            if (!conf_src_result.getBoolean("result")) {
                return conf_src_result.toJSONString();
            }
            String cdnCNAME = randomBucketName + CDNConstant.CDN_POSTFIX;

            //3. 配置缓存规则
            JSONObject conf_cache_result = configCache(config, randomBucketName);
            if (!conf_cache_result.getBoolean("result")) {
                return conf_cache_result.toJSONString();
            }
            //4. 为bucket绑定域名
            String domain = config.getDomain();
            int fixedLength = ".eayun.com".length();
            String subDomain = domain.substring(0,domain.length()-fixedLength);
            JSONObject bind_result = bindDomain(bucketName, randomBucketName, domain);
            if (!bind_result.getBoolean("result")) {
                return bind_result.toJSONString();
            }
            //5. 如果为https协议,则开启https访问
            if("https".equals(getRequestHeader())){
            	JSONObject result=new JSONObject();
            	for(int i= 0;i<1200;i++){//循环100次调开启https接口,确保域名审核通过并开启https成功,若100后仍开启失败,则本次开通cdn操作失败
            		result =configHttps(config,randomBucketName);
            		if (result.getBoolean("result")) {
            			break;
            		}
            		Thread.sleep(500);//每次循环等待500ms
            	}
            	if(!result.getBoolean("result")){
            		String rollbackMessage = rollbackCreating(bucketName, randomBucketName,domain);
            		result.put("result", "false");
            		return result.toJSONString();
            	}
            }
            //6. 调用DNS解析添加一条cname记录
            DNSRecord record = new DNSRecord();
            record.setDomainName(DNSConstant.EAYUN_DOMAIN_NAME);	//域名eayun.com
            record.setSubDomain(subDomain);		//bucketName.file
            record.setRecordType("CNAME");
            record.setRecordLine("default");
            record.setValue(cdnCNAME);
            record.setTtl(600);
            ALiDNS dns = DNSUtil.getALiDNS();
            String tmp = dns.createRecord(record);
            JSONObject c_record_result = JSONObject.parseObject(tmp);
            String recordId;
            if (null == c_record_result.getString("Code")) {
                recordId = c_record_result.getString("RecordId");
            } else {
                String rollbackMessage = rollbackCreating(bucketName, randomBucketName,domain);
                c_record_result.put("result", "false");
                c_record_result.put("message", "Fail in creating dns cname record.\n" + rollbackMessage);
                return c_record_result.toJSONString();
            }
          
            respData.put("result", "true");
            respData.put("message", "Succeed in creating cdn domain.");
            respData.put("cdn_cname", cdnCNAME);
            respData.put("domain_id", randomBucketName);
            respData.put("record_id", recordId);

        } catch (Exception e) {
            respData.put("result", "false");
            respData.put("message", "Fail in creating cdn domain due to " + e);
            log.error(e.getMessage(),e);
        }
        return respData.toJSONString();
    }

    private JSONObject configHttps(CDNConfig config,String randomBucketName) throws Exception{
    	String url=CDNConstant.CDN_CERTIFICATE;
		String domain=config.getDomain();
		String cdnCertificateId=CDNConstant.getCdnCertificateId();
		JSONObject json=new JSONObject();
		json.put("certificate_id", cdnCertificateId);
		json.put("domain", domain);
		json.put("https", true);
		HttpResponse response = doPost(url, json);
        int statusCode = response.getStatusLine().getStatusCode();
        log.info("response is \n" + response + "\nstatus code is " + statusCode);

        HttpEntity responseBody = response.getEntity();
        String respData = EntityUtils.toString(responseBody, "UTF-8");
        log.info("response data is \n" + respData);
        JSONObject respJson = JSONObject.parseObject(respData);
        if (respJson.containsKey("error_code")) {
            respJson.put("result", "false");
        } else {
            respJson.put("message", "Succeed in configuring cdn source.");
            respJson.put("result", true);
        }
        addCDNLog(config.getBucketName(), randomBucketName, url, CDNConstant.operationCdn.OPEN_HTTPS, json, statusCode, respJson);
        return respJson;
	}

	private String rollbackCreating(String bucketName, String randomBucketName, String domain) throws Exception {
        //解除域名绑定
        JSONObject unbind_result = unbindDomain(bucketName, randomBucketName, domain);
        //删除bucket
        if(unbind_result.getBoolean("result")){
            String result = deleteDomain(randomBucketName, domain);
            JSONObject del_result = JSONObject.parseObject(result);
            if(del_result.getBoolean("result")){
                return "Rollback binding domain and creating bucket successfully";
            }else {
                return "Fail to rollback creating bucket";
            }
        }else {
            return "Fail to rollback binding domain";
        }
    }

    /**
     * 底层删除bucket，如果该bucket已开启CDN加速，则删除加速域名绑定
     * @param domainId bucketName对应UpYun平台的domainId
     * @param domain 加速域名，如bucketName.file.eayun.com
     * @return
     */
    public String deleteDomain(String domainId, String domain) {
        JSONObject respData;
        String result = configVisibility(domainId, false);
        respData = JSONObject.parseObject(result);
        if(!respData.getBoolean("result")){
            return respData.toJSONString();
        }
        String bucketName = domain.substring(0, domain.indexOf("."));
        try {
            respData = unbindDomain(bucketName,domainId, domain);
        } catch (Exception e) {
            respData.put("result", "false");
            respData.put("message", "Fail in unbinding domain due to " + e);
            log.error(e.getMessage(),e);
        }
        return respData.toJSONString();
    }


    /**
     * 启用CDN加速域名
     *
     * @param domainId
     * @return
     */
    public String enableDomain(String domainId) {
        //对于Upyun来讲，使用开启/关闭空间外链状态来实现启用/禁用加速域名的功能，此外还需要业务层停用该子域名DNS中的cname记录：
        return configVisibility(domainId, true);
    }

    /**
     * 禁用CDN加速域名
     *
     * @param domainId
     * @return
     */
    public String disableDomain(String domainId) {
        return configVisibility(domainId, false);
    }

    /**
     * 获取CDN加速配置<br/>UpYunCDN不需要该接口获取配置状态等信息
     *
     * @param domainId
     * @return
     */
    public String getDomainConfiguration(String domainId) {
//        String url = CDNConstant.BUCKET_INFO + "?bucket_name="+domainId;
//        JSONObject respData = new JSONObject();
//        int statusCode = 0;
//        try {
//            HttpResponse response = doGet(url);
//            statusCode = response.getStatusLine().getStatusCode();
//            log.info("response is \n" + response + "\nstatus code is " + statusCode);
//
//            HttpEntity responseBody = response.getEntity();
//            String tmp = EntityUtils.toString(responseBody, "UTF-8");
//            log.info("response data is \n" + tmp);
//            respData = JSONObject.parseObject(tmp);
//            if(respData.containsKey("error_code")){
//                respData.put("result","false");
//            }else{
//                respData.put("result","true");
//                respData.put("message","Succeed in get statistics.");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        //do nothing
        return null;
    }

    /**
     * 获取CDN流量统计信息
     *
     * @param domainId
     * @param from     起始日期
     * @param to       截止日期
     * @return
     */
    public String getStatistics(String domainId, Date from, Date to) {
        JSONObject respData = new JSONObject();
        int statusCode = 0;
        String url = "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd%20HH:mm:ss");
            String fromStr = sdf.format(from);
            String toStr = sdf.format(to);
            log.info("date from is " + fromStr + ", date to " + toStr);
            url = CDNConstant.CDN_STATISTICS + "?bucket_name=" + domainId + "&start_time=" + fromStr + "&end_time=" + toStr;
            log.info("url is " + url);

            HttpResponse response = doGet(url);
            statusCode = response.getStatusLine().getStatusCode();
            log.info("response is \n" + response + "\nstatus code is " + statusCode);

            HttpEntity responseBody = response.getEntity();
            String tmp = EntityUtils.toString(responseBody, "UTF-8");
            log.info("response data is \n" + tmp);
            respData = JSONObject.parseObject(tmp);
            if (respData.containsKey("error_code")) {
                respData.put("result", "false");
            } else {
                respData.put("result", "true");
                respData.put("message", "Succeed in get statistics.");
            }
        } catch (Exception e) {
            respData.put("result", "false");
            respData.put("message", "Fail in getting cdn statistics due to " + e);
            log.error(e.getMessage(),e);
        }
        addCDNLog(null, domainId, url, CDNConstant.operationCdn.QUERY_BANDWIDTH, null, statusCode, respData);
        return respData.toJSONString();

    }

    /**
     * 清除缓存文件
     *
     * @param fileUrls 需要完整的file url, e.g:http://{bucket_name}.file.eayun.com/xxx.pdf
     * @return
     */
    public String purgeFiles(String[] fileUrls) {
        JSONObject respData = new JSONObject();
        String url = CDNConstant.CDN_PURGE;
        JSONObject json = new JSONObject();
        int statusCode = 0;
        try {
            StringBuffer fileURLs = new StringBuffer();
            for (String fileURL : fileUrls) {
                fileURLs.append(fileURL).append("\n");
            }
            json.put("urls", fileURLs.toString());

            HttpResponse response = doPost(url, json);
            statusCode = response.getStatusLine().getStatusCode();
            log.info("response is \n" + response + "\nstatus code is " + statusCode);

            HttpEntity responseBody = response.getEntity();
            String tmp = EntityUtils.toString(responseBody, "UTF-8");
            log.info("response data is \n" + tmp);
            respData = JSONObject.parseObject(tmp);
            if (respData.containsKey("error_code")) {
                respData.put("result", "false");
            } else {
                respData.put("message", "Succeed in purging files.");
            }
        } catch (Exception e) {
            respData.put("result", "false");
            respData.put("message", "Fail in purging files due to " + e);
            log.error(e.getMessage(),e);
        }
        addCDNLog(null, null, url, CDNConstant.operationCdn.REFRESH_CACHE, json, statusCode, respData);
        return respData.toJSONString();
    }

    private HttpResponse doGet(String url) throws Exception {
        String accessToken = getAccessToken();
        HttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(url);
        get.setHeader("Accept", "application/json");
        get.setHeader("Content-Type", "application/json");
        get.setHeader("Authorization", "Bearer " + accessToken);

        return client.execute(get);

    }

    private HttpResponse doDelete(String url) throws Exception {
        String accessToken = getAccessToken();
        HttpClient client = HttpClients.createDefault();
        HttpDelete get = new HttpDelete(url);
        get.setHeader("Accept", "application/json");
        get.setHeader("Content-Type", "application/json");
        get.setHeader("Authorization", "Bearer " + accessToken);

        return client.execute(get);

    }

    private HttpResponse doPost(String url, JSONObject params) throws Exception {
        String accessToken = getAccessToken();
        HttpClient client = HttpClients.createDefault();
        StringEntity requestBody = new StringEntity(params.toJSONString(), "UTF-8");
        log.info("request body is " + requestBody);

        HttpPost post = new HttpPost(url);
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-Type", "application/json");
        post.setHeader("Authorization", "Bearer " + accessToken);
        post.setEntity(requestBody);

        return client.execute(post);
    }

    private HttpResponse doPut(String url, JSONObject params) throws Exception {
        String accessToken = getAccessToken();
        HttpClient client = HttpClients.createDefault();
        StringEntity requestBody = new StringEntity(params.toJSONString(), "UTF-8");
        log.info("request body is " + requestBody);

        HttpPut put = new HttpPut(url);
        put.setHeader("Accept", "application/json");
        put.setHeader("Content-Type", "application/json");
        put.setHeader("Authorization", "Bearer " + accessToken);
        put.setEntity(requestBody);

        return client.execute(put);
    }

    private String configVisibility(String domainId, boolean isVisible) {
        JSONObject respData = new JSONObject();
        String url = CDNConstant.BUCKET_VISIBLE;
        JSONObject json = new JSONObject();
        int statusCode = 0;
        try {
            json.put("bucket_name", domainId);
            json.put("visible", isVisible);

            HttpResponse response = doPost(url, json);
            statusCode = response.getStatusLine().getStatusCode();
            log.info("response is \n" + response + "\nstatus code is " + statusCode);

            HttpEntity responseBody = response.getEntity();
            String tmp = EntityUtils.toString(responseBody, "UTF-8");
            log.info("response data is \n" + tmp);
            respData = JSONObject.parseObject(tmp);
            if (respData.containsKey("error_code")) {
                respData.put("result", "false");
            } else {
                respData.put("message", "Succeed in setting visibility of bucket.");
            }
        } catch (Exception e) {
            respData.put("result", "false");
            respData.put("message", "Fail in setting visibility of bucket due to " + e);
            log.error(e.getMessage(),e);
        }
        addCDNLog(null, domainId, url, CDNConstant.operationCdn.CONFIG_VISIBLE, json, statusCode, respData);
        return respData.toJSONString();

    }

    private JSONObject createBucket(String bucketName) throws Exception {
        //如果生成的bucketname在upyun平台存在，则继续尝试生成，直至成功
        while (true) {
            String randomBucketName = generateRandomBucketName(16);
            JSONObject result = createBucket(bucketName, randomBucketName);
            //如果创建bucket失败，则检查是否是由于error_code=21202, 即Bucket already exists引起的，如果是，则continue；如果否，结束。
            if (!result.getBoolean("result")) {
                if (result.getIntValue("error_code") == 21202) {
                    continue;
                } else {
                    return result;
                }
            } else {
                result.put("randomBucketName", randomBucketName);
                return result;
            }
        }
    }

    private JSONObject createBucket(String bucketName, String randomBucketName) throws Exception {
        String url = CDNConstant.BUCKET;
        JSONObject json = new JSONObject();
        json.put("bucket_name", randomBucketName);
        json.put("type", "ucdn");

        HttpResponse response = doPut(url, json);
        int statusCode = response.getStatusLine().getStatusCode();
        log.info("response is \n" + response + "\nstatus code is " + statusCode);

        HttpEntity responseBody = response.getEntity();
        String respData = EntityUtils.toString(responseBody, "UTF-8");
        log.info("response data is \n" + respData);
        JSONObject respJson = JSONObject.parseObject(respData);
        if (respJson.containsKey("error_code")) {
            respJson.put("result", "false");
        } else {
            respJson.put("message", "Succeed in creating bucket.");
        }
        addCDNLog(bucketName, randomBucketName, url, CDNConstant.operationCdn.CREATE_BUCKETS, json, statusCode, respJson);
        return respJson;
    }

    private JSONObject configSource(CDNConfig config, String randomBucketName) throws Exception {
        String url = CDNConstant.CDN_SOURCE;

        String bucketName = config.getBucketName();
        //拿到传入的回源地址，判断是否包含端口号，如果包含，则拆分出回源地址和端口号两部分；如果不包含端口号，则使用默认端口号80.
        Map<String, String> params = processOrigin(config.getOrigin());
        String port = params.get("port");
        String origin = params.get("origin");

        JSONObject json = new JSONObject();
        json.put("bucket_name", randomBucketName);
        json.put("domain", origin);

        JSONObject cdn = new JSONObject();
        JSONObject bgp = new JSONObject();
        JSONArray servers = new JSONArray();
        JSONObject server = new JSONObject();
        server.put("host", origin);
        server.put("port", port);
        //TODO  weight, max_fails和fail_timeout目前给出了一个默认值，后面看情况再做调整
        server.put("weight", 1);
        server.put("max_fails", 3);
        server.put("fail_timeout", 30);
        servers.add(server);

        bgp.put("servers", servers);
        cdn.put("bgp", bgp);
        json.put("cdn", cdn);
        String requestHeader = getRequestHeader();
        json.put("source_type", requestHeader);//enum('http', 'https', 'protocol_follow')
        json.put("domain_follow", "disable");//enable, disable, 是否开启域名跟随：加速域名将通过Host请求头传递给源站

        HttpResponse response = doPost(url, json);
        int statusCode = response.getStatusLine().getStatusCode();
        log.info("response is \n" + response + "\nstatus code is " + statusCode);

        HttpEntity responseBody = response.getEntity();
        String respData = EntityUtils.toString(responseBody, "UTF-8");
        log.info("response data is \n" + respData);
        JSONObject respJson = JSONObject.parseObject(respData);
        if (respJson.containsKey("error_code")) {
            respJson.put("result", "false");
        } else {
            respJson.put("message", "Succeed in configuring cdn source.");
        }
        addCDNLog(bucketName, randomBucketName, url, CDNConstant.operationCdn.CONFIG_SOURCE, json, statusCode, respJson);
        return respJson;
    }

    private String getRequestHeader() {
		String header=ObsUtil.getRequestHeader();
		if(header.indexOf(":")!=-1){
			header=header.substring(0, header.indexOf(":"));
		}
		return header;
	}

	private JSONObject configCache(CDNConfig config, String randomBucketName) throws Exception {
        String bucketName = config.getBucketName();
        String url = CDNConstant.CDN_CACHE;
        log.info("url is " + url);
        List<CacheRule> cacheRuleList = config.getCacheRuleList();
        JSONObject json = new JSONObject();
        json.put("bucket_name", randomBucketName);

        JSONArray specificRules = new JSONArray();
        for (CacheRule rule : cacheRuleList) {
            JSONObject spRule = new JSONObject();
            spRule.put("uri", rule.getUri());
            spRule.put("expires", rule.getTtl());
            specificRules.add(spRule);
        }
        json.put("specific_rules", specificRules);

        //目前的不缓存列表不指定任何不缓存的情况。
        JSONArray nocahceList = new JSONArray();
        json.put("nocache_list", nocahceList);

        HttpResponse response = doPost(url, json);
        int statusCode = response.getStatusLine().getStatusCode();
        log.info("response is \n" + response + "\nstatus code is " + statusCode);

        HttpEntity responseBody = response.getEntity();
        String respData = EntityUtils.toString(responseBody, "UTF-8");
        log.info("response data is \n" + respData);
        JSONObject respJson = JSONObject.parseObject(respData);
        if (respJson.containsKey("error_code")) {
            respJson.put("result", "false");
        } else {
            respJson.put("message", "Succeed in configuring cdn cache rules.");
        }
        addCDNLog(bucketName, randomBucketName, url, CDNConstant.operationCdn.CONFIG_CACHE, json, statusCode, respJson);
        return respJson;
    }

    private JSONObject bindDomain(String bucketName, String randomBucketName, String domain) throws Exception {
        String url = CDNConstant.BUCKET_DOMAINS;
        log.info("url is " + url);

        JSONObject json = new JSONObject();
        json.put("bucket_name", randomBucketName);
        json.put("domain", domain);

        HttpResponse response = doPut(url, json);
        int statusCode = response.getStatusLine().getStatusCode();
        log.info("response is \n" + response + "\nstatus code is " + statusCode);

        HttpEntity responseBody = response.getEntity();
        String respData = EntityUtils.toString(responseBody, "UTF-8");
        log.info("response data is \n" + respData);
        JSONObject respJson = JSONObject.parseObject(respData);
        if (respJson.containsKey("error_code")) {
            respJson.put("result", "false");
        } else {
            respJson.put("message", "Succeed in bind domain to bucket.");
        }
        addCDNLog(bucketName, randomBucketName, url, CDNConstant.operationCdn.BIND_DOMAIN, json, statusCode, respJson);
        return respJson;
    }

    private Map<String, String> processOrigin(String origin) {
        Map<String, String> params = new HashMap<>();
        boolean hasPort = origin.contains(":");
        String port = "80";
        if (hasPort) {
            port = origin.substring(origin.indexOf(":") + 1);
            origin = origin.substring(0, origin.indexOf(":"));
        }else{
        	String header=ObsUtil.getRequestHeader();
        	if("https://".equals(header)){
        		port="443";
        	}
        }
        params.put("port", port);
        params.put("origin", origin);
        return params;
    }

    /**
     *
     * @param bucketName
     * @param randomBucketName
     * @param domain
     * @return
     * @throws Exception
     */
    private JSONObject unbindDomain(String bucketName, String randomBucketName, String domain) throws Exception {
        String url = CDNConstant.BUCKET_DOMAINS + "?bucket_name="+randomBucketName+"&domain="+domain;

        HttpResponse response = doDelete(url);
        int statusCode = response.getStatusLine().getStatusCode();
        log.info("response is \n" + response + "\nstatus code is " + statusCode);

        HttpEntity responseBody = response.getEntity();
        String respData = EntityUtils.toString(responseBody, "UTF-8");
        log.info("response data is \n" + respData);
        JSONObject respJson = JSONObject.parseObject(respData);
        if (respJson.containsKey("error_code")) {
            respJson.put("result", "false");
        } else {
            respJson.put("message", "Succeed in unbinding domain to bucket.");
        }
        addCDNLog(bucketName, randomBucketName, url, CDNConstant.operationCdn.BIND_DOMAIN, null, statusCode, respJson);
        return respJson;
    }

    private String generateRandomBucketName(int length) {
        char[] chs = new char[length];
        chs[0] = getRandomInitial();
        for (int i = 1; i < length; i++) {
            chs[i] = getRandomNumOrChars();
        }
        return new String(chs);
    }

    private char getRandomNumOrChars() {
        String temp = "0123456789qwertyuiopasdfghjklzxcvbnm";
        return temp.charAt(iRandom(0, temp.length()));
    }

    private char getRandomInitial() {
        String index = "qwertyuiopasdfghjklzxcvbnm";
        return index.charAt(iRandom(0, index.length()));
    }

    /**
     * 获取区间[m,n)内的一个正整数
     *
     * @param m
     * @param n
     * @return
     */
    private int iRandom(int m, int n) {
        Random random = new Random();
        int small = m > n ? n : m;
        int big = m <= n ? n : m;
        return small + random.nextInt(big - small);
    }

    private void addCDNLog(String bucketName, String domainId, String url,
                           Object operation, JSONObject requestBody, int statusCode,
                           JSONObject result) {
        JSONObject json = new JSONObject();
        json.put("bucketName", bucketName);
        json.put("domainId", domainId);
        json.put("URL", url);
        json.put("operation", operation);
        json.put("provider", CDNConstant.cdnProvider.UpYun.toString());
        json.put("requestBody", requestBody);
        json.put("statusCode", statusCode);
        json.put("timestamp", new Date());

        //TODO 记录详细API返回信息
        json.put("message", result.get("message"));
        json.put("status", result.getString("result").equals("true") ? "1" : "0");
        mongoTemplate.insert(json, MongoCollectionName.LOG_API_CDN);
    }


    private String getAccessToken() throws Exception {
        if (ACCESS_TOKEN == null || ACCESS_TOKEN.equals("")) {
            String tokenInfo = jedisUtil.get("sys_data_tree:0009007");
            JSONObject token = JSONObject.parseObject(tokenInfo);
            ACCESS_TOKEN = token.getString("para1");
        }
        return ACCESS_TOKEN;
    }
    /**
     * 获取指定bucket在指定时间范围内的回源流量
     * @param domainId 
     * @param from
     * @param to
     * @return
     * @throws Exception
     */
    public String getBackSource(String domainId,Date from , Date to) throws Exception{
    	log.info("开始进行获取bucket回源流量,domainId:"+domainId+",from:"+from+",to:"+to);
    	List<NameValuePair> params = new ArrayList<NameValuePair>();  
        params.add(new BasicNameValuePair("start_time", DateUtil.dateToString(from)));  
        params.add(new BasicNameValuePair("end_time", DateUtil.dateToString(to)));  
        params.add(new BasicNameValuePair("flow_source", "backsource"));  
        params.add(new BasicNameValuePair("query_type", "bucket"));  
        params.add(new BasicNameValuePair("query_value", domainId));  
        String url=CDNConstant.CDN_COMMONDATA+"?"+URLEncodedUtils.format(params, HTTP.UTF_8);
        log.info("url:"+url);
        HttpResponse res=doGet(url);
        int statusCode = res.getStatusLine().getStatusCode();
        String resData = EntityUtils.toString(res.getEntity());
        log.info("response:"+resData);
        JSONObject result = new JSONObject();
        
        if("{}".equals(resData)){
        	result.put("result", false);
        	result.put("message", "response is {}");
        }else{
        	try {
    			JSONArray jsonArray =  JSONArray.parseArray(resData);
    			result.put("data", jsonArray);
    			result.put("result", true);
    			result.put("message", "Succeed in get backsource.");
    		} catch (JSONException e) {
    			result = JSONObject.parseObject(resData);
    			if(result.containsKey("error_code")){
    				result.put("result", false);
    			}
    		}
        }
        addCDNLog(null, domainId, url, CDNConstant.operationCdn.QUERY_BACKSOURCE, null, statusCode, result);
    	return result.toJSONString();
    }
}
