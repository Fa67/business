package com.eayun.database.instance.thread.status;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.database.instance.model.CloudRDSInstance;
import com.eayun.database.instance.service.CloudRDSInstanceService;

public class CloudRDSInstanceStatusThread implements Runnable {
	
	private static final Logger log = LoggerFactory
            .getLogger(CloudRDSInstanceStatusThread.class);
	private CloudRDSInstanceService cloudRDSInstanceService;

    public CloudRDSInstanceStatusThread(CloudRDSInstanceService cloudRDSInstanceService) {
        this.cloudRDSInstanceService = cloudRDSInstanceService;
    }
	@Override
	public void run() {
		String value = null;
		boolean isSync = false;
		String stackStatus = null;
		CloudRDSInstance cloudRDSInstance = null;
		try{
			value = cloudRDSInstanceService.pop(RedisKey.rdsKey);
			JSONObject valueJson = JSONObject.parseObject(value);
			cloudRDSInstance = JSON.parseObject(value, CloudRDSInstance.class);
			JSONObject json = null;
			if(null!=value){
				log.info("从云数据库队列中取出："+value);
				if("BUILD".equals(cloudRDSInstance.getRdsStatus()) || "BUILDING".equals(cloudRDSInstance.getRdsStatus())){
					isSync = cloudRDSInstanceService.syncRDSInstanceInBuild(cloudRDSInstance);
				}else {
					json = cloudRDSInstanceService.get(valueJson);
					log.info("底层返回JSON:"+json);
					if(null != json){
						if("SHUTDOWN".equals(cloudRDSInstance.getRdsStatus())){
							if ("true".equals(json.getString("deletingStatus"))){
								
								cloudRDSInstance.setIsDeleted("1");
								cloudRDSInstanceService.deleteRdsInstance(cloudRDSInstance);
								
								isSync = true;
							}
						}
						stackStatus = json.getString("status");
						if(!StringUtils.isEmpty(stackStatus)){
							stackStatus = stackStatus.toUpperCase();
						}
						if("BACKUP".equalsIgnoreCase(cloudRDSInstance.getRdsStatus()) &&
								!"BACKUP".equalsIgnoreCase(stackStatus)){
							cloudRDSInstanceService.updateRdsInstance(cloudRDSInstance, json.getString("status").toUpperCase(), false);
							isSync = true;
						}else if("REBOOT".equalsIgnoreCase(cloudRDSInstance.getRdsStatus()) &&
								!"REBOOT".equalsIgnoreCase(stackStatus)){
							// 0:正常重启操作；1：解绑后的重启操作；2：绑定后的重启操作
							switch (cloudRDSInstance.getIsNeedAttach()) {
							case "0":
								cloudRDSInstanceService.updateRdsInstance(cloudRDSInstance, json.getString("status").toUpperCase(), false);
								break;
							case "1":
								cloudRDSInstanceService.rebootSuccessForDetach(cloudRDSInstance);
								break;
							case "2":
								cloudRDSInstanceService.updateRdsInstance(cloudRDSInstance, json.getString("status").toUpperCase(), true);
								break;
							default:
								break;
							}
							isSync = true;
						}else if("DETACH".equalsIgnoreCase(cloudRDSInstance.getRdsStatus()) &&
								!"DETACH".equalsIgnoreCase(stackStatus)){
							cloudRDSInstanceService.detachReplicaSuccess(cloudRDSInstance, json.getString("status").toUpperCase());
							isSync = true;
						}else if("RESIZE".equalsIgnoreCase(cloudRDSInstance.getRdsStatus()) &&
								!"RESIZE".equalsIgnoreCase(stackStatus)){
							// 0：只升级规格； 1：即升级规格又升级数据盘大小；2：只升级数据盘大小；3：针对规格和数据盘都升级的操作，升级数据盘失败后对规格的回滚
							switch (cloudRDSInstance.getResizeType()) {
							case "0":
								cloudRDSInstanceService.upgradeSuccess(cloudRDSInstance, stackStatus);
								break;
							case "1":
								cloudRDSInstanceService.resizeRdsInstanceVolume(cloudRDSInstance, stackStatus);
								break;
							case "2":
								cloudRDSInstanceService.upgradeSuccess(cloudRDSInstance, stackStatus);
								break;
							case "3":
								cloudRDSInstanceService.updateRdsInstance(cloudRDSInstance, stackStatus, false);
								break;
							default:
								break;
							}
							isSync = true;
						}
					}
				}
			}
			if(isSync){
				if("BUILD".equals(cloudRDSInstance.getRdsStatus()) || "BUILDING".equals(cloudRDSInstance.getRdsStatus())){
					log.info("订单编号："+cloudRDSInstance.getOrderNo()+"资源状态刷新成功，移除任务调度！");
				}
				else{
					log.info("云数据库ID："+cloudRDSInstance.getRdsId()+"状态刷新成功，移除任务调度！");
				}
			}
			else {
				int count = cloudRDSInstance.getCount();
				if(count > 100){
					if("BUILD".equals(cloudRDSInstance.getRdsStatus()) || "BUILDING".equals(cloudRDSInstance.getRdsStatus())){
						log.info("订单编号："+cloudRDSInstance.getOrderNo()+"资源已执行"+count+"次状态未刷新，移除任务调度！");
						// 移除任务调度，并且订单置为处理失败已取消！
						cloudRDSInstanceService.deleteBuildInstance(cloudRDSInstance);
					}
					else{
						cloudRDSInstanceService.updateRdsInstance(cloudRDSInstance, stackStatus, false);
					}
				}else{
					valueJson.put("count", count+1);
					if("BUILD".equals(cloudRDSInstance.getRdsStatus()) || "BUILDING".equals(cloudRDSInstance.getRdsStatus())){
						log.info("订单编号："+cloudRDSInstance.getOrderNo()+"资源状态未刷新，等待下次调度！");
					}
					else{
						log.info("云数据库ID："+cloudRDSInstance.getRdsId()+"状态未刷新，等待下次调度！");
					}
					cloudRDSInstanceService.push(RedisKey.rdsKey, valueJson.toJSONString());
				}
			}
		}catch(Exception e){
			// 对于既升级规格又升级数据盘时，如果升级数据盘失败，抛出异常，RDSInstanceService的方法upgradeFailHandler是会向消息队列中push一条关于回滚规格的信息
			// 所以此时是不需要在此继续push的。
			if(null != value && 
					(null != cloudRDSInstance && !"1".equals(cloudRDSInstance.getResizeType()))){
				cloudRDSInstanceService.push(RedisKey.rdsKey, value);
			}
			log.error(e.getMessage(), e);
		}
	}

}
