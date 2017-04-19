package com.eayun.virtualization.dao;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.virtualization.model.BaseCloudFloatIp;
import com.eayun.virtualization.model.BaseCloudVm;

public interface CloudFloatIpDao extends IRepository<BaseCloudFloatIp , String> {

    @Query("select count(*) from BaseCloudFloatIp bc where bc.prjId = ? and resourceId is not null and resourceId <>'' ")
    public int getCountByPro(String prjId);
    
    @Query("select count(*) from BaseCloudFloatIp bc where bc.prjId = ? and bc.isDeleted='0'")
    public int getCountByPrjId(String prjId);
    
    @Query("select count(*) from BaseCloudFloatIp bc where bc.prjId = ? and bc.isDeleted= '0' and bc.isVisable = '1'")
    public int getCountByPrjIdVisibled(String prjId);
    
    @Query("select new map(cs.floId as floId,cs.floIp as floIp,cs.createTime as createTime,cp.prjName as prjName,cs.resourceId as resourceId,cs.netId as netId,dc.name as dcname) from BaseCloudFloatIp cs,BaseCloudProject cp,BaseDcDataCenter dc where cs.prjId = cp.projectId and cs.dcId = dc.id and cs.floIp >= ? and cs.floIp<= ? ")
    public List<Map<String, Object>> findSubNetWorkByNetIdAndGatewayIp(String floIpmin,String floIpmax);
    
    @Query(" from BaseCloudVm vm where vm.vmId = ? ")
    public List<BaseCloudVm> findFloatIpOne(String vmId);
    
    /**
	 * 获取绑定了云主机的浮动IP数量
	 * @author zengbo
	 * @return
	 */
    @Query("select count(*) from BaseCloudFloatIp where prjId = ? and resourceId is not null and isDeleted='0' and isVisable = '1'")
    public int findBindCountByPriId(String prjId);
    
    @Modifying
    @Query("update BaseCloudFloatIp t set t.resourceId = ?2, t.resourceType = ?3 where t.floId = ?1")
    public int updateResourceByFloatId(String floatId, String resourceId, String resourceType);
    
    /**
     * 查询项目下未绑定云主机的浮动 IP列表
     * @author liujingang
     * @param prjId
     * @return List<BaseCloudFloatIp>
     */
    @Query("from BaseCloudFloatIp where prjId = ? and resourceId is null and isDeleted = '0' and chargeState = '0' and isVisable = '1' order by floIp desc")
    public List<BaseCloudFloatIp> getUnBindFloatIp(String prjId);
    
    /**
     * 查询某资源绑定的弹性公网IP
     * @Author: duanbinbin
     * @param resourceId
     * @param resourceType
     * @return
     *<li>Date: 2016年4月26日</li>
     */
    @Query("from BaseCloudFloatIp where resourceId = ? and resourceType = ? and isDeleted = '0'")
    public List<BaseCloudFloatIp> getFloatIpByResourceId(String resourceId,String resourceType);

}
