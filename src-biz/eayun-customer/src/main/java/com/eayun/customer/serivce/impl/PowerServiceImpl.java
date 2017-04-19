package com.eayun.customer.serivce.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.ConstantClazz;
import com.eayun.common.exception.AppException;
import com.eayun.common.util.BeanUtils;
import com.eayun.customer.dao.PowerDao;
import com.eayun.customer.model.BasePower;
import com.eayun.customer.model.Power;
import com.eayun.customer.serivce.PowerService;
import com.eayun.customer.serivce.RolePowerService;
/**
 * 可用于的分配功能权限
 * @Filename: PowerServiceImpl.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2015年12月11日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Service
@Transactional
@SuppressWarnings("unchecked")
public class PowerServiceImpl implements PowerService {
    private static final Logger log = LoggerFactory.getLogger(PowerServiceImpl.class);

    @Autowired
    private PowerDao            powerDao;
    
    @Autowired
    private RolePowerService    rolePowerService;

    /**查询数据库中所有可用来分配额功能权限*/
    @Override
    public List<Power> getListByPower(Power power) {
        log.info("查询功能模块列表");
        List<String> list = new ArrayList<String>();
        StringBuffer strb = new StringBuffer();
        strb.append("from BasePower where 1=1");
        strb.append(" order by powerSort ");
        List<BasePower> basePowerList = powerDao.find(strb.toString(), list.toArray());
        List<Power> powerList = new ArrayList<Power>();
        for (BasePower basePower : basePowerList) {
            Power pow = new Power();
            BeanUtils.copyPropertiesByModel(pow, basePower);
            powerList.add(pow);
        }
        return powerList;
    }

    @Override
    public Power addPower(Power power) {
        BasePower basePower = new BasePower();
        BeanUtils.copyPropertiesByModel(basePower, power);
        powerDao.saveEntity(basePower);
        return power;
    }

    @Override
    public Power updatePower(Power power) {
        BasePower basePower = new BasePower();
        BeanUtils.copyPropertiesByModel(basePower, power);
        powerDao.saveOrUpdate(basePower);
        return power;
    }

    @Override
    public boolean deletePower(String powerId) {
        log.info("删除 功能权限");
        //删除该权限的关联记录
        rolePowerService.deleteByPower(powerId);
        
        StringBuffer hql = new StringBuffer();
        hql.append("delete BasePower where powerId = ?");
        powerDao.executeUpdate(hql.toString(), powerId);
        return true;
    }

    @Override
    public Power findPowerById(String powerId) {
        BasePower basePower = powerDao.findOne(powerId);
        Power power = new Power();
        BeanUtils.copyPropertiesByModel(power, basePower);
        return power;
    }

    @Override
    public List<Power> getMenuList() {
        log.info("得到菜单列表");

        List<Power> result = new ArrayList<Power>();
        // 先得到根菜单，再得到二级菜单
        StringBuffer sb = new StringBuffer();
        sb.append(" from BasePower where parentId is null order by powerSort ");
        List<BasePower> rootList = powerDao.find(sb.toString());
        for (BasePower root : rootList) {
            result.add(getPower(root));

            sb.setLength(0);
            sb.append("from BasePower where parentId = ? order by powerSort");
            List<BasePower> childList = powerDao.find(sb.toString(), root.getParentId());
            for (BasePower child : childList) {
                result.add(getPower(child));
            }
        }

        return result;
    }

    /**
     * BasePower --> Power
     * 
     * @param basePower
     * @return
     */
    private Power getPower(BasePower basePower) {
        Power power = new Power();
        BeanUtils.copyPropertiesByModel(power, basePower);

        return power;
    }

    @Override
    public List<Power> getAuthList(String roleId) {
        log.info("得到有权限的操作列表");
        StringBuffer sb = new StringBuffer();
        sb.append(" from BasePower where powerId in (select powerId from BaseRolePower where roleId = ? ) ");
        List<BasePower> list = powerDao.find(sb.toString(),roleId);
        List<Power> result = new ArrayList<Power>();
        for (BasePower basePower : list) {
            result.add(getPower(basePower));
        }
        return result;
    }

    @Override
    public boolean hasPower(List<Power> powerList, String powerRoute) {
        log.info("验证是否有权限");
        for (Power power : powerList) {
            if (powerRoute.equals(power.getPowerRoute())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Power> getChildrenList(String powerId) {
        log.info("获取权限下级目录");
        StringBuffer sb = new StringBuffer();
        sb.append(" from BasePower where parentId = ? ");
        sb.append(" order by power_sort ");
        List<BasePower> basepowers = powerDao.find(sb.toString(),powerId);
        List<Power> powerList = new ArrayList<Power>();
        for (BasePower basePower : basepowers) {
            powerList.add(getPower(basePower));
        }
        return powerList;
    }

    @Override
    public Power findPowerByRoute(String route) {
        log.info("根据标识获取权限信息");
        List<BasePower> basepowerList = powerDao.findByRoute(route);
        Power power = new Power();
        if(basepowerList==null || basepowerList.size() == 0){
            throw new AppException("该标识权限表无数据，标识为："+route);
        }else if(basepowerList.size() == 1){
            BeanUtils.copyPropertiesByModel(power, basepowerList.get(0));
        }else{
            throw new AppException("权限表数据标识重复，标识为："+route);
        }
        return power;
    }

    /**
     * 此处业务逻辑及数据已更改
     * 2015-12-03
     * @return
     */
    @Override
    public List<Power> getDefaultList() {
        log.info("获取默认权限列表");
        List<BasePower> baseList = new ArrayList<BasePower>();
        for(int i = 0;i < ConstantClazz.DEFAULT_POWER.length; i++){
            StringBuffer hql = new StringBuffer();
            hql.append(" from BasePower where powerRoute like ? ");
            hql.append(" order by power_sort ");
            String param = ConstantClazz.DEFAULT_POWER[i]+"%";
            List<BasePower> basepowers = powerDao.find(hql.toString(),param);
            baseList.addAll(basepowers);
        }
        List<Power> powerList = new ArrayList<Power>();
        for (BasePower basePower : baseList) {
            powerList.add(getPower(basePower));
        }
        return powerList;
    }
    
    @Override
    public List<String> havePowerRoutes(String roleId) {
        log.info("查询角色所拥有的的权限标示");
        List<String> routes = new ArrayList<>();
        List<BasePower> basePowerList = new ArrayList<BasePower>();
        StringBuffer hql = new StringBuffer("select bp from BasePower bp , BaseRolePower brp where bp.powerId = brp.powerId "
                + "and brp.roleId = ? ");
        List<String> list = new ArrayList<String>();
        list.add(roleId);
        basePowerList = powerDao.find(hql.toString(), list.toArray());
        for(BasePower power : basePowerList){
            if(!(null ==power.getPowerRoute()||power.getPowerRoute().equals(""))){
                routes.add(power.getPowerRoute());
            }
        }
        return routes;
    }

    /**
     * 检验权限标示是否重复
     * @param powerRoute
     * @param powerId
     * @return
     */
    @Override
    public boolean checkByRouteName(String powerRoute, String powerId) {
        if(null == powerId || "".equals(powerId)){
            List<BasePower> basepowerList = powerDao.findByRoute(powerRoute);
            if(basepowerList.size()==0){
                return true;
            }else{
                return false;
            }
        }else{
            int count = powerDao.getByRouteAndId(powerRoute, powerId);
            if(count == 0){
                return true;
            }else{
                return false;
            }
        }
    }
}
