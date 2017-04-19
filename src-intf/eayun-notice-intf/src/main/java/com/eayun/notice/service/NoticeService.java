package com.eayun.notice.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;

/**
 * 
 *                       
 * @Filename: NoticeService.java
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
public interface NoticeService {
    @SuppressWarnings("rawtypes")
    public List findNoticeList();
    /**
     * 暂时用作公告轮播测试
     * @param object
     * @throws Exception
     */
    public void getNoticeList(JSONObject object) throws Exception;
    /**
     * 展开公告详情的交互
     * @param noticeId
     * @return
     * @throws Exception
     */
    public void getNoticeDetail(String noticeId, JSONObject object) throws Exception;
    /**
     * 获取后台的当前时间
     * @param object
     * @throws Exception
     */
    public void getNowTime(JSONObject object) throws Exception;
    /**
     * 查询公告列表
     * @param page
     * @param queryMap
     * @return
     */
	public Page getNoticePage(Page page, QueryMap queryMap);
}
