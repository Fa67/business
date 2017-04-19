package com.eayun.obs.service.impl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.util.DateUtil;
import com.eayun.obs.model.ObsUsedType;
import com.eayun.obs.service.ObsUsedService;

@Service
@Transactional
public class ObsUsedServiceImpl implements ObsUsedService {
	private static final Logger log = LoggerFactory.getLogger(ObsUsedServiceImpl.class);
	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public List<ObsUsedType> getObsUsedList(Date startTime, Date endTime,
			String cusId) throws Exception {
		Date today = new Date();
		String userId = cusId;
		List<ObsUsedType> obsList = new ArrayList<ObsUsedType>();
		String endStr = DateUtil.dateToStr(endTime);// format.format(endTime);
		String todayStr = DateUtil.dateToStr(today);// format.format(today);
		String todayUse = DateUtil.dateToString(today);// formatss.format(today);//

		Date date = null;
		Sort sort = new Sort(Direction.DESC, "timestamp");
		Criteria criatira = null;
		DecimalFormat df1 = new DecimalFormat("0.00");// 格式化小数

		today = DateUtil.strToDate(todayStr); // format.parse(todayStr);
		date = DateUtil.stringToDate(todayUse);// formatss.parse(todayUse);
		long kb = 1024;
		long mb = kb * 1024;
		long gb = mb * 1024;
		long tb = gb * 1024;
		// 如果查询时间包括当天，就把当天取得不完整的数据拿出来
		if (endStr.equals(todayStr)) {
			long hours = (date.getTime() - today.getTime()) / 1000 / 60 / 60;
			if(hours<=1){
				hours=1;
			}
			criatira = new Criteria();
			criatira.andOperator(
					Criteria.where("owner").is(userId),
					Criteria.where("timestamp").gte(
							DateUtil.addDay(today, new int[] { 0, 0, 0, 1 })),
					Criteria.where("timestamp").lte(date));
			List<JSONObject> usedlistToday = mongoTemplate.find(new Query(
					criatira).with(sort), JSONObject.class, MongoCollectionName.OBS_USED_1H);
			List<JSONObject> storageUsedToday = mongoTemplate.find(new Query(
					criatira).with(sort), JSONObject.class,
					MongoCollectionName.OBS_STORAGE_1H);
			
            /** 添加CDN下载流量**/
            Aggregation agg = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("cus_id").is(userId)),
                    Aggregation.match(Criteria.where("timestamp").gte(DateUtil.addDay(today, new int[]{0,0,0,1}))),
                    Aggregation.match(Criteria.where("timestamp").lte(date)),
                    Aggregation.group().sum("flow_data").as("sum")
                    );
            AggregationResults<JSONObject> totalresult = mongoTemplate.aggregate(agg,MongoCollectionName.OBS_CDN_1H, JSONObject.class);
            
            List<JSONObject> totallist = totalresult.getMappedResults();
            long cdnFlow = 0;
            if(totallist.size() > 0){
                cdnFlow = totallist.get(0).getLong("sum");
            }
            
            /**从下载流量中剔除回源流量 */
            Criteria backCriatira = new Criteria();
            backCriatira.andOperator(
					Criteria.where("cus_id").is(userId),
					Criteria.where("timestamp").gte(
							DateUtil.addDay(today, new int[] { 0, 0, 0, 1 })),
					Criteria.where("timestamp").lte(date));
            List<JSONObject> backJsonListToday = mongoTemplate.find(new Query(
            		backCriatira).with(sort), JSONObject.class, MongoCollectionName.CDN_BACKSOURCE_1H);
			
