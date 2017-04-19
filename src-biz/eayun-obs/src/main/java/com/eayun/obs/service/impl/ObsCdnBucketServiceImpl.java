package com.eayun.obs.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.cdn.bean.CDNConfig;
import com.eayun.cdn.bean.CacheRule;
import com.eayun.cdn.impl.ALiDNS;
import com.eayun.cdn.intf.CDN;
import com.eayun.cdn.util.CDNConstant;
import com.eayun.cdn.util.CDNUtil;
import com.eayun.cdn.util.DNSUtil;
import com.eayun.common.RedisNodeIdConstant;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.ObsUtil;
import com.eayun.costcenter.service.AccountOverviewService;
import com.eayun.obs.dao.CdnBucketDao;
import com.eayun.obs.model.BaseCdnBucket;
import com.eayun.obs.model.BucketStorageBean;
import com.eayun.obs.model.CdnBucket;
import com.eayun.obs.service.ObsCdnBucketService;
import com.eayun.obs.service.ObsOpenService;
import com.eayun.obs.service.ObsStorageService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Scope("prototype")
@Transactional
public class ObsCdnBucketServiceImpl implements ObsCdnBucketService {
	
	private static final Logger log = LoggerFactory.getLogger(ObsCdnBucketServiceImpl.class);
	
	@Autowired
	private CdnBucketDao cdnBucketDao;
	
	@Autowired
    private MongoTemplate mongoTemplate;
	
	@Autowired
    private JedisUtil jedisUtil;
	
	@Autowired
	private ObsOpenService obsOpenService;
	@Autowired
	private AccountOverviewService accountOverviewService;
	@Autowired
	private ObsStorageService obsStorageService;

