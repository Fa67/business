package com.eayun.obs.ecmcservice.impl;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.alibaba.fastjson.JSONObject;
import com.eayun.accesskey.model.AccessKey;
import com.eayun.accesskey.model.BaseAccessKey;
import com.eayun.accesskey.service.AccessKeyService;
import com.eayun.common.ConstantClazz;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.DateUtil;
import com.eayun.customer.ecmcservice.EcmcCustomerService;
import com.eayun.customer.model.BaseCusServiceState;
import com.eayun.customer.model.BaseCustomer;
import com.eayun.customer.model.Customer;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.obs.dao.CusServiceStateDao;
import com.eayun.obs.dao.ObsUserDao;
import com.eayun.obs.ecmcmodel.EcmcObsEchartsBean;
import com.eayun.obs.ecmcmodel.EcmcObsTopModel;
import com.eayun.obs.ecmcservice.EcmcObsOverviewService;
import com.eayun.obs.model.BaseObsUser;
import com.eayun.obs.model.ObsBucket;
import com.eayun.obs.model.ObsUsedType;
import com.eayun.syssetup.ecmcservice.EcmcSysDataTreeService;
import com.eayun.syssetup.model.BaseEcmcSysDataTree;
import com.eayun.syssetup.model.EcmcSysDataTree;

/**
 * EcmcObsUsedServiceImpl
 * 
 * @Filename: EcmcObsUsedServiceImpl.java
 * @Description:
 * @Version: 1.1
 * @Author: liyanchao
 * @Email: yanchao.li@eayun.com
 * @History:<br> <li>Date: 2016年3月28日</li>
 * 
 */
@Service
@Transactional
public class EcmcObsOverviewServiceImpl implements EcmcObsOverviewService {
    private static final Logger log = LoggerFactory.getLogger(EcmcObsOverviewServiceImpl.class);
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private EcmcSysDataTreeService ecmcSysDataTreeService;
	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
	private EcmcCustomerService ecmcCustomerService;
	@Autowired
    private EcmcLogService ecmcLogService;
	@Autowired
	private ObsUserDao obsUserDao;
	@Autowired
	private AccessKeyService accessKeyService;
	@Autowired
	private CusServiceStateDao cusServiceStateDao;

	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat formatss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	/**
	 * 24小时新增：当前时间的存储量-24小时前的时间点的存储量=存储容量； 总下载量与请求次数：直接查询时间〉=24小时之前
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ObsUsedType getObs24Used() throws Exception {
		Sort sort = new Sort(Direction.DESC, "timestamp");
		// 获取当前时间整点
		Date now = new Date();
		Calendar date = Calendar.getInstance();
		date.setTime(now);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		now = date.getTime();
		// mongo求大于等于当前整点时间的SUM:usage.size_kb_actual
		Aggregation aggStorage = Aggregation.newAggregation(
				Aggregation.match(Criteria.where("timestamp").gte(now)),
				Aggregation.group().sum("usage.size_kb_actual").as("total")
				);
		AggregationResults<JSONObject> totalresult = mongoTemplate.aggregate(aggStorage, MongoCollectionName.OBS_STORAGE_1H, JSONObject.class);
		List<JSONObject> totalStorageList = totalresult.getMappedResults();
		long capacity = 0l;
		if (null != totalStorageList && totalStorageList.size() > 0) {
			Map<String, Object> map = totalStorageList.get(0);
			String totalString = map.get("total").toString();
			capacity = Long.parseLong(totalString);
		}
		ObsUsedType out = new ObsUsedType();
		out.setStorageUsed(capacity);
		//获取24小时前“整点”时间点：now-24h
		Date agostart = new Date();
		Calendar agodate = Calendar.getInstance();
		agodate.setTime(agostart);
		agodate.set(Calendar.DATE, agodate.get(Calendar.DATE) - 1);
		agodate.set(Calendar.HOUR_OF_DAY, agodate.get(Calendar.HOUR_OF_DAY));
		agodate.set(Calendar.MINUTE, 0);
		agodate.set(Calendar.SECOND, 0);
		agodate.set(Calendar.MILLISECOND, 0);
		agostart = agodate.getTime();// 24小时前“整点”开始时间
		agodate.setTime(agostart);
		agodate.set(Calendar.HOUR_OF_DAY, agodate.get(Calendar.HOUR_OF_DAY) + 1);// 当前时间为10， 查询需要加1h
		Date agoEnd = agodate.getTime();// 24小时结束时间
		//mongo求24小时前的正点的SUM，查询一个h时间段
		Aggregation aggs = Aggregation.newAggregation(
				Aggregation.match(Criteria.where("timestamp").gte(agostart)),
				Aggregation.match(Criteria.where("timestamp").lt(agoEnd)),
				Aggregation.group().sum("usage.size_kb_actual").as("total"));
		AggregationResults<JSONObject> agoTotal = mongoTemplate.aggregate(aggs,MongoCollectionName.OBS_STORAGE_1H, JSONObject.class);
		List<JSONObject> agoStorageList = agoTotal.getMappedResults();
		long storage = 0l;
		if (null != agoStorageList && agoStorageList.size() > 0) {
			Map<String, Object> map = agoStorageList.get(0);
			String totalString = map.get("total").toString();
			storage = Long.parseLong(totalString);
		}

		out.setStorageUsed(out.getStorageUsed() - storage); // 24小时内新增存储量
		Criteria cFlow = new Criteria();
		cFlow.andOperator(Criteria.where("timestamp").gte(agostart));
		// 获取24小时前流量与访问次数
		List<JSONObject> usedlistToday = mongoTemplate.find(new Query(cFlow).with(sort), JSONObject.class, MongoCollectionName.OBS_USED_1H);
		
		/**从每小时的数据中剔除回源流量，正则相加，负则为0再相加*/
		Criteria backCriatira = new Criteria();
        backCriatira.andOperator(Criteria.where("timestamp").gte(agostart));
        List<JSONObject> backJsonList = mongoTemplate.find(new Query(backCriatira),JSONObject.class,MongoCollectionName.CDN_BACKSOURCE_1H);
		
