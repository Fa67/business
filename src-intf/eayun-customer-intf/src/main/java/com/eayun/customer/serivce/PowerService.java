package com.eayun.customer.serivce;

import java.util.List;

import com.eayun.customer.model.Power;

public interface PowerService {
    
    /**
     * 是否有权限
     * 
     * @param roleId
     * @param powerRoute
     * @return
     */
    public boolean hasPower(List<Power> powerList, String powerRoute);
    
    /**
     * 得到指定角色有权限的菜单和操作
     * 
     * @param roleId
     * @return
     */
    public List<Power> getAuthList(String roleId);

    /**
     * 得到菜单列表
     * 
     * @return
     */
    public List<Power> getMenuList();

    /**
     * 查询权限模块
     * @param power
     * @return
     */
    public List<Power> getListByPower(Power power);

    /**
     * 新增权限模块
     * @param power
     * @return
     */
    public Power addPower(Power power);

    /**
     * 修改权限模块
     * @param power
     * @return
     */
    public Power updatePower(Power power);

    /**
     * 删除权限模块
     * @param power
     * @return
     */
    public boolean deletePower(String powerId);

    /**
     * 根据PowerId查询指定权限模块
     * @param powerId
     * @return
     */
    public Power findPowerById(String powerId);
    /**
     * 获得权限下级目录
     * @param powerId
     * @return
     */
    public List<Power> getChildrenList(String powerId);
    /**
     * 根据标识获取权限的信息
     * @param route
     * @return
     */
    public Power findPowerByRoute(String route);
    /**
     * 获取默认权限列表
     * @return
     */
    public List<Power> getDefaultList();
    /**
     * 返回角色所拥有的的权限标示
     * @param roleId
     * @return
     */
    public List<String> havePowerRoutes(String roleId);

    /**
     * 检验权限标示，不可重复
     * @param powerRoute
     * @param powerId
     * @return
     */
    public boolean checkByRouteName(String powerRoute, String powerId);
}
