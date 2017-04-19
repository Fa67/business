package com.eayun.schedule.thread.status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.schedule.service.CloudVmAttVolService;
import com.eayun.virtualization.model.CloudVm;

public class CloudVmAttVolStatusThread implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(CloudVmAttVolStatusThread.class);
	private CloudVmAttVolService cloudVmAttVolService;

	public CloudVmAttVolStatusThread(CloudVmAttVolService cloudVmAttVolService){
		this.cloudVmAttVolService = cloudVmAttVolService;
	}
	@Override
	public void run() {
		String value = null ;
		boolean isSync = false;
		try {
			value = cloudVmAttVolService.pop(RedisKey.volAttVmKey);
			JSONObject valueJson = JSONObject.parseObject(value);
			CloudVm cloudVm = JSON.parseObject(value, CloudVm.class);
			if(null!=value){
				log.info("从云主机绑定云硬盘队列中取出："+value);
				if(null!=cloudVm.getIsAttch()&&"true".equals(cloudVm.getIsAttch())){
					isSync=cloudVmAttVolService.syncAllSuccess(cloudVm);
				}else if("BUILD".equals(cloudVm.getVmStatus()) || "BUILDING".equals(cloudVm.getVmStatus())){
					isSync=cloudVmAttVolService.syncVmAndVolumeInBuild(cloudVm);
				}
				
				if(isSync){
					if(null!=cloudVm.getIsAttch()&&"true".equals(cloudVm.getIsAttch())){
						log.info("订单编号："+cloudVm.getOrderNo()+"所有云主机、云硬盘最终成功，移除任务调度！");
					}else if("BUILD".equals(cloudVm.getVmStatus()) || "BUILDING".equals(cloudVm.getVmStatus())){
						log.info("订单编号："+cloudVm.getOrderNo()+"资源状态刷新成功，移除任务调度！");
					}
				}
				else {
					int count = cloudVm.getCount();
					if(count>720){
						 if(null!=cloudVm.getIsAttch()&&"true".equals(cloudVm.getIsAttch())){
							log.info("订单编号："+cloudVm.getOrderNo()+"所有云主机、云硬盘挂载已执行"+count+"次状态未刷新，移除任务调度！");
						}else if("BUILD".equals(cloudVm.getVmStatus()) || "BUILDING".equals(cloudVm.getVmStatus())){
							log.info("订单编号："+cloudVm.getOrderNo()+"资源已执行"+count+"次状态未刷新，移除任务调度！");
						}
					}else{
						valueJson.put("count", count+1);
						valueJson.put("vmSure",cloudVm.getVmSure());
						valueJson.put("volumeSure",cloudVm.getVolumeSure());
						
						if(null!=cloudVm.getIsAttch()&&"true".equals(cloudVm.getIsAttch())){
							log.info("订单编号："+cloudVm.getOrderNo()+"所有云主机、云硬盘挂载状态未刷新，等待下次调度！");
						}else if("BUILD".equals(cloudVm.getVmStatus()) || "BUILDING".equals(cloudVm.getVmStatus())){
							log.info("订单编号："+cloudVm.getOrderNo()+"资源状态未刷新，等待下次调度！");
						}
						cloudVmAttVolService.push(RedisKey.volAttVmKey, valueJson.toJSONString());
					}
				}
			}
		
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}

	}

}
