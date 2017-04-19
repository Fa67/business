package com.eayun.common.dao.jpa;

import java.util.List;

import javax.persistence.Query;

/**
 * JPA 查询对象 工具
 * @author zhujun
 * @date 2016年3月30日
 *
 */
public class JpaQueryUtils {

	private JpaQueryUtils() {
		
	}
	
	/**
	 * 设置参数
	 * @param query
	 * @param params
	 */
	@SuppressWarnings("rawtypes")
    public static void setParameters(Query query, List params) {
		if (params == null || params.isEmpty()) {
			return;
		}
		
		for (int i = 0; i < params.size(); i++) {
			query.setParameter(i+1, params.get(i));
		}
	}
	
	/**
	 * 设置分页参数
	 * 
	 * @param query
	 * @param pageNo 页号
	 * @param pageSize 页大小
	 */
	public static void setPage(Query query, Integer pageNo, Integer pageSize) {
		pageNo = (pageNo == null ? 1 : pageNo);
		pageSize = (pageSize == null ? 20 : pageSize);
		query.setMaxResults(pageSize).setFirstResult(pageSize * (pageNo - 1));
	}
	
	
}
