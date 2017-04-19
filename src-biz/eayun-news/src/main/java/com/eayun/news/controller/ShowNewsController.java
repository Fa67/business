package com.eayun.news.controller;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.util.DateUtil;
import com.eayun.news.model.NewsRec;
import com.eayun.news.service.ShowNewsService;

/**
 *                       
 * @Filename: ShowNewsController.java
 * @Description: 
 * @Version: 1.0
 * @Author:wang ning
 * @Email: ning.wang@eayun.com
 * @History:<br>
 *<li>Date: 2015年9月14日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Controller
@RequestMapping("/sys/news")
public class ShowNewsController extends BaseController {
	private static final Logger log = LoggerFactory.getLogger(ShowNewsController.class);
	@Autowired
	private  ShowNewsService  newService;
	@RequestMapping(value = "/getNewsList", method = RequestMethod.POST)
    @ResponseBody
	public String getNewsList(HttpServletRequest request, Page page,@RequestBody ParamsMap map)
			throws Exception {				
		try {
			SessionUserInfo sessionUserInfo =(SessionUserInfo)request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
			String userAccount = sessionUserInfo.getUserName();     //用户Account
			String title = map.getParams().get("newsTitle").toString();
			String begin = map.getParams().get("beginTime").toString();
            String end = map.getParams().get("endTime").toString();
            String isCollect = map.getParams().get("isCollect").toString();
            
            Date beginTime = DateUtil.timestampToDate(begin);
            Date endTime = DateUtil.timestampToDate(end);
            int pageSize = map.getPageSize();
            int pageNumber = map.getPageNumber();
			QueryMap queryMap=new QueryMap();
			queryMap.setPageNum(pageNumber);
			queryMap.setCURRENT_ROWS_SIZE(pageSize);
			page = newService.getNewsList(page, beginTime, endTime, title, userAccount, isCollect ,queryMap);
		} catch (Exception e) {
			throw e;
		}
		return JSONObject.toJSONString(page);
	}
	/**
	 */
	@RequestMapping(value="/collect",method = RequestMethod.POST)
	@ResponseBody
	public void collect(HttpServletRequest request,@RequestBody NewsRec newsRec) throws Exception{
		newService.update(newsRec,"collect");
	}
	@RequestMapping(value="/uncollect",method = RequestMethod.POST)
	@ResponseBody
	public void unCollect(HttpServletRequest request,@RequestBody NewsRec newsRec) throws Exception{
		newService.update(newsRec,"uncollect");
	}
	@RequestMapping(value="/statu",method = RequestMethod.POST)
	@ResponseBody
	public String statu(HttpServletRequest request,@RequestBody NewsRec newsRec) throws Exception{
		newService.update(newsRec,"statu");
		return JSONObject.toJSONString(newsRec);
	}
	/**
	 */
	@RequestMapping(value="/unreadCount",method = RequestMethod.POST)
	@ResponseBody
	public int unreadCount(HttpServletRequest request,@RequestBody Map<String,String> map) throws Exception{
		String id = map.get("userAccount");
		return newService.newsCount(id);
	}
	/**
	 * 判断传入的Account是否含有收藏的消息记录，有则为true
	 * @param request
	 * @param userAccount
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/whetherHasCollect",method = RequestMethod.POST)
	@ResponseBody
	public boolean whetherHasCollect(HttpServletRequest request,@RequestBody Map map) throws Exception{
	    String userAccount = map.get("userAccount").toString();
		boolean isCollect = false;
		try {
			isCollect = newService.whetherHasCollect(userAccount);
		} catch(Exception e) {
			throw e;
		}
		return isCollect;
	}
	@RequestMapping(value = "/getUnreadList" , method = RequestMethod.POST)
    @ResponseBody
	public String getUnreadList(HttpServletRequest request) throws Exception{
		SessionUserInfo sessionUserInfo =(SessionUserInfo)request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		String userAccount = sessionUserInfo.getUserName();
		JSONObject object = new JSONObject();
		try {
			newService.getUnreadList(userAccount,object);
		} catch (Exception e) {
			throw e;
		}
		return JSON.toJSONString(object);
	}
	
	
	/**
	 * 获取单个消息
	 * */
	 
		@RequestMapping(value="/getbyid",method = RequestMethod.POST)
		@ResponseBody
		public String getByNewsId(HttpServletRequest request,@RequestBody Map map) throws Exception{
		    String id = map.get("id").toString();
		    NewsRec model= newService.getbyid(id);
		    //newService.update(model,"statu");//修改状态
			return  JSON.toJSONString(model);
		}
	
}
