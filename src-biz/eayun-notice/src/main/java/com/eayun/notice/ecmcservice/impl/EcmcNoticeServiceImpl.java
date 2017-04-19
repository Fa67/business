package com.eayun.notice.ecmcservice.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.notice.dao.NoticeDao;
import com.eayun.notice.ecmcservice.EcmcNoticeService;
import com.eayun.notice.model.BaseNotice;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年4月1日
 */
@Service
@Transactional
public class EcmcNoticeServiceImpl implements EcmcNoticeService {

	@Autowired
	private NoticeDao noticedao;
	
	@Override
	public Page queryNoticeList(Page page, QueryMap querymap, Date beginTime, Date endTime, String memo,
			String isUsed,String title) throws Exception {
		List<Object> map = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer("select notice_id,memo,valid_time,invalid_time,is_used,notice_url,title,content from notice_info where 1=1 ");
		
		if(null != beginTime){
			hql.append(" and valid_time >= ? ");
			map.add(beginTime);
		}
		if(null != endTime){
			hql.append(" and valid_time <= ? ");
			map.add(endTime);
		}
		if(null != memo && !memo.trim().equals("")){
			hql.append(" and memo like ? ");
			memo = memo.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
			map.add("%" + memo + "%");
		}
		if(null != title && !title.trim().equals("")){
			hql.append(" and title like ? ");
			title = title.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
			map.add("%" + title + "%");
		}
		if(null != isUsed && !isUsed.trim().equals("")){
			hql.append(" and memo is_used = ? ");
			map.add(isUsed);
		}
		hql.append(" order by valid_time desc");
		
		page = noticedao.pagedNativeQuery(hql.toString(), querymap, map.toArray());
		List newlist = (List) page.getResult();
        int a = newlist.size();
        BaseNotice notice = null;
        for (int i = 0; i < newlist.size(); i++) {
            Object[] objs = (Object[]) newlist.get(i);
            notice = new BaseNotice();
            notice.setId(String.valueOf(objs[0]));
            notice.setMemo(String.valueOf(objs[1]));
            notice.setValidTime((Date)objs[2]);
            notice.setInvalidTime((Date) objs[3]);
            notice.setIsUsed(String.valueOf(objs[4]));
            notice.setUrl(String.valueOf(objs[5]));
            notice.setTitle(String.valueOf(objs[6]));
            notice.setContent(String.valueOf(objs[7]));
            newlist.set(i, notice);
        }
		return page;
	}

	@Override
	public void save(BaseNotice notice) throws Exception {
		noticedao.saveOrUpdate(notice);
	}

	@Override
	public void edit(BaseNotice notice) throws Exception {
		noticedao.saveOrUpdate(notice);
	}

	@Override
	public void deleteNotice(BaseNotice notice) throws Exception {
		noticedao.delete(notice);
	}

}
