package com.eayun.log.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.annotation.ApiMethod;
import com.eayun.common.annotation.ApiService;
import com.eayun.common.exception.ApiException;
import com.eayun.common.model.ApiServiceLog;
import com.eayun.common.constant.ApiConstant;
import com.eayun.common.model.ApiServiceLogDetail;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.ApiUtil;
import com.eayun.customer.serivce.CustomerService;
import com.eayun.datacenter.ecmcservice.EcmcDataCenterService;
import com.eayun.log.service.ApiLogService;
import com.eayun.project.service.ProjectService;
import com.eayun.virtualization.model.CloudProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import java.util.*;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@ApiService
public class ApiLogServiceImpl implements ApiLogService {

    //操作日志错误代码信息集合
    private static final String API_LOG_LIMIT_ERROR_CODE          = "100027" ;
    private static final String API_LOG_OffSET_ERROR_CODE         = "100026" ;
    private static final String API_LOG_SEARCHWORD_ERROR_CODE     = "100023" ;
    private static final String API_LOG_TIME_ERROR_CODE           = "100024" ;
    private static final String API_LOG_NOT_EXIST_DATA_ERROR_CODE = "100025" ;
    private static final String API_LOG_JOBS_FORMAT_ERROR_CODE    = "100029" ;
    private static final String API_LOG_NO_PROJECT_IN_CURRENTDC_ERROR_CODE = "100030" ;
    private static final String API_LOG_STATE_ERROR_CODE          = "100034" ;

    @Autowired
    private MongoTemplate mongoTemplate ;
    @Autowired
    private EcmcDataCenterService ecmcDataCenterService ;
    @Autowired
    private CustomerService customerService ;
    @Autowired
    private ProjectService projectService ;
    @Autowired
    private JedisUtil jedisUtil ;
    private static Logger logger = LoggerFactory.getLogger(ApiLogServiceImpl.class);

    @ApiMethod("V1/OperationLog")
    public JSONObject OperationLog(JSONObject params) throws Exception{

        // TODO: 2017/3/16 若客户在指定数据中心下无项目，则提示错误信息
        CloudProject cloudProject = projectService.queryProjectByDcAndCus(String.valueOf(params.get("DcId")), String.valueOf(params.get("CusId")));
        if(cloudProject == null){
            // TODO: 2017/3/16 客户在指定的数据中心下无项目，则抛出指定的错误信息
            throw ApiException.createApiException(API_LOG_NO_PROJECT_IN_CURRENTDC_ERROR_CODE);
        }

        // TODO: 2017/3/14 首先声明本次查询中所有全部用到的查询参数
        String SearchWord = null, Status = null, StartTime = null, EndTime = null;
        Integer Offset = 0, Limit = 0;
        List<String> Jobs = null ;
        // TODO: 2017/3/14 首先声明本次查询中所有全部用到的查询参数

        //根据指定的参数约束规则解析对应每个参数的具体内容
        Limit                  = checkLimitValue(params) ;
        Offset                 = checkOffsetValue(params) ;
        SearchWord             = checkSearchWordValue(params) ;
        Status                 = checkStatusValue(params) ;
        JSONObject timeParames = checkTimeValue(params) ;
        if (timeParames.containsKey("StartTime")){
            StartTime = timeParames.getString("StartTime") ;
        }else {StartTime = null ;}
        if (timeParames.containsKey("EndTime")){
            EndTime = timeParames.getString("EndTime") ;
        }else {EndTime = null ; }
        Jobs = checkJobsValue(params) ;

        //每个参数对应的Criteria条件，在Mongodb数据查询中使用
        Criteria SearchWordCriteria = null ,
                  StartTimeCriteria = null ,
                    EndTimeCriteria = null ,
                       JobsCriteria = null ,
                     StatusCriteria = null;

        //实例化一个根Criteria，负责管理所有的条件，是且的关系，容纳多个参数的筛选信息
        List<Criteria> andCriteria = new ArrayList<Criteria>() ;

        //查询当前客户自己的日志操作信息
        logger.info("operatorId : " + String.valueOf(params.get(ApiConstant.TAG_CUS_ID)));
        andCriteria.add(where("operatorId").is(String.valueOf(params.get(ApiConstant.TAG_CUS_ID))));

        //如果关键字不为空，按照关键字信息模糊查询
        logger.info("SearchWord : " + SearchWord);
        if (SearchWord != null) {
            //SearchWordCriteria = where("operationName").regex(Pattern.compile("^.*"+SearchWord+".*$", Pattern.CASE_INSENSITIVE));
            String content = ApiUtil.getNodeIdByKeywordSearch("V1", SearchWord) ;
            SearchWordCriteria = where("apiNameNodeId").in(
                    content == null ? new String[]{"NO_DATA"} : content.split(",")
            );
            andCriteria.add(SearchWordCriteria) ;
        }

        //如果执行状态不为空，则查询指定状态的日志信息
        logger.info("Status : " + Status);
        if (Status != null){
            StatusCriteria = where("status").is(Status);
            andCriteria.add(StatusCriteria) ;
        }

        //如果开始时间不为空，则查询所有开始时间符合条件的日志数据
        logger.info("StartTime : " + StartTime);
        if (StartTime != null){
            StartTimeCriteria = where("createTime").gte(DateUtil.getBeijingTimeByUTCTimeString(StartTime)) ;
            andCriteria.add(StartTimeCriteria) ;
        }

        //如果结束时间不为空，则查询所有结束时间符合条件的日志数据
        logger.info("EndTime : " + EndTime);
        if (EndTime != null) {
            EndTimeCriteria = where("createTime").lt(new Date(DateUtil.getBeijingTimeByUTCTimeString(EndTime).getTime() + 60000));
            andCriteria.add(EndTimeCriteria);
        }

        //如果传入的日志ID数组不为空，则查询ID编号在这个数组之内的所有日志数据
        logger.info("Jobs : " + Jobs);
        if (Jobs != null && Jobs.size() != 0){
            JobsCriteria = where("_id").in(Jobs);
            andCriteria.add(JobsCriteria);
        }

        //排除由于某些原因而导致的日志信息中无处理时间的数据
        andCriteria.add(where("takeTime").ne(null));
        Criteria baseCriteria = new Criteria() ;
        if (andCriteria.size() != 0) {
            baseCriteria.andOperator(andCriteria.toArray(new Criteria[]{}));
        }

        //所有数据按照操作时间的倒序排列
        Query query = new Query(baseCriteria).with(new Sort(Sort.Direction.DESC, "createTime"))
                .skip(Offset * Limit).limit(Limit);

        //查询全部的日志数据
        List<ApiServiceLog> apiServiceLogList = mongoTemplate.find(query, ApiServiceLog.class, ApiConstant.API_LOG_COLLECTION_NAME);

        JSONObject logServiceResult = new JSONObject() ;
        if (apiServiceLogList.size() == 0){
            throw ApiException.createApiException(API_LOG_NOT_EXIST_DATA_ERROR_CODE) ;
        }else {
            logServiceResult.put("Totalcount", apiServiceLogList.size()) ;
            //根据一定的规则重新解析结果数据，转换为符合要求的返回结果格式
            logServiceResult.put("LogSet",     reverseByArrayList(processAPIServiceLogDetail(apiServiceLogList))) ;
        }

        return logServiceResult ;
    }

