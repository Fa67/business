package com.eayun.dashboard.api.service;

import java.util.Date;
import java.util.List;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.customer.model.Customer;
import com.eayun.dashboard.api.bean.ApiIndexDetail;

public interface ApiOverviewService {

	/**
	 * API概览指标折线图
	 * @param cusId
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public List<ApiIndexDetail> getApiOverviewDetails(String cusId, Date startTime,
			Date endTime);

	/**
	 * API概览指标列表
	 * @param page
	 * @param queryMap
	 * @param cusId
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Page getApiDetailsPage(Page page, QueryMap queryMap, String cusId,
			Date startTime, Date endTime);

	/**
	 * 查询已申请密钥的客户列表
	 * @param cusOrg
	 * @return
	 */
	public List<Customer> getCusListByOrg(String cusOrg);

}
