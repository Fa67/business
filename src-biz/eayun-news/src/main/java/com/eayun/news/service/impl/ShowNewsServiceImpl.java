package com.eayun.news.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.redis.JedisUtil;
import com.eayun.news.dao.NewsRecDao;
import com.eayun.news.dao.NewsSendDao;
import com.eayun.news.model.BaseNewsRec;
import com.eayun.news.model.NewsRec;
import com.eayun.news.service.ShowNewsService;
@Transactional
@Service
public class ShowNewsServiceImpl implements ShowNewsService{
	private static final Logger log = LoggerFactory.getLogger(ShowNewsServiceImpl.class);
	@Autowired
	private NewsRecDao newsRecDao;
	@Autowired
    private NewsSendDao newsSendDao;
	@Autowired
	private JedisUtil jedisUtil;
	@Override
	public Page getNewsList(Page page, Date beginTime, Date endTime,String title, String userAccount,String isCollect,QueryMap queryMap)throws Exception {
	    log.info("查询消息列表");
	    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<Object> list = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer("select r.info_id id,r.news_id,r.rec_person,r.is_collect,r.statu,r.read_date,r.is_delete,s.news_title,s.memo,s.send_date,s.send_person,s.rec_type,c.cus_id,c.cus_name ");  //定义一个基础查询语句
		sql.append("from news_recinfo r ");
		sql.append("left join news_sendinfo s on r.news_id = s.news_id ");
		sql.append("left join sys_selfcustomer c on s.cus_id = c.cus_id ");
		sql.append("where 1 = 1 ");
		sql.append("and s.send_date <= ? ");
		list.add(new Date());
		if(null!=beginTime){         							//验证开始时间格式和是否为空
			sql.append("and s.send_date >= ? ");                	//拼接查询语句 
            list.add(beginTime);
        }
        if(null!=endTime){
        	sql.append("and s.send_date < ? ");
            list.add(endTime);
        }
		if(null!=title && !title.trim().equals("")){
			sql.append("and s.news_title like ? ");
			list.add("%"+title+"%");
		}
		if(null!=userAccount && !userAccount.trim().equals("")){
			sql.append("and r.rec_person =? ");
			list.add(userAccount);
		}
		if(null!=isCollect&&!isCollect.trim().equals("")){
			sql.append("and r.is_collect =? ");
			list.add(isCollect);
		}
		sql.append("order by r.statu asc,s.send_date desc");
		page = newsRecDao.pagedNativeQuery(sql.toString(),queryMap,list.toArray());
        List newList = (List)page.getResult();
        for(int i=0;i<newList.size();i++){
            Object[] objs = (Object[])newList.get(i);
            NewsRec news = new NewsRec();
            news.setId(String.valueOf(objs[0]));
            news.setNewsId(String.valueOf(objs[1]));
            news.setRecPerson(String.valueOf(objs[2]));
            news.setIsCollect(String.valueOf(objs[3]));
            news.setStatu(String.valueOf(objs[4]));
            if(!String.valueOf(objs[5]).equals("null")){
            	news.setReadDate(simpleDateFormat.parse(String.valueOf(objs[5])));
            }
            news.setIsDelete(String.valueOf(objs[6]));
            news.setNewsTitle(String.valueOf(objs[7]));
            news.setMemo(String.valueOf(objs[8]));
            news.setSendDate(dateFromStringToString(String.valueOf(objs[9])));
            news.setSendPerson(String.valueOf(objs[10]));
            news.setRecType(String.valueOf(objs[11]));
            news.setCusId(String.valueOf(objs[12]));
            news.setCusName(String.valueOf(objs[13]));
            newList.set(i, news);
        }
        return page;
	}
	@Override
	public boolean update(NewsRec newsRec, String type) throws Exception {
	    log.info("更新消息信息（阅读状态,是否收藏）");
	    try{
	    	StringBuffer hql= new StringBuffer("update BaseNewsRec set ");
	    	List listParams = new ArrayList();
	    	if(type != null && type.equals("collect")){
	    		hql.append("isCollect = '1' ");
	    		jedisUtil.push(RedisKey.MESSAGE_COLLECT_QUEUE,newsRec.getNewsId());
	    		/**********redis收藏计数加一 开始**********/
	    		if( jedisUtil.get(RedisKey.MESSAGE_COLLECT_COUNT + newsRec.getNewsId()) == null ){
	    			jedisUtil.set(RedisKey.MESSAGE_COLLECT_COUNT + newsRec.getNewsId(),"0");
		        }
		        jedisUtil.increase(RedisKey.MESSAGE_COLLECT_COUNT + newsRec.getNewsId());
	    		/**********redis收藏计数加一 结束**********/
	    	}
	    	if(type != null && type.equals("uncollect")){
	    		hql.append("isCollect = '0' ");
	    		jedisUtil.push(RedisKey.MESSAGE_UNCOLLECT_QUEUE,newsRec.getNewsId());
	    		if( jedisUtil.get(RedisKey.MESSAGE_UNCOLLECT_COUNT + newsRec.getNewsId()) == null ){
	    			jedisUtil.set(RedisKey.MESSAGE_UNCOLLECT_COUNT + newsRec.getNewsId(),"0");
		        }
		        jedisUtil.increase(RedisKey.MESSAGE_UNCOLLECT_COUNT + newsRec.getNewsId());
	    	}
	    	if(type != null && type.equals("statu")){
	    		if(newsRec.getReadDate() == null){
	        		newsRec.setReadDate(new Date()); 
	        		hql.append("readDate = ?,");
	        		listParams.add(newsRec.getReadDate());
	        	}
	    		hql.append("statu = '1'");
		        jedisUtil.push(RedisKey.MESSAGE_STATUS_QUEUE,newsRec.getNewsId());
		        if( jedisUtil.get(RedisKey.MESSAGE_STATUS_COUNT + newsRec.getNewsId()) == null ){
		        	jedisUtil.set(RedisKey.MESSAGE_STATUS_COUNT + newsRec.getNewsId(),"0");
		        }
		        jedisUtil.increase(RedisKey.MESSAGE_STATUS_COUNT + newsRec.getNewsId());
		        
	    	}
	    	hql.append("where id = ?");
	        listParams.add(newsRec.getId());
	        newsRecDao.executeUpdate(hql.toString(),listParams.toArray());
		    if(hql==null || "".equals(hql.toString())){
		        return false;
		    }
		    return true;
	    } catch (Exception e) {
	    	throw e;
	    }
	}
    @Override
    public int newsCount(String userId) throws Exception {
        try{
	        List<Object> list = new ArrayList<Object>();
	        StringBuffer hql = new StringBuffer(" from BaseNewsRec r,BaseNewsSend s where r.newsId = s.id and r.statu='0' and s.sendDate <= ? ");  //定义一个基础查询语句
			list.add(new Date());
	        if(null != userId && !userId.trim().equals("")){
	            hql.append(" and r.recPerson = ? ");
	            list.add(userId);
	        }
	        List newslist = newsRecDao.find(hql.toString(),list.toArray());
	        int sum = 0;
	        if(newslist != null){
	        	sum = newslist.size();
	        }
	        return sum;
        }catch(Exception e){
            throw e;
        }
    }
    /**
	 * 将数据库中日期的展现格式去掉.0
	 * @param dateString
	 * @return
	 * @throws Exception
	 */
	private String dateFromStringToString(String dateString) throws Exception {
		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = simpleDateFormat.parse(dateString);
			dateString = simpleDateFormat.format(date);
			return dateString;
		} catch (Exception e) {
			throw e;
		}
	}
	@Override
	public boolean whetherHasCollect(String userAccount){
		String hql = "from BaseNewsRec where isCollect = '1' and recPerson = ?";
		List<BaseNewsRec> list = newsRecDao.find(hql,new Object[]{userAccount});
		if(null!=list&&list.size() != 0){
			return true;
		}
		return false;
	}
	@Override
	public JSONObject getUnreadList(String userAccount,JSONObject object) throws Exception{
		StringBuffer sql = new StringBuffer("select r.id,r.newsId,r.recPerson,r.statu,s.newsTitle,s.memo,s.sendPerson,s.sendDate from BaseNewsRec r,BaseNewsSend s ");
		sql.append("where r.newsId = s.id and r.statu = '0' and r.recPerson = ? and s.sendDate < ? ");
		sql.append("order by s.sendDate desc");
		List<Object[]> list = newsSendDao.find(sql.toString(),new Object[]{userAccount,new Date()});
		List<NewsRec> unreadList = new ArrayList<NewsRec>();
		for(int i = 0;i < 5 && list.size() - i > 0;i++){
			Object[] objs = list.get(i);
			NewsRec newsRec = new NewsRec();
			newsRec.setId(String.valueOf(objs[0]));
			newsRec.setNewsId(String.valueOf(objs[1]));
			newsRec.setRecPerson(String.valueOf(objs[2]));
			newsRec.setStatu(String.valueOf(objs[3]));
			newsRec.setNewsTitle(String.valueOf(objs[4]));
			newsRec.setMemo(String.valueOf(objs[5]));
			newsRec.setSendPerson(String.valueOf(objs[6]));
			newsRec.setSendDate(dateFromStringToString(String.valueOf(objs[7])));
			unreadList.add(newsRec);
		}
		object.put("unreadList",unreadList);
		return object;
	}
	@Override
	public NewsRec getbyid(String id) throws Exception {
		StringBuffer sql = new StringBuffer("select rc.info_id,rc.news_id,rc.rec_person,rc.statu,sc.news_title,sc.memo,sc.send_person,rc.send_date,sc.is_syssend from news_recinfo rc"
					+"		left join news_sendinfo sc on  rc.news_id=sc.news_id"	
					+"	where rc.info_id=?");
		List<Object[]> list = newsSendDao.createSQLNativeQuery(sql.toString(),new Object[]{id}).getResultList();
		NewsRec newsRec = new NewsRec();
		for(int i = 0;i < 5 && list.size() - i > 0;i++){
			Object[] objs = list.get(i);
			
			newsRec.setId(String.valueOf(objs[0]));
			newsRec.setNewsId(String.valueOf(objs[1]));
			newsRec.setRecPerson(String.valueOf(objs[2]));
			newsRec.setStatu(String.valueOf(objs[3]));
			newsRec.setNewsTitle(String.valueOf(objs[4]));
			newsRec.setMemo(String.valueOf(objs[5]));
			newsRec.setSendPerson(String.valueOf(objs[6]));
			newsRec.setSendDate(dateFromStringToString(String.valueOf(objs[7])));
			newsRec.setIssyssend(String.valueOf(objs[8]));
			
		}
		
		return newsRec;
	}
}
