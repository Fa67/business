package com.eayun.work.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 
 * @author 陈鹏飞
 *
 */
@Entity
@Table(name = "order_number")
public class BaseOrderNum implements Serializable{
	private static final long serialVersionUID = -1836564218008259634L;
	
	@Id
	@Column(name = "prefix", length = 32)
	private String prefix;
	
	@Id
	@Column(name = "s_date", length = 32)
	private String dateFrommat;
	
	@Column(name = "max_seq")
	private Integer maxSeq=1;
	
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public String getDateFrommat() {
		return dateFrommat;
	}
	public void setDateFrommat(String dateFrommat) {
		this.dateFrommat = dateFrommat;
	}
	public Integer getMaxSeq() {
		return maxSeq;
	}
	public void setMaxSeq(Integer maxSeq) {
		this.maxSeq = maxSeq;
	}
	
}
