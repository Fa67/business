package com.eayun.obs.thread;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.DateUtil;

public class ObsStorageSortDetailSummaryThread implements Runnable {

	private static final Logger log = LoggerFactory
			.getLogger(ObsStorageSortDetailSummaryThread.class);
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	private MongoTemplate mongoTemplate;
	private JedisUtil jedisUtil;

	public ObsStorageSortDetailSummaryThread(MongoTemplate mongoTemplate,JedisUtil jedisUtil) {
		this.mongoTemplate = mongoTemplate;
		this.jedisUtil = jedisUtil;
	}

	@Override
	public void run() {
		log.info("开始汇总上周存储容量Top10");
		try {

			Date now = new Date();
			String todayStr = format.format(now);
			Date today = DateUtil.strToDate(todayStr);
			Date Monday = DateUtil.addDay(today, new int[] { 0, 0, -7 });

			Criteria criatira = new Criteria();
			criatira.andOperator(Criteria.where("timestamp").gte(Monday),
					Criteria.where("timestamp").lt(today));
			List<JSONObject> jsonList = mongoTemplate.find(new Query(criatira),
					JSONObject.class, MongoCollectionName.OBS_STORAGE_1H);
			List<JSONObject> result = new ArrayList<JSONObject>();
			for (int i = 0; i < jsonList.size(); i++) {
				JSONObject object = jsonList.get(i);
				long storage = 0;
				String bucketName = object.getString("bucket");
				String owner = object.getString("owner");
				JSONObject usage = object.getJSONObject("usage");
				if (null != usage && usage.size() > 0) {
					long storageUsed = usage.getLong("size_kb_actual");
					storage += storageUsed;
				}
				JSONObject newBucket = new JSONObject();
				newBucket.put("name", bucketName);
				newBucket.put("owner", owner);
				newBucket.put("storageUsed", storage);
				
				JSONObject obj = getJSONObject(result, owner, bucketName);
				if (obj == null) {
					result.add(newBucket);
				} else {
					long newStorage = newBucket.getLong("storageUsed");
					long oldStorage = obj.getLong("storageUsed");
					obj.put("storageUsed", newStorage + oldStorage);
				}
			}
			jedisUtil.delete(RedisKey.OBS_SORT_BY_STORAGE_USED);
			for (JSONObject object : result) {
				long storage = object.getLong("storageUsed");
				jedisUtil.addToSortedSet(RedisKey.OBS_SORT_BY_STORAGE_USED, storage,object.toJSONString());
			}

		} catch (Exception e) {
		    log.error(e.getMessage(),e);
		}
	}

	private JSONObject getJSONObject(List<JSONObject> list, String owner,
			String bucketName) {
		for (JSONObject object : list) {
			if (owner.equals(object.getString("owner"))&& bucketName.equals(object.getString("name"))) {
				return object;
			}
		}
		return null;

	}

}