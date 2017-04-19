package com.eayun.ecmcmenu.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.eayun.common.exception.AppException;
import com.eayun.common.util.BeanUtils;
import com.eayun.ecmcauthority.dao.EcmcSysAuthorityDao;
import com.eayun.ecmcmenu.dao.EcmcSysMenuDao;
import com.eayun.ecmcmenu.dao.EcmcSysRoleMenuDao;
import com.eayun.ecmcmenu.model.BaseEcmcSysMenu;
import com.eayun.ecmcmenu.model.BaseEcmcSysRoleMenu;
import com.eayun.ecmcmenu.model.EcmcSysMenuTreeGrid;
import com.eayun.ecmcmenu.service.EcmcSysMenuService;

/**
* @Author fangjun.yang
* @Date 2016年3月1日
*/
@Service
@Transactional
public class EcmcSysMenuServiceImpl implements EcmcSysMenuService {

    private static final Logger log = LoggerFactory.getLogger(EcmcSysMenuServiceImpl.class);

    @Autowired
    private EcmcSysMenuDao      ecmcSysMenuDao;

    @Autowired
    private EcmcSysRoleMenuDao  ecmcSysRoleMenuDao;

    @Autowired
    private EcmcSysAuthorityDao ecmcSysAuthorityDao;

    public void addSysMenu(BaseEcmcSysMenu ecmcSysMenu) throws Exception {
        log.info("添加菜单");
        ecmcSysMenu.setCreateTime(new Date());
        ecmcSysMenuDao.saveEntity(ecmcSysMenu);
    }

    @SuppressWarnings("unchecked")
    public List<BaseEcmcSysMenu> getAllSysMenuList() throws Exception {
        log.info("查询所有菜单");
        return IteratorUtils.toList(ecmcSysMenuDao.findAll().iterator());
    }

    public void deleteSysMenu(String menuId) throws Exception {
        log.info("删除菜单");
        if (StringUtils.isEmpty(menuId)) {
            throw new AppException("error.globe.system");
        }
        if (ecmcSysMenuDao.exists(menuId)) {
            if (this.hasSubMenu(menuId)) {
                throw new AppException("error.globe.system", new String[] { "存在子菜单，不能删除" });
            }
            if (this.existsAuths(menuId)) {
                throw new AppException("error.globe.system", new String[] { "菜单下存在权限，请先删除权限再删除菜单" });
            }
            ecmcSysMenuDao.delete(menuId);
            ecmcSysRoleMenuDao.deleteByMenuId(menuId);
        }
    }

    public boolean hasSubMenu(String parentId) throws AppException {
        return ecmcSysMenuDao.countByParentId(parentId) > 0 ? true : false;
    }

    public boolean existsAuths(String menuId) throws AppException {
        return ecmcSysAuthorityDao.countByMenuId(menuId) > 0 ? true : false;
    }

    public void updateSysMenu(BaseEcmcSysMenu ecmcSysMenu) throws Exception {
        log.info("修改菜单");
        ecmcSysMenuDao.saveOrUpdate(ecmcSysMenu);
    }

    public BaseEcmcSysMenu getSysMenuById(String menuId) throws Exception {
        log.info("查询单个菜单");
        if (StringUtils.isEmpty(menuId)) {
            throw new AppException("error.globe.system");
        }
        return ecmcSysMenuDao.findOne(menuId);
    }

    public List<BaseEcmcSysRoleMenu> findRoleMenuByRoleId(String roleId) throws Exception {
        log.info("查询角色菜单列表");
        if (StringUtils.isEmpty(roleId)) {
            throw new AppException("error.globe.system");
        }
        return ecmcSysRoleMenuDao.findByRoleId(roleId);
    }

    @Override
    public List<BaseEcmcSysMenu> findRoleMenuByRoleIds(List<String> roleIds) throws Exception {
        log.info("查询角色集合菜单");
        if (CollectionUtils.isEmpty(roleIds)) {
            throw new AppException("error.globe.system");
        }
        return ecmcSysMenuDao.findByRoleIds(roleIds);
    }

    public List<EcmcSysMenuTreeGrid> getMenuGridyByRoleIds(List<String> roleIds) throws Exception {
        log.info("查询角色集合菜单树");
        return sort(menuTreeGridMapToList(baseMenuToMenuTreeGridMap(ecmcSysMenuDao.findByRoleIds(roleIds))));
    }

    public List<EcmcSysMenuTreeGrid> getEcmcSysMenuGridList() throws AppException {
        log.info("查询系统菜单树");
        return sort(menuTreeGridMapToList(baseMenuToMenuTreeGridMap(ecmcSysMenuDao.findAllMenu())));
    }

    protected Map<String, EcmcSysMenuTreeGrid> baseMenuToMenuTreeGridMap(List<BaseEcmcSysMenu> baseList) {
        Map<String, EcmcSysMenuTreeGrid> treeGridMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(baseList)) {
            for (BaseEcmcSysMenu baseMenu : baseList) {
                EcmcSysMenuTreeGrid grid = new EcmcSysMenuTreeGrid();
                BeanUtils.copyPropertiesByModel(grid, baseMenu);
                treeGridMap.put(baseMenu.getId(), grid);
            }
        }
        return treeGridMap;
    }

    protected List<EcmcSysMenuTreeGrid> menuTreeGridMapToList(Map<String, EcmcSysMenuTreeGrid> menuMap) {
        List<EcmcSysMenuTreeGrid> rootMenus = new ArrayList<EcmcSysMenuTreeGrid>();
        if (!CollectionUtils.isEmpty(menuMap)) {
            Iterator<String> keys = menuMap.keySet().iterator();
            while (keys.hasNext()) {
                EcmcSysMenuTreeGrid menu = menuMap.get(keys.next());
                //顶级菜单
                if (StringUtils.isBlank(menu.getParentId())) {
                    rootMenus.add(menu);
                } else {
                    EcmcSysMenuTreeGrid parent = menuMap.get(menu.getParentId());
                    if (parent != null) {
                        //添加到父菜单下
                        parent.addChild(menu);
                    }
                }
            }
        }
        return rootMenus;
    }

    protected List<EcmcSysMenuTreeGrid> sort(List<EcmcSysMenuTreeGrid> menuGrids) {
        if (!CollectionUtils.isEmpty(menuGrids)) {
            Collections.sort(menuGrids, new Comparator<EcmcSysMenuTreeGrid>() {
                public int compare(EcmcSysMenuTreeGrid o1, EcmcSysMenuTreeGrid o2) {
                    return o1.getOrderNo() != null && o2.getOrderNo() != null && o1.getOrderNo() <= o2.getOrderNo() ? -1 : 1;
                }
            });
            for (EcmcSysMenuTreeGrid ecmcSysMenuGrid : menuGrids) {
                sort(ecmcSysMenuGrid.getChildren());
            }
        }
        return menuGrids;
    }

    public BaseEcmcSysMenu getMenuById(String menuId) throws AppException {
        if (StringUtils.isEmpty(menuId)) {
            throw new AppException("error.globe.system", new String[] { "找不到菜单ID" });
        }
        return ecmcSysMenuDao.findOne(menuId);
    }
}