    /**
     * 将基础的Log信息转换为Detail信息
     * @return
     */
    private List<ApiServiceLogDetail> processAPIServiceLogDetail(List<ApiServiceLog> origin) throws Exception{
        Map<String,String> resourceTypeNames = new HashMap<>();
        Map<String,String> apiNames = new HashMap<>();
        Map<String,String> apiOperation = new HashMap<>();
        Map<String,String> regions = new HashMap<>();
        Map<String,String> cuses = new HashMap<>();
        List<ApiServiceLogDetail> apiServiceLogDetails = new ArrayList<>() ;
        for (ApiServiceLog apiServiceLog : origin){
            // TODO: 2017/3/16 日志扩充的实体类
            ApiServiceLogDetail apiServiceLogDetail = new ApiServiceLogDetail() ;
            BeanUtils.copyProperties(apiServiceLogDetail, apiServiceLog);

            // TODO: 2017/3/16 此时不需要ResourceTYPE这个参数
            String resourceNodeId = apiServiceLog.getresourceTypeNodeId() ;
            if (resourceTypeNames.get(resourceNodeId) == null && resourceNodeId != null){
                resourceTypeNames.put(resourceNodeId, DictUtil.getDataTreeByNodeId(resourceNodeId).getNodeName());
            }
            apiServiceLogDetail.setresourceType(resourceTypeNames.get(resourceNodeId));

            String apiNodeId = apiServiceLog.getapiNameNodeId() ;
            if (apiNames.get(apiNodeId) == null && apiNodeId != null){
                apiNames.put(apiNodeId,DictUtil.getDataTreeByNodeId(apiNodeId).getNodeName());
            }
            apiServiceLogDetail.setapiName(apiNames.get(apiNodeId));
            if (apiOperation.get(apiNodeId) == null && apiNodeId != null){
                apiOperation.put(apiNodeId,DictUtil.getDataTreeByNodeId(apiNodeId).getNodeNameEn());
            }
            apiServiceLogDetail.setoperationName(apiOperation.get(apiNodeId));

            String regionId = apiServiceLog.getregionId();
            if (regions.get(regionId) == null && regionId != null){
                regions.put(regionId,ecmcDataCenterService.getdatacenterbyid(regionId).getApiDcCode());
            }
            apiServiceLogDetail.setregion(regions.get(regionId));

            String cusId = apiServiceLog.getoperatorId() ;
            if (cuses.get(cusId) == null && cusId != null){
                cuses.put(cusId,customerService.findCustomerById(cusId).getCusOrg());
            }
            apiServiceLogDetail.setoperator(cuses.get(cusId));

            apiServiceLogDetails.add(apiServiceLogDetail);
        }
        return apiServiceLogDetails;
    }

