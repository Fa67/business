package com.eayun.ecmcuser.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;

/**
 * @author zengbo 用户表映射实体类
 *
 */
@Entity
@Table(name = "ecmc_sys_user")
public class BaseEcmcSysUser implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "id", unique = true, nullable = false, length = 32)
	private String id;																				//ID
	//updatable=false，账号不允许修改
	@Column(name = "account", length = 32, updatable=false)
	private String account;																	//登录帐号

	@Column(name = "name", length = 50)
	private String name;																		//名称、姓名

	@Column(name = "password", length = 50)
	private String password;																//密码

	@Column(name = "tel", length = 100)
	private String tel;																			//联系电话

	@Column(name = "mail", length = 50)
	private String mail;																			//邮箱

	@Column(name = "depart_id", length = 32)
	private String departId;																//部门id

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "createtime", nullable = false, length = 19, updatable=false)
	private Date createTime;																//创建时间

	@Column(name = "createdby", length = 32, updatable=false)
	private String createdBy;																//创建者ID

	@Column(name = "enableflag", length = 1)
	private boolean enableFlag;															//是否启用

	@Column(name = "salt", length = 20)
	private String salt;																			//密码干扰串

	@Column(name = "sex", length = 1)
	private Character sex;																		//性别

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getDepartId() {
		return departId;
	}

	public void setDepartId(String departId) {
		this.departId = departId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public boolean isEnableFlag() {
		return enableFlag;
	}

	public void setEnableFlag(boolean enableFlag) {
		this.enableFlag = enableFlag;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public Character getSex() {
		return sex;
	}

	public void setSex(Character sex) {
		this.sex = sex;
	}
}
