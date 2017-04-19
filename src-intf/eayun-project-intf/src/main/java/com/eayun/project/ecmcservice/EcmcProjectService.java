package com.eayun.project.ecmcservice;

import java.util.List;
import java.util.Map;

import com.eayun.common.exception.AppException;
import com.eayun.customer.model.Customer;
import com.eayun.virtualization.model.BaseCloudProject;
import com.eayun.virtualization.model.CloudProject;

public interface EcmcProjectService {

	/**
	 * 创建项目
	 * @param baseCloudProject	项目实体类
	 * @param baseCustomer	客户实体类
	 * @return 
	 * @throws Exception 
	 */
	public Map<String, Object> createProject(CloudProject cloudProject, Customer customer, boolean isProjectOnly) throws Exception;

	/**
	 * 根据客户查询项目列表
	 * @param customerId
	 * @return
	 */
	public List<CloudProject> getProjectByCustomer(String customerId);

	/**
	 * 根据ID获取项目信息
	 * @param projectId
	 * @throws Exception 
	 */
	public CloudProject getProjectById(String projectId) throws Exception;

	/**
	 * 获取项目资源池信息
	 * @param projectId
	 * @return
	 * @throws Exception 
	 */
	public CloudProject getProjectQuotaPool(String projectId) throws Exception;

	/**
	 * 更新项目信息
	 * @param baseProject
	 * @return 
	 * @throws Exception 
	 */
	public BaseCloudProject updateProject(BaseCloudProject baseProject) throws Exception;

	/**
	 * 删除项目信息
	 * @param projectId
	 * @return
	 */
	public BaseCloudProject deleteProject(String projectId);
	
	/**
	 * 根据数据中心查询项目数量
	 * @param dcId
	 * @return
	 */
	public int getCountByDataCenterId(String dcId);
	/**
	 * 根据数据中心ID获取项目集合
	 * @param datacenterId
	 * @return
	 * @throws AppException
	 * @throws Exception 
	 */
	public List<CloudProject> firewallProject(String datacenterId) throws AppException, Exception;

	/**
	 * 判断客户在所选择的数据中心下是否有项目
	 * @param dcId 数据中心ID
	 * @param cusId 客户ID
	 * @return
	 */
	public boolean hasProjectByCustomerAndDc(String dcId, String cusId);

	Map<String, String> hasResource(String prjId) throws Exception;

	/**
	 * 根据数据中心ID取得全部的项目信息
	 * @param dcId
	 * @return
	 * @throws AppException
     */
	public List<BaseCloudProject> getByDataCenterId(String dcId) throws AppException ;

}
