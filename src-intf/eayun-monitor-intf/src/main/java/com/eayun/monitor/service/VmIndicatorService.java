package com.eayun.monitor.service;

import java.util.Date;
import java.util.List;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.monitor.bean.VmIndicator;

public interface VmIndicatorService {

    public Page getvmList(Page page , QueryMap queryMap , String projectId , String vmName);
    
    public VmIndicator getvmById(String vmId);
    
    public List<VmIndicator> getDataById(Date endTime , int count, String vmId , String type, String cusId,String instanceId);
}
