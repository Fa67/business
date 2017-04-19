package com.eayun.eayunstack.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;

public interface OpenstackBaseService<T> {

	public List<T> listAll(String datacenterId) throws AppException;

	public List<T> list(String datacenterId, String projectId)
			throws AppException;

	public T getById(String datacenterId, String projectId, String id)
			throws AppException;

	public T create(String datacenterId, String projectId, JSONObject data)
			throws AppException;

	public boolean delete(String datacenterId, String projectId, String id)
			throws AppException;

	public T update(String datacenterId, String projectId, JSONObject data,
			String id) throws AppException;

}
