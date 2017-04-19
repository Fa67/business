package com.eayun.syssetup.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eayun.common.dao.IRepository;
import com.eayun.syssetup.model.BaseEcmcSysDataTree;

public interface EcmcSysDataTreeDao extends IRepository<BaseEcmcSysDataTree, String>{
	
	@Query("select max(nodeId) from BaseEcmcSysDataTree where parentId=?")
	public String findMaxId(String parentId);
	
	@Query("select new BaseEcmcSysDataTree(dt1.nodeId as nodeId, dt2.parentId as parentId) from BaseEcmcSysDataTree dt1, BaseEcmcSysDataTree dt2 where dt2.nodeId = dt1.parentId")
	public List<BaseEcmcSysDataTree> findChildDataTree();
	
	@Query("from BaseEcmcSysDataTree where nodeNameEn is not null")
	public List<BaseEcmcSysDataTree> findStatusDataTree();
	
	@Modifying
	@Query("update BaseEcmcSysDataTree set sort = :sort where nodeId = :nodeId")
	public void updateDataTreeSort(@Param("nodeId") String nodeId, @Param("sort") long sort);

	@Query("from BaseEcmcSysDataTree where parentId = ? and flag = 1 order by sort asc")
	public List<BaseEcmcSysDataTree> findDataTreeChildren(String parentId);
	
	@Query("select max(sort) from BaseEcmcSysDataTree where parentId = ?")
	public Long findMaxSortByPid(String parentId);
}
