package com.eayun.virtualization.ecmcservice;

import java.util.List;
import java.util.Map;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.Firewall;
import com.eayun.virtualization.model.BaseCloudFireWall;
import com.eayun.virtualization.model.CloudFireWall;
import com.eayun.virtualization.model.CloudProject;

/**
 * @author jingang.liu@eayun.com to beijing
 * @date 2016年4月12日
 */
public interface EcmcCloudFireWallService {

	/**
	 * 防火墙页面的展示
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param name
	 * @return
	 * @throws AppException
	 */
	public Page list(Page page ,String datacenterId,String projectId,String name,String cus_org,QueryMap querymap) throws AppException;
	
	/**
	 * 检查防火墙名称是否存在
	 * @param name
	 * @return
	 * @throws AppException
	 */
	public boolean checkName(String name,String datacenterId,String projectId,String fwId)throws AppException;
	
	/**
	 * 根据ID获取防火墙
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public CloudFireWall getFWById(String id) throws AppException;
	
	/**
	 * 创建防火墙
	 * @param parmes
	 * @return
	 * @throws AppException
	 */
	public Firewall create(Map<String, String> parmes) throws AppException;
	
	/**
	 * 修改防火墙
	 * @param parmes
	 * @return
	 * @throws AppException
	 */
	public Firewall update(Map<String, String> parmes) throws AppException;
	
	/**
	 * 通过id获取防火墙信息，删除防火墙
	 * @param datacenterId
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public boolean delete(String datacenterId,String projectId,String id) throws AppException;
	
	/**
	 * 获取没有防火墙的项目
	 * 原：判断每个项目下只有一个防火墙
	 * @param datacenterId
	 * @return
	 * @throws AppException
	 */
	public List<CloudProject> projects(String datacenterId)throws AppException;
	/**
	 * 根据数据中心ID获取防火墙
	 * @param datacenterId
	 * @return
	 * @throws AppException
	 */
	public List<BaseCloudFireWall> getfirewallBydcId(String datacenterId) throws AppException;

	int countFireWallByPrjId(String prjId) throws AppException;
	
	/**
	 * 创建防火墙
	 * 同时创建策略和规则，策略是默认的，规则是根据参数创建
	 * @param parmes
	 * @return
	 * @throws AppException
	 */
	public Firewall createFwAndFwpAndRule(Map<String, String> parmes) throws AppException;
	
	/**
	 * 删除防火墙
	 * 同时删除策略和规则
	 * @param parmes
	 * @return
	 * @throws AppException
	 */
	public boolean deleteFwAndFwpAndRule(Map<String, String> parmes) throws AppException;
	
	/**
	 * 查询防火墙详情
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public CloudFireWall getFwByIdDetail(String id) throws AppException;
	
}
