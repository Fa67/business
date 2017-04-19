package com.eayun.log.ecmcsevice.impl;

import com.eayun.common.constant.RedisKey;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.model.ApiServiceLog;
import com.eayun.common.constant.ApiConstant;
import com.eayun.common.model.ApiServiceLogDetail;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.customer.serivce.CustomerService;
import com.eayun.datacenter.ecmcservice.EcmcDataCenterService;
import com.eayun.log.ecmcsevice.ApiLogInfoService;
import com.eayun.sys.model.SysDataTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class ApiLogInfoServiceImpl implements ApiLogInfoService, Serializable {

    @Autowired
    private MongoTemplate mongoTemplate ;
    @Autowired
    private EcmcDataCenterService ecmcDataCenterService ;
    @Autowired
    private CustomerService customerService ;
    @Autowired
    private JedisUtil jedisUtil ;

    /**
     * ECMC端分页查询API服务日志信息，通用日志分页查询
     * @param createStartTime   创建开始时间
     * @param createEndTime     创建结束时间
     * @param apiName           API名称
     * @param region            数据中心区域
     * @param resourceType      资源类型
     * @param operator          操作人
     * @param ip                操作人IP地址
     * @param status            执行状态
     * @param queryMap          分页参数封装
     * @return                  Page对象，包含查询结果内容
     */
    public Page getApiLogList(String createStartTime, String createEndTime, String apiName, String region, String resourceType,
                              String operator, String ip, String status,
                              QueryMap queryMap) {

        Page page = new Page();
        int pageNum = queryMap.getPageNum();
        int pageSize = queryMap.getCURRENT_ROWS_SIZE();
        page.setPageSize(pageSize);

        Query query = new Query() ;
        query.with(new Sort(Sort.Direction.DESC, "createTime")) ;

        Criteria createCRI = null ;
        if (createStartTime != null && createEndTime != null) {
            createCRI = where("createTime").gte(DateUtil.timestampToDate(createStartTime.trim()))
                    .lt(new Date(DateUtil.timestampToDate(createEndTime.trim()).getTime() + 60000));
        }
        if (createStartTime != null && createEndTime == null) {
            createCRI = where("createTime").gte(DateUtil.timestampToDate(createStartTime.trim()));
        }
        if (createStartTime == null && createEndTime != null) {
            createCRI = where("createTime").lt(new Date(DateUtil.timestampToDate(createEndTime.trim()).getTime() + 60000));
        }
        List<Criteria> criteriaList = new ArrayList<Criteria>() ;
        if (createCRI != null) {
            criteriaList.add(createCRI) ;
        }

        if (!StringUtil.isEmpty(apiName)){criteriaList.add(     where("apiNameNodeId").is(apiName.trim()));}
        // TODO: 2017/3/2 传递的数据中心ID信息可能为“-------”需要处理
        if (!StringUtil.isEmpty(region)){
            if ("-------".equals(region)){
                //此时API访问日志中没有保存正确的数据中心信息
                criteriaList.add(where("regionId").is(null));
            }else {
                criteriaList.add(where("regionId").is(region.trim()));
            }
        }
        if (!StringUtil.isEmpty(resourceType)){
            criteriaList.add(where("resourceTypeNodeId").is(resourceType));
//            criteriaList.add(where("version").is(resourceType.trim().split("/")[0]));
        }
        if (!StringUtil.isEmpty(operator)){
            if ("-------".equals(operator)){
                //此时API访问日志中没有保存正确的访问客户信息
                criteriaList.add(where("operatorId").is(null));
            }else {
                criteriaList.add(where("operatorId").is(operator.trim()));
            }
        }
        if (!StringUtil.isEmpty(ip)){criteriaList.add(          where("ip").is(ip.trim()));}
        if (!StringUtil.isEmpty(status)){criteriaList.add(      where("status").is(status.trim()));}

        if (criteriaList.size() != 0) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[]{}))) ;
        }

        long totalCount = mongoTemplate.count(query, ApiServiceLog.class, ApiConstant.API_LOG_COLLECTION_NAME);
        page.setTotalCount(totalCount);
        query.skip((pageNum - 1) * pageSize);
        query.limit(pageSize) ;

        List<ApiServiceLog> apiServiceLogList = mongoTemplate.find(query, ApiServiceLog.class, ApiConstant.API_LOG_COLLECTION_NAME);
        System.out.println(apiServiceLogList.size());

        //TODO:转换得到全部的API日志详细信息。
        try {
            page.setResult(processAPIServiceLogDetail(apiServiceLogList)) ;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return page;
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
            ApiServiceLogDetail apiServiceLogDetail = new ApiServiceLogDetail() ;
            BeanUtils.copyProperties(apiServiceLogDetail, apiServiceLog);

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
     * 查找所有的API资源类型和API名称
     * @return
     */
    @Override
    public Map<String, Object> fetchApiNamesAndResourceTypes(){
        Map<String,Object> rs = new HashMap<>();
        Map<String,String> baseEntity = null;
        //将Action值的根记录，遍历所有版本号的信息
        List<Map<String,String>> allResourceTypes = new ArrayList<>();
        List<Map<String,String>>  allApiNames      = new ArrayList<>();
        String actionRootNodeId = "0016001" ;
        List<SysDataTree> sysDataTrees = DictUtil.getDataTreeByParentId(actionRootNodeId) ;
        for (SysDataTree sysDataTree : sysDataTrees) {
            //所有的版本号信息
            for (SysDataTree sysDataTree1 : DictUtil.getDataTreeByParentId(sysDataTree.getNodeId())){
                //版本号下的所有资源类型信息
                baseEntity = new HashMap<>();
                baseEntity.put("nodeId", sysDataTree1.getNodeId());
                baseEntity.put("nodeName", sysDataTree.getNodeName() + "/" + sysDataTree1.getNodeName());
                allResourceTypes.add(baseEntity) ;
                //保存所有不重复的API名称信息
                for (SysDataTree sysDataTree2 : DictUtil.getDataTreeByParentId(sysDataTree1.getNodeId())){
                    baseEntity = new HashMap<>();
                    baseEntity.put("nodeId", sysDataTree2.getNodeId());
                    baseEntity.put("nodeName", sysDataTree.getNodeName() + "/" + sysDataTree2.getNodeName());
                    allApiNames.add(baseEntity);
                }
            }
        }
        Collections.sort(allResourceTypes, new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> o1, Map<String, String> o2) {
                return o1.get("nodeId").compareTo(o2.get("nodeId"));
            }
        });
        Collections.sort(allApiNames, new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> o1, Map<String, String> o2) {
                return o1.get("nodeId").compareTo(o2.get("nodeId"));
            }
        });
        rs.put("allResourceTypes",     allResourceTypes);
        rs.put("allApiNames",          allApiNames);
        return rs;
    }

    @Override
    public List<Map<String,String>> getApiNamesByVersion(String typeName) {
        String actionRootNodeId = "0016001" ;
        List<Map<String,String>> apiNames = new ArrayList<>();
        Map<String,String> baseEntity = null;
        //获取所有的版本号信息。
        if (StringUtil.isEmpty(typeName)){
            for (SysDataTree sysDataTree : DictUtil.getDataTreeByParentId(actionRootNodeId)) {
                for (SysDataTree sysDataTree1 : DictUtil.getDataTreeByParentId(sysDataTree.getNodeId())){
                    for (SysDataTree sysDataTree2 : DictUtil.getDataTreeByParentId(sysDataTree1.getNodeId())){
                        baseEntity = new HashMap<>();
                        baseEntity.put("nodeId", sysDataTree2.getNodeId());
                        baseEntity.put("nodeName", sysDataTree.getNodeName() + "/" + sysDataTree2.getNodeName());
                        apiNames.add(baseEntity);
                    }
                }
            }
        }else {
            for (SysDataTree sysDataTree : DictUtil.getDataTreeByParentId(actionRootNodeId)) {
                for (SysDataTree sysDataTree1 : DictUtil.getDataTreeByParentId(sysDataTree.getNodeId())){
                    if (typeName.equals(sysDataTree1.getNodeId())) {
                        for (SysDataTree sysDataTree2 : DictUtil.getDataTreeByParentId(sysDataTree1.getNodeId())) {
                            baseEntity = new HashMap<>();
                            baseEntity.put("nodeId", sysDataTree2.getNodeId());
                            baseEntity.put("nodeName", sysDataTree.getNodeName() + "/" +  sysDataTree2.getNodeName());
                            apiNames.add(baseEntity);
                        }
                    }
                }
            }
        }
        Collections.sort(apiNames, new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> o1, Map<String, String> o2) {
                return o1.get("nodeId").compareTo(o2.get("nodeId"));
            }
        });
        return apiNames;
    }
}