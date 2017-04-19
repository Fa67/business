package com.eayun.obs.ecmcmodel;

import java.math.BigDecimal;

/**
 * EcmcObsTopModel
 * 
 * @Filename: EcmcObsTopModel.java
 * @Description:对象存储资源排行类
 * @Version: 1.1
 * @Author: liyanchao
 * @Email: yanchao.li@eayun.com
 * @History:<br> <li>Date: 2016年3月28日</li> 
 * <li>Version: 1.1</li>
 */
public class EcmcObsTopModel {
	private String bucketName;
	private BigDecimal size;
	private String belongUser;//所属客户
	private String belongUserId;//所属客户id
	private int lastTop;
	private long count;
	private int rank;//排名
	private String sizeStr; // 占用存储空间大小(MB)";"使用流量大小(MB)";"请求次数(万次)

	
	
	
	public BigDecimal getSize() {
		return size;
	}

	public void setSize(BigDecimal size) {
		this.size = size;
	}

	public String getBelongUserId() {
		return belongUserId;
	}

	public void setBelongUserId(String belongUserId) {
		this.belongUserId = belongUserId;
	}

	public String getSizeStr() {
		return sizeStr;
	}

	public void setSizeStr(String sizeStr) {
		this.sizeStr = sizeStr;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

	
	public String getBelongUser() {
		return belongUser;
	}

	public void setBelongUser(String belongUser) {
		this.belongUser = belongUser;
	}

	public int getLastTop() {
		return lastTop;
	}

	public void setLastTop(int lastTop) {
		this.lastTop = lastTop;
	}

}
