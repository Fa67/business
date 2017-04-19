package com.eayun.virtualization.job;

import java.util.List;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.customer.model.Customer;
import com.eayun.customer.serivce.CustomerService;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.project.service.ProjectService;
import com.eayun.virtualization.service.ResourceDisposeService;
import com.eayun.virtualization.thread.ContinuationNoticePool;
import com.eayun.virtualization.thread.ContinuationNoticeThread;
/**
 * 资源即将到期发送消息JOB
 * @author xiangyu.cao@eayun.com
 *
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ContinuationNoticeJob extends BaseQuartzJobBean {
	private final Logger log = LoggerFactory
			.getLogger(ContinuationNoticeJob.class);
	private CustomerService customerService;
	private MessageCenterService messageCenterService;
	private ResourceDisposeService resourceDispostService;
	private ProjectService projectService;

	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {
		log.info("开始进行资源即将到期发送消息处理");
		ApplicationContext applicationContext = getApplicationContext(context);
		customerService = applicationContext.getBean(CustomerService.class);
		messageCenterService = applicationContext
				.getBean(MessageCenterService.class);
		resourceDispostService = applicationContext
				.getBean(ResourceDisposeService.class);
		projectService = applicationContext.getBean(ProjectService.class);
		try {
			List<Customer> list = customerService.findNotFreeze();
			for (Customer customer : list) {
				String cusId = customer.getCusId();
				log.info("开始进行资源即将到期发送消息处理,cusId:"+cusId);
				ContinuationNoticeThread thread = new ContinuationNoticeThread(
						cusId, messageCenterService, resourceDispostService,
						projectService);
				ContinuationNoticePool.pool.submit(thread);
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
	}

}
