package com.eayun.obs.job.cdn;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.eayun.common.template.EayunRabbitTemplate;
import com.eayun.obs.service.ObsGetAllUsersService;
import com.eayun.obs.thread.backsource.CdnBacksourceDetailGatherPool;
import com.eayun.obs.thread.backsource.CdnBacksourceDetailGatherThread;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.eayun.cdn.impl.UpYunCDN;
import com.eayun.cdn.intf.CDN;
import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.obs.model.CdnBucket;
import com.eayun.obs.service.ObsCdnBucketService;
import com.eayun.obs.thread.cdn.CdnDetailGatherPool;
import com.eayun.obs.thread.cdn.CdnDetailGatherThread;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class CdnDetailGatherJob extends BaseQuartzJobBean {

    private final Logger log = LoggerFactory.getLogger(CdnDetailGatherJob.class);

    private MongoTemplate mongoTemplate;

    private EayunRabbitTemplate eayunRabbitTemplate;

    private ObsCdnBucketService obsCdnBucketService;

    private ObsGetAllUsersService obsGetAllUsersService;

    private CDN cdn;

    @Override
    protected void executeInternal(JobExecutionContext context)
            throws JobExecutionException {
        log.info("开始采集每小时CDN下载流量、CDN动态请求数和CDN-HTTPS请求数");
        ApplicationContext applicationContext = getApplicationContext(context);
        cdn = applicationContext.getBean(UpYunCDN.class);
        mongoTemplate = (MongoTemplate) applicationContext.getBean("mongoTemplate");
        obsCdnBucketService = applicationContext.getBean(ObsCdnBucketService.class);
        eayunRabbitTemplate = applicationContext.getBean(EayunRabbitTemplate.class);
        obsGetAllUsersService = applicationContext.getBean(ObsGetAllUsersService.class);
//		obsCdnBucketService = (ObsCdnBucketService) applicationContext.getBean(ObsCdnBucketService.class);
//
//		Date now=new Date();
//		List<CdnBucket> cdnBucketList = obsCdnBucketService.getListForFlowData();
//		for(CdnBucket cdnBucket : cdnBucketList){
//			CdnDetailGatherThread thread = new CdnDetailGatherThread(mongoTemplate,cdn,cdnBucket,now);
//			CdnDetailGatherPool.pool.submit(thread);
//		}

        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        now = calendar.getTime();

        // FIXME: 2016/12/1 将采集修改为客户的维度

        List<String> obsUsers = null;
        try {
            obsUsers = obsGetAllUsersService.getObsAllUsers();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        if (obsUsers != null) {
            for (String cusId : obsUsers) {
                CdnDetailGatherThread thread = new CdnDetailGatherThread(cdn, mongoTemplate, obsCdnBucketService, eayunRabbitTemplate, cusId, now);
                CdnBacksourceDetailGatherPool.pool.submit(thread);
            }
        }

    }

}
