/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.pay.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eayun.common.dao.IRepository;
import com.eayun.pay.model.BasePayRecord;

/**
 *                       
 * @Filename: PayRecordDao.java
 * @Description: 
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2016年7月28日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public interface PayRecordDao extends IRepository<BasePayRecord, String> {

    @Modifying
    @Query(value = "insert into pay_orderrecord(trade_no,order_no) values(?,?)", nativeQuery = true)
    public int savePayOrderRecord(String tradeNo, String orderNo);
    
    public BasePayRecord findByTradeNo(String tradeNo);
    
    @Query(value = "select order_no from pay_orderrecord where trade_no = ?", nativeQuery = true)
    public List<String> findOrderNo(String tradeNo);
    
    @Query(value = "select trade_no from pay_orderrecord where order_no in(:orderNoList)", nativeQuery = true)
    public List<String> findTradeNoByOrderNoIn(@Param("orderNoList") List<String> orderNoList);
    
    public List<BasePayRecord> findByTradeNoIn(List<String> tradeNoList);
    
    @Query("from BasePayRecord pr where pr.payType != '0' and pr.payStatus = ?")
    public List<BasePayRecord>  findThirdPartPayRecordByPayStatus(String payStatus);
}
