package com.eayun.obs.base.service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.obs.model.ObsAccessBean;
import com.eayun.obs.model.ObsResultBean;

public interface ObsBaseService {

	/**
	 * 查询 列表  
	 * @param ObsAccessBean
	 * @return JSONObject
	 * @throws Exception
	 */
	public ObsResultBean get(ObsAccessBean obsBean) throws Exception;
	/**
	 * 创建类    
	 * @param ObsAccessBean
	 * @return JSONObject
	 * @throws Exception
	 */
	public JSONObject put(ObsAccessBean obsBean) throws Exception;
	
	/**
	 * 删除bucket    
	 * @param ObsAccessBean
	 * @return JSONObject
	 * @throws Exception
	 */
	public JSONObject deleteBucket(ObsAccessBean obsBean) throws Exception;
	
	public String deleteAccessKey(ObsAccessBean obsBean) throws Exception;
	/**
	 * 为bucket设置CORS属性使之允许跨源请求
	 * @param accessKey
	 * @param secretKey
	 * @param bucketName
	 * @throws Exception
	 */
	public void setBucketCORS(String accessKey ,String secretKey ,String bucketName) throws Exception;
	
}
