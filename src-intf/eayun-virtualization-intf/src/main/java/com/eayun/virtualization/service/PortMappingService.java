package com.eayun.virtualization.service;

import java.util.List;
import java.util.Map;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.eayunstack.model.PortMapping;
import com.eayun.virtualization.model.BaseCloudPortMapping;
import com.eayun.virtualization.model.CloudPortMapping;

public interface PortMappingService {

	/**
	 * 获取端口映射数据表的接口
	 * @author gaoxiang
	 * @param dcId
	 * @param prjId
	 * @param routeId
	 * @param queryMap
	 * @return
	 * @throws Exception
	 */
	public Page getPortMappingList(Page page, String dcId, String prjId, String routeId, QueryMap queryMap) throws Exception;
	/**
	 * 添加端口映射关系
	 * @author gaoxiang
	 * @param cloudPortMapping
	 * @return
	 */
	public CloudPortMapping addPortMapping(CloudPortMapping cloudPortMapping);
	/**
	 * 修改端口映射关系
	 * @author gaoxiang
	 * @param cloudPortMapping
	 * @return
	 */
	public CloudPortMapping updatePortMapping(CloudPortMapping cloudPortMapping);
	/**
	 * 删除端口映射关系
	 * @author gaoxiang
	 * @param dcId
	 * @param prjId
	 * @param portMappingId
	 * @return
	 */
	public boolean deletePortMapping(String dcId, String prjId, String portMappingId);
	/**
	 * 根据端口映射云主机id删除对应的端口映射关系
	 * @param destinyId
	 * @return
	 */
	public boolean deletePortMappingListByDestinyId(String dcId, String prjId, String destinyId);
	/**
	 * 根据端口映射云主机id查询对应的端口映射关系
	 * @author gaoxiang
	 * @param destinyId
	 * @return
	 */
	public List<BaseCloudPortMapping> queryPortMappingListByDestinyId(String destinyId);
	/**
     * 根据项目查询端口映射使用量
     * @author gaoxiang
     * @param prjId
     * @return
     */
    public int getCountByPrjId(String prjId);
    
    public List<PortMapping> listAllPortMapping(String dcId);
    /**
     * 检查源端口是否重复
     * @param params
     * @return
     */
	public boolean checkResourcePort(Map<String, String> params);
}
