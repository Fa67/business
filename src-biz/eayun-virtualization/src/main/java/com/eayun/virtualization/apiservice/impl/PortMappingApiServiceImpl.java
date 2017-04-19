package com.eayun.virtualization.apiservice.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.annotation.ApiService;
import com.eayun.eayunstack.service.OpenstackPortMappingService;
import com.eayun.virtualization.apiservice.PortMappingApiService;
import com.eayun.virtualization.baseservice.BasePortMappingService;
import com.eayun.virtualization.dao.PortMappingDao;
import com.eayun.virtualization.model.BaseCloudPortMapping;
/**
 * 
 * 端口映射api业务
 * @author gaoxiang
 * @date 2016-12-2
 *
 */
@ApiService
@Service
@Transactional
public class PortMappingApiServiceImpl extends BasePortMappingService implements PortMappingApiService {
    @Autowired
    private PortMappingDao portMappingDao;
    @Autowired
    private OpenstackPortMappingService openstackPortMappingService;
    
    /**
     * 获取绑定对象id的端口映射列表
     * @author gaoxiang
     * @param destinyId
     * @return
     */
    public List<BaseCloudPortMapping> queryPortMappingListByDestinyId(String destinyId) {
        StringBuffer hql = new StringBuffer();
        hql.append(" from ");
        hql.append("    BaseCloudPortMapping");
        hql.append(" where ");
        hql.append("    destinyId = ?");
        List<BaseCloudPortMapping> list = portMappingDao.find(hql.toString(), new Object[] { destinyId });
        return list;
    }
    
    /**
     * 删除对应id的端口映射
     * @author gaoxiang
     * @param dcId
     * @param prjId
     * @param portMappingId
     * @return
     */
    public boolean deletePortMapping(String dcId, String prjId, String portMappingId) {
        boolean deleteFlag = openstackPortMappingService.delete(dcId, prjId, portMappingId);
        if (deleteFlag) {
            portMappingDao.delete(portMappingId);
        }
        return deleteFlag;
    }
    
    /**
     * 删除绑定对象id的端口映射列表
     * @author gaoxiang
     * @param dcId
     * @param prjId
     * @param destinyId
     * @return
     */
    public boolean deletePortMappingListByDestinyId(String dcId, String prjId, String destinyId) {
        List<BaseCloudPortMapping> list = queryPortMappingListByDestinyId(destinyId);
        for (int i = 0; i < list.size(); i++) {
            deletePortMapping(dcId, prjId, list.get(i).getPmId());
        }
        return false;
    }
}
