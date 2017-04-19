package com.eayun.eayunstack.service.impl;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.eayunstack.service.OpenstackMeterService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;

@Service
public class OpenstackMeterServiceImpl extends OpenstackBaseServiceImpl implements
                                                                       OpenstackMeterService {

    @Override
    public JSONArray getNetworkFlow(String meter, String dcId, String projectId, String startTime,
                                    String endTime) {
        
        RestTokenBean restTokenBean = getRestTokenBean(dcId, projectId,
            OpenstackUriConstant.METER_SERVICE_URI);
        
        restTokenBean.setUrl(OpenstackUriConstant.QUERY_SAMPLES_URI);
        
        JSONObject meterDataJson = new JSONObject();
        meterDataJson.put("meter", meter);
        JSONObject meterJson = new JSONObject();
        meterJson.put("=", meterDataJson);
        
        JSONObject projectDataJson = new JSONObject();
        projectDataJson.put("project_id", projectId);
        JSONObject projectJson = new JSONObject();
        projectJson.put("=", projectDataJson);
        
        JSONObject starttimeDataJson = new JSONObject();
        starttimeDataJson.put("timestamp", startTime);
        JSONObject starttimeJson = new JSONObject();
        starttimeJson.put(">=", starttimeDataJson);
        
        JSONObject endtimeDataJson = new JSONObject();
        endtimeDataJson.put("timestamp", endTime);
        JSONObject endtimeJson = new JSONObject();
        endtimeJson.put("<", endtimeDataJson);
        
        JSONArray array = new JSONArray();
        array.add(starttimeJson);
        array.add(endtimeJson);
        array.add(meterJson);
        array.add(projectJson);
        
        JSONObject value = new JSONObject();
        value.put("and", array);
        
        
        JSONObject data = new JSONObject();
        data.put("filter", value.toString().replaceAll("\"", "\\\""));
        
        JSONArray obj = restService.postJSONArray(restTokenBean, data);
        return obj;
    }

    @Override
    public JSONArray getNetwork(String labelId, String dcId, String projectId, 
                                 String startTime,String endTime) {
        RestTokenBean restTokenBean = getRestTokenBean(dcId, projectId,
            OpenstackUriConstant.METER_SERVICE_URI);
        StringBuffer sb = new StringBuffer();
        sb.append("/v2/meters/bandwidth?");
        
        sb.append("q.field=resource_id");
        sb.append("&q.field=timestamp");
        sb.append("&q.field=timestamp");
        sb.append("&q.op=eq");
        sb.append("&q.op=ge");
        sb.append("&q.op=lt");
        sb.append("&q.type=");
        sb.append("&q.type=");
        sb.append("&q.type=");
        sb.append("&q.value="+labelId);
        sb.append("&q.value="+startTime);
        sb.append("&q.value="+endTime);

        restTokenBean.setUrl(sb.toString());
        JSONArray array = restService.getJsonArray(restTokenBean);
        if(!array.isEmpty()){
            return array;
        }
        return null;
    }
}
