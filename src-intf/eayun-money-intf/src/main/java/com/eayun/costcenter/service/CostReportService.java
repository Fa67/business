package com.eayun.costcenter.service;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.costcenter.model.MoneyRecord;
import com.eayun.order.model.Order;

public interface CostReportService {
	/**
	 * 查询费用报表信息(分页)
	 * @param page
	 * @param searchType	1为按账期搜索 2为按计费时间搜索
	 * @param monMonth		账期
	 * @param beginTime		计费时间开始时间
	 * @param endTime		计费时间截止时间
	 * @param type			1为预付费  2为后付费
	 * @param productName	产品名
	 * @param resourceName	资源名称
	 * @param cusId			客户id
	 * @param queryMap		分页信息
	 * @return
	 * @throws Exception
	 */
	public Page getReportListPage(Page page,String searchType,String monMonth,Date beginTime,Date endTime,String type,String productName,String resourceName,String cusId,QueryMap queryMap) throws Exception;
	
	/**
	 * 获取计费时间范围内的总计金额
	 * @param searchType	1为按账期搜索 2为按计费时间搜索
	 * @param monMonth		账期
	 * @param beginTime		计费时间开始时间
	 * @param endTime		计费时间截止时间
	 * @param type			1为预付费  2为后付费
	 * @param productName	产品名
	 * @param resourceName	资源名称
	 * @param cusId			客户id
	 * @return
	 * @throws Exception
	 */
	public String getTotalCost(String searchType, String monMonth,
			Date beginTime, Date endTime, String type, String productName,
			String resourceName, String cusId) throws Exception;
	/**
	 * 获取后付费费用报表列表(list)
	 * @param type
	 * @param searchType
	 * @param beginTime
	 * @param endTime
	 * @param monMonth
	 * @param productName
	 * @param resourceName
	 * @param cusId
	 * @return
	 */
	public List<MoneyRecord> queryPostPay(String type, String searchType,
			Date beginTime, Date endTime, String monMonth,
			String productName, String resourceName, String cusId) throws Exception;
	/**
	 * 获取后付费费用报表详情
	 * @param id
	 * @return
	 */
	public MoneyRecord getPostpayDetail(String id);

	public void exportPostPayExcel(OutputStream os, String type,
			String searchType, Date begin, Date end, String monMonth,
			String productName, String resourceName, String cusId) throws Exception;

	public void exportPrepaymentExcel(OutputStream outputStream,
			String type, Date begin, Date end, String productName, String cusId) throws Exception;
	/**
	 * 修改预付费费用报表状态
	 * @param orderNo 订单编号
	 * @return
	 * @throws Exception
	 */
	public MoneyRecord changePrepaymentState(String orderNo) throws Exception;

	/**
	 * 查询指定客户的所有欠费的计费记录
	 * @param cusId
	 * @return
     */
	List<MoneyRecord> getArrearsListByCusId(String cusId) throws Exception;

	void updateMoneyRecord(MoneyRecord moneyRecord) throws Exception;
	
	/**
	 * 获取预付费费用报表详情
	 * @param orderNo
	 * @return
	 * @throws Exception
	 */
	public Order getPrepaymentDetails(String orderNo) throws Exception;
	/**
	 * 该订单编号是否属于这个客户
	 * @param orderNo
	 * @return	true为属于这个客户
	 * @throws Exception
	 */
	public boolean orderIsBelong(String orderNo) throws Exception;
	
	/**
	 * 获取obs/cdn 在指定时间范围内费用总和
	 * @param cusId
	 * @param begin
	 * @param end
	 * @param type obs/cdn
	 * @return
	 * @throws Exception
	 */
	public BigDecimal getCostForObs(String cusId,Date begin, Date end, String type) throws Exception;
}
