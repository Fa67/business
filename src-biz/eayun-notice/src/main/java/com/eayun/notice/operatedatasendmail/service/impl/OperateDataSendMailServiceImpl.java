package com.eayun.notice.operatedatasendmail.service.impl;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.exception.AppException;
import com.eayun.common.util.DateUtil;
import com.eayun.notice.model.MessageOperateModel;
import com.eayun.notice.operatedatasendmail.service.OperateDataSendMailService;
import com.eayun.notice.service.ExampleChart;
import com.eayun.notice.service.MessageCenterService;


@Transactional
@Service
public class OperateDataSendMailServiceImpl implements OperateDataSendMailService{
	
	@Autowired
	private  MessageCenterService messageCenterService; 
	@Autowired
	private ExampleChart exampleChart;

	@Override
	public void getOperateDataSendMail() throws AppException {
		
		String stratTime=DateUtil.dateToString(DateUtil.getNearlyDateTime(new Date(), -1, Calendar.DATE, false));//开始时间
		String endTime=DateUtil.dateToString(DateUtil.getNearlyDateTime(new Date(), -1, Calendar.DATE, true));//结束时间
		
		
		MessageOperateModel model=new MessageOperateModel();
		model.setOnedaynewincome(new BigDecimal(100.00));
		model.setOnedayneworder(10);
		model.setOnedaycus(5);
		model.setOnedaycusname("程龙");
		model.setOnedaycusmoney(new BigDecimal(10000.00));
		model.setOnedayRecusname("别人");
		model.setOnedayRecusmoney(new BigDecimal(9999.19));
		model.setOnedaynewvmcount(100);
		model.setOnedaynewvolumecount(100);
		model.setOnedaynewbackups(50);
		model.setOnedaynewbalanc(20);
		model.setOnedaynewvpn(2);
		model.setOnedaynewmysql(3);
		
		model.setAlldaynewincome(new BigDecimal(999999.99));
		model.setAlldayneworder(20000);
		model.setAlldaycus(500);
		model.setAlldaycusname("程龙");
		model.setAlldaycusmoney(new BigDecimal(100000.00));
		model.setAlldayRecusname("别人");
		model.setAlldayRecusmoney(new BigDecimal(90000.00));
		model.setAlldayVmcusname("程龙");
		model.setAlldayVmcount(2000);
		model.setAlldayIPcusname("程龙");
		model.setAlldayIPcount(100);
		model.setAlldaycpu(new Integer []{100,120});
		model.setAlldaymemory(new Integer []{1000,1200});
		model.setAlldaydisk(new Integer []{1000,1200});
		model.setAlldayip(new Integer []{1000,1200});
		model.setAlldayvmcount(500000);
		model.setAlldaydatacount(30000);
		model.setAlldaydatabackupcount(2000);
		model.setAlldaybalanccount(100);
		model.setAlldayVPNcount(1200);
		model.setAlldayobject(new Integer []{1000,1200});
		model.setAlldaymysqlcount(3000);
		
		
		model.setDayTime(dateToStringTwo(DateUtil.stringToDate(stratTime)));
		String alipayid=exampleChart.setBar(new String [] {"支付宝","微信","ECMC"},new Integer[]{1,2,3});
		String orderid=exampleChart.setBar(new String [] {"云主机","云硬盘","VPN"},new Integer[]{10,15,20});
		String allalipayid=exampleChart.setBar(new String [] {"支付宝","微信","ECMC"},new Integer[]{100,200,300});
		String allorderid=exampleChart.setBar(new String [] {"云主机","云硬盘","VPN"},new Integer[]{100,105,200});
		
		model.setOnedayfileidmoney(alipayid);
		model.setOnedayfileidorder(orderid);
		model.setAlldayfileidmoney(allalipayid);
		model.setAlldayfileidorder(allorderid);
		
		
		
	
		
		messageCenterService.OperateMail(model);
		System.out.println("消息已经开始发送了");
		
	}
	
	  private static String dateToStringTwo(Date date){
	       String dateStr = "";
	       if(date != null){
	           DateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");   
	           try {   
	               dateStr = sdf.format(date);  
	           } catch (Exception e) {   
	              e.printStackTrace();
	           }
	       }
	       return dateStr;
	   }

}