	@Override
	public JSONObject enableDomain(String bucketName, String cusId , String cdnProvider,String userName) throws Exception{
		log.info("开启CDN服务");
		boolean isSuccess = false;
		JSONObject returnJson = new JSONObject();
		JSONObject isAllowOpen=obsOpenService.isAllowOpen(userName, cusId);
		boolean isStop=obsStorageService.obsIsStopService(cusId);
		if(("obsIsAllowOpen".equals(isAllowOpen.getString("state"))||"balanceIsNotEnough".equals(isAllowOpen.getString("state")))&&!isStop){
			CdnBucket cdnBucket = getUnDeleteByCusAndName(bucketName, cusId , cdnProvider);
			if(null == cdnBucket.getId()){
				//第一次开启服务，需创建
				if(cdnProvider.equals(CDNConstant.cdnProvider.UpYun.toString())){
					CDNConfig config = new CDNConfig();
					config.setBucketName(bucketName);
					config.setOrigin(bucketName+"."+ObsUtil.getEayunObsHost());
					List<CacheRule> cacheRuleList = getCacheRuleList(RedisNodeIdConstant.CDN_CACHE_CONFIG);
					config.setCacheRuleList(cacheRuleList);
					String domain = getCdnUrlByNodeID(RedisNodeIdConstant.CDN_ACCELERATE_ADDRESS);
					config.setDomain(bucketName+"."+domain);
					
					CDN cdn = CDNUtil.getUpYunCDN();
					String result = cdn.createDomain(config);
					JSONObject json = JSONObject.parseObject(result);
					String isok = json.getString("result");
					if("true".equals(isok)){
						createCndBucket(cdnProvider, cusId, bucketName, json);
						return json;
					}else{
						log.error(json.getString("message"));
						returnJson.put("result", false);
						returnJson.put("message", json.getString("message"));
						int count = 0;
						while(null != json.getString("error_code") && json.getString("error_code").equals(CDNConstant.CDN_SYSERROR_CODE) && count < CDNConstant.CDN_ERROR_COUNT){
							count++;
							String elseResult = cdn.createDomain(config);
							json = JSONObject.parseObject(elseResult);
							String elseIsok = json.getString("result");
							if("true".equals(elseIsok)){
								createCndBucket(cdnProvider, cusId, bucketName, json);
								return json;
							}else{
								log.error(json.getString("message"));
								returnJson.put("result", false);
								returnJson.put("message", json.getString("message"));
							}
						}
					}
				}else{
					//其他CDN提供商情况，未定
				}
			}else{
				//关闭后重新开启
				if(cdnProvider.equals(CDNConstant.cdnProvider.UpYun.toString())){
					if("0".equals(cdnBucket.getIsOpencdn())){
						CDN cdn = CDNUtil.getUpYunCDN();
						String result = cdn.enableDomain(cdnBucket.getDomainId());
						JSONObject json = JSONObject.parseObject(result);
						String isok = json.getString("result");
						if("true".equals(isok)){
							try {
								isSuccess = editCdnBucket(cdnBucket);
								returnJson.put("result", isSuccess);
							} catch (IOException e) {
							    log.error(e.getMessage(),e);
							} catch (Exception e) {
							    log.error(e.getMessage(),e);
							}
						}else{
							log.error(json.getString("message"));
							returnJson.put("result", false);
							returnJson.put("message", json.getString("message"));
							int count = 0;
							while(null != json.getString("error_code") && json.getString("error_code").equals(CDNConstant.CDN_SYSERROR_CODE) && count < CDNConstant.CDN_ERROR_COUNT){
								count++;
								String elseResult = cdn.enableDomain(cdnBucket.getDomainId());
								json = JSONObject.parseObject(elseResult);
								String elseIsok = json.getString("result");
								if("true".equals(elseIsok)){
									try {
										isSuccess = editCdnBucket(cdnBucket);
										returnJson.put("result", isSuccess);
									} catch (IOException e) {
									    log.error(e.getMessage(),e);
									} catch (Exception e) {
									    log.error(e.getMessage(),e);
									}
								}else{
									log.error(json.getString("message"));
									returnJson.put("result", false);
									returnJson.put("message", json.getString("message"));
								}
							}
						}
					}else{
						//原本即是已开启状态时直接返回
						returnJson.put("result", true);
					}
				}else{
					returnJson.put("result", true);
					//其他CDN提供商情况，未定
				}
			}
		}else{
			returnJson.put("result", false);
			returnJson.put("message", isAllowOpen.getString("state"));
		}
		return returnJson;
	}
	/**(第一次)开启时创建关联记录 */
	private void createCndBucket(String cdnProvider,String cusId,String bucketName,JSONObject json){
		BaseCdnBucket baseCdnBucket = new BaseCdnBucket();
		baseCdnBucket.setCdnProvider(cdnProvider);
		baseCdnBucket.setCusId(cusId);
		baseCdnBucket.setBucketName(bucketName);
		baseCdnBucket.setIsOpencdn("1");
		baseCdnBucket.setCdnStatus("2");
		baseCdnBucket.setCdnPath(json.getString("cdn_cname"));
		baseCdnBucket.setDomainId(json.getString("domain_id"));
		baseCdnBucket.setRecordId(json.getString("record_id"));
		baseCdnBucket.setIsDelete("0");
		cdnBucketDao.saveEntity(baseCdnBucket);
	}
	/**开启时修改关联记录
	 * @throws Exception */
	@SuppressWarnings("unused")
    private boolean editCdnBucket(CdnBucket cdnBucket) throws Exception{
		//设置DNS记录状态为true
		ALiDNS dns = DNSUtil.getALiDNS();
		String dnsResp = dns.setRecordStatus(cdnBucket.getRecordId() ,true);
		JSONObject dnsJson = JSONObject.parseObject(dnsResp);
		String code = dnsJson.getString("Code");
		if(null == code){
			BaseCdnBucket baseCdnBucket = new BaseCdnBucket();
			BeanUtils.copyPropertiesByModel(baseCdnBucket, cdnBucket);
			baseCdnBucket.setIsOpencdn("1");
			baseCdnBucket.setCdnStatus("2");
			cdnBucketDao.merge(baseCdnBucket);
			return true;
		}else{
			log.error(dnsJson.getString("Message"));
			CDN cdn = CDNUtil.getUpYunCDN();
			String result = cdn.disableDomain(cdnBucket.getDomainId());
			return false;
		}
	}
	/**
	 * 获取加速域名,如设置为file.eayun.com
	 * @Author: duanbinbin
	 * @param nodeId
	 * @return
	 *<li>Date: 2016年6月24日</li>
	 */
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
	/**
	 * 获取缓存规则
	 * @Author: duanbinbin
	 * @param parentId
	 * @return
	 *<li>Date: 2016年6月22日</li>
	 */
	private List<CacheRule> getCacheRuleList(String parentId){
		List<CacheRule> cacheRuleList = new ArrayList<CacheRule>();
		Set<String> DataSet = null;
		List<String> DataList = new ArrayList<String>();
		try {
			DataSet = jedisUtil.getSet("sys_data_tree:parent:node_id:"+parentId);
			for(String data:DataSet){
				DataList.add(data);
			}
			Collections.sort(DataList);
			for(String data:DataList){
				String jsonStr = jedisUtil.get("sys_data_tree:"+data);
				
				JSONObject json = JSONObject.parseObject(jsonStr);
				CacheRule rule = new CacheRule();
				rule.setUri(json.getString("para1"));
				rule.setTtl(json.getLongValue("para2"));
				cacheRuleList.add(rule);
			}
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			throw new AppException("查询redis数据异常："+parentId);
		}
		return cacheRuleList;
	}
	@Override
	public JSONObject disableDomain(String bucketName, String cusId , String cdnProvider,String userName) throws Exception {
		log.info("关闭CDN服务");
		boolean isSuccess = false;
		JSONObject returnJson = new JSONObject();
		JSONObject isAllowOpen=obsOpenService.isAllowOpen(userName, cusId);
		boolean isStop=obsStorageService.obsIsStopService(cusId);
		if(("obsIsAllowOpen".equals(isAllowOpen.getString("state"))||"balanceIsNotEnough".equals(isAllowOpen.getString("state")))&&!isStop){
			CdnBucket cdnBucket = getUnDeleteByCusAndName(bucketName, cusId , cdnProvider);
			if(cdnProvider.equals(CDNConstant.cdnProvider.UpYun.toString())){
				if("1".equals(cdnBucket.getIsOpencdn())){//开通状态
					CDN cdn = CDNUtil.getUpYunCDN();
					String result = cdn.disableDomain(cdnBucket.getDomainId());
					JSONObject json = JSONObject.parseObject(result);
					String isok = json.getString("result");
					if(null != isok && "true".equals(isok)){
						try {
							isSuccess = editCloseCdnBucket(cdnBucket);
							returnJson.put("result", isSuccess);
						} catch (IOException e) {
						    log.error(e.getMessage(),e);
						} catch (Exception e) {
						    log.error(e.getMessage(),e);
						}
					}else{
						log.error(json.getString("message"));
						//isSuccess = false;
						returnJson.put("result", false);
						returnJson.put("message", json.getString("message"));
						int count = 0;
						while(null != json.getString("error_code") && json.getString("error_code").equals(CDNConstant.CDN_SYSERROR_CODE) && count < CDNConstant.CDN_ERROR_COUNT){
							count++;
							String elseResult = cdn.disableDomain(cdnBucket.getDomainId());
							json = JSONObject.parseObject(elseResult);
							String elseIsok = json.getString("result");
							if(null != elseIsok && "true".equals(elseIsok)){
								try {
									isSuccess = editCloseCdnBucket(cdnBucket);
									return returnJson;
								} catch (IOException e) {
								    log.error(e.getMessage(),e);
								} catch (Exception e) {
								    log.error(e.getMessage(),e);
								}
							}else{
								log.error(json.getString("message"));
								//isSuccess = false;
								returnJson.put("result", false);
								returnJson.put("message", json.getString("message"));
							}
						}
					}
				}else{
					//isSuccess = true;
					returnJson.put("result", true);
				}
			}else{
				returnJson.put("result", true);
				//其他CDN提供商情况，未定
			}
		}else{
			returnJson.put("result", false);
			returnJson.put("message", isAllowOpen.getString("state"));
		}
		return returnJson;
	}
	/**关闭时修改关联记录*/
	@SuppressWarnings("unused")
    private boolean editCloseCdnBucket(CdnBucket cdnBucket) throws Exception{
		//设置DNS记录状态为false
		ALiDNS dns = DNSUtil.getALiDNS();
		String dnsResp = dns.setRecordStatus(cdnBucket.getRecordId() ,false);
		JSONObject dnsJson = JSONObject.parseObject(dnsResp);
		String code = dnsJson.getString("Code");
		if(null == code){
			BaseCdnBucket baseCdnBucket = new BaseCdnBucket();
			BeanUtils.copyPropertiesByModel(baseCdnBucket, cdnBucket);
			baseCdnBucket.setIsOpencdn("0");
			baseCdnBucket.setCdnStatus("0");
			baseCdnBucket.setCloseTime(new Date());
			cdnBucketDao.merge(baseCdnBucket);
			return true;
		}else{
			log.error(dnsJson.getString("Message"));
			CDN cdn = CDNUtil.getUpYunCDN();
			String result = cdn.enableDomain(cdnBucket.getDomainId());
			return false;
		}
	}

