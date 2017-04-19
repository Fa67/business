package com.eayun.work.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.work.dao.OrderNumDao;
import com.eayun.work.model.BaseOrderNum;
import com.eayun.work.service.OrderNumService;

@Service
@Transactional
public class OrderNumServiceImpl implements OrderNumService{
	@Autowired
	private OrderNumDao orderNumDao;
	//获取流水帐号
	@Override
	public String getOrderNum(String prefix,String format,int length){
		Date date = new Date();
		if(format==null){
			format="yyyyMMdd";
		}
		DateFormat sdf = new SimpleDateFormat(format);
		String dateStr=sdf.format(date);
		
		String getNumSql = "from BaseOrderNum where prefix=? and s_date=?";
		List<String> list = new ArrayList<String>();
		list.add(prefix);
		list.add(dateStr);
		BaseOrderNum order=(BaseOrderNum) orderNumDao.findUnique(getNumSql, list.toArray());
		if(order!=null){//有记录
			order.setMaxSeq(order.getMaxSeq()+1);
		}else{
			order=new BaseOrderNum();
			order.setDateFrommat(dateStr);
			order.setPrefix(prefix);
		}
		orderNumDao.saveOrUpdate(order);
		if(length<4 || length>8){
			length = 8;
		}
		String str="%0"+length+"d";
		//拼接编号
		String num = "";
		num=prefix+"_"+dateStr+"_"+String.format(str, order.getMaxSeq());
		return num;
	}
	
}
