package com.eayun.log.ecmcsevice;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ApiLogInfoService {

    /**
     * ECMC多字段多条件查询日志信息。
     * @param createStartTime   创建开始时间
     * @param createEndTime     创建结束时间
     * @param apiName           API名称
     * @param region            数据中心区域
     * @param resourceType      资源类型
     * @param operator          操作人
     * @param ip                操作人IP地址
     * @param status            执行状态
     * @param queryMap          分页参数封装
     * @return                  日志信息的集合
     */
    Page getApiLogList(String createStartTime, String createEndTime, String apiName, String region,
                       String resourceType, String operator, String ip, String status,
                       QueryMap queryMap) ;

    /**
     * 查询所有的API名称以及所有的资源类型名称
     * @return
     */
    Map<String,Object> fetchApiNamesAndResourceTypes();

    /**
     * 根据指定的版本号查询所有的API名称信息
     * @return
     */
    List<Map<String,String>> getApiNamesByVersion(String typeName);

}