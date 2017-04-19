package com.eayun.news.service;

import java.util.Date;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.news.model.NewsRec;


/**
 * 
 *                       
 * @Filename: ShowNewsService.java
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
public interface ShowNewsService {
   /**
    * 
    * @param page
    * @param title
    * @param userId
    * @param pageNum
    * @return
    * @throws Exception 
    */

    public Page getNewsList(Page page, Date beginTime, Date endTime, String title, String userId,String isCollect,QueryMap queryMap) throws Exception;

    /**
     * 更新收藏,阅读标志位
     * @param NewsRec String
     * @throws Exception 
     */
    public boolean update(NewsRec newsRec, String type) throws Exception;
    /**
     * 查找未读消息数量
     * @param userId 用户id
     * @throws Exception 
     */
    public int newsCount(String userAccount) throws Exception;
    /**
     * 检验某个账户是否有收藏消息
     * @param userAccount
     * @return
     */
    public boolean whetherHasCollect(String userAccount);
    /**
     * 获取当前未读的消息列表
     * @param userAccount
     * @return
     */
    public JSONObject getUnreadList(String userAccount,JSONObject object) throws Exception;
    
    /**
     * 获取单个消息
     * */
    public NewsRec getbyid(String id) throws Exception;
}
