package com.eayun.eayunstack.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eayun.common.exception.AppException;
import com.eayun.common.util.StringUtil;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.datacenter.model.DcDataCenter;
import com.eayun.datacenter.service.DataCenterService;
import com.eayun.eayunstack.service.RestService;
import com.eayun.eayunstack.util.RestTokenBean;
import com.eayun.project.service.ProjectService;

@Service
public abstract class OpenstackBaseServiceImpl<T> {
	private static final Log log = LogFactory
			.getLog(OpenstackBaseServiceImpl.class);

	@Autowired
	protected DataCenterService dataCenterService;
	
	@Autowired
	protected ProjectService projectService;

	@Autowired
	protected RestService restService;

	/**
	 * @param datacenterId
	 * @param projectId
	 * @param serviceName
	 * @return
	 * @throws AppException
	 */
	protected RestTokenBean getRestTokenBean(String datacenterId,
			String projectId, String serviceName) throws AppException {
		RestTokenBean restTokenBean = new RestTokenBean();
		BaseDcDataCenter dataCenter = dataCenterService.getById(datacenterId);
		if (dataCenter == null) {
			throw new AppException("数据中心不存在！");
		}

		restTokenBean.setGetTokenUrl(dataCenter.getDcAddress());
		restTokenBean.setUserName(dataCenter.getVCenterUsername());
		restTokenBean.setPassword(dataCenter.getVCenterPassword());
		restTokenBean.setTenantId(projectId);
		restTokenBean.setServiceName(serviceName);
		restTokenBean.setKeyStoneRegion(dataCenter.getOsKeystoneRegion());
		restTokenBean.setCommonRegion(dataCenter.getOsCommonRegion());
		if(!StringUtil.isEmpty(dataCenter.getCommonRegionUrlType())){
			restTokenBean.setCommonRegionUrlType(dataCenter.getCommonRegionUrlType());
		}
		
		return restTokenBean;
	}

	/**
	 * @param datacenterId
	 * @param serviceName
	 * @return
	 * @throws AppException
	 */
	protected RestTokenBean getRestTokenBean(String datacenterId,
			String serviceName) throws AppException {
		RestTokenBean restTokenBean = new RestTokenBean();
		BaseDcDataCenter dataCenter = dataCenterService.getById(datacenterId);
		if (dataCenter == null) {
			throw new AppException("数据中心不存在！");
		}

		restTokenBean.setGetTokenUrl(dataCenter.getDcAddress());
		restTokenBean.setUserName(dataCenter.getVCenterUsername());
		restTokenBean.setPassword(dataCenter.getVCenterPassword());
		restTokenBean.setTenantId(dataCenter.getOsAdminProjectId());
		restTokenBean.setServiceName(serviceName);
		restTokenBean.setKeyStoneRegion(dataCenter.getOsKeystoneRegion());
		restTokenBean.setCommonRegion(dataCenter.getOsCommonRegion());
		if(!StringUtil.isEmpty(dataCenter.getCommonRegionUrlType())){
			restTokenBean.setCommonRegionUrlType(dataCenter.getCommonRegionUrlType());
		}

		return restTokenBean;
	}

	/**
	 * @param dataCenter
	 * @param serviceName
	 * @return
	 * @throws AppException
	 */
	@Deprecated
	protected RestTokenBean getRestTokenBean(BaseDcDataCenter dataCenter,
			String serviceName) throws AppException {
		RestTokenBean restTokenBean = new RestTokenBean();
		if (dataCenter == null) {
			throw new AppException("数据中心不存在！");
		}

		restTokenBean.setGetTokenUrl(dataCenter.getDcAddress());
		restTokenBean.setUserName(dataCenter.getVCenterUsername());
		restTokenBean.setPassword(dataCenter.getVCenterPassword());
		restTokenBean.setTenantId(dataCenter.getOsAdminProjectId());
		restTokenBean.setServiceName(serviceName);
		restTokenBean.setKeyStoneRegion(dataCenter.getOsKeystoneRegion());
		restTokenBean.setCommonRegion(dataCenter.getOsCommonRegion());
		if(!StringUtil.isEmpty(dataCenter.getCommonRegionUrlType())){
			restTokenBean.setCommonRegionUrlType(dataCenter.getCommonRegionUrlType());
		}
		
		return restTokenBean;
	}
	
