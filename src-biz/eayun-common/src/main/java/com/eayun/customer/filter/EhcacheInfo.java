package com.eayun.customer.filter;

import java.util.HashMap;
import java.util.Map;


public class EhcacheInfo {
private Map<String, String> configMap = new HashMap<String,String>();
	
	private static EhcacheInfo ehcacheInfo = null;
	private EhcacheInfo(){
		
	}
	
	public static EhcacheInfo getInstance(){
		if(ehcacheInfo == null){
			 ehcacheInfo = new EhcacheInfo();
			 return ehcacheInfo;
		}
		return ehcacheInfo;
	}

	public Map<String, String> getConfigMap() {
		return configMap;
	}

	public void setConfigMap(Map<String, String> configMap) {
		this.configMap = configMap;
	}

}
