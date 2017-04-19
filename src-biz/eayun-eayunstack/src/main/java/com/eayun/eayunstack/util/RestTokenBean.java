package com.eayun.eayunstack.util;

public class RestTokenBean {

	private String userName = "";

	private String password = "";

	private String tenantId = "";

	/**
	 * 获得Token的Url
	 * 
	 */
	private String getTokenUrl = "";

	private String serviceName = "";

	private String keyStoneRegion = "";

	private String commonRegion = "";

	private String tokenId = "";

	private String endpoint = "";

	private long lastGetTime = 0l;

	private String commonRegionUrlType;
	/**
	 * 提交的Url
	 * 
	 */
	private String url;

	public String getUrl() {
		if (url == null) {
			return getTokenUrl;
		} else {
			return url;
		}
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getGetTokenUrl() {
		return getTokenUrl;
	}

	public void setGetTokenUrl(String getTokenUrl) {
		this.getTokenUrl = getTokenUrl;
	}

	public String getTokenId() {
		return tokenId;
	}

	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public long getLastGetTime() {
		return lastGetTime;
	}

	public void setLastGetTime(long lastGetTime) {
		this.lastGetTime = lastGetTime;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getKeyStoneRegion() {
		return keyStoneRegion;
	}

	public void setKeyStoneRegion(String keyStoneRegion) {
		this.keyStoneRegion = keyStoneRegion;
	}

	public String getCommonRegion() {
		return commonRegion;
	}

	public void setCommonRegion(String commonRegion) {
		this.commonRegion = commonRegion;
	}

	// 仅仅判断userName+password+tenantId+getTokenUrl+serviceName+keyStoneRegion+commonRegion
	@Override
	public int hashCode() {
		int result = 0;
		if (this.getUserName() != null) {
			result += 23 * this.getUserName().hashCode();
		}
		if (this.getPassword() != null) {
			result += 29 * this.getPassword().hashCode();
		}
		if (this.getTenantId() != null) {
			result += 31 * this.getTenantId().hashCode();
		}
		if (this.getGetTokenUrl() != null) {
			result += 37 * this.getGetTokenUrl().hashCode();
		}
		if (this.getServiceName() != null) {
			result += 3 * this.getServiceName().hashCode();
		}
		if (this.getKeyStoneRegion() != null) {
			result += 5 * this.getKeyStoneRegion().hashCode();
		}
		if (this.getCommonRegion() != null) {
			result += 7 * this.getCommonRegion().hashCode();
		}

		return result;
	}

	// 仅仅判断userName+password+tenantId+getTokenUrl+serviceName+keyStoneRegion+commonRegion
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof RestTokenBean)) {
			return false;
		}
		RestTokenBean myObj = (RestTokenBean) obj;

		return equals(this.getUserName(), myObj.getUserName())
				&& equals(this.getPassword(), myObj.getPassword())
				&& equals(this.getTenantId(), myObj.getTenantId())
				&& equals(this.getGetTokenUrl(), myObj.getGetTokenUrl())
				&& equals(this.getServiceName(), myObj.getServiceName())
				&& equals(this.getKeyStoneRegion(), myObj.getKeyStoneRegion())
				&& equals(this.getCommonRegion(), myObj.getCommonRegion());
	}

	private boolean equals(String a, String b) {
	    if(a == null){
	        if(b == null){
	            return true;
	        }else{
	            return false;
	        }
	        
	    }else{
	        return a.equals(b);
	    }
	}

	public String getCommonRegionUrlType() {
		return commonRegionUrlType;
	}

	
	public void setCommonRegionUrlType(String commonRegionUrlType) {
		this.commonRegionUrlType = commonRegionUrlType;
	}
	
}
