package com.eayun.obs.ecmcservice;

import java.util.Date;
import java.util.List;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.obs.ecmcmodel.EcmcObsEchartsBean;
import com.eayun.obs.model.ObsBucket;
import com.eayun.obs.model.ObsUsedType;

/**
 * EcmcObsCustomerService
 * 
 * @Filename: EcmcObsCustomerService.java
 * @Description:
 * @Version: 1.0
 * @Author: liyanchao
 * @Email: yanchao.li@eayun.com
 * @History:<br> <li>Date: 2016年4月1日</li> <li>Version: 1.1</li>
 */
public interface EcmcObsCustomerService {

	/**
	 * 根据客户Id得到所有属于他的bucket
	 * 
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public List<ObsBucket> getBucketsByUserId(String userId) throws Exception;

	/**
	 * 获取指定客户本月使用量
	 * 
	 * @param cusId
	 * @return
	 */
	public ObsUsedType getObsInMonthUsed(String cusId) throws Exception;

	/**
	 * 获取客户历史账单,分页
	 * 
	 * @param cusId
	 * @return
	 */
	public Page getObsHistoryResources(Page page, QueryMap queryMap,String cusId) throws Exception;
	/**
	 * 调用obs底层，获取用户配额
	 * @param customer
	 * @return
	 * @throws Exception
	 */
	public ObsUsedType getQuota(String customer) throws Exception;
	
	/**
	 * 设置用户配额
	 * @param customer
	 * @param storage
	 * @param flow
	 * @param requestCount
	 * @return
	 * @throws Exception
	 */
	public String setQuota(String cusId, String storage, String flow,String requestCount) throws Exception;
	
	/**
     * 对象存储客户详情--资源详情--折线图
     */
	public EcmcObsEchartsBean getObsUsedView(String bucketName,String cusId,String type, Date startTime, Date endTime)throws Exception;
	/**
	 * 获取资源统计表格数据
	 * 
	 * @param start
	 * @param end
	 * @param cusId
	 * @return
	 */
	public Page getObsResources(Page page, QueryMap queryMap,Date startTime, Date endTime,
			String cusId) throws Exception;
}
