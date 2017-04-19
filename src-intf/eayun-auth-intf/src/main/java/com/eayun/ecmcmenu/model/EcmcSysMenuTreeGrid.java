/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.ecmcmenu.model;

import java.util.ArrayList;
import java.util.List;

/**
 *                       
 * @Filename: EcmcSysMenuGrid.java
 * @Description: 
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2016年3月30日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class EcmcSysMenuTreeGrid extends BaseEcmcSysMenu{

    private static final long serialVersionUID = -9012866074125271677L;
    
    private List<EcmcSysMenuTreeGrid> children = new ArrayList<EcmcSysMenuTreeGrid>();

    public List<EcmcSysMenuTreeGrid> getChildren() {
        return children;
    }

    public void setChildren(List<EcmcSysMenuTreeGrid> children) {
        this.children = children;
    }
    
    public void addChild(EcmcSysMenuTreeGrid child){
        this.children.add(child);
    }
    
}
