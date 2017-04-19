package com.eayun.obs.ecmcmodel;

import java.math.BigDecimal;
import java.util.List;

/**
 * EcmcObsEchartsBean
 * @Filename: EcmcObsEchartsBean.java
 * @Description:ecmc对象存储【存储量，请求次数、内网上传下载流量】生成图表专用bean
 * @Version: 1.1
 * @Author: liyanchao
 * @Email: yanchao.li@eayun.com
 * @History:<br> <li>Date: 2016年3月28日</li> 
 * <li>Version: 1.1</li>
 */
public class EcmcObsEchartsBean {
	private List<BigDecimal> yData;//y轴数据
	private List<String> xTime;//x轴数据
	private String type;//存储量/请求次数/下载流量
	private String yDataMin;//yData中最小值*0.8
    private String yDataMax;//yData中最大值*1.2
	
	private String originalDataMax;//y轴原始最大值
    private String storageQuota;//返回阈值设定的存储量的值
    private String requestQuota;//返回阈值设定的请求次数的值
    private String loadFlowQuota; //返回阈值设定的下载流量的值
    
	public String getOriginalDataMax() {
		return originalDataMax;
	}
	public void setOriginalDataMax(String originalDataMax) {
		this.originalDataMax = originalDataMax;
	}
	public List<BigDecimal> getyData() {
		return yData;
	}
	public void setyData(List<BigDecimal> yData) {
		this.yData = yData;
	}
	
	public List<String> getxTime() {
		return xTime;
	}
	public void setxTime(List<String> xTime) {
		this.xTime = xTime;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getyDataMin() {
		return yDataMin;
	}
	public void setyDataMin(String yDataMin) {
		this.yDataMin = yDataMin;
	}
	public String getyDataMax() {
		return yDataMax;
	}
	public void setyDataMax(String yDataMax) {
		this.yDataMax = yDataMax;
	}
	public String getStorageQuota() {
		return storageQuota;
	}
	public void setStorageQuota(String storageQuota) {
		this.storageQuota = storageQuota;
	}
	public String getRequestQuota() {
		return requestQuota;
	}
	public void setRequestQuota(String requestQuota) {
		this.requestQuota = requestQuota;
	}
	public String getLoadFlowQuota() {
		return loadFlowQuota;
	}
	public void setLoadFlowQuota(String loadFlowQuota) {
		this.loadFlowQuota = loadFlowQuota;
	}
	
    
	
}
