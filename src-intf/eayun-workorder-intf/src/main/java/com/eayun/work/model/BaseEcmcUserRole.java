package com.eayun.work.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
/**
 * 
 *                       
 * @Filename: BaseEcmcUserRole.java
 * @Description: 
 * @Version: 1.0
 * @Author: 陈鹏飞
 * @Email: pengfei.chen@eayun.com
 * @History:<br>
 *<li>Date: 2015年10月30日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Entity
@Table(name = "userrole")
public class BaseEcmcUserRole implements Serializable {

    private static final long serialVersionUID = 8802988578853503091L;
    
    @Id
    @Column(name = "id", length = 32)
    private String userRoleId;
    @Column(name = "operid", length = 32)
    private String operId;
    @Column(name = "roleid", length = 32)
    private String roleId;
    @Column(name = "username", length = 32)
    private String userName;
    @Column(name = "enableflag", length = 32)
    private String enableFlag;
    public String getUserRoleId() {
        return userRoleId;
    }
    public void setUserRoleId(String userRoleId) {
        this.userRoleId = userRoleId;
    }
    public String getOperId() {
        return operId;
    }
    public void setOperId(String operId) {
        this.operId = operId;
    }
    public String getRoleId() {
        return roleId;
    }
    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getEnableFlag() {
        return enableFlag;
    }
    public void setEnableFlag(String enableFlag) {
        this.enableFlag = enableFlag;
    }
    
}
