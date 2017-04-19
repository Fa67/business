package com.eayun.costcenter.model;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "money_account")
public class BaseMoneyAccount implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name="mon_id", length=32)
    private String monId;					//主键id
	@Column(name="mon_money")
	private BigDecimal money;				//账户余额
	@Column(name="mon_cusid")
	private String cusId;					//客户id
	public String getMonId() {
		return monId;
	}
	public void setMonId(String monId) {
		this.monId = monId;
	}
	
	public BigDecimal getMoney() {
		return money;
	}
	public void setMoney(BigDecimal money) {
		this.money = money;
	}
	public String getCusId() {
		return cusId;
	}
	public void setCusId(String cusId) {
		this.cusId = cusId;
	}
	
}
