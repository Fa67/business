package com.eayun.bean;

import com.eayun.common.tools.ExcelTitle;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2017年3月16日
 */
public class NewAccessExcel {

    @ExcelTitle(name="ICP密码")
    private String password;

    @ExcelTitle(name="网站备案号")
    private String webRecordNo;
    
    @ExcelTitle(name="备案状态")
    private String status;
    
    @ExcelTitle(name="所属用户")
    private String RecordCusName;
    
    @ExcelTitle(name="网站分布地点")
    private String webAddress;

    @ExcelTitle(name="网站接入方式")
    private String accessType;
    
    @ExcelTitle(name="IP地址段起始-IP地址段截至")
    private String IPPeriod;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRecordCusName() {
        return RecordCusName;
    }

    public void setRecordCusName(String recordCusName) {
        RecordCusName = recordCusName;
    }

    public String getWebRecordNo() {
        return webRecordNo;
    }

    public void setWebRecordNo(String webRecordNo) {
        this.webRecordNo = webRecordNo;
    }

    public String getWebAddress() {
        return webAddress;
    }

    public void setWebAddress(String webAddress) {
        this.webAddress = webAddress;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public String getIPPeriod() {
        return IPPeriod;
    }

    public void setIPPeriod(String iPPeriod) {
        IPPeriod = iPPeriod;
    }
    
}
