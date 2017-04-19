package com.eayun.obs.base.service;

import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.amazonaws.AmazonClientException;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.eayun.accesskey.model.AccessKey;
import com.eayun.common.RedisNodeIdConstant;
import com.eayun.common.exception.AppException;
import com.eayun.common.httpclient.HttpClientFactory;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.HmacSHA1Util;
import com.eayun.common.util.ObsUtil;
import com.eayun.common.util.XMLUtil;
import com.eayun.obs.model.CdnBucket;
import com.eayun.obs.service.ObsCdnBucketService;

@Service
@Transactional
public class ObsBaseStorageServiceImpl implements ObsBaseStorageService {
    private static final Logger log = LoggerFactory.getLogger(ObsBaseStorageServiceImpl.class);
    
    @Autowired
	private ObsCdnBucketService obsCdnBucketService;
    
    @Autowired
    private JedisUtil jedisUtil;

    public static Map<String, Upload> uploadMap = new HashMap<String, Upload>();

    @Override
    public ObjectListing getResJson(String bucketName, String folderName, AccessKey accessKeyObj) throws Exception {
        AmazonS3 client = ObsUtil.createClient(accessKeyObj.getAccessKey(), accessKeyObj.getSecretKey());
        ListObjectsRequest loq = new ListObjectsRequest();
        loq.setBucketName(bucketName);
        loq.setDelimiter("/");
        if (!folderName.equals("")) {
            loq.setPrefix(folderName);
        }
        ObjectListing objects = client.listObjects(loq);
        return objects;
    }
    
    public JSONObject getResJson2(String bucketName, String folderName, AccessKey accessKeyObj) throws Exception {
    	String accessKey  = accessKeyObj.getAccessKey();
        String secretKey  = accessKeyObj.getSecretKey();
        String host=ObsUtil.getEayunObsHost();
        String header=ObsUtil.getRequestHeader();
        String date = DateUtil.getRFC2822Date(new Date());
        String url = header + bucketName + "."+host+"/?delimiter=/&prefix=" + folderName;
        String signature = ObsUtil.getSignature("GET", "", "", "\nx-amz-date:" + date, "/" + bucketName + "/", "");
		
        String hmacSHA1 = HmacSHA1Util.getEncrypt(signature, secretKey);
        
        HttpClient httpclient = HttpClientFactory.getHttpClient("http://".equals(header));
        
        HttpGet get = new HttpGet(url);
        get.addHeader("Authorization", "AWS "+accessKey+":"+hmacSHA1);
        get.addHeader("Host", bucketName + "."+host);
        get.addHeader("x-amz-date", date);
        HttpResponse res = httpclient.execute(get);
        int code = res.getStatusLine().getStatusCode();
        String resData = EntityUtils.toString(res.getEntity(),"UTF-8");
        String resDataString = XMLUtil.xml2JSON(resData);
        JSONObject resJsonData = JSONObject.parseObject(resDataString);
        log.info("code:" + code);
        log.info("res:" + res);
        log.info("resData:" + resData);
        log.info("resDataString:" + resDataString);
        log.info("resJsonData:" + resJsonData);
        log.info("end");
        JSONObject resJson = new JSONObject();
        resJson.put("code", code);
        resJson.put("resJsonData", resJsonData);
        return resJson;
    }

    @Override
    public void delete(String bucketName, String obsName, AccessKey accessKeyObj) throws Exception {
        AmazonS3 client = ObsUtil.createClient(accessKeyObj.getAccessKey(), accessKeyObj.getSecretKey());
        client.deleteObject(bucketName, obsName);
    }

    @Override
    public JSONObject add(String bucketName, String folderName, String obsName, AccessKey accessKeyObj) throws Exception {
        String accessKey = accessKeyObj.getAccessKey();
        String secretKey = accessKeyObj.getSecretKey();
        String host=ObsUtil.getEayunObsHost();
        String header=ObsUtil.getRequestHeader();
        String date = DateUtil.getRFC2822Date(new Date());
        String url = header + bucketName + "."+host+"/" + folderName + obsName + "/";
        String signature = ObsUtil.getSignature("PUT", "", "application/octet-stream", date, "/" + bucketName + "/" + folderName + obsName + "/", "");

        String hmacSHA1 = HmacSHA1Util.getEncrypt(signature, secretKey);

        HttpClient httpclient = HttpClientFactory.getHttpClient("http://".equals(header));

        HttpPut put = new HttpPut(url);
        put.addHeader("Authorization", "AWS " + accessKey + ":" + hmacSHA1);
        put.addHeader("Host", bucketName + "."+host);
        put.addHeader("Date", date);
        put.setHeader("Content-Type", "application/octet-stream");
        HttpResponse res = httpclient.execute(put);
        String resData = EntityUtils.toString(res.getEntity(),"UTF-8");
        int code = res.getStatusLine().getStatusCode();
        log.info("code:" + code);
        log.info("res:" + res);
        log.info("resData:" + resData);
        JSONObject resJson = new JSONObject();
        resJson.put("code", code);
        resJson.put("resData", resData);
        return resJson;
    }

