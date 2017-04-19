package com.eayun.news.service.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.redis.JedisUtil;
import com.eayun.news.dao.NewsSendDao;
import com.eayun.news.model.BaseNewsSend;
import com.eayun.news.service.NewsCountService;

@Transactional
@Service
public class NewsCountServiceImpl implements NewsCountService {
    private static final Logger log = LoggerFactory.getLogger(NewsCountServiceImpl.class);
	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
	private NewsSendDao newsSendDao;
	
	@Override
	public String pop(String key){
		String value = null;
        try {
            value = jedisUtil.pop(key);
        } catch (NullPointerException e) {
            log.error(e.getMessage(),e);
            return null;
        } catch (Exception e){
            log.error(e.getMessage(),e);
        }
        return value;
    }
	
	@Override
	public Set<String> unionSet(Set<String> setA,Set<String> setB){
		Set<String> unionSet = new HashSet<String>();
		Iterator<String> iterA = setA.iterator();
		unionSet.addAll(setB);
		while (iterA.hasNext()) {
			String tempInner = iterA.next();
			if (!setB.contains(tempInner)) {
				unionSet.add(tempInner);
			}
		}
		return unionSet;
	}
	
	@Override
	public void updateStatu(String newsId,int count){
		BaseNewsSend baseNewsSend = newsSendDao.findOne(newsId);
		baseNewsSend.setReaded(baseNewsSend.getReaded() + count);
		newsSendDao.saveOrUpdate(baseNewsSend);
	}
	
	@Override
	public void updateCollect(String newsId,int count){
		BaseNewsSend baseNewsSend = newsSendDao.findOne(newsId);
		baseNewsSend.setCollected(baseNewsSend.getCollected() + count);
		newsSendDao.saveOrUpdate(baseNewsSend);
	}
}
