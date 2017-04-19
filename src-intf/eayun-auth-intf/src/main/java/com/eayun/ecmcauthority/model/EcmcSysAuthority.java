/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.ecmcauthority.model;

import com.eayun.common.util.BeanUtils;

/**
 *                       
 * @Filename: EcmcSysAuthority.java
 * @Description: 
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2016年3月29日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class EcmcSysAuthority extends BaseEcmcSysAuthority{
    
    private static final long serialVersionUID = 9157329121833468225L;
    
    private String menuName;
    
    public EcmcSysAuthority(){super();}
    
    public EcmcSysAuthority(BaseEcmcSysAuthority base){
        super();
        BeanUtils.copyPropertiesByModel(this, base);
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }
    
}
