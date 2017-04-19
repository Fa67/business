package com.eayun.eayunstack.service;

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.util.RestTokenBean;

public interface RestService {

    /**
     * rest请求的获取列表方法
     * 
     * @param bean
     * @param dataName
     * @return
     * @throws AppException
     */
    public List<JSONObject> list(RestTokenBean bean, String dataName) throws AppException;

    /**
     * 根据id获得信息
     * 
     * @param bean
     * @param dataName
     * @param id
     * @return
     * @throws AppException
     */
    public JSONObject getById(RestTokenBean bean, String url, String dataName, String id)
                                                                                         throws AppException;

    public JSONObject get(RestTokenBean bean, String dataName) throws AppException;

    public JSONArray getJsonArray(RestTokenBean bean) throws AppException;
    
    public JSONObject create(RestTokenBean bean, String dataName, JSONObject data)
                                                                                  throws AppException;
    public JSONObject create(RestTokenBean bean, String dataName,  net.sf.json.JSONObject data)
            throws AppException;

    public JSONObject update(RestTokenBean bean, String dataName, JSONObject data)
                                                                                  throws AppException;
    
    public JSONObject updateByNetJson(RestTokenBean restTokenBean,String dataName, net.sf.json.JSONObject data)throws AppException;

    public boolean delete(RestTokenBean bean) throws AppException;

    public JSONObject operate(RestTokenBean bean, JSONObject data) throws AppException;

    public JSONObject patch(RestTokenBean bean, String jsonresult) throws AppException;

    public JSONObject put(RestTokenBean bean, JSONObject object) throws AppException;

    /**
     * json对象和java对象转换
     * 
     * @param jSONObject
     * @param clazz
     * @return
     */
    public <T> T json2bean(JSONObject jSONObject, Class<T> clazz);

    public RestTokenBean getToken(RestTokenBean restTokenBean) throws AppException;

    public JSONObject extend(RestTokenBean bean, String dataName) throws AppException;

    public JSONObject post(RestTokenBean bean, JSONObject json) throws AppException;
    
    public JSONArray postJSONArray(RestTokenBean bean, JSONObject json) throws AppException;

    public JSONObject postWithoutToken(String url, JSONObject json) throws AppException;
    
    public JSONObject getJSONById(RestTokenBean bean , String url, String id) throws Exception;
    
    public String getStringById(RestTokenBean bean , String url, String id) throws Exception;
    
    public JSONObject getResponse(RestTokenBean bean);

	

}