	/**
	 * @param dataCenter
	 * @param serviceName
	 * @return
	 * @throws AppException
	 */
	protected RestTokenBean getRestTokenBean(DcDataCenter dataCenter,
	                                         String serviceName) throws AppException {
	    RestTokenBean restTokenBean = new RestTokenBean();
	    if (dataCenter == null) {
	        throw new AppException("数据中心不存在！");
	    }
	    
	    restTokenBean.setGetTokenUrl(dataCenter.getDcAddress());
	    restTokenBean.setUserName(dataCenter.getVCenterUsername());
	    restTokenBean.setPassword(dataCenter.getVCenterPassword());
	    restTokenBean.setTenantId(dataCenter.getOsAdminProjectId());
	    restTokenBean.setServiceName(serviceName);
	    restTokenBean.setKeyStoneRegion(dataCenter.getOsKeystoneRegion());
	    restTokenBean.setCommonRegion(dataCenter.getOsCommonRegion());
	    if(!StringUtil.isEmpty(dataCenter.getCommonRegionUrlType())){
			restTokenBean.setCommonRegionUrlType(dataCenter.getCommonRegionUrlType());
		}
	    
	    return restTokenBean;
	}

	/**
	 * @param dataCenter
	 * @param projectId
	 * @param serviceName
	 * @return
	 * @throws AppException
	 */
	@Deprecated
	protected RestTokenBean getRestTokenBean(BaseDcDataCenter dataCenter,
			String projectId, String serviceName) throws AppException {
		RestTokenBean restTokenBean = new RestTokenBean();
		if (dataCenter == null) {
			throw new AppException("数据中心不存在！");
		}

		restTokenBean.setGetTokenUrl(dataCenter.getDcAddress());
		restTokenBean.setUserName(dataCenter.getVCenterUsername());
		restTokenBean.setPassword(dataCenter.getVCenterPassword());
		restTokenBean.setTenantId(projectId);
		restTokenBean.setServiceName(serviceName);
		restTokenBean.setKeyStoneRegion(dataCenter.getOsKeystoneRegion());
		restTokenBean.setCommonRegion(dataCenter.getOsCommonRegion());
		if(!StringUtil.isEmpty(dataCenter.getCommonRegionUrlType())){
			restTokenBean.setCommonRegionUrlType(dataCenter.getCommonRegionUrlType());
		}
		
		return restTokenBean;
	}
	
	/**
	 * @param dataCenter
	 * @param projectId
	 * @param serviceName
	 * @return
	 * @throws AppException
	 */
	protected RestTokenBean getRestTokenBean(DcDataCenter dataCenter,
	                                         String projectId, String serviceName) throws AppException {
	    RestTokenBean restTokenBean = new RestTokenBean();
	    if (dataCenter == null) {
	        throw new AppException("数据中心不存在！");
	    }
	    
	    restTokenBean.setGetTokenUrl(dataCenter.getDcAddress());
	    restTokenBean.setUserName(dataCenter.getVCenterUsername());
	    restTokenBean.setPassword(dataCenter.getVCenterPassword());
	    restTokenBean.setTenantId(projectId);
	    restTokenBean.setServiceName(serviceName);
	    restTokenBean.setKeyStoneRegion(dataCenter.getOsKeystoneRegion());
	    restTokenBean.setCommonRegion(dataCenter.getOsCommonRegion());
	    if(!StringUtil.isEmpty(dataCenter.getCommonRegionUrlType())){
			restTokenBean.setCommonRegionUrlType(dataCenter.getCommonRegionUrlType());
		}
	    
	    return restTokenBean;
	}
}