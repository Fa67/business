package com.eayun.costcenter.service;

import java.math.BigDecimal;

import com.eayun.costcenter.bean.RecordBean;

public interface ChangeBalanceService {
	public BigDecimal changeBalanceByCharge(final RecordBean recordBean);
	
	public BigDecimal changeBalance(final RecordBean recordBean);
	
	public BigDecimal changeBalanceByPay(final RecordBean recordBean) throws Exception;
	
	/**
	 * 只针对第三方支付时,充值与消费
	 * @param recordBean
	 * @return
	 * @throws Exception
	 */
	public BigDecimal changeBalanceForThird(final RecordBean recordBean);
}