			ObsUsedType type = new ObsUsedType();
			long countRequest = 0;
			long download = 0;
			double storage = 0;
			if (null != usedlistToday && usedlistToday.size() > 0) {
				for (int i = 0; i < usedlistToday.size(); i++) {
					JSONObject obsUsed = usedlistToday.get(i);
					Date timestamp = obsUsed.getDate("timestamp");
					timestamp = DateUtil.dateRemoveSec(timestamp);
					String bucketName = obsUsed.getString("bucket");
					String time = DateUtil.dateToStr(timestamp);
					if (time.equals(todayStr)) {
						JSONArray categories = obsUsed
								.getJSONArray("categories");
						long oneData = 0;
						for (int j = 0; j < categories.size(); j++) {
							long Request = categories.getJSONObject(j).getLong(
									"ops");
							countRequest += Request;
							long byteSet = categories.getJSONObject(j).getLong(
									"bytes_sent");
							oneData += byteSet;
						}
						/** 当天的数据统计，遍历每个bucket每个时间的统计，正则加，负则为0*/
						long backData=0;
						if(null!=backJsonListToday&&!backJsonListToday.isEmpty()){
							for(int j = 0; j < backJsonListToday.size(); j++){
								JSONObject backJson = backJsonListToday.get(j);
								Date backDate = backJson.getDate("timestamp");
								String backBucketName = backJson.getString("bucket_name");
								backDate = DateUtil.dateRemoveSec(backDate);
								if(timestamp.getTime()==backDate.getTime() && bucketName.equals(backBucketName)){
									backData = backJson.getLongValue("backsource");
									break;
								}
							}
						}
						long diffData = (oneData-backData)>0?oneData-backData:0;
						download += diffData;
					}
				}
			}

			if (null != storageUsedToday && storageUsedToday.size() > 0) {
				for (int j = 0; j < storageUsedToday.size(); j++) {
					JSONObject obj = storageUsedToday.get(j);
					Date timestamp = obj.getDate("timestamp");
					String time = DateUtil.dateToStr(timestamp);
					if (time.equals(todayStr)) {
						JSONObject usage = obj.getJSONObject("usage");
						if (null != usage && usage.size() > 0) {
							long storageUsed = usage.getLong("size_kb_actual");
							storage += storageUsed;
						}
					}
				}
			}

			storage = storage / hours;

			if (download >= tb) {
				float loadsize = (float) download / tb;
				String load = df1.format(loadsize);
				type.setLoadDown(load + "TB");
			} else if (download >= gb && download < tb) {
				float loadsize = (float) download / gb;
				String load = df1.format(loadsize);
				type.setLoadDown(load + "GB");
			} else if (download >= mb && download < gb) {
				float loadsize = (float) download / mb;
				String load = df1.format(loadsize);
				type.setLoadDown(load + "MB");
			} else if (download >= kb && download < mb) {
				float loadsize = (float) download / kb;
				String load = df1.format(loadsize);
				type.setLoadDown(load + "KB");

			} else {
				type.setLoadDown(download + "B");
			}

			if (countRequest >= 10000) {
				float requestCount = (float) countRequest / 10000;
				String request = df1.format(requestCount);
				type.setRequestCount(request + "万次");
			} else {
				float requestCount = (float) countRequest / 100;
				String request = df1.format(requestCount);
				type.setRequestCount(request + "百次");
			}

			if (storage >= gb) {
				double storageCount = storage / gb;
				String storageUse = df1.format(storageCount);
				type.setUsedStorage(storageUse + "TB");
			} else if (storage >= mb && storage < gb) {
				double storageCount = storage / mb;
				String storageUse = df1.format(storageCount);
				type.setUsedStorage(storageUse + "GB");
			} else if (storage >= kb && storage < mb) {
				double storageCount = storage / kb;
				String storageUse = df1.format(storageCount);
				type.setUsedStorage(storageUse + "MB");
			} else {
				String storageUse = df1.format(storage);
				type.setUsedStorage(storageUse + "KB");
			}

	         /** 添加CDN下载流量**/
            if(cdnFlow>=tb){
                float cdnsize = (float) cdnFlow / tb;
                String cdn = df1.format(cdnsize);
                type.setCdnFlowStr(cdn + "TB");
            }else if(cdnFlow>=gb&&cdnFlow<tb){
                float cdnsize = (float) cdnFlow / gb;
                String cdn = df1.format(cdnsize);
                type.setCdnFlowStr(cdn + "GB");
            }else if (cdnFlow >= mb&&cdnFlow<gb) {
                float cdnsize = (float) cdnFlow / mb;
                String cdn = df1.format(cdnsize);
                type.setCdnFlowStr(cdn + "MB");
            }else if(cdnFlow >= kb&&cdnFlow<mb) {
                float cdnsize = (float) cdnFlow / kb;
                String cdn = df1.format(cdnsize);
                type.setCdnFlowStr(cdn + "KB");
            }else{
                type.setCdnFlowStr(cdnFlow + "B");
            }
            type.setCdnFlow(cdnFlow);
			
