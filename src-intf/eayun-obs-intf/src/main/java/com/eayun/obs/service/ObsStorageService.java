package com.eayun.obs.service;

import java.util.Iterator;
import java.util.List;

import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.eayun.accesskey.model.AccessKey;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;

public interface ObsStorageService {

    public JSONObject checkStorageName(String bucketName, String folderName, String address, AccessKey accessKeyObj, JSONObject object) throws Exception;

    public Page getStorageList(Page page, QueryMap queryMap, String bucketName, String obsName, String folderName, AccessKey accessKeyObj) throws Exception;

    public boolean delete(String bucketName, List<String> obsNames, AccessKey accessKeyObj) throws Exception;

    public JSONObject add(String bucketName, String folderName, String obsName, AccessKey accessKeyObj) throws Exception;

    public void upload(Iterator<String> itr, MultipartHttpServletRequest request, String bucketName, String folderName, AccessKey accessKeyObj) throws Exception;

    public void initialProgressPercent(JSONObject object);

    public void getProgressPercent(String obsName, JSONObject object) throws Exception;

    public boolean abordUpload(String bucketName, String obsName, AccessKey accessKeyObj) throws Exception;

    public void getUrl(String bucketName, String obsName, AccessKey accessKeyObj, JSONObject object) throws Exception;

    public JSONObject getAuthorization(String contentType ,String uri, String httpMethod, JSONObject object, AccessKey accessKeyObj) throws Exception;

    public void junkUploadIdRecycling(String bucketName ,String obsName ,String uploadId) throws Exception;

	public boolean obsIsStopService(String cusId) throws Exception;
}
