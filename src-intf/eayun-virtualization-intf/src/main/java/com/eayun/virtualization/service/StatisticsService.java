package com.eayun.virtualization.service;

import java.io.OutputStream;
import java.util.Date;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.virtualization.bean.CloudTypes;

public interface StatisticsService {
    /**
     * 获取云主机资源列表
     * @param projectId
     * @param cusId 
     * @param startTime
     * @param endTime
     * @return
     */
    public Page getCloudVmResources(Page page ,String projectId , String cusId, Date startTime , Date endTime ,String sort ,String orderBy, QueryMap queryMap);

    /**
     * 获取云硬盘资源列表
     * @param projectId
     * @param cusId 
     * @param startTime
     * @param endTime
     * @return
     */
    public  Page getCloudVolumeResources(Page page,String projectId, String cusId, Date startTime, Date endTime ,String sort ,String orderBy, QueryMap queryMap);
    /**
     * 获取网络流量统计资源列表
     * @param projectId
     * @param startTime
     * @param endTime
     * @return
     */
    public  CloudTypes getNet(String projectId, Date startTime, Date endTime);
    /**
     * 导出excel数据
     * @param projectId
     * @param startTime
     * @param endTime
     * @return
     */
    public boolean getResourcesForExcel(String dcId,String projectId, Date startTime, Date endTime);
    
    public void exportSheets(OutputStream os,String dcId ,String cusId,String projectId, Date startTime,Date endTime , 
    		String sort ,String orderBy,String sortVol ,String orderByVol) throws Exception;
    
    public String getProNameById(String projectId);

	String getDcNameById(String dcId);
}
