package com.eayun.eayunstack.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.StringUtil;
import com.eayun.eayunstack.service.RestService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;

@Service
@SuppressWarnings("rawtypes")
public class RestServiceImpl implements RestService {
    private static final Logger       log                  = LoggerFactory
                                                               .getLogger(RestServiceImpl.class);
    
    private static Object lock = new Object();

    private static Timer              clearCacheTokenTimer = null;

    private static Set<RestTokenBean> tokenSet             = new HashSet<RestTokenBean>();

    private static final long         HALF_HOUR            = 1000 * 60 * 30;

    public RestServiceImpl() {
        synchronized (lock) {
            if (clearCacheTokenTimer == null) {
                clearCacheTokenTimer = new Timer();
                // 定时去掉缓存中过时的token
                clearCacheTokenTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Date now = new Date();

                        Iterator<RestTokenBean> it = tokenSet.iterator();
                        while (it.hasNext()) {
                            RestTokenBean restTokenBean = it.next();
                            if (now.getTime() - restTokenBean.getLastGetTime() >= HALF_HOUR) {
                                it.remove();
                            }
                        }
                    }
                }, 10000, 10000);
            }
        }
    }

    public <T> T json2bean(JSONObject jSONObject, Class<T> clazz) {
        return JSON.parseObject(jSONObject.toJSONString(), clazz);
    }

    public List<JSONObject> list(RestTokenBean bean, String dataName) throws AppException {
        List<JSONObject> list = new ArrayList<JSONObject>();;
        
        String nextLinkUrl = null;
        String resData = get(bean);
        nextLinkUrl = handleLinks(dataName,resData,list);
        
        while(!StringUtils.isEmpty(nextLinkUrl)){
        	String linkResData = getLinks(bean,nextLinkUrl);
        	
        	nextLinkUrl = handleLinks(dataName,linkResData,list);
        }
        
        return list;
    }

    /**
     * 处理分页数据及下一页LINK
     * @param dataName
     * @param resData
     * @param list
     * @return
     */
    private String handleLinks(String dataName , String resData, List<JSONObject> list){
    	String nextLinkUrl = null;
    	String nextLink = dataName+"_links";
    	
    	JSONObject data = JSONObject.parseObject(resData);
        JSONArray linksArray = data.getJSONArray(nextLink);
        if(null != linksArray && linksArray.size() == 1){
        	JSONObject link1 = linksArray.getJSONObject(0);
        	if(null != link1){
        		nextLinkUrl = link1.getString("href");
        	}
        }
        
        JSONArray array = data.getJSONArray(dataName);
        if (array != null && array.size() > 0) {
        	if(list == null ){
        		list = new ArrayList<JSONObject>();
        	}
            for (int i = 0; i < array.size(); i++) {
                list.add(array.getJSONObject(i));
            }
        }
        
        return nextLinkUrl;
    }
    
    public JSONObject getById(RestTokenBean bean, String url, String dataName, String id)
                                                                                         throws AppException {
        bean.setUrl(url + id);
        String resData = get(bean);
        JSONObject data = JSONObject.parseObject(resData);
        if (null != data && null != dataName) {
            data = data.getJSONObject(dataName);
        }
        return data;
    }

    /**
     * 云业务rest请求的获取详情方法
     * 
     * @throws Exception
     */
    public JSONObject get(RestTokenBean bean, String dataName) throws AppException {
        String resData = get(bean);
        JSONObject data = JSONObject.parseObject(resData);
        if (data != null && dataName != null) {
            return data.getJSONObject(dataName);
        } else if (data != null) {
            return data;
        }
        return null;
    }

    public JSONArray getJsonArray(RestTokenBean bean) throws AppException {
        String resData = get(bean);
        JSONArray result = JSONArray.parseArray(resData);

        return result;
    }

    public JSONObject create(RestTokenBean bean, String dataName, JSONObject data)
                                                                                  throws AppException {
        JSONObject response = post(bean, data);
        JSONObject result = null;
        if (dataName != null) {
            result = response.getJSONObject(dataName);
            return result;
        }
        return response;
    }
    
    public JSONObject create(RestTokenBean bean, String dataName,  net.sf.json.JSONObject data)
            throws AppException {
        JSONObject response = post(bean, data);
        JSONObject result = null;
        if (dataName != null) {
            result = response.getJSONObject(dataName);
            return result;
        }
        return response;
    }

    public JSONObject update(RestTokenBean bean, String dataName, JSONObject data)
                                                                                  throws AppException {
        JSONObject response = put(bean, data);
        if (dataName == null || "".equals(dataName)) {
            return response;
        }
        JSONObject result = response.getJSONObject(dataName);
        return result;
    }
    
    @Override
	public JSONObject updateByNetJson(RestTokenBean bean,String dataName, net.sf.json.JSONObject data)throws AppException {
    	 JSONObject response = putByNetJson(bean, data);
         if (dataName == null || "".equals(dataName)) {
             return response;
         }
         JSONObject result = response.getJSONObject(dataName);
         return result;
	}

   

	/**
     * rest 调用的delete方法，用于删除记录
     * 
     * @return
     * @throws AppException
     */
    public boolean delete(RestTokenBean bean) throws AppException {
        if (StringUtil.isEmpty(bean.getTokenId())) {
            bean = getToken(bean);
        }
        HttpClient httpclient = HttpClients.createDefault();
        HttpDelete delete = new HttpDelete(bean.getEndpoint()+bean.getUrl());
        log.info("The current URL is:" + bean.getEndpoint()+bean.getUrl());
        String resData = null;
        JSONObject resJson = null;
        int response = 0;
        try {
            delete.setHeader("Content-Type", "application/json");
            delete.setHeader("Accept", "application/json");
            if (bean.getTokenId() != null) {
                delete.setHeader("X-Auth-Token", bean.getTokenId());
            }
            HttpResponse res = httpclient.execute(delete);
            log.info("HttpResponse=" + res);
            response = res.getStatusLine().getStatusCode();
            HttpEntity resEntity = res.getEntity();
            if (resEntity != null) {
                resData = EntityUtils.toString(res.getEntity());
                if (resData != null) {
                    resJson = JSONObject.parseObject(resData);
                }
            }
            log.info("The response Json=" + resJson);
            this.newException(response, resJson);
        } catch (AppException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException("error.openstack.message", new String[]{response+","+e.getMessage()});
        }
        return true;
    }

    public JSONObject operate(RestTokenBean bean, JSONObject data) throws AppException {
        JSONObject response = post(bean, data);
        return response;
    }

    /**
     * rest调用的post方法，用于新增记录
     * 
     * @param jsonresult
     * @return
     * @throws AppException
     */
    public JSONObject patch(RestTokenBean bean, String jsonresult) throws AppException {
        if (StringUtil.isEmpty(bean.getTokenId())) {
            bean = getToken(bean);
        }
        HttpClient httpclient = HttpClients.createDefault();
        HttpPatch patch = new HttpPatch(bean.getEndpoint()+bean.getUrl());
        log.info("The current URL is:" +bean.getEndpoint()+ bean.getUrl());
        log.info("The requestbody is:" + jsonresult);
        JSONObject resJson = null;
        String resData = null;
        int response = 0;
        try {
            StringEntity s = new StringEntity(jsonresult.toString(), ContentType.create(
                "application/json", "utf-8"));
            patch.setEntity(s);
            if(OpenstackUriConstant.IMAGE_SERVICE_URI.equals(bean.getServiceName())){
            	patch.addHeader("Accept", "application/openstack-images-v2.1-json-patch");
            	patch.addHeader("Content-Type",
            			"application/openstack-images-v2.1-json-patch;charset=utf-8");
            }
            else {
            	patch.addHeader("Accept", "application/json");
            	patch.addHeader("Content-Type","application/json");
            }
            // rest请求设置token
            if (bean.getTokenId() != null) {
                patch.addHeader("X-Auth-Token", bean.getTokenId());
            }
            // 执行post请求并获取响应
            HttpResponse res = httpclient.execute(patch);
            // 获取返回状态码
            response = res.getStatusLine().getStatusCode();
            HttpEntity resEntity = res.getEntity();
            if (resEntity != null) {
                resData = EntityUtils.toString(res.getEntity());
                if (resData != null) {
                    resJson = JSONObject.parseObject(resData);
                }
            }
            log.info("The response Json=" + resJson);
            this.newException(response, resJson);
        } catch (AppException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException("error.openstack.message", new String[]{response+","+e.getMessage()});
        }
        return resJson;
    }

    /**
     * 云业务rest请求的更新指定id记录的方法
     */
    public JSONObject extend(RestTokenBean bean, String dataName) throws AppException {
        JSONObject response = put(bean, null);
        JSONObject result = response.getJSONObject(dataName);
        return result;
    }

    /**
     * rest调用的post方法，用于新增记录
     * 
     * @param json
     * @return
     * @throws AppException
     */
    public JSONObject post(RestTokenBean bean, JSONObject json) throws AppException {
        if (StringUtil.isEmpty(bean.getTokenId())) {
            bean = getToken(bean);
        }
        HttpClient httpclient = HttpClients.createDefault();
        log.info("The current URL is:" + bean.getEndpoint()+bean.getUrl());
        log.info("The requestbody is:" + json);
        HttpPost post = new HttpPost(bean.getEndpoint()+bean.getUrl());
        JSONObject resJson = null;
        String resData = null;
        int response = 0;
        try {
            StringEntity s = new StringEntity(json.toString(), ContentType.create(
                "application/json", "utf-8"));
            post.setEntity(s);
            post.addHeader("Accept", "application/json");
            post.addHeader("Content-Type", "application/json;charset=utf-8");
            // rest请求设置token
            if (bean.getTokenId() != null) {
                post.addHeader("X-Auth-Token", bean.getTokenId());
            }
            // 执行post请求并获取响应
            HttpResponse res = httpclient.execute(post);
            // 获取返回状态码
            response = res.getStatusLine().getStatusCode();
            HttpEntity resEntity = res.getEntity();
            if (resEntity != null) {
                resData = EntityUtils.toString(res.getEntity());
                log.info("返回数据："+resData);
                if (resData != null) {
                    resJson = JSONObject.parseObject(resData);
                }
            }
            log.info("The response Json=" + resJson);
            this.newException(response, resJson);
        } catch (AppException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException("error.openstack.message", new String[]{response+","+e.getMessage()});
        }
        return resJson;
    }
    
    public JSONObject post(RestTokenBean bean,  net.sf.json.JSONObject json) throws AppException {
        if (StringUtil.isEmpty(bean.getTokenId())) {
            bean = getToken(bean);
        }
        HttpClient httpclient = HttpClients.createDefault();
        log.info("The current URL is:" + bean.getEndpoint()+bean.getUrl());
        log.info("The requestbody is:" + json);
        HttpPost post = new HttpPost(bean.getEndpoint()+bean.getUrl());
        JSONObject resJson = null;
        String resData = null;
        int response = 0;
        try {
            StringEntity s = new StringEntity(json.toString(), ContentType.create(
                    "application/json", "utf-8"));
            post.setEntity(s);
            post.addHeader("Accept", "application/json");
            post.addHeader("Content-Type", "application/json;charset=utf-8");
            // rest请求设置token
            if (bean.getTokenId() != null) {
                post.addHeader("X-Auth-Token", bean.getTokenId());
            }
            // 执行post请求并获取响应
            HttpResponse res = httpclient.execute(post);
            // 获取返回状态码
            response = res.getStatusLine().getStatusCode();
            HttpEntity resEntity = res.getEntity();
            if (resEntity != null) {
                resData = EntityUtils.toString(res.getEntity());
                if (resData != null) {
                    resJson = JSONObject.parseObject(resData);
                }
            }
            log.info("The response Json=" + resJson);
            this.newException(response, resJson);
        } catch (AppException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException("error.openstack.message", new String[]{response+","+e.getMessage()});
        }
        return resJson;
    }
    /**
     * rest调用的post方法，用于新增记录
     * 
     * @param json
     * @return
     * @throws AppException
     */
    public JSONArray postJSONArray(RestTokenBean bean, JSONObject json) throws AppException {
        if (StringUtil.isEmpty(bean.getTokenId())) {
            bean = getToken(bean);
        }
        HttpClient httpclient = HttpClients.createDefault();
        log.info("The current URL is:" + bean.getEndpoint()+bean.getUrl());
        log.info("The requestbody is:" + json);
        HttpPost post = new HttpPost(bean.getEndpoint()+bean.getUrl());
        JSONArray resJson = null;
        String resData = null;
        int response = 0;
        try {
            StringEntity s = new StringEntity(json.toString(), ContentType.create(
                "application/json", "utf-8"));
            post.setEntity(s);
            post.addHeader("Accept", "application/json");
            post.addHeader("Content-Type", "application/json;charset=utf-8");
            // rest请求设置token
            if (bean.getTokenId() != null) {
                post.addHeader("X-Auth-Token", bean.getTokenId());
            }
            // 执行post请求并获取响应
            HttpResponse res = httpclient.execute(post);
            // 获取返回状态码
            response = res.getStatusLine().getStatusCode();
            HttpEntity resEntity = res.getEntity();
            if (resEntity != null) {
                resData = EntityUtils.toString(res.getEntity());
                if (resData != null) {
                    resJson = JSONArray.parseArray(resData);
                }
            }
            log.info("The response Json=" + resJson);
            this.newException(response, resJson);
        } catch (AppException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException("error.openstack.message", new String[]{response+","+e.getMessage()});
        }
        return resJson;
    }

    /**
     * rest调用的post方法，用于新增记录
     * 
     * @param json
     * @return
     * @throws AppException
     */
    public JSONObject postWithoutToken(String url, JSONObject json) throws AppException {
        HttpClient httpclient = HttpClients.createDefault();
        log.info("The current URL is:" + url);
        log.info("The requestbody is:" + json);
        HttpPost post = new HttpPost(url + "/tokens");
        JSONObject resJson = null;
        String resData = null;
        int response = 0;
        try {
            StringEntity s = new StringEntity(json.toString(), ContentType.create(
                "application/json", "utf-8"));
            post.setEntity(s);
            post.addHeader("Accept", "application/json");
            post.addHeader("Content-Type", "application/json;charset=utf-8");
            // 执行post请求并获取响应
            HttpResponse res = httpclient.execute(post);
            // 获取返回状态码
            response = res.getStatusLine().getStatusCode();
            HttpEntity resEntity = res.getEntity();
            if (resEntity != null) {
                resData = EntityUtils.toString(res.getEntity());
                if (resData != null) {
                    resJson = JSONObject.parseObject(resData);
                }
            }
            log.info("The response Json=" + resJson);
            this.newException(response, resJson);
        } catch (AppException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException("error.openstack.message", new String[]{response+","+e.getMessage()});
        }
        return resJson;
    }

    /**
     * rest调用的get方法，用户获取记录信息
     * 
     * @return
     * @throws AppException
     */
    private String get(RestTokenBean bean) throws AppException {
        if (StringUtil.isEmpty(bean.getTokenId())) {
            bean = getToken(bean);
        }
        HttpClient httpclient = HttpClients.createDefault();
        HttpGet get = new HttpGet(bean.getEndpoint()+bean.getUrl());
        log.info("The current URL is:" + bean.getEndpoint()+bean.getUrl());
        JSONObject resJson = null;
        String resData = null;
        int response = 0;
        try {
            get.setHeader("Content-Type", "application/json");
            get.setHeader("Accept", "application/json");
            if (bean.getTokenId() != null) {
                get.setHeader("X-Auth-Token", bean.getTokenId());
            }
            HttpResponse res = httpclient.execute(get);
            response = res.getStatusLine().getStatusCode();
            HttpEntity resEntity = res.getEntity();
            if (resEntity != null) {
                resData = EntityUtils.toString(resEntity);
                log.info("resData:"+resData);
            }
            this.newException(response, resJson);
        } catch (AppException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException("error.openstack.message", new String[]{response+","+e.getMessage()});
        }
        return resData;
    }
    
    /**
     * rest调用的get方法，获取分页数据
     * 
     * @return
     * @throws AppException
     */
    private String getLinks(RestTokenBean bean,String links) throws AppException {
    	if (StringUtil.isEmpty(bean.getTokenId())) {
    		bean = getToken(bean);
    	}
    	HttpClient httpclient = HttpClients.createDefault();
    	HttpGet get = new HttpGet(links);
    	log.info("The current URL is:" + links);
    	JSONObject resJson = null;
    	String resData = null;
    	int response = 0;
    	try {
    		get.setHeader("Content-Type", "application/json");
    		get.setHeader("Accept", "application/json");
    		if (bean.getTokenId() != null) {
    			get.setHeader("X-Auth-Token", bean.getTokenId());
    		}
    		HttpResponse res = httpclient.execute(get);
    		response = res.getStatusLine().getStatusCode();
    		HttpEntity resEntity = res.getEntity();
    		if (resEntity != null) {
    			resData = EntityUtils.toString(resEntity);
    			log.info("resData:"+resData);
    		}
    		this.newException(response, resJson);
    	} catch (AppException e) {
    	    log.error(e.getMessage(), e);
    		throw e;
    	} catch (Exception e) {
    	    log.error(e.getMessage(), e);
    		throw new AppException("error.openstack.message", new String[]{response+","+e.getMessage()});
    	}
    	return resData;
    }

    /**
     * rest 调用的put方法，用于修改记录
     * 
     * @param object
     * @return
     * @throws AppException
     */
    public JSONObject put(RestTokenBean bean, JSONObject object) throws AppException {
        if (StringUtil.isEmpty(bean.getTokenId())) {
            bean = getToken(bean);
        }
        HttpClient httpclient = HttpClients.createDefault();
        HttpPut put = new HttpPut(bean.getEndpoint()+bean.getUrl());
        log.info("The current URL is:" + bean.getEndpoint()+bean.getUrl());
        log.info("The requestbody is:" + object);
        JSONObject resJson = null;
        String resData = null;
        int response = 0;
        try {
            if (bean.getTokenId() != null) {
                put.addHeader("X-Auth-Token", bean.getTokenId());
            }
            if (object != null) {
                StringEntity s = new StringEntity(object.toString(), ContentType.create(
                    "application/json", "utf-8"));
                put.setEntity(s);
                put.addHeader("Accept", "application/json");
                put.addHeader("Content-Type", "application/json;charset=utf-8");
            }
            HttpResponse res = httpclient.execute(put);
            response = res.getStatusLine().getStatusCode();
            HttpEntity resEntity = res.getEntity();
            if (resEntity != null) {
                resData = EntityUtils.toString(res.getEntity());
                if (resData != null) {
                    resJson = JSONObject.parseObject(resData);
                }
            }
            log.info("The response Json=" + resJson);
            this.newException(response, resJson);
        } catch (AppException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException("error.openstack.message", new String[]{response+","+e.getMessage()});
        }
        return resJson;
    }
    
    private JSONObject putByNetJson(RestTokenBean bean,
			net.sf.json.JSONObject object) {
    	if (StringUtil.isEmpty(bean.getTokenId())) {
            bean = getToken(bean);
        }
        HttpClient httpclient = HttpClients.createDefault();
        HttpPut put = new HttpPut(bean.getEndpoint()+bean.getUrl());
        log.info("The current URL is:" + bean.getEndpoint()+bean.getUrl());
        log.info("The requestbody is:" + object);
        JSONObject resJson = null;
        String resData = null;
        int response = 0;
        try {
            if (bean.getTokenId() != null) {
                put.addHeader("X-Auth-Token", bean.getTokenId());
            }
            if (object != null) {
                StringEntity s = new StringEntity(object.toString(), ContentType.create(
                    "application/json", "utf-8"));
                put.setEntity(s);
                put.addHeader("Accept", "application/json");
                put.addHeader("Content-Type", "application/json;charset=utf-8");
            }
            HttpResponse res = httpclient.execute(put);
            response = res.getStatusLine().getStatusCode();
            HttpEntity resEntity = res.getEntity();
            if (resEntity != null) {
                resData = EntityUtils.toString(res.getEntity());
                if (resData != null) {
                    resJson = JSONObject.parseObject(resData);
                }
            }
            log.info("The response Json=" + resJson);
            this.newException(response, resJson);
        } catch (AppException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException("error.openstack.message", new String[]{response+","+e.getMessage()});
        }
        return resJson;
	}

    /**
     * rest调用的post方法，重载方法，用于获取token
     * 
     * @param url
     * @return
     * @throws AppException
     * setConnectTimeout：设置连接超时时间，单位毫秒。
     * setConnectionRequestTimeout：设置从connect Manager获取Connection 超时时间，单位毫秒。这个属性是新加的属性，因为是可以共享连接池的。
     * setSocketTimeout：请求获取数据的超时时间，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
     */
	private JSONObject getToken(JSONObject json, String url) throws AppException {
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
            HttpEntity resEntity = res.getEntity();
            if (resEntity != null) {
                resData = EntityUtils.toString(res.getEntity());
                if (resData != null) {
                    resJson = JSONObject.parseObject(resData);
                }
            }
            log.info("The response Json=" + resJson);
            this.newException(response, resJson);
        } catch (AppException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException("error.openstack.message", new String[]{response+","+e.getMessage()});
        }
        return resJson;
    }

    /**
     * 从缓存中获取token，若缓存中数据不存在，调用rest接口从底层获得
     * 
     * @param restTokenBean
     * @return
     * @throws AppException
     */
    public synchronized RestTokenBean getToken(RestTokenBean restTokenBean) throws AppException {
        // 先在缓存中判断token是否存在，若不存在，再从OpenStack中获取
        boolean find = false;
        for (RestTokenBean bean : tokenSet) {
            if (bean.equals(restTokenBean)) {
            	String url=restTokenBean.getUrl();
            	BeanUtils.copyPropertiesByModel(restTokenBean, bean);
                restTokenBean.setUrl(url);
                find = true;
                break;
            }
        }
        Date now = null;
        if (find) {
            now = new Date();
            if (now.getTime() - restTokenBean.getLastGetTime() < HALF_HOUR) {// 30分钟内
                return restTokenBean;
            } else { // 超时，移除
                tokenSet.remove(restTokenBean);
            }
        }

        // 设置鉴权信息
        JSONObject jsonObject0 = new JSONObject();

        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("username", restTokenBean.getUserName());
        jsonObject1.put("password", restTokenBean.getPassword());

        jsonObject0.put("passwordCredentials", jsonObject1);
        jsonObject0.put("tenantId", restTokenBean.getTenantId());

        JSONObject auth = new JSONObject();
        auth.put("auth", jsonObject0);
        // 发送请求，获取token返回值
        JSONObject access = getToken(auth, restTokenBean.getGetTokenUrl());
        // 获取tokenId
        String tokenId = (((JSONObject) ((JSONObject) access.get("access")).get("token")).get("id"))
            .toString();
        JSONArray array = (JSONArray) ((JSONObject) access.get("access")).get("serviceCatalog");
        String endpoint = null;
        // 获取指定服务名的endpoint
        for (Object object : array) {
            JSONObject data = (JSONObject) object;
            // 如果是keystone服务，获取adminURL
            boolean isKeystone = OpenstackUriConstant.IDENTITY_SERVICE_URI.equals(restTokenBean
                .getServiceName());
            boolean isThisService = data.get("type").equals(restTokenBean.getServiceName());
            // 当前请求的为keystone服务时，获取keyStoneRegion对应的adminURL
            if (isKeystone && isThisService) {
                JSONArray dataArray = (JSONArray) data.get("endpoints");
                for (Object object2 : dataArray) {
                    JSONObject json = (JSONObject) object2;
                    if (json.getString("region").equals(restTokenBean.getKeyStoneRegion())) {
                        endpoint = json.getString(OpenstackUriConstant.ADMIN_URL_TYPE);
                        break;
                    }
                }
            }
            // 非keystone组件，获取commonRegion的publicURL
            else if (isThisService) {
                JSONArray dataArray = (JSONArray) data.get("endpoints");
                for (Object object2 : dataArray) {
                    JSONObject json = (JSONObject) object2;
                    if (json.getString("region").equals(restTokenBean.getCommonRegion())) {
                    	String urlType = OpenstackUriConstant.PUBLIC_URL_TYPE;
                    	if(!StringUtil.isEmpty(restTokenBean.getCommonRegionUrlType()) ){
                    		urlType = restTokenBean.getCommonRegionUrlType();
                    	}
                        endpoint = json.getString(urlType);
                        break;
                    }
                }
            }
            if (endpoint != null) {
                break;
            }
        }
        restTokenBean.setTokenId(tokenId);
        now = new Date();
        restTokenBean.setLastGetTime(now.getTime());
        restTokenBean.setEndpoint(endpoint);
        tokenSet.add(restTokenBean);

        return restTokenBean;
    }

    /**
     * 判断http请求的返回状态码，如需要，抛出相关异常信息
     * 
     * @param response
     * @param resJson
     * @throws AppException
     */
    private void newException(int response, JSONObject resJson) throws AppException {
        if (response >= 300) {
            StringBuilder message = null;
            if (resJson != null) {
                message = new StringBuilder();
                Map map = (Map) resJson;
                Iterator iterator = map.keySet().iterator();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    JSONObject value = (JSONObject) JSONObject.toJSON(map.get(key));
                    message.append(key).append("-->").append(value.getString("message"));
                }
            }
            String info = message == null ? "操作openstack平台时出错，请检查！" : message.toString();
            throw new AppException("error.openstack.message", new String[] { info });
        }
    }
    
    /**
     * 判断http请求的返回状态码，如需要，抛出相关异常信息
     * 
     * @param response
     * @param resJson
     * @throws AppException
     */
    private void newException(int response, JSONArray resJson) throws AppException {
        if (response >= 300) {
            StringBuilder message = null;
            if (resJson != null) {
                message = new StringBuilder();
                Map map = (Map) resJson;
                Iterator iterator = map.keySet().iterator();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    JSONObject value = (JSONObject) JSONObject.toJSON(map.get(key));
                    message.append(key).append("-->").append(value.getString("message"));
                }
            }
            String info = message == null ? "操作openstack平台时出错，请检查！" : message.toString();
            throw new AppException("error.openstack.message", new String[] { info });
        }
    }
    
    /**
     * rest调用的getJSON方法
     * 
     * @return
     * @throws AppException
     */
    public JSONObject getJSONById(RestTokenBean bean , String url, String id) throws Exception {
    	JSONObject json = null;
    	bean.setUrl(url + id);
        if (StringUtil.isEmpty(bean.getTokenId())) {
            bean = getToken(bean);
        }
        HttpClient httpclient = HttpClients.createDefault();
        HttpGet get = new HttpGet(bean.getEndpoint()+bean.getUrl());
        log.info("The current URL is:" + bean.getEndpoint()+bean.getUrl());
        String resData = null;
        try {
            get.setHeader("Content-Type", "application/json");
            get.setHeader("Accept", "application/json");
            if (bean.getTokenId() != null) {
                get.setHeader("X-Auth-Token", bean.getTokenId());
            }
            HttpResponse res = httpclient.execute(get);
            HttpEntity resEntity = res.getEntity();
            if (resEntity != null) {
                resData = EntityUtils.toString(resEntity);
            }
            json = JSONObject.parseObject(resData);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        return json;
    }
    
    /**
     * rest调用的getJSON方法
     * 
     * @return
     * @throws AppException
     */
    public String getStringById(RestTokenBean bean , String url, String id) throws Exception {
    	if (StringUtil.isEmpty(bean.getTokenId())) {
    		bean = getToken(bean);
    	}
    	HttpClient httpclient = HttpClients.createDefault();
    	HttpGet get = new HttpGet(bean.getEndpoint()+url + id);
    	log.info("The current URL is:" + bean.getEndpoint()+bean.getUrl());
    	String resData = null;
    	try {
    		get.setHeader("Content-Type", "application/json");
    		get.setHeader("Accept", "application/json");
    		if (bean.getTokenId() != null) {
    			get.setHeader("X-Auth-Token", bean.getTokenId());
    		}
    		HttpResponse res = httpclient.execute(get);
    		HttpEntity resEntity = res.getEntity();
    		if (resEntity != null) {
    			resData = EntityUtils.toString(resEntity);
    		}
    	} catch (Exception e) {
    	    log.error(e.getMessage(), e);
    		throw e;
    	}
    	return resData;
    }
    
    public JSONObject getResponse(RestTokenBean bean){
        String resData = get(bean);
        JSONObject data = JSONObject.parseObject(resData);
        return data;
    }
	
}
