package com.eayun.schedule.thread.status;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.schedule.service.CloudLdVipService;
import com.eayun.virtualization.model.CloudLdVip;

public class CloudLdVipStatusThread implements Runnable{
	private static final Logger log = LoggerFactory.getLogger(CloudLdVipStatusThread.class);
	private CloudLdVipService cloudLdVipService ;
	
	public CloudLdVipStatusThread (CloudLdVipService cloudLdVipService){
		this.cloudLdVipService = cloudLdVipService;
	}

	@Override
	public void run() {
		String value = null ;
		boolean isSync = false;
		try{
			value = cloudLdVipService.pop(RedisKey.ldVipKey);
			JSONObject valueJson = JSONObject.parseObject(value);
			CloudLdVip cloudVip = JSON.parseObject(value, CloudLdVip.class);
			if(null!=value){
				log.info("从负载均衡VIP队列中取出："+value);
				JSONObject json = cloudLdVipService.get(valueJson);
				log.info("底层返回JSON:"+json);
				String stackStatus = json.getString("status");
				if ("true".equals(json.getString("deletingStatus"))){
					isSync = true;
					cloudLdVipService.deleteVip(cloudVip);
				}
				if (!StringUtils.isEmpty(stackStatus)) {
					stackStatus = stackStatus.toUpperCase();
					if(!stackStatus.equals(cloudVip.getVipStatus())){
						cloudVip.setVipStatus(stackStatus);
						cloudLdVipService.updateLdv(cloudVip);
						isSync = true;
					}
				}
				if(isSync){
					log.info("负载均衡VIP ID："+cloudVip.getVipId()+"状态刷新成功，移除任务调度！");
				}
				else {
					int count = cloudVip.getCount();
					if(count>100){
						log.info("负载均衡VIP ID："+cloudVip.getVipId()+"已执行"+count+"次状态未刷新，移除任务调度！");
					}else{
						valueJson.put("count", count+1);
						log.info("负载均衡VIP ID："+cloudVip.getVipId()+"状态未刷新，等待下次调度！");
						cloudLdVipService.push(RedisKey.ldVipKey, valueJson.toJSONString());
					}
				}
			}
			
		}
		catch(Exception e){
		    log.error(e.getMessage(),e);
			if(null!= value ){
				cloudLdVipService.push(RedisKey.ldVipKey, value);
			}
		}
		
	}
	
	
}
