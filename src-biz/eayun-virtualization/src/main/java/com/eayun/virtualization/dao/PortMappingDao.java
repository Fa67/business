package com.eayun.virtualization.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eayun.common.dao.IRepository;
import com.eayun.virtualization.model.BaseCloudPortMapping;

public interface PortMappingDao extends IRepository<BaseCloudPortMapping, String> {
	@Query("select count(*) from BaseCloudPortMapping where prjId=?")
    public int getCountByPrjId(String prjId);
	/**
	 * 获取网络创建的端口映射的数量
	 * @param resourceId
	 * @author liuzhuangzhuang
	 * @return
	 */
	@Query("select count(*) from BaseCloudPortMapping where resourceId=?")
    public int getCountByRouteId(String resourceId);
	
	/**
	 * 获取指定路由下指定端口的数量
	 * @param resourceId
	 * @param resourcePort
	 * @return
	 * @author liuzhuangzhuang
	 */
	@Query("select count(*) from BaseCloudPortMapping where (pmId<>:pmId or :pmId = null) and resourceId=:resourceId and protocol = :protocol and resourcePort = :resourcePort")
	public int countMultiResourcePort(@Param("pmId") String pmId, @Param("resourceId") String resourceId, @Param("protocol") String protocol, @Param("resourcePort") String resourcePort);
}
