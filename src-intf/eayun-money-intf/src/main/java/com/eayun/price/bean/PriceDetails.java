package com.eayun.price.bean;

import java.math.BigDecimal;

/**
 * 一次业务价格计算返回的bean，既包含最终价格，也包含每一种计费单位的价格
 *                       
 * @Filename: PriceDetails.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2016年7月25日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class PriceDetails {

	private Integer number;		//批量个数
	
	private Integer cycleCount;	//预付费月数，或后付费时小时数
	
	private BigDecimal cpuPrice;	//CPU价格
	
	private BigDecimal ramPrice;	//内存价格
	
	private BigDecimal dataDiskPrice;	//数据盘价格
	
	private BigDecimal sysDiskPrice;	//系统盘价格
	
	private BigDecimal snapshotPrice;	//备份价格
	
	private BigDecimal ImagePrice;	//收费镜像价格
	
	private BigDecimal cusImagePrice;	//自定义镜像价格
	
	private BigDecimal bandWidthPrice;	//私有网络价格
	
	private BigDecimal poolPrice;	//负载均衡价格
	
	private BigDecimal vpnPrice;	//vpn价格
	
	private BigDecimal ipPrice;		//Ip价格
	
	private BigDecimal spacePrice;		//存储空间价格
	
	private BigDecimal downPrice;		//下行流量价格
	
	private BigDecimal requestPrice;	//请求次数价格

    private BigDecimal cdnDownloadPrice;    //CDN下载流量费用

    private BigDecimal cdnDreqsPrice;       //CDN动态请求数费用

    private BigDecimal cdnHreqsPrice;       //CDN HTTPS请求数费用
	
	private BigDecimal totalPrice;		//此次业务总价格

    public BigDecimal getCdnDownloadPrice() {
        return cdnDownloadPrice;
    }

    public void setCdnDownloadPrice(BigDecimal cdnDownloadPrice) {
        this.cdnDownloadPrice = cdnDownloadPrice;
    }

    public BigDecimal getCdnDreqsPrice() {
        return cdnDreqsPrice;
    }

    public void setCdnDreqsPrice(BigDecimal cdnDreqsPrice) {
        this.cdnDreqsPrice = cdnDreqsPrice;
    }

    public BigDecimal getCdnHreqsPrice() {
        return cdnHreqsPrice;
    }

    public void setCdnHreqsPrice(BigDecimal cdnHreqsPrice) {
        this.cdnHreqsPrice = cdnHreqsPrice;
    }

    public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public Integer getCycleCount() {
		return cycleCount;
	}

	public void setCycleCount(Integer cycleCount) {
		this.cycleCount = cycleCount;
	}

	public BigDecimal getCpuPrice() {
		return cpuPrice;
	}

	public void setCpuPrice(BigDecimal cpuPrice) {
		this.cpuPrice = cpuPrice;
	}

	public BigDecimal getRamPrice() {
		return ramPrice;
	}

	public void setRamPrice(BigDecimal ramPrice) {
		this.ramPrice = ramPrice;
	}

	public BigDecimal getDataDiskPrice() {
		return dataDiskPrice;
	}

	public void setDataDiskPrice(BigDecimal dataDiskPrice) {
		this.dataDiskPrice = dataDiskPrice;
	}

	public BigDecimal getSysDiskPrice() {
		return sysDiskPrice;
	}

	public void setSysDiskPrice(BigDecimal sysDiskPrice) {
		this.sysDiskPrice = sysDiskPrice;
	}

	public BigDecimal getSnapshotPrice() {
		return snapshotPrice;
	}

	public void setSnapshotPrice(BigDecimal snapshotPrice) {
		this.snapshotPrice = snapshotPrice;
	}

	public BigDecimal getImagePrice() {
		return ImagePrice;
	}

	public void setImagePrice(BigDecimal imagePrice) {
		ImagePrice = imagePrice;
	}

	public BigDecimal getCusImagePrice() {
		return cusImagePrice;
	}

	public void setCusImagePrice(BigDecimal cusImagePrice) {
		this.cusImagePrice = cusImagePrice;
	}

	public BigDecimal getBandWidthPrice() {
		return bandWidthPrice;
	}

	public void setBandWidthPrice(BigDecimal bandWidthPrice) {
		this.bandWidthPrice = bandWidthPrice;
	}

	public BigDecimal getPoolPrice() {
		return poolPrice;
	}

	public void setPoolPrice(BigDecimal poolPrice) {
		this.poolPrice = poolPrice;
	}

	public BigDecimal getVpnPrice() {
		return vpnPrice;
	}

	public void setVpnPrice(BigDecimal vpnPrice) {
		this.vpnPrice = vpnPrice;
	}

	public BigDecimal getIpPrice() {
		return ipPrice;
	}

	public void setIpPrice(BigDecimal ipPrice) {
		this.ipPrice = ipPrice;
	}

	public BigDecimal getSpacePrice() {
		return spacePrice;
	}

	public void setSpacePrice(BigDecimal spacePrice) {
		this.spacePrice = spacePrice;
	}

	public BigDecimal getDownPrice() {
		return downPrice;
	}

	public void setDownPrice(BigDecimal downPrice) {
		this.downPrice = downPrice;
	}

	public BigDecimal getRequestPrice() {
		return requestPrice;
	}

	public void setRequestPrice(BigDecimal requestPrice) {
		this.requestPrice = requestPrice;
	}

	public BigDecimal getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(BigDecimal totalPrice) {
		this.totalPrice = totalPrice;
	}
	
	
}