	@Override
	public void addCdnLog(String bucketName,String doamin, String url,
			String operationCdn, JSONObject requestBody, int statuscode,
			JSONObject result) {
		log.info("添加CDN日志");
		JSONObject json = new JSONObject();
		json.put("bucketName", bucketName);
		json.put("cdnProvider", CDNConstant.cdnProvider.UpYun.toString());
		json.put("domainId", doamin);
		json.put("timestamp", new Date());
		json.put("operationCdn", operationCdn);
		json.put("URL", url);
		json.put("RequestBody", requestBody);
		json.put("statuscode", statuscode);
		
		json.put("message", result.get("message"));
		json.put("status", result.getString("result").equals("true")?"1":"0");
		mongoTemplate.insert(json,	MongoCollectionName.LOG_API_CDN);
	}

	@Override
	public List<BucketStorageBean> getCDNFlowData(String bucketName,String cusId,String cdnProvider) {
		log.info("查询CDN下载流量");
		List<BucketStorageBean> dataList = new ArrayList<BucketStorageBean>();
		Date now = new Date();
		Date startTime = DateUtil.addDay(now, new int[]{0,0,0,-25});
		
		String domain = "";
		CdnBucket cdnBucket = getUnDeleteByCusAndName(bucketName, cusId, cdnProvider);
		if(null != cdnBucket.getId()){
			domain = cdnBucket.getDomainId();
		}
		
		Sort sort = new Sort(Direction.ASC, "flow_data");
		Aggregation agg = Aggregation.newAggregation(
		        Aggregation.match(Criteria.where("timestamp").gt(startTime)),
		        Aggregation.match(Criteria.where("timestamp").lte(now)),
		        Aggregation.match(Criteria.where("domain_id").is(domain)),
		        Aggregation.match(Criteria.where("cdnProvider").is(cdnProvider)),
		        Aggregation.sort(sort)
		        );
		AggregationResults<JSONObject> totalresult = mongoTemplate.aggregate(agg,"obs.cdn.1h", JSONObject.class);
		List<JSONObject> totallist = totalresult.getMappedResults();
		for(int i = 0; i < 25;i++){
			Date timestamp = DateUtil.addDay(startTime, new int[]{0,0,0,i+1});
			Date last = DateUtil.addDay(startTime, new int[]{0,0,0,i});
			BucketStorageBean bean = new BucketStorageBean();
			bean.setTimestamp(timestamp);
			BigDecimal cdnFlow = new BigDecimal(0);
			for(JSONObject obj : totallist){
				Date time = obj.getDate("timestamp");
				if((time.after(last)&&time.before(timestamp))||time.equals(timestamp)){
					cdnFlow = obj.getBigDecimal("flow_data");
					break;
				}
			}
			bean.setCdnFlow(cdnFlow.divide(new BigDecimal(1024 * 1024d), 2, BigDecimal.ROUND_HALF_EVEN));
			dataList.add(bean);
		}
		BigDecimal min = new BigDecimal(0);
		BigDecimal max = new BigDecimal(0);
		
		if(totallist.size() > 0){
			min = totallist.get(0).getBigDecimal("flow_data").multiply(new BigDecimal(1));
	        max = totallist.get(totallist.size() - 1).getBigDecimal("flow_data").multiply(new BigDecimal(1));
		}
        min = min.divide(new BigDecimal(1024 * 1024d), 2, BigDecimal.ROUND_HALF_EVEN);
        max = max.divide(new BigDecimal(1024 * 1024d), 2, BigDecimal.ROUND_HALF_EVEN);
        
        max = getAxisMax(max);
		min = getAxisMin(min,max);
		dataList.get(0).setMaxStorage(max);
		dataList.get(0).setMinStorage(min);
		return dataList;
	}
	/**
	 * 获取纵坐标的最大值，为5*10*N或10*10*N
	 * @Author: duanbinbin
	 * @param max
	 * @return
	 *<li>Date: 2016年7月7日</li>
	 */
	private BigDecimal getAxisMax(BigDecimal max){
		BigDecimal axisMax = new BigDecimal(5);
		if(max.compareTo(new BigDecimal(0)) <= 0){
			return axisMax;
		}
		if(max.compareTo(new BigDecimal(1)) >= 0){
			int a = max.toBigInteger().toString().length();
			axisMax = new BigDecimal(Math.pow(10,a));
			if(max.compareTo(axisMax.divide(new BigDecimal(10))) == 0){
				return max;
			}
			if(max.compareTo(axisMax.divide(new BigDecimal(2))) <= 0){
				axisMax = axisMax.divide(new BigDecimal(2));
			}
		}else{
			BigDecimal number = new BigDecimal(1).divide(max,2,BigDecimal.ROUND_HALF_EVEN);
			int a = number.toBigInteger().toString().length();
			
			axisMax = new BigDecimal(1).divide(new BigDecimal(Math.pow(10,a-1)));
			if(max.compareTo(axisMax.divide(new BigDecimal(2))) <= 0){
				axisMax = axisMax.divide(new BigDecimal(2));
			}
		}
		return axisMax;
	}
	/**
	 * 获取纵坐标的最小值，为0或5*10*N
	 * @Author: duanbinbin
	 * @param min
	 * @param axisMax
	 * @return
	 *<li>Date: 2016年7月7日</li>
	 */
	private BigDecimal getAxisMin(BigDecimal min,BigDecimal axisMax){
		BigDecimal axisMin = new BigDecimal(0);
		BigDecimal mid = axisMax.divide(new BigDecimal(2));
		if(min.compareTo(mid) >= 0){
			axisMin = mid;
		}
		return axisMin;
	}
	@Override
	public void addDnsLog(String bucketName, String domain, String url,
			String operationDns, JSONObject responseBody,String recordId) {
		log.info("添加DNS日志");
		JSONObject json = new JSONObject();
		json.put("bucketName", bucketName);
		json.put("recordId", recordId);
		json.put("domainId", domain);
		json.put("timestamp", new Date());
		json.put("operationCdn", operationDns);
		json.put("URL", url);
		json.put("ResponseBody", responseBody);
		String status = responseBody.getJSONObject("status").getString("code");
		if("1".equals(status)){
			json.put("status", "1");
		}else{
			json.put("status", "0");
		}
		mongoTemplate.insert(json, MongoCollectionName.LOG_API_DNS);
		
	}

	/**
	 * 查询客户所有未删除的关联记录
	 * @Author: duanbinbin
	 * @param cusId
	 * @return
	 *<li>Date: 2016年6月16日</li>
	 */
	@SuppressWarnings("unchecked")
    @Override
	public List<CdnBucket> getUnDeleteListByCusId(String cusId) {
		List<CdnBucket> cdnBucketList = new ArrayList<CdnBucket>();
		String hql = "from BaseCdnBucket where cusId = ? and isDelete = '0' ";
		List<BaseCdnBucket> baseCdnBucketList = cdnBucketDao.find(hql, cusId);
		for(BaseCdnBucket baseCdnBucket :baseCdnBucketList){
			CdnBucket cdnBucket = new CdnBucket();
			BeanUtils.copyPropertiesByModel(cdnBucket, baseCdnBucket);
			cdnBucketList.add(cdnBucket);
		}
		return cdnBucketList;
	}

	/**
	 * 查询指定客户下指定bucket是否有未删除的关联记录，有则返回
	 * @Author: duanbinbin
	 * @param bucketName
	 * @param cusId
	 * @param cdnProvider
	 * @return
	 *<li>Date: 2016年6月20日</li>
	 */
	@SuppressWarnings("unchecked")
    @Override
	public CdnBucket getUnDeleteByCusAndName(String bucketName, String cusId,
			String cdnProvider) {
		CdnBucket cdnBucket = new CdnBucket();
		List<String> list = new ArrayList<String>();
		StringBuffer hql = new StringBuffer("from BaseCdnBucket where bucketName = ?  and cusId = ? and isDelete = '0' ");
		list.add(bucketName);
		list.add(cusId);
		if(null != cdnProvider){
			hql.append(" and cdnProvider = ?");
			list.add(cdnProvider);
		}
		List<BaseCdnBucket> baseCdnBucketList = cdnBucketDao.find(hql.toString(), list.toArray());
		if(null != baseCdnBucketList && baseCdnBucketList.size() > 0){
			BaseCdnBucket baseCdnBucket = baseCdnBucketList.get(0);
			BeanUtils.copyPropertiesByModel(cdnBucket, baseCdnBucket);
		}
		return cdnBucket;
	}

