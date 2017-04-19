package com.eayun.customer.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
/**
 * 客户开通的服务
 * @author xiangyu.cao@eayun.com
 *
 */
@Entity
@Table(name = "cus_service_state")
public class BaseCusServiceState implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "cs_id", unique = true, nullable = false, length = 32)
	private String csId;
	
	@Column(name = "cus_id")
	private String cusId;
	
	@Column(name = "obs_state")
	private String obsState;
	
	public String getCsId() {
		return csId;
	}
	public void setCsId(String csId) {
		this.csId = csId;
	}
	public String getCusId() {
		return cusId;
	}
	public void setCusId(String cusId) {
		this.cusId = cusId;
	}
	public String getObsState() {
		return obsState;
	}
	public void setObsState(String obsState) {
		this.obsState = obsState;
	}
	
	
}
