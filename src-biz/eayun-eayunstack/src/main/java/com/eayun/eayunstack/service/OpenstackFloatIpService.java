package com.eayun.eayunstack.service;

import java.util.List;

import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.FloatIp;
import com.eayun.virtualization.model.BaseCloudFloatIp;
import com.eayun.virtualization.model.CloudFloatIp;

public interface OpenstackFloatIpService {

    /**
     * 分配浮动iP给项目
     * @param datacenterId
     * @param projectId
     * @param pool
     * @return
     * @throws AppException
     */
	public FloatIp allocateIp(String datacenterId, String projectId, String pool)
			throws AppException;

	/**
	 * 释放浮动IP
	 * @param datacenterId
	 * @param projectId
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public boolean deallocateFloatIp(String datacenterId, String projectId,
			String id) throws AppException;
	/**
	 * 云主机绑定浮动IP
	 * @param datacenterId
	 * @param projectId
	 * @param vmId
	 * @param vmIp
	 * @param address
	 * @return
	 * @throws AppException
	 */
	public boolean addFloatIp(String datacenterId, String projectId, String vmId,String vmIp,
			String address) throws AppException;
	/**
	 * 获取列表（底层）
	 * @param datacenterId
	 * @return
	 * @throws AppException
	 */
	public List<FloatIp> listAll(String datacenterId) throws AppException;

	/**
	 * 获取列表（底层）
	 * @param datacenterId
	 * @param projectId
	 * @return
	 * @throws AppException
	 */
	public List<FloatIp> list(String datacenterId, String projectId)
			throws AppException;

	/**
	 * 获取一条
	 * @param datacenterId
	 * @param projectId
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public FloatIp getById(String datacenterId, String projectId, String id)
			throws AppException;

	/**
	 * 解绑浮动IP
	 * @param datacenterId
	 * @param projectId
	 * @param vm
	 * @param address
	 * @return
	 * @throws AppException
	 */
	public boolean removeFloatIp(String datacenterId, String projectId,
			String vm, String address) throws AppException;
	
	public List<BaseCloudFloatIp> getStackList (BaseDcDataCenter dataCenter,String prjId);
	
	/**
	 * 绑定负载均衡器的浮动IP
	 * 
	 * @author zhouhaitao
	 * @param datacenterId
	 * @param projectId
	 * @param portId    portId==null解绑
	 * @param floatId
	 * @return
	 * @throws AppException
	 */
	public boolean bindLoadBalancerFloatIp(String datacenterId, String projectId, String portId,String floatId) throws AppException;
	
	/**
	 * 查询底层network服务下的port
	 * -----------------
	 * @author zhouhaitao
	 * @param dataCenter
	 * @return
	 */
	public List<CloudFloatIp> getPoolFloatIpList (BaseDcDataCenter dataCenter);
}