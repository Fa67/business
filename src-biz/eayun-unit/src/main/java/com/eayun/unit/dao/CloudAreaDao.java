package com.eayun.unit.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.unit.model.BaseCloudArea;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年12月22日
 */
public interface CloudAreaDao extends IRepository<BaseCloudArea, String> {

	/**
	 * 查询第一级行政区域
	 * @return
	 */
	@Query("from BaseCloudArea  where parentCode is null and code not in('710000','810000','820000') order by code ")
	public List<BaseCloudArea> getParentArea();
	/**
	 * 查询子集行政区域
	 * @param parentcode
	 * @return
	 */
	@Query(" from BaseCloudArea  where parentCode = ? order by code ")
	public List<BaseCloudArea> getArea(String parentcode);
	
}
