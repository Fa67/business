package com.eayun.monitor.ecmcservice.impl;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.customer.model.BaseCustomer;
import com.eayun.customer.model.Customer;
import com.eayun.customer.serivce.CustomerService;
import com.eayun.datacenter.ecmcservice.EcmcDataCenterService;
import com.eayun.monitor.ecmcservice.EcmcApiIndicatorService;
import com.eayun.monitor.model.ApiMonitorData;
import com.eayun.monitor.model.ApiMonitorDataDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
@Transactional
public class EcmcApiIndicatorServiceImpl implements EcmcApiIndicatorService {

    @Autowired
    private MongoTemplate mongoTemplate ;
    @Autowired
    private EcmcDataCenterService ecmcDataCenterService ;
    @Autowired
    private CustomerService customerService ;

    @Override
    public Page getApiIndicatorList(String cus, String ip, String region, String sortColumn, String sortDirection,String endDataString, String dataType , QueryMap queryMap) throws Exception {


        Page page = new Page();
        int pageNum  = queryMap.getPageNum();
        int pageSize = queryMap.getCURRENT_ROWS_SIZE();
        page.setPageSize(pageSize);

        Query query = new Query() ;
        if (!StringUtil.isEmpty(cus)){
            //客户不为空
            List<BaseCustomer> customers = customerService.findCustomersByNameKeyword(cus);
            if (customers != null && customers.size() != 0) {
                //现在设定的是“客户名称、IP”两个条件都是精确查找
                List<String> cusIds = new ArrayList<>() ;
                for (BaseCustomer baseCustomer : customers){
                    cusIds.add(baseCustomer.getCusId()) ;
                }
                query.addCriteria(Criteria.where("cusId").in(cusIds)) ;
            }else {
                query.addCriteria(Criteria.where("cusId").is("-----")) ;
            }
        }
        if (!StringUtil.isEmpty(ip)){
            //ip信息不为空
            query.addCriteria(Criteria.where("ip").is(ip)) ;
        }
        if (!StringUtil.isEmpty(region)) {
            //数据中心不为空
            query.addCriteria(Criteria.where("region").is(region));
        }
        if ("real-time".equals(dataType)){
            //TODO:如果是实时数据，则查询在Mongo中按照时间倒序排列的第一个值，求出指定时间最大值
            ApiMonitorData apiMonitorData = mongoTemplate.findOne(new Query().with(new Sort(Sort.Direction.DESC,
                    "timestamp")).limit(1),ApiMonitorData.class) ;
            if (apiMonitorData != null) {
                //若指定的Mongo集合不存在，则不添加任何的条件，此时的结果肯定为空
                query.addCriteria(where("timestamp").is(apiMonitorData.getTimestamp()));
            }
        }else {
            Date historyEnd = DateUtil.timestampToDate(endDataString) ;
            //设定了开始时间和结束时间，查找指定时间范围内的数据
            Date trueEndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(historyEnd) + ":00");
            Date trueStartDate = new Date(trueEndDate.getTime() - (Integer.parseInt(dataType) - 1) * 60 * 1000L) ;
            String a = DateUtil.dateToStr(trueStartDate);
            String aa = DateUtil.dateToStr(trueEndDate);
            query.addCriteria(new Criteria().andOperator(
                Criteria.where("timestamp").gte(trueStartDate), Criteria.where("timestamp").lte(trueEndDate)
            )) ;
        }

        if ("real-time".equals(dataType)) {
            if (!StringUtil.isEmpty(sortColumn)) {
                String columnName = null;
                Sort sort = null;
                switch (sortColumn) {
                    case "Keyong":
                        columnName = "availability";
                        break;
                    case "Right":
                        columnName = "correct";
                        break;
                    case "RequestNumber":
                        columnName = "requestsNumber";
                        break;
                    case "DealTime":
                        columnName = "avgdealTime";
                        break;
                }
                switch (sortDirection) {
                    case "ASC":
                        sort = new Sort(Sort.Direction.ASC, columnName);
                        break;
                    case "DESC":
                        sort = new Sort(Sort.Direction.DESC, columnName);
                        break;
                }
                query.with(sort);
            } else {
                query.with(new Sort(Sort.Direction.DESC, "timestamp"));
            }
        }

