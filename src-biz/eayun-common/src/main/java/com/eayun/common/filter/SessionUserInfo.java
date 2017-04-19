package com.eayun.common.filter;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eayun.common.util.BeanUtils;

public class SessionUserInfo implements Serializable {
    private static final Logger log              = LoggerFactory.getLogger(SessionUserInfo.class);
    /**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long   serialVersionUID = 1737797028593737241L;

    private String              userId;                                                           //用户id
    private String              userName;                                                         //用户名称
    private String              cusId;                                                            //客户id
    private String              cusName;                                                          //客户名称
    private String              cusOrg;                                                           //客户id
    private String              roleId;                                                           //角色id
    private String              roleName;                                                         //角色名称
    private String              phone;                                                            //联系电话
    private String              email;                                                            //联系邮箱
    private Boolean             isAdmin;                                                          //是否为超级管理员
    private String              error;                                                            //错误是放入错误

    private Date                lastTime;                                                         //上次登录时间
    private Date                lastVerifySmsTime;                                                //最后一次短信验证时间

    private String              cusCpname;                                                        //登录客户名称(公司中文名称)
    private String              IP;                                                               //登录IP

    public Date getLastVerifySmsTime() {
        return lastVerifySmsTime;
    }

    public void setLastVerifySmsTime(Date lastVerifySmsTime) {
        this.lastVerifySmsTime = lastVerifySmsTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCusId() {
        return cusId;
    }

    public void setCusId(String cusId) {
        this.cusId = cusId;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getRoleId() {
        return roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getLastTime() {
        return lastTime;
    }

    public void setLastTime(Date lastTime) {
        this.lastTime = lastTime;
    }

    public String getCusName() {
        return cusName;
    }

    public void setCusName(String cusName) {
        this.cusName = cusName;
    }

    public String getCusOrg() {
        return cusOrg;
    }

    public void setCusOrg(String cusOrg) {
        this.cusOrg = cusOrg;
    }

    public String getCusCpname() {
        return cusCpname;
    }

    public void setCusCpname(String cusCpname) {
        this.cusCpname = cusCpname;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String iP) {
        IP = iP;
    }

    @Override
    public String toString() {
        Field[] fields = BeanUtils.getDeclaredFields(SessionUserInfo.class);
        StringBuffer sb = new StringBuffer();
        try {
            for (Field field : fields) {
                sb.append(field.getName() + ":");
                sb.append(field.get(this));
                sb.append(";");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return sb.toString();
    }

}
