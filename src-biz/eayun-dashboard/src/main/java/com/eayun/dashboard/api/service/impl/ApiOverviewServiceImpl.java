package com.eayun.dashboard.api.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.ApiConstant;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.customer.model.Customer;
import com.eayun.dashboard.api.bean.ApiIndexDetail;
import com.eayun.dashboard.api.dao.ApiSwitchDao;
import com.eayun.dashboard.api.service.ApiOverviewService;

@Service
@Transactional
public class ApiOverviewServiceImpl implements ApiOverviewService {
	
	private static final Logger   log = LoggerFactory.getLogger(ApiOverviewServiceImpl.class);

	@Autowired
    private MongoTemplate mongoTemplate;
	
	@Autowired
	private ApiSwitchDao apiSwitchDao;

	@Override
	public List<ApiIndexDetail> getApiOverviewDetails(String cusId,
			Date startTime, Date endTime) {
		log.info("查看客户api指标概览折线图实现...");
		List<ApiIndexDetail> list = new ArrayList<ApiIndexDetail>();
		List<Long> countList = new ArrayList<Long>();
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String strStart = format.format(startTime);
		String strEnd = format.format(endTime);
        try {
        	startTime = format.parse(strStart);
        	endTime = format.parse(strEnd);
        } catch (ParseException e) {
            log.error(e.getMessage(),e);
        }
        long hours = DateUtil.dayToDay(startTime, endTime);
        int days = (int) (hours/24)+1;		//相差天数+1，代表总天数
        Date firstDate = startTime;
        if(days <= 5){
        	for(int i = 1;i < 7;i++){
        		ApiIndexDetail apiIndexDetail = new ApiIndexDetail();
        		Aggregation success = Aggregation.newAggregation(
                        Aggregation.match(Criteria.where("operatorId").is(cusId)),
                        Aggregation.match(Criteria.where("createTime").gte(firstDate)),
                        Aggregation.match(Criteria.where("createTime").lt(DateUtil.addDay(firstDate,new int[]{0, 0, 0, days*4}))),
                        Aggregation.match(Criteria.where("status").is("1")),
                        Aggregation.group().count().as("successCount")
                        );
                AggregationResults<JSONObject> successresult = mongoTemplate.aggregate(success,ApiConstant.API_LOG_COLLECTION_NAME, JSONObject.class);
                List<JSONObject> successlist = successresult.getMappedResults();
                long successCount = 0;
                if(successlist.size() > 0){
                	successCount = successlist.get(0).getLong("successCount");
                }
                
                Aggregation fail = Aggregation.newAggregation(
                        Aggregation.match(Criteria.where("operatorId").is(cusId)),
                        Aggregation.match(Criteria.where("createTime").gte(firstDate)),
                        Aggregation.match(Criteria.where("createTime").lt(DateUtil.addDay(firstDate,new int[]{0, 0, 0, days*4}))),
                        Aggregation.match(Criteria.where("status").is("0")),
                        Aggregation.group().count().as("failCount")
                        );
                AggregationResults<JSONObject> failresult = mongoTemplate.aggregate(fail,ApiConstant.API_LOG_COLLECTION_NAME, JSONObject.class);
                List<JSONObject> faillist = failresult.getMappedResults();
                long failCount = 0;
                if(faillist.size() > 0){
                	failCount = faillist.get(0).getLong("failCount");
                }
                
                Aggregation total = Aggregation.newAggregation(
                        Aggregation.match(Criteria.where("operatorId").is(cusId)),
                        Aggregation.match(Criteria.where("createTime").gte(firstDate)),
                        Aggregation.match(Criteria.where("createTime").lt(DateUtil.addDay(firstDate,new int[]{0, 0, 0, days*4}))),
                        Aggregation.group().count().as("totalCount")
                        );
                AggregationResults<JSONObject> totalresult = mongoTemplate.aggregate(total,ApiConstant.API_LOG_COLLECTION_NAME, JSONObject.class);
                List<JSONObject> totallist = totalresult.getMappedResults();
                long totalCount = 0;
                if(totallist.size() > 0){
                	totalCount = totallist.get(0).getLong("totalCount");
                }
                apiIndexDetail.setCusId(cusId);
                apiIndexDetail.setSuccessCount(successCount);
                apiIndexDetail.setFailCount(failCount);
                apiIndexDetail.setTotalCount(totalCount);
                apiIndexDetail.setTimestamp(DateUtil.addDay(firstDate,new int[]{0, 0, 0, days*4}));
                list.add(apiIndexDetail);
                countList.add(successCount);
                countList.add(failCount);
                countList.add(totalCount);
        		firstDate = DateUtil.addDay(firstDate,new int[]{0, 0, 0, days*4});
        	}
        }else{
        	for(int i = 0;i < days;i++){
        		ApiIndexDetail apiIndexDetail = new ApiIndexDetail();
        		Aggregation success = Aggregation.newAggregation(
                        Aggregation.match(Criteria.where("operatorId").is(cusId)),
                        Aggregation.match(Criteria.where("createTime").gte(firstDate)),
                        Aggregation.match(Criteria.where("createTime").lt(DateUtil.addDay(firstDate,new int[]{0, 0, 1}))),
                        Aggregation.match(Criteria.where("status").is("1")),
                        Aggregation.group().count().as("successCount")
                        );
                AggregationResults<JSONObject> successresult = mongoTemplate.aggregate(success,ApiConstant.API_LOG_COLLECTION_NAME, JSONObject.class);
                List<JSONObject> successlist = successresult.getMappedResults();
                long successCount = 0;
                if(successlist.size() > 0){
                	successCount = successlist.get(0).getLong("successCount");
                }
                
                Aggregation fail = Aggregation.newAggregation(
                        Aggregation.match(Criteria.where("operatorId").is(cusId)),
                        Aggregation.match(Criteria.where("createTime").gte(firstDate)),
                        Aggregation.match(Criteria.where("createTime").lt(DateUtil.addDay(firstDate,new int[]{0, 0, 1}))),
                        Aggregation.match(Criteria.where("status").is("0")),
                        Aggregation.group().count().as("failCount")
                        );
                AggregationResults<JSONObject> failresult = mongoTemplate.aggregate(fail,ApiConstant.API_LOG_COLLECTION_NAME, JSONObject.class);
                List<JSONObject> faillist = failresult.getMappedResults();
                long failCount = 0;
                if(faillist.size() > 0){
                	failCount = faillist.get(0).getLong("failCount");
                }
                
                Aggregation total = Aggregation.newAggregation(
                        Aggregation.match(Criteria.where("operatorId").is(cusId)),
                        Aggregation.match(Criteria.where("createTime").gte(firstDate)),
                        Aggregation.match(Criteria.where("createTime").lt(DateUtil.addDay(firstDate,new int[]{0, 0, 1}))),
                        Aggregation.group().count().as("totalCount")
                        );
                AggregationResults<JSONObject> totalresult = mongoTemplate.aggregate(total,ApiConstant.API_LOG_COLLECTION_NAME, JSONObject.class);
                List<JSONObject> totallist = totalresult.getMappedResults();
                long totalCount = 0;
                if(totallist.size() > 0){
                	totalCount = totallist.get(0).getLong("totalCount");
                }
                apiIndexDetail.setCusId(cusId);
                apiIndexDetail.setSuccessCount(successCount);
                apiIndexDetail.setFailCount(failCount);
                apiIndexDetail.setTotalCount(totalCount);
                apiIndexDetail.setTimestamp(firstDate);
                list.add(apiIndexDetail);
                countList.add(successCount);
                countList.add(failCount);
                countList.add(totalCount);
        		firstDate = DateUtil.addDay(firstDate,new int[]{0, 0, 1});
        	}
        }        
        long max = Collections.max(countList);
        long min = Collections.min(countList);
        list.get(0).setMaxCount(max);
        list.get(0).setMinCount(min);
		return list;
	}

