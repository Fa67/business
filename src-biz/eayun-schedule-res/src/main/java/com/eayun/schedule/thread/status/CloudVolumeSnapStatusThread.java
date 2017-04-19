package com.eayun.schedule.thread.status;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.schedule.service.CloudVolumeSnapService;
import com.eayun.virtualization.model.CloudSnapshot;

public class CloudVolumeSnapStatusThread implements Runnable{
	private static final Logger log = LoggerFactory.getLogger(CloudVolumeSnapStatusThread.class);
	private CloudVolumeSnapService cloudVolumeSnapService ;
	
	public CloudVolumeSnapStatusThread (CloudVolumeSnapService cloudVolumeSnapService){
		this.cloudVolumeSnapService = cloudVolumeSnapService;
	}

	@Override
	public void run() {
		String value = null ;
		boolean isSync = false;
		try{
			value = cloudVolumeSnapService.pop(RedisKey.volSphKey);
			JSONObject valueJson = JSONObject.parseObject(value);
			CloudSnapshot cloudSnapshot = JSON.parseObject(value, CloudSnapshot.class);
			if(null!=value){
				log.info("从云硬盘备份队列中取出："+value);
				JSONObject json = cloudVolumeSnapService.get(valueJson);
				log.info("底层返回JSON:"+json);
				String stackStatus = json.getString("status");
				if (!StringUtils.isEmpty(stackStatus)) {
					stackStatus = stackStatus.toUpperCase();
				}
				if ("true".equals(json.getString("deletingStatus"))){
					isSync = true;
					cloudVolumeSnapService.deleteVolSnap(cloudSnapshot);
				}
				else if (!StringUtils.isEmpty(stackStatus)) {
					if(!stackStatus.equals(cloudSnapshot.getSnapStatus())){
						
						if("CREATING".equals(cloudSnapshot.getSnapStatus().toUpperCase())){
							cloudSnapshot.setSnapStatus(stackStatus);
							isSync=cloudVolumeSnapService.syncSnapshotInBuild(cloudSnapshot);
						}else{
							cloudSnapshot.setSnapStatus(stackStatus);
							isSync = true;
							cloudVolumeSnapService.updateVolSnap(cloudSnapshot);
						}
						
					}
				}
				if(isSync){
					log.info("云硬盘备份ID："+cloudSnapshot.getSnapId()+"状态刷新成功，移除任务调度！");
				}
				else {
					int count = cloudSnapshot.getCount();
					if(count>720){
						log.info("云硬盘备份ID："+cloudSnapshot.getSnapId()+"已执行"+count+"次状态未刷新，移除任务调度！");
					}else{
						valueJson.put("count", count+1);
						log.info("云硬盘备份ID："+cloudSnapshot.getSnapId()+"状态未刷新，等待下次调度！");
						cloudVolumeSnapService.push(RedisKey.volSphKey, valueJson.toJSONString());
					}
				}
			}
			
		}catch(Exception e){
		    log.error(e.getMessage(),e);
			if(null!= value ){
				cloudVolumeSnapService.push(RedisKey.volSphKey, value);
			}
		}
		
	}
	
	
}
