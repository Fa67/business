package com.eayun.dashboard.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eayun.common.dao.IRepository;
import com.eayun.dashboard.model.BaseOverviewIncomeData;

public interface OverviewIncomeDataDao extends IRepository<BaseOverviewIncomeData, String>{
	
}
