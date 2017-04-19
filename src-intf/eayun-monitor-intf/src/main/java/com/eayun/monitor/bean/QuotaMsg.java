package com.eayun.monitor.bean;

/**
 * 配额队列信息。
 *                       
 * @Filename: QuotaMsg.java
 * @Description: 
 * @Version: 1.0
 * @Author: Fan Zhang
 * @Email: fan.zhang@eayun.com
 * @History:<br>
 *<li>Date: 2016年4月12日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class QuotaMsg {

    private String biz;//业务类型
    private String customerId;//客户ID
    private String projectId;//项目ID
    public String getBiz() {
        return biz;
    }
    public void setBiz(String biz) {
        this.biz = biz;
    }
    public String getCustomerId() {
        return customerId;
    }
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    public String getProjectId() {
        return projectId;
    }
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

}
