package com.eayun.schedule.thread.status;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.schedule.service.CloudVolumeService;
import com.eayun.virtualization.model.CloudVolume;

public class CloudVolumeStatusThread implements Runnable{
	private static final Logger log = LoggerFactory.getLogger(CloudVolumeStatusThread.class);
	private CloudVolumeService cloudVolumeService ;
	
	public CloudVolumeStatusThread (CloudVolumeService cloudVolumeService){
		this.cloudVolumeService = cloudVolumeService;
	}

	@Override
	public void run() {
		String value = null ;
		boolean isSync = false;
		try{
			value = cloudVolumeService.pop(RedisKey.volKey);
			if(null!=value){
				JSONObject valueJson = JSONObject.parseObject(value);
				CloudVolume cloudVolume = JSON.parseObject(value, CloudVolume.class);
				log.info("从云硬盘队列中取出："+value);
				if("CREATING".equals(cloudVolume.getVolStatus())&&"0".equals(cloudVolume.getVolBootable())&&null!=cloudVolume.getFromSnapId()&&!"".equals(cloudVolume.getFromSnapId())){
					isSync=cloudVolumeService.syncVolumeByBackUpInBuild(cloudVolume);
				}else if(("DOWNLOADING".equals(cloudVolume.getVolStatus())||"CREATING".equals(cloudVolume.getVolStatus()))&&"0".equals(cloudVolume.getVolBootable())){
					isSync=cloudVolumeService.syncVolumeInBuild(cloudVolume);
				}else{
					JSONObject json = cloudVolumeService.get(valueJson);
					log.info("底层返回JSON:"+json);
					String stackStatus = json.getString("status");
					String attachments = json.getString("attachments");
					String bindPoint = "" ;
					if ("true".equals(json.getString("deletingStatus"))){
						cloudVolume.setIsDeleted("1");
						cloudVolumeService.updateDeteleStatus(cloudVolume);
						isSync = true;
					}
					JSONArray attas = JSONArray.parseArray(attachments);
					if(null!= attas && attas.size()>0){
						JSONObject atta = attas.getJSONObject(0);
						bindPoint = atta.getString("device");
					}
	
					if (!StringUtils.isEmpty(stackStatus)) {
						stackStatus = stackStatus.toUpperCase();
						if(!stackStatus.equals(cloudVolume.getVolStatus())&&!"DELETING".equals(cloudVolume.getVolStatus())){
							
							if(!"IN-USE".equals(stackStatus)&&"ATTACHING".equals(cloudVolume.getVolStatus())){
								cloudVolume.setVolStatus(stackStatus);
								if(!StringUtils.isEmpty(bindPoint)){
									cloudVolume.setBindPoint(bindPoint);
								}
								cloudVolumeService.updateBindVol(cloudVolume);
								isSync = true;
							}else if(!"DOWNLOADING".equals(stackStatus)){
								cloudVolume.setVolStatus(stackStatus);
								if(!StringUtils.isEmpty(bindPoint)){
									cloudVolume.setBindPoint(bindPoint);
								}
								cloudVolumeService.updateVol(cloudVolume);
								isSync = true;
							}
						}
				  }
			  }
				
				
				if(isSync){
					if(null!=cloudVolume.getFromSnapId()&&!"".equals(cloudVolume.getFromSnapId())&&("CREATING".equals(cloudVolume.getVolStatus()) || "DOWNLOADING".equals(cloudVolume.getVolStatus()))){
						log.info("订单编号："+cloudVolume.getOrderNo()+"备份创建云硬盘第一步成功，移除任务调度！");
					}
					else if("CREATING".equals(cloudVolume.getVolStatus()) || "DOWNLOADING".equals(cloudVolume.getVolStatus())){
						log.info("订单编号："+cloudVolume.getOrderNo()+"资源状态刷新成功，移除任务调度！");
					}
					else{
						log.info("云硬盘ID："+cloudVolume.getVolId()+"状态刷新成功，移除任务调度！");
					}
				}else {
					int count = cloudVolume.getCount();
					if(count>720){
						if(null!=cloudVolume.getFromSnapId()&&!"".equals(cloudVolume.getFromSnapId())&&("CREATING".equals(cloudVolume.getVolStatus()) || "DOWNLOADING".equals(cloudVolume.getVolStatus()))){
							log.info("订单编号："+cloudVolume.getOrderNo()+"备份创建云硬盘第一步已执行"+count+"次状态未刷新，移除任务调度！");
						}else if("CREATING".equals(cloudVolume.getVolStatus()) || "DOWNLOADING".equals(cloudVolume.getVolStatus())){
							log.info("订单编号："+cloudVolume.getOrderNo()+"资源已执行"+count+"次状态未刷新，移除任务调度！");
						}
						else{
							log.info("云硬盘ID："+cloudVolume.getVolId()+"已执行"+count+"次状态未刷新，移除任务调度！");
						}
					}else{
						valueJson.put("count", count+1);
						if(null!=cloudVolume.getFromSnapId()&&!"".equals(cloudVolume.getFromSnapId())&&("CREATING".equals(cloudVolume.getVolStatus()) || "DOWNLOADING".equals(cloudVolume.getVolStatus()))){
							log.info("订单编号："+cloudVolume.getOrderNo()+"备份创建云硬盘第一步状态未刷新，等待下次调度！");
						}else if("CREATING".equals(cloudVolume.getVolStatus()) || "DOWNLOADING".equals(cloudVolume.getVolStatus())){
							log.info("订单编号："+cloudVolume.getOrderNo()+"资源状态未刷新，等待下次调度！");
						}
						else{
							log.info("云硬盘ID："+cloudVolume.getVolId()+"状态未刷新，等待下次调度！");
						}
						cloudVolumeService.push(RedisKey.volKey, valueJson.toJSONString());
					}

				}
			}
			
		}catch(Exception e){
			log.error(e.getMessage(),e);
			if(null!= value ){
				cloudVolumeService.push(RedisKey.volKey, value);
			}
		}
		
	}
	
}
