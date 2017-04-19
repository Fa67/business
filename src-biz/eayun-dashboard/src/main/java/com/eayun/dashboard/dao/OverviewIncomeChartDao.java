package com.eayun.dashboard.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.dashboard.model.BaseOverviewIncomeChart;

public interface OverviewIncomeChartDao extends IRepository<BaseOverviewIncomeChart, String> {

	
	public List<BaseOverviewIncomeChart> findByYear(String year);

}