    /**
     * 检查Limit的合法性
     * @param orign
     * @return
     * @throws Exception
     */
    private Integer checkLimitValue(JSONObject orign) throws Exception {
        if (orign.containsKey("Limit")) {
            if (orign.get("Limit") instanceof Integer || orign.get("Limit") instanceof String) {
                Integer originValue = null ;
                try {
                    originValue = Integer.parseInt(String.valueOf(orign.get("Limit")));
                }catch (Exception e) {
                    if ("".equals(String.valueOf(orign.get("Limit")))){
                        return 20 ;
                    }else {
                        throw ApiException.createApiException(API_LOG_LIMIT_ERROR_CODE);
                    }
                }
                if (Integer.parseInt(String.valueOf(orign.get("Limit"))) < 0 ||
                        Integer.parseInt(String.valueOf(orign.get("Limit"))) > 100) {
                    throw ApiException.createApiException(API_LOG_LIMIT_ERROR_CODE) ;
                }else if (Integer.parseInt(String.valueOf(orign.get("Limit"))) == 0){
                    throw ApiException.createApiException(API_LOG_NOT_EXIST_DATA_ERROR_CODE) ;
                }else {
                    return originValue ;
                }
            } else {
                throw ApiException.createApiException(API_LOG_LIMIT_ERROR_CODE) ;
            }
        } else {
            //如果没有偏移量关键字的话，则默认显示的记录数应该为 20 条。
            return 20 ;
        }
    }

    /**
     * 验证偏移量值Offset值的合法性
     * @param orign
     * @return
     * @throws Exception
     */
    private Integer checkOffsetValue(JSONObject orign) throws Exception {
        if (orign.containsKey("Offset")) {
            if (orign.get("Offset") instanceof Integer || orign.get("Offset") instanceof String) {
                Integer originValue = null ;
                try {
                    originValue = Integer.parseInt(String.valueOf(orign.get("Offset")));
                }catch (Exception e){
                    if ("".equals(String.valueOf(orign.get("Offset")))){
                        return 0 ;
                    }else {
                        throw ApiException.createApiException(API_LOG_OffSET_ERROR_CODE);
                    }
                }
                if (Integer.parseInt(String.valueOf(orign.get("Offset"))) >= 0) {
                    return originValue ;
                }else {
                    throw ApiException.createApiException(API_LOG_OffSET_ERROR_CODE) ;
                }
            } else {
                throw ApiException.createApiException(API_LOG_OffSET_ERROR_CODE) ;
            }
        } else {
            return 0 ;
        }
    }

    /**
     * 检验Jobs ID数组集合的有效性
     * @param orign
     * @return
     * @throws Exception
     */
    private List<String> checkJobsValue(JSONObject orign) throws Exception {
        List<String> jobs = new ArrayList<String>() ;
        if (orign.containsKey("Jobs")) {
            if (orign.get("Jobs") instanceof JSONArray) {
                Iterator iterator = ((JSONArray)orign.get("Jobs")).iterator() ;
                while (iterator.hasNext()){
                    Object curObj = iterator.next();
                    if (String.class != curObj.getClass()){
                        throw ApiException.createApiException(API_LOG_JOBS_FORMAT_ERROR_CODE) ;
                    }else {
                        if ((String.valueOf(curObj)).length() != 24){
                            throw ApiException.createApiException(API_LOG_JOBS_FORMAT_ERROR_CODE) ;
                        }else {
                            jobs.add(String.valueOf(curObj));
                        }
                    }
                }
            } else {
                if (orign.get("Jobs") instanceof String) {
                    if ("".equals(String.valueOf(orign.get("Jobs")))){
                        return null ;
                    }else {
                        throw ApiException.createApiException(API_LOG_JOBS_FORMAT_ERROR_CODE) ;
                    }
                }else {
                    throw ApiException.createApiException(API_LOG_JOBS_FORMAT_ERROR_CODE) ;
                }
            }
        } else {
            return null ;
        }
        if (jobs.size() > 20){
            throw ApiException.createApiException(API_LOG_JOBS_FORMAT_ERROR_CODE) ;
        }
        return jobs;
    }

