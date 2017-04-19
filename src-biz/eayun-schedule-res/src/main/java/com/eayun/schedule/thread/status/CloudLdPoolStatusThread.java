package com.eayun.schedule.thread.status;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.schedule.service.CloudLdPoolService;
import com.eayun.virtualization.model.CloudLdPool;

public class CloudLdPoolStatusThread implements Runnable{
	private static final Logger log = LoggerFactory.getLogger(CloudLdPoolStatusThread.class);
	private CloudLdPoolService cloudLdPoolService ;
	
	public CloudLdPoolStatusThread (CloudLdPoolService cloudLdPoolService){
		this.cloudLdPoolService = cloudLdPoolService;
	}

	@Override
	public void run() {
		String value = null ;
		boolean isSync = false;
		try{
			value = cloudLdPoolService.pop(RedisKey.ldPoolKey);
			JSONObject valueJson = JSONObject.parseObject(value);
			CloudLdPool cloudLdpool = JSON.parseObject(value, CloudLdPool.class);
			if(null!=value){
				log.info("从负载均衡资源池队列中取出："+value);
				JSONObject json = cloudLdPoolService.get(valueJson);
				log.info("底层返回JSON:"+json);
				String stackStatus = json.getString("status");
				if ("true".equals(json.getString("deletingStatus"))){
					isSync = true;
					cloudLdPoolService.deletePool(cloudLdpool);
				}
				if (!StringUtils.isEmpty(stackStatus)) {
					stackStatus = stackStatus.toUpperCase();
					if(!stackStatus.equals(cloudLdpool.getPoolStatus())){
						cloudLdpool.setPoolStatus(stackStatus);
						isSync = true;
						
						cloudLdPoolService.updateLdp(cloudLdpool);
					}
				}
				if(isSync){
					log.info("负载均衡资源池ID："+cloudLdpool.getPoolId()+"状态刷新成功，移除任务调度！");
				}
				else {
					int count = cloudLdpool.getCount();
					if(count>100){
						log.info("负载均衡资源池ID："+cloudLdpool.getPoolId()+"已执行"+count+"次状态未刷新，移除任务调度！");
					}else{
						valueJson.put("count", count+1);
						log.info("负载均衡资源池ID："+cloudLdpool.getPoolId()+"状态未刷新，等待下次调度！");
						cloudLdPoolService.push(RedisKey.ldPoolKey, valueJson.toJSONString());
					}
				}
			}
			
		}
		catch(Exception e){
			if(null!= value ){
				cloudLdPoolService.push(RedisKey.ldPoolKey, value);
			}
			log.error(e.getMessage(), e);
		}
		
	}
	
	
}