			type.setCountRequest(countRequest);
			type.setDownload(download);
			type.setStorageUsed(storage);
			type.setTimeDis(todayStr);
			type.setTime(today);
			obsList.add(type);
		}
		endTime = DateUtil.addDay(endTime, new int[] { 0, 0, 1 });
		criatira = new Criteria();
		criatira.andOperator(Criteria.where("owner").is(userId), Criteria
				.where("timestamp").gte(startTime), Criteria.where("timestamp")
				.lt(endTime));
		List<JSONObject> usedlist = mongoTemplate.find(
				new Query(criatira).with(sort), JSONObject.class,
				MongoCollectionName.OBS_USED_24H);
		List<JSONObject> storageUsed = mongoTemplate.find(
				new Query(criatira).with(sort), JSONObject.class,
				MongoCollectionName.OBS_STORAGE_24H);
	      /** 添加CDN下载流量**/
        criatira=new Criteria();
        criatira.andOperator(Criteria.where("cus_id").is(userId), Criteria.where("timestamp").gte(startTime), Criteria.where("timestamp").lte(endTime));
        List<JSONObject> obsCdnFlowList = mongoTemplate.find(new Query(criatira).with(sort), JSONObject.class,MongoCollectionName.OBS_CDN_1D);
		
		if (null != usedlist && usedlist.size() > 0
				&& (null == storageUsed || storageUsed.size() <= 0)) {
			for (int i = 0; i < usedlist.size(); i++) {
				JSONObject obsUsed = usedlist.get(i);
				ObsUsedType type = new ObsUsedType();
				long coutRequest = obsUsed.getLong("countRequest");
				/**整天的统计量：改为取新加的属性值*/
				long byteSet = null==obsUsed.getLong("final_data")?obsUsed.getLong("download"):obsUsed.getLong("final_data");// 从底层取出的下载流量单位是byte
				Date timestamp = obsUsed.getDate("timestamp");
				String time = DateUtil.dateToStr(timestamp);
				if (byteSet >= tb) {
					float loadsize = (float) byteSet / tb;
					String load = df1.format(loadsize);
					type.setLoadDown(load + "TB");
				} else if (byteSet >= gb && byteSet < tb) {
					float loadsize = (float) byteSet / gb;
					String load = df1.format(loadsize);
					type.setLoadDown(load + "GB");
				} else if (byteSet >= mb && byteSet < gb) {
					float loadsize = (float) byteSet / mb;
					String load = df1.format(loadsize);
					type.setLoadDown(load + "MB");
				} else if (byteSet >= kb && byteSet < mb) {
					float loadsize = (float) byteSet / kb;
					String load = df1.format(loadsize);
					type.setLoadDown(load + "KB");
				} else {
					type.setLoadDown(byteSet + "B");
				}

				if (coutRequest >= 10000) {
					float requestCount = (float) coutRequest / 10000;
					String request = df1.format(requestCount);
					type.setRequestCount(request + "万次");
				} else {
					float requestCount = (float) coutRequest / 100;
					String request = df1.format(requestCount);
					type.setRequestCount(request + "百次");
				}

				type.setCountRequest(coutRequest);
				type.setDownload(byteSet);
				type.setUsedStorage("0KB");
				type.setStorageUsed(0);
				type.setTimeDis(time);
				type.setTime(timestamp);
				obsList.add(type);
			}
		} else if (null != storageUsed && storageUsed.size() > 0
				&& (null == usedlist || usedlist.size() <= 0)) {
			for (int i = 0; i < storageUsed.size(); i++) {
				JSONObject storUsed = storageUsed.get(i);
				ObsUsedType obsType = new ObsUsedType();
				Date timestamp = storUsed.getDate("timestamp");
				String time = DateUtil.dateToStr(timestamp);
				double storage = storUsed.getDouble("storageUsed");

				if (storage >= gb) {
					double storageCount = storage / gb;
					String storageUse = df1.format(storageCount);
					obsType.setUsedStorage(storageUse + "TB");
				} else if (storage >= mb && storage < gb) {
					double storageCount = storage / mb;
					String storageUse = df1.format(storageCount);
					obsType.setUsedStorage(storageUse + "GB");
				} else if (storage >= kb && storage < mb) {
					double storageCount = storage / kb;
					String storageUse = df1.format(storageCount);
					obsType.setUsedStorage(storageUse + "MB");
				} else {
					String storageUse = df1.format(storage);
					obsType.setUsedStorage(storageUse + "KB");
				}

				obsType.setRequestCount("0百次");
				obsType.setCountRequest(0);
				obsType.setLoadDown("0KB");
				obsType.setDownload(0);
				obsType.setTime(timestamp);
				obsType.setTimeDis(time);
				obsType.setStorageUsed(storage);
				obsList.add(obsType);
			}
		} else if (null != storageUsed && storageUsed.size() > 0
				&& null != usedlist && usedlist.size() > 0) {
			for (int i = 0; i < usedlist.size(); i++) {
				JSONObject obsUsed = usedlist.get(i);
				ObsUsedType type = new ObsUsedType();
				long coutRequest = obsUsed.getLong("countRequest");
				/**整天的统计量：改为取新加的属性值*/
				long byteSet = null==obsUsed.getLong("final_data")?obsUsed.getLong("download"):obsUsed.getLong("final_data");
				Date timestamp = obsUsed.getDate("timestamp");
				String time = DateUtil.dateToStr(timestamp);
				type.setTime(timestamp);
				type.setTimeDis(time);
				type.setCountRequest(coutRequest);
				type.setDownload(byteSet);

				if (byteSet >= tb) {
					float loadsize = (float) byteSet / tb;
					String load = df1.format(loadsize);
					type.setLoadDown(load + "TB");
				} else if (byteSet >= gb && byteSet < tb) {
					float loadsize = (float) byteSet / gb;
					String load = df1.format(loadsize);
					type.setLoadDown(load + "GB");
				} else if (byteSet >= mb && byteSet < gb) {
					float loadsize = (float) byteSet / mb;
					String load = df1.format(loadsize);
					type.setLoadDown(load + "MB");
				} else if (byteSet >= kb && byteSet < mb) {
					float loadsize = (float) byteSet / kb;
					String load = df1.format(loadsize);
					type.setLoadDown(load + "KB");
				} else {
					type.setLoadDown(byteSet + "B");
				}

				if (coutRequest >= 10000) {
					float requestCount = (float) coutRequest / 10000;
					String request = df1.format(requestCount);
					type.setRequestCount(request + "万次");
				} else {
					float requestCount = (float) coutRequest / 100;
					String request = df1.format(requestCount);
					type.setRequestCount(request + "百次");
				}

				for (int j = 0; j < storageUsed.size(); j++) {
					JSONObject stoageUsed = storageUsed.get(j);
					Date times = stoageUsed.getDate("timestamp");
					String timeDis = DateUtil.dateToStr(times);
					if (time.trim().equals(timeDis.trim())) {
						double storage = stoageUsed.getDouble("storageUsed");

						if (storage >= gb) {
							double storageCount = storage / gb;
							String storageUse = df1.format(storageCount);
							type.setUsedStorage(storageUse + "TB");
						} else if (storage >= mb && storage < gb) {
							double storageCount = storage / mb;
							String storageUse = df1.format(storageCount);
							type.setUsedStorage(storageUse + "GB");
						} else if (storage >= kb && storage < mb) {
							double storageCount = storage / kb;
							String storageUse = df1.format(storageCount);
							type.setUsedStorage(storageUse + "MB");
						} else {
							String storageUse = df1.format(storage);
							type.setUsedStorage(storageUse + "KB");
						}
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
		
        /** 添加CDN下载流量**/
        for(ObsUsedType type:obsList){
            if(null == type.getCdnFlowStr()){
                type.setCdnFlowStr("0B");
            }
        }
        if (null != obsCdnFlowList && obsCdnFlowList.size() > 0) {
            for (int i = 0; i < obsCdnFlowList.size(); i++) {
                JSONObject cdnFlow = obsCdnFlowList.get(i);
                long flow = cdnFlow.getLong("flow_data");
                Date timestamp = cdnFlow.getDate("timestamp");
                String time = DateUtil.dateToStr(timestamp);
                
                for(ObsUsedType type:obsList){
                    if(time.equals(type.getTimeDis())){
                        if(flow>=tb){
                            float cdnsize = (float) flow / tb;
                            String cdn = df1.format(cdnsize);
                            type.setCdnFlowStr(cdn + "TB");
                        }else if(flow>=gb&&flow<tb){
                            float cdnsize = (float) flow / gb;
                            String cdn = df1.format(cdnsize);
                            type.setCdnFlowStr(cdn + "GB");
                        }else if (flow >= mb&&flow<gb) {
                            float cdnsize = (float) flow / mb;
                            String cdn = df1.format(cdnsize);
                            type.setCdnFlowStr(cdn + "MB");
                        }else if(flow >= kb&&flow<mb) {
                            float cdnsize = (float) flow / kb;
                            String cdn = df1.format(cdnsize);
                            type.setCdnFlowStr(cdn + "KB");
                        }else{
                            type.setCdnFlowStr(flow + "B");
                        }
                        type.setCdnFlow(flow);
                        break;
                    }
                }
            }
        }
		return obsList;
	}

	@SuppressWarnings("rawtypes")
    @Override
	public double getObsStorage(String cusId, Date startTime, Date endTime)
			throws Exception {
		Date start = startTime;
		// 1 时间格式化到小时 如2016-06-10 15:00
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
		double storage = 0;
		startTime = DateUtil.stringToDate(sdf.format(startTime));
		endTime = DateUtil.stringToDate(sdf.format(endTime));
		long hours = (endTime.getTime() - startTime.getTime())
				/ (1000 * 60 * 60);
			Date end = null;
			for (int i = 0; i < hours; i++) {
				end = DateUtil.addDay(startTime, new int[] { 0, 0, 0, 1 });
				Criteria criatira = new Criteria();
				Sort sort = new Sort(Direction.DESC, "timestamp");
				criatira.andOperator(Criteria.where("owner").is(cusId),
						Criteria.where("timestamp").gt(startTime), Criteria
								.where("timestamp").lte(end));
				List<JSONObject> storageUsed = mongoTemplate.find(new Query(
						criatira).with(sort), JSONObject.class,
						MongoCollectionName.OBS_STORAGE_1H);

				for (JSONObject json : storageUsed) {
					Map map = (HashMap) json.get("usage");
					if (map.size() > 0) {
						// 已用存储量(现在)
						long usedStorage = Long.parseLong(map.get(
								"size_kb_actual").toString());
						storage += usedStorage;
					}
				}
				startTime = DateUtil
						.addDay(startTime, new int[] { 0, 0, 0, 1 });
			}
		storage = storage / hours / 1024 / 1024;
		log.info("客户"+cusId+"的在"+start+"到"+endTime+"间的存储容量:"+storage+"GB");
		return storage;
	}

	@Override
	public Map<String, Object> getObsUsed(String cusId, Date startTime, Date endTime)
			throws Exception {
		Date start=startTime;
		// 1 时间格式化到小时 如2016-06-10 15:00
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
		startTime = DateUtil.stringToDate(sdf.format(startTime));
		endTime = DateUtil.stringToDate(sdf.format(endTime));
		long hours = (endTime.getTime() - startTime.getTime())
				/ (1000 * 60 * 60);
		long countRequest = 0;
		double download = 0;
			Date end = null;
			for (int i = 0; i < hours; i++) {
				end = DateUtil.addDay(startTime, new int[] { 0, 0, 0, 1 });
				Criteria criatira = new Criteria();
				criatira.andOperator(
						Criteria.where("owner").is(cusId),
						Criteria.where("timestamp").gt(
								startTime),
						Criteria.where("timestamp").lte(end));
				List<JSONObject> usedlistToday = mongoTemplate.find(new Query(
						criatira), JSONObject.class, MongoCollectionName.OBS_USED_1H);

				if (null != usedlistToday && usedlistToday.size() > 0) {
					for (int j = 0; j < usedlistToday.size(); j++) {
						JSONObject obsUsed = usedlistToday.get(j);
							JSONArray categories = obsUsed
									.getJSONArray("categories");
							for (int l = 0; l < categories.size(); l++) {
								long Request = categories.getJSONObject(l).getLong(
										"ops");
								countRequest += Request;
								long byteSet = categories.getJSONObject(l).getLong(
										"bytes_sent");
								download += byteSet;
							}
					}
				}
				startTime = DateUtil
						.addDay(startTime, new int[] { 0, 0, 0, 1 });
			}
		Map<String, Object> map=new HashMap<String, Object>();
		download=download/1024/1024/1024;
		map.put("download", download);
		map.put("requestCount", countRequest);
		log.info("客户"+cusId+"在"+start+"到"+endTime+"间下载流量:"+download+"GB,请求次数:"+countRequest+"次");
		return map;
	}

}
