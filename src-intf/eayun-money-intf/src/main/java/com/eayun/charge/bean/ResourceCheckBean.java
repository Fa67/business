package com.eayun.charge.bean;

import java.io.Serializable;

/**
 * 用于判断资源是否存在的实体类
 * Created by ZH.F on 2016/10/14.
 */
public class ResourceCheckBean implements Serializable{

    /**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1755477999216654875L;
    private String resourceName;
    private boolean isExisted;

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public boolean isExisted() {
        return isExisted;
    }

    public void setExisted(boolean existed) {
        isExisted = existed;
    }
}
