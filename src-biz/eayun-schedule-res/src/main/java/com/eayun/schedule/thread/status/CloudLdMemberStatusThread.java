package com.eayun.schedule.thread.status;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.schedule.service.CloudLdMemberService;
import com.eayun.virtualization.model.CloudLdMember;

public class CloudLdMemberStatusThread implements Runnable{
	private static final Logger log = LoggerFactory.getLogger(CloudLdMemberStatusThread.class);
	private CloudLdMemberService cloudLdMemberService ;
	
	public CloudLdMemberStatusThread (CloudLdMemberService cloudLdMemberService){
		this.cloudLdMemberService = cloudLdMemberService;
	}

	@Override
	public void run() {
		String value = null ;
		boolean isSync = false;
		try{
			value = cloudLdMemberService.pop(RedisKey.ldMemberKey);
			JSONObject valueJson = JSONObject.parseObject(value);
			CloudLdMember cloudLdm = JSON.parseObject(value, CloudLdMember.class);
			if(null!=value){
				log.info("从负载均衡成员队列中取出："+value);
				JSONObject json = cloudLdMemberService.get(valueJson);
				log.info("底层返回JSON:"+json);
				String stackStatus = json.getString("status");
				if ("true".equals(json.getString("deletingStatus"))){
					isSync = true;
					cloudLdMemberService.deleteMember(cloudLdm);
				}
				if (!StringUtils.isEmpty(stackStatus)) {
					stackStatus = stackStatus.toUpperCase();
					if(!stackStatus.equals(cloudLdm.getMemberStatus())){
						cloudLdm.setMemberStatus(stackStatus);
						isSync = true;
						
						cloudLdMemberService.updateMember(cloudLdm);
					}
				}
				
				if(isSync){
					log.info("负载均衡成员ID："+cloudLdm.getMemberId()+"状态刷新成功，移除任务调度！");
				}
				else {
					int count = cloudLdm.getCount();
					if(count>100){
						log.info("负载均衡成员ID："+cloudLdm.getMemberId()+"已执行"+count+"次状态未刷新，移除任务调度！");
					}else{
						valueJson.put("count", count+1);
						log.info("负载均衡成员ID："+cloudLdm.getMemberId()+"状态未刷新，等待下次调度！");
						cloudLdMemberService.push(RedisKey.ldMemberKey, valueJson.toJSONString());
					}
				}
			}
			
		}
		catch(Exception e){
		    log.error(e.getMessage(),e);
			if(null!= value ){
				cloudLdMemberService.push(RedisKey.ldMemberKey, value);
			}
		}
		
	}
	
	
}
