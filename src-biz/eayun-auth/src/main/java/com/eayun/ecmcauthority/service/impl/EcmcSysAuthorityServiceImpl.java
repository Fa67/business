package com.eayun.ecmcauthority.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.eayun.common.exception.AppException;
import com.eayun.common.util.BeanUtils;
import com.eayun.ecmcauthority.dao.EcmcSysAuthorityDao;
import com.eayun.ecmcauthority.dao.EcmcSysRoleAuthDao;
import com.eayun.ecmcauthority.model.BaseEcmcSysAuthority;
import com.eayun.ecmcauthority.model.EcmcSysAuthority;
import com.eayun.ecmcauthority.service.EcmcSysAuthorityService;

/**
* @Author fangjun.yang
* @Date 2016年3月1日
*/
@Service
@Transactional
public class EcmcSysAuthorityServiceImpl implements EcmcSysAuthorityService {

    private static final Logger log = LoggerFactory.getLogger(EcmcSysAuthorityServiceImpl.class);

    @Autowired
    private EcmcSysAuthorityDao ecmcSysAuthorityDao;

    @Autowired
    private EcmcSysRoleAuthDao  ecmcSysRoleAuthDao;

    public void addSysAuthority(BaseEcmcSysAuthority auth) throws AppException {
        log.info("添加权限");
        auth.setCreateTime(new Date());
        ecmcSysAuthorityDao.saveEntity(auth);
    }

    public List<BaseEcmcSysAuthority> getSysAutorityListByRoleId(String roleId) throws Exception {
        log.info("查询角色权限");
        if (StringUtils.isEmpty(roleId)) {
            throw new AppException("error.globe.system");
        }
        return ecmcSysAuthorityDao.findByRoleId(roleId);
    }

    public List<BaseEcmcSysAuthority> getSysAuthorityListByRoleIds(List<String> roleIds) throws Exception {
        log.info("查询角色集合权限");
        if (CollectionUtils.isEmpty(roleIds)) {
            throw new AppException("error.globe.system");
        }
        return ecmcSysAuthorityDao.findByRoleIds(roleIds);
    }

    public void deleteSysAuthority(String authId) throws AppException {
        log.info("删除权限");
        if (StringUtils.isEmpty(authId)) {
            throw new AppException("error.globe.system");
        }
        ecmcSysAuthorityDao.delete(authId);
        ecmcSysRoleAuthDao.deleteSysRoleAuthByAuthId(authId);
    }

    public void updateSysAuthority(BaseEcmcSysAuthority auth) throws AppException {
        log.info("修改权限");
        ecmcSysAuthorityDao.saveOrUpdate(auth);
    }

    public BaseEcmcSysAuthority findSysAuthorityById(String authId) throws Exception {
        log.info("查询单个权限");
        if (StringUtils.isEmpty(authId)) {
            throw new AppException("error.globe.system");
        }
        return ecmcSysAuthorityDao.findOne(authId);
    }

    public List<EcmcSysAuthority> getSysAuthorityList(String menuId) throws AppException {
        log.info("查询权限列表");
        List<Map<String, Object>> maps = ecmcSysAuthorityDao.findByMenuId(menuId);
        if (maps != null) {
            List<EcmcSysAuthority> resultList = new ArrayList<EcmcSysAuthority>(maps.size());
            for (Map<String, Object> map : maps) {
                EcmcSysAuthority auth = new EcmcSysAuthority();
                BeanUtils.mapToBean(auth, map);
                resultList.add(auth);
            }
            return resultList;
        }
        return new ArrayList<EcmcSysAuthority>(0);
    }

    public List<EcmcSysAuthority> getAllEnableAuthList() throws AppException {
        return baseListToSubs(ecmcSysAuthorityDao.findAllEnableAuthority());
    }
    
    /**
     * base集合转子类
     * @param baseList
     * @return
     * @throws AppException
     */
    protected List<EcmcSysAuthority> baseListToSubs(List<BaseEcmcSysAuthority> baseList) throws AppException {
        if(CollectionUtils.isEmpty(baseList)){
            return Collections.<EcmcSysAuthority>emptyList();
        }
        List<EcmcSysAuthority> resultList = new ArrayList<EcmcSysAuthority>(baseList.size());
        for (int i = 0; i < baseList.size(); i++) {
            resultList.add(new EcmcSysAuthority(baseList.get(i)));
        }
        return resultList;
    }
}
