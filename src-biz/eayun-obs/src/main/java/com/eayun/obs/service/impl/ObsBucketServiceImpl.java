package com.eayun.obs.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.Grant;
import com.amazonaws.services.s3.model.Grantee;
import com.eayun.accesskey.model.AccessKey;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.HmacSHA1Util;
import com.eayun.common.util.ObsUtil;
import com.eayun.common.util.XMLUtil;
import com.eayun.obs.base.service.ObsBaseService;
import com.eayun.obs.model.BucketOwner;
import com.eayun.obs.model.BucketStorageBean;
import com.eayun.obs.model.BucketUesdAndRequestBean;
import com.eayun.obs.model.CdnBucket;
import com.eayun.obs.model.ObsAccessBean;
import com.eayun.obs.model.ObsBucket;
import com.eayun.obs.model.ObsResultBean;
import com.eayun.obs.service.ObsBucketService;
import com.eayun.obs.service.ObsCdnBucketService;
import com.eayun.obs.util.SortClass;
import com.eayun.obs.util.SortDoubleClass;

/**
 * ObsBucketServiceImpl
 *
 * @Filename: ObsBucketServiceImpl.java
 * @Description:
 * @Version: 1.0
 * @Author: liyanchao
 * @Email: yanchao.li@eayun.com
 * @History:<br> <li>Date: 2016年1月12日</li> <li>Version: 1.0</li> <li>Content:
 * create</li>
 */
