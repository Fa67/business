package com.eayun.monitor.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

public class ApiMonitorDataDetail extends ApiMonitorData implements Serializable {

    private String cusName ;
    private String regionName ;

    public void setCusName(String cusName) {
        this.cusName = cusName;
    }

    public String getCusName() {
        return cusName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getRegionName() {
        return regionName;
    }
}