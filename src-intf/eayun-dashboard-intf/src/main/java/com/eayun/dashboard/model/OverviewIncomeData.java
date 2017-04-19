package com.eayun.dashboard.model;

import java.util.ArrayList;
import java.util.List;

public class OverviewIncomeData extends BaseOverviewIncomeData {
	
	/**
	 * 按收入金额降序排序top5
	 */
	private List<OverviewIncomeChart> cusIncomeTops = new ArrayList<OverviewIncomeChart>();
	
	/**
	 * 按按需消费金额降序排序top5
	 */
	private List<OverviewIncomeChart> cusConsumeAsNeededTops = new ArrayList<OverviewIncomeChart>();

	public List<OverviewIncomeChart> getCusIncomeTops() {
		return cusIncomeTops;
	}

	public void setCusIncomeTops(List<OverviewIncomeChart> cusIncomeTops) {
		this.cusIncomeTops = cusIncomeTops;
	}

	public List<OverviewIncomeChart> getCusConsumeAsNeededTops() {
		return cusConsumeAsNeededTops;
	}

	public void setCusConsumeAsNeededTops(List<OverviewIncomeChart> cusConsumeAsNeededTops) {
		this.cusConsumeAsNeededTops = cusConsumeAsNeededTops;
	}



}