@Service
@Transactional
public class ObsBucketServiceImpl implements ObsBucketService {
    private static final Logger log = LoggerFactory.getLogger(ObsBucketServiceImpl.class);
    @Autowired
    private ObsBaseService obsBaseService;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private ObsCdnBucketService obsCdnBucketService;
    /**
     * 获取Bucket分页列表信息
     * 前端页面输入的查询内容，用于过滤不匹配的记录
     *
     * @return page
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public Page getBucketPageList(Page page, String name, QueryMap queryMap,
                                  AccessKey accessKeyObj, String bucketName) throws Exception {
        String secretKey = accessKeyObj.getSecretKey();
        String accessKey = accessKeyObj.getAccessKey();
        List<ObsBucket> listBucket = new ArrayList<ObsBucket>();
        String host = ObsUtil.getEayunObsHost();
        String httpHeader = ObsUtil.getRequestHeader();
        String date = DateUtil.getRFC2822Date(new Date());
        String canonicalizedResource = "/";
        String signature = ObsUtil.getSignature("GET", "", "", date, "",
                canonicalizedResource);
        String hmacSHA1 = HmacSHA1Util.getEncrypt(signature, secretKey);
        ObsAccessBean obsBean = new ObsAccessBean();
        obsBean.setHost(host);
        obsBean.setUrl(httpHeader + host + canonicalizedResource);
        obsBean.setHmacSHA1(hmacSHA1);
        obsBean.setAccessKey(accessKey);
        obsBean.setRFC2822Date(date);
        obsBean.setHttp("http://".equals(httpHeader));

        ObsResultBean resultBean = obsBaseService.get(obsBean);

        String resDataString = "";
        if (resultBean.getCode().equals("200")) {
            resDataString = XMLUtil.xml2JSON(resultBean.getResData());

            JSONObject resJson = JSONObject.parseObject(resDataString);

            JSONObject resultOwner = resJson.getJSONObject("Owner");
            BucketOwner owner = new BucketOwner();

            owner.setOwnerId(resultOwner.get("ID").toString());
            owner.setDisplayName(resultOwner.get("DisplayName").toString());
            JSONObject resultBuckets = resJson.getJSONObject("Buckets");

            if (null != resultBuckets) {

                try {
                    JSONArray resultArray = resultBuckets.getJSONArray("Bucket");
                    JSONArray result = JSONArray.parseArray(resultArray.toString());
                    for (int i = 0; i < result.size(); i++) {
                        ObsBucket bucket = new ObsBucket();
                        bucket.setBucketName(JSONObject.parseObject(result.get(i).toString()).get("Name").toString());
                        bucket.setCreationDate(DateUtil.dateToString(DateUtil.formatUTCDate(JSONObject.parseObject(result.get(i).toString()).get("CreationDate").toString())));

                        listBucket.add(bucket);
                    }

                } catch (Exception e) {
                    log.error("getBucketPageList异常。当前resultBuckets是："+resultBuckets.toJSONString(), e);
                    JSONObject result = resultBuckets.getJSONObject("Bucket");
                    if (null != result) {
                        ObsBucket bucket = new ObsBucket();
                        bucket.setBucketName(result.get("Name").toString());
                        bucket.setCreationDate(DateUtil.dateToString(DateUtil.formatUTCDate(result.get("CreationDate").toString())));
                        listBucket.add(bucket);
                    }

                } finally {
                    // 按时间Desc排序开始
                    SortClass sort = new SortClass();
                    Collections.sort(listBucket, sort);

                    // 条件查询
                    List<ObsBucket> listBucketResult = new ArrayList<ObsBucket>();
                    if (!"".equals(bucketName) && null != bucketName) {
                        for (int i = 0; i < listBucket.size(); i++) {
                            if (listBucket.get(i).getBucketName()
                                    .contains(bucketName.trim())) {
                                listBucketResult.add(listBucket.get(i));
                            }
                        }
                    } else {
                        listBucketResult = listBucket;
                    }
                    /************************* 构造假的分页开始 ***************************/
                    int startIndex = (queryMap.getPageNum() - 1)
                            * queryMap.getCURRENT_ROWS_SIZE();
                    int end = (startIndex + queryMap.getCURRENT_ROWS_SIZE()) - 1;
                    if (listBucketResult.size() - 1 < end) {
                        end = listBucketResult.size() - 1;
                    }
                    List<ObsBucket> listResult = new ArrayList<ObsBucket>();
                    List<ObsBucket> listResultAcl = new ArrayList<ObsBucket>();
                    for (int i = 0; i < listBucketResult.size(); i++) {
                        listBucketResult.get(i).setCdnStatus("0");
                        listBucketResult.get(i).setIsOpencdn("0");
                        if (startIndex <= i && i <= end) {
                            listResult.add(listBucketResult.get(i));
                        }
                    }
                    listResultAcl = this.getBucketAclList(listResult, accessKeyObj);
                    /************************* 获取CDN关联状态开始 ***************************/
                    List<CdnBucket> cdnBucketList = obsCdnBucketService.getUnDeleteListByCusId(owner.getOwnerId());
                    for(CdnBucket cdnBucket : cdnBucketList){
                        for(ObsBucket obsBucket :listResultAcl){
                            if(obsBucket.getBucketName().equals(cdnBucket.getBucketName())){
                                obsBucket.setCdnStatus(cdnBucket.getCdnStatus());
                                obsBucket.setIsOpencdn(cdnBucket.getIsOpencdn());
                                obsBucket.setDomainId(cdnBucket.getDomainId());
                                break;
                            }
                        }
                    }
                    page = new Page(startIndex, listBucketResult.size(), 10,
                            listResultAcl);
                }


            }

        }

        return page;
    }

    //返回bucketAcllist
    @SuppressWarnings("unchecked")
    public List<ObsBucket> bucketAclList(AccessKey accessKeyObj) throws Exception {
        String secretKey = accessKeyObj.getSecretKey();
        String accessKey = accessKeyObj.getAccessKey();
        List<ObsBucket> listBucket = new ArrayList<ObsBucket>();
        List<ObsBucket> listResultAcl = new ArrayList<ObsBucket>();
        String host = ObsUtil.getEayunObsHost();
        String httpHeader = ObsUtil.getRequestHeader();
        String date = DateUtil.getRFC2822Date(new Date());
        String canonicalizedResource = "/";
        String signature = ObsUtil.getSignature("GET", "", "", date, "",
                canonicalizedResource);
        String hmacSHA1 = HmacSHA1Util.getEncrypt(signature, secretKey);
        ObsAccessBean obsBean = new ObsAccessBean();
        obsBean.setHost(host);
        obsBean.setUrl(httpHeader + host + canonicalizedResource);
        obsBean.setHmacSHA1(hmacSHA1);
        obsBean.setAccessKey(accessKey);
        obsBean.setRFC2822Date(date);
        obsBean.setHttp("http://".equals(httpHeader));

        ObsResultBean resultBean = obsBaseService.get(obsBean);

        String resDataString = "";
        if (resultBean.getCode().equals("200")) {
            resDataString = XMLUtil.xml2JSON(resultBean.getResData());

            JSONObject resJson = JSONObject.parseObject(resDataString);

            JSONObject resultOwner = resJson.getJSONObject("Owner");
            BucketOwner owner = new BucketOwner();

            owner.setOwnerId(resultOwner.get("ID").toString());
            owner.setDisplayName(resultOwner.get("DisplayName").toString());
            JSONObject resultBuckets = resJson.getJSONObject("Buckets");

            if (null != resultBuckets) {

                try {
                    JSONArray resultArray = resultBuckets.getJSONArray("Bucket");
                    JSONArray result = JSONArray.parseArray(resultArray.toString());
                    for (int i = 0; i < result.size(); i++) {
                        ObsBucket bucket = new ObsBucket();
                        bucket.setBucketName(JSONObject.parseObject(result.get(i).toString()).get("Name").toString());
                        bucket.setCreationDate(DateUtil.dateToString(DateUtil.formatUTCDate(JSONObject.parseObject(result.get(i).toString()).get("CreationDate").toString())));

                        listBucket.add(bucket);
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(),e);
                    JSONObject result = resultBuckets.getJSONObject("Bucket");
                    if (null != result) {
                        ObsBucket bucket = new ObsBucket();
                        bucket.setBucketName(result.get("Name").toString());
                        bucket.setCreationDate(DateUtil.dateToString(DateUtil.formatUTCDate(result.get("CreationDate").toString())));
                        listBucket.add(bucket);
                    }

                } finally {
                    // 按时间Desc排序开始
                    SortClass sort = new SortClass();
                    Collections.sort(listBucket, sort);
                    listResultAcl = this.getBucketAclList(listBucket, accessKeyObj);

                }


            }

        }

        return listResultAcl;
    }

    /**
     * 获取BucketList
     *
     * @return List
     * @throws Exception
     */
    public List<ObsBucket> getBucketList(AccessKey accessKeyObj) throws Exception {
        String secretKey = accessKeyObj.getSecretKey();
        String accessKey = accessKeyObj.getAccessKey();
        List<ObsBucket> listBucket = new ArrayList<ObsBucket>();
        String host = ObsUtil.getEayunObsHost();
        String httpHeader = ObsUtil.getRequestHeader();
        String date = DateUtil.getRFC2822Date(new Date());
        String canonicalizedResource = "/";
        String signature = ObsUtil.getSignature("GET", "", "", date, "",
                canonicalizedResource);
        String hmacSHA1 = HmacSHA1Util.getEncrypt(signature, secretKey);
        ObsAccessBean obsBean = new ObsAccessBean();
        obsBean.setHost(host);
        obsBean.setUrl(httpHeader + host + canonicalizedResource);
        obsBean.setHmacSHA1(hmacSHA1);
        obsBean.setAccessKey(accessKey);
        obsBean.setRFC2822Date(date);
        obsBean.setHttp("http://".equals(httpHeader));
        ObsResultBean resultBean = obsBaseService.get(obsBean);

        String resDataString = "";
        if (resultBean.getCode().equals("200")) {
            resDataString = XMLUtil.xml2JSON(resultBean.getResData());

            JSONObject resJson = JSONObject.parseObject(resDataString);

            JSONObject resultOwner = resJson.getJSONObject("Owner");
            BucketOwner owner = new BucketOwner();

            owner.setOwnerId(resultOwner.get("ID").toString());
            owner.setDisplayName(resultOwner.get("DisplayName").toString());
            JSONObject resultBuckets = resJson.getJSONObject("Buckets");

            if (null != resultBuckets) {

                try {
                    JSONArray resultArray = resultBuckets
                            .getJSONArray("Bucket");
                    JSONArray result = JSONArray.parseArray(resultArray
                            .toString());
                    for (int i = 0; i < result.size(); i++) {
                        ObsBucket bucket = new ObsBucket();
                        bucket.setBucketName(JSONObject
                                .parseObject(result.get(i).toString())
                                .get("Name").toString());
                        bucket.setCreationDate(DateUtil.dateToString(DateUtil
                                .formatUTCDate(JSONObject
                                        .parseObject(result.get(i).toString())
                                        .get("CreationDate").toString())));

                        listBucket.add(bucket);
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(),e);
                    JSONObject result = resultBuckets.getJSONObject("Bucket");
                    if (null != result) {
                        ObsBucket bucket = new ObsBucket();
                        bucket.setBucketName(result.get("Name").toString());
                        bucket.setCreationDate(DateUtil.dateToString(DateUtil
                                .formatUTCDate(result.get("CreationDate")
                                        .toString())));
                        listBucket.add(bucket);
                    }

                }

            }

        }

        return listBucket;
    }
    
    /**
     * 获取Bucket权限
     *
     * @param bucketList
     * @return List
     * @throws Exception
     */
    public List<ObsBucket> getBucketAclList(List<ObsBucket> obsList,
                                            AccessKey accessKeyObj) throws Exception {
        String accessKey = accessKeyObj.getAccessKey();
        String secretKey = accessKeyObj.getSecretKey();
        List<ObsBucket> bucketAclList = new ArrayList<ObsBucket>();
        
        AmazonS3 client = ObsUtil.createClient(accessKey, secretKey);
        
        // 循环获取每个bucket的权限
        for (ObsBucket bucket : obsList) {
            AccessControlList controlList = client.getBucketAcl(bucket
                    .getBucketName());
            List<Grant> linkGrant = controlList.getGrantsAsList();
            StringBuffer isPublic = new StringBuffer();
            StringBuffer permission = new StringBuffer();
            for (Grant grant : linkGrant) {
                Grantee grantee = grant.getGrantee();
                String identifier = grantee.getIdentifier();
                if (identifier.contains("AllUsers")) {
                    isPublic.append("public" + "、");
                }
                permission.append(grant.getPermission() + "、");
            }
            if (!isPublic.toString().contains("public")) {
                bucket.setPermission("私有读写");
                bucket.setPermissionEn("Private");
            } else if (isPublic.toString().contains("public")
                    && permission.toString().contains("READ")
                    && permission.toString().contains("WRITE")) {
                bucket.setPermission("公有读写");
                bucket.setPermissionEn("PublicReadWrite");
            } else if (isPublic.toString().contains("public")
                    && permission.toString().contains("READ")) {
                bucket.setPermission("公有读私有写");
                bucket.setPermissionEn("PublicRead");
            }
            bucketAclList.add(bucket);
        }

        return bucketAclList;
    }

    /**
     * 校验Bucket名称唯一
     *
     * @param name
     * @return boolean
     * @throws Exception
     */
    public boolean checkBucketName(String bucketName, AccessKey accessKeyObj)
            throws Exception {
        boolean flag = false;
        String secretKey = accessKeyObj.getSecretKey();
        String accessKey = accessKeyObj.getAccessKey();
        List<ObsBucket> listBucket = new ArrayList<ObsBucket>();

        String host = ObsUtil.getEayunObsHost();
        String httpHeader = ObsUtil.getRequestHeader();
        String date = DateUtil.getRFC2822Date(new Date());
        String canonicalizedResource = "/";
        String signature = ObsUtil.getSignature("GET", "", "", date, "",
                canonicalizedResource);
        String hmacSHA1 = HmacSHA1Util.getEncrypt(signature, secretKey);
        ObsAccessBean obsBean = new ObsAccessBean();
        obsBean.setHost(host);
        obsBean.setUrl(httpHeader + host + canonicalizedResource);
        obsBean.setHmacSHA1(hmacSHA1);
        obsBean.setAccessKey(accessKey);
        obsBean.setRFC2822Date(date);
        obsBean.setHttp("http://".equals(httpHeader));
        ObsResultBean resultBean = obsBaseService.get(obsBean);
        if (resultBean.getCode().equals("200")) {
            String resDataString = XMLUtil.xml2JSON(resultBean.getResData());
            JSONObject resJson = JSONObject.parseObject(resDataString);

            JSONObject resultBuckets = resJson.getJSONObject("Buckets");
            if (null != resultBuckets) {
                try {
                    JSONArray resultArray = resultBuckets.getJSONArray("Bucket");
                    JSONArray result = JSONArray.parseArray(resultArray.toString());
                    for (int i = 0; i < result.size(); i++) {
                        ObsBucket bucket = new ObsBucket();
                        bucket.setBucketName(JSONObject.parseObject(result.get(i).toString()).get("Name").toString());
                        bucket.setCreationDate(DateUtil.dateToString(DateUtil.formatUTCDate(JSONObject.parseObject(result.get(i).toString()).get("CreationDate").toString())));

                        listBucket.add(bucket);
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(),e);
                    JSONObject result = resultBuckets.getJSONObject("Bucket");
                    if (null != result) {
                        ObsBucket bucket = new ObsBucket();
                        bucket.setBucketName(result.get("Name").toString());
                        bucket.setCreationDate(DateUtil.dateToString(DateUtil
                                .formatUTCDate(result.get("CreationDate")
                                        .toString())));
                        listBucket.add(bucket);
                    }
                }
            }
            // 判断是否有相同名称的bucket
            if (!"".equals(bucketName) && null != bucketName) {
                for (int i = 0; i < listBucket.size(); i++) {
                    if (listBucket.get(i).getBucketName().equals(bucketName)) {
                        flag = true;
                        break;
                    }
                }
            }
        }

        return flag;
    }

    /**
     * 创建Bucket列表信息
     * 前端页面输入的查询内容，用于过滤不匹配的记录
     *
     * @return JSONObject
     * @throws Exception
     */

    public JSONObject addBucket(AccessKey accessKeyObj, Map<String, String> map)
            throws Exception {
        JSONObject resJson = new JSONObject();
        String secretKey = accessKeyObj.getSecretKey();
        String accessKey = accessKeyObj.getAccessKey();
        // 验证已有的bucket数量是否超限
        AmazonS3 client = ObsUtil.createClient(accessKey, secretKey);
        List<Bucket> bucketS3 = client.listBuckets();
        if (bucketS3.size() >= 99) {
            resJson.put("resCode", "101");
            resJson.put("resValue", "当前Bucket数目已达上限");
            return resJson;
        }

        // 验证是否重名
        String bucketName = map.get("name");
        boolean flag = this.checkBucketName(bucketName, accessKeyObj);
        if (flag) {
            resJson.put("resCode", "102");
            resJson.put("resValue", "Bucket名称已存在");
            return resJson;
        }
        // 创建
        String permission = map.get("permission");
        Bucket bucket = new Bucket();
        CreateBucketRequest createRequest = new CreateBucketRequest(bucketName);
        if ("Private".equals(permission)) {
            createRequest.setCannedAcl(CannedAccessControlList.Private);
        } else if ("PublicRead".equals(permission)) {
            createRequest.setCannedAcl(CannedAccessControlList.PublicRead);
        } else if ("PublicReadWrite".equals(permission)) {
            createRequest.setCannedAcl(CannedAccessControlList.PublicReadWrite);
        }
        try {
            bucket = client.createBucket(createRequest);
            if (null != bucket && null != bucket.getName()) {
                resJson.put("resCode", "200");
            }
        } catch (AmazonS3Exception e) {
            log.error(e.getMessage(),e);
            String errorMessage = e.getErrorCode();
            if (errorMessage.equals("BucketAlreadyExists")) {
                resJson.put("resCode", "102");
                resJson.put("resValue", "Bucket名称已存在");
            }
        }
        obsBaseService.setBucketCORS(accessKey, secretKey, bucketName);
        return resJson;
    }

    /**
     * 修改Bucket
     *
     * @return boolean
     * @throws Exception
     */
    public boolean editBucket(AccessKey accessKeyObj, Map<String, String> map)
            throws Exception {
        boolean flag = false;
        String secretKey = accessKeyObj.getSecretKey();
        String accessKey = accessKeyObj.getAccessKey();
        // 验证已有的bucket数量是否超限
        AmazonS3 client = ObsUtil.createClient(accessKey, secretKey);
        String bucketName = map.get("bucketName");
        // 修改
        String permission = map.get("permissionEn");
        if ("Private".equals(permission)) {
            client.setBucketAcl(bucketName, CannedAccessControlList.Private);
            flag = true;
        } else if ("PublicRead".equals(permission)) {
            client.setBucketAcl(bucketName, CannedAccessControlList.PublicRead);
            flag = true;
        } else if ("PublicReadWrite".equals(permission)) {
            client.setBucketAcl(bucketName,
                    CannedAccessControlList.PublicReadWrite);
            flag = true;
        }

        return flag;
    }

    /**
     * 删除Bucket
     * 前端页面输入的查询内容，用于过滤不匹配的记录
     *
     * @return JSONObject
     * @throws Exception
     */
    public JSONObject deleteBucket(String bucketName, AccessKey accessKeyObj)
            throws Exception {
        String secretKey = accessKeyObj.getSecretKey();
        String accessKey = accessKeyObj.getAccessKey();
        String requestHeader = ObsUtil.getRequestHeader();
        String baseHost = ObsUtil.getEayunObsHost(); // "obs.eayun.com";
        String uri = "/";
        String outFile = uri;

        String url = requestHeader + bucketName + "." + baseHost + outFile;
        String date = DateUtil.getRFC2822Date(new Date());
        String contentType = "application/octet-stream";
        String ContentMD5 = "";

        String relativePath = "/" + bucketName + outFile;
        StringBuffer sb = new StringBuffer(bucketName);
        sb.append(".");
        sb.append(baseHost);
        String signature = ObsUtil.getSignature("DELETE", ContentMD5,
                contentType, date, relativePath, "");
        String hmacSHA1 = HmacSHA1Util.getEncrypt(signature, secretKey);
        ObsAccessBean obsBean = new ObsAccessBean();
        obsBean.setHost(sb.toString());
        obsBean.setUrl(url);
        obsBean.setHmacSHA1(hmacSHA1);
        obsBean.setAccessKey(accessKey);
        obsBean.setRFC2822Date(date);
        obsBean.setHttp("http://".equals(requestHeader));
        obsBean.setBucketName(bucketName);
        JSONObject resJson = obsBaseService.deleteBucket(obsBean);
        String result = resJson.getString("code");
        JSONObject returnJson = new JSONObject();
        if ("409".equals(result)) {
            String resData = resJson.getString("resData");
            String resDataString = XMLUtil.xml2JSON(resData);
            String re = JSONObject.parseObject(resDataString).getString("Code");
            if ("BucketNotEmpty".equals(re)) {
                returnJson.put("resCode", "409");
                returnJson.put("value", "非空Bucket不能删除");
            }
        } else if ("204".equals(result)) {
            returnJson.put("resCode", "204");
        } else if ("404".equals(result)) {
            String resData = resJson.getString("resData");
            String resDataString = XMLUtil.xml2JSON(resData);
            String re = JSONObject.parseObject(resDataString).getString("Code");
            if ("NoSuchBucket".equals(re)) {
                returnJson.put("resCode", "404");
                returnJson.put("value", "Bucket名称不存在");
            }

        }
        return returnJson;
    }

    /**
     * Bucket总存储量
     *
     * @return List
     */
    @SuppressWarnings("unchecked")
    public List<BucketStorageBean> getBucketStorage(String cusId , String bucketName) throws Exception {
        List<BucketStorageBean> storageList = new ArrayList<BucketStorageBean>();
        List<BucketStorageBean> minMax = new ArrayList<BucketStorageBean>();
        List<BucketStorageBean> storageEndList = new ArrayList<BucketStorageBean>();
        int interval = 25;
        Date lastTime = new Date();
        Date beforeTime = DateUtil.addDay(lastTime, new int[]{0, 0, 0, -25});
        Date end = DateUtil.addDay(lastTime, new int[]{0, 0, 0, -24});
        Date start = beforeTime;
        for (int i = 0; i < interval; i++) {
            BucketStorageBean storageBean = new BucketStorageBean();
            Criteria criatira = new Criteria();
            criatira.andOperator(Criteria.where("bucket").is(bucketName),
            		Criteria.where("owner").is(cusId),
                    Criteria.where("timestamp").gte(start),
                    Criteria.where("timestamp").lt(end));
            List<JSONObject> jsonList = mongoTemplate.find(new Query(criatira), JSONObject.class, MongoCollectionName.OBS_STORAGE_1H);
            if (null != jsonList && jsonList.size() > 0) {
                JSONObject result = jsonList.get(jsonList.size() - 1);
                String bucketStorage = result.getJSONObject("usage").getString("size_kb_actual");

                storageBean.setTimestamp(end);
                if (null == bucketStorage) {
                    storageBean.setBucketStorage(new BigDecimal(0));
                } else {
                    storageBean.setBucketStorage(new BigDecimal(bucketStorage).divide(new BigDecimal(1024), 2, BigDecimal.ROUND_HALF_UP));
                }

            } else {
                storageBean.setBucketStorage(new BigDecimal(0));
                storageBean.setTimestamp(end);
            }
            storageList.add(storageBean);

            start = end;
            end = DateUtil.addDay(start, new int[]{0, 0, 0, +1});

        }
        //将list重新赋值给新的变量
        for (BucketStorageBean bean : storageList) {
            storageEndList.add(bean);
        }

        minMax = storageList;

        // 按数值大小升序排序开始
        SortDoubleClass sort = new SortDoubleClass();
        Collections.sort(minMax, sort);
//		BigDecimal min = minMax.get(0).getBucketStorage().multiply(new BigDecimal(0.8)).setScale(2,BigDecimal.ROUND_HALF_UP);
//		BigDecimal max = minMax.get(minMax.size() - 1).getBucketStorage().multiply(new BigDecimal(1.2)).setScale(2,BigDecimal.ROUND_HALF_UP);

        BigDecimal min = minMax.get(0).getBucketStorage().multiply(new BigDecimal(1));
        BigDecimal max = minMax.get(minMax.size() - 1).getBucketStorage().multiply(new BigDecimal(1));

        BigDecimal maxAvg = getStroageSelfAvg(minMax);
//		System.out.println("maxAvg:"+maxAvg+"max:"+max);
        //第一种情况min<=avg<=max
//		if(maxAvg.compareTo(min)>=0 && maxAvg.compareTo(max)<=0){
//			max = maxAvg.multiply(new BigDecimal(2));
//			min = min.multiply(new BigDecimal(0));
//		}else if(maxAvg.compareTo(min)>0 && maxAvg.compareTo(max)>0){//第2种情况min<avg   avg>max
//			max = maxAvg;
//			min = min.multiply(new BigDecimal(0));
//		}else if(maxAvg.compareTo(min)>0 && maxAvg.compareTo(max)<=0){//第3种情况min>avg   avg>max
//			max = maxAvg.multiply(new BigDecimal(2));
//			min = maxAvg;
//		}
        if (maxAvg.compareTo(max) >= 0 && maxAvg.compareTo(min) > 0) {  //avg>=max && avg>min
            max = maxAvg;
            min = new BigDecimal(0);
        } else if (maxAvg.compareTo(max) < 0 && maxAvg.compareTo(min) > 0) {  //avg<max && avg>min
            max = maxAvg.multiply(new BigDecimal(2));
            min = new BigDecimal(0);
        } else if (maxAvg.compareTo(max) < 0 && maxAvg.compareTo(min) <= 0) {  //avg<max && avg<=min
            max = maxAvg.multiply(new BigDecimal(2));
            min = maxAvg;
        }


        for (int j = 0; j < minMax.size(); j++) {
            storageEndList.get(j).setMinStorage(min);
            storageEndList.get(j).setMaxStorage(max);
        }
        return storageEndList;
    }

    /**
     * Bucket流量请求次数或流入流出流量
     *
     * @return List
     */
    public List<BucketUesdAndRequestBean> getBucketUsedAndRequest(
    		String cusId , String bucketName, String type) throws Exception {

        List<BucketUesdAndRequestBean> storageList = new ArrayList<BucketUesdAndRequestBean>();
        int interval = 25;
        Date lastTime = new Date();
        Date beforeTime = DateUtil.addDay(lastTime, new int[]{0, 0, 0, -25});
        Date end = DateUtil.addDay(lastTime, new int[]{0, 0, 0, -24});
        Date start = beforeTime;
        for (int i = 0; i < interval; i++) {

            BucketUesdAndRequestBean usedAndRequestBean = new BucketUesdAndRequestBean();
            Criteria criatira = new Criteria();
            criatira.andOperator(Criteria.where("bucket").is(bucketName),
            		Criteria.where("owner").is(cusId),
                    Criteria.where("timestamp").gte(start),
                    Criteria.where("timestamp").lt(end));
            List<JSONObject> jsonList = mongoTemplate.find(new Query(criatira),
                    JSONObject.class, MongoCollectionName.OBS_USED_1H);
            
            /**剔除掉回源流量：dbb*/
            Criteria backCriatira = new Criteria();
            backCriatira.andOperator(Criteria.where("bucket_name").is(bucketName),
            		Criteria.where("cus_id").is(cusId),
                    Criteria.where("timestamp").gte(start),
                    Criteria.where("timestamp").lt(end));
            List<JSONObject> backJsonList = mongoTemplate.find(new Query(backCriatira),
                    JSONObject.class, MongoCollectionName.CDN_BACKSOURCE_1H);
            
            
            if (type.equals("used")) {// 查询流量的流入流出
                BigDecimal download = new BigDecimal(0);
                BigDecimal upload = new BigDecimal(0);
                for (int j = 0; j < jsonList.size(); j++) {
                    JSONObject obj = jsonList.get(j);
                    JSONArray categories = obj.getJSONArray("categories");
                    Date thisTime = obj.getDate("timestamp");
                    thisTime = DateUtil.dateRemoveSec(thisTime);
                    long oneSent = 0;
                    for (int k = 0; k < categories.size(); k++) {
                        Long bytesSent = categories.getJSONObject(k).getLong(
                                "bytes_sent");
                        oneSent+=bytesSent;
                        Long received = categories.getJSONObject(k).getLong(
                                "bytes_received");
                        upload = upload.add(new BigDecimal(received));
                    }
                    /**剔除掉回源流量：dbb*/
                    long backData=0;
                    if(null!=backJsonList && !backJsonList.isEmpty()){
                    	for (int k = 0; k < backJsonList.size(); k++) {
                    		JSONObject backJson = backJsonList.get(k);
                    		Date backThisTime = backJson.getDate("timestamp");
                    		backThisTime = DateUtil.dateRemoveSec(backThisTime);
                    		if(thisTime.getTime()==backThisTime.getTime()){
                    			backData = backJson.getLongValue("backsource");
                    			break;
                    		}
                    	}
                    }
                    long diffData = (oneSent-backData)>0?oneSent-backData:0;
                    download = download.add(new BigDecimal(diffData));
                    
                }
                usedAndRequestBean.setNetIn(download.divide(new BigDecimal(1024 * 1024d), 2, BigDecimal.ROUND_HALF_EVEN));
                usedAndRequestBean.setNetOut(upload.divide(new BigDecimal(1024 * 1024d), 2, BigDecimal.ROUND_HALF_EVEN));
                usedAndRequestBean.setTimestamp(end);
                usedAndRequestBean.setType(type);
            } else if (type.equals("request")) {// 查询put、get、delete的请求次数
                BigDecimal Get = new BigDecimal(0);
                BigDecimal Put = new BigDecimal(0);
                BigDecimal Delete = new BigDecimal(0);
                if (null != jsonList && jsonList.size() > 0) {
                    JSONObject obj = jsonList.get(0);
                    JSONArray categories = obj.getJSONArray("categories");
                    for (int k = 0; k < categories.size(); k++) {
                        String category = categories.getJSONObject(k)
                                .getString("category");
                        if (category.contains("put_")) {
                            String put = categories.getJSONObject(k).getString("ops");
                            Put = Put.add(new BigDecimal(put));
                        } else if (category.contains("delete_")) {
                            String delete = categories.getJSONObject(k).getString("ops");
                            Delete = Delete.add(new BigDecimal(delete));
                        } else if (category.contains("get_")) {
                            String get = categories.getJSONObject(k).getString("ops");
                            Get = Get.add(new BigDecimal(get));

                        }
                    }
                }
                usedAndRequestBean.setRequestPut(Put);
                usedAndRequestBean.setRequestGet(Get);
                usedAndRequestBean.setRequestDelete(Delete);
                usedAndRequestBean.setTimestamp(end);
                usedAndRequestBean.setType(type);
            }

            storageList.add(usedAndRequestBean);

            start = end;
            end = DateUtil.addDay(start, new int[]{0, 0, 0, +1});

        }

        if (type.equals("used")) {// 查询流量的流入流出
            BigDecimal netMin = new BigDecimal(0);
            BigDecimal netMax = new BigDecimal(0);
            //排序流入流量最大值，最小值
            List<BigDecimal> netInList = new ArrayList<BigDecimal>();
            List<BigDecimal> netOutList = new ArrayList<BigDecimal>();
            for (BucketUesdAndRequestBean inOutBean : storageList) {
                netInList.add(inOutBean.getNetIn());
                netOutList.add(inOutBean.getNetOut());
            }
            Collections.sort(netInList, new Comparator<BigDecimal>() {
                @Override
                public int compare(BigDecimal o1, BigDecimal o2) {
                    return o1.compareTo(o2);
                }
            });
            Collections.sort(netOutList, new Comparator<BigDecimal>() {
                @Override
                public int compare(BigDecimal o1, BigDecimal o2) {
                    return o1.compareTo(o2);
                }
            });
            //得到流量最小值
            if (netOutList.get(0).compareTo(netInList.get(0)) < 0) {
                netMin = netOutList.get(0);
            } else {
                netMin = netInList.get(0);
            }
            //得到流量最大值
            if (netOutList.get(netOutList.size() - 1).compareTo(netInList.get(netInList.size() - 1)) < 0) {
                netMax = netInList.get(netInList.size() - 1);
            } else {
                netMax = netOutList.get(netOutList.size() - 1);
            }
            //赋值流入流量最大值，最小值
//			BigDecimal min = netMin.multiply(new BigDecimal(0.8)).setScale(2,BigDecimal.ROUND_HALF_UP);
//			BigDecimal max = netMax.multiply(new BigDecimal(1.2)).setScale(2,BigDecimal.ROUND_HALF_UP);
            BigDecimal min = netMin;
            BigDecimal max = netMax;
            BigDecimal maxAvg = getNetInOutSelfAvg(netMax);
            //第一种情况min<=avg<=max
//			if(maxAvg.compareTo(min)>=0 && maxAvg.compareTo(max)<=0){
//				max = maxAvg.multiply(new BigDecimal(2));
//				min = min.multiply(new BigDecimal(0));
//			}else if(maxAvg.compareTo(min)>0 && maxAvg.compareTo(max)>0){//第2种情况min<avg   avg>max
//				max = maxAvg;
//				min = min.multiply(new BigDecimal(0));
//			}else if(maxAvg.compareTo(min)>0 && maxAvg.compareTo(max)>0){//第3种情况min>avg   avg>max
//				max = maxAvg.multiply(new BigDecimal(2));
//				min = maxAvg;
//			}
            if (maxAvg.compareTo(max) >= 0 && maxAvg.compareTo(min) > 0) {  //avg>=max && avg>min
                max = maxAvg;
                min = new BigDecimal(0);
            } else if (maxAvg.compareTo(max) < 0 && maxAvg.compareTo(min) > 0) {  //avg<max && avg>min
                max = maxAvg.multiply(new BigDecimal(2));
                min = new BigDecimal(0);
            } else if (maxAvg.compareTo(max) < 0 && maxAvg.compareTo(min) <= 0) {  //avg<max && avg<=min
                max = maxAvg.multiply(new BigDecimal(2));
                min = maxAvg;
            }


            for (BucketUesdAndRequestBean inOutBean : storageList) {
                inOutBean.setNetMin(min);
                inOutBean.setNetMax(max);
            }

        } else if (type.equals("request")) {// 查询put、get、delete的请求次数
            BigDecimal requestMinTimes = new BigDecimal(0);
            BigDecimal requestMaxTimes = new BigDecimal(0);
            List<BigDecimal> getList = new ArrayList<BigDecimal>();
            List<BigDecimal> putList = new ArrayList<BigDecimal>();
            List<BigDecimal> deleteList = new ArrayList<BigDecimal>();
            List<BigDecimal> requestList = new ArrayList<BigDecimal>();
            for (BucketUesdAndRequestBean inOutBean : storageList) {
                getList.add(inOutBean.getRequestGet());
                putList.add(inOutBean.getRequestPut());
                deleteList.add(inOutBean.getRequestDelete());
            }
            //比较get
            Collections.sort(getList, new Comparator<BigDecimal>() {
                @Override
                public int compare(BigDecimal o1, BigDecimal o2) {
                    return o1.compareTo(o2);
                }
            });
            //比较put
            Collections.sort(putList, new Comparator<BigDecimal>() {
                @Override
                public int compare(BigDecimal o1, BigDecimal o2) {
                    return o1.compareTo(o2);
                }
            });
            //比较delete
            Collections.sort(deleteList, new Comparator<BigDecimal>() {
                @Override
                public int compare(BigDecimal o1, BigDecimal o2) {
                    return o1.compareTo(o2);
                }
            });
            //生成图标y轴最大值最小值
            if (null != getList && getList.size() > 0) {
                requestList.add(getList.get(0));
                requestList.add(getList.get(getList.size() - 1));
            }

            if (null != putList && putList.size() > 0) {
                requestList.add(putList.get(0));
                requestList.add(putList.get(putList.size() - 1));
            }

            if (null != deleteList && deleteList.size() > 0) {
                requestList.add(deleteList.get(0));
                requestList.add(deleteList.get(deleteList.size() - 1));
            }
            //排序最终y轴数据
            Collections.sort(requestList, new Comparator<BigDecimal>() {
                @Override
                public int compare(BigDecimal o1, BigDecimal o2) {
                    return o1.compareTo(o2);
                }
            });

            if (null != requestList && requestList.size() > 0) {
                requestMinTimes = requestList.get(0);
                requestMaxTimes = requestList.get(requestList.size() - 1);
            }
//			BigDecimal min = requestMinTimes.multiply(new BigDecimal(0.8)).setScale(2,BigDecimal.ROUND_HALF_UP);
//			BigDecimal max = requestMaxTimes.multiply(new BigDecimal(1.2)).setScale(2,BigDecimal.ROUND_HALF_UP);
            BigDecimal min = requestMinTimes;
            BigDecimal max = requestMaxTimes;
            BigDecimal maxAvg = getRequestTimesSelfAvg(requestList);
            //第一种情况min<=avg<=max
//			if(maxAvg.compareTo(min)>=0 && maxAvg.compareTo(max)<=0){
//				max = maxAvg.multiply(new BigDecimal(2));
//				min = min.multiply(new BigDecimal(0));
//			}else if(maxAvg.compareTo(min)>0 && maxAvg.compareTo(max)>0){//第2种情况min<avg   avg>max
//				max = maxAvg;
//				min = min.multiply(new BigDecimal(0));
//			}else if(maxAvg.compareTo(min)>0 && maxAvg.compareTo(max)>0){//第3种情况min>avg   avg>max
//				max = maxAvg.multiply(new BigDecimal(2));
//				min = maxAvg;
//			}
            if (maxAvg.compareTo(max) >= 0 && maxAvg.compareTo(min) > 0) {  //avg>=max && avg>min
                max = maxAvg;
                min = new BigDecimal(0);
            } else if (maxAvg.compareTo(max) < 0 && maxAvg.compareTo(min) > 0) {  //avg<max && avg>min
                max = maxAvg.multiply(new BigDecimal(2));
                min = new BigDecimal(0);
            } else if (maxAvg.compareTo(max) < 0 && maxAvg.compareTo(min) <= 0) {  //avg<max && avg<=min
                max = maxAvg.multiply(new BigDecimal(2));
                min = maxAvg;
            }

            for (BucketUesdAndRequestBean inOutBean : storageList) {
                inOutBean.setRequestMinTimes(min);
                inOutBean.setRequestMaxTimes(max);
            }
        }

        return storageList;
    }

    /**
     * 根据最大值，最小值，得到Stroage自定义的avg参考最大值
     *
     * @return BigDecimal
     */
    public BigDecimal getStroageSelfAvg(List<BucketStorageBean> list) {
        BigDecimal max = list.get(list.size() - 1).getBucketStorage();
//		System.out.println("最大值为："+max);
        int weiShu = 0;
        if (max.compareTo(new BigDecimal(1)) >= 0) {
            weiShu = max.subtract(new BigDecimal(1)).toBigInteger().toString().length();
        } else {
            weiShu = 1;
        }
        BigDecimal jiShu = new BigDecimal(1);
        for (int i = 0; i < weiShu; i++) {
            jiShu = jiShu.multiply(new BigDecimal(10));
        }
//		System.out.println("最大平均值avg为："+jiShu.divide(new BigDecimal(2), 0, BigDecimal.ROUND_HALF_UP));
        return jiShu.divide(new BigDecimal(2), 0, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 根据最大值，最小值，得到请求次数   自定义的avg参考最大值
     *
     * @return BigDecimal
     */
    public BigDecimal getRequestTimesSelfAvg(List<BigDecimal> list) {
        BigDecimal max = list.get(list.size() - 1);
//		System.out.println("最大值为："+max);
        int weiShu = 0;
        if (max.compareTo(new BigDecimal(1)) >= 0) {
            weiShu = max.subtract(new BigDecimal(1)).toBigInteger().toString().length();
        } else {
            weiShu = 1;
        }
        BigDecimal jiShu = new BigDecimal(1);
        for (int i = 0; i < weiShu; i++) {
            jiShu = jiShu.multiply(new BigDecimal(10));
        }
        return jiShu.divide(new BigDecimal(2), 0, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 根据最大值，最小值，得到流量流入，流出   自定义的avg参考最大值
     *
     * @param BigDecimal
     * @return BigDecimal
     */
    public BigDecimal getNetInOutSelfAvg(BigDecimal maxNet) {
        BigDecimal max = maxNet;
//		System.out.println("最大值为："+max);
        int weiShu = 0;
        if (max.compareTo(new BigDecimal(1)) >= 0) {
            weiShu = max.subtract(new BigDecimal(1)).toBigInteger().toString().length();
        } else {
            weiShu = 1;
        }
        BigDecimal jiShu = new BigDecimal(1);
        for (int i = 0; i < weiShu; i++) {
            jiShu = jiShu.multiply(new BigDecimal(10));
        }
        return jiShu.divide(new BigDecimal(2), 0, BigDecimal.ROUND_HALF_UP);
    }
}
