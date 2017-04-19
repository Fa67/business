package com.eayun.apiverify.service;

import com.alibaba.fastjson.JSONObject;

public interface AuthorizationService {
	/**
	 * API鉴权
	 * @param params  请求参数
	 * @param contentType 请求中的content-type
	 * @param ip  请求ip地址
	 * @param version  请求版本号
	 * @return
	 * @throws Exception
	 */
	public JSONObject checkAuthorization(JSONObject params ,String contentType ,String ip ,String version) throws Exception ;
}
