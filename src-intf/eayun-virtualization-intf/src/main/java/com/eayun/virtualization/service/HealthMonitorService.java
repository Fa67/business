package com.eayun.virtualization.service;

import java.util.List;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.virtualization.model.BaseCloudLdMonitor;
import com.eayun.virtualization.model.CloudLdMonitor;
import com.eayun.virtualization.model.CloudLdPool;

/**
 * HealthMonitorService
 * 
 * @Filename: HealthMonitorService.java
 * @Description:
 * @Version: 1.0
 * @Author: liyanchao
 * @Email: yanchao.li@eayun.com
 * @History:<br> <li>Date: 2015年11月12日</li> <li>Version: 1.0</li> <li>Content:
 *               create</li>
 * 
 */
public interface HealthMonitorService {
	
	/**
	 * 分页查询项目的健康检查
	 * -----------------------
	 * @author zhouhaitao
	 * @param page
	 * @param dcId
	 * @param prjId
	 * @param ldmName
	 * @param queryMap
	 * @return
	 */
	public Page getMonitorList(Page page,String dcId,String prjId,String ldmName ,QueryMap queryMap);
	
	/**
	 * 
	 *根据prjId查询个数 
	 */
	public int getCountByPrjId(String prjId);
	
	/**
	 * 查询项目下的健康检查
	 * 
	 * @author zhouhaitao
	 * @param pool
	 * @return
	 */
	public List <CloudLdMonitor>getMonitorListByPool(CloudLdPool pool);
	
	/**
	 * 健康检查
	 * ------------------
	 * @author zhouhaitao
	 * @param pool
	 * @return
	 * @throws AppException
	 */
	public List<CloudLdMonitor> bindHealthMonitor(CloudLdPool pool);
	
	/**
	 * 添加健康检查
	 * ------------------
	 * @author zhouhaitao
	 * @param monitor
	 * @param sessionUser
	 */
	public BaseCloudLdMonitor addHealthMonitor(CloudLdMonitor monitor,SessionUserInfo sessionUser);
	
	/**
	 * 修改健康检查
	 * --------------------
	 * @author zhouhaitao
	 * @param monitor
	 * @return
	 */
	public BaseCloudLdMonitor updateMonitor(CloudLdMonitor monitor);
	
	/**
	 * 删除健康检查
	 * ------------------
	 * @author zhouhaitao
	 * @param monitor
	 * @return
	 */
	public boolean deleteMonitor(CloudLdMonitor monitor);
	
	/**
	 * 校验健康检查重名
	 * ------------------
	 * @author zhouhaitao
	 * @param monitor
	 * @return
	 */
	public boolean bindHealthMonitor(CloudLdMonitor monitor);

	/**
	 * 根据id查询健康检查
	 * @param monitor
	 * @return
	 * @throws Exception
     */
	public CloudLdMonitor getHealthMonitor(String monitor) throws Exception;
	
	/**
	 * 为负载均衡绑定健康检查
	 * ------------------
	 * @author caoxiangyu
	 * @param pool
	 * @return
	 * @throws AppException
	 */
	public CloudLdMonitor bindHealthMonitorForPool(CloudLdPool pool);
	/**
	 * 为负载均衡解除健康检查
	 * ------------------
	 * @author caoxiangyu
	 * @param pool
	 * @return
	 * @throws AppException
	 */
	public List<CloudLdMonitor> unBindHealthMonitorForPool(CloudLdPool pool) throws Exception;
	/**
	 * 查询该负载均衡绑定的健康检查
	 * @param poolId
	 * @return
	 * @throws Exception
	 */
	public CloudLdMonitor getHealthMonitorByPool(CloudLdPool pool) throws Exception;
}
