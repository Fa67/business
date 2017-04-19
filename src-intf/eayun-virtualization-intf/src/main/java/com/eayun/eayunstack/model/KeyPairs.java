package com.eayun.eayunstack.model;
/**
 * @author jingang.liu@eayun.com
 * @Dadte 2017年2月24日
 */
public class KeyPairs {

    private String id;//底层ID
    private String name;//密钥名称
    private String desc;//密钥描述
    private String private_key;//私钥
    private String public_key;//公钥
    private String fingerprint;//指纹信息
    private String user_id;//租户ID对应项目ID
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDesc() {
        return desc;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }
    public String getPrivate_key() {
        return private_key;
    }
    public void setPrivate_key(String private_key) {
        this.private_key = private_key;
    }
    public String getPublic_key() {
        return public_key;
    }
    public void setPublic_key(String public_key) {
        this.public_key = public_key;
    }
    public String getFingerprint() {
        return fingerprint;
    }
    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }
    public String getUser_id() {
        return user_id;
    }
    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
    
}
