package com.eayun.obs.base.service;

import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.amazonaws.services.s3.model.ObjectListing;
import com.eayun.accesskey.model.AccessKey;

public interface ObsBaseStorageService {
	public ObjectListing getResJson(String url, String folderName, AccessKey accessKeyObj) throws Exception;
    public JSONObject getResJson2(String url, String folderName, AccessKey accessKeyObj) throws Exception;

    public void delete(String bucketName, String obsName, AccessKey accessKeyObj) throws Exception;

    public JSONObject add(String bucketName, String folderName, String obsName, AccessKey accessKeyObj) throws Exception;

    public void upload(MultipartFile multipartFile, String bucketName, String folderName, AccessKey accessKeyObj) throws Exception;

    public void initialProgressPercent(JSONObject object);

    public void getProgressPercent(String obsName, JSONObject object) throws Exception;

    public boolean abortUpload(String bucketName, String obsName, AccessKey accessKeyObj) throws Exception;

    public void getUrl(String bucketName, String obsName, AccessKey accessKeyObj, JSONObject object) throws Exception;

    public JSONObject getAuthorization(String contentType ,String uri, String httpMethod, JSONObject object, AccessKey accessKeyObj) throws Exception;

}
