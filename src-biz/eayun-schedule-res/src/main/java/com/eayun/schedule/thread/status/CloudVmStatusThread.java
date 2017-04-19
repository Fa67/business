package com.eayun.schedule.thread.status;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.schedule.service.CloudVmService;
import com.eayun.virtualization.model.CloudVm;

public class CloudVmStatusThread implements Runnable{
	private static final Logger log = LoggerFactory.getLogger(CloudVmStatusThread.class);
	private CloudVmService cloudVmService ;
	
	public CloudVmStatusThread (CloudVmService cloudVmService){
		this.cloudVmService = cloudVmService;
	}

	@Override
	public void run() {
		String value = null ;
		boolean isBuild = false; 
		boolean isSync = false;
		String stackStatus = null;
		try{
			value = cloudVmService.pop(RedisKey.vmKey);
			JSONObject valueJson = JSONObject.parseObject(value);
			CloudVm cloudVm = JSON.parseObject(value, CloudVm.class);
			JSONObject json = null;
			if(null!=value){
				log.info("从云主机队列中取出："+value);
				if("BUILD".equals(cloudVm.getVmStatus()) || "BUILDING".equals(cloudVm.getVmStatus())){
					isBuild = true;
					isSync = cloudVmService.syncVmInBuild(cloudVm);
				}
				else {
					json = cloudVmService.get(valueJson);
					log.info("底层返回JSON:"+json);
					if(null != json){
						if("DELETING".equals(cloudVm.getVmStatus())){
							if ("true".equals(json.getString("deletingStatus"))){
								cloudVm.setVmStatus("DELETED");
								cloudVm.setIsDeleted("1");
								cloudVmService.deleteVm(cloudVm,false);
								
								isSync = true;
							}
						}
						stackStatus = json.getString("status");
						if(!StringUtils.isEmpty(stackStatus)){
							stackStatus = stackStatus.toUpperCase();
						}
						
						if("RESIZE".equalsIgnoreCase(cloudVm.getVmStatus())
								&&"VERIFY_RESIZE".equalsIgnoreCase(stackStatus)){
							cloudVmService.resized(cloudVm);
							valueJson.put("vmStatus", "RESIZED");
						}
						else if("RESIZED".equals(cloudVm.getVmStatus())
								&&!"RESIZED".equalsIgnoreCase(stackStatus)){
							cloudVm.setVmStatus(stackStatus);
							cloudVm.setHostId(json.getString("hostId"));
							cloudVm.setHostName(json.getString("OS-EXT-SRV-ATTR:hypervisor_hostname"));
							cloudVmService.resize(cloudVm);
							isSync = true;
						}
						else if("SOFT_DELETING".equals(cloudVm.getVmStatus())
								&&"SOFT_DELETED".equalsIgnoreCase(stackStatus)){
							cloudVm.setVmStatus(stackStatus);
							cloudVm.setIsDeleted("2");
							cloudVmService.deleteVm(cloudVm,true);
							isSync = true;
						}
						else if("SOFT_RESUME".equals(cloudVm.getVmStatus())
								&&"ACTIVE".equalsIgnoreCase(stackStatus)){
							cloudVmService.resumeVm(cloudVm);
							isSync = true;
						}
						
						else{
							if (("0".equals(cloudVm.getIsExsit()) && !stackStatus
									.equals(cloudVm.getPerStatus()))
									|| ("1".equals(cloudVm.getIsExsit()) && !stackStatus
											.equals(cloudVm.getVmStatus()))) {
								
								cloudVm.setVmStatus(stackStatus);
								cloudVmService.updateVm(cloudVm);
								isSync = true;
							}
						}
					}
				}
				if(isSync){
					if("BUILD".equals(cloudVm.getVmStatus()) || "BUILDING".equals(cloudVm.getVmStatus())){
						log.info("订单编号："+cloudVm.getOrderNo()+"资源状态刷新成功，移除任务调度！");
					}
					else{
						log.info("云主机ID："+cloudVm.getVmId()+"状态刷新成功，移除任务调度！");
					}
				}
				else {
					int count = cloudVm.getCount();
					if(count>100){
						if("BUILD".equals(cloudVm.getVmStatus()) || "BUILDING".equals(cloudVm.getVmStatus())){
							log.info("订单编号："+cloudVm.getOrderNo()+"资源已执行"+count+"次状态未刷新，移除任务调度！");
						}
						else{
							log.info("云主机ID："+cloudVm.getVmId()+"已执行"+count+"次状态未刷新，移除任务调度！");
							if ("true".equals(json.getString("deletingStatus"))){
								cloudVm.setVmStatus("DELETED");
								cloudVm.setIsDeleted("1");
								cloudVmService.deleteVm(cloudVm,false);
							}
							else{
								cloudVm.setVmStatus(stackStatus);
								cloudVmService.updateVm(cloudVm);
							}
						}
					}else{
						valueJson.put("count", count+1);
						if("BUILD".equals(cloudVm.getVmStatus()) || "BUILDING".equals(cloudVm.getVmStatus())){
							log.info("订单编号："+cloudVm.getOrderNo()+"资源状态未刷新，等待下次调度！");
						}
						else{
							log.info("云主机ID："+cloudVm.getVmId()+"状态未刷新，等待下次调度！");
						}
						cloudVmService.push(RedisKey.vmKey, valueJson.toJSONString());
					}
				}
			}
		}
		catch(Exception e){
		    log.error(e.getMessage(), e);
			if(null!= value && !isBuild){
				cloudVmService.push(RedisKey.vmKey, value);
			}
		}
		
	}
	
	
}
