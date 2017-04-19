package com.eayun.log.bean;

import com.eayun.common.tools.ExcelTitle;

public class ExcelLog {

    @ExcelTitle(name="操作项")
    private String actItem;
    
    @ExcelTitle(name="数据中心")
    private String dcName;
    
    @ExcelTitle(name="资源类型")
    private String resourceType;
    
    @ExcelTitle(name="资源名称")
    private String resourceName;
    
    @ExcelTitle(name="操作者")
    private String  actPerson;

    @ExcelTitle(name="操作时间")
    private String  actTime;
    
    @ExcelTitle(name="执行状态")
    private String statu;

    public String getActItem() {
        return actItem;
    }

    public void setActItem(String actItem) {
        this.actItem = actItem;
    }

    public String getDcName() {
        return dcName;
    }

    public void setDcName(String dcName) {
    	this.dcName = dcName;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getActPerson() {
        return actPerson;
    }

    public void setActPerson(String actPerson) {
        this.actPerson = actPerson;
    }

    public String getActTime() {
        return actTime;
    }

    public void setActTime(String actTime) {
        this.actTime = actTime;
    }

    public String getStatu() {
        return statu;
    }

    public void setStatu(String statu) {
        this.statu = statu;
    }

    public ExcelLog() {
        super();
    }

    public ExcelLog(String actItem, String dcName, String resourceType, String resourceName,
                    String actPerson, String actTime, String statu) {
        super();
        this.actItem = actItem;
        this.dcName = dcName;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.actPerson = actPerson;
        this.actTime = actTime;
        this.statu = statu;
    }
    
    
}
