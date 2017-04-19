package com.eayun.virtualization.ecmcservice;

import java.util.List;
import java.util.Map;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.virtualization.model.BaseCloudPortMapping;
import com.eayun.virtualization.model.CloudPortMapping;

public interface EcmcPortMappingService {
	
	/**
	 * 获取端口映射列表
	 * @param page
	 * @param dcId
	 * @param prjId
	 * @param routeId
	 * @param queryMap
	 * @return
	 * @throws Exception
	 */
	public Page getPortMappingList(Page page, String dcId, String prjId,
            String routeId, QueryMap queryMap) throws Exception;

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
    /**
     * 检查端口映射源端口是否存在
     * @param params
     * @return
     */
	public boolean checkResourcePort(Map<String, String> params);
	/**
	 * 根据端口映射云主机id删除对应的端口映射关系
	 * @param destinyId
	 * @return
	 */
	public boolean deletePortMappingListByDestinyId(String dcId, String prjId, String destinyId);
}
