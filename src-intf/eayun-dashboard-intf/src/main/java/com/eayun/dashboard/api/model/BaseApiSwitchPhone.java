package com.eayun.dashboard.api.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
/**
 * API开关最高权限手机号码
 *                       
 * @Filename: BaseApiSwitch.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2016年11月28日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Entity
@Table(name = "api_switch_phone")
public class BaseApiSwitchPhone implements Serializable {

	/**
	 *Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 5018610180025877831L;

	@Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "id", unique = true, nullable = false, length = 36)
    private String            id;

    @Column(name = "phone", length = 36)
    private String            phone;		//绑定手机号码
    
    @Column(name = "name", length = 100)   //联系人
    private String name;
    
    @Column(name = "type", length = 1)     //类型
    private String type;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	

}
