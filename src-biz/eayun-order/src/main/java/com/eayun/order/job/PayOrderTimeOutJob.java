package com.eayun.order.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.order.service.OrderService;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class PayOrderTimeOutJob  extends BaseQuartzJobBean{
	@Autowired
	private OrderService orderService;

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		ApplicationContext applicationContext = getApplicationContext(context);
		orderService = applicationContext.getBean(OrderService.class);
		orderService.updatePayExpireOrder();
	}
	

}