	@Override
	public Page getApiDetailsPage(Page page, QueryMap queryMap, String cusId,
			Date startTime, Date endTime) {
		log.info("查看客户api指标信息列表实现...");
		List<ApiIndexDetail> list = new ArrayList<ApiIndexDetail>();
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String strStart = format.format(startTime);
		String strEnd = format.format(endTime);
        try {
        	startTime = format.parse(strStart);
        	endTime = format.parse(strEnd);
        } catch (ParseException e) {
            log.error(e.getMessage(),e);
        }
        long hours = DateUtil.dayToDay(startTime, endTime);
        int days = (int) (hours/24)+1;		//相差天数+1，代表总天数
        Date firstDate = endTime;
        for(int i = 0;i < days;i++){
        	ApiIndexDetail apiIndexDetail = new ApiIndexDetail();
    		Aggregation success = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("operatorId").is(cusId)),
                    Aggregation.match(Criteria.where("createTime").gte(firstDate)),
                    Aggregation.match(Criteria.where("createTime").lt(DateUtil.addDay(firstDate,new int[]{0, 0, 1}))),
                    Aggregation.match(Criteria.where("status").is("1")),
                    Aggregation.group().count().as("successCount")
                    );
            AggregationResults<JSONObject> successresult = mongoTemplate.aggregate(success,ApiConstant.API_LOG_COLLECTION_NAME, JSONObject.class);
            List<JSONObject> successlist = successresult.getMappedResults();
            long successCount = 0;
            if(successlist.size() > 0){
            	successCount = successlist.get(0).getLong("successCount");
            }
            
