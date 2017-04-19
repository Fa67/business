package com.eayun.news.dao;

import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.news.model.BaseNewsRec;

public interface NewsRecDao extends IRepository<BaseNewsRec,String>{
	
	@Query("from BaseNewsRec where newsId=?")
	public BaseNewsRec getById(String id);

}
