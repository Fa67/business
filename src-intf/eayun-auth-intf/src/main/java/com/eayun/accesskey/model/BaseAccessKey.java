package com.eayun.accesskey.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
/**
 * accessKey
 * @author Administrator
 *
 */
@Entity
@Table(name = "obs_accesskey")
public class BaseAccessKey implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "ak_id", unique = true, nullable = false, length = 32)
	private String akId; //ak表主键id
	
	@Column(name = "user_id")
	private String userId;//存储对象用户id
	
	@Column(name = "access_key")
	private String accessKey;
	
	@Column(name = "secret_key")
	private String secretKey;
	
	@Column(name = "acck_state")
	private String acckState;//accesskey是否启用，0为启用，1为停用，默认为启用0
	
	@Column(name = "create_date")
	private Date createDate;//accesskey创建时间
	
	@Column(name="is_stopservice")
	private Boolean isStopService;//标识是否欠费、冻结账户
	public Boolean getIsStopService() {
		return isStopService;
	}
	public void setIsStopService(Boolean isStopService) {
		this.isStopService = isStopService;
	}
	@Column(name = "acck_isshow")
	private String acckIsShow;		//是否显示 0为显示
	
	@Column(name="is_default")
	private String isDefault;		//是否为默认
	
	public String getIsDefault() {
		return isDefault;
	}
	public void setIsDefault(String isDefault) {
		this.isDefault = isDefault;
	}
	public String getAcckIsShow() {
		return acckIsShow;
	}
	public void setAcckIsShow(String acckIsShow) {
		this.acckIsShow = acckIsShow;
	}
	public String getAkId() {
		return akId;
	}
	public void setAkId(String akId) {
		this.akId = akId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getAccessKey() {
		return accessKey;
	}
	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}
	public String getSecretKey() {
		return secretKey;
	}
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	public String getAcckState() {
		return acckState;
	}
	public void setAcckState(String acckState) {
		this.acckState = acckState;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	
}
