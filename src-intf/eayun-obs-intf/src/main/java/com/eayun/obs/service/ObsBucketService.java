package com.eayun.obs.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.eayun.accesskey.model.AccessKey;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.obs.model.BucketStorageBean;
import com.eayun.obs.model.BucketUesdAndRequestBean;
import com.eayun.obs.model.ObsBucket;

/**
 * ObsBucketService
 * 
 * @Filename: ObsBucketService.java
 * @Description:
 * @Version: 1.0
 * @Author: liyanchao
 * @Email: yanchao.li@eayun.com
 * @History:<br> <li>Date: 2016年1月11日</li> <li>Version: 1.0</li> <li>Content:
 *               create</li>
 */
public interface ObsBucketService {
	
	/**
	 * 获取Bucket分页列表信息
	 * 
	 * @param name
	 *            前端页面输入的查询内容，用于过滤不匹配的记录
	 * @return
	 * @throws Exception
	 */
	public Page getBucketPageList(Page page, String name, QueryMap queryMap,AccessKey accessKeyObj,String bucketName)throws Exception;
	
	
	/**
	 * 获取BucketList
	 * @param name
	 * @return BucketList
	 * @throws Exception
	 */
	public List<ObsBucket> getBucketList(AccessKey accessKeyObj) throws Exception;
	
	public JSONObject addBucket(AccessKey accessKeyObj, Map<String,String> map) throws Exception;
	/**
	 * 校验Bucket名称唯一
	 * 
	 * @param name
	 *           
	 * @return boolean
	 * @throws Exception
	 */
	public boolean checkBucketName( String bucketName, AccessKey accessKeyObj) throws Exception;
	/**
	 * 获取Bucket列表信息
	 * @param name
	 *            前端页面输入的查询内容，用于过滤不匹配的记录
	 * @return
	 * @throws Exception
	 */
	public JSONObject deleteBucket(String bucketName, AccessKey accessKeyObj) throws Exception;
	
	
	/**
	 * 获取Bucket权限
	 * 
	 * @param bucketList
	 *            
	 * @return bucketListAcl
	 * @throws Exception
	 */
	public List<ObsBucket> getBucketAclList(List<ObsBucket> obsList,AccessKey accessKeyObj ) throws Exception;
	/**
	 * 修改Bucket
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public boolean editBucket(AccessKey accessKeyObj, Map<String,String> map) throws Exception;
	/**
	 * Bucket总存储量
	 * @param request
	 * @param idStr
	 * @return
	 */
	public List<BucketStorageBean> getBucketStorage(String cusId , String bucketName) throws Exception;
	/**
	 * Bucket流量请求次数或流入流出流量
	 * @param request
	 * @param idStr
	 * @return
	 */
	public List<BucketUesdAndRequestBean> getBucketUsedAndRequest(String cusId , String bucketName,String type) throws Exception;
	/**
	 * 获取BucketAclList列表
	 * @param AccessKey
	 * @return
	 */
	public List<ObsBucket> bucketAclList(AccessKey accessKeyObj) throws Exception;
}
