package com.eayun.monitor.ecmcservice;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.monitor.model.ApiMonitorDataDetail;

import java.util.List;

public interface EcmcApiIndicatorService {

    /**
     * 分页查找并显示实时指标监测数据
     * @param cus           客户
     * @param ip            IP
     * @param region        数据中心
     * @param sortColumn    排序列
     * @param sortDirection 排序方向
     * @param endDataString 历史数据截止时间
     * @param dataType      即时数据选择范围
     * @param queryMap      分页等参数封装
     * @return
     * @throws Exception
     */
    Page getApiIndicatorList(String cus, String ip, String region , String sortColumn, String sortDirection, String endDataString, String dataType , QueryMap queryMap) throws Exception;

    /**
     * 查询所有的实时监测数据
     * @return
     */
    List<ApiMonitorDataDetail> findAllApiIndicatorData();
}