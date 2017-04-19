package com.eayun.obs.service.impl;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.accesskey.model.AccessKey;
import com.eayun.accesskey.service.AccessKeyService;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.XMLUtil;
import com.eayun.obs.base.service.ObsBaseStorageService;
import com.eayun.obs.model.ObsStorage;
import com.eayun.obs.service.ObsStorageService;

@Service
@Transactional
public class ObsStorageServiceImpl implements ObsStorageService {
    @Autowired
    private ObsBaseStorageService obsBaseStorageService;
    @Autowired
    private JedisUtil jedisUtil;
    @Autowired
    private AccessKeyService accessKeyService;
    
    private String getEncodeName(String name, boolean isFolder) throws Exception {
        if (isFolder) {
            String nameEncoded = "";
            List<String> nameList = Arrays.asList(name.split("/"));
            for (String nameIndex : nameList) {
                if (!nameIndex.equals("")) {
                    nameEncoded += URLEncoder.encode(nameIndex, "utf-8") + "/";
                }
            }
            return nameEncoded;
        } else {
            if (name.indexOf("/") != -1) {
//				return name.substring(0, name.lastIndexOf("/")) + "/" + URLEncoder.encode(name.substring(name.lastIndexOf("/") + 1, name.length()), "utf-8");
                return getEncodeName(name.substring(0, name.lastIndexOf("/")), true) + URLEncoder.encode(name.substring(name.lastIndexOf("/") + 1, name.length()), "utf-8");
            }
            return URLEncoder.encode(name, "utf-8");
        }
    }

    private String getDecodeName(String name, boolean isFolder) throws Exception {
        String nameDecoded = "";
        List<String> nameList = Arrays.asList(name.split("/"));
        int index=name.lastIndexOf("/");
        for (String nameIndex : nameList) {
            if (!nameIndex.equals("")) {
                nameDecoded += URLDecoder.decode(nameIndex, "utf-8") + "/";
            	/*if(index!=-1&&index==name.length()-1){
            		nameDecoded += URLDecoder.decode(nameIndex, "utf-8") + "/";
            	}else{
            		nameDecoded +=URLDecoder.decode(nameIndex, "utf-8");
            	}*/
            }
        }
        if (index!=name.length()-1) {
            return nameDecoded.substring(0, nameDecoded.length()-1);
        }
        return nameDecoded;
        /*if (isFolder) {
            return nameDecoded;
        } else {
            return nameDecoded;
        }*/
    }

