package com.eayun.physical.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.util.BeanUtils;
import com.eayun.physical.dao.ServerDao;
import com.eayun.physical.model.BaseDcServer;
import com.eayun.physical.model.DcServer;
import com.eayun.physical.service.ServerService;

@Service
@Transactional
public class ServerServiceImpl implements ServerService {

    @Autowired
    private ServerDao serverDao;

    @Override
    public List<DcServer> getServerListByDataCenter(String datacenterId) {
        List<BaseDcServer> list = serverDao.getListByDataCenter(datacenterId);
        List<DcServer> result = new ArrayList<DcServer>();
        for(BaseDcServer baseDcServer :list){
            DcServer dcServer = new DcServer();
            BeanUtils.copyPropertiesByModel(dcServer, baseDcServer);
            result.add(dcServer);
            
        }
        return result;
    }

}
