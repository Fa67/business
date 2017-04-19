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
import com.eayun.syssetup.service.SysDataTreeService;
import com.eayun.virtualization.service.ResourceDisposeService;
import com.eayun.virtualization.thread.ResourceExceedNoticePool;
import com.eayun.virtualization.thread.ResourceExceedNoticeThread;
/**
* 资源过期处理(超过保留时长)发送消息通知JOB
* @author xiangyu.cao@eayun.com
*
*/
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ResourceExceedNoticeJob extends BaseQuartzJobBean{
	private final Logger log = LoggerFactory
			.getLogger(ResourceExceedNoticeJob.class);
	private CustomerService customerService;
	private MessageCenterService messageCenterService;
	private ResourceDisposeService resourceDispostService;
	private ProjectService projectService;
	private SysDataTreeService sysDataTreeService;
	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {
		log.info("开始进行包年包月资源到期(已到期)发送消息处理");
		ApplicationContext applicationContext = getApplicationContext(context);
		customerService = applicationContext.getBean(CustomerService.class);
		messageCenterService = applicationContext
				.getBean(MessageCenterService.class);
		resourceDispostService = applicationContext
				.getBean(ResourceDisposeService.class);
		projectService = applicationContext.getBean(ProjectService.class);
		sysDataTreeService=applicationContext.getBean(SysDataTreeService.class);
		try {
			List<Customer> list = customerService.findNotFreeze();
			for (Customer customer : list) {
				String cusId = customer.getCusId();
				log.info("开始进行包年包月资源到期(已超过保留时长)发送消息,cusId:"+cusId);
				ResourceExceedNoticeThread thread = new ResourceExceedNoticeThread(
						cusId, messageCenterService, resourceDispostService,
						projectService,sysDataTreeService);
				ResourceExceedNoticePool.pool.submit(thread);
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
	}
}
