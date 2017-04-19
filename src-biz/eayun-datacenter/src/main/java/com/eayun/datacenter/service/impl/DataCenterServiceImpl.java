package com.eayun.datacenter.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.util.BeanUtils;
import com.eayun.datacenter.dao.DataCenterDao;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.datacenter.model.DcDataCenter;
import com.eayun.datacenter.service.DataCenterService;

/**
 * DataCenterServiceImpl
 *                       
 * @Filename: DataCenterServiceImpl.java
 * @Description: 
 * @Version: 1.0
 * @Author: chenhao
 * @Email: hao.chen@eayun.com
 * @History:<br>
 *<li>Date: 2015年9月1日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Service
@Transactional
public class DataCenterServiceImpl implements DataCenterService {

    @Autowired
    private DataCenterDao dataCenterDao;

    @Override
    public List<DcDataCenter> getAllList() {
        List<BaseDcDataCenter> list = dataCenterDao.getAllList();
        List<DcDataCenter> result = new ArrayList<DcDataCenter>();
        for (BaseDcDataCenter baseDcDataCenter : list) {
            DcDataCenter dcDataCenter = new DcDataCenter();
            BeanUtils.copyPropertiesByModel(dcDataCenter, baseDcDataCenter);
            result.add(dcDataCenter);
        }
        return result;
    }

	@Override
	public BaseDcDataCenter getById(String dcId) {
		return dataCenterDao.findOne(dcId);
	}
	
	@SuppressWarnings("unchecked")
	public List<BaseDcDataCenter> getDcList(String sql,Object[] values){
		return dataCenterDao.find(sql, values);
	}
}
