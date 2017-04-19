package com.eayun.accesskey.dao;


import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.eayun.accesskey.model.BaseAccessKey;
import com.eayun.common.dao.IRepository;

public interface AccessKeyDao extends IRepository<BaseAccessKey, String> {

	/**
     * 查询客户下的非默认且启用状态的ak
     * @author liyanchao
     * @return
     */
	 @Query("from BaseAccessKey ak where ak.userId = ? and ak.acckState= '0' and ak.isDefault = '1' ")
	 public List<BaseAccessKey> getRunningAkExceptDefaultByCusId(String cusId);
	
	/**
     * 查询客户下的全部aksk
     * @author liyanchao
     * @return
     */
	 @Query("from BaseAccessKey ak where ak.userId = ? ")
	 public List<BaseAccessKey> getAkByCusId(String cusId);
	
}
