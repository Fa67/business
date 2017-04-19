package com.eayun.virtualization.apiservice;

import java.util.List;

import com.eayun.virtualization.model.BaseCloudPortMapping;

public interface PortMappingApiService {
    
    /**
     * 获取绑定对象id的端口映射列表
     * @author gaoxiang
     * @param destinyId
     * @return
     */
    public List<BaseCloudPortMapping> queryPortMappingListByDestinyId(String destinyId);
    
    /**
     * 删除绑定对象id的端口映射列表
     * @author gaoxiang
     * @param dcId
     * @param prjId
     * @param destinyId
     * @return
     */
    public boolean deletePortMappingListByDestinyId(String dcId, String prjId, String destinyId);
    
    /**
     * 删除对应id的端口映射
     * @author gaoxiang
     * @param dcId
     * @param prjId
     * @param portMappingId
     * @return
     */
    public boolean deletePortMapping(String dcId, String prjId, String portMappingId);
}