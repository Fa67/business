package com.eayun.notice.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.notice.dao.NoticeDao;
import com.eayun.notice.model.BaseNotice;
import com.eayun.notice.model.Notice;
import com.eayun.notice.service.NoticeService;

/**
 * 
 *                       
 * @Filename: NoticeServiceImpl.java
 * @Description: 
 * @Version: 1.0
 * @Author:wang ning
 * @Email: ning.wang@eayun.com
 * @History:<br>
 *<li>Date: 2015年10月9日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Service
public class NoticeServiceImpl implements NoticeService{
    
    @Autowired
    private NoticeDao noticeDao;
    public List findNoticeList() {
        Date date=new Date();
        List<Object> list = new ArrayList<Object>();
        StringBuffer hql = new StringBuffer(" from BaseNotice  where 1=1  ");  //定义一个基础查询语句
            hql.append(" and validTime <= ? ");
            list.add(date);
            hql.append(" and invalidTime >= ? ");
            list.add(date);
        List newslist = noticeDao.find(hql.toString(),list.toArray());
        return newslist;
    }

	@Override
	public void getNoticeDetail(String noticeId, JSONObject object) throws Exception {
		try {
			BaseNotice notice = noticeDao.findOne(noticeId);
			object.put("notice" ,notice);
		} catch (Exception e) {
			throw e;
		}
	}
    /**
     * 获取当前启用的公告
     * @param object
     * @throws Exception
     */
    @Override
	public void getNoticeList(JSONObject object) throws Exception{
		try {
			String hqlNotice = "from BaseNotice where validTime < ? and invalidTime > ? and isUsed = '1' order by validTime desc";
			List<BaseNotice> listNotice = noticeDao.find(hqlNotice,new Object[]{new Date(),new Date()});
			if(listNotice.size() > 0){
				List<Notice> list = new ArrayList<Notice>();
				for(int i = 0;i < listNotice.size();i++){
					Notice notice = new Notice();
					notice.setId(listNotice.get(i).getId());
					notice.setMemo(listNotice.get(i).getMemo());
					notice.setUrl(listNotice.get(i).getUrl());
					notice.setInvalidTime(listNotice.get(i).getInvalidTime());
					notice.setTitle(listNotice.get(i).getTitle());
					notice.setContent(listNotice.get(i).getContent());
					notice.setValidTime(listNotice.get(i).getValidTime());
					list.add(notice);
				}
				object.put("hasListOrNot", true);
				object.put("notice", list);
			} else {
				object.put("hasListOrNot", false);
			}
		} catch (Exception e) {
			throw e;
		}
	}
    @Override
    public void getNowTime(JSONObject object) throws Exception{
    	try {
    		long nowTime = new Date().getTime();
    		object.put("nowTime", nowTime);
    	} catch (Exception e) {
    		throw e;
    	}
    }

    /**
     * 总览页弹出框显示公告列表，5条一页
     * @param page
     * @param queryMap
     * @return
     */
	@Override
	public Page getNoticePage(Page page, QueryMap queryMap) {
		StringBuffer hql = new StringBuffer();
		hql.append("from BaseNotice where validTime < ? and invalidTime > ? and isUsed = '1' order by validTime desc");
		page = noticeDao.pagedQuery(hql.toString(), queryMap, new Object[]{new Date(),new Date()});
		return page;
	}
}
