package com.eayun.obs.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.eayun.obs.model.BaseCdnBucket;
import com.eayun.obs.model.BucketStorageBean;
import com.eayun.obs.model.CdnBucket;

public interface ObsCdnBucketService {
	
	/**
	 * 开启CDN服务
	 * @Author: duanbinbin
	 * @param bucketName
	 * @param cusId
	 * @param cdnProvider
	 *<li>Date: 2016年6月16日</li>
	 */
	public JSONObject enableDomain(String bucketName , String cusId, String cdnProvider,String userName) throws Exception;
	
	/**
	 * 关闭CDN服务
	 * @Author: duanbinbin
	 * @param bucketName
	 * @param cusId
	 * @param cdnProvider
	 *<li>Date: 2016年6月16日</li>
	 */
	public JSONObject disableDomain(String bucketName , String cusId, String cdnProvider,String userName) throws Exception;
	
	/**
	 * 添加CDN日志
	 * @Author: duanbinbin
	 * @param bucketName	bucket名称
	 * @param doamin		域名标识
	 * @param url			接口URL
	 * @param operationCdn	CDN接口操作
	 * @param requestBody	接口请求体，包含业务参数信息
	 * @param statuscode		请求状态
	 * @param result			返回json，包含result（成功true，失败false）和message（失败详情）
	 *<li>Date: 2016年6月16日</li>
	 */
	public void addCdnLog(String bucketName,String doamin,String url,String operationCdn ,
			JSONObject requestBody ,int statuscode,JSONObject result);
	
	/**
	 * 添加DNS日志
	 * @Author: duanbinbin
	 * @param bucketName		bucket名称
	 * @param domain			域名标识
	 * @param url				DNS接口 URL（包含了业务参数）
	 * @param operationDns		DNS接口操作
	 * @param responseBody		所有原生返回信息（里面code为1时，表示请求成功）
	 * @param recordId			DNS记录
	 *<li>Date: 2016年6月16日</li>
	 */
	public void addDnsLog(String bucketName,String domain,String url,
			String operationDns ,JSONObject responseBody ,String recordId);
	
	/**
	 * 查询bucket的CDN下载流量
	 * @Author: duanbinbin
	 * @param bucketName
	 * @return
	 *<li>Date: 2016年6月16日</li>
	 */
	public List<BucketStorageBean> getCDNFlowData(String bucketName,String cusId,String cdnProvider);
	
	/**
	 * 查询客户所有未删除的关联记录
	 * @Author: duanbinbin
	 * @param cusId
	 * @return
	 *<li>Date: 2016年6月16日</li>
	 */
	public List<CdnBucket> getUnDeleteListByCusId(String cusId);
	
	/**
	 * 查询指定客户下指定bucket是否有未删除的关联记录，有则返回
	 * @Author: duanbinbin
	 * @param bucketName
	 * @param cusId
	 * @return
	 *<li>Date: 2016年6月16日</li>
	 */
	public CdnBucket getUnDeleteByCusAndName(String bucketName , String cusId , String cdnProvider);
	
	/**
	 * 查询所有开通过CDN服务（或两个小时内关闭）的关联记录，用于统计流量信息
	 * @Author: duanbinbin
	 * @return
	 *<li>Date: 2016年6月20日</li>
	 */
	public List<CdnBucket> getListForFlowData();

	/**
	 * 修改关联记录
	 * @Author: duanbinbin
	 * @param baseCdnBucket
	 *<li>Date: 2016年6月21日</li>
	 */
	public void update(BaseCdnBucket baseCdnBucket);
	
	/**
	 * 查询某bucket是否开启了CDN，用于获取URL
	 * @Author: duanbinbin
	 * @param bucketName
	 * @return
	 *<li>Date: 2016年6月22日</li>
	 */
	public CdnBucket getOpenByName(String bucketName);
	
	/**
	 * 根据domainId找到一条未删除的记录，用于删除bucket的计划任务
	 * @Author: duanbinbin
	 * @param domain
	 * @return
	 *<li>Date: 2016年7月8日</li>
	 */
	public CdnBucket getDeleteByDomain(String domain);

	/**
	 * 查询客户所有开通过CDN服务（或两个小时内关闭）的关联记录，用于统计客户回源流量信息
	 * @param cusId
	 * @return
	 */
	public List<CdnBucket> getListForBackByCusId(String cusId);
	/**
	 * 客户两个时间点内的纯下载流量
	 * @param cusId
	 * @param start
	 * @param end
	 * @return
	 */
	public double getBacksourceByCusId(String cusId,Date start,Date end);

    /**
     * 获取客户指定时间范围内的CDN用量（CDN下载流量、动态请求数和HTTPS请求数）
     * @param cusId
     * @param chargeFrom
     * @param chargeTo
     * @return
     */
    Map<String,Object> getCdnDetail(String cusId, Date chargeFrom, Date chargeTo);
}
