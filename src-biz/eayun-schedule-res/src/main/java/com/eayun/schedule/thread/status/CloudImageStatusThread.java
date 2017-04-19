package com.eayun.schedule.thread.status;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.schedule.service.CloudImageService;
import com.eayun.virtualization.model.CloudImage;

public class CloudImageStatusThread implements Runnable{
	private static final Logger log = LoggerFactory.getLogger(CloudImageStatusThread.class);
	private CloudImageService cloudImageService ;
	
	public CloudImageStatusThread (CloudImageService cloudImageService){
		this.cloudImageService = cloudImageService;
	}

	@Override
	public void run() {
		String value = null ;
		boolean isSync = false;
		try{
			value = cloudImageService.pop(RedisKey.imageKey);
			JSONObject valueJson = JSONObject.parseObject(value);
			CloudImage cloudImage = JSON.parseObject(value, CloudImage.class);
			if(null!=value){
				log.info("从自定义镜像队列中取出："+value);
				JSONObject json = cloudImageService.get(valueJson);
				log.info("底层返回JSON:"+json);
				String stackStatus = json.getString("status");
				if (!StringUtils.isEmpty(stackStatus)) {
					stackStatus = stackStatus.toUpperCase();
				}
				if ("true".equals(json.getString("deletingStatus"))){
					isSync = true;
					cloudImageService.deleteImage(cloudImage);
				}
				else if(!StringUtils.isEmpty(stackStatus)){
					stackStatus=stackStatus.toUpperCase();
					if(!stackStatus.equals(cloudImage.getImageStatus())){
						if(!"SAVING".equals(stackStatus)&&!"QUEUED".equals(stackStatus)){
							cloudImage.setImageStatus(stackStatus);
							long size = json.getLongValue("OS-EXT-IMG-SIZE:size");
							if(size>0){
								cloudImage.setImageSize(new BigDecimal(size));
							}
							isSync = true ;
							cloudImageService.updateImage(cloudImage);
						}
					}
				}
				if(isSync){
					log.info("自定义镜像ID："+cloudImage.getImageId()+"状态刷新成功，移除任务调度！");
				}
				else {
					int count = cloudImage.getCount();
					if(count>30){
						log.info("自定义镜像ID："+cloudImage.getImageId()+"已执行"+count+"次状态未刷新，移除任务调度！");
					}else{
						valueJson.put("count", count+1);
						log.info("自定义镜像ID："+cloudImage.getImageId()+"状态未刷新，等待下次调度！");
						cloudImageService.push(RedisKey.imageKey, valueJson.toJSONString());
					}
				}
			}
		}
		catch(Exception e){
		    log.error(e.getMessage(),e);
			if(null!= value ){
				cloudImageService.push(RedisKey.imageKey, value);
			}
		}
		
	}
	
	
}
