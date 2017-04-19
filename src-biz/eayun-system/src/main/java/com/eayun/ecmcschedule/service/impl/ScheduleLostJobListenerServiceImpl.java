package com.eayun.ecmcschedule.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.ecmcschedule.model.ScheduleLostJobMongoInfo;
import com.eayun.ecmcschedule.service.ScheduleLostJobListenerService;
import com.eayun.sys.model.SysDataTree;

@Service
public class ScheduleLostJobListenerServiceImpl implements
		ScheduleLostJobListenerService {

	@Autowired
	private MongoTemplate mongoTemplate ;
	public static final String MONITOR_SCHEDULED_TASK_ROOT_KEY = "0014" ;
	private static final Logger log = LoggerFactory.getLogger(ScheduleLostJobListenerServiceImpl.class) ;
	@Override
	public Page getTaskList(ParamsMap paramsMap) {
		log.info("分页获取系统计划任务漏跑信息");
		List<String> allMonitorScheduledTaskName = new ArrayList<>() ;
		List<SysDataTree> allMonitorScheduledTask = DictUtil
				.getDataTreeByParentId(MONITOR_SCHEDULED_TASK_ROOT_KEY) ;
		if(allMonitorScheduledTask != null && allMonitorScheduledTask.size() != 0){
			for (SysDataTree sysDataTree : allMonitorScheduledTask){
				allMonitorScheduledTaskName.add(sysDataTree.getPara2().trim()) ;
			}
		}
		Query query = new Query() ;
		QueryMap queryMap = new QueryMap();
		Map<String, Object> params = paramsMap.getParams();
		queryMap.setPageNum(paramsMap.getPageNumber() == null ? 1 : paramsMap.getPageNumber());
		if (paramsMap.getPageSize() != null) {
			queryMap.setCURRENT_ROWS_SIZE(paramsMap.getPageSize());
		}
		Page page = new Page();
		int pageNum = queryMap.getPageNum();
		int pageSize = queryMap.getCURRENT_ROWS_SIZE();
		page.setPageSize(pageSize);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;
		String jname = MapUtils.getString(params, "jname") ;
		Date begin = DateUtil.timestampToDate(MapUtils.getString(params, "beginTime"));
		Date end = DateUtil.timestampToDate(MapUtils.getString(params, "endTime"));
		String beginTime = null ;
		try {
			beginTime = format.format(end) ;
		} catch (Exception e) {
		}
		String endTime = null ;
		try {
			endTime = format.format(begin);
		} catch (Exception e) {
		}
		Criteria criteria1 = new Criteria() ;
		Criteria criteria2 = new Criteria() ;
		if(beginTime != null && endTime != null){
			criteria1 = new Criteria().norOperator(Criteria.where("firstTime").gt(beginTime) , 
					Criteria.where("endTime").lt(endTime));
		}
		if(beginTime != null && endTime == null){
			criteria1 = new Criteria().norOperator(Criteria.where("firstTime").gt(beginTime));
		}
		if(beginTime == null && endTime != null){
			criteria1 = new Criteria().norOperator(Criteria.where("endTime").lt(endTime));
		}
		if(!StringUtil.isEmpty(jname)){
			criteria2 = Criteria.where("jobName").regex(".*?" +jname+ ".*");
		}
		
		if(allMonitorScheduledTaskName.size() != 0){
			Criteria criteria3 = Criteria.where("jobName").in(allMonitorScheduledTaskName) ;
			query.addCriteria(new Criteria().andOperator(criteria1,criteria2,criteria3)) ;
		}else{
			query.addCriteria(new Criteria().andOperator(criteria1,criteria2)) ;
		}
		
		//查询数据(数据总数目以及数据列表)
		long totalCount = mongoTemplate.count(query, ScheduleLostJobMongoInfo.class, 
					MongoCollectionName.SCHEDULE_LOST_JOB);
		page.setTotalCount(totalCount);
		query.skip((pageNum - 1) * pageSize);
		query.limit(pageSize);
		List<ScheduleLostJobMongoInfo> jobMongoInfos = mongoTemplate.find(
				query.with(new Sort(Direction.DESC, "firstTime", "jobName")), 
				ScheduleLostJobMongoInfo.class, 
				MongoCollectionName.SCHEDULE_LOST_JOB) ;
		page.setResult(jobMongoInfos);
		return page ;
	}
}