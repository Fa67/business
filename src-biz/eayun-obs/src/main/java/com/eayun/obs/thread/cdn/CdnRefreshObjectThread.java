package com.eayun.obs.thread.cdn;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.cdn.intf.CDN;
import com.eayun.cdn.util.CDNConstant;
import com.eayun.common.RedisNodeIdConstant;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.ObsUtil;
import com.eayun.obs.model.CdnBucket;
import com.eayun.obs.service.ObsCdnBucketService;

public class CdnRefreshObjectThread implements Runnable {
	
	private final Logger log = LoggerFactory.getLogger(CdnRefreshObjectThread.class);

	private JedisUtil jedisUtil;
	
	private ObsCdnBucketService obsCdnBucketService;
	
	private CDN cdn;
	
	public CdnRefreshObjectThread(JedisUtil jedisUtil,CDN cdn,ObsCdnBucketService obsCdnBucketService) {
		this.jedisUtil = jedisUtil;
		this.obsCdnBucketService = obsCdnBucketService;
		this.cdn=cdn;
	}

	@Override
	public void run() {
		try {
			String value = jedisUtil.pop("CDN_REFRESH:SYNCOBJECT");
			if(null != value){
				try {
					log.info("处理底层操作object消息");
					JSONObject json = new JSONObject();
					json = JSONObject.parseObject(value);
					String bucketName = json.getString("bucket");
					String cusId = json.getString("customerId");
					CdnBucket cdnBucket = obsCdnBucketService.getUnDeleteByCusAndName(bucketName, cusId, null);
					if(null != cdnBucket.getId()){
						if(CDNConstant.cdnProvider.UpYun.toString().equals(cdnBucket.getCdnProvider())){
							List<String> urls = new ArrayList<String>();
							String[] fileUrls = new String[]{};
							JSONArray objects = json.getJSONArray("objects");
							for(int i = 0 ; i < objects.size();i++){
								JSONObject obj = objects.getJSONObject(i);
								String type = obj.getString("type");
								String object = obj.getString("object");
								if("".equals(type)||"".equals(object)){
									continue;
								}
								object = ObsUtil.getRequestHeader()+bucketName+"."+getCdnUrlByNodeID(RedisNodeIdConstant.CDN_ACCELERATE_ADDRESS)+"/"+object;
								urls.add(object);
							}
							fileUrls = urls.toArray(new String[urls.size()]);
							
							String result = cdn.purgeFiles(fileUrls);
							JSONObject respData = JSONObject.parseObject(result);
							String isok = respData.getString("result");
							if(null == isok || "false".equals(isok)){
								log.error(respData.getString("message"));
								jedisUtil.push("CDN_REFRESH:SYNCOBJECT", value);
							}
						}
					}
				} catch (Exception e) {
					jedisUtil.push("CDN_REFRESH:SYNCOBJECT", value);
					log.error(e.getMessage(),e);
				}
			}
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
		}
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

}