            Aggregation fail = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("operatorId").is(cusId)),
                    Aggregation.match(Criteria.where("createTime").gte(firstDate)),
                    Aggregation.match(Criteria.where("createTime").lt(DateUtil.addDay(firstDate,new int[]{0, 0, 1}))),
                    Aggregation.match(Criteria.where("status").is("0")),
                    Aggregation.group().count().as("failCount")
                    );
            AggregationResults<JSONObject> failresult = mongoTemplate.aggregate(fail,ApiConstant.API_LOG_COLLECTION_NAME, JSONObject.class);
            List<JSONObject> faillist = failresult.getMappedResults();
            long failCount = 0;
            if(faillist.size() > 0){
            	failCount = faillist.get(0).getLong("failCount");
            }
            
            Aggregation total = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("operatorId").is(cusId)),
                    Aggregation.match(Criteria.where("createTime").gte(firstDate)),
                    Aggregation.match(Criteria.where("createTime").lt(DateUtil.addDay(firstDate,new int[]{0, 0, 1}))),
                    Aggregation.group().count().as("totalCount")
                    );
            AggregationResults<JSONObject> totalresult = mongoTemplate.aggregate(total,ApiConstant.API_LOG_COLLECTION_NAME, JSONObject.class);
            List<JSONObject> totallist = totalresult.getMappedResults();
            long totalCount = 0;
            if(totallist.size() > 0){
            	totalCount = totallist.get(0).getLong("totalCount");
            }
            apiIndexDetail.setCusId(cusId);
            apiIndexDetail.setSuccessCount(successCount);
            apiIndexDetail.setFailCount(failCount);
            apiIndexDetail.setTotalCount(totalCount);
            apiIndexDetail.setTimestamp(firstDate);
            list.add(apiIndexDetail);
    		firstDate = DateUtil.addDay(firstDate,new int[]{0, 0, -1});
        }
        
        int pageSize = queryMap.getCURRENT_ROWS_SIZE();
        int pageNumber = queryMap.getPageNum();
        
        int start = (pageNumber-1)*pageSize;
        List<ApiIndexDetail> resultList = new ArrayList<ApiIndexDetail>();
        if(list.size()>0){
            int end = start+pageSize;
            resultList = list.subList(start, end < list.size()?end:list.size());
        }
        page = new Page(start, list.size(), pageSize, resultList);
		return page;
	}

	/**
	 * 客户注册流程状态变化：
	 * ①、客户在ECSC提交注册工单后，客户表新增记录，用户表无记录，cus_flag=0,cus_org=null
	 * ②、ECMC通过审核，无用户记录，cus_flag=1,cus_org=null
	 * ③、ECMC创建客户及项目，增加用户记录，cus_flag=1,cus_org=创建时的客户名称，cus_number=登录名
	 * @param cusOrg
	 * @return
	 */
	@Override
	public List<Customer> getCusListByOrg(String cusOrg) {
		List<Customer> customerList = new ArrayList<Customer>();
		List<Object> list = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer("SELECT cus.cus_id,cus.cus_name,cus.cus_cpname,cus.cus_org "
				+ "FROM sys_selfcustomer cus WHERE cus.cus_falg = '1' AND cus.cus_org IS NOT NULL ");
		if(!StringUtil.isEmpty(cusOrg)){
			cusOrg = cusOrg.replaceAll("\\_", "\\\\_");
			hql.append(" AND cus.cus_org LIKE ? ");
			list.add("%" + cusOrg + "%");
		}
		hql.append(" ORDER BY cus.cus_org");
		javax.persistence.Query query = apiSwitchDao.createSQLNativeQuery(hql.toString(), list.toArray());
		List datalist = (List) query.getResultList();
        int a = datalist.size();
        for (int i = 0; i < datalist.size(); i++) {
            Object[] objs = (Object[]) datalist.get(i);
            Customer customer = new Customer();
            customer.setCusId(ObjectUtils.toString(objs[0], null));
            customer.setCusName(ObjectUtils.toString(objs[1], null));
            customer.setCusCpname(ObjectUtils.toString(objs[2], null));
            customer.setCusOrg(ObjectUtils.toString(objs[3], null));
            customerList.add(customer);
        }
		return customerList;
	}

}
