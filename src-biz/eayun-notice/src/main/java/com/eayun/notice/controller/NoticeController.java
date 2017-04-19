package com.eayun.notice.controller;

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
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.notice.service.NoticeService;

/**
 * 
 *                       
 * @Filename: NoticeController.java
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

@Controller
@RequestMapping("/sys/notice")
public class NoticeController extends BaseController{
    private static final Logger log = LoggerFactory.getLogger(NoticeController.class);
    @Autowired
    private NoticeService noticeService;
    
    /**
     * 
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getNoticeList" , method = RequestMethod.POST)
    @ResponseBody
	public String getNoticeList(HttpServletRequest request) throws Exception{
		JSONObject object = new JSONObject();
		try {
			noticeService.getNoticeList(object);
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			throw e;
		}
		return JSON.toJSONString(object);
	}

    /**
     * 公告详情
     * @param request
     * @param map
     * @return
     * @throws Exception
     */
	@RequestMapping(value = "/getNoticeDetail" , method = RequestMethod.POST)
	@ResponseBody
	public String getNoticeDetail(HttpServletRequest request, @RequestBody Map<String, String> map) throws Exception {
		JSONObject object = new JSONObject();
		String noticeId = map.get("noticeId");
		try {
			noticeService.getNoticeDetail(noticeId, object);
		} catch (Exception e) {
			throw e;
		}
		return JSON.toJSONString(object);
	}
    
    @RequestMapping(value = "/getNowTime" , method = RequestMethod.POST)
    @ResponseBody
    public String getNowTime(HttpServletRequest request) throws Exception{
    	JSONObject object = new JSONObject();
    	try {
    		noticeService.getNowTime(object);
    	} catch (Exception e) {
    		throw e;
    	}
    	return JSON.toJSONString(object);
    }
    /**
     * 总览页弹出框显示所有公告分页列表，5条一页
     * @param request
     * @param page
     * @param map
     * @return
     */
    @RequestMapping(value = "/getnoticepage", method = RequestMethod.POST)
    @ResponseBody
    public String getNoticePage(HttpServletRequest request, Page page, @RequestBody ParamsMap map){
    	log.info("查询公告列表");
        int pageSize = map.getPageSize();
        pageSize = 5;
        int pageNumber = map.getPageNumber();
        
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        try {
			page = noticeService.getNoticePage(page,queryMap);
		} catch (Exception e) {
		    log.error(e.getMessage(), e);
		}
        return JSONObject.toJSONString(page);
    }
}
