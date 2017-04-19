package com.eayun.costcenter.service;

import java.util.Date;
import java.util.List;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.costcenter.bean.ExcelRecord;
import com.eayun.costcenter.model.MoneyAccount;
import com.eayun.costcenter.model.MoneyRecord;

public interface AccountOverviewService {
	
	/**
	 * 修改账户余额(按时扣费)
	 * @param recordBean
	 * @return
	 * @throws Exception
	 */
	public MoneyAccount changeBalanceByCharge(MoneyRecord moneyRecord) throws Exception;
	
	/**
	 * 修改账户余额(使用余额支付,购买/续费/升级)
	 * @param recordBean
	 * @return
	 * @throws Exception
	 */
	public MoneyAccount changeBalanceByPay(MoneyRecord moneyRecord) throws Exception;
	/**
	 * 获取账户信息(三位小数)
	 * @param cusId
	 * @return
	 * @throws Exception
	 */
	public MoneyAccount getAccountInfo(String cusId) throws Exception;
	/**\
	 * 获取账户余额信息(两位小数)
	 * @param cusId
	 * @return
	 * @throws Exception
	 */
	public MoneyAccount getAccountBalance(String cusId) throws Exception;
	/**
	 * 获取交易记录列表(分页)
	 * @param page
	 * @param cusId
	 * @param beginTime
	 * @param endTime
	 * @param queryMap
	 * @return
	 * @throws Exception
	 */
	public Page getRecordPage(Page page, String cusId, Date beginTime, Date endTime,String incomeType,QueryMap queryMap) throws Exception;
	/**
	 * 获取交易记录列表
	 * @param incomeType 
	 * @param begin
	 * @param end
	 * @param cusId
	 * @param isEcmc 是否是ecmc的请求
	 * @return
	 */
	public List<ExcelRecord> queryRecordExcel(String incomeType, Date begin, Date end, String cusId, boolean isEcmc) throws Exception;
	
	/**
	 * 这个订单是否已经退过款 
	 * @param orderNo
	 * @return true已退过
	 * @throws Exception
	 */
	public boolean thisOrderIsRefunded(String orderNo) throws Exception;
	
	/**
	 * 只针对第三方支付订单时使用
	 * @param moneyRecord
	 * @return
	 * @throws Exception
	 */
	public MoneyAccount changeBalanceForThird(MoneyRecord moneyRecord) throws Exception;
	
}
