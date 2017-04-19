package com.eayun.ecmcschedule.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.ecmcschedule.model.EcmcScheduleLog;
import com.eayun.ecmcschedule.service.EcmcScheduleLogService;

@Service
@Transactional
public class EcmcScheduleLogServiceImpl implements EcmcScheduleLogService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public Page getLogList(String triggerName, String jobName, String startTime, String endTime, String queryStr, String isSuccess,
			QueryMap queryMap) {
		Date startDate = DateUtil.timestampToDate(startTime);
		Date endDate = DateUtil.timestampToDate(endTime);

		Page page = new Page();
		int pageNum = queryMap.getPageNum();
		int pageSize = queryMap.getCURRENT_ROWS_SIZE();
		page.setPageSize(pageSize);

		Query query = new Query();
		
		Criteria criteriaTime = Criteria.where("jobStartTime").gte(startDate).lte(endDate);
		Criteria criteriaName = new Criteria();
		Criteria criteriaFlag = new Criteria();
		if(!StringUtil.isEmpty(triggerName) && !StringUtil.isEmpty(jobName)){
			criteriaName.andOperator(Criteria.where("jobName").is(jobName), 
					Criteria.where("triggerName").is(triggerName));
		}else if(!StringUtil.isEmpty(queryStr)){
			criteriaName.orOperator(Criteria.where("jobName").regex(".*?" + queryStr + ".*", "i"), 
					Criteria.where("triggerName").regex(".*?" + queryStr + ".*", "i"));
		}
		
		if (!StringUtil.isEmpty(isSuccess)) {
			criteriaFlag = Criteria.where("triggerCode").is(isSuccess);
		}
		query.addCriteria(new Criteria().andOperator(criteriaTime, criteriaName, criteriaFlag ))
			.with(new Sort(Direction.DESC, "jobStartTime"));

		long totalCount = mongoTemplate.count(query, EcmcScheduleLog.class, MongoCollectionName.LOG_SCHEDULE);
		page.setTotalCount(totalCount);
		query.skip((pageNum - 1) * pageSize);
		query.limit(pageSize);
		List<EcmcScheduleLog> logs = mongoTemplate.find(query, EcmcScheduleLog.class, MongoCollectionName.LOG_SCHEDULE);
		page.setResult(logs);
		return page;
	}

}
