package com.eayun.eayunstack.model;

import java.util.Set;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2017年2月24日
 */
public class VmUserData {

    private boolean ssh_pwauth;//是否启用ssh密钥
    private int disable_root;//禁用root
    private String user;//用户名
    private String password;//密码
    private Chpasswd chpasswd;
    private Set<String> ssh_authorized_keys;//绑定的SSH密钥(列表中的所有 SSH 公钥将会在虚拟机下次启动时被注入到系统中)
    private Set<String> ssh_authorized_keys_deleted;//删除的SSH密钥(列表中的所有 SSH 公钥将会在虚拟机下次启动时从系统中删除)
    
    public boolean isSsh_pwauth() {
        return ssh_pwauth;
    }
    public void setSsh_pwauth(boolean ssh_pwauth) {
        this.ssh_pwauth = ssh_pwauth;
    }
    public int getDisable_root() {
        return disable_root;
    }
    public void setDisable_root(int disable_root) {
        this.disable_root = disable_root;
    }
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public Chpasswd getChpasswd() {
        return chpasswd;
    }
    public void setChpasswd(Chpasswd chpasswd) {
        this.chpasswd = chpasswd;
    }
    public Set<String> getSsh_authorized_keys() {
        return ssh_authorized_keys;
    }
    public void setSsh_authorized_keys(Set<String> ssh_authorized_keys) {
        this.ssh_authorized_keys = ssh_authorized_keys;
    }
    public Set<String> getSsh_authorized_keys_deleted() {
        return ssh_authorized_keys_deleted;
    }
    public void setSsh_authorized_keys_deleted(Set<String> ssh_authorized_keys_deleted) {
        this.ssh_authorized_keys_deleted = ssh_authorized_keys_deleted;
    }
    
    
}
