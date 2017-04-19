package com.eayun.order.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eayun.common.dao.IRepository;
import com.eayun.order.model.BaseOrder;

public interface OrderDao extends IRepository<BaseOrder, String> {
	
	@Query("from BaseOrder where orderNo in (:ordersNo)")
	public List<BaseOrder> findByOrdersNo(@Param("ordersNo")List<String> ordersNo);
	
	@Query("select sum(thirdPartPayment) from BaseOrder where orderNo in (:ordersNo)")
	public BigDecimal getThirdPartPayment(@Param("ordersNo")List<String> ordersNo);
	
	public BaseOrder findByOrderNo(String orderNo);

}
