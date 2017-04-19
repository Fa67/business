package com.eayun.virtualization.thread;

import com.alibaba.fastjson.JSONObject;

public interface CdnService {

	/**
	 * 添加CDN日志
	 * @param bucketName		bucket名称（我们底层的bucket名称）
	 * @param url				接口URL
	 * @param operationType		业务操作类型
	 * @param operationCdn		CDN接口操作
	 * @param requestBody		接口请求体，包含业务参数信息
	 * @param statuscode		请求状态
	 * @param result			返回json，包含result（成功true，失败false）和message（失败详情）
	 *<li>Date: 2016年6月14日</li>
	 */
	public void addCdnLog(String bucketName,String url,String operationType ,String operationCdn ,
			JSONObject requestBody ,int statuscode,JSONObject result);
	/**
	 * @param bucketName		我们生成的用于CDN交互的bucket_name,即domainId。（原网宿时，cdn接口返回的id）
	 * @param url				DNS接口 URL（包含了业务参数）
	 * @param operationType		业务操作类型
	 * @param operationDns		DNS接口操作
	 * @param responseBody		所有原生返回信息（里面code为1时，表示请求成功）
	 * @param recordId			DNS记录的id
	 *<li>Date: 2016年6月14日</li>
	 */
	public void addDnsLog(String bucketName,String url,String operationType ,String operationDns ,
			JSONObject responseBody ,String recordId);
}
