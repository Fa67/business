package com.eayun.dashboard.ecmcservice.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eayun.dashboard.ecmcservice.DatacenterSyncInitService;
import com.eayun.schedule.service.DatacenterSyncService;

/**
 *                       
 * @Filename: DubboInitServiceImpl.java
 * @Description: 
 * @Version: 1.0
 * @Author: chenhao
 * @Email: hao.chen@eayun.com
 * @History:<br>
 *<li>Date: 2016年9月9日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Service
public class DatacenterSyncInitServiceImpl implements DatacenterSyncInitService {
    
    @Autowired
    private DatacenterSyncService datacenterSyncService;

}
