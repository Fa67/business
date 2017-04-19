package com.eayun.cdn.impl;

import com.alibaba.fastjson.JSONObject;
import com.eayun.cdn.bean.DNSRecord;
import com.eayun.cdn.util.DNSConstant;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.redis.JedisUtil;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ZH.F on 2016/6/23.
 */
@Component
public class DNS {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private JedisUtil jedisUtil;

    private String ACCESS_TOKEN;

    private String getAccessToken() throws Exception {
        if (ACCESS_TOKEN == null || ACCESS_TOKEN.equals("")) {
            String tokenInfo = jedisUtil.get("sys_data_tree:0009007");
            JSONObject token = JSONObject.parseObject(tokenInfo);
            ACCESS_TOKEN = token.getString("para2");
        }
        return ACCESS_TOKEN;
    }

    private String getPublicParams() throws Exception {
        String accessToken = getAccessToken();

        StringBuffer sb = new StringBuffer();
        sb.append("login_token=");
        sb.append(accessToken);
        sb.append("&format=json");
        return sb.toString();
    }

    /**
     * 在DNS上添加一个新域名
     *
     * @param domainName
     * @return
     * @throws Exception
     */
    public String createDomain(String domainName) throws Exception {
        String url = DNSConstant.DOMAIN_CREATE;
        String params = getPublicParams();
        params += "&domain=" + domainName;
        String respData = doPost(url, params);

        return respData;
    }

    /**
     * 获取在DNS上的域名列表
     *
     * @return
     * @throws Exception
     */
    public String getDomainList() throws Exception {
        String url = DNSConstant.DOMAIN_LIST;
        String params = getPublicParams();
        String respData = doPost(url, params);

        return respData;
    }

    /**
     * 根据域名获取域名信息
     *
     * @param domainName
     * @return
     * @throws Exception
     */
    public String getDomainInfoByName(String domainName) throws Exception {
        String url = DNSConstant.DOMAIN_INFO;
        String params = getPublicParams();
        params += "&domain=" + domainName;
        String respData = doPost(url, params);

        return respData;
    }

    /**
     * 根据域名获取域名绑定列表，注：域名绑定列表不是域名的记录（如@、A、CNAME等）
     *
     * @param domainName
     * @return
     * @throws Exception
     */
    public String getDomainAliasListByName(String domainName) throws Exception {
        String url = DNSConstant.DOMAIN_ALIAS_LIST;
        String params = getPublicParams();
        params += "&domain=" + domainName;
        String respData = doPost(url, params);

        return respData;
    }

    /**
     * 根据域名获取记录列表
     *
     * @param domainName
     * @return
     * @throws Exception
     */
    public String getDomainRecordListByName(String domainName) throws Exception {
        String url = DNSConstant.DOMAIN_RECORD_LIST;
        String params = getPublicParams();
        params += "&domain=" + domainName;
        String respData = doPost(url, params);

        return respData;
    }

    /**
     * 根据域名ID获取记录列表
     *
     * @param domainId
     * @return
     * @throws Exception
     */
    public String getDomainRecordListById(String domainId) throws Exception {
        return getDomainRecordListById(domainId, null);
    }

    /**
     * 根据域名ID获取记录列表，通过keyword过滤，如 指定keyword="CNAME"，则查询的记录列表为CNAME记录
     *
     * @param domainId
     * @param keyword
     * @return
     * @throws Exception
     */
    public String getDomainRecordListById(String domainId, String keyword) throws Exception {
        String url = DNSConstant.DOMAIN_RECORD_LIST;
        String params = getPublicParams();
        params += "&domain_id=" + domainId;
        if (keyword != null && !keyword.trim().equals("")) {
            params += "&keyword=" + keyword;
        }
        String respData = doPost(url, params);

        return respData;
    }

    /**
     * 根据域名等级获取允许的记录类型<br/>domain_grade 域名等级,分别为：
     * <br/>D_Free, D_Plus, D_Extra, D_Expert, D_Ultra, 分别对应免费套餐、个人豪华、企业Ⅰ、企业Ⅱ、企业Ⅲ.
     * <br/>新套餐：DP_Free, DP_Plus, DP_Extra, DP_Expert, DP_Ultra, 分别对应新免费、个人专业版、企业创业版、企业标准版、企业旗舰版
     *
     * @param domainGrade
     * @return
     * @throws Exception
     */
    public String getRecordTypeByDomainGrade(String domainGrade) throws Exception {
        String url = DNSConstant.DOMAIN_RECORD_TYPE;
        String params = getPublicParams();
        params += "&domain_grade=" + domainGrade;
        String respData = doPost(url, params);

        return respData;
    }

    /**
     * 根据域名等级获取允许的记录线路
     *
     * @param domainGrade
     * @return
     * @throws Exception
     */
    public String getRecordLineByDomainGrade(String domainGrade) throws Exception {
        String url = DNSConstant.DOMAIN_RECORD_LINE;
        String params = getPublicParams();
        params += "&domain_grade=" + domainGrade;
        String respData = doPost(url, params);

        return respData;
    }

    /**
     * 添加一条记录
     *
     * @param record
     * @return
     * @throws Exception
     */
    public String createRecord(DNSRecord record) throws Exception {
        Assert.notNull(record, "记录不能为空");

        String url = DNSConstant.DOMAIN_RECORD_CREATE;
        String params = getPublicParams();
        StringBuffer sb = new StringBuffer(params);
        appendParams(sb, record);
        String respData = doPost(url, sb.toString());

        doLog(record, url, respData);
        return respData;
    }

    /**
     * 执行添加日志
     *
     * @param record
     * @param url
     * @param respData
     */
    private void doLog(DNSRecord record, String url, String respData) {
        String subDomain = record.getSubDomain();
        String cname = record.getValue();
        String bucketName = subDomain.substring(0, subDomain.indexOf("."));
        String randomBucketName = cname.substring(0, cname.indexOf("."));
        JSONObject resp = JSONObject.parseObject(respData);
        String recordId = resp.getJSONObject("record").getString("id");
        addDNSLog(bucketName, randomBucketName, url, DNSConstant.operationDns.CREATE_RECORD, resp, recordId);
    }

    /**
     * 修改记录<br/>
     * 如果1小时之内，提交了超过5次没有任何变动的记录修改请求，该记录会被系统锁定1小时，不允许再次修改。比如原记录值已经是 1.1.1.1，新的请求还要求修改为 1.1.1.1
     *
     * @param record
     * @return
     * @throws Exception
     */
    public String modifyRecord(DNSRecord record) throws Exception {
        Assert.notNull(record, "记录不能为空");

        String url = DNSConstant.DOMAIN_RECORD_MODIFY;
        String params = getPublicParams();
        StringBuffer sb = new StringBuffer(params);
        appendParams(sb, record);
        String respData = doPost(url, params);

        return respData;
    }

    /**
     * 删除域名ID为domainId的一条记录ID为recordId的的记录
     *
     * @param domainId
     * @param recordId
     * @return
     * @throws Exception
     */
    public String removeRecord(String domainId, String recordId) throws Exception {
        String url = DNSConstant.DOMAIN_RECORD_REMOVE;
        String params = getPublicParams();
        params += "&domain_id=" + domainId + "&record_id=" + recordId;
        String respData = doPost(url, params);

        return respData;

    }

    /**
     * 获取指定域名ID的指定记录ID的详情信息
     *
     * @param domainId
     * @param recordId
     * @return
     * @throws Exception
     */
    public String getRecordInfo(String domainId, String recordId) throws Exception {
        String url = DNSConstant.DOMAIN_RECORD_INFO;
        String params = getPublicParams();
        params += "&domain_id=" + domainId + "&record_id=" + recordId;
        String respData = doPost(url, params);

        return respData;

    }

    /**
     * 设置指定域名ID的指定记录ID的记录状态
     *
     * @param domainId
     * @param recordId
     * @param status
     * @return
     * @throws Exception
     */
    public String setRecordStatus(String domainId, String recordId, boolean status) throws Exception {
        String url = DNSConstant.DOMAIN_RECORD_STATUS;
        String params = getPublicParams();
        params += "&domain_id=" + domainId + "&record_id=" + recordId;
        if (status) {
            params += "&status=enable";
        } else {
            params += "&status=disable";
        }
        String respData = doPost(url, params);

        return respData;
    }

    /**
     * 执行HttpPost
     *
     * @param url
     * @param params
     * @return
     * @throws IOException
     */
    private String doPost(String url, String params) throws IOException {
        StringEntity entity = new StringEntity(params, "UTF-8");
        entity.setContentType("application/x-www-form-urlencoded");

        HttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(url);
        post.setEntity(entity);

        HttpResponse response = client.execute(post);
        HttpEntity respEntity = response.getEntity();
        return EntityUtils.toString(respEntity, "UTF-8");
    }

    /**
     * 处理和拼接参数
     *
     * @param params
     * @param record
     * @throws IllegalAccessException
     */
    private void appendParams(StringBuffer params, DNSRecord record) throws IllegalAccessException {
        Map<String, String> fieldsMap = removeNullFields(record);
        if (fieldsMap.containsKey("domainId")) {
            params.append("&domain_id=").append(fieldsMap.get("domainId"));
        }
        if (fieldsMap.containsKey("subDomain")) {
            params.append("&sub_domain=").append(fieldsMap.get("subDomain"));
        }
        if (fieldsMap.containsKey("recordType")) {
            params.append("&record_type=").append(fieldsMap.get("recordType"));
        }
        if (fieldsMap.containsKey("recordLine")) {
            params.append("&record_line=").append(fieldsMap.get("recordLine"));
        }
        if (fieldsMap.containsKey("value")) {
            params.append("&value=").append(fieldsMap.get("value"));
        }
        if (fieldsMap.containsKey("mx")) {
            params.append("&mx=").append(fieldsMap.get("mx"));
        }
        if (fieldsMap.containsKey("ttl")) {
            params.append("&ttl=").append(fieldsMap.get("ttl"));
        }
        if (fieldsMap.containsKey("status")) {
            params.append("&status=").append(fieldsMap.get("status"));
        }
        if (fieldsMap.containsKey("weight")) {
            params.append("&weight=").append(fieldsMap.get("weight"));
        }
    }

    /**
     * 移除DNSRecord配置中的null字段
     *
     * @param record
     * @return
     * @throws IllegalAccessException
     */
    private Map<String, String> removeNullFields(DNSRecord record) throws IllegalAccessException {
        Map<String, String> fieldsMap = new HashMap<String, String>();
        Field[] fields = record.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            f.setAccessible(true);
            Object val = f.get(record);
            String type = f.getType().getName();
            if (type.endsWith("String")) {
                if (val != null && !String.valueOf(val).trim().equals("")) {
                    fieldsMap.put(f.getName(), (String) val);
                }
            } else if (type.endsWith("int") || type.endsWith("long")) {
                if (val != null) {
                    fieldsMap.put(f.getName(), String.valueOf(val));
                }
            }
        }
        return fieldsMap;
    }

    /**
     * 向MongoDB中添加日志
     *
     * @param bucketName
     * @param domainId
     * @param url
     * @param operation
     * @param responseBody
     * @param recordId
     */
    private void addDNSLog(String bucketName, String domainId, String url,
                           Object operation, JSONObject responseBody, String recordId) {
        JSONObject json = new JSONObject();
        json.put("bucketName", bucketName);
        json.put("domainId", domainId);
        json.put("URL", url);
        json.put("operation", operation);
        json.put("responseBody", responseBody);
        json.put("recordId", recordId);
        json.put("timestamp", new Date());
        String status = responseBody.getJSONObject("status").getString("code");
        if ("1".equals(status)) {
            json.put("status", "1");
        } else {
            json.put("status", "0");
        }
        mongoTemplate.insert(json, MongoCollectionName.LOG_API_DNS);
    }
}
