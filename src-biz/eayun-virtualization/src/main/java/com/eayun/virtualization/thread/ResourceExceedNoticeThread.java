package com.eayun.virtualization.thread;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.notice.model.MessageResourcesStopModel;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.project.service.ProjectService;
import com.eayun.syssetup.model.EcmcSysDataTree;
import com.eayun.syssetup.service.SysDataTreeService;
import com.eayun.virtualization.bean.AboutToExpire;
import com.eayun.virtualization.service.ResourceDisposeService;
/**
 * 包年包月资源到期（已停用）消息通知Thread
 * @author xiangyu.cao@eayun.com
 *
 */
public class ResourceExceedNoticeThread implements Runnable{
private static final Logger     log    = LoggerFactory.getLogger(ResourceExceedNoticeThread.class);
    
	private String cusId;
	private MessageCenterService messageCenterService;
	private ResourceDisposeService resourceDisposeService;
	private ProjectService projectService;
	private SysDataTreeService sysDataTreeService;
	public ResourceExceedNoticeThread(String cusId,MessageCenterService messageCenterService,ResourceDisposeService resourceDisposeService,ProjectService projectService,SysDataTreeService sysDataTreeService) {
		this.cusId=cusId;
		this.messageCenterService=messageCenterService;
		this.resourceDisposeService=resourceDisposeService;
		this.projectService=projectService;
		this.sysDataTreeService=sysDataTreeService;
	}

	@Override
	public void run() {
		log.info("开始获取停用资源并发送消息");
		Date nowZero=getNowZero();
		Date exceedTime=getExceedTime(nowZero);
		List<MessageResourcesStopModel> resourList=new ArrayList<MessageResourcesStopModel>();
		try {
			List<AboutToExpire> aboutToExpireList=resourceDisposeService.getExpireResources(cusId,exceedTime,"3");
			log.info("开始获取停用资源并发送消息,cusId:"+cusId+"aboutToExpireListSize:"+aboutToExpireList.size());
			for (AboutToExpire aboutToExpire : aboutToExpireList) {
				MessageResourcesStopModel resourcesExModel=new MessageResourcesStopModel();
				log.info("停用资源发送消息,cusId:"+cusId+"停用时间:"+nowZero+",resourceName:"+aboutToExpire.getResourcesName()+",resourceType:"+aboutToExpire.getResourcesType());
				resourcesExModel.setExpireDate(nowZero);
				resourcesExModel.setResourcesName(aboutToExpire.getResourcesName());
				resourcesExModel.setResourcesType(aboutToExpire.getResourcesType());
				if(!StringUtil.isEmpty(aboutToExpire.getResourcesName())){
					resourList.add(resourcesExModel);
				}
			}
		} catch (Exception e) {
			log.error("获取停用资源失败",e);
		}
		if(resourList.size()>0){
			try {
				log.info("停用资源并发送消息,调用消息中心接口");
				messageCenterService.resourStopMessage(resourList, cusId);
			} catch (Exception e) {
				log.error("发送消息失败",e);
			}
		}
		
	}
	private Date getExceedTime(Date todayZero) {
		int hours = Integer.parseInt(sysDataTreeService.getRecoveryTime());
		todayZero=DateUtil.addDay(todayZero, new int[]{0,0,0,-hours});
		return todayZero;
	}

	private Date getNowZero() {
		Date endTime = new Date();
		// 获取当日0点
		Calendar c = Calendar.getInstance();
		c.setTime(endTime);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		endTime = c.getTime();
		return endTime;
	}
}