        if ("real-time".equals(dataType)) {
            long totalCount = mongoTemplate.count(query, ApiMonitorData.class);
            page.setTotalCount(totalCount);
            query.skip((pageNum - 1) * pageSize);
            query.limit(pageSize);
            List<ApiMonitorData> apiServiceLogList = mongoTemplate.find(query, ApiMonitorData.class);
            try {
                page.setResult(processApiMonitorData(apiServiceLogList));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            List<ApiMonitorData> apiServiceLogList = mongoTemplate.find(query, ApiMonitorData.class);
            Map<String, Map<String,Long>> baseApiData = new HashMap<>();

            for (ApiMonitorData data : apiServiceLogList){

                String weidu = data.getCusId() + ":" + data.getIp() + ":" + data.getRegion() ;
                if (baseApiData.get(weidu) == null){
                    Map<String, Long> meta = new HashMap<>() ;
                    meta.put("available", data.getAvailabilityfz()) ;
                    meta.put("correct", data.getCorrectfz());
                    meta.put("requestTime", data.getAvgDealTimefz());
                    meta.put("requestNumber", data.getRequestsNumber()) ;
                    baseApiData.put(weidu, meta) ;
                }else {
                    Map<String, Long> meta = baseApiData.get(weidu) ;
                    meta.put("available",     meta.get("available")     + data.getAvailabilityfz()) ;
                    meta.put("correct",       meta.get("correct")       + data.getCorrectfz());
                    meta.put("requestTime",   meta.get("requestTime")   + data.getAvgDealTimefz());
                    meta.put("requestNumber", meta.get("requestNumber") + data.getRequestsNumber()) ;
                    baseApiData.put(weidu, meta) ;
                }
            }

            List<ApiMonitorData> caculateRs = new ArrayList<>() ;
            for (Map.Entry<String, Map<String,Long>> base : baseApiData.entrySet()){
                ApiMonitorData apiMonitorData = new ApiMonitorData() ;
                apiMonitorData.setCusId(base.getKey().split(":")[0]);
                apiMonitorData.setIp(base.getKey().split(":")[1]);
                apiMonitorData.setRegion(base.getKey().split(":")[2]);
                apiMonitorData.setAvailability(  base.getValue().get("available") * 100.0/base.getValue().get("requestNumber")   );
                apiMonitorData.setCorrect(       base.getValue().get("correct")   * 100.0/base.getValue().get("requestNumber")   );
                apiMonitorData.setAvgdealTime( base.getValue().get("requestTime") * 1.0/base.getValue().get("requestNumber")   );
                apiMonitorData.setRequestsNumber(                                         base.getValue().get("requestNumber")   );
                caculateRs.add(apiMonitorData);
            }

            String fieldName = null ;
            String fieldFetchMethodName = null ;
            String sort = null;
            if (!StringUtil.isEmpty(sortColumn)) {
                switch (sortColumn) {
                    case "Keyong":
                        fieldName = "availability" ;
                        break;
                    case "Right":
                        fieldName = "correct";
                        break;
                    case "RequestNumber":
                        fieldName = "requestsNumber";
                        break;
                    case "DealTime":
                        fieldName = "avgdealTime";
                        break;
                }
                fieldFetchMethodName = "get" + fieldName.substring(0,1).toUpperCase() + fieldName.substring(1) ;
                switch (sortDirection) {
                    case "ASC":
                        sort = "ASC" ;
                        break;
                    case "DESC":
                        sort = "DESC" ;
                        break;
                }
                final String mName = fieldFetchMethodName ;
                final String sortTag = sort ;
                Collections.sort(caculateRs, new Comparator<ApiMonitorData>() {
                    @Override
                    public int compare(ApiMonitorData o1, ApiMonitorData o2) {
                        try {
                            Method method = ApiMonitorData.class.getMethod(mName);
                            Double o1Value = Double.parseDouble(String.valueOf(method.invoke(o1))) ;
                            Double o2Value = Double.parseDouble(String.valueOf(method.invoke(o2))) ;
                            if ("ASC".equals(sortTag)){
                                //倒序排列
                                if (o1Value > o2Value){
                                    return 1 ;
                                }else if (o1Value < o2Value){
                                    return -1 ;
                                }else {
                                    return 0 ;
                                }
                            }else {
                                //将序排列
                                if (o1Value > o2Value){
                                    return -1 ;
                                }else if (o1Value < o2Value){
                                    return 1 ;
                                }else {
                                    return 0 ;
                                }
                            }
                        }catch (Exception e){
                            return 0 ;
                        }
                    }
                });
            }

            page.setTotalCount(caculateRs.size());
            List<ApiMonitorData> pageQueryResult = new ArrayList<>() ;
            int position = 0 ;
            if (caculateRs.size() < (pageSize + (pageNum - 1) * pageSize)){
                position = caculateRs.size() ;
            }else {
                position = pageSize + (pageNum - 1) * pageSize ;
            }
            for (int foot = ((pageNum - 1) * pageSize) ; foot < position ; foot ++ ){
                pageQueryResult.add(caculateRs.get(foot));
            }

            page.setResult(processApiMonitorData(pageQueryResult));

        }



        return page;

    }

    @Override
    public List<ApiMonitorDataDetail> findAllApiIndicatorData() {
        List<ApiMonitorData> apiMonitorDataList = mongoTemplate.findAll(ApiMonitorData.class);
        return processApiMonitorData(apiMonitorDataList);
    }

    private List<ApiMonitorDataDetail> processApiMonitorData(List<ApiMonitorData> origin){
        Map<String,String> regions = new HashMap<>();
        Map<String,String> cuses = new HashMap<>();
        List<ApiMonitorDataDetail> apiMonitorDataDetails = new ArrayList<>() ;
        for (ApiMonitorData apiMonitorData : origin){
            ApiMonitorDataDetail apiMonitorDataDetail = new ApiMonitorDataDetail() ;
            try {
                BeanUtils.copyProperties(apiMonitorDataDetail, apiMonitorData);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String regionId = apiMonitorData.getRegion();
            if ("-".equals(regionId)){
                regions.put(regionId,"----");
            }else if (regions.get(regionId) == null && regionId != null){
                regions.put(regionId,ecmcDataCenterService.getdatacenterbyid(regionId).getApiDcCode());
            }
            apiMonitorDataDetail.setRegionName(regions.get(regionId));
            String cusId = apiMonitorData.getCusId() ;
            if ("-".equals(cusId)){
                cuses.put(cusId,"----");
            }else if (cuses.get(cusId) == null && cusId != null){
                cuses.put(cusId,customerService.findCustomerById(cusId).getCusOrg());
            }
            apiMonitorDataDetail.setCusName(cuses.get(cusId));
            apiMonitorDataDetails.add(apiMonitorDataDetail);
        }
        return apiMonitorDataDetails;
    }

    public static void main(String[] args) throws Exception {
        long num = 1000L ;
        System.out.println(Double.parseDouble(String.valueOf(num)));
    }
}