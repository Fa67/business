package com.eayun.obs.ecmcservice.impl;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.accesskey.model.AccessKey;
import com.eayun.accesskey.service.AccessKeyService;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.httpclient.HttpClientFactory;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.HmacSHA1Util;
import com.eayun.common.util.ObsUtil;
import com.eayun.costcenter.service.CostReportService;
import com.eayun.customer.ecmcservice.EcmcCustomerService;
import com.eayun.obs.base.service.ObsBaseService;
import com.eayun.obs.ecmcmodel.EcmcObsEchartsBean;
import com.eayun.obs.ecmcservice.EcmcObsCustomerService;
import com.eayun.obs.ecmcservice.EcmcObsOverviewService;
import com.eayun.obs.model.ObsAccessBean;
import com.eayun.obs.model.ObsBucket;
import com.eayun.obs.model.ObsResultBean;
import com.eayun.obs.model.ObsUsedType;

/**
 * EcmcObsCustomerServiceImpl
 * @Filename: EcmcObsCustomerServiceImpl.java
 * @Description:
 * @Version: 1.1
 * @Author: liyanchao
 * @Email: yanchao.li@eayun.com
 * @History:<br> <li>Date: 2016年4月1日</li>
 * 
 */
@Service
@Transactional
public class EcmcObsCustomerServiceImpl implements EcmcObsCustomerService {
	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private ObsBaseService obsBaseService;
	@Autowired
	private EcmcObsOverviewService ecmcObsOverviewService;
	@Autowired
	private EcmcCustomerService ecmcCustomerService;
	@Autowired
	private AccessKeyService accessKeyService;
	@Autowired
	private CostReportService costReportService;
	

	private long kb = 1024;
	private long mb = kb * 1024;
	private long gb = mb * 1024;
	private long tb = gb * 1024;
	
