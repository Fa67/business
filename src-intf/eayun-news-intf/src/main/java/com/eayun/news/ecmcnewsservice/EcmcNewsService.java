package com.eayun.news.ecmcnewsservice;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.news.model.BaseNewsSend;
import com.eayun.news.model.NewsSendVOE;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年3月30日
 */
public interface EcmcNewsService {

	/**
	 * ECMC消息查询
	 * @param page       分页对象
	 * @param querymap   分页条件及参数
	 * @param beginTime  开始时间
	 * @param endTime    截止时间
	 * @param title      标题
	 * @param userId     用户ID
	 * @return
	 * @throws Exception
	 */
	public Page getNewsList(Page page, QueryMap querymap, Date beginTime, Date endTime, String title, String userId,String issyssend) throws AppException;
	
	/**
	 * 添加发送体消息
	 * @param model     NewsSendVOE
	 * @throws Exception
	 */
	public void save(BaseNewsSend model) throws AppException;
	/**
	 * 检验现在是否已经生效
	 * @param 生效时间timeStart
	 * @return
	 */
	public boolean timeFlag(Long timeStart);
	
	/**
	 * 根据ID删除消息
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public Map<Boolean,String> deleteById(String id) throws AppException;
	
	/**
	 * 修改NewsSendVOE
	 * @param nsv
	 * @throws Exception
	 */
	public void editNewsRec(NewsSendVOE nsv) throws AppException;
	public void edit(NewsSendVOE nsv) throws AppException;
	
	/**
	 * 获取收件人和发送方动态下拉列表数据
	 * @param JSONObject
	 * @throws Exception
	 */
	public Map<String, List<NewsSendVOE>> getList() throws AppException;
	
	/**
	 * 获取消息相关的计数
	 * @param object
	 * @param nsv
	 * @throws Exception
	 */
	public Object getCount(NewsSendVOE nsv) throws AppException;
	
}
