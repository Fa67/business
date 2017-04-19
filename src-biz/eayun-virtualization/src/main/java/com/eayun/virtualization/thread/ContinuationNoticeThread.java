package com.eayun.virtualization.thread;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.notice.model.MessageExpireRenewResourcesModel;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.project.service.ProjectService;
import com.eayun.virtualization.bean.AboutToExpire;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.service.ResourceDisposeService;
/**
 * 资源即将到期发送消息Thread
 * @author xiangyu.cao@eayun.com
 *
 */
public class ContinuationNoticeThread implements Runnable{
	private static final Logger     log    = LoggerFactory.getLogger(ContinuationNoticeThread.class);
    
	private String cusId;
	private MessageCenterService messageCenterService;
	private ResourceDisposeService resourceDisposeService;
	private ProjectService projectService;
	
	public ContinuationNoticeThread(String cusId,MessageCenterService messageCenterService,ResourceDisposeService resourceDisposeService,ProjectService projectService) {
		this.cusId=cusId;
		this.messageCenterService=messageCenterService;
		this.resourceDisposeService=resourceDisposeService;
		this.projectService=projectService;
	}

	@Override
	public void run() {
		log.info("开始获取即将到期资源并发送消息,cusId:"+cusId);
		Date todayZero=getTodayZero();
		Date threeDay = DateUtil.addDay(todayZero, new int[]{0,0,0,72});
		List<MessageExpireRenewResourcesModel> resourList=new ArrayList<MessageExpireRenewResourcesModel>();
		try {
			List<AboutToExpire> aboutToExpireList=resourceDisposeService.getExpireResources(cusId,threeDay,"0");
			log.info("开始获取即将到期资源并发送消息,cusId:"+cusId+",aboutToExpireListSize:"+aboutToExpireList.size());
			for (AboutToExpire aboutToExpire : aboutToExpireList) {
				log.info("开始获取即将到期资源并发送消息,cusId:"+cusId+",到期时间:"+aboutToExpire.getEndTime()+",resourceName:"+aboutToExpire.getResourcesName()+",resourceType:"+aboutToExpire.getResourcesType());
				MessageExpireRenewResourcesModel resourcesExModel=new MessageExpireRenewResourcesModel();
				resourcesExModel.setExpireDate(aboutToExpire.getEndTime());
				resourcesExModel.setResourcesName(aboutToExpire.getResourcesName());
				resourcesExModel.setResourcesType(aboutToExpire.getResourcesType());
				if(!StringUtil.isEmpty(aboutToExpire.getResourcesName())){
					resourList.add(resourcesExModel);
				}
			}
		} catch (Exception e) {
			log.error("获取即将到期资源失败",e);
		}
		if(resourList.size()>0){
			try {
				log.info("开始发送即将到期资源消息,调用消息中心接口");
				messageCenterService.expireRenewMessage(resourList, cusId);
			} catch (Exception e) {
				log.error("发送消息失败",e);
			}
		}
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
