package com.eayun.schedule.thread.status;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.schedule.service.CloudFirewallService;
import com.eayun.virtualization.model.CloudFireWall;

public class CloudFirewallStatusThread implements Runnable{
	private static final Logger log = LoggerFactory.getLogger(CloudFirewallStatusThread.class);
	private CloudFirewallService cloudFirewallService ;
	
	public CloudFirewallStatusThread (CloudFirewallService cloudFirewallService){
		this.cloudFirewallService = cloudFirewallService;
	}

	@Override
	public void run() {
		String value = null ;
		boolean isSync = false;
		try{
			value = cloudFirewallService.pop(RedisKey.fwKey);
			JSONObject valueJson = JSONObject.parseObject(value);
			CloudFireWall cloudFw = JSON.parseObject(value, CloudFireWall.class);
			if(null!=value){
				log.info("从防火墙队列中取出："+value);
				JSONObject json = cloudFirewallService.get(valueJson);
				log.info("底层返回JSON:"+json);
				String stackStatus = json.getString("status");
				if ("true".equals(json.getString("deletingStatus"))){
					isSync = true;
					cloudFirewallService.deleteFw(cloudFw);
				}
				if (!StringUtils.isEmpty(stackStatus)) {
					stackStatus = stackStatus.toUpperCase();
					if(!stackStatus.equals(cloudFw.getFwStatus())){
						cloudFw.setFwStatus(stackStatus);
						cloudFirewallService.updateFw(cloudFw);
						isSync = true;
					}
				}
				if(isSync){
					log.info("防火墙ID："+cloudFw.getFwId()+"状态刷新成功，移除任务调度！");
				}
				else {
					int count = cloudFw.getCount();
					if(count>100){
						log.info("防火墙ID："+cloudFw.getFwId()+"已执行"+count+"次状态未刷新，移除任务调度！");
					}else{
						valueJson.put("count", count+1);
						log.info("防火墙ID："+cloudFw.getFwId()+"状态未刷新，等待下次调度！");
						cloudFirewallService.push(RedisKey.fwKey, valueJson.toJSONString());
					}
				}
			}
			
		}
		catch(Exception e){
		    log.error(e.getMessage(),e);
			if(null!= value ){
				cloudFirewallService.push(RedisKey.fwKey, value);
			}
		}
		
	}
	
	
}
