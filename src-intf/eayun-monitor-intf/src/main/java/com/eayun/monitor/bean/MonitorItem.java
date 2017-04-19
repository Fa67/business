package com.eayun.monitor.bean;

/**
 * 监控项
 *                       
 * @Filename: MonitorItem.java
 * @Description: 
 * @Version: 1.0
 * @Author: fan.zhang
 * @Email: fan.zhang@eayun.com
 * @History:<br>
 *<li>Date: 2015年12月14日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class MonitorItem {
    private String name;
    private String nameEN;
    private String nodeId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameEN() {
        return nameEN;
    }

    public void setNameEN(String nameEN) {
        this.nameEN = nameEN;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
}