	@SuppressWarnings("unchecked")
    @Override
	public List<CdnBucket> getListForFlowData() {
		List<CdnBucket> cdnBucketList = new ArrayList<CdnBucket>();
		String hql = "from BaseCdnBucket where isOpencdn = '1' or (isOpencdn = '0' and closeTime > ?)";
		Date closeTime = DateUtil.addDay(new Date(), new int[] {0,0,0,-2});
		List<BaseCdnBucket> baseCdnBucketList = cdnBucketDao.find(hql, closeTime);
		if(null != baseCdnBucketList){
			for(BaseCdnBucket baseCdnBucket : baseCdnBucketList){
				CdnBucket cdnBucket = new CdnBucket();
				BeanUtils.copyPropertiesByModel(cdnBucket, baseCdnBucket);
				cdnBucketList.add(cdnBucket);
			}
		}
		return cdnBucketList;
	}

	@Override
	public void update(BaseCdnBucket baseCdnBucket) {
		cdnBucketDao.merge(baseCdnBucket);
	}

	@SuppressWarnings("unchecked")
    @Override
	public CdnBucket getOpenByName(String bucketName) {
		CdnBucket cdnBucket = new CdnBucket();
		List<String> list = new ArrayList<String>();
		StringBuffer hql = new StringBuffer("from BaseCdnBucket where bucketName = ?  and isOpencdn = '1' and isDelete = '0' ");
		list.add(bucketName);
		List<BaseCdnBucket> baseCdnBucketList = cdnBucketDao.find(hql.toString(), list.toArray());
		if(null != baseCdnBucketList && baseCdnBucketList.size() > 0){
			BaseCdnBucket baseCdnBucket = baseCdnBucketList.get(0);
			BeanUtils.copyPropertiesByModel(cdnBucket, baseCdnBucket);
		}
		return cdnBucket;
	}
	
