package com.eayun.obs.thread.cdn;

import com.alibaba.fastjson.JSONObject;
import com.eayun.cdn.impl.ALiDNS;
import com.eayun.cdn.intf.CDN;
import com.eayun.cdn.util.CDNConstant;
import com.eayun.common.RedisNodeIdConstant;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.obs.model.BaseCdnBucket;
import com.eayun.obs.model.CdnBucket;
import com.eayun.obs.service.ObsCdnBucketService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class CdnRefreshBucketThread implements Runnable {
	
	private final Logger log = LoggerFactory.getLogger(CdnRefreshBucketThread.class);

	private JedisUtil jedisUtil;
	
	private CDN cdn;
	
	private ALiDNS dns;
	
	private ObsCdnBucketService obsCdnBucketService;

	public CdnRefreshBucketThread(JedisUtil jedisUtil,CDN cdn,ALiDNS dns,ObsCdnBucketService obsCdnBucketService) {
		this.jedisUtil = jedisUtil;
		this.obsCdnBucketService = obsCdnBucketService;
		this.cdn=cdn;
		this.dns=dns;
	}

	@Override
	public void run() {
		try {
			String value = jedisUtil.pop("CDN_REFRESH:SYNCBUCKET");
			if(null != value){
				try {
					JSONObject json = new JSONObject();
					json = JSONObject.parseObject(value);
					String bucketName = json.getString("bucket");
					String cusId = json.getString("customerId");
					log.info("处理底层操作bucket:"+bucketName+"消息");
					
					if(null != json.getString("domain")){
						CdnBucket cdnBucket = obsCdnBucketService.getDeleteByDomain(json.getString("domain"));
						if(null != cdnBucket.getId()){
							//关联表信息改为已删除，时间设置为当前
							BaseCdnBucket baseCdnBucket = new BaseCdnBucket();
							BeanUtils.copyPropertiesByModel(baseCdnBucket, cdnBucket);
							baseCdnBucket.setIsDelete("1");
							//删除DNS记录
							if(null != cdnBucket.getRecordId() && null != json.getString("error_type") && "DNS".equals(json.getString("error_type"))){
								String dnsResult = dns.removeRecord(cdnBucket.getRecordId());
								JSONObject dnsResp = new JSONObject();
								dnsResp = JSONObject.parseObject(dnsResult);
								String code = dnsResp.getString("Code");
								if(null == code){
									baseCdnBucket.setRecordId(null);
								}else{
									log.error(dnsResp.getString("Message"));
									
									json.put("error_type", "DNS");
									json.put("domain", cdnBucket.getDomainId());
									json.put("message", dnsResp.getString("Message"));
									jedisUtil.push("CDN_REFRESH:SYNCBUCKET", json.toJSONString());
								}
							}
							//CDN操作,外链状态设置为false,解除域名绑定
							if(CDNConstant.cdnProvider.UpYun.toString().equals(cdnBucket.getCdnProvider())){
								if(null != json.getString("error_type") && "CDN".equals(json.getString("error_type"))){//当前为开通开通状态
									String cdnResult = cdn.deleteDomain(cdnBucket.getDomainId(), cdnBucket.getBucketName()+"."+getCdnUrlByNodeID(RedisNodeIdConstant.CDN_ACCELERATE_ADDRESS));
									JSONObject cdnResp = new JSONObject();
									cdnResp = JSONObject.parseObject(cdnResult);
									String isok = cdnResp.getString("result");
									if(null != isok && "true".equals(isok)){
										baseCdnBucket.setIsOpencdn("0");
										baseCdnBucket.setCdnStatus("0");
										baseCdnBucket.setCloseTime(new Date());
									}else{
										log.error(cdnResp.getString("message"));
										
										json.put("error_type", "CDN");
										json.put("domain", cdnBucket.getDomainId());
										json.put("message", cdnResp.getString("message"));
										jedisUtil.push("CDN_REFRESH:SYNCBUCKET", json.toJSONString());
									}
								}
							}else{	//其他供应商的情况，未定
							}
							obsCdnBucketService.update(baseCdnBucket);
						}
					}else{
						CdnBucket cdnBucket = obsCdnBucketService.getUnDeleteByCusAndName(bucketName, cusId, null);
						if(null != cdnBucket.getId()){
							//关联表信息改为已删除，时间设置为当前
							BaseCdnBucket baseCdnBucket = new BaseCdnBucket();
							BeanUtils.copyPropertiesByModel(baseCdnBucket, cdnBucket);
							baseCdnBucket.setDeleteTime(new Date());
							baseCdnBucket.setIsDelete("1");
							//删除DNS记录
							if(null != cdnBucket.getRecordId()){
								String dnsResult = dns.removeRecord(cdnBucket.getRecordId());
								JSONObject dnsResp = new JSONObject();
								dnsResp = JSONObject.parseObject(dnsResult);
								String code = dnsResp.getString("Code");
								if(null == code){
									baseCdnBucket.setRecordId(null);
								}else{
									
									log.error(dnsResp.getString("Message"));
									
									json.put("error_type", "DNS");
									json.put("domain", cdnBucket.getDomainId());
									json.put("message", dnsResp.getString("Message"));
									jedisUtil.push("CDN_REFRESH:SYNCBUCKET", json.toJSONString());
								}
							}
							//CDN操作,外链状态设置为false,解除域名绑定
							if(CDNConstant.cdnProvider.UpYun.toString().equals(cdnBucket.getCdnProvider())){
								String cdnResult = cdn.deleteDomain(cdnBucket.getDomainId(), cdnBucket.getBucketName()+"."+getCdnUrlByNodeID(RedisNodeIdConstant.CDN_ACCELERATE_ADDRESS));
								JSONObject cdnResp = new JSONObject();
								cdnResp = JSONObject.parseObject(cdnResult);
								String isok = cdnResp.getString("result");
								if(null != isok && "true".equals(isok)){
									baseCdnBucket.setIsOpencdn("0");
									baseCdnBucket.setCdnStatus("0");
									baseCdnBucket.setCloseTime(new Date());
								}else{
									log.error(cdnResp.getString("message"));
									
									json.put("error_type", "CDN");
									json.put("domain", cdnBucket.getDomainId());
									json.put("message", cdnResp.getString("message"));
									jedisUtil.push("CDN_REFRESH:SYNCBUCKET", json.toJSONString());
								}
							}else{	//其他供应商的情况，未定
							}
							obsCdnBucketService.update(baseCdnBucket);
						}
					}
				} catch (Exception e) {
					jedisUtil.push("CDN_REFRESH:SYNCBUCKET", value);
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
