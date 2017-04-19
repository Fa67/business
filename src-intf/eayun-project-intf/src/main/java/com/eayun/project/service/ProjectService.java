package com.eayun.project.service;

import java.util.List;

import javax.persistence.Query;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.virtualization.model.BaseCloudProject;
import com.eayun.virtualization.model.CloudProject;

/**
 * ProjectService
 * 
 * @Filename: ProjectService.java
 * @Description:
 * @Version: 1.0
 * @Author: chenhao
 * @Email: hao.chen@eayun.com
 * @History:<br> <li>Date: 2015年9月1日</li> <li>Version: 1.0</li> <li>Content:
 *               create</li>
 * 
 */
public interface ProjectService {

	/**
	 * 根据数据中心得到项目
	 * 
	 * @return
	 */
	List<CloudProject> getProjectListByDataCenter(String dcId);
	/**
	 * 返回当前客户下的所有项目
	 * @param cutomerId
	 * @return
	 */
	List<CloudProject> getProjectListByCustomer(String cutomerId);
	/**
	 * 根据ID查找项目
	 * @param projectId
	 * @return
	 */
	CloudProject findProject(String projectId);

	// 根据客户得到项目列表,将当前的项目放到首位。
	List<BaseCloudProject> getListByCustomerAndPrjId(String cusId, String prjId);
	
	List<CloudProject> getAllProjects();
	
	public boolean findProByDcId(String cusId , String dcId);
	
	/**
	 * 根据用户id得到项目列表（非超级管理员）
	 * @param userId
	 * @return
	 */
	List<CloudProject> getProjectListByUser(String cusId , String userId);
	/**
	 * 得到每个用户的管辖项目名称列表
	 * 		因对客户隐藏项目相关信息，此方法由查询项目名称改为查询数据中心名称
	 * @param sysUser
	 * @return
	 */
	List<String> getProNameListByUser(boolean isAdmin , String cusId , String userId);
	
	public void save (BaseCloudProject project);
	
	public void delete (String id);
	
	public List<BaseCloudProject> find(String hql,Object [] args);
	
	public void execSQL (String hql,Object [] args);
	
	List<CloudProject> getListByCusAndPrj(String cusId, String prjId);
	
	List<CloudProject> getListByPrj(String prjId);
	
	public Query createSQLNativeQuery(String sql , Object [] args);
	
	public Page pagedNativeQuery(String sql ,QueryMap queryMap,Object [] args);
	
	/**
	 * 根据数据中心Id和客户ID查询项目<br>
	 * -------------------------
	 * @author zhouhaitao
	 * @param dcId 			数据中心ID
	 * @param cusId			客户ID
	 * 
	 * @return
	 */
	public CloudProject queryProjectByDcAndCus(String dcId,String cusId);
	
	/**
	 * 查询项目下的配额信息和已使用配额信息（仅限云主机使用涉及的配额信息及订单中的资源）<br>
	 * ---------------------------------
	 * @author zhouhaitao
	 * 
	 * @param prjId
	 * @return
	 */
	public CloudProject queryProjectQuotaAndUsedQuotaForVm(String prjId);
}