	@SuppressWarnings("unchecked")
    @Override
	public CdnBucket getDeleteByDomain(String domain) {
		CdnBucket cdnBucket = new CdnBucket();
		List<String> list = new ArrayList<String>();
		StringBuffer hql = new StringBuffer("from BaseCdnBucket where domainId = ?");
		list.add(domain);
		List<BaseCdnBucket> baseCdnBucketList = cdnBucketDao.find(hql.toString(), list.toArray());
		if(null != baseCdnBucketList && baseCdnBucketList.size() > 0){
			BaseCdnBucket baseCdnBucket = baseCdnBucketList.get(0);
			BeanUtils.copyPropertiesByModel(cdnBucket, baseCdnBucket);
		}
		return cdnBucket;
	}
	
	@SuppressWarnings("unchecked")
    @Override
	public List<CdnBucket> getListForBackByCusId(String cusId) {
		List<CdnBucket> cdnBucketList = new ArrayList<CdnBucket>();
		String hql = "from BaseCdnBucket where (isOpencdn = '1' or (isOpencdn = '0' and closeTime > ?)) and cusId = ? ";
		Date closeTime = DateUtil.addDay(new Date(), new int[] {0,0,0,-2});
		List<Object> list = new ArrayList<Object>();
		list.add(closeTime);
		list.add(cusId);
		List<BaseCdnBucket> baseCdnBucketList = cdnBucketDao.find(hql, list.toArray());
		if(null != baseCdnBucketList){
			for(BaseCdnBucket baseCdnBucket : baseCdnBucketList){
				CdnBucket cdnBucket = new CdnBucket();
				BeanUtils.copyPropertiesByModel(cdnBucket, baseCdnBucket);
				cdnBucketList.add(cdnBucket);
			}
		}
		return cdnBucketList;
	}
	/**
	 * 
	 * @param cusId
	 * @param start
	 * @param end
	 * 返回选定时间内的纯下载流量
	 * @return
	 */
	@Override
	public double getBacksourceByCusId(String cusId, Date start, Date end) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
		start = DateUtil.stringToDate(sdf.format(start));
		end = DateUtil.stringToDate(sdf.format(end));
		
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		
		Date startTime  = DateUtil.strToDate(sf.format(start));
		Date endTime  = DateUtil.strToDate(sf.format(end));
		
