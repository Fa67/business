package com.eayun.dashboard.api.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eayun.common.dao.IRepository;
import com.eayun.dashboard.api.model.BaseApiSwitchPhone;

public interface ApiSwitchDao extends IRepository<BaseApiSwitchPhone, String> {
	
	@Query("select asp.phone from BaseApiSwitchPhone asp where asp.phone is not null and asp.phone <> '' and type=:type ")
    public List<String> getPublicPhone(@Param("type") String type);
	
	@Query("from BaseApiSwitchPhone t where  type=:type ")
	public List<BaseApiSwitchPhone> getVolumeTypePhone(@Param("type") String type);
}
