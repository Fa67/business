package com.eayun.price.bean;

import java.math.BigDecimal;
import java.util.Date;

public class PriceRedis {
	
	private String id ;		//记录id
	
	private BigDecimal price ;	//价格
	
	private Long start;		//区间开始
	
	private Long end;		//区间截止
	
	private Date createTime;//创建时间

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Long getStart() {
		return start;
	}

	public void setStart(Long start) {
		this.start = start;
	}

	public Long getEnd() {
		return end;
	}

	public void setEnd(Long end) {
		this.end = end;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	
}