		long backsource = 0;
		//两个时间天数相差超过1天（因每天的计划任务在零点15分执行，因此endTime为0点时，查询不到昨天整天的记录）
		if(endTime.after(DateUtil.addDay(startTime, new int[]{0,0,1}))){
			//计算整天的纯下载流量
			Aggregation aggd = Aggregation.newAggregation(
					Aggregation.match(Criteria.where("owner").is(cusId)),
					Aggregation.match(Criteria.where("timestamp").gte(startTime)),
					Aggregation.match(Criteria.where("timestamp").lt(DateUtil.addDay(endTime, new int[]{0,0,-1}))),
					Aggregation.match(Criteria.where("final_data").exists(false)),//没有新加属性的按照旧下载流量相加
					Aggregation.group().sum("download").as("totalDownLoad")
					);
			AggregationResults<JSONObject> downLoadResult = mongoTemplate.aggregate(aggd, 
					MongoCollectionName.OBS_USED_24H, JSONObject.class);
			List<JSONObject> downLoadList = downLoadResult.getMappedResults();
			if (null != downLoadList && downLoadList.size() > 0) {
				JSONObject json = downLoadList.get(0);
				long data = json.getLongValue("totalDownLoad");
				backsource = backsource + data;
			}
			Aggregation aggback = Aggregation.newAggregation(
					Aggregation.match(Criteria.where("owner").is(cusId)),
					Aggregation.match(Criteria.where("timestamp").gte(startTime)),
					Aggregation.match(Criteria.where("timestamp").lt(DateUtil.addDay(endTime, new int[]{0,0,-1}))),
					Aggregation.match(Criteria.where("final_data").exists(true)),//有新加属性的按照新加属性相加
					Aggregation.group().sum("final_data").as("totalFinalData")
					);
			AggregationResults<JSONObject> backResult = mongoTemplate.aggregate(aggback, 
					MongoCollectionName.OBS_USED_24H, JSONObject.class);
			List<JSONObject> backList = backResult.getMappedResults();
			if (null != backList && !backList.isEmpty()) {
				JSONObject json = backList.get(0);
				long data = json.getLongValue("totalFinalData");
				backsource = backsource + data;
			}
			
			//计算今天的纯下载流量
			Criteria criatira = new Criteria();				//下载流量原始数据
	        criatira.andOperator(Criteria.where("owner").is(cusId),Criteria.where("timestamp").
	        		gt(DateUtil.addDay(endTime, new int[]{0,0,-1})), Criteria.where("timestamp").lte(end));
	        List<JSONObject> jsonList = mongoTemplate.find(new Query(criatira),JSONObject.class,MongoCollectionName.OBS_USED_1H);
	        
			Criteria backCriatira = new Criteria();			//回源流量原始数据
	        backCriatira.andOperator(Criteria.where("cus_id").is(cusId),Criteria.where("timestamp").
	        		gt(DateUtil.addDay(endTime, new int[]{0,0,-1})), Criteria.where("timestamp").lte(end));
	        List<JSONObject> backJsonList = mongoTemplate.find(new Query(backCriatira),JSONObject.class,MongoCollectionName.CDN_BACKSOURCE_1H);
			
	        if(null != jsonList && !jsonList.isEmpty()){
	        	for(int i = 0;i < jsonList.size();i++){
	        		JSONObject obj=jsonList.get(i);
		        	JSONArray categories = obj.getJSONArray("categories");
		        	Date thisTime = obj.getDate("timestamp");
		        	thisTime = DateUtil.dateRemoveSec(thisTime);
		        	String bucketName = obj.getString("bucket");
		        	long oneData = 0;
		        	for(int j=0;j<categories.size();j++){
		        		long bytesSent=categories.getJSONObject(j).getLong("bytes_sent");
		        		oneData+=bytesSent;
		        	}
		        	long oneBacksource=0;
	        		if(null != backJsonList && !backJsonList.isEmpty()){
		        		for(int j = 0;j < backJsonList.size();j++){
		        			JSONObject backJson=backJsonList.get(j);
		        			Date backThisTime = backJson.getDate("timestamp");
		        			backThisTime = DateUtil.dateRemoveSec(backThisTime);
				        	String backBucketName = backJson.getString("bucket_name");
				        	if(thisTime.getTime()==backThisTime.getTime()&&bucketName.equals(backBucketName)){
				        		oneBacksource = backJson.getLongValue("backsource");
				        		break;
				        	}
			        	}
			        }
	        		long diff=(oneData-oneBacksource)>0?oneData-oneBacksource:0;
	        		backsource = backsource + diff;
	        	}
	        }
		}else{	//两个时间天数相差 小于或等于1天
			Criteria criatira = new Criteria();				//下载流量原始数据
	        criatira.andOperator(Criteria.where("owner").is(cusId),Criteria.where("timestamp").gt(start), Criteria.where("timestamp").lte(end));
	        List<JSONObject> jsonList = mongoTemplate.find(new Query(criatira),JSONObject.class,MongoCollectionName.OBS_USED_1H);
	        
			Criteria backCriatira = new Criteria();			//回源流量原始数据
	        backCriatira.andOperator(Criteria.where("cus_id").is(cusId),Criteria.where("timestamp").gt(start), Criteria.where("timestamp").lte(end));
	        List<JSONObject> backJsonList = mongoTemplate.find(new Query(backCriatira),JSONObject.class,MongoCollectionName.CDN_BACKSOURCE_1H);
	        
	        if(null != jsonList && !jsonList.isEmpty()){
	        	for(int i = 0;i < jsonList.size();i++){
	        		JSONObject obj=jsonList.get(i);
		        	JSONArray categories = obj.getJSONArray("categories");
		        	Date thisTime = obj.getDate("timestamp");
		        	thisTime = DateUtil.dateRemoveSec(thisTime);
		        	String bucketName = obj.getString("bucket");
		        	long oneData = 0;
		        	for(int j=0;j<categories.size();j++){
		        		long bytesSent=categories.getJSONObject(j).getLong("bytes_sent");
		        		oneData+=bytesSent;
		        	}
		        	long oneBacksource=0;
	        		if(null != backJsonList && !backJsonList.isEmpty()){
		        		for(int j = 0;j < backJsonList.size();j++){
		        			JSONObject backJson=backJsonList.get(j);
		        			Date backThisTime = backJson.getDate("timestamp");
		        			backThisTime = DateUtil.dateRemoveSec(backThisTime);
				        	String backBucketName = backJson.getString("bucket_name");
				        	if(thisTime.getTime()==backThisTime.getTime()&&bucketName.equals(backBucketName)){
				        		oneBacksource = backJson.getLongValue("backsource");
				        		break;
				        	}
			        	}
			        }
	        		long diff=(oneData-oneBacksource)>0?oneData-oneBacksource:0;
	        		backsource = backsource + diff;
	        	}
	        }
		}
        double result = 0.0d;
        result = (double)backsource/1024/1024/1024;
		return result;
	}

    @Override
    public Map<String, Object> getCdnDetail(String cusId, Date chargeFrom, Date chargeTo) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
        chargeFrom = DateUtil.stringToDate(sdf.format(chargeFrom));
        chargeTo = DateUtil.stringToDate(sdf.format(chargeTo));
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("cus_id").is(cusId)),
                Aggregation.match(Criteria.where("timestamp").gt(chargeFrom)),
                Aggregation.match(Criteria.where("timestamp").lte(chargeTo)),
                Aggregation.group("_id").sum("flow_data").as("cdnDownload").sum("dreqs").as("cdnDreqs").sum("hreqs").as("cdnHreqs")
        );
        AggregationResults<JSONObject> cdnDetail = mongoTemplate.aggregate(aggregation,
                MongoCollectionName.OBS_CDN_1H, JSONObject.class);
        List<JSONObject> cdnDetails = cdnDetail.getMappedResults();
        Map<String, Object> result = new HashMap<>();
        if (null != cdnDetails && !cdnDetails.isEmpty()) {
            long cdnDownload = 0L;
            long cdnDreqs = 0L;
            long cdnHreqs = 0L;
            for(int i=0; i<cdnDetails.size();i++){
                JSONObject json = cdnDetails.get(i);
                cdnDownload += json.getLongValue("cdnDownload");
                cdnDreqs += json.getLongValue("cdnDreqs");
                cdnHreqs += json.getLongValue("cdnHreqs");
            }
            double downloadGB =  cdnDownload/1024/1024/1024;
            log.info("客户"+cusId+"的在"+chargeFrom+"到"+chargeTo+"间的CDN下载流量:"+downloadGB+"GB");
            long dreqsTotal = cdnDreqs;
            log.info("客户"+cusId+"的在"+chargeFrom+"到"+chargeTo+"间的CDN动态请求数:"+dreqsTotal+"次");
            long hreqsTotal = cdnHreqs;
            log.info("客户"+cusId+"的在"+chargeFrom+"到"+chargeTo+"间的CDN-HTTPS请求数:"+hreqsTotal+"次");
            result.put("cdnDownload",cdnDownload);//集合中存储的单位是B，实际返回的时候也是B
            result.put("cdnDreqs",dreqsTotal);//集合中存储的动态请求数单位是次，实际返回的时候也是次
            result.put("cdnHreqs",hreqsTotal );//集合中存储的HTTPS请求的次数是次，实际返回的时候也是次
        }
        return result;
    }
}