    /**
     * 将对象转换成JSONArray
     * @param object
     * @return
     */
    private JSONArray convertToJSONArray(Object object) {
    	if (object == null) {
    		return null;
    	} else {
    		String objString = object.toString();
    		if (!objString.contains("[")) {
    			objString = "[" + objString + "]";
    		}
    		JSONArray objArray = JSONArray.parseArray(objString);
    		return objArray;
    	}
    }
    /**
     * 拼装数据列表
     * @param bucketName
     * @param accessKeyObj
     * @param listObsStorage
     * @param folderList
     * @param obsList
     * @return
     * @throws Exception
     */
    private List<ObsStorage> assemblingDataList(String bucketName, AccessKey accessKeyObj, List<ObsStorage> listObsStorage, JSONArray folderList, JSONArray obsList) throws Exception {
    	if (folderList != null) {
        	for (int i = 0; i < folderList.size(); i++) {
        		ObsStorage storage = new ObsStorage();
        		storage.setObsName(getDecodeName(JSONObject.parseObject(folderList.get(i).toString()).getString("Prefix"), true));
        		storage.setBucketName(bucketName);
        		List<String> showNameList = Arrays.asList(storage.getObsName().split("/"));
        		storage.setObsShowName(showNameList.get(showNameList.size() - 1));
        		storage.setSize(-1);
        		storage.setType("文件夹");
        		storage.setFolder(true);
        		JSONObject object = obsBaseStorageService.getResJson2(bucketName, getEncodeName(storage.getObsName(), true), accessKeyObj);
        		JSONObject resObject = object.getJSONObject("resJsonData");
        		Object obj = resObject.get("Contents");
        		JSONArray array = convertToJSONArray(obj);
        		storage.setCreateTime(DateUtil.dateToString(DateUtil.formatUTCDate(JSONObject.parseObject(array.get(0).toString()).getString("LastModified"))));
        		listObsStorage.add(storage);
        	}
        }
        if (obsList != null) {
        	for (int i = 0; i < obsList.size(); i++) {
        		JSONObject object = JSONObject.parseObject(obsList.get(i).toString());
        		ObsStorage storage = new ObsStorage();
        		storage.setObsName(getDecodeName(object.getString("Key"), false));
        		int dotIndex = storage.getObsName().lastIndexOf(".");
        		int separator = storage.getObsName().lastIndexOf("/");
        		if (separator != storage.getObsName().length() - 1 ) {
        			if (dotIndex != -1 && dotIndex > separator) {
        				storage.setType(storage.getObsName().substring(dotIndex + 1));
        			} else {
        				/*这里处理没有文件后缀名的文件类型, 暂时为空*/
        			}
        		} else {
        			continue;
        		}
        		storage.setBucketName(bucketName);
        		if (storage.getObsName().indexOf("/") != -1) {
        			List<String> showNameList = Arrays.asList(storage.getObsName().split("/"));
        			storage.setObsShowName(showNameList.get(showNameList.size() - 1));
        		} else {
        			storage.setObsShowName(storage.getObsName());
        		}
        		storage.setSize(Long.parseLong(object.getString("Size")));
        		storage.setFolder(false);
        		storage.setCreateTime(DateUtil.dateToString(DateUtil.formatUTCDate(object.getString("LastModified"))));
        		listObsStorage.add(storage);
        	}
        }
        return listObsStorage;
    }
    /**
     * 拼装分页
     * @param page
     * @param queryMap
     * @param listObsStorage
     * @param obsName
     * @return
     */
    private Page assemblingDataPage(Page page, QueryMap queryMap, List<ObsStorage> listObsStorage, String obsName, String folderName) {
    	List<ObsStorage> listObsStorageResult = new ArrayList<ObsStorage>();
        if (!"".equals(obsName) && null != obsName) {
            for (int i = 0; i < listObsStorage.size(); i++) {
            	String fileName = listObsStorage.get(i).getObsShowName();
                if (fileName.contains(obsName.trim())) {
                    listObsStorageResult.add(listObsStorage.get(i));
                }
            }
        } else {
            for (int i = 0; i < listObsStorage.size(); i++) {
                listObsStorageResult.add(listObsStorage.get(i));
            }
        }

        int startIndex = (queryMap.getPageNum() - 1) * queryMap.getCURRENT_ROWS_SIZE();
        int end = (startIndex + queryMap.getCURRENT_ROWS_SIZE()) - 1;
        if (listObsStorageResult.size() - 1 < end) {
            end = listObsStorageResult.size() - 1;
        }
        List<ObsStorage> listResult = new ArrayList<ObsStorage>();
        for (int i = 0; i < listObsStorageResult.size(); i++) {
            if (startIndex <= i && i <= end) {
                listResult.add(listObsStorageResult.get(i));
            }
        }
        JSONObject json = new JSONObject();
        json.put("code", "200");
        json.put("data", listResult);
        page = new Page(startIndex, listObsStorageResult.size(), 10, json);
        return page;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Page getStorageList(Page page, QueryMap queryMap, String bucketName, String obsName, String folderName, AccessKey accessKeyObj) throws Exception {
    	JSONObject resJson = obsBaseStorageService.getResJson2(bucketName, getEncodeName(folderName, true), accessKeyObj);
    	String code = resJson.getString("code");
    	if (code.equals("200")) {
    		JSONObject jsonData = resJson.getJSONObject("resJsonData");
    		Object obsObject = jsonData.get("Contents");
    		if (obsObject == null && !folderName.equals("")) {
    			JSONObject json = new JSONObject();
    			json.put("code", "404");
    			json.put("msg", "该文件夹不存在！");
    			page = new Page(0, 0, 1, json);
    			return page;
    		}
    		JSONArray obsList = convertToJSONArray(obsObject);
    		Object folderObject = jsonData.get("CommonPrefixes");
    		JSONArray folderList = convertToJSONArray(folderObject);
    		List<ObsStorage> listObsStorage = assemblingDataList(bucketName, accessKeyObj, (List<ObsStorage>) page.getResult(), folderList, obsList);
    		page = assemblingDataPage(page, queryMap, listObsStorage, obsName, folderName);
    	} else if (code.equals("404")) {
    		JSONObject jsonData = resJson.getJSONObject("resJsonData");
    		Object resObject = jsonData.get("Code");
    		if ("NoSuchBucket".equals(resObject.toString())) {
    			JSONObject json = new JSONObject();
    			json.put("code", "404");
    			json.put("msg", "该Bucket不存在！");
    			page = new Page(0, 0, 1, json);
    		}
    	}
    	return page;
    }
    
    /*@Override
    public Page getStorageList(Page page, QueryMap queryMap, String bucketName, String obsName, String folderName, AccessKey accessKeyObj) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ObjectListing objectListing = obsBaseStorageService.getResJson(URLEncoder.encode(bucketName, "utf-8"), getEncodeName(folderName, true), accessKeyObj);
        if (!folderName.equals("")) {
            folderName += "/";
        }
        ObjectListing objectListingUncode = obsBaseStorageService.getResJson(URLEncoder.encode(bucketName, "utf-8"), folderName, accessKeyObj);
        List<String> listFolder = new ArrayList<String>();
        List<S3ObjectSummary> listObs = new ArrayList<S3ObjectSummary>();
        if (objectListing.getCommonPrefixes().size() > objectListingUncode.getCommonPrefixes().size()) {
            listFolder = objectListing.getCommonPrefixes();
            listObs = objectListing.getObjectSummaries();
        } else {
            listFolder = objectListingUncode.getCommonPrefixes();
            listObs = objectListingUncode.getObjectSummaries();
        }
        List<ObsStorage> listObsStorage = (List<ObsStorage>) page.getResult();
        if (listFolder != null || listObs != null) {
            for (int i = 0; i < listFolder.size(); i++) {
                ObsStorage storage = new ObsStorage();URLDecoder.decode(listFolder.get(i),"utf-8")
                storage.setObsName(getDecodeName(listFolder.get(i), true));
                storage.setBucketName(URLDecoder.decode(bucketName, "utf-8"));
                List<String> showNameList = Arrays.asList(storage.getObsName().split("/"));
                storage.setObsShowName(showNameList.get(showNameList.size() - 1));
                storage.setSize(-1);
                storage.setType("文件夹");
                storage.setFolder(true);
                ObjectListing list = obsBaseStorageService.getResJson(URLEncoder.encode(bucketName, "utf-8"), getEncodeName(storage.getObsName(), true), accessKeyObj);
                ObjectListing listUncode = obsBaseStorageService.getResJson(URLEncoder.encode(bucketName, "utf-8"), storage.getObsName(), accessKeyObj);
                if (list.getObjectSummaries().size() >= listUncode.getObjectSummaries().size()) {
                    storage.setCreateTime(formatter.format(list.getObjectSummaries().get(0).getLastModified()));
                } else {
                    storage.setCreateTime(formatter.format(listUncode.getObjectSummaries().get(0).getLastModified()));
                }
                listObsStorage.add(storage);

            }
            for (int i = 0; i < listObs.size(); i++) {
                S3ObjectSummary obj = listObs.get(i);
                ObsStorage storage = new ObsStorage();
                storage.setObsName(URLDecoder.decode(obj.getKey(), "utf-8"));
                int dotIndex = storage.getObsName().lastIndexOf(".");
                int separator = storage.getObsName().lastIndexOf("/");
                if (dotIndex != -1 && separator != storage.getObsName().length() - 1) {
                    storage.setType(storage.getObsName().substring(dotIndex + 1));
                } else {
                    continue;
                }
                storage.setBucketName(URLDecoder.decode(bucketName, "utf-8"));
                if (storage.getObsName().indexOf("/") != -1) {
                    List<String> showNameList = Arrays.asList(storage.getObsName().split("/"));
                    storage.setObsShowName(showNameList.get(showNameList.size() - 1));
                } else {
                    storage.setObsShowName(storage.getObsName());
                }
                storage.setSize(obj.getSize());
                storage.setFolder(false);
                storage.setCreateTime(formatter.format(obj.getLastModified()));
                listObsStorage.add(storage);
            }
        }
        List<ObsStorage> listObsStorageResult = new ArrayList<ObsStorage>();
        if (!"".equals(obsName) && null != obsName) {
            for (int i = 0; i < listObsStorage.size(); i++) {
                if (listObsStorage.get(i).getObsName().contains(obsName.trim())) {
                    listObsStorageResult.add(listObsStorage.get(i));
                }
            }
        } else {
            for (int i = 0; i < listObsStorage.size(); i++) {
                listObsStorageResult.add(listObsStorage.get(i));
            }
        }

        int startIndex = (queryMap.getPageNum() - 1) * queryMap.getCURRENT_ROWS_SIZE();
        int end = (startIndex + queryMap.getCURRENT_ROWS_SIZE()) - 1;
        if (listObsStorageResult.size() - 1 < end) {
            end = listObsStorageResult.size() - 1;
        }
        List<ObsStorage> listResult = new ArrayList<ObsStorage>();
        for (int i = 0; i < listObsStorageResult.size(); i++) {
            if (startIndex <= i && i <= end) {
                listResult.add(listObsStorageResult.get(i));
            }
        }
        page = new Page(startIndex, listObsStorageResult.size(), 10, listResult);
        return page;
    }*/
	
    @Override
    public JSONObject checkStorageName(String bucketName, String folderName, String address, AccessKey accessKeyObj, JSONObject object) throws Exception {
        JSONObject resObject = obsBaseStorageService.getResJson2(bucketName, getEncodeName(address, true), accessKeyObj);
        JSONObject obj = resObject.getJSONObject("resJsonData");
        JSONArray array = convertToJSONArray(obj.get("CommonPrefixes"));
        if (array != null) {
        	for (int i = 0; i < array.size(); i++) {
        		List<String> showNameList = Arrays.asList(getEncodeName(JSONObject.parseObject(array.get(i).toString()).getString("Prefix"), true).split("/"));
        		String folder = showNameList.get(showNameList.size() - 1);
        		if (folderName.equals(folder)) {
        			object.put("reName", true);
        			return object;
        		}
        	}
        }
        object.put("reName", false);
        return object;
    }

    @Override
    public boolean delete(String bucketName, List<String> obsNames, AccessKey accessKeyObj) throws Exception {
        boolean flag = true;
        for (String obsName : obsNames) {
            boolean isFolder = (obsName.lastIndexOf("/") == obsName.length() - 1);
            JSONObject resObject = obsBaseStorageService.getResJson2(bucketName, getEncodeName(obsName, isFolder), accessKeyObj);
            if (isFolder) {
            	JSONObject obj = resObject.getJSONObject("resJsonData");
            	JSONArray commonPrefixesArray = convertToJSONArray(obj.get("CommonPrefixes"));
            	JSONArray contentsArray = convertToJSONArray(obj.get("Contents"));
            	if (commonPrefixesArray == null && contentsArray.size() <= 1) {
            		obsBaseStorageService.delete(bucketName, obsName, accessKeyObj);
            	} else {
            		flag = false;
            	}
            } else {
            	obsBaseStorageService.delete(bucketName, obsName, accessKeyObj);
            }
        }
        return flag;
    }

    @Override
    public JSONObject add(String bucketName, String folderName, String obsName, AccessKey accessKeyObj) throws Exception {
        JSONObject resJson = new JSONObject();
        if (checkStorageName(bucketName, obsName, folderName, accessKeyObj, new JSONObject()).getBoolean("reName")) {
            resJson.put("resCode", "102");
            resJson.put("resMsg", "文件夹名称已经存在！");
            return resJson;
        }
        JSONObject object = obsBaseStorageService.add(bucketName, getEncodeName(folderName, true), URLEncoder.encode(obsName, "utf-8"), accessKeyObj);
        if ("200".equals(object.getString("code"))) {
        	resJson.put("resCode", "200");
        } else if ("403".equals(object.getString("code"))) {
        	String resDataString = XMLUtil.xml2JSON(object.getString("resData"));
        	String resCode = JSONObject.parseObject(resDataString).getString("Code");
        	if ("QuotaExceeded".equals(resCode)) {
        		resJson.put("resCode", "403");
        		resJson.put("resMsg", "对象创建已经超过了最大配额！");
        	}
        } else if ("400".equals(object.getString("code"))) {
        	String resDataString = XMLUtil.xml2JSON(object.getString("resData"));
        	String resCode = JSONObject.parseObject(resDataString).getString("Code");
        	if ("InvalidObjectName".equals(resCode)) {
        		resJson.put("resCode", "400");
        		resJson.put("resMsg", "文件夹Object总长度必须在1-1023字符之间！");
        	}
        }
		return resJson;
    }

    @Override
    public void upload(Iterator<String> itr, MultipartHttpServletRequest request, String bucketName, String folderName, AccessKey accessKeyObj) throws Exception {
        while (itr.hasNext()) {
            MultipartFile multipartFile = request.getFile(itr.next());
            if (folderName != "") {
                folderName += "/";
            }
//			obsBaseStorageService.upload(multipartFile ,bucketName ,getEncodeName(folderName ,false) ,accessKeyObj);
            obsBaseStorageService.upload(multipartFile, bucketName, folderName, accessKeyObj);
        }
    }

    @Override
    public void initialProgressPercent(JSONObject object) {
        obsBaseStorageService.initialProgressPercent(object);
    }

    @Override
    public void getProgressPercent(String obsName, JSONObject object) throws Exception {
//		obsBaseStorageService.getProgressPercent(getEncodeName(obsName ,false) ,object);
        obsBaseStorageService.getProgressPercent(getDecodeName(obsName, false), object);
    }

    @Override
    public boolean abordUpload(String bucketName, String obsName, AccessKey accessKeyObj) throws Exception {
//		return obsBaseStorageService.abortUpload(bucketName, getEncodeName(obsName ,false), accessKeyObj);
        return obsBaseStorageService.abortUpload(bucketName, getDecodeName(obsName, false), accessKeyObj);
    }

    @Override
    public void getUrl(String bucketName, String obsName, AccessKey accessKeyObj, JSONObject object) throws Exception {
        obsBaseStorageService.getUrl(bucketName, getDecodeName(obsName, false), accessKeyObj, object);
    }

    @Override
    public JSONObject getAuthorization(String contentType ,String uri, String httpMethod, JSONObject object, AccessKey accessKeyObj) throws Exception {
        return obsBaseStorageService.getAuthorization(contentType ,uri, httpMethod, object, accessKeyObj);
    }

    @Override
    public void junkUploadIdRecycling(String bucketName ,String obsName ,String uploadId) throws Exception {
    	JSONObject object = new JSONObject();
    	object.put("BucketName", bucketName);
    	object.put("ObjectName", obsName);
    	object.put("UploadId", uploadId);
    	jedisUtil.push("obs:obsJunkData", object.toJSONString());
    }

	@Override
	public boolean obsIsStopService(String cusId) throws Exception {
		AccessKey ak=accessKeyService.getDefaultAK(cusId);
		if(null==ak||ak.getIsStopService()==null){
			return false;
		}
		return ak.getIsStopService();
	}
}
