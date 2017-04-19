package com.eayun.customer.serivce.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.util.BeanUtils;
import com.eayun.customer.dao.RolePowerDao;
import com.eayun.customer.model.BaseRolePower;
import com.eayun.customer.model.RolePower;
import com.eayun.customer.serivce.RolePowerService;

@Service
@Transactional
public class RolePowerServiceImpl implements RolePowerService {
    private static final Logger log = LoggerFactory.getLogger(RolePowerServiceImpl.class);
    
    @Autowired
    private RolePowerDao rolePowerDao;

    @Override
    public List<RolePower> getListByRole(String roleId) {
        log.info("得到指定角色下权限列表");
        
        List<BaseRolePower> BaseRpList = rolePowerDao.getListByRoleId(roleId);
        List<RolePower> rpList = new ArrayList<RolePower>();
        for (BaseRolePower baseRp : BaseRpList) {
            RolePower rp = new RolePower();
            BeanUtils.copyPropertiesByModel(rp, baseRp);
            rpList.add(rp);
        }
        return rpList;
    }

    /**
     * 设置角色权限：全删全增
     * @param roleId
     * @param powerIds
     */
    @Override
    public void setRolePower(String roleId, List<String> powerIds) {
        log.info("设置角色权限");
        
        StringBuffer sb = new StringBuffer();
        sb.append("delete BaseRolePower where roleId = ?");
        rolePowerDao.executeUpdate(sb.toString(), roleId);
        for (String powerId : powerIds) {
            BaseRolePower rolePower = new BaseRolePower();
            rolePower.setPowerId(powerId);
            rolePower.setRoleId(roleId);
            rolePower.setRpState('1');

            rolePowerDao.saveEntity(rolePower);
        }
    }
    
    @Override
    public void deleteByRole(String roleId) {
        log.info("删除一个角色的所有关联权限记录");
        StringBuffer hql = new StringBuffer();
        hql.append("delete BaseRolePower where roleId = ?");
        rolePowerDao.executeUpdate(hql.toString(), roleId);
    }
    @Override
    public void deleteByPower(String powerId) {
        log.info("删除一项功能权限的所有关联记录");
        StringBuffer hql = new StringBuffer();
        hql.append("delete BaseRolePower where powerId = ?");
        rolePowerDao.executeUpdate(hql.toString(), powerId);
    }

}
