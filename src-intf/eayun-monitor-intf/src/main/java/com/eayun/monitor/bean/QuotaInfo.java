package com.eayun.monitor.bean;

/**
 * 客户项目配额信息
 * Created by ZH.F on 2016/4/14.
 */
public class QuotaInfo {
    private String customerID;
    private String projectID;
    private int totalQuota;
    private int sentQuota;

    public int getSentQuota() {
        return sentQuota;
    }

    public void setSentQuota(int sentQuota) {
        this.sentQuota = sentQuota;
    }

    public int getTotalQuota() {
        return totalQuota;
    }

    public void setTotalQuota(int totalQuota) {
        this.totalQuota = totalQuota;
    }

    public String getProjectID() {
        return projectID;
    }

    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }




}
