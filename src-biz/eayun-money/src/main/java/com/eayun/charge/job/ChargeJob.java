package com.eayun.charge.job;

import com.eayun.charge.pool.ChargeThreadPool;
import com.eayun.charge.service.ChargeRecordService;
import com.eayun.charge.service.ChargeService;
import com.eayun.charge.thread.ChargeThread;
import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.common.template.EayunRabbitTemplate;
import com.eayun.costcenter.service.AccountOverviewService;
import com.eayun.customer.model.BaseCustomer;
import com.eayun.customer.serivce.CustomerService;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.syssetup.service.SysDataTreeService;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

/**
 * 后付费资源（除OBS外）计费计划任务
 *
 * @Filename: ChargeJob.java
 * @Description:
 * @Version: 1.0
 * @Author: zhangfan
 * @Email: fan.zhang@eayun.com
 * @History:<br> <li>Date: 2016年8月2日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ChargeJob extends BaseQuartzJobBean{

    private static final Logger log = LoggerFactory.getLogger(ChargeJob.class);
    private CustomerService customerService;
    private ChargeRecordService chargeRecordService;
    private AccountOverviewService accountOverviewService;
    private EayunRabbitTemplate rabbitTemplate;
    private ChargeService chargeService;
    private MessageCenterService msgCenterService;
    private SysDataTreeService sysDataTreeService;
    private MongoTemplate mongoTemplate;

    @SuppressWarnings("rawtypes")
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("后付费资源（除OBS外）计费计划任务开始");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH");
        //1.获取客户列表
        ApplicationContext applicationContext = getApplicationContext(context);
        customerService = applicationContext.getBean(CustomerService.class);
        chargeRecordService =  applicationContext.getBean(ChargeRecordService.class);
        accountOverviewService =  applicationContext.getBean(AccountOverviewService.class);
        rabbitTemplate = applicationContext.getBean(EayunRabbitTemplate.class);
        chargeService = applicationContext.getBean(ChargeService.class);
        msgCenterService = applicationContext.getBean(MessageCenterService.class);
        sysDataTreeService = applicationContext.getBean(SysDataTreeService.class);
        mongoTemplate = applicationContext.getBean(MongoTemplate.class);

        List<BaseCustomer> customerList = customerService.getAllCustomerList();
        //2.针对每个客户，起一个线程，执行客户的后付费资源计费任务
        Date currentTime = null;
        try {
            currentTime = format.parse(format.format(new Date()));
        } catch (ParseException e) {
            log.error("日期格式化失败", e);
        }
        List<Future> futureList = new ArrayList<>();
        for (BaseCustomer customer: customerList) {
            String cusId = customer.getCusId();
            ChargeThread thread = new ChargeThread(rabbitTemplate, accountOverviewService, customerService, chargeRecordService, chargeService, msgCenterService,sysDataTreeService, mongoTemplate, cusId, currentTime);
            Future f = ChargeThreadPool.pool.submit(thread);
            futureList.add(f);
        }

        log.info(new Date() + "线程提交线程池完毕，开始监控异步任务完成情况");
        while(true){
            for(Future f: new ArrayList<>(futureList)){
                if(f.isDone()){
                    //isDone返回true的场景：正常结束、异常、线程被取消
                    futureList.remove(f);
                }
            }

            if(futureList.isEmpty()){
                break;
            }
            try {
                Thread.sleep(50);//sleep a while，give cpu a break
            } catch (InterruptedException e) {
                log.error("线程休眠50ms异常",e);
            }
        }
        log.info("后付费资源（除OBS外）计费计划任务结束");
    }
}