    @Override
    public void upload(MultipartFile multipartFile, String bucketName, String folderName, AccessKey accessKeyObj) throws Exception {
        long beginTime = System.currentTimeMillis();
        AmazonS3 client = ObsUtil.createClient(accessKeyObj.getAccessKey(), accessKeyObj.getSecretKey());
        final long size = multipartFile.getSize();
        InputStream inputStream = multipartFile.getInputStream();
//        String obj = folderName + URLEncoder.encode(multipartFile.getOriginalFilename(),"utf-8");
        String obj = folderName + multipartFile.getOriginalFilename();
        String buc = bucketName;
//        File f = new File(nativeLocation + multipartFile.getOriginalFilename());
//        multipartFile.transferTo(f);
        TransferManager tm = new TransferManager(client);
        //构造一个上传请求
        ObjectMetadata metaData = new ObjectMetadata();
        metaData.setContentLength(size);
        PutObjectRequest request = new PutObjectRequest(buc, obj, inputStream, metaData);
//        PutObjectRequest request = new PutObjectRequest(buc ,obj, f);
        //注册侦听接口
        request.setGeneralProgressListener(new ProgressListener() {
            double transferred = 0;

            @Override
            public void progressChanged(ProgressEvent progressEvent) {
                transferred += progressEvent.getBytesTransferred();
                log.info("Transferred bytes: " + transferred * 100 / size + "%");
            }
        });
        //上传
        Upload upload = tm.upload(request);
        if (uploadMap.containsKey(multipartFile.getOriginalFilename()) && uploadMap.get(multipartFile.getOriginalFilename()) == null) {
            upload.abort();
            uploadMap.remove(multipartFile.getOriginalFilename());
            return;
        } else {
            uploadMap.put(multipartFile.getOriginalFilename(), upload);
        }
        try {
            upload.waitForCompletion();
            log.info("上传成功");
//        	f.delete();
            log.info((System.currentTimeMillis() - beginTime) / 1000 + "s");
        } catch (AmazonClientException amazonClientException) {
            log.error("上传失败",amazonClientException);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }

    @Override
    public void initialProgressPercent(JSONObject object) {
        object.put("progressPercent", 0);
        object.put("progressDone", "unready");
    }

    @Override
    public void getProgressPercent(String obsName, JSONObject object) throws Exception {
        if (uploadMap.containsKey(obsName)) {
            object.put("progressPercent", uploadMap.get(obsName).getProgress().getPercentTransferred());
            object.put("progressDone", uploadMap.get(obsName).isDone());
            if (uploadMap.get(obsName).isDone()) {
                uploadMap.remove(obsName);
                object.put("progressDone", true);
            } else {
                object.put("progressDone", false);
            }
        } else {
            object.put("progressDone", "unready");
        }
    }

    @Override
    public boolean abortUpload(String bucketName, String obsName, AccessKey accessKeyObj) throws Exception {
//		AmazonS3 client = initialization(accessKeyObj.getAccessKey() ,accessKeyObj.getSecretKey());
        /*String id = new String();
		ListMultipartUploadsRequest allMultpartUploadsRequest = new ListMultipartUploadsRequest(bucketName);
		MultipartUploadListing multipartUploadListing = client.listMultipartUploads(allMultpartUploadsRequest);*/

//		TransferManager tm = new TransferManager(client);
        try {
//			tm.shutdownNow();
//			tm.abortMultipartUploads(bucketName ,new Date(System.currentTimeMillis()));
            if (uploadMap.containsKey(obsName)) {
                Upload upload = uploadMap.get(obsName);
                upload.abort();
                uploadMap.remove(obsName);
                log.info("终止上传");
            } else {
                uploadMap.put(obsName, null);
                log.info("扼杀上传");
                return false;
            }
        } catch (AmazonClientException amazonClientException) {
            log.error(amazonClientException.getMessage(),amazonClientException);
        }
        return true;
    }

    @Override
    public void getUrl(String bucketName, String obsName, AccessKey accessKeyObj, JSONObject object) throws Exception {
        AmazonS3 client = ObsUtil.createClient(accessKeyObj.getAccessKey(), accessKeyObj.getSecretKey());
        CdnBucket cdnBucket = obsCdnBucketService.getOpenByName(bucketName);
        if(null != cdnBucket.getId()){
        	String post = getCdnUrlByNodeID(RedisNodeIdConstant.CDN_ACCELERATE_ADDRESS);
        	client.setEndpoint(post);
        }
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, obsName);
        URL url = client.generatePresignedUrl(request);
        object.put("url", url);
    }
    private String getCdnUrlByNodeID(String nodeId) {
        String cdnUrl = null;
        try {
            String jsonStr = jedisUtil.get("sys_data_tree:"+nodeId);
            JSONObject json = JSONObject.parseObject(jsonStr);
            
            cdnUrl = json.getString("para1");
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
        return cdnUrl;
    }

    @Override
    public JSONObject getAuthorization(String contentType ,String uri, String httpMethod, JSONObject object, AccessKey accessKeyObj) throws Exception {
        String accessKey = accessKeyObj.getAccessKey();
        String secretKey = accessKeyObj.getSecretKey();
        String date = DateUtil.getRFC2822Date(new Date());
        String signature = ObsUtil.getSignature(httpMethod, "", contentType, "\nx-amz-date:" + date, URLDecoder.decode(uri ,"utf-8"), "");
        String hmacSHA1 = HmacSHA1Util.getEncrypt(signature, secretKey);
        object.put("authorization", "AWS " + accessKey + ":" + hmacSHA1);
        object.put("date", date);
        return object;
    }

}