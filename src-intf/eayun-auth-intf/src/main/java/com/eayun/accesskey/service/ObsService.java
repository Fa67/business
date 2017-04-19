package com.eayun.accesskey.service;

public interface ObsService {
	
	public String put(String cusId,String ak,String sk) throws Exception;
	
	public String delete(String accessKey) throws Exception;
}
