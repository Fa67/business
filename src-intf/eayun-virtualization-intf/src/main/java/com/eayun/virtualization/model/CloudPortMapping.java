package com.eayun.virtualization.model;

public class CloudPortMapping extends BaseCloudPortMapping {

    /**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -7868388643137214097L;
    private String subnetId;

    public String getSubnetId() {
        return subnetId;
    }
    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }
    
}
