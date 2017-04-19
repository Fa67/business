package com.eayun.log.ecmccontroller;

import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.customer.serivce.CustomerService;
import com.eayun.log.ecmcsevice.ApiLogInfoService;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/ecmc/system/api/log")
public class ApiLogInfoController {

    @Autowired
    private ApiLogInfoService apiLogInfoService;

    @RequestMapping(value = "/getloglist")
    @ResponseBody
    public Object getApiLogList(@RequestBody ParamsMap paramsMap)throws Exception{
        try {
            QueryMap queryMap = new QueryMap();
            Map<String, Object> params = paramsMap.getParams();
            queryMap.setPageNum(paramsMap.getPageNumber() == null ? 1 : paramsMap.getPageNumber());
            if (paramsMap.getPageSize() != null) {
                queryMap.setCURRENT_ROWS_SIZE(paramsMap.getPageSize());
            }

            String createStartTime = MapUtils.getString(params, "createStartTime");
            String createEndTime =   MapUtils.getString(params, "createEndTime");
            String apiName =         MapUtils.getString(params, "apiName");
            String region =          MapUtils.getString(params, "region");
            String resourceType =    MapUtils.getString(params, "resourceType");
            String operator =        MapUtils.getString(params, "operator");
            String ip =              MapUtils.getString(params, "ip");
            String status =          MapUtils.getString(params, "status");

            return this.apiLogInfoService.getApiLogList(
                    createStartTime, createEndTime, apiName,         region,        resourceType,
                    operator       , ip,            status,          queryMap
            ) ;

        } catch (Exception e) {
            e.printStackTrace();
            return null ;
        }
    }

    @RequestMapping(value = "/fetchApiNamesAndResourceTypes")
    @ResponseBody
    public Object fetchApiNamesAndResourceTypes()throws Exception{
        return this.apiLogInfoService.fetchApiNamesAndResourceTypes();
    }

    @RequestMapping(value = "/getApiNamesByVersion")
    @ResponseBody
    public Object getApiNamesByVersion(@RequestBody Map map)throws Exception{
        List<Map<String,String>> rs = this.apiLogInfoService.getApiNamesByVersion(
                String.valueOf(map.get("type"))
        );
        rs.size();
        return rs;
    }
}