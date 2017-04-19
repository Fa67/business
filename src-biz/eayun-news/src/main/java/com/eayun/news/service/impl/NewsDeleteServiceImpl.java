package com.eayun.news.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.news.dao.NewsRecDao;
import com.eayun.news.dao.NewsSendDao;
import com.eayun.news.service.NewsDeleteService;

@Transactional
@Service
public class NewsDeleteServiceImpl implements NewsDeleteService{
	
	@Autowired
	private NewsSendDao newsSendDao;
	@Autowired
	private NewsRecDao newsRecDao;
	/**
	 * 获得10天前的日期
	 * @param day
	 * @return
	 * @throws Exception
	 */
	private Date getDate(Date day) throws Exception {  
        Date dateOk = new Date(day.getTime() - 10 * 24 * 60 * 60 * 1000);  
        return dateOk;  
    }
	/**
	 * 获得到今天为止会失效的生效日期
	 * @param todayDate
	 * @return
	 */
	private Date getValidDate(Date todayDate) throws Exception {
		Date validDate = getDate(getDate(getDate(todayDate)));
		return validDate;
	}
	/**
	 * 删除所有为收藏的超过生效日期30天的消息
	 */
	@Override
	public void deleteNews() throws Exception {
		Date validDate = getValidDate(new Date());
		String sqlRec = new String("delete from news_recinfo where is_collect = 0 and send_date <= ?");
		newsRecDao.execSQL(sqlRec,validDate);
	}
}
