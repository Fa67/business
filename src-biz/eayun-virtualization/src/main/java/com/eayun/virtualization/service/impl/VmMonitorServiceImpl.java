package com.eayun.virtualization.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.util.DateUtil;
import com.eayun.eayunstack.service.impl.OpenstackBaseServiceImpl;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
import com.eayun.virtualization.service.VmMonitorService;

@SuppressWarnings("rawtypes")
@Service
public class VmMonitorServiceImpl extends OpenstackBaseServiceImpl implements VmMonitorService {
	
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	
	private static final Logger log = LoggerFactory.getLogger(VmMonitorServiceImpl.class);

    @Override
    public JSONArray getMeter(String meter[], String dataCenterId, String projectId , String now , String late) {

        RestTokenBean restTokenBean = getRestTokenBean(dataCenterId, projectId,
            OpenstackUriConstant.METER_SERVICE_URI);
        StringBuffer sb = new StringBuffer();
        sb.append("/v2/meters/" + meter[1]);
        
        sb.append("?q.field=project_id");
        sb.append("&q.field=timestamp");
        sb.append("&q.field=timestamp");
        sb.append("&q.op=eq");
        sb.append("&q.op=ge");
        sb.append("&q.op=lt");
        sb.append("&q.type=");
        sb.append("&q.type=");
        sb.append("&q.type=");
        sb.append("&q.value="+projectId);
        sb.append("&q.value="+late);
        sb.append("&q.value="+now);

        restTokenBean.setUrl(sb.toString());

        JSONArray array = restService.getJsonArray(restTokenBean);
        return array;
    }

    @Override
    public JSONArray getMeterTwo(String meter[], String dataCenterId, String projectId, String vmId) {

        RestTokenBean restTokenBean = getRestTokenBean(dataCenterId, projectId,
            OpenstackUriConstant.METER_SERVICE_URI);
        StringBuffer sb = new StringBuffer();
        sb.append("/v2/meters/" + meter[1]);
        sb.append("?limit=2");
        sb.append("&q.op=eq");
        sb.append("&q.field=" + meter[0]);
        sb.append("&q.value=" + vmId);

        restTokenBean.setUrl(sb.toString());

        JSONArray array = restService.getJsonArray(restTokenBean);
        if (!array.isEmpty()) {
            return array;
        }
        return null;
    }
    
    @Override
    public void summary(MongoTemplate mongoTemplate, String meter, String projectId, String vmId,
                        String interval,Date now) {
    	if(interval.equals("1d")){
    		String todayStr = format.format(now);
            try {
                now = format.parse(todayStr);
            } catch (ParseException e) {
                log.error(e.toString(),e);
            }
    	}
        Date startTime = getStartTime(now, interval);
    	
    	Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("vm_id").is(vmId)),
                Aggregation.match(Criteria.where("timestamp").gte(startTime)),
                Aggregation.match(Criteria.where("timestamp").lt(now)),
                Aggregation.group().avg("counter_volume").as("average")
                );
        AggregationResults<JSONObject> totalresult = mongoTemplate.aggregate(agg,meter + ".detail", JSONObject.class);
        List<JSONObject> totallist = totalresult.getMappedResults();
        if(totallist.size() == 0){
        	return;
        }
        Double totalvolume = 0d;
        totalvolume = totallist.get(0).getDouble("average");
    	
        String unit = "";
        switch (meter) {
            case "disk.read.bytes.rate":
            case "disk.write.bytes.rate":
                unit = "MB/s";
                break;
            case "network.incoming.bytes.rate":
            case "network.outgoing.bytes.rate":
                unit = "Mb/s";
                break;
            case "cpu_util":
            case "memory.usage":
                unit = "%";
                if(totalvolume > 100d){
            		totalvolume = 100d;
            	}
            	break;
        }
        now = DateUtil.dateRemoveSec(now);
        JSONObject json = new JSONObject();
        json.put("counter_name", meter);
        json.put("vm_id", vmId);
        json.put("project_id", projectId);
        json.put("timestamp", now);
        json.put("count_unit", unit);
        json.put("counter_volume", totalvolume);
        json.put("real_time", new Date());
        
        mongoTemplate.insert(json, meter + "." + interval);
    }

    private Date getStartTime(Date now, String interval) {
        Date time = new Date();
        switch (interval) {
            case "3min":
                time = DateUtil.addDay(now, new int[]{0,0,0,0,-3});
                break;
            case "5min":
            	time = DateUtil.addDay(now, new int[]{0,0,0,0,-5});
                break;
            case "10min":
            	time = DateUtil.addDay(now, new int[]{0,0,0,0,-10});
                break;
            case "1h":
            	time = DateUtil.addDay(now, new int[]{0,0,0,-1});
                break;
            case "1d":
            	time = DateUtil.addDay(now, new int[]{0,0,-1});
                break;
        }

        return time;
    }
}
