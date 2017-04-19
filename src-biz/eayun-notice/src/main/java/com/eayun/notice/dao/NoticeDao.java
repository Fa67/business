package com.eayun.notice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.customer.model.BaseUser;
import com.eayun.notice.model.BaseNotice;

public interface NoticeDao extends IRepository<BaseNotice,String>{
	@Query("from BaseUser  where  isAdmin=?  And cusId =?")
	public List<BaseUser> getPhoneandEmail(Boolean isadmin,String cusid);

}
