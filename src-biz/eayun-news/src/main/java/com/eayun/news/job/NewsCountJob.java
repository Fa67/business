package com.eayun.news.job;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.eayun.common.constant.RedisKey;
import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.news.service.NewsCountService;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class NewsCountJob extends BaseQuartzJobBean {
    private static final Logger log = LoggerFactory.getLogger(NewsCountJob.class);
    
	private NewsCountService newsCountService;
    
    
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
    	ApplicationContext applicationContext = getApplicationContext(context);
    	newsCountService = applicationContext.getBean(NewsCountService.class);
    	//该for循环控制内存中每次执行MailSendJob计划任务时创建的线程数量
    	try{
    		Map<String,Integer> statuMap = new HashMap<String,Integer>();
    		Map<String,Integer> collectMap = new HashMap<String,Integer>();
    		Map<String,Integer> uncollectMap = new HashMap<String,Integer>();
	        for(int i = 0;i < 10;i++){
	        	String statu = newsCountService.pop(RedisKey.MESSAGE_STATUS_QUEUE);
	        	String collect = newsCountService.pop(RedisKey.MESSAGE_COLLECT_QUEUE);
	        	String uncollect = newsCountService.pop(RedisKey.MESSAGE_UNCOLLECT_QUEUE);
	        	if(statu == null && collect == null && uncollect == null){
	        		break;
	        	}
	        	if(statu != null){
		        	if(statuMap.containsKey(statu)){
		        		statuMap.put(statu, (statuMap.get(statu) + 1));
		        	} else {
		        		statuMap.put(statu, 1);
		        	}
	        	}
	        	if(collect != null){
		        	if(collectMap.containsKey(collect)){
		        		collectMap.put(collect, (collectMap.get(collect) + 1));
		        	} else {
		        		collectMap.put(collect, 1);
		        	}
	        	}
	        	if(uncollect != null){
		        	if(uncollectMap.containsKey(uncollect)){
		        		uncollectMap.put(uncollect, (uncollectMap.get(uncollect) + 1));
		        	} else {
		        		uncollectMap.put(uncollect, 1);
		        	}
	        	}
	        }
	        for(String newsId:statuMap.keySet()){
	        	newsCountService.updateStatu(newsId,statuMap.get(newsId));
	        }
	        Set<String> collectIds = newsCountService.unionSet(collectMap.keySet(),uncollectMap.keySet());
	        for(String newsId:collectIds){
	        	int countCollect = 0;
	        	int countUncollect = 0;
	        	if(collectMap.containsKey(newsId)){
	        		countCollect = collectMap.get(newsId);
	        	}
	        	if(uncollectMap.containsKey(newsId)){
	        		countUncollect = uncollectMap.get(newsId);
	        	}
	        	int countResult = countCollect - countUncollect;
	        	newsCountService.updateCollect(newsId,countResult);
	        }
    	}catch (Exception e){
    	    log.error(e.getMessage(),e);
    	}
    }
}
