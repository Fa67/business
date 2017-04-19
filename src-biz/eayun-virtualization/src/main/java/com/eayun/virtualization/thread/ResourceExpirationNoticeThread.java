package com.eayun.virtualization.thread;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.notice.model.MessageResourcesExpiredModel;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.project.service.ProjectService;
import com.eayun.syssetup.ecmcservice.EcmcSysDataTreeService;
import com.eayun.syssetup.model.EcmcSysDataTree;
import com.eayun.syssetup.service.SysDataTreeService;
import com.eayun.virtualization.bean.AboutToExpire;
import com.eayun.virtualization.service.ResourceDisposeService;

/**
 * 包年包月资源到期（已到期）消息通知Thread
 * @author xiangyu.cao@eayun.com
 *
 */
public class ResourceExpirationNoticeThread implements Runnable{
	private static final Logger     log    = LoggerFactory.getLogger(ResourceExpirationNoticeThread.class);
    
	private String cusId;
	private MessageCenterService messageCenterService;
	private ResourceDisposeService resourceDisposeService;
	private ProjectService projectService;
	private SysDataTreeService sysDataTreeService;
	
	public ResourceExpirationNoticeThread(String cusId,MessageCenterService messageCenterService,ResourceDisposeService resourceDisposeService,ProjectService projectService,SysDataTreeService sysDataTreeService) {
		this.cusId=cusId;
		this.messageCenterService=messageCenterService;
		this.resourceDisposeService=resourceDisposeService;
		this.projectService=projectService;
		this.sysDataTreeService=sysDataTreeService;
	}

	@Override
	public void run() {
		log.info("开始获取到期资源(已到期未超过保留时长)并发送消息");
		Date todayZero=getTodayZero();
		Date exceedTime=getExceedTime(todayZero);
		List<MessageResourcesExpiredModel> resourList=new ArrayList<MessageResourcesExpiredModel>();
		try {
			List<AboutToExpire> aboutToExpireList=resourceDisposeService.getExpireResources(cusId,todayZero,"2");
			log.info("到期资源(已到期未超过保留时长),cusId:"+cusId+",aboutToExpireListSize:"+aboutToExpireList.size());
			for (AboutToExpire aboutToExpire : aboutToExpireList) {
				log.info("到期资源(已到期未超过保留时长)发送消息,cusId:"+cusId+"停用时间:"+exceedTime+",resourceName:"+aboutToExpire.getResourcesName()+",resourceType:"+aboutToExpire.getResourcesType());
				MessageResourcesExpiredModel resourcesExModel=new MessageResourcesExpiredModel();
				resourcesExModel.setExpireDate(exceedTime);
				resourcesExModel.setResourcesName(aboutToExpire.getResourcesName());
				resourcesExModel.setResourcesType(aboutToExpire.getResourcesType());
				if(!StringUtil.isEmpty(aboutToExpire.getResourcesName())){
					resourList.add(resourcesExModel);
				}
			}
		} catch (Exception e) {
			log.error("获取到期资源失败",e);
		}
		if(resourList.size()>0){
			try {
				log.info("到期资源(已到期未超过保留时长)发送消息,调用消息中心接口");
				messageCenterService.resourExpiredMessage(resourList, cusId);
			} catch (Exception e) {
				log.error("发送消息失败",e);
			}
		}
		
	}
	private Date getExceedTime(Date todayZero) {
		int hours = Integer.parseInt(sysDataTreeService.getRecoveryTime());
		todayZero=DateUtil.addDay(todayZero, new int[]{0,0,0,hours});
		return todayZero;
	}

	private Date getTodayZero() {
		Date endTime = new Date();
		// 获取当日0点
		Calendar c = Calendar.getInstance();
		c.setTime(endTime);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		endTime = c.getTime();
		return endTime;
	}
}
