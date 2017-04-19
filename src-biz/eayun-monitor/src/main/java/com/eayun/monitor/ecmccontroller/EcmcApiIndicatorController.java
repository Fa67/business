package com.eayun.monitor.ecmccontroller;

import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.monitor.ecmcservice.EcmcApiIndicatorService;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Created by Administrator on 2016/12/27.
 */
@Controller
@RequestMapping("/ecmc/monitor/resource")
public class EcmcApiIndicatorController {

    @Autowired
    private EcmcApiIndicatorService ecmcApiIndicatorService ;

    @RequestMapping(value = "/getApiIndicatorData", method = RequestMethod.POST)
    @ResponseBody
    public  Object getApiIndicatorList(@RequestBody ParamsMap paramsMap)throws Exception{




        try {
            QueryMap queryMap = new QueryMap();
            Map<String, Object> params = paramsMap.getParams();
            queryMap.setPageNum(paramsMap.getPageNumber() == null ? 1 : paramsMap.getPageNumber());
            if (paramsMap.getPageSize() != null) {
                queryMap.setCURRENT_ROWS_SIZE(paramsMap.getPageSize());
            }

            String ip = MapUtils.getString(params, "ip");
            String cusName = MapUtils.getString(params, "cusName");
            String regionName = MapUtils.getString(params, "regionName");

            String endDateString = MapUtils.getString(params, "endDate") ;
            String dataType = MapUtils.getString(params, "dataType") ;

            String orderBy = MapUtils.getString(params, "orderBy") ;
            String sort = MapUtils.getString(params, "sort");


            return this.ecmcApiIndicatorService.getApiIndicatorList(cusName, ip, regionName, orderBy,sort,endDateString, dataType, queryMap) ;
        } catch (Exception e) {
            e.printStackTrace();
            return null ;
        }
    }
}