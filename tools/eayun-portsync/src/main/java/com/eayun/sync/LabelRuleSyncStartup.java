package com.eayun.sync;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class LabelRuleSyncStartup {
    private final static Logger log = LoggerFactory.getLogger(LabelRuleSyncStartup.class);
    public static ClassPathXmlApplicationContext context = null;
    public static String NETWORK_SERVICE_NAME = "network";
    public static String METERING_LABELS_RULE_URI = "/v2.0/metering/metering-label-rules";
    public static String METERING_LABELS_RULE_DATANAMES = "metering_label_rules";
    
    public static void main (String[] args) {
        try {
            String dcId = args[0];
            context = new ClassPathXmlApplicationContext("classpath*:spring/*.xml");
            
            Map<String, JSONObject> stackMap = new HashMap<String, JSONObject>();
            Map<String, String> dbMap = new HashMap<String, String>();
            
            JSONObject data = getInfoFromDatacenter(dcId);
            JSONObject respData = assembleDataForToken(data, NETWORK_SERVICE_NAME);
            String resData = get(respData.getString("tokenId"), respData.getString("endpoint") + METERING_LABELS_RULE_URI);
            List<JSONObject> stackList = string2Json(resData, METERING_LABELS_RULE_DATANAMES);
            
            List<JSONObject> dbList = getAllLabelRulesByDcId(dcId);
            
            List<JSONObject> stackListForDelete = new ArrayList<JSONObject>();
            List<JSONObject> dbListForDelete = new ArrayList<JSONObject>();
            
            if (null != dbList) {
                for (JSONObject json : dbList) {
                    dbMap.put(json.getString("inLabelRuleId"), "");
                    dbMap.put(json.getString("outLabelRuleId"), "");
                }
            }
            
            if (null != stackList) {
                for (JSONObject json : stackList) {
                    stackMap.put(json.getString("id"), json);
                    if (!dbMap.containsKey(json.getString("id"))) {
                        stackListForDelete.add(json);
                    }
                }
            }
            
            if (null != dbList) {
                for (JSONObject json : dbList) {
                    if (!stackMap.containsKey(json.getString("inLabelRuleId")) || !stackMap.containsKey(json.getString("outLabelRuleId"))) {
                        dbListForDelete.add(json);
                    }
                }
            }
            
            log.info("底层比上层多出来的label-rule为：" + stackListForDelete);
            log.info("上层比底层多出来的label-rule为：" + dbListForDelete);
            log.info("待执行的删除label-rule的id开始");
            for (JSONObject json : stackListForDelete) {
                log.info(json.get("id").toString());
            }
            log.info("待执行的删除label-rule的id结束");
            //TODO 删除 增加
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }
    
    private static List<JSONObject> getAllLabelRulesByDcId (String dcId) {
        List<JSONObject> jsonList = new ArrayList<JSONObject>();
        try {
            JdbcTemplate jdbcTemplate = context.getBean(JdbcTemplate.class);
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT                                 ");
            sql.append("    subnet_id                           ");
            sql.append("    ,in_label_rule_id                   ");
            sql.append("    ,out_label_rule_id                  ");
            sql.append(" FROM                                   ");
            sql.append("    cloud_subnetwork                    ");
            sql.append(" WHERE                                  ");
            sql.append("    dc_id = ?                           ");
            sql.append("    AND in_label_rule_id is not null    ");
            sql.append("    AND out_label_rule_id is not null   ");
            jsonList = jdbcTemplate.query(sql.toString(), new Object[] {dcId},new RowMapper<JSONObject>(){
                @Override
                public JSONObject mapRow(ResultSet rs, int rowNum) throws SQLException {
                    JSONObject json = new JSONObject();
                    json.put("subnetId", rs.getString("subnet_id"));
                    json.put("inLabelRuleId", rs.getString("in_label_rule_id"));
                    json.put("outLabelRuleId", rs.getString("out_label_rule_id"));
                    return json;
                }});
            if (null != jsonList && jsonList.size() > 0) {
                log.info("上层label-rule查询结果为：" + jsonList);
                return jsonList;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            e.printStackTrace();
            throw e;
        }
        return null;
    }
    
    private static JSONObject getInfoFromDatacenter (String dcId) {
        JSONObject json = new JSONObject();
        try {
            JdbcTemplate jdbcTemplate = context.getBean(JdbcTemplate.class);
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT                     ");
            sql.append("    id                      ");
            sql.append("    ,dc_name                ");
            sql.append("    ,dc_address             ");
            sql.append("    ,os_admin_project_id    ");
            sql.append("    ,v_center_username      ");
            sql.append("    ,v_center_password      ");
            sql.append(" FROM                       ");
            sql.append("    dc_datacenter           ");
            sql.append(" WHERE                      ");
            sql.append("    id = ?                  ");
            json = jdbcTemplate.queryForObject(sql.toString(), new Object[] {dcId}, new RowMapper<JSONObject>(){
                @Override
                public JSONObject mapRow(ResultSet rs, int rowNum) throws SQLException {
                    JSONObject json = new JSONObject();
                    json.put("username", rs.getString("v_center_username"));
                    json.put("password", rs.getString("v_center_password"));
                    json.put("url", rs.getString("dc_address"));
                    json.put("prjId", rs.getString("os_admin_project_id"));
                    return json;
                }
            });
        } catch (Exception e) {
            log.error("数据中心不存在！");
        }
        return json;
    }
    
    private static JSONObject assembleDataForToken (JSONObject tokenJson, String serviceName) {
        JSONObject respJson = new JSONObject();
        try {
            // 设置鉴权信息
            JSONObject jsonObject0 = new JSONObject();
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("username", tokenJson.getString("username"));
            jsonObject1.put("password", tokenJson.getString("password"));
            
            jsonObject0.put("passwordCredentials", jsonObject1);
            jsonObject0.put("tenantId", tokenJson.getString("prjId"));
            
            JSONObject auth = new JSONObject();
            auth.put("auth", jsonObject0);
            // 发送请求，获取token返回值
            JSONObject access = getToken(auth, tokenJson.getString("url"));
            // 获取tokenId
            String tokenId = (((JSONObject) ((JSONObject) access.get("access")).get("token")).get("id")).toString();
            JSONArray array = (JSONArray) ((JSONObject) access.get("access")).get("serviceCatalog");
            String endpoint = null;
            // 获取指定服务名的endpoint
            for (Object object : array) {
                JSONObject data = (JSONObject) object;
                boolean isThisService = data.get("type").equals(serviceName);
                // 非keystone组件，获取commonRegion的publicURL
                if (isThisService) {
                    JSONArray dataArray = (JSONArray) data.get("endpoints");
                    for (Object object2 : dataArray) {
                        JSONObject json = (JSONObject) object2;
                        if (json.getString("region").equals("RegionOne")) {
                            endpoint = json.getString("publicURL");
                            break;
                        }
                    }
                }
                if (endpoint != null) {
                    break;
                }
            }
            respJson.put("tokenId", tokenId);
            respJson.put("endpoint", endpoint);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return respJson;
    }
    
    private static JSONObject getToken(JSONObject json, String url) throws Exception {
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost post = new HttpPost(url + "/tokens");
        //设置请求超时
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(60000).setConnectTimeout(60000).build(); 
        post.setConfig(requestConfig);  

        log.info("The current URL is:" + url);
        log.info("The requestbody is:" + json);
        JSONObject resJson = null;
        String resData = null;
        int response = 0;
        log.info("url=" + url);
        log.info("paras=" + json);

        try {
            StringEntity s = new StringEntity(json.toString());
            s.setContentEncoding("UTF-8");
            s.setContentType("application/json");
            post.setEntity(s);
            post.setHeader("Accept", "application/json");
            HttpResponse res = httpclient.execute(post);
            response = res.getStatusLine().getStatusCode();
            log.info("The response code=" + response);
            HttpEntity resEntity = res.getEntity();
            if (resEntity != null) {
                resData = EntityUtils.toString(res.getEntity());
                if (resData != null) {
                    resJson = JSONObject.parseObject(resData);
                }
            }
            log.info("The response Json=" + resJson);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        return resJson;
    }
    
    private static String get(String tokenId, String url) {
        HttpClient httpclient = HttpClients.createDefault();
        HttpGet get = new HttpGet(url);
        log.info("The current URL is:" + url);
        String resData = null;
        int respCode = 0;
        try {
            get.setHeader("Content-Type", "application/json");
            get.setHeader("Accept", "application/json");
            get.setHeader("X-Auth-Token", tokenId);
            HttpResponse res = httpclient.execute(get);
            respCode = res.getStatusLine().getStatusCode();
            log.info("respCode:" + respCode);
            HttpEntity resEntity = res.getEntity();
            if (resEntity != null) {
                resData = EntityUtils.toString(resEntity);
                log.info("resData:"+resData);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return resData;
    }
    
    private static List<JSONObject> string2Json (String str, String dataName) {
        List<JSONObject> list = new ArrayList<JSONObject>();
        JSONObject json = JSONObject.parseObject(str);
        JSONArray array = json.getJSONArray(dataName);
        if (null != array && array.size() > 0) {
            for (int i = 0; i < array.size(); i++) {
                list.add(array.getJSONObject(i));
            }
        }
        return list;
    }
}