	/**
	 * 根据客户Id得到所有属于他的bucket
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public List<ObsBucket> getBucketsByUserId(String userId) throws Exception {
		List<ObsBucket> result = new ArrayList<ObsBucket>();
		Set<String> buckets = jedisUtil.keys(RedisKey.BUCKET_START  + userId + ":" + "*");
		for (String buckect : buckets) {
			String[] args = buckect.split(":");
			String bucketName = args[args.length - 1];
			ObsBucket newBucket = new ObsBucket();
			newBucket.setBucketName(bucketName);
			newBucket.setOwner(userId);
			newBucket.setBucketId(bucketName);
			result.add(newBucket);
		}
		return result;
	}

	/**
	 * 获取指定客户本月使用量
	 * 
	 * @param cusId
	 * @return
	 */
	public ObsUsedType getObsInMonthUsed(String cusId) throws Exception {
		Date today = new Date();
		SimpleDateFormat formatMonth = new SimpleDateFormat("yyyy-MM");
		SimpleDateFormat formatHour = new SimpleDateFormat("yyyy-MM-dd 01:00:00");
		String beginStr = formatMonth.format(today);// 格式化到月份：2016-04
		Date begin = formatMonth.parse(beginStr);// 格式化当月的date:Fri Apr 01 00:00:00 CST 2016
		begin=DateUtil.addDay(begin, new int[]{0,0,0,1});
		ObsUsedType obsType = new ObsUsedType();

		long request = 0l;
		double obsStorageUsed = 0;
		long downLoad = 0l;
		// mongo求  本月使用量:存储容量
		Aggregation aggStorage = Aggregation.newAggregation(
				Aggregation.match(Criteria.where("owner").is(cusId)),
				Aggregation.match(Criteria.where("timestamp").gte(begin)),
				Aggregation.match(Criteria.where("timestamp").lt(today)),
				Aggregation.group().sum("usage.size_kb_actual").as("totalStorage"));
		AggregationResults<JSONObject> storageResult = mongoTemplate.aggregate(aggStorage, MongoCollectionName.OBS_STORAGE_1H, JSONObject.class);
		List<JSONObject> storageList = storageResult.getMappedResults();
		if (null != storageList && storageList.size() > 0) {
			Map<String, Object> map = storageList.get(0);
			obsStorageUsed =Double.parseDouble(map.get("totalStorage").toString());
		}
		AccessKey ak=accessKeyService.getDefaultAK(cusId);
		Date openObsTime=new Date();
		if(ak!=null){
			openObsTime=ak.getCreateDate();
		}
		if(openObsTime.getTime()>begin.getTime()){
			begin=openObsTime;
		}
		int hour = (int) ((today.getTime() - begin.getTime()) / (60 * 60 * 1000));// 得到相差小时数
		if(hour<=0){
			hour=1;
		}
		obsStorageUsed=obsStorageUsed/hour;
		
		// mongo求sum下载量
		Aggregation aggDownLoad = Aggregation.newAggregation(
				Aggregation.match(Criteria.where("owner").is(cusId)),
				Aggregation.match(Criteria.where("timestamp").gte(begin)),
				Aggregation.match(Criteria.where("timestamp").lt(today)),
				Aggregation.match(Criteria.where("final_data").exists(false)),//没有新加属性的按照旧下载流量相加
				Aggregation.group().sum("download").as("totalDownLoad"));
		AggregationResults<JSONObject> downLoadResult = mongoTemplate.aggregate(aggDownLoad, MongoCollectionName.OBS_USED_24H, JSONObject.class);
		List<JSONObject> downLoadList = downLoadResult.getMappedResults();
		if (null != downLoadList && downLoadList.size() > 0) {
			Map<String, Object> map = downLoadList.get(0);
			downLoad = (long) map.get("totalDownLoad");
		}
		//计算扣除掉回源流量后的下载流量
		Aggregation aggback = Aggregation.newAggregation(
				Aggregation.match(Criteria.where("owner").is(cusId)),
				Aggregation.match(Criteria.where("timestamp").gte(begin)),
				Aggregation.match(Criteria.where("timestamp").lt(today)),
				Aggregation.match(Criteria.where("final_data").exists(true)),//有新加属性的按照新加属性相加
				Aggregation.group().sum("final_data").as("totalFinalData")
				);
		AggregationResults<JSONObject> backResult = mongoTemplate.aggregate(aggback, 
				MongoCollectionName.OBS_USED_24H, JSONObject.class);
		List<JSONObject> backList = backResult.getMappedResults();
		if (null != backList && !backList.isEmpty()) {
			JSONObject json = backList.get(0);
			long data = json.getLongValue("totalFinalData");
			downLoad = downLoad + data;
		}
		//获取当日下载量obs
		String todayOneStr=formatHour.format(today);
		Date todayOne=formatHour.parse(todayOneStr);
		 Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("owner").is(cusId),Criteria.where("timestamp").gte(todayOne), Criteria.where("timestamp").lt(today));
        List<JSONObject> jsonList = mongoTemplate.find(new Query(criatira),JSONObject.class,MongoCollectionName.OBS_USED_1H);
        Criteria backCriatira = new Criteria();
        backCriatira.andOperator(Criteria.where("cus_id").is(cusId),Criteria.where("timestamp").gte(todayOne), Criteria.where("timestamp").lt(today));
        List<JSONObject> backJsonList = mongoTemplate.find(new Query(backCriatira),JSONObject.class,MongoCollectionName.CDN_BACKSOURCE_1H);
        long finalData=0;
        long countRequest=0;
        for(int i=0;i<jsonList.size();i++){
        	JSONObject obj=jsonList.get(i);
        	JSONArray categories = obj.getJSONArray("categories");
        	Date thisTime = obj.getDate("timestamp");
        	thisTime = DateUtil.dateRemoveSec(thisTime);
        	String bucketName = obj.getString("bucket");
        	
        	long oneData = 0;
        	for(int j=0;j<categories.size();j++){
        		long bytesSent=categories.getJSONObject(j).getLong("bytes_sent");
        		long ops=categories.getJSONObject(j).getLong("ops");
        		countRequest+=ops;
        		oneData+=bytesSent;
        		
        	}
        	long oneBacksource=0;
        	if(null!=backJsonList && !backJsonList.isEmpty()){
        		for(int j=0;j<backJsonList.size();j++){
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
        	finalData = finalData+diff;
        }
        downLoad+=finalData;
		// mongo求sum请求次数
		Aggregation aggRequest = Aggregation.newAggregation(
				Aggregation.match(Criteria.where("owner").is(cusId)),
				Aggregation.match(Criteria.where("timestamp").gte(begin)),
				Aggregation.match(Criteria.where("timestamp").lt(today)),
				Aggregation.group().sum("countRequest").as("totalRequest"));
		AggregationResults<JSONObject> requestResult = mongoTemplate.aggregate(aggRequest, MongoCollectionName.OBS_USED_24H, JSONObject.class);
		List<JSONObject> requestList = requestResult.getMappedResults();
		if (null != requestList && requestList.size() > 0) {
			Map<String, Object> map = requestList.get(0);
			request = (long) map.get("totalRequest");
		}
		request+=countRequest;
		//转换单位：存储量、流量、请求次数
		String resultStorage = this.convertStorageUnit(obsStorageUsed);
		String loadFlow = this.convertFlowLoadUnit(downLoad);
		String requestCount = this.convertRequestCountUnit(request);
		obsType.setLoadDown(loadFlow);
		obsType.setRequestCount(requestCount);
		obsType.setUsedStorage(resultStorage);
		BigDecimal cost=costReportService.getCostForObs(cusId, begin, today, "obs");
		String costStr=formatCost(cost);
		obsType.setCost(costStr+"元");
		obsType.setCountRequest(request);
		obsType.setStorageUsed(obsStorageUsed);
		obsType.setDownload(downLoad);
		return obsType;

	}
	/**
	 * 格式化金额
	 * @param cost
	 * @return
	 */
	private String formatCost(BigDecimal cost) {
		DecimalFormat df = new DecimalFormat("0.00");
		String result = df.format(cost.doubleValue());
		return result;
	}
	
	/**
	 * 换算单位：存储量（GB、MB）
	 **/
	 private String convertStorageUnit(double storageAgo){
		 DecimalFormat df1 = new DecimalFormat("0.00");// 格式化小数
		 String storage = "";
		// 单位 （GB）:存储量的
		if (storageAgo >= gb) {//因为storage本身单位为kb 故有此除法逻辑
			double storageCount = storageAgo / gb;
			String storageUse = df1.format(storageCount);
			storage = storageUse + "TB";
		} else if (storageAgo >= mb && storageAgo < gb) {
			double storageCount = storageAgo / mb;
			String storageUse = df1.format(storageCount);
			storage = storageUse + "GB";
		} else if (storageAgo >= kb && storageAgo < mb) {
			double storageCount = storageAgo / kb;
			String storageUse = df1.format(storageCount);
			storage = storageUse + "MB";
		} else {
			String storageUse = df1.format(storageAgo);
			storage = storageUse + "KB";
		}
		return storage;
	 }
	 /**
	 * 换算单位：下载流量（GB、MB）
	 **/
	 private String convertFlowLoadUnit(long flowAgo){
		 DecimalFormat df1 = new DecimalFormat("0.00");// 格式化小数
		 String flowLoad = "";
		// 单位 （GB）:下载流量
		if (flowAgo >= tb) {
			float loadsize = (float) flowAgo / tb;
			String load = df1.format(loadsize);
			flowLoad = load+"TB";
		} else if (flowAgo >= gb && flowAgo < tb) {
			float loadsize = (float) flowAgo / gb;
			String load = df1.format(loadsize);
			flowLoad = load+"GB";
		} else if (flowAgo >= mb && flowAgo < gb) {
			float loadsize = (float) flowAgo / mb;
			String load = df1.format(loadsize);
			flowLoad = load+"MB";
		} else if (flowAgo >= kb && flowAgo < mb) {
			float loadsize = (float) flowAgo / kb;
			String load = df1.format(loadsize);
			flowLoad = load+"KB";
		} else {
			flowLoad = flowAgo+"B";
		}
		return flowLoad;
	 }
	
	
	/**
	 * 换算单位：请求次数（百次、万次）
	 */
	private String convertRequestCountUnit(long requestCountAgo) {
		DecimalFormat df1 = new DecimalFormat("0.00");// 格式化小数
		String request = "";
		// 单位 （万次）:请求次数
		if (requestCountAgo >= 10000) {
			float requestCount = (float) requestCountAgo / 10000;
			String requestNum = df1.format(requestCount);
			request = requestNum + "万次";
		} else {
			float requestCount = (float) requestCountAgo / 100;
			String requestNum = df1.format(requestCount);
			request = requestNum + "百次";
		}
		return request;
	}
	/**
	 * 获取客户历史账单,分页
	 * @param cusId
	 * @return
	 */
	public Page getObsHistoryResources(Page page, QueryMap queryMap,String cusId) throws Exception{
		List<ObsUsedType> obsList = new ArrayList<ObsUsedType>();
		List<ObsUsedType> resultList = new ArrayList<ObsUsedType>();
		SimpleDateFormat formatsMonth = new SimpleDateFormat("yyyy-MM");
		Criteria criatira = new Criteria();
		Sort sort = new Sort(Direction.DESC, "timestamp");
		criatira.andOperator(Criteria.where("owner").is(cusId));
		List<JSONObject> usedlist = mongoTemplate.find(new Query(criatira).with(sort),JSONObject.class, MongoCollectionName.OBS_USED_1MONTH);
		List<JSONObject> storageUsed = mongoTemplate.find( new Query(criatira).with(sort), JSONObject.class, MongoCollectionName.OBS_STORAGE_1MONTH);
		// usedlist有数据且storageUsed无数据
		if (null != usedlist && usedlist.size() > 0 && (null == storageUsed || storageUsed.size() <= 0)) {
			for (int i = 0; i < usedlist.size(); i++) {
				JSONObject obsUsed = usedlist.get(i);
				ObsUsedType type = new ObsUsedType();
				long coutRequest = obsUsed.getLong("countRequest");
				long byteSet = null!=obsUsed.getLong("final_data")?
						obsUsed.getLong("final_data"):obsUsed.getLong("download");//根据新加的属性剔除回源
				Date timestamp = obsUsed.getDate("timestamp");
				String time = formatsMonth.format(timestamp);
				Date monthBegin=formatsMonth.parse(time);
				Date monthEnd=DateUtil.addDay(monthBegin, new int[]{0,1});
				BigDecimal cost = costReportService.getCostForObs(cusId, DateUtil.addDay(monthBegin, new int[]{0,0,0,1}), monthEnd, "obs");
				String costStr=formatCost(cost);
				//转换单位：存储量、流量、请求次数
				String loadFlow  = this.convertFlowLoadUnit(byteSet);
				String requestCount = this.convertRequestCountUnit(coutRequest);
				type.setLoadDown(loadFlow);
				type.setRequestCount(requestCount);
				type.setCost(costStr+"元");
				type.setUsedStorage("0KB");
				type.setCountRequest(coutRequest);
				type.setDownload(byteSet);
				type.setStorageUsed(0);
				type.setTimeDis(time);
				type.setTime(timestamp);
				obsList.add(type);
			}
			// usedlist无数据且storageUsed有数据
		} else if (null != storageUsed && storageUsed.size() > 0 && (null == usedlist || usedlist.size() <= 0)) {
			for (int i = 0; i < storageUsed.size(); i++) {
				JSONObject storUsed = storageUsed.get(i);
				ObsUsedType obsType = new ObsUsedType();
				Date timestamp = storUsed.getDate("timestamp");
				String time = formatsMonth.format(timestamp);
				Date monthBegin=formatsMonth.parse(time);
				Date monthEnd=DateUtil.addDay(monthBegin, new int[]{0,1});
				BigDecimal cost = costReportService.getCostForObs(cusId, DateUtil.addDay(monthBegin, new int[]{0,0,0,1}), monthEnd, "obs");
				String costStr=formatCost(cost);
				double storage = storUsed.getDouble("storageUsed");
				//转换单位：存储量、流量、请求次数
				String resultStorage  = this.convertStorageUnit(storage);
				obsType.setUsedStorage(resultStorage);
//				//转换单位：存储量、流量、请求次数
				obsType.setCost(costStr+"元");
				obsType.setRequestCount("0百次");
				obsType.setLoadDown("0B");
				obsType.setTime(timestamp);
				obsType.setTimeDis(time);
				obsType.setCountRequest(0);
				obsType.setDownload(0);
				obsType.setStorageUsed(storage);
				obsList.add(obsType);
			}
			// usedlist有数据且storageUsed有数据
		} else if (null != storageUsed && storageUsed.size() > 0 && null != usedlist && usedlist.size() > 0) {
			for (int i = 0; i < usedlist.size(); i++) {
				ObsUsedType obsType = new ObsUsedType();
				JSONObject obsUsed = usedlist.get(i);
				Date timestamp = obsUsed.getDate("timestamp");
				String time = formatsMonth.format(timestamp);
				Date monthBegin=formatsMonth.parse(time);
				Date monthEnd=DateUtil.addDay(monthBegin, new int[]{0,1});
				BigDecimal cost = costReportService.getCostForObs(cusId, DateUtil.addDay(monthBegin, new int[]{0,0,0,1}), monthEnd, "obs");
				String costStr=formatCost(cost);
				long coutRequest = obsUsed.getLong("countRequest");
				long byteSet = null!=obsUsed.getLong("final_data")?
						obsUsed.getLong("final_data"):obsUsed.getLong("download");//根据新加的属性剔除回源
				obsType.setTimeDis(time);
				obsType.setTime(timestamp);
				obsType.setCountRequest(coutRequest);
				obsType.setDownload(byteSet);
				obsType.setCost(costStr+"元");
				//转换单位：存储量、流量、请求次数
				String requestCount = this.convertRequestCountUnit(coutRequest);
				String loadFlow = this.convertFlowLoadUnit(byteSet);
				obsType.setLoadDown(loadFlow);
				obsType.setRequestCount(requestCount);
				
				for (int j = 0; j < storageUsed.size(); j++) {
					JSONObject obsStorageUsed = storageUsed.get(j);
					Date timesta = obsStorageUsed.getDate("timestamp");
					String time1 = formatsMonth.format(timesta);
					if (time.equals(time1)) {
						double storage = obsStorageUsed.getDouble("storageUsed");
						//转换单位：存储量、流量、请求次数
						String resultStorage  =this.convertStorageUnit(storage);
						obsType.setUsedStorage(resultStorage);
						obsType.setStorageUsed(storage);
						break;
					} else {
						obsType.setStorageUsed(0);
						obsType.setUsedStorage("0KB");
					}
				}
				obsList.add(obsType);
			}
		}

		int startIndex = (queryMap.getPageNum() - 1)*queryMap.getCURRENT_ROWS_SIZE();
		int end = (startIndex + queryMap.getCURRENT_ROWS_SIZE()) - 1;
		if (obsList.size() - 1 < end) {
			end = obsList.size() - 1;
		}
		for (int i = 0; i < obsList.size(); i++) {
			if (startIndex <= i && i <= end) {
				resultList.add(obsList.get(i));
			}
		}
		// 构造分页
		page = new Page(startIndex, obsList.size(), 10, resultList);
		return page;
	}

	/**
	 * 调用obs底层，获取用户配额
	 * @param customer
	 * @return
	 * @throws Exception
	 */
	public ObsUsedType getQuota(String customer) throws Exception {
		String accessKey = ObsUtil.getAdminAccessKey();
		String secretKey = ObsUtil.getAdminSecretKey();
		String date = DateUtil.getRFC2822Date(new Date());
		String url = "/admin/user";
		String signature = ObsUtil.getSignature("GET", "", "", date, "", url);
		String hmacSHA1 = HmacSHA1Util.getEncrypt(signature, secretKey);

		String host = ObsUtil.getEayunObsHost();
		String header = ObsUtil.getRequestHeader();

		ObsAccessBean obsBean = new ObsAccessBean();
		obsBean.setHost(host);
		obsBean.setUrl(header + host + url + "?uid=" + customer+ "&quota&quota-type=user");
		obsBean.setHmacSHA1(hmacSHA1);
		obsBean.setAccessKey(accessKey);
		obsBean.setRFC2822Date(date);
		obsBean.setHttp("http://".equals(header));
		ObsResultBean resultBean = obsBaseService.get(obsBean);
		ObsUsedType obsUsedType = new ObsUsedType();
		if ("200".equals(resultBean.getCode())) {
			String resData = resultBean.getResData();
			JSONObject resJson = JSONObject.parseObject(resData);
			String storageStr = resJson.getString("max_size_kb");
			double storage = -1;
			if (storageStr != null && storageStr.length() > 0) {
				storage = Double.parseDouble(storageStr);
				storage = storage / 1024 / 1024;
				storage = formatToseparaDouble(storage);
				obsUsedType.setStorageUsed(storage);
			}
		}
		return obsUsedType;
	}

	/**
	 * 将数字保留2位点分隔
	 * @param data
	 * @return
	 */
	public double formatToseparaDouble(Double data) {
		data=(double)(Math.round(data*100)/100.0); 
		return Double.parseDouble(data.toString());
	}

	/**
	 * 设置用户配额
	 * @param customer
	 * @param storage
	 * @param flow
	 * @param requestCount
	 * @return
	 * @throws Exception
	 */
	public String setQuota(String cusId, String storage, String flow,String requestCount) throws Exception {
		Integer stro = 0;
		if (null != storage && storage.length() > 0) {
			double sto = Double.parseDouble(storage);
			sto = sto * 1024 * 1024;
			stro = (int) (Math.floor(sto));
		}
		if (stro == 0) {
			stro = -1;
		}
		storage = stro.toString();
		String accessKey = ObsUtil.getAdminAccessKey();
		String secretKey = ObsUtil.getAdminSecretKey();
		String date = DateUtil.getRFC2822Date(new Date());
		String url = "/admin/user";

		String signature = ObsUtil.getSignature("PUT", "", "application/json",date, "", url);
		String hmacSHA1 = HmacSHA1Util.getEncrypt(signature, secretKey);
		HttpClient httpclient = HttpClientFactory.getHttpClient("http://".equals(ObsUtil.getRequestHeader()));
		String host = ObsUtil.getEayunObsHost();
		String header = ObsUtil.getRequestHeader();
		HttpPut put = new HttpPut(header + host + url + "?uid=" + cusId+ "&quota&quota-type=user");
		put.addHeader("Authorization", "AWS " + accessKey + ":" + hmacSHA1);
		put.addHeader("Host", host);
		put.addHeader("Date", date);
		put.addHeader("Content-Type", "application/json");
		JSONObject json = new JSONObject();
		json.put("quota-scope", "user");
		json.put("enabled", true);
		json.put("max_objects", -1);
		json.put("max_size_kb", storage);
		StringEntity se = new StringEntity(json.toString(), ContentType.create("application/json", "utf-8"));
		put.setEntity(se);

		HttpResponse res = httpclient.execute(put);
		Integer code = res.getStatusLine().getStatusCode();
		JSONObject resJson = new JSONObject();
		if ("200".equals(code.toString())) {// 更新成功
			resJson.put("code", "000000");
		} else {// 更新失败
			resJson.put("code", "010120");
		}
		return resJson.toString();
	}

	/**
	 * 对象存储客户详情--资源详情--折线图
	 */
	@Override
	public EcmcObsEchartsBean getObsUsedView(String bucketName, String cusId,
			String type, Date startTime, Date endTime) throws Exception {
		EcmcObsEchartsBean echartsBean = new EcmcObsEchartsBean();
		List<BigDecimal> yDataList = new ArrayList<BigDecimal>();
		// 步骤1
		if ("storage".equals(type)) {
			echartsBean = this.getStorage(bucketName, cusId, startTime, endTime);
		} else if ("loadFlow".equals(type)) {
			echartsBean = this.getLoadFlow(cusId, bucketName, startTime,endTime);
		} else if ("request".equals(type)) {
			echartsBean = this.getRequestTimes(cusId, bucketName, startTime,endTime);
		}
		
		// 步骤2：求出对应的各项阈值  设置总存储量 需要获取
		ObsUsedType storageQuota = new ObsUsedType();
		storageQuota = this.getQuota(cusId);
		// 步骤2：根据EcmcObsEchartsBean 求出maxYData、minYData
		if (null != echartsBean) {
			for (BigDecimal decimal : echartsBean.getyData()) {
				yDataList.add(decimal);
			}
			
			Collections.sort(yDataList, new Comparator<BigDecimal>() {
				@Override
				public int compare(BigDecimal o1, BigDecimal o2) {
					return o1.compareTo(o2);
				}
			});
			BigDecimal minData = yDataList.get(0);
			BigDecimal maxData = yDataList.get(yDataList.size() - 1);
			echartsBean.setOriginalDataMax(maxData.toString());
			minData = minData.multiply(BigDecimal.valueOf(0.8)).setScale(2,
					BigDecimal.ROUND_HALF_UP);
			maxData = maxData.multiply(BigDecimal.valueOf(1.2)).setScale(2,
					BigDecimal.ROUND_HALF_UP);
			echartsBean.setType(type);
			echartsBean.setyDataMin(minData.toString());
			echartsBean.setyDataMax(maxData.toString());
			echartsBean.setStorageQuota(storageQuota.getStorageUsed()+"");
		}
		return echartsBean;
	}

	// 客户详情：资源详情：获取存储量
	@SuppressWarnings("rawtypes")
    private EcmcObsEchartsBean getStorage(String bucketName, String cusId, Date start, Date end) throws Exception {
		if (null == bucketName || "".equals(bucketName) || null == cusId || "".equals(cusId)) {
			return null;
		}
		DateFormat sdf = new SimpleDateFormat("MM-dd HH:mm"); 
		DateFormat sdfMonthDay = new SimpleDateFormat("MM-dd"); 
		
		List<ObsUsedType> echartsList = new ArrayList<ObsUsedType>();
		EcmcObsEchartsBean echartsBean = new EcmcObsEchartsBean();
		int value = (int) ((end.getTime() - start.getTime()) / (24 * 60 * 60 * 1000));// 得到相差天数

		if (value <= 4) {
			int hourse = (value + 1) * 24 / 6;
			Date beginTime = start;
			Date endTime = DateUtil.addDay(beginTime, new int[] { 0, 0, 0,+hourse });
			for (int i = 0; i < 6; i++) {
				// 查mongo 需加1h
				Date timeBegin = DateUtil.addDay(beginTime, new int[] { 0, 0,0, +1 });
				Date timeEnd = DateUtil.addDay(endTime,new int[] { 0, 0, 0, +1 });
				/*** 重写该算法开始***/
				BigDecimal count=new BigDecimal(0);
				Sort sort = new Sort(Direction.DESC, "timestamp");
				Criteria criatira = new Criteria();
				criatira.andOperator(
						Criteria.where("owner").is(cusId),
						Criteria.where("bucket").is(bucketName),
						Criteria.where("timestamp").gte(timeBegin),
						Criteria.where("timestamp").lt(timeEnd)
						);
				List<JSONObject> storageUsed = mongoTemplate.find(new Query(criatira).with(sort), JSONObject.class, MongoCollectionName.OBS_STORAGE_1H);
				List<ObsUsedType> list=new ArrayList<ObsUsedType>();
				for (JSONObject json : storageUsed) {
					Map map = (HashMap) json.get("usage");
					if (map.size() > 0) {
						// 已用存储量(现在)
						long storage= Long.parseLong(map.get("size_kb_actual").toString());
						Date daet=(Date)json.get("timestamp");
						ObsUsedType out=new ObsUsedType();
						out.setStorageUsed(storage);
						out.setTime(daet);
						list.add(out);
					}
				}
				if (list != null && list.size()>0) {
					for(ObsUsedType out : list){
						count=count.add(new BigDecimal(out.getStorageUsed()));
					}
					count=count.divide(new BigDecimal(hourse * 1024), 2, BigDecimal.ROUND_HALF_UP);
				}
				/*** 重写该算法结束***/
				BigDecimal yData = new BigDecimal(0);
				yData = count;
				ObsUsedType used = new ObsUsedType();
				used.setEcmcStorageUsed(yData);
				String dateStr = "";
				dateStr = sdf.format(endTime);
				used.setTimeDis(dateStr);
				echartsList.add(used);

				beginTime = endTime;
				endTime = DateUtil.addDay(endTime,new int[] { 0, 0, 0, +hourse });
			}
		} else {// 获取整天的数据

			Date beginTime = start;
			Date endTime = DateUtil.addDay(beginTime, new int[] { 0, 0, 1 });// TODO这个开始、结束时间需要把时分秒清零
			for (int i = 0; i <= value; i++) {
				Date now = new Date();
				String bucketStart = jedisUtil.get(RedisKey.BUCKET_START  + cusId+ ":" + bucketName);
				String bucketEnd = jedisUtil.get(RedisKey.BUCKET_END + cusId + ":"+ bucketName);
				Date startBucket = DateUtil.stringToDate(bucketStart);
				Date endBucket = DateUtil.stringToDate(bucketEnd);

				if (formatDate(beginTime).equals(formatDate(startBucket)) || (endBucket.getTime() - endTime.getTime()) < 0) {
					double storage = 0;
					Criteria criatira = new Criteria();
					Sort sort = new Sort(Direction.DESC, "timestamp");
					criatira.andOperator(
							Criteria.where("owner").is(cusId),
							Criteria.where("bucket").is(bucketName),
							Criteria.where("timestamp").gte(DateUtil.addDay(beginTime, new int[] { 0,0, 0, 1 })),
							Criteria.where("timestamp").lt(DateUtil.addDay(endTime, new int[] { 0, 0,0, 1 })));
					List<JSONObject> storageUsed = mongoTemplate.find(new Query(criatira).with(sort), JSONObject.class,MongoCollectionName.OBS_STORAGE_1H);
					for (JSONObject json : storageUsed) {
						Map map = (HashMap) json.get("usage");
						if (map.size() > 0) {
							// 已用存储量(现在)
							long usedStorage = Long.parseLong(map.get("size_kb_actual").toString());
							storage += usedStorage;
						}
					}
					
					if (formatDate(now).equals(formatDate(beginTime))) {
						long hours = (now.getTime() - beginTime.getTime()) / 1000 / 60 / 60;
						storage = storage / hours;
					} else {
						storage = storage / 24;
					}
					BigDecimal yData = new BigDecimal(storage);
					yData = yData.divide(new BigDecimal(1024), 2,BigDecimal.ROUND_HALF_UP);
					ObsUsedType used = new ObsUsedType();
					used.setEcmcStorageUsed(yData);
					String dateStr = "";
					dateStr = sdfMonthDay.format(beginTime);
					used.setTimeDis(dateStr);
					echartsList.add(used);
				} else {
					Aggregation agg = Aggregation.newAggregation(
							Aggregation.match(Criteria.where("owner").is(cusId)),
							Aggregation.match(Criteria.where("bucket").is(bucketName)),
							Aggregation.match(Criteria.where("timestamp").gte(DateUtil.addDay(beginTime, new int[] { 0, 0, 0,1 }))),
							Aggregation.match(Criteria.where("timestamp").lt(DateUtil.addDay(endTime,new int[] { 0, 0, 0, 1 }))),
							Aggregation.group("bucket").avg("usage.size_kb_actual").as("total"));
					AggregationResults<JSONObject> totalresult = mongoTemplate.aggregate(agg, MongoCollectionName.OBS_STORAGE_1H,JSONObject.class);
					List<JSONObject> totallist = totalresult.getMappedResults();
					BigDecimal yData = new BigDecimal(0);
					if (null != totallist && totallist.size() > 0) {
						Map<String, Object> map = totallist.get(0);
						String total = map.get("total").toString();
						yData = new BigDecimal(total);
						yData = yData.divide(new BigDecimal(1024), 2,BigDecimal.ROUND_HALF_UP);
					}
					ObsUsedType used = new ObsUsedType();
					used.setEcmcStorageUsed(yData);
					String dateStr = "";
					dateStr = sdfMonthDay.format(beginTime);
					used.setTimeDis(dateStr);
					echartsList.add(used);
				}

				beginTime = endTime;
				endTime = DateUtil.addDay(endTime, new int[] { 0, 0, +1 });
			}

		}
		List<BigDecimal> yData = new ArrayList<BigDecimal>();
		List<String> xTime = new ArrayList<String>();

		for (ObsUsedType used : echartsList) {
			yData.add(used.getEcmcStorageUsed());
			xTime.add(used.getTimeDis());
		}
		echartsBean.setyData(yData);
		echartsBean.setxTime(xTime);
		return echartsBean;
	}

	// 客户详情：资源详情：获取下载流量
	@SuppressWarnings({ "rawtypes", "unchecked" })
    private EcmcObsEchartsBean getLoadFlow(String cusId, String bucketName, Date start, Date end) {
		DateFormat sdf = new SimpleDateFormat("MM-dd HH:mm"); 
		DateFormat sdfMonthDay = new SimpleDateFormat("MM-dd"); 
		List<ObsUsedType> echartsList = new ArrayList<ObsUsedType>();
		EcmcObsEchartsBean echartsBean = new EcmcObsEchartsBean();
		int value = (int) ((end.getTime() - start.getTime()) / (24 * 60 * 60 * 1000));// 得到相差天数
		List<BigDecimal> yData = new ArrayList<BigDecimal>();
		List<String> xTime = new ArrayList<String>();
		if (value <= 4) {
			int hourse = (value + 1) * 24 / 6;
			Date beginTime = start;
			Date endTime = DateUtil.addDay(beginTime, new int[] { 0, 0, 0,+hourse });
			for (int i = 0; i < 6; i++) {
				Criteria criatira = new Criteria();
				criatira.andOperator(
						Criteria.where("owner").is(cusId),
						Criteria.where("bucket").is(bucketName),
						Criteria.where("timestamp").gte(DateUtil.addDay(beginTime, new int[] { 0, 0, 0,+1 })),
						Criteria.where("timestamp").lt(DateUtil.addDay(endTime, new int[] { 0, 0, 0,+1 })));
				List<JSONObject> jsonList = mongoTemplate.find(new Query(criatira), JSONObject.class, MongoCollectionName.OBS_USED_1H);
				
				/**从下载流量中扣除回源流量*/
				Criteria backCriatira = new Criteria();
		        backCriatira.andOperator(Criteria.where("cus_id").is(cusId),Criteria.where("bucket_name").is(bucketName),
		        		Criteria.where("timestamp").gte(DateUtil.addDay(beginTime, new int[] { 0, 0, 0,+1 })),
		        		Criteria.where("timestamp").lt(DateUtil.addDay(endTime, new int[] { 0, 0, 0,+1 })));
		        List<JSONObject> backJsonList = mongoTemplate.find(new Query(backCriatira),JSONObject.class,MongoCollectionName.CDN_BACKSOURCE_1H);
		        
				long data = 0l;
				for (JSONObject json : jsonList) {
					long flow = 0l;
					Date thisTime = json.getDate("timestamp");
		        	thisTime = DateUtil.dateRemoveSec(thisTime);
		        	
					List<HashMap> list2 = (ArrayList<HashMap>) json.get("categories");
					for (HashMap map : list2) {
						// 下载流量
						flow += Long.parseLong(map.get("bytes_sent").toString());
					}
					long oneBacksource=0;
		        	if(null!=backJsonList && !backJsonList.isEmpty()){
		        		for(int j=0;j<backJsonList.size();j++){
		        			JSONObject backJson=backJsonList.get(j);
		        			Date backThisTime = backJson.getDate("timestamp");
		        			backThisTime = DateUtil.dateRemoveSec(backThisTime);
				        	if(thisTime.getTime()==backThisTime.getTime()){
				        		oneBacksource = backJson.getLongValue("backsource");
				        		break;
				        	}
		        		}
		        		flow = (flow > oneBacksource)?(flow - oneBacksource):0;
		        	}
					data = data + flow;
				}
				BigDecimal bigDecimal = new BigDecimal(data);
				//将下载流量字节转换为MB
				bigDecimal = bigDecimal.divide(new BigDecimal(1024 * 1024), 2,BigDecimal.ROUND_HALF_UP);
				ObsUsedType used = new ObsUsedType();
				used.setEcmcDownLoad(bigDecimal);
				String dateStr = "";
				dateStr = sdf.format(endTime);
				used.setTimeDis(dateStr);
				echartsList.add(used);

				beginTime = endTime;
				endTime = DateUtil.addDay(endTime,
						new int[] { 0, 0, 0, +hourse });
			}
		} else {
			Date beginTime = start;
			Date endTime = DateUtil.addDay(beginTime, new int[] { 0, 0, +1 });
			for (int i = 0; i <= value; i++) {
				Criteria criatira = new Criteria();
				Sort sort = new Sort(Direction.DESC, "timestamp");
				criatira.andOperator(Criteria.where("owner").is(cusId),
						Criteria.where("bucket").is(bucketName), Criteria
								.where("timestamp").gte(beginTime),
						Criteria.where("timestamp").lt(endTime));
				List<JSONObject> usedList = mongoTemplate.find(new Query(
						criatira).with(sort), JSONObject.class, MongoCollectionName.OBS_USED_1H);
				List<ObsUsedType> list = new ArrayList<ObsUsedType>();
				
				/**从下载流量中扣除回源流量*/
				Criteria backCriatira = new Criteria();
		        backCriatira.andOperator(Criteria.where("cus_id").is(cusId),
		        		Criteria.where("bucket_name").is(bucketName),
		        		Criteria.where("timestamp").gte(beginTime),
		        		Criteria.where("timestamp").lt(endTime));
		        List<JSONObject> backJsonList = mongoTemplate.find(new Query(backCriatira),
		        		JSONObject.class,MongoCollectionName.CDN_BACKSOURCE_1H);
				
				for (JSONObject json : usedList) {
					long flow = 0;
					Date thisTime = json.getDate("timestamp");
		        	thisTime = DateUtil.dateRemoveSec(thisTime);
					List<HashMap> list2 = (ArrayList<HashMap>) json.get("categories");
					for (HashMap map : list2) {
						flow += Long.parseLong(map.get("bytes_sent").toString());
					}
					long oneBacksource=0;
		        	if(null!=backJsonList && !backJsonList.isEmpty()){
		        		for(int j=0;j<backJsonList.size();j++){
		        			JSONObject backJson=backJsonList.get(j);
		        			Date backThisTime = backJson.getDate("timestamp");
		        			backThisTime = DateUtil.dateRemoveSec(backThisTime);
				        	if(thisTime.getTime()==backThisTime.getTime()){
				        		oneBacksource = backJson.getLongValue("backsource");
				        		break;
				        	}
		        		}
		        		flow = (flow > oneBacksource)?(flow - oneBacksource):0;
		        	}
					
					ObsUsedType out = new ObsUsedType();
					out.setDownload(flow);
					list.add(out);
				}
				long load = 0l;
				for (ObsUsedType loadDown : list) {
					load = load + loadDown.getDownload();
				}
				ObsUsedType used = new ObsUsedType();
				//将下载流量字节转换为MB
				used.setEcmcDownLoad(new BigDecimal(load).divide(new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP));
				String dateStr = "";
				dateStr = sdfMonthDay.format(beginTime);
				used.setTimeDis(dateStr);
				echartsList.add(used);

				beginTime = endTime;
				endTime = DateUtil.addDay(endTime, new int[] { 0, 0, +1 });
			}
		}

		for (ObsUsedType used : echartsList) {
			yData.add(used.getEcmcDownLoad());
			xTime.add(used.getTimeDis());
		}
		echartsBean.setyData(yData);
		echartsBean.setxTime(xTime);
		return echartsBean;
	}

	// 客户详情：资源详情：获取请求次数
	@SuppressWarnings({ "rawtypes", "unchecked" })
    private EcmcObsEchartsBean getRequestTimes(String cusId, String bucketName,
			Date start, Date end) {
		DateFormat sdf = new SimpleDateFormat("MM-dd HH:mm"); 
		DateFormat sdfMonthDay = new SimpleDateFormat("MM-dd");
		List<ObsUsedType> echartsList = new ArrayList<ObsUsedType>();
		EcmcObsEchartsBean echartsBean = new EcmcObsEchartsBean();
		int value = (int) ((end.getTime() - start.getTime()) / (24 * 60 * 60 * 1000));// 得到相差天数
		List<BigDecimal> yData = new ArrayList<BigDecimal>();
		List<String> xTime = new ArrayList<String>();
		if (value <= 4) {
			int hourse = (value + 1) * 24 / 6;
			Date beginTime = start;
			Date endTime = DateUtil.addDay(beginTime, new int[] { 0, 0, 0,+hourse });
			for (int i = 0; i < 6; i++) {

				Criteria criatira = new Criteria();
				criatira.andOperator(
						Criteria.where("owner").is(cusId),
						Criteria.where("bucket").is(bucketName),
						Criteria.where("timestamp").gte(
								DateUtil.addDay(beginTime, new int[] { 0, 0, 0,
										+1 })),
						Criteria.where("timestamp").lt(
								DateUtil.addDay(endTime, new int[] { 0, 0, 0,
										+1 })));
				List<JSONObject> jsonList = mongoTemplate.find(new Query(
						criatira), JSONObject.class, MongoCollectionName.OBS_USED_1H);
				long data = 0l;
				for (JSONObject json : jsonList) {
					long requestCount = 0l;
					List<HashMap> list2 = (ArrayList<HashMap>) json.get("categories");
					for (HashMap map : list2) {
						// 请求次数
						requestCount += Long.parseLong(map.get("ops").toString());
					}
					data = data + requestCount;
				}
				ObsUsedType used = new ObsUsedType();
				used.setEcmcCountRequest(BigDecimal.valueOf(data));
				
				String dateStr = "";
				dateStr = sdf.format(endTime);
				used.setTimeDis(dateStr);
				echartsList.add(used);

				beginTime = endTime;
				endTime = DateUtil.addDay(endTime,new int[] { 0, 0, 0, +hourse });
			}
		} else {// 查询整天的数据
			Date beginTime = start;
			Date endTime = DateUtil.addDay(beginTime, new int[] { 0, 0, +1 });
			for (int i = 0; i <= value; i++) {
				Criteria criatira = new Criteria();
				Sort sort = new Sort(Direction.DESC, "timestamp");
				criatira.andOperator(Criteria.where("owner").is(cusId),
						Criteria.where("bucket").is(bucketName), Criteria
								.where("timestamp").gte(beginTime), Criteria
								.where("timestamp").lt(endTime));
				List<JSONObject> requestList = mongoTemplate.find(new Query(
						criatira).with(sort), JSONObject.class, MongoCollectionName.OBS_USED_1H);

				List<ObsUsedType> list = new ArrayList<ObsUsedType>();
				for (JSONObject json : requestList) {
					long requestCount = 0;
					List<HashMap> list2 = (ArrayList<HashMap>) json.get("categories");
					for (HashMap map : list2) {
						// 请求次数
						requestCount += Long.parseLong(map.get("ops").toString());
					}
					ObsUsedType out = new ObsUsedType();
					out.setCountRequest(requestCount);
					list.add(out);
				}
				long requestCount = 0l;
				for (ObsUsedType request : list) {
					requestCount = requestCount+request.getCountRequest();
				}

				ObsUsedType used = new ObsUsedType();
				used.setEcmcCountRequest(new BigDecimal(requestCount));
				String dateStr = "";
				dateStr = sdfMonthDay.format(beginTime);
				used.setTimeDis(dateStr);
				echartsList.add(used);

				beginTime = endTime;
				endTime = DateUtil.addDay(endTime, new int[] { 0, 0, +1 });
			}
		}

		for (ObsUsedType used : echartsList) {
			yData.add(used.getEcmcCountRequest());
			xTime.add(used.getTimeDis());
		}
		echartsBean.setyData(yData);
		echartsBean.setxTime(xTime);
		return echartsBean;
	}

	/**
	 * 获取资源统计表格数据
	 * @param start
	 * @param end
	 * @param cusId
	 * @return
	 */
	public Page getObsResources(Page page, QueryMap queryMap,Date startTime, Date endTime,String cusId) throws Exception {
		Date today = new Date();
		SimpleDateFormat formatSecond = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat formatDay = new SimpleDateFormat("yyyy-MM-dd");
		String userId = cusId;
		List<ObsUsedType> obsList = new ArrayList<ObsUsedType>();
		List<ObsUsedType> resultList = new ArrayList<ObsUsedType>();
		String endStr = formatDay.format(endTime);//"yyyy-MM-dd"
		String todayStr = formatDay.format(today);
		String todayUse = formatSecond.format(today);//2016-04-11 17:53:44
		Date date = null;
		today = formatDay.parse(todayStr);//yyyy-MM-dd
		date = formatSecond.parse(todayUse);//yyyy-MM-dd HH:mm:ss
		Sort sort = new Sort(Direction.DESC, "timestamp");
		Criteria criatira = null;

		//步骤一： 如果查询时间包括当天，就把当天取得不完整的数据拿出来
		if (endStr.equals(todayStr)) {
			long hours=(date.getTime()-today.getTime())/1000/60/60;
			criatira = new Criteria();
			Date add1h =DateUtil.addDay(today, new int[] { 0, 0, 0, 1 });
			criatira.andOperator(Criteria.where("owner").is(userId),Criteria.where("timestamp").gte(add1h),Criteria.where("timestamp").lte(date));
			List<JSONObject> usedlistToday = mongoTemplate.find(new Query(criatira).with(sort), JSONObject.class, MongoCollectionName.OBS_USED_1H);
			List<JSONObject> storageUsedToday = mongoTemplate.find(new Query(criatira).with(sort), JSONObject.class,MongoCollectionName.OBS_STORAGE_1H);
			
			Criteria backCriatira = new Criteria();
	        backCriatira.andOperator(Criteria.where("cus_id").is(cusId),Criteria.where("timestamp").gte(add1h), Criteria.where("timestamp").lte(date));
	        List<JSONObject> backJsonList = mongoTemplate.find(new Query(backCriatira),JSONObject.class,MongoCollectionName.CDN_BACKSOURCE_1H);
			
			double storageUsedResult = 0;
			if(null!=storageUsedToday&&storageUsedToday.size()>0){
				for(int j=0;j<storageUsedToday.size();j++){
					 JSONObject obj=storageUsedToday.get(j);
					 Date timestamp = obj.getDate("timestamp");
					 String time = formatDay.format(timestamp);
					  if(time.equals(todayStr)){
				        JSONObject usage=obj.getJSONObject("usage");
				        if(null!=usage&&usage.size()>0){
				        	long storageUsed=usage.getLong("size_kb_actual");
				        	storageUsedResult+=storageUsed;
				        }
					  }
				}
			}
			storageUsedResult=storageUsedResult / hours;
			ObsUsedType type = new ObsUsedType();
			long countRequest = 0;
			long download = 0;
			//下载流量、请求次数
			if (null != usedlistToday && usedlistToday.size() > 0) {
				for (int i = 0; i < usedlistToday.size(); i++) {
					JSONObject obsUsed = usedlistToday.get(i);
					Date timestamp = obsUsed.getDate("timestamp");
					String time = formatDay.format(timestamp);
					
					Date thisTime = DateUtil.dateRemoveSec(timestamp);
		        	String bucketName = obsUsed.getString("bucket");
					if (time.equals(todayStr)) {
						long oneData = 0;
						JSONArray categories = obsUsed.getJSONArray("categories");
						for (int j = 0; j < categories.size(); j++) {
							long Request = categories.getJSONObject(j).getLong("ops");
							countRequest += Request;
							long byteSet = categories.getJSONObject(j).getLong("bytes_sent");
							oneData += byteSet;
						}
						long oneBacksource=0;
			        	if(null!=backJsonList && !backJsonList.isEmpty()){
			        		for(int j=0;j<backJsonList.size();j++){
			        			JSONObject backJson=backJsonList.get(j);
			        			Date backThisTime = backJson.getDate("timestamp");
			        			backThisTime = DateUtil.dateRemoveSec(backThisTime);
					        	String backBucketName = backJson.getString("bucket_name");
					        	if(thisTime.getTime()==backThisTime.getTime()&&bucketName.equals(backBucketName)){
					        		oneBacksource = backJson.getLongValue("backsource");
					        		break;
					        	}
			        		}
			        		oneData = (oneData > oneBacksource)?(oneData - oneBacksource):0;
			        	}
			        	download += oneData;
					}
				}
			}

			//转换单位：存储量、流量、请求次数
			String loadFlow =this.convertFlowLoadUnit(download);
			String requestCount = this.convertRequestCountUnit(countRequest);
			type.setLoadDown(loadFlow);
			type.setRequestCount(requestCount);
			//转换单位：存储量、流量、请求次数
			String resultStorage = this.convertStorageUnit(storageUsedResult);
			type.setUsedStorage(resultStorage);
			type.setCountRequest(countRequest);
			type.setDownload(download);
			type.setStorageUsed(storageUsedResult);
			type.setTimeDis(todayStr);
			type.setTime(today);
			obsList.add(type);
		}
		
		//步骤二：计算>=start <end的数据
		endTime = DateUtil.addDay(endTime, new int[] { 0, 0, 1 });
		criatira = new Criteria();
		criatira.andOperator(Criteria.where("owner").is(userId), 
				Criteria.where("timestamp").gte(startTime), 
				Criteria.where("timestamp").lt(endTime));
		//以下两个find只为把mongo数据查出来然后赋值，so： 不能mongo自动求和
		List<JSONObject> usedlist = mongoTemplate.find(new Query(criatira).with(sort), JSONObject.class,MongoCollectionName.OBS_USED_24H);
		List<JSONObject> storageUsed = mongoTemplate.find(new Query(criatira).with(sort), JSONObject.class,MongoCollectionName.OBS_STORAGE_24H);
		//第一种可能：只有下载流量与请求次数的数据
		if (null != usedlist && usedlist.size() > 0 && (null == storageUsed || storageUsed.size() <= 0)) {
			for (int i = 0; i < usedlist.size(); i++) {
				JSONObject obsUsed = usedlist.get(i);
				ObsUsedType type = new ObsUsedType();
				long coutRequest = obsUsed.getLong("countRequest");
				long byteSet = null != obsUsed.getLong("final_data")?
						obsUsed.getLong("final_data"):obsUsed.getLong("download");//根据新加的属性剔除回源
				Date timestamp = obsUsed.getDate("timestamp");
				String time = formatDay.format(timestamp);
				//转换单位：存储量、流量、请求次数
				String requestCount = this.convertRequestCountUnit(coutRequest);
				String loadFlow  = this.convertFlowLoadUnit(byteSet);
				type.setLoadDown(loadFlow);
				type.setRequestCount(requestCount);

				type.setUsedStorage("0KB");
				type.setStorageUsed(0);
				type.setCountRequest(coutRequest);
				type.setDownload(byteSet);
				type.setTimeDis(time);
				type.setTime(timestamp);
				obsList.add(type);
			}
			//第2种可能：只有存储容量的数据
		} else if (null != storageUsed && storageUsed.size() > 0&& (null == usedlist || usedlist.size() <= 0)) {
			for (int i = 0; i < storageUsed.size(); i++) {
				JSONObject storUsed = storageUsed.get(i);
				ObsUsedType obsType = new ObsUsedType();
				Date timestamp = storUsed.getDate("timestamp");
				String time = formatDay.format(timestamp);
				double storage = storUsed.getDouble("storageUsed");
				//转换单位：存储量、流量、请求次数
				String resultStorage = this.convertStorageUnit(storage);
				obsType.setUsedStorage(resultStorage);

				obsType.setRequestCount("0百次");
				obsType.setCountRequest(0);
				obsType.setLoadDown("0B");
				obsType.setDownload(0);
				obsType.setTime(timestamp);
				obsType.setTimeDis(time);
				obsType.setStorageUsed(storage);
				obsList.add(obsType);
			}
			//第3种可能：存储容量、下载流量、请求次数的数据都有
		} else if (null != storageUsed && storageUsed.size() > 0&& null != usedlist && usedlist.size() > 0) {
			for (int i = 0; i < usedlist.size(); i++) {
				JSONObject obsUsed = usedlist.get(i);
				ObsUsedType type = new ObsUsedType();
				long coutRequest = obsUsed.getLong("countRequest");
				long byteSet = null != obsUsed.getLong("final_data")?
						obsUsed.getLong("final_data"):obsUsed.getLong("download");//根据新加的属性剔除回源
				Date timestamp = obsUsed.getDate("timestamp");
				String time = formatDay.format(timestamp);
				type.setTime(timestamp);
				type.setTimeDis(time);
				type.setCountRequest(coutRequest);
				type.setDownload(byteSet);

				//转换单位：存储量、流量、请求次数
				String requestCount = this.convertRequestCountUnit(coutRequest);
				String loadFlow = this.convertFlowLoadUnit(byteSet);
				type.setLoadDown(loadFlow);
				type.setRequestCount(requestCount);
				
				for (int j = 0; j < storageUsed.size(); j++) {
					JSONObject stoageUsed = storageUsed.get(j);
					Date times = stoageUsed.getDate("timestamp");
					String timeDis = formatDay.format(times);
					if (time.trim().equals(timeDis.trim())) {
						double storage = stoageUsed.getDouble("storageUsed");
						//转换单位：存储量、流量、请求次数
						String resultStorage = this.convertStorageUnit(storage);
						type.setUsedStorage(resultStorage);
						type.setStorageUsed(storage);
						break;
					} else {
						type.setStorageUsed(0);
						type.setUsedStorage("0KB");
					}
				}
				obsList.add(type);
			}
		}
		//对消费统计计算并赋值
		ObsUsedType useType = this.getConsumeStatisticsAvg(obsList);
		//构造分页
		int startIndex = (queryMap.getPageNum() - 1)*queryMap.getCURRENT_ROWS_SIZE();
		int end = (startIndex + queryMap.getCURRENT_ROWS_SIZE()) - 1;
		if (obsList.size() - 1 < end) {
			end = obsList.size() - 1;
		}
		for (int i = 0; i < obsList.size(); i++) {
			if (startIndex <= i && i <= end) {
				resultList.add(obsList.get(i));
			}
		}
		//将阈值信息放入list
		for(ObsUsedType obsUsedType:resultList){
	    	obsUsedType.setAvgCountRequest(useType.getAvgCountRequest());
	    	obsUsedType.setAvgDownLoad(useType.getAvgDownLoad());
	    	obsUsedType.setAvgStorageUsed(useType.getAvgStorageUsed());
	    }
	    page = new Page(startIndex, obsList.size(), 10, resultList);
		return page;

	}
	/**
	 * 对消费统计进行赋值
	 * @param 
	 * @return
	 */
	private ObsUsedType getConsumeStatisticsAvg (List<ObsUsedType> list){
		ObsUsedType used = new ObsUsedType();
		DecimalFormat df1 = new DecimalFormat("0.00");// 格式化小数
		double storage = 0;
		long request = 0l;
		long flow = 0l;
		
		for(ObsUsedType use:list){
			storage = storage + use.getStorageUsed();
			request = request + use.getCountRequest();
			flow = flow + use.getDownload();
		}
		storage = storage/list.size();
		//给消费统计专用字段赋值
		if(flow >= tb){
			float loadsize = (float) flow / tb;
			String load = df1.format(loadsize);
			used.setAvgDownLoad(load + "TB");
		}else if(flow >= gb && flow<tb){
			float loadsize = (float) flow / gb;
			String load = df1.format(loadsize);
			used.setAvgDownLoad(load + "GB");
		}else if(flow >= mb&&flow<gb){
			float loadsize = (float) flow / mb;
			String load = df1.format(loadsize);
			used.setAvgDownLoad(load + "MB");
		}else if (flow >= kb&&flow<mb) {
			float loadsize = (float) flow / kb;
			String load = df1.format(loadsize);
			used.setAvgDownLoad(load + "KB");
		} else {
			used.setAvgDownLoad(flow + "B");
		}
		//请求次数赋值
		if (request >= 10000) {
			float requestCount = (float) request / 10000;
			String requestTimes = df1.format(requestCount);
			used.setAvgCountRequest(requestTimes + "万次");
		} else {
			float requestCount = (float) request / 100;
			String requestTimes = df1.format(requestCount);
			used.setAvgCountRequest(requestTimes + "百次");
		}
		//存储容量赋值
		if (storage >= gb) {
			double storageCount = storage / gb;
			String storageUse = df1.format(storageCount);
			used.setAvgStorageUsed(storageUse + "TB");
		} else if (storage >= mb && storage < gb) {
			double storageCount = storage / mb;
			String storageUse = df1.format(storageCount);
			used.setAvgStorageUsed(storageUse + "GB");
		} else if (storage >= kb && storage < mb) {
			double storageCount = storage / kb;
			String storageUse = df1.format(storageCount);
			used.setAvgStorageUsed(storageUse + "MB");
		} else {
			String storageUse = df1.format(storage);
			used.setAvgStorageUsed(storageUse + "KB");
		}
		return used;
	}
	/**
	 * 功能：格式化日期 yyyy-MM-dd 时间：May 24, 2011 3:05:11 PM
	 */
	public static String formatDate(Date date) {
		SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd");
		return outFormat.format(date);
	}

}
