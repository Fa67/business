package com.eayun.obs.model;

public class ObsBucket {
    private String bucketName;
    private String creationDate;
    private String permission;   //bucket权限
    private String permissionEn;
    
    private String isOpencdn;   //是否开启CDN   未开通：0   已开通：1
    private String domainId;    //
    private String cdnStatus;   //CDN状态     未加速：0   设置中：1   已加速：2

    /*******ecmc1.1*********/
    private String bucketId;
    private String owner;

    public String getBucketId() {
        return bucketId;
    }

    public void setBucketId(String bucketId) {
        this.bucketId = bucketId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getPermissionEn() {
        return permissionEn;
    }

    public void setPermissionEn(String permissionEn) {
        this.permissionEn = permissionEn;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
    public String getIsOpencdn() {
        return isOpencdn;
    }
    public void setIsOpencdn(String isOpencdn) {
        this.isOpencdn = isOpencdn;
    }
    public String getDomainId() {
        return domainId;
    }
    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }
    public String getCdnStatus() {
        return cdnStatus;
    }
    public void setCdnStatus(String cdnStatus) {
        this.cdnStatus = cdnStatus;
    }

}
