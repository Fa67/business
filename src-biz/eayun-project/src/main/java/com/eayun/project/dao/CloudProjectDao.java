package com.eayun.project.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eayun.common.dao.IRepository;
import com.eayun.virtualization.model.BaseCloudProject;

public interface CloudProjectDao extends IRepository<BaseCloudProject, String> {
	
	@Query("from BaseCloudProject t where t.dcId =:dcId")
	public List<BaseCloudProject> getListByDataCenter(@Param("dcId") String dcId);
	
	@Query("from BaseCloudProject t where 1=1")
	public List<BaseCloudProject> getAllList();
	
	@Query("from BaseCloudProject t where 1=1 and customerId = ?")
    public List<BaseCloudProject> getProjectListByCustomer(String customerId);
	/*分1 2 两步查询,根据查询；第1步*/
	@Query("from BaseCloudProject t where 1=1 and customerId = ? and projectId = ?")
    public List<BaseCloudProject> getProjectListByCusIdAndprjIdFirst(String customerId,String projectId);
	/*分1 2 两步查询,根据查询；第2步*/
	@Query("from BaseCloudProject t where 1=1 and customerId = ? and projectId <> ?")
    public List<BaseCloudProject> getProjectListByCusIdAndprjIdSecond(String customerId,String projectId);
	
	
	@Query("select t.prjName from BaseCloudProject t where t.customerId = ?")
    public List<String> getProNameListByCusId(String customerId);
	
	@Query("select count(projectId) from BaseCloudProject t where t.customerId = ? and t.dcId = ? ")
    public int findProByDcId(String cusId, String dcId);
	
	/**
	 * 查询项目总数
	 * @author zengbo
	 * @return
	 */
	@Query("select count(*) from BaseCloudProject")
    public int getAllCount();
	
	@Query("from BaseCloudProject t where 1=1 and dcId = ? and customerId = ? ")
    public List<BaseCloudProject> getProjectByDcIdAndCusId(String dcId,String customerId);
	
}
