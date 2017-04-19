package com.eayun.virtualization.model;
/**
 * @author jingang.liu@eayun.com
 * @Dadte 2017年2月28日
 */
public class CloudSecretKey extends BaseCloudSecretKey {

    private String privateKeyFileId;//下载私钥的文件ID
    private String countnum;//密钥关联多少个云主机

    public String getPrivateKeyFileId() {
        return privateKeyFileId;
    }

    public void setPrivateKeyFileId(String privateKeyFileId) {
        this.privateKeyFileId = privateKeyFileId;
    }

    public String getCountnum() {
        return countnum;
    }

    public void setCountnum(String countnum) {
        this.countnum = countnum;
    }
    
    
}