		long flowAgo = 0l;
		long requestCountAgo = 0l;
		for (JSONObject ult : usedlistToday) {
			List<HashMap> list = (ArrayList<HashMap>) ult.get("categories");
			Date thisTime = ult.getDate("timestamp");
        	thisTime = DateUtil.dateRemoveSec(thisTime);
        	String bucketName = ult.getString("bucket");
        	String owner = ult.getString("owner");
        	long oneData = 0l;
			for (HashMap map : list) {
				// 流量
				oneData += Long.parseLong(map.get("bytes_sent").toString());
				// 请求次数
				requestCountAgo += Long.parseLong(map.get("ops").toString());
			}
			long oneBacksource = 0l;
			if(null!=backJsonList && !backJsonList.isEmpty()){
        		for(int j=0;j<backJsonList.size();j++){
        			JSONObject backJson=backJsonList.get(j);
        			Date backThisTime = backJson.getDate("timestamp");
        			backThisTime = DateUtil.dateRemoveSec(backThisTime);
		        	String backBucketName = backJson.getString("bucket_name");
		        	String cusId = backJson.getString("cus_id");
		        	if(thisTime.getTime()==backThisTime.getTime() && bucketName.equals(backBucketName) && owner.equals(cusId)){
		        		oneBacksource = backJson.getLongValue("backsource");
		        		break;
		        	}
        		}
        	}
			long diffData = (oneData > oneBacksource)?(oneData - oneBacksource):0;
			flowAgo += diffData;
		}
		out.setCountRequest(requestCountAgo);
		out.setDownload(flowAgo);
		// 转换单位：存储量、下载流量、请求次数
		String resultStorage = this.convertStorageUnit(out.getStorageUsed());
		String flowLoad = this.convertFlowLoadUnit(flowAgo);
		String request = this.convertRequestCountUnit(requestCountAgo);
		out.setUsedStorage(resultStorage);
		out.setRequestCount(request);
		out.setLoadDown(flowLoad);
		return out;
	}

	/**
	 * 换算单位：存储量（GB、MB）
	 **/
	private String convertStorageUnit(double storageAgo) {
		String storage = "";
		// 单位 （GB）:存储量的
		if (Math.abs(storageAgo) >= 1024 * 1024) {
			double storageSize = (double) storageAgo / (1024 * 1024);
			String size = formatTosepara(storageSize);
			storage = size + "GB";
		} else {
			// 单位 （MB）
			double storageSize = (double) storageAgo / (1024);
			String size = formatTosepara(storageSize);
			storage = size + "MB";
		}
		return storage;
	}

	/**
	 * 换算单位：下载流量（GB、MB）
	 **/
	private String convertFlowLoadUnit(long flowAgo) {
		String flowLoad = "";
		// 单位 （GB）:下载流量
		if (flowAgo >= 1024 * 1024 * 1024) {
			double flow = (double) flowAgo / (1024 * 1024 * 1024);
			String size = formatTosepara(flow);
			flowLoad = size + "GB";
		} else {
			// 单位 （MB）
			double flow = (double) flowAgo / (1024 * 1024);
			String size = formatTosepara(flow);
			flowLoad = size + "MB";
		}
		return flowLoad;
	}

	/**
	 * 换算单位：请求次数（百次、万次）
	 */
	private String convertRequestCountUnit(long requestCountAgo) {
		String request = "";
		// 单位 （万次）:请求次数
		if (requestCountAgo >= 10000) {
			double requestCount = (double) requestCountAgo / 10000;
			String size = formatTosepara(requestCount);
			request = size + "万次";
		} else {
			// 单位 （百次）
			double requestCount = (double) requestCountAgo / 100;
			String size = formatTosepara(requestCount);
			request = size + "百次";
		}
		return request;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
	public ObsUsedType getObsView() throws Exception {
		Date today = new Date();
		Calendar date = Calendar.getInstance();
		date.setTime(today);
		//获取当前整点时间
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		today = date.getTime();
		Criteria criatira = new Criteria();
		Sort sort = new Sort(Direction.ASC, "timestamp");
		criatira.andOperator(Criteria.where("timestamp").gte(today));// >=当前整点时间
		//只为算出bucket总数
		List<JSONObject> storageUsedToday = mongoTemplate.find(new Query(criatira).with(sort), JSONObject.class, MongoCollectionName.OBS_STORAGE_1H);

		long capacity = 0l;// 已用存储量(现在)
		long objectNum = 0l;// object总数
		// mongo求当前整点 --》“存储概览：已用”     容量的和
		Aggregation aggStorage = Aggregation.newAggregation(
				Aggregation.match(Criteria.where("timestamp").gte(today)),
				Aggregation.group().sum("usage.size_kb_actual").as("totalStorage"));
		AggregationResults<JSONObject> totalStorageResult = mongoTemplate.aggregate(aggStorage, MongoCollectionName.OBS_STORAGE_1H, JSONObject.class);
		List<JSONObject> totalStorageList = totalStorageResult.getMappedResults();
		if (null != totalStorageList && totalStorageList.size() > 0) {
			Map<String, Object> map = totalStorageList.get(0);
			String totalString = map.get("totalStorage").toString();
			capacity = Long.parseLong(totalString);
		}
		//“存储概览：object总数”
		Aggregation numObject = Aggregation.newAggregation(
				Aggregation.match(Criteria.where("timestamp").gte(today)),
				Aggregation.group().sum("usage.num_objects").as("totalNumObject"));
		AggregationResults<JSONObject> totalNumObject = mongoTemplate.aggregate(numObject, MongoCollectionName.OBS_STORAGE_1H, JSONObject.class);
		List<JSONObject> totalNumObjectList = totalNumObject.getMappedResults();
		if (null != totalNumObjectList && totalNumObjectList.size() > 0) {
			Map<String, Object> map = totalNumObjectList.get(0);
			String totalString = map.get("totalNumObject").toString();
			objectNum = Long.parseLong(totalString);
		}
		//bean赋值： 创建bucket总数。。。
		ObsUsedType out = new ObsUsedType();
		out.setBucketCount(storageUsedToday.size());
		out.setStorageUsed(capacity);
		out.setObjectCount(objectNum);
		// 把当前时间改为当天 00：00：00，获取今天零点到现在的数据
		Criteria usedNow = new Criteria();
		Date todayStart = new Date();
		Calendar todayStartC = Calendar.getInstance();
		todayStartC.setTime(todayStart);
		todayStartC.set(Calendar.HOUR_OF_DAY, 0);
		todayStartC.set(Calendar.MINUTE, 0);
		todayStartC.set(Calendar.SECOND, 0);
		todayStartC.set(Calendar.MILLISECOND, 0);
		todayStart = todayStartC.getTime();
		usedNow.andOperator(Criteria.where("timestamp").gte(todayStart));
		List<JSONObject> usedlistToday = mongoTemplate.find(new Query(usedNow).with(sort), JSONObject.class, MongoCollectionName.OBS_USED_1H);
		
		/**从今天的数据中扣除回源流量 */
		Criteria backCriatira = new Criteria();
        backCriatira.andOperator(Criteria.where("timestamp").gte(todayStart));
        List<JSONObject> backJsonList = mongoTemplate.find(new Query(backCriatira).with(sort),JSONObject.class,
        		MongoCollectionName.CDN_BACKSOURCE_1H);
		long sent = 0l;// 下载流量
		long received = 0l;// 上传流量
		long getRequestNum = 0l;// get请求次数
		long putRequestNum = 0l;
		long deleteRequestNum = 0l;
		for (JSONObject ult : usedlistToday) {
			Date thisTime = ult.getDate("timestamp");
        	thisTime = DateUtil.dateRemoveSec(thisTime);
        	String bucketName = ult.getString("bucket");
        	String owner = ult.getString("owner");
			List<HashMap> list = (ArrayList<HashMap>) ult.get("categories");
			long oneData = 0;
			for (HashMap map : list) {
				// 下载流量
				oneData += Long.parseLong(map.get("bytes_sent").toString());
				// 上传流量
				received += Long.parseLong(map.get("bytes_received").toString());
				// get请求次数
				String category = (String) map.get("category");
				boolean getFlag = category.startsWith("get");
				if (getFlag) {
					getRequestNum += Long.parseLong(map.get("ops").toString());
				}
				// put请求次数
				boolean putFlag = category.startsWith("put");
				if (putFlag) {
					putRequestNum += Long.parseLong(map.get("ops").toString());
				}
				// delete请求次数
				boolean deleteFlag = category.startsWith("delete");
				if (deleteFlag) {
					deleteRequestNum += Long.parseLong(map.get("ops").toString());
				}
			}
			long oneBacksource=0;
			if(null!=backJsonList && !backJsonList.isEmpty()){
				for(int j=0;j<backJsonList.size();j++){
        			JSONObject backJson=backJsonList.get(j);
        			Date backThisTime = backJson.getDate("timestamp");
        			backThisTime = DateUtil.dateRemoveSec(backThisTime);
		        	String backBucketName = backJson.getString("bucket_name");
		        	String cusId = backJson.getString("cus_id");
		        	if(thisTime.getTime()==backThisTime.getTime() && bucketName.equals(backBucketName)
		        			&& owner.equals(cusId)){
		        		oneBacksource = backJson.getLongValue("backsource");
		        		break;
		        	}
        		}
			}
			long diffData = (oneData > oneBacksource)?(oneData - oneBacksource):0;
			sent += diffData;
		}

		/**查询今天之前的数据*/
		// mongo：查<= 当前日期 obs.used.24h 的下载的和
		Aggregation aggd = Aggregation.newAggregation(
				Aggregation.match(Criteria.where("timestamp").lte(new Date())),
				Aggregation.match(Criteria.where("final_data").exists(false)),//没有新加属性的按照旧下载流量相加
				Aggregation.group().sum("download").as("totalDownLoad")
				);
		AggregationResults<JSONObject> downLoadResult = mongoTemplate.aggregate(aggd, 
				MongoCollectionName.OBS_USED_24H, JSONObject.class);
		List<JSONObject> downLoadList = downLoadResult.getMappedResults();

		if (null != downLoadList && downLoadList.size() > 0) {
			Map<String, Object> map = downLoadList.get(0);
			sent = sent + (long) map.get("totalDownLoad");
		}
		Aggregation aggback = Aggregation.newAggregation(
				Aggregation.match(Criteria.where("timestamp").lte(new Date())),
				Aggregation.match(Criteria.where("final_data").exists(true)),//有新加属性的按照新加属性相加
				Aggregation.group().sum("final_data").as("totalFinalData")
				);
		AggregationResults<JSONObject> backResult = mongoTemplate.aggregate(aggback, 
				MongoCollectionName.OBS_USED_24H, JSONObject.class);
		List<JSONObject> backList = backResult.getMappedResults();
		
		if (null != backList && !backList.isEmpty()) {
			Map<String, Object> map = backList.get(0);
			sent = sent + (long) map.get("totalFinalData");
		}
		
		//mongo：查<= 当前日期 obs.used.24h 上传总和
		Aggregation aggu = Aggregation.newAggregation(
				Aggregation.match(Criteria.where("timestamp").lte(new Date())),
				Aggregation.group().sum("upload").as("totalUpLoad"));
		AggregationResults<JSONObject> upLoadResult = mongoTemplate.aggregate(
				aggu, MongoCollectionName.OBS_USED_24H, JSONObject.class);
		List<JSONObject> upLoadList = upLoadResult.getMappedResults();
		if (null != upLoadList && upLoadList.size() > 0) {
			Map<String, Object> map = upLoadList.get(0);
			received = received+(long) map.get("totalUpLoad");
		}
		//查<= 当前日期 obs.used.24h求get总和
		Aggregation aggg = Aggregation.newAggregation(
				Aggregation.match(Criteria.where("timestamp").lte(new Date())),
				Aggregation.group().sum("getCount").as("totalGetCount"));
		AggregationResults<JSONObject> getCountResult = mongoTemplate.aggregate(aggg, MongoCollectionName.OBS_USED_24H, JSONObject.class);
		List<JSONObject> getCountList = getCountResult.getMappedResults();
		if (null != getCountList && getCountList.size() > 0) {
			Map<String, Object> map = getCountList.get(0);
			getRequestNum = getRequestNum + (long) map.get("totalGetCount");
		}
		// mongo查<= 当前日期 obs.used.24h 求put总和
		Aggregation aggp = Aggregation.newAggregation(
				Aggregation.match(Criteria.where("timestamp").lte(new Date())),
				Aggregation.group().sum("putCount").as("totalPutCount"));
		AggregationResults<JSONObject> putCountResult = mongoTemplate.aggregate(aggp, MongoCollectionName.OBS_USED_24H, JSONObject.class);
		List<JSONObject> putCountList = putCountResult.getMappedResults();
		if (null != putCountList && putCountList.size() > 0) {
			Map<String, Object> map = putCountList.get(0);
			putRequestNum = putRequestNum + (long) map.get("totalPutCount");
		}
		// mongo查<= 当前日期 obs.used.24h 求delete总和
		Aggregation aggdelete = Aggregation.newAggregation(
				Aggregation.match(Criteria.where("timestamp").lte(new Date())),
				Aggregation.group().sum("deleteCount").as("totalDeleteCount")
				);
		AggregationResults<JSONObject> deleteCountResult = mongoTemplate.aggregate(aggdelete, MongoCollectionName.OBS_USED_24H, JSONObject.class);
		List<JSONObject> deleteCountList = deleteCountResult.getMappedResults();
		if (null != deleteCountList && deleteCountList.size() > 0) {
			Map<String, Object> map = deleteCountList.get(0);
			deleteRequestNum = deleteRequestNum + (long) map.get("totalDeleteCount");
		}

		out.setDeleteRequestCount(deleteRequestNum);
		out.setGetRequestCount(getRequestNum);
		out.setPutRequestCount(putRequestNum);
		out.setSent(sent);
		out.setReceived(received);
		// 设置总存储量 需要获取
		BaseEcmcSysDataTree sysDataTree = ecmcSysDataTreeService.getDataTreeById("0009002");
		String totalStorage = "0";
		if (null != sysDataTree) {
			totalStorage = sysDataTree.getMemo();
		}

		out.setTotalStorage(Double.parseDouble(totalStorage) * 1024 * 1024);
		out.setBucketCountStr(formatTosepara(out.getBucketCount()));
		// 存储量 单位 （MB）
		double storage = (double) capacity / (1024);
		out.setUsedStorage(formatTosepara(storage) + "MB");
		// object 单位 （百个）
		double object = (double) objectNum / 100;
		out.setObjectCountStr(formatTosepara(object));
		// delete 单位 （百个）
		double delete = (double) deleteRequestNum / 100;
		out.setDeleteRequestCountStr(formatTosepara(delete));
		// get 单位 （百个）
		double get = (double) getRequestNum / 100;
		out.setGetRequestCountStr(formatTosepara(get));
		// put 单位 （百个）
		double put = (double) putRequestNum / 100;
		out.setPutRequestCountStr(formatTosepara(put));
		// 下载 单位 （MB）
		double sents = (double) sent / (1024 * 1024);
		out.setSentStr(formatTosepara(sents));
		// 上传 单位 （MB）
		double rece = (double) received / (1024 * 1024);
		out.setReceivedStr(formatTosepara(rece));
		out.setUnStorageUsed(out.getTotalStorage() - capacity);
		double unUsed = (double) (out.getTotalStorage() - capacity) / (1024);
		out.setUnUsedStorage(formatTosepara(unUsed) + "MB");
		return out;

	}

	@Override
	public EcmcObsEchartsBean getChart(String type, Date startTime, Date endTime)
			throws Exception {
		EcmcObsEchartsBean echartsBean = new EcmcObsEchartsBean();
		List<BigDecimal> yDataList = new ArrayList<BigDecimal>();
		// 步骤1
		if ("storage".equals(type)) {
			echartsBean = this.getStorage(startTime, endTime);
		} else if ("loadFlow".equals(type)) {
			echartsBean = this.getLoadFlow(startTime, endTime);
		} else if ("request".equals(type)) {
			echartsBean = this.getRequestTimes(startTime, endTime);
		}
		// 步骤2：求出对应的各项阈值
		// 设置总存储量 需要获取
		ObsUsedType threshold = this.getThreshold();
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
			minData = minData.multiply(BigDecimal.valueOf(0.8)).setScale(2,BigDecimal.ROUND_HALF_UP);
			maxData = maxData.multiply(BigDecimal.valueOf(1.2)).setScale(2,BigDecimal.ROUND_HALF_UP);
			echartsBean.setyDataMin(minData.toString());
			echartsBean.setyDataMax(maxData.toString());
			echartsBean.setType(type);
			echartsBean.setRequestQuota(threshold.getRequestCount());
			echartsBean.setLoadFlowQuota(threshold.getLoadDown());
			echartsBean.setStorageQuota(threshold.getUsedStorage());
		}
		return echartsBean;
	}

	@Override
	public String setThreshold(String storage, String flow, String requestCount) {
		EcmcSysDataTree sysDataTreeStorage = new EcmcSysDataTree();
		EcmcSysDataTree sysDataTreeLoadDown = new EcmcSysDataTree();
		EcmcSysDataTree sysDataTreeRequestCount = new EcmcSysDataTree();
		boolean flagStorage = false;
		boolean flagLoadDown = false;
		boolean flagRequest = false;

		sysDataTreeStorage = ecmcSysDataTreeService.getDataTreeById("0009001001");
		if (null != sysDataTreeStorage && null!=storage) {
			Double storageDouble=Double.parseDouble(storage);
			storageDouble=(double)(Math.round(storageDouble*100)/100.0);
			sysDataTreeStorage.setMemo(storageDouble.toString());
			try {
				flagStorage = ecmcSysDataTreeService.updateDataTree(sysDataTreeStorage);
			} catch (Exception e) {
			    log.error(e.getMessage(),e);
				return ConstantClazz.ERROR_CODE;
			}
			
		}
		sysDataTreeLoadDown = ecmcSysDataTreeService.getDataTreeById("0009001002");
		if (null != sysDataTreeLoadDown && null!=flow ) {
			try {
				Double flowDouble=Double.parseDouble(flow);
				flowDouble=(double)(Math.round(flowDouble*100)/100.0); 
				sysDataTreeLoadDown.setMemo(flowDouble.toString());
				flagLoadDown = ecmcSysDataTreeService.updateDataTree(sysDataTreeLoadDown);
			} catch (Exception e) {
			    log.error(e.getMessage(),e);
				return ConstantClazz.ERROR_CODE;
			}
			
		}

		sysDataTreeRequestCount = ecmcSysDataTreeService.getDataTreeById("0009001003");
		if (null != sysDataTreeLoadDown && null!=requestCount) {
			try {
				Double requestCountDouble=Double.parseDouble(requestCount);
				requestCountDouble=(double)(Math.round(requestCountDouble*100)/100.0);
				sysDataTreeRequestCount.setMemo(requestCountDouble.toString());
				flagRequest = ecmcSysDataTreeService.updateDataTree(sysDataTreeRequestCount);
			} catch (Exception e) {
			    log.error(e.getMessage(),e);
				return ConstantClazz.ERROR_CODE;
			}
		}
		if(flagStorage || flagLoadDown || flagRequest){
			ecmcLogService.addLog("更改阈值", "对象存储", null, null, 1, null, null);
			return ConstantClazz.SUCCESS_CODE;
		}else {
			ecmcLogService.addLog("更改阈值", "对象存储", null, null, 0, null, null);
			return ConstantClazz.ERROR_CODE;
		}

	}

	/**
	 * 获取阈值
	 * @return
	 */
	public ObsUsedType getThreshold() {
		BaseEcmcSysDataTree storageTree = new BaseEcmcSysDataTree();
		BaseEcmcSysDataTree loadDownTree = new BaseEcmcSysDataTree();
		BaseEcmcSysDataTree requestCountTree = new BaseEcmcSysDataTree();
		ObsUsedType out = new ObsUsedType();
		storageTree = ecmcSysDataTreeService.getDataTreeById("0009001001");
		if (null != storageTree) {
			out.setUsedStorage(storageTree.getMemo());
		}
		
		loadDownTree = ecmcSysDataTreeService.getDataTreeById("0009001002");
		if (null != loadDownTree) {
			out.setLoadDown(loadDownTree.getMemo());
		}

		requestCountTree = ecmcSysDataTreeService.getDataTreeById("0009001003");
		if (null != requestCountTree) {
			out.setRequestCount(requestCountTree.getMemo());
		}
		return out;
	}

	@Override
	public List<EcmcObsTopModel> getTop10(String type) throws Exception {
		List<EcmcObsTopModel> bucketList = new ArrayList<EcmcObsTopModel>();
		List<JSONObject> storageUsedToday = new ArrayList<JSONObject>();
		final Date lastDay = this.getMondayOfThisWeek();
		// 第一种情况：获取存储量
		if ("storage".equals(type)) {
			// >=本周1,获取mongo时间段的list
			Criteria criatira = new Criteria();
			Sort sort = new Sort(Direction.DESC, "timestamp");
			criatira.andOperator(Criteria.where("timestamp").gte(lastDay));
			storageUsedToday = mongoTemplate.find(new Query(criatira).with(sort), JSONObject.class,MongoCollectionName.OBS_STORAGE_1H);
			List<Customer> cusList =  ecmcCustomerService.getObsCustomer();
			for (Customer customer : cusList) {
				//以bucket分组，获取bucket>=本周一 的每个bucket存储量
				Aggregation agg = Aggregation.newAggregation(
								Aggregation.match(Criteria.where("timestamp").gte(lastDay)),
								Aggregation.match(Criteria.where("owner").is(customer.getCusId())),
								Aggregation.group("bucket").sum("usage.size_kb_actual").as("total"));
				AggregationResults<JSONObject> totalresult = mongoTemplate.aggregate(agg, MongoCollectionName.OBS_STORAGE_1H, JSONObject.class);
				List<JSONObject> totallist = totalresult.getMappedResults();
				for (JSONObject mapJson : totallist) {
					EcmcObsTopModel model = new EcmcObsTopModel();
					BigDecimal mongoSize = new BigDecimal(mapJson.get("total").toString());
					mongoSize = mongoSize.divide(new BigDecimal(1024), 2,BigDecimal.ROUND_HALF_UP);
					model.setSize(mongoSize);// 得到MB
					model.setBucketName(mapJson.get("_id").toString());
					bucketList.add(model);
				}
			}
			
			//将每个bucket添加所属客户名称
			for (JSONObject sut : storageUsedToday) {
				String bucketName = sut.getString("bucket");
				String obsId = sut.getString("owner");
				for (EcmcObsTopModel model : bucketList) {
					if (model.getBucketName().equals(bucketName)&& null == model.getBelongUser()) {
						BaseCustomer cus = ecmcCustomerService.getCustomerById(obsId);
						if (cus != null && cus.getCusId() != null && cus.getCusId().length() > 0) {
							model.setBelongUserId(obsId);
							model.setBelongUser(cus.getCusOrg());
						}
						break;
					}
				}

			}
			// 第2种情况：获取下载流量
		} else if ("loadFlow".equals(type)) {
			Criteria criatiraFlow = new Criteria();
			Sort sortFlow = new Sort(Direction.DESC, "timestamp");
			criatiraFlow.andOperator(Criteria.where("timestamp").gte(lastDay));
			List<JSONObject> usedlistToday = mongoTemplate.find(new Query(criatiraFlow).with(sortFlow), JSONObject.class, MongoCollectionName.OBS_USED_1H);
			List<EcmcObsTopModel> list = new ArrayList<EcmcObsTopModel>();
			bucketList = this.getBucketListFromMongo(usedlistToday, list, type,"bytes_sent",lastDay);
			// 第3种情况：获取请求次数
		} else if ("request".equals(type)) {
			Criteria criatiraRequest = new Criteria();
			Sort sortRequest = new Sort(Direction.DESC, "timestamp");
			criatiraRequest.andOperator(Criteria.where("timestamp").gte(lastDay));
			List<JSONObject> usedlistToday = mongoTemplate.find(new Query(criatiraRequest).with(sortRequest), JSONObject.class, MongoCollectionName.OBS_USED_1H);
			List<EcmcObsTopModel> list = new ArrayList<EcmcObsTopModel>();
			bucketList = this.getBucketListFromMongo(usedlistToday, list, type,"ops",lastDay);
		}

		// 从大到小 将bucketList排序
		Collections.sort(bucketList, new Comparator<EcmcObsTopModel>() {
			@Override
			public int compare(EcmcObsTopModel o1, EcmcObsTopModel o2) {
				return o2.getSize().compareTo(o1.getSize());
			}
		});
		List<EcmcObsTopModel> otmList = new ArrayList<EcmcObsTopModel>();
		// 根据传入的list 查询bucket在redis中的上期排名
		if ("storage".equals(type)) {
			otmList = this.getBucketLastRankByRedis(bucketList,RedisKey.OBS_SORT_BY_STORAGE_USED, lastDay,type);
		} else if ("loadFlow".equals(type)) {
			otmList = this.getBucketLastRankByRedis(bucketList,RedisKey.OBS_SORT_BY_DOWNLOAD, lastDay,type);
		} else if ("request".equals(type)) {
			otmList = this.getBucketLastRankByRedis(bucketList,RedisKey.OBS_SORT_BY_COUNT_REQUEST, lastDay,type);
		}

		return otmList;
	}

	/**
	 * 根据传入的type:loadFlow或request，得到换算好的list loadFlow
	 * 对应categoriesKey:bytes_sent; request 对应categoriesKey:ops
	 */
	//TODO 此方法需要改造
	@SuppressWarnings({ "rawtypes", "unchecked" })
    private List<EcmcObsTopModel> getBucketListFromMongo(List<JSONObject> usedlistToday, List<EcmcObsTopModel> bucketList,
    		String type, String categoriesKey, Date lastDay) {
		List<EcmcObsTopModel> list = new ArrayList<EcmcObsTopModel>();
		List<JSONObject> backJsonList = null;
		if(type.equals("loadFlow")){
			Criteria backCriatira = new Criteria();
			Sort backsort = new Sort(Direction.DESC, "timestamp");
	        backCriatira.andOperator(Criteria.where("timestamp").gte(lastDay));
	        backJsonList = mongoTemplate.find(new Query(backCriatira).with(backsort),
	        		JSONObject.class,MongoCollectionName.CDN_BACKSOURCE_1H);
		}
		for (JSONObject sut : usedlistToday) {
			long flow = 0l;
			String bucketName = (String) sut.get("bucket");
			Date thisTime = sut.getDate("timestamp");
			thisTime = DateUtil.dateRemoveSec(thisTime);
			EcmcObsTopModel otm = new EcmcObsTopModel();
			if (!bucketName.equals("")) {
				// list放入第一个值
				if (list.size() == 0) {
					otm.setBucketName(bucketName);
					String obsId = sut.getString("owner");
					// 流量(现在)
					List<HashMap> flowList = (ArrayList<HashMap>) sut.get("categories");
					for (HashMap map : flowList) {
						flow += Long.parseLong(map.get(categoriesKey).toString());
					}
					/**第一个bucket，扣除该时段的回源流量*/
					long oneBacksource = 0;
					if(null != backJsonList && !backJsonList.isEmpty()){
						for(int j = 0 ;j < backJsonList.size();j++){
							JSONObject backJson = backJsonList.get(j);
							Date backThisTime = backJson.getDate("timestamp");
		        			backThisTime = DateUtil.dateRemoveSec(backThisTime);
				        	String backBucketName = backJson.getString("bucket_name");
				        	String cusId = backJson.getString("cus_id");
				        	if(thisTime.getTime()==backThisTime.getTime() && bucketName.equals(backBucketName)
				        			&& obsId.equals(cusId)){
				        		oneBacksource = backJson.getLongValue("backsource");
				        		break;
				        	}
						}
					}
					flow = (flow > oneBacksource)?(flow - oneBacksource):0;
					otm.setSize(new BigDecimal(flow));
					
					otm.setBelongUserId(obsId);
					BaseCustomer cus = ecmcCustomerService.getCustomerById(obsId);
					if (cus != null && cus.getCusId() != null && cus.getCusId().length() > 0) {
						otm.setBelongUserId(obsId);
						otm.setBelongUser(cus.getCusOrg());
						list.add(otm);
					}

				} else {
					boolean flag = false;
					// 判断是否名称相同
					for (EcmcObsTopModel obsTopModel : list) {
						if (bucketName.equals(obsTopModel.getBucketName())) {// 判断现在有的list中是否含有当前的bucketName
							// 流量(现在)
							List<HashMap> flowList = (ArrayList<HashMap>) sut.get("categories");
							String obsId = sut.getString("owner");
							flow = obsTopModel.getSize().longValue();
							for (HashMap map : flowList) {
								flow = flow + Long.parseLong(map.get(categoriesKey).toString());
							}
							//TODO 这里判断可能有问题
							/**前面已有过的bucket，扣除该时段的回源流量*/
							long oneBacksource = 0;
							if(null != backJsonList && !backJsonList.isEmpty()){
								for(int j = 0 ;j < backJsonList.size();j++){
									JSONObject backJson = backJsonList.get(j);
									Date backThisTime = backJson.getDate("timestamp");
				        			backThisTime = DateUtil.dateRemoveSec(backThisTime);
						        	String backBucketName = backJson.getString("bucket_name");
						        	String cusId = backJson.getString("cus_id");
						        	if(thisTime.getTime()==backThisTime.getTime() && bucketName.equals(backBucketName)
						        			&& obsId.equals(cusId)){
						        		oneBacksource = backJson.getLongValue("backsource");
						        		break;
						        	}
								}
							}
							flow = (flow > oneBacksource)?(flow - oneBacksource):0;
							
							obsTopModel.setSize(new BigDecimal(flow));
							
							otm.setBelongUserId(obsId);
							BaseCustomer cus = ecmcCustomerService.getCustomerById(obsId);
							if (cus != null && cus.getCusId() != null && cus.getCusId().length() > 0) {
								otm.setBelongUser(cus.getCusOrg());
							}
							flag = true;
							break;
						}
					}
					if (!flag) {// 上面的for中没有这个bucket，则在此加入list
						otm.setBucketName(bucketName);
						// 流量(现在)
						List<HashMap> flowList = (ArrayList<HashMap>) sut.get("categories");
						String obsId = sut.getString("owner");
						for (HashMap map : flowList) {
							flow += Long.parseLong(map.get(categoriesKey).toString());
						}
						/**新的bucket，扣除该时段的回源流量*/
						long oneBacksource = 0;
						if(null != backJsonList && !backJsonList.isEmpty()){
							for(int j = 0 ;j < backJsonList.size();j++){
								JSONObject backJson = backJsonList.get(j);
								Date backThisTime = backJson.getDate("timestamp");
			        			backThisTime = DateUtil.dateRemoveSec(backThisTime);
					        	String backBucketName = backJson.getString("bucket_name");
					        	String cusId = backJson.getString("cus_id");
					        	if(thisTime.getTime()==backThisTime.getTime() && bucketName.equals(backBucketName)
					        			&& obsId.equals(cusId)){
					        		oneBacksource = backJson.getLongValue("backsource");
					        		break;
					        	}
							}
						}
						flow = (flow > oneBacksource)?(flow - oneBacksource):0;
						
						otm.setSize(new BigDecimal(flow));
						otm.setBelongUserId(obsId);
						BaseCustomer cus = ecmcCustomerService.getCustomerById(obsId);
						if (cus != null && cus.getCusId() != null && cus.getCusId().length() > 0) {
							otm.setBelongUserId(obsId);
							otm.setBelongUser(cus.getCusOrg());
							list.add(otm);
						}
					}
				}
			}
		}

		// 单位转换
		for (EcmcObsTopModel model : list) {
			EcmcObsTopModel original = model;
			if (type.equals("loadFlow")) {// 如果选择流量下载MB
				original.setSize(original.getSize().divide(new BigDecimal(1024 * 1024), 2,BigDecimal.ROUND_HALF_UP));
				original.setSizeStr(original.getSize()+"");
			} else {// 请求次数的换算单位：（万次）
				original.setSize(original.getSize().divide(new BigDecimal(10000), 2, BigDecimal.ROUND_HALF_UP));
				original.setSizeStr(original.getSize()+"");
			}
			bucketList.add(original);
		}

		return bucketList;
	}

	/*
	 * 根据传入的list 查询bucket在redis中的上期排名
	 */
	private List<EcmcObsTopModel> getBucketLastRankByRedis(List<EcmcObsTopModel> bucketList, String RedisKey, Date lastDay,String type) {
		// lastDay为始终为每周的周一日期
		List<EcmcObsTopModel> otmList = new ArrayList<EcmcObsTopModel>();
		List<String> listStorageUsed = jedisUtil.getZSetByRevRange(RedisKey, 0,-1);
		for (int i = 0; i < bucketList.size(); i++) {
			if (i < 10) {
				EcmcObsTopModel otm = bucketList.get(i);
				otm.setRank(i + 1);
				//根据type做单位转换
				if(type.equals("storage")){
					int value = (int) ((new Date().getTime() - lastDay.getTime()) / (1000 * 60 * 60));
					otm.setSize((otm.getSize().divide(new BigDecimal(value+1), 2,BigDecimal.ROUND_HALF_UP)));
					String size = formatTosepara(otm.getSize());
					otm.setSizeStr(size);
				}
				
				for (int j = 0; j < listStorageUsed.size(); j++) {
					String str = listStorageUsed.get(j);
					JSONObject json = JSONObject.parseObject(str);
					String bucketName = json.getString("name");
					if (otm.getBucketName().equals(bucketName)) {
						otm.setLastTop(j + 1);
						break;
					} 
				}
				otmList.add(otm);
			}
		}
	return otmList;
	}

	/**
	 * 将数字每三位逗号分隔
	 * 
	 * @param
	 * @return String
	 */
	public String formatTosepara(BigDecimal data) {
		DecimalFormat df = new DecimalFormat("#,###.##");
		return df.format(data);
	}

	/**
	 * 将数字每三位逗号分隔
	 * 
	 * @param
	 * @return String
	 */
	public String formatTosepara(double data) {
		DecimalFormat df = new DecimalFormat("#,###.##");
		return df.format(data);
	}
	/**
	 * 将数字每三位逗号分隔
	 * 
	 * @param
	 * @return String
	 */
	public String formatBigDecimalTosepara(BigDecimal data) {
		DecimalFormat df = new DecimalFormat("#,###.##");
		return df.format(data);
	}

	// 总览：存储详情：获取存储量
	private EcmcObsEchartsBean getStorage(Date start, Date end) throws Exception{
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
				/**** 封装代替上面注释代码 *****/
				Date startUse = DateUtil.addDay(beginTime, new int[] { 0, 0, 0,+1 });
				Date endUse = DateUtil.addDay(endTime, new int[] { 0, 0, 0, +1 });
				List<JSONObject> totallist = this.getResultSumByMongo(MongoCollectionName.OBS_STORAGE_1H, "usage.size_kb_actual", startUse,endUse);

				BigDecimal yData = new BigDecimal(0);
				if (null != totallist && totallist.size() > 0) {
					Map<String, Object> map = totallist.get(0);
					String totalString = map.get("total").toString();
					yData = new BigDecimal(totalString);
					//由字节转为MB.每小时的 存储量
					yData = yData.divide(new BigDecimal(1024*hourse), 2,BigDecimal.ROUND_HALF_UP);
				}
				ObsUsedType used = new ObsUsedType();
				used.setEcmcStorageUsed(yData);
				String dateStr = "";
				dateStr = sdf.format(endTime);
				used.setTimeDis(dateStr);
				echartsList.add(used);

				beginTime = endTime;
				endTime = DateUtil.addDay(endTime,new int[] { 0, 0, 0, +hourse });
			}
		} else {
			Date beginTime = start;
			Date endTime = DateUtil.addDay(beginTime, new int[] { 0, 0, +1 });
			for (int i = 0; i <= value; i++) {
				/**** 封装代替上面注释代码 *****/
				List<JSONObject> totallist = this.getResultSumByMongo(MongoCollectionName.OBS_STORAGE_24H, "storageUsed", beginTime,endTime);

				BigDecimal yData = new BigDecimal(0);
				if (null != totallist && totallist.size() > 0) {
					Map<String, Object> map = totallist.get(0);
					String totalString = map.get("total").toString();
					yData = new BigDecimal(totalString);
					//由字节转为MB
					yData = yData.divide(new BigDecimal(1024), 2,BigDecimal.ROUND_HALF_UP);
				}
				ObsUsedType used = new ObsUsedType();
				used.setEcmcStorageUsed(yData);
				String dateStr = "";
				dateStr = sdfMonthDay.format(beginTime);
				used.setTimeDis(dateStr);
				echartsList.add(used);
				beginTime = endTime;
				endTime = DateUtil.addDay(endTime, new int[] { 0, 0, +1 });
			}
			beginTime=DateUtil.addDay(beginTime, new int[]{0,0,-1});
			endTime = DateUtil.addDay(beginTime, new int[] { 0, 0, 1 });// TODO这个开始、结束时间需要把时分秒清零
			
			Date now = new Date();
			String todayStr = format.format(now);
			String todayUse = formatss.format(now);//2016-04-11 17:53:44
			Date date = new Date();
			now = format.parse(todayStr);//yyyy-MM-dd
			date = formatss.parse(todayUse);//yyyy-MM-dd HH:mm:ss
			Sort sort = new Sort(Direction.DESC, "timestamp");
			List<Customer> obsCustomers=ecmcCustomerService.getObsCustomer();
			double result = 0;
			for (Customer customer : obsCustomers) {
				String cusId=customer.getCusId();
					long hours=(date.getTime()-beginTime.getTime())/1000/60/60;
					if(hours<=1){
						hours=1;
					}
					Criteria criatira = new Criteria();
					Date add1h =DateUtil.addDay(beginTime, new int[] { 0, 0, 0, 1 });
					criatira.andOperator(Criteria.where("owner").is(cusId),Criteria.where("timestamp").gte(add1h),Criteria.where("timestamp").lte(date));
					List<JSONObject> storageUsedToday = mongoTemplate.find(new Query(criatira).with(sort), JSONObject.class,MongoCollectionName.OBS_STORAGE_1H);
					double storageUsedResult = 0;
					if(null!=storageUsedToday&&storageUsedToday.size()>0){
						for(int j=0;j<storageUsedToday.size();j++){
							 JSONObject obj=storageUsedToday.get(j);
							 Date timestamp = obj.getDate("timestamp");
							 String time = format.format(timestamp);
							  if(time.equals(todayStr)){
						        JSONObject usage=obj.getJSONObject("usage");
						        if(null!=usage&&usage.size()>0){
						        	long storageUsed=usage.getLong("size_kb_actual");
						        	storageUsedResult+=storageUsed;
						        }
							  }
						}
					}
					result+=storageUsedResult / hours;
			}
			ObsUsedType out=echartsList.get(echartsList.size()-1);
			BigDecimal yData=new BigDecimal(result);
			yData = yData.divide(new BigDecimal(1024), 2,BigDecimal.ROUND_HALF_UP);
			out.setEcmcStorageUsed(yData);
			echartsList.set(echartsList.size()-1, out);
			
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

	// 总览：存储详情：获取请求次数
	private EcmcObsEchartsBean getRequestTimes(Date start, Date end) {
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
				long data = 0l;
				/*** mongo查表，内存求和；代替上面注释代码 **/
				Date startUse = DateUtil.addDay(beginTime, new int[] { 0, 0, 0,+1 });
				Date endUse = DateUtil.addDay(endTime,new int[] { 0, 0, 0, +1 });
				data = this.getResultSumByRam(MongoCollectionName.OBS_USED_1H, "ops", startUse,endUse);
				ObsUsedType used = new ObsUsedType();
				used.setEcmcCountRequest(BigDecimal.valueOf(data));
				String dateStr = "";
				dateStr = sdf.format(endTime);
				used.setTimeDis(dateStr);
				echartsList.add(used);

				beginTime = endTime;
				endTime = DateUtil.addDay(endTime,new int[] { 0, 0, 0, +hourse });
			}
		} else {
			Date beginTime = start;
			Date endTime = DateUtil.addDay(beginTime, new int[] { 0, 0, +1 });
			for (int i = 0; i <= value; i++) {
				/**** 封装代替上面注释代码 *****/
				List<JSONObject> totallist = this.getResultSumByMongo(MongoCollectionName.OBS_USED_24H, "countRequest", beginTime, endTime);
				BigDecimal data = new BigDecimal(0);
				if (null != totallist && totallist.size() > 0) {
					Map<String, Object> map = totallist.get(0);
					String totalString = map.get("total").toString();
					data = new BigDecimal(totalString);
				}
				ObsUsedType used = new ObsUsedType();
				used.setEcmcCountRequest(data);
				String dateStr = "";
				dateStr = sdfMonthDay.format(beginTime);
				used.setTimeDis(dateStr);
				echartsList.add(used);

				beginTime = endTime;
				endTime = DateUtil.addDay(endTime, new int[] { 0, 0, +1 });
			}
			beginTime=DateUtil.addDay(beginTime, new int[]{0,0,-1,1});
			long result = this.getResultSumByRam(MongoCollectionName.OBS_USED_1H, "ops", beginTime,endTime);
			
			ObsUsedType out=echartsList.get(echartsList.size()-1);
			BigDecimal data = new BigDecimal(result);
			out.setEcmcCountRequest(data);
			echartsList.set(echartsList.size()-1, out);
		}

		for (ObsUsedType used : echartsList) {
			yData.add(used.getEcmcCountRequest());
			xTime.add(used.getTimeDis());
		}
		echartsBean.setyData(yData);
		echartsBean.setxTime(xTime);
		return echartsBean;
	}

	// 总览：存储详情：获取下载流量
	private EcmcObsEchartsBean getLoadFlow(Date start, Date end) {
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
				long data = 0l;
				Date startUse = DateUtil.addDay(beginTime, new int[] { 0, 0, 0,+1 });
				Date endUse = DateUtil.addDay(endTime,new int[] { 0, 0, 0, +1 });
				/*** mongo查表，内存求和；代替上面注释代码 **/
				data = this.getResultSumByRam(MongoCollectionName.OBS_USED_1H, "bytes_sent",startUse, endUse);
				BigDecimal bigDecimal = new BigDecimal(data);
				bigDecimal = bigDecimal.divide(new BigDecimal(1024 * 1024), 2,BigDecimal.ROUND_HALF_UP);
				ObsUsedType used = new ObsUsedType();
				used.setEcmcDownLoad(bigDecimal);
				String dateStr = "";
				dateStr = sdf.format(endTime);
				used.setTimeDis(dateStr);
				echartsList.add(used);

				beginTime = endTime;
				endTime = DateUtil.addDay(endTime,new int[] { 0, 0, 0, +hourse });
			}
		} else {
			Date beginTime = start;
			Date endTime = DateUtil.addDay(beginTime, new int[] { 0, 0, +1 });
			for (int i = 0; i <= value; i++) {

				List<JSONObject> totallist = this.getResultSumByMongo(MongoCollectionName.OBS_USED_24H, "download", beginTime, endTime);
				BigDecimal data = new BigDecimal(0);
				if (null != totallist && totallist.size() > 0) {
					Map<String, Object> map = totallist.get(0);
					String total = map.get("total").toString();
					data = new BigDecimal(total);
					if(totallist.size() > 1){	//判断是否有回源流量
						Map<String, Object> mapback = totallist.get(1);
						String totalback = mapback.get("total").toString();
						BigDecimal databack = new BigDecimal(totalback);
						data = data.add(databack);
					}
					data = data.divide(new BigDecimal(1024 * 1024), 2,BigDecimal.ROUND_HALF_UP);
				}
				ObsUsedType used = new ObsUsedType();
				used.setEcmcDownLoad(data);
				String dateStr = "";
				dateStr = sdfMonthDay.format(beginTime);
				used.setTimeDis(dateStr);
				echartsList.add(used);

				beginTime = endTime;
				endTime = DateUtil.addDay(endTime, new int[] { 0, 0, +1 });
			}
			beginTime=DateUtil.addDay(beginTime, new int[]{0,0,-1,1});
			long result = this.getResultSumByRam(MongoCollectionName.OBS_USED_1H, "bytes_sent", beginTime,endTime);
			
			ObsUsedType out=echartsList.get(echartsList.size()-1);
			BigDecimal data = new BigDecimal(result);
			data=data.divide(new BigDecimal(1024 * 1024), 2,BigDecimal.ROUND_HALF_UP);
			out.setEcmcDownLoad(data);
			echartsList.set(echartsList.size()-1, out);
		}

		for (ObsUsedType used : echartsList) {
			yData.add(used.getEcmcDownLoad());
			xTime.add(used.getTimeDis());
		}
		echartsBean.setyData(yData);
		echartsBean.setxTime(xTime);
		return echartsBean;
	}

	/**
	 * 获取周一日期
	 * 
	 * @return
	 */
	public Date getMondayOfThisWeek() {
		Calendar c = Calendar.getInstance();
		int day_of_week = c.get(Calendar.DAY_OF_WEEK) - 1;
		if (day_of_week == 0)
			day_of_week = 7;
		c.add(Calendar.DATE, -day_of_week + 1);
		c.set(Calendar.HOUR_OF_DAY, 1);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	/****** 通过mongo求和 ******/
	private List<JSONObject> getResultSumByMongo(String mongoTable,String sumField, Date beginTime, Date endTime) {
		List<JSONObject> totalList = new ArrayList<JSONObject>();
		/**总览详情-下载流量曲线图数据，扣除掉回源流量*/
		if(sumField.equals("download")){
			Aggregation agg = Aggregation.newAggregation(
					Aggregation.match(Criteria.where("timestamp").gte(beginTime)),
					Aggregation.match(Criteria.where("timestamp").lt(endTime)),
					Aggregation.match(Criteria.where("final_data").exists(false)),//有新加属性的按照新加属性相加
					Aggregation.group().sum("download").as("total"));
			AggregationResults<JSONObject> totalResult = mongoTemplate.aggregate(agg, mongoTable, JSONObject.class);
			totalList = totalResult.getMappedResults();
			
			Aggregation aggback = Aggregation.newAggregation(
					Aggregation.match(Criteria.where("timestamp").gte(beginTime)),
					Aggregation.match(Criteria.where("timestamp").lt(endTime)),
					Aggregation.match(Criteria.where("final_data").exists(true)),//有新加属性的按照新加属性相加
					Aggregation.group().sum("final_data").as("total"));
			AggregationResults<JSONObject> backResult = mongoTemplate.aggregate(aggback, 
					MongoCollectionName.OBS_USED_24H, JSONObject.class);
			List<JSONObject> backList = backResult.getMappedResults();
			if(null != backList && !backList.isEmpty()){
				if(totalList.isEmpty()){
					totalList = backList;
				}else{
					List<JSONObject> list = new ArrayList<JSONObject>();
					list.addAll(totalList);
					list.addAll(backList);
					return list;
				}
				
			}
		}else{
			Aggregation agg = Aggregation.newAggregation(
					Aggregation.match(Criteria.where("timestamp").gte(beginTime)),
					Aggregation.match(Criteria.where("timestamp").lt(endTime)),
					Aggregation.group().sum(sumField).as("total"));
			AggregationResults<JSONObject> totalResult = mongoTemplate.aggregate(agg, mongoTable, JSONObject.class);
			totalList = totalResult.getMappedResults();
		}
		return totalList;
	}

	/****** 通过查mongo，内存求和 ******/
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private long getResultSumByRam(String mongoTable, String sumField,
			Date beginTime, Date endTime) {
		Criteria criatira = new Criteria();
		criatira.andOperator(Criteria.where("timestamp").gte(beginTime),Criteria.where("timestamp").lt(endTime));
		List<JSONObject> jsonList = mongoTemplate.find(new Query(criatira),JSONObject.class, mongoTable);
		
		/**对象存储总览详情-下载流量曲线数据显示,下载流量时扣除掉回源流量部分*/
		List<JSONObject> backJsonList = null;
		if(sumField.equals("bytes_sent")){
			Criteria backCriatira = new Criteria();
	        backCriatira.andOperator(Criteria.where("timestamp").gte(beginTime), Criteria.where("timestamp").lt(endTime));
	        backJsonList = mongoTemplate.find(new Query(backCriatira),JSONObject.class,MongoCollectionName.CDN_BACKSOURCE_1H);
		}
		long data = 0l;
		for (JSONObject json : jsonList) {
			long count = 0l;
			Date thisTime = json.getDate("timestamp");
        	thisTime = DateUtil.dateRemoveSec(thisTime);
        	String bucketName = json.getString("bucket");
        	String owner = json.getString("owner");
			List<HashMap> list2 = (ArrayList<HashMap>) json.get("categories");
			for (HashMap map : list2) {
				count += Long.parseLong(map.get(sumField).toString());
			}
			long oneBacksource = 0;
			if(null != backJsonList && !backJsonList.isEmpty()){//扣除回源
				for(int j=0;j<backJsonList.size();j++){
        			JSONObject backJson=backJsonList.get(j);
        			Date backThisTime = backJson.getDate("timestamp");
        			backThisTime = DateUtil.dateRemoveSec(backThisTime);
		        	String backBucketName = backJson.getString("bucket_name");
		        	String cusId = backJson.getString("cus_id");
		        	if(thisTime.getTime()==backThisTime.getTime() && bucketName.equals(backBucketName) 
		        			&& owner.equals(cusId)){
		        		oneBacksource = backJson.getLongValue("backsource");
		        		break;
		        	}
        		}
				count = (count > oneBacksource)?(count - oneBacksource):0;
			}
			data = data + count;
		}

		return data;
	}

	@Override
	public EayunResponseJson syncObsUser() throws Exception {
		EayunResponseJson eayunResponseJson=new EayunResponseJson();
		if(syncObsUserFromDB()&&syncObsServiceFromDB()&&syncObsAkFromDB()){
			eayunResponseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		}else{
			eayunResponseJson.setRespCode(ConstantClazz.ERROR_CODE);
		}
		return eayunResponseJson;
	}
	/**
	 * 同步obs用户集合
	 */
	@SuppressWarnings("unchecked")
    private boolean syncObsUserFromDB(){
		try {
			Set<String> keys=jedisUtil.keys(RedisKey.OBSUSER_CUSID+"*");
			//删除redis中obsuser集合的数据
			for (String key : keys) {
				jedisUtil.delete(key);
			}
			//获取数据库中obsuser数据
			StringBuffer hql = new StringBuffer();
			hql.append("from BaseObsUser");
			List<BaseObsUser> obsUsers=obsUserDao.find(hql.toString());
			for (BaseObsUser obsUser : obsUsers) {
				jedisUtil.set(RedisKey.OBSUSER_CUSID + obsUser.getCusId(), JSONObject.toJSONString(obsUser));
			}
			return true;
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			return false;
		}
	}
	/**
	 * 同步obsAccesskey
	 */
	private boolean syncObsAkFromDB(){
		try {
			Set<String> keys=jedisUtil.keys(RedisKey.AK+"*");
			//删除redis中ak集合的数据
			for (String key : keys) {
				jedisUtil.delete(key);
			}
			//获取数据库中obsaccesskey数据
			
			List<AccessKey> accessKeys=accessKeyService.getAllAk();
			for (BaseAccessKey accessKey : accessKeys) {
				jedisUtil.set(RedisKey.AK_AKID+accessKey.getAkId(), JSONObject.toJSONString(accessKey));
				jedisUtil.addToSet(RedisKey.AK_CUSID+accessKey.getUserId(), accessKey.getAkId());
				jedisUtil.set(RedisKey.AK_ACCESSKEY+accessKey.getAccessKey(), JSONObject.toJSONString(accessKey));
			}
			return true;
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			return false;
		}
	}
	/**
	 * 同步开通服务状态
	 */
	@SuppressWarnings("unchecked")
    private boolean syncObsServiceFromDB(){
		try {
			Set<String> keys=jedisUtil.keys(RedisKey.CUSSERVICESTATE_CUSID+"*");
			for (String key : keys) {
				jedisUtil.delete(key);
			}
			StringBuffer hql = new StringBuffer();
			hql.append("from BaseCusServiceState");
			List<BaseCusServiceState> cusServiceStates=cusServiceStateDao.find(hql.toString());
			for (BaseCusServiceState cusServiceState : cusServiceStates) {
				jedisUtil.set(RedisKey.CUSSERVICESTATE_CUSID +cusServiceState.getCusId(), JSONObject.toJSONString(cusServiceState));
			}
			return true;
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			return false;
		}
		
	}
	/**
	 * 功能：格式化日期 yyyy-MM-dd 时间：May 24, 2011 3:05:11 PM
	 */
	public static String formatDate(Date date) {
		SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd");
		return outFormat.format(date);
	}
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
}
