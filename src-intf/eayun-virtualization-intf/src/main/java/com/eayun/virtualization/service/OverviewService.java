package com.eayun.virtualization.service;

import java.util.List;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.virtualization.model.CloudProject;

public interface OverviewService {
    public CloudProject findStatisticsByPrjId(String prjId);

    /**
     * 获取该登录客户已创建有项目，且登录用户有权限的数据中心列表
     * model包含四个属性prjId,prjName,dcId和dcName
     * @param sessionUser
     * @return
     */
	public List<CloudProject> getValidDcList(SessionUserInfo sessionUser);

	public Page getToExpireResources(Page page, QueryMap queryMap,
			String cusId, String prjId);

	/**
	 * 总览页获取待支付订单列表
	 * @param page
	 * @param queryMap
	 * @param cusId
	 * @return
	 */
	public Page getToPayOrderPage(Page page, QueryMap queryMap, String cusId);
}
