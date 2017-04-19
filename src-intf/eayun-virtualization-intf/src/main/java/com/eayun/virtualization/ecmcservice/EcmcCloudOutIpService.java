package com.eayun.virtualization.ecmcservice;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.virtualization.model.BaseCloudOutIp;
import com.eayun.virtualization.model.CloudOutIp;

/**
 * @author jingang.liu@eayun.com to beijing
 * @date 2016年4月13日
 */
public interface EcmcCloudOutIpService {

	/**
	 * IP分页集合
	 * @param page
	 * @param datacenterId
	 * @param usestauts
	 * @param distribution
	 * @param ip
	 * @param querymap
	 * @return
	 * @throws AppException
	 */
	public Page list(Page page ,String datacenterId, String usestauts,String distribution, String ip,String[] pns,String[] cuss,QueryMap querymap) throws AppException;
	
	/**
	 * 添加IP
	 * @param outip
	 * @return
	 * @throws AppException
	 */
	public boolean createOutIp(String cidr,String pooldate,String dcId,String netId,String subnetId,String routeId,String ipVersion) throws AppException;
	
	/**
	 * 修改IP
	 * @param outip
	 * @return
	 * @throws AppException
	 */
	public boolean updateOutIp(BaseCloudOutIp outip) throws AppException;
	
	/**
	 * 删除IP
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public boolean deleteOutIp(String subnetId) throws AppException;
	
	/**
	 * 查询详细
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public CloudOutIp queryByOne(String id) throws AppException;
	
}
