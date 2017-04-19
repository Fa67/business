package com.eayun.obs.model;

public class ObsAccessBean {
    
    private String bucketName;
    private String host;
    private String PutHeaderHost;
    private String url;
    private String hmacSHA1;
    private String accessKey;
    private String RFC2822Date;
    private boolean isHttp = true;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHmacSHA1() {
        return hmacSHA1;
    }

    public void setHmacSHA1(String hmacSHA1) {
        this.hmacSHA1 = hmacSHA1;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getRFC2822Date() {
        return RFC2822Date;
    }

    public void setRFC2822Date(String rFC2822Date) {
        RFC2822Date = rFC2822Date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getPutHeaderHost() {
        return PutHeaderHost;
    }

    public void setPutHeaderHost(String putHeaderHost) {
        PutHeaderHost = putHeaderHost;
    }

    public boolean isHttp() {
        return isHttp;
    }

    public void setHttp(boolean isHttp) {
        this.isHttp = isHttp;
    }

}
