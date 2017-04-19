package com.eayun.virtualization.thread;

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
import org.springframework.data.mongodb.core.query.Query;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.util.DateUtil;
import com.eayun.virtualization.model.CloudProject;

/**
 *                       
 * @Filename: NetworkFlowSummaryGatherThread.java
 * @Description: 
 * @Version: 1.0
 * @Author: chenhao
 * @Email: hao.chen@eayun.com
 * @History:<br>
 *<li>Date: 2015年12月8日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class NetworkFlowSummaryGatherThread implements Runnable {

    private static final Logger     log    = LoggerFactory
                                               .getLogger(NetworkFlowSummaryGatherThread.class);

    private static String[]         meters = new String[] { "bandwidth.network.outgoing",
            "bandwidth.network.incoming"      };

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    private MongoTemplate           mongoTemplate;
    private CloudProject            project;
    private Date            		now;

    public NetworkFlowSummaryGatherThread(MongoTemplate mongoTemplate, CloudProject project,Date now) {
        this.mongoTemplate = mongoTemplate;
        this.project = project;
        this.now = now;
    }

    @Override
    public void run() {
        log.info("开始汇总项目【" + project.getPrjName() + "】的流量数据");
        String todayStr = format.format(now);
        
        Date today = null;
        try {
            today = format.parse(todayStr);
        } catch (ParseException e) {
            log.error(e.getMessage(),e);
        }
        Date yesterday = DateUtil.addDay(today, new int[]{0,0,-1});

        for (String meter : meters) {
        	Aggregation agg = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("project_id").is(project.getProjectId())),
                    Aggregation.match(Criteria.where("timestamp").gte(yesterday)),
                    Aggregation.match(Criteria.where("timestamp").lt(today)),
                    Aggregation.group().sum("counter_volume").as("total")
                    );
            AggregationResults<JSONObject> totalresult = mongoTemplate.aggregate(agg,meter + ".detail", JSONObject.class);
            List<JSONObject> totallist = totalresult.getMappedResults();
            if(totallist.size() == 0){
                continue;
            }
            Double totalvolume = 0d;
            totalvolume = totallist.get(0).getDouble("total");
        	
            /*Criteria criatira = new Criteria();
            criatira.andOperator(Criteria.where("project_id").is(project.getProjectId()), Criteria
                .where("timestamp").gte(yesterday), Criteria.where("timestamp").lt(today));

            List<JSONObject> list = mongoTemplate.find(new Query(criatira), JSONObject.class,
                meter + ".detail");
            if (list.size() == 0) {
                continue;
            }
            Double volume = 0d;
            for (JSONObject json : list) {
                volume += json.getDouble("counter_volume");
            }*/

            JSONObject json = new JSONObject();
            json.put("counter_volume", totalvolume);
            json.put("project_id", project.getProjectId());
            json.put("unit", "MB");
            json.put("date", yesterday);
            json.put("meter", meter);
            json.put("timestamp", now);
            json.put("real_time", new Date());

            // 入库
            mongoTemplate.insert(json, meter + ".summary");
        }
    }
}