    /**
     * 验证时间条件的合法性
     * @param orign
     * @return
     * @throws Exception
     */
    private JSONObject checkTimeValue(JSONObject orign) throws Exception {
        JSONObject timeParams = new JSONObject();
        try {
            //UTC格式时间串
            Long start=0l,end=0l ;
            if (orign.containsKey("StartTime")){
                String startStr = String.valueOf(orign.get("StartTime")) ;
                if (!"".equals(startStr)) {
                    start = DateUtil.getBeijingTimeByUTCTimeString(startStr).getTime();
                    timeParams.put("StartTime", startStr);
                }
            }
            if (orign.containsKey("EndTime")){
                String endStr = String.valueOf(orign.get("EndTime")) ;
                if (!"".equals(endStr)) {
                    end = DateUtil.getBeijingTimeByUTCTimeString(endStr).getTime();
                    timeParams.put("EndTime", endStr);
                }
            }
            if (start != 0l && end != 0l){
                if (start > end){
                    //开始时间不能大于结束时间
                    throw ApiException.createApiException(API_LOG_TIME_ERROR_CODE) ;
                }
            }
        }catch (Exception e){
            if (ApiException.class == e.getClass()){
                throw e ;
            }else {
                throw ApiException.createApiException(API_LOG_TIME_ERROR_CODE);
            }
        }
        return timeParams ;
    }

    /**
     * 验证查询关键字的合法性
     * @param orign
     * @return
     * @throws Exception
     */
    private String checkSearchWordValue(JSONObject orign) throws Exception {
        if (orign.containsKey("SearchWord")) {
            if (orign.get("SearchWord") instanceof String) {
                String originValue = String.valueOf(orign.get("SearchWord")) ;
                if ("".equals(originValue)){
                    return null ;
                }
                if (originValue.matches("^[a-zA-Z]*") && originValue.length() <= 36) {
                    return originValue ;
                } else {
                    throw ApiException.createApiException(API_LOG_SEARCHWORD_ERROR_CODE) ;
                }
            } else {
                throw ApiException.createApiException(API_LOG_SEARCHWORD_ERROR_CODE) ;
            }
        } else {
            return null ;
        }
    }


    /**
     * 验证API执行状态关键字的合法性
     * 成功success、执行中working、失败failed
     * @param orign
     * @return
     * @throws Exception
     */
    private String checkStatusValue(JSONObject orign) throws Exception {
        if (orign.containsKey("Status")) {
            if (orign.get("Status") instanceof String) {
                String originValue = String.valueOf(orign.get("Status"));
                if ("".equals(originValue)){
                    return null ;
                }
                if ("success".equals(originValue)){
                    return "1" ;
                }else if ("working".equals(originValue)){
                    return "2" ;
                }else if ("failed".equals(originValue)){
                    return "0";
                }else {
                    throw ApiException.createApiException(API_LOG_STATE_ERROR_CODE) ;
                }
            } else {
                throw ApiException.createApiException(API_LOG_STATE_ERROR_CODE) ;
            }
        } else {
            return null ;
        }
    }


    /**
     * 将List集合项目转换为JSONArray对象实例
     * @param list
     * @return
     */
    private JSONArray reverseByArrayList(List<ApiServiceLogDetail> list){
        List<String> needShowColumns = Arrays.asList("job_Id","createTime","operationName","region","resourceType","resourceId","operatorId","status","status_time");
        JSONArray jsonArray = new JSONArray() ;
        try {
            Class<?> c = ApiServiceLogDetail.class;
            JSONObject jsonObject = null ;
            for (ApiServiceLog log : list) {
                jsonObject = new JSONObject();
                for (String field : needShowColumns){

                        if ("status_time".equals(field)){
                            Date date = (Date) c.getMethod("get" + field).invoke(log);
                            jsonObject.put(field.substring(0,1).toUpperCase() + field.substring(1), DateUtil.getUTCDateZ(date, true));
                        }else if ("createTime".equals(field)){
                            Date date = (Date) c.getMethod("get" + field).invoke(log);
                            jsonObject.put("OperationTime", DateUtil.getUTCDateZ(date, true));
                        }else if("status".equals(field)){
                            String statusString = String.valueOf(c.getMethod("get" + field).invoke(log));
                            switch (statusString){
                                case "0":
                                    jsonObject.put("Status", "failed");
                                    break;
                                case "1":
                                    jsonObject.put("Status", "success");
                                    break;
                                case "2":
                                    jsonObject.put("Status", "working");
                                    break;
                            }
                        }else if ("resourceType".equals(field)){
                            jsonObject.put("OperationType",
                                    String.valueOf(c.getMethod("getversion").invoke(log)) + "/" +
                                            String.valueOf(c.getMethod("getresourceType").invoke(log)));
                        }else {
                            jsonObject.put(field.substring(0,1).toUpperCase() + field.substring(1), c.getMethod("get" + field).invoke(log));
                        }

                }
                jsonArray.add(jsonObject) ;
            }
            return jsonArray ;
        }catch (Exception e){
            return null ;
        }
    }
}