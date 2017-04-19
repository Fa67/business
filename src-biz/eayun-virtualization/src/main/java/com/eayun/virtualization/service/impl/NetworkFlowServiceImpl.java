package com.eayun.virtualization.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.virtualization.service.NetworkFlowService;

@Service
public class NetworkFlowServiceImpl implements NetworkFlowService {

    private static final Logger log = LoggerFactory.getLogger(NetworkFlowServiceImpl.class);

    @Autowired
    private MongoTemplate           mongoTemplate;

    @Override
    public List<Map<String, Object>> getIncomingData(Date starttime, Date endtime, String projectId) {
        return getNetworkFlowData("network.incoming.bytes", starttime, endtime, projectId);
    }

    @Override
    public List<Map<String, Object>> getOutgoingData(Date starttime, Date endtime, String projectId) {
        return getNetworkFlowData("network.outgoing.bytes", starttime, endtime, projectId);
    }

    /**
     * 头尾两天查明细表，中间日期查汇总表
     * 
     * @param meter
     * @param starttime
     * @param endtime
     * @param projectId
     * @return
     */
    private List<Map<String, Object>> getNetworkFlowData(String meter, Date starttime,
                                                         Date endtime, String projectId) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        SimpleDateFormat format  = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat format2 = new SimpleDateFormat("HH:mm:ss");
        Date[] midDatetime = getMidDatetime(starttime, endtime);
        String startStr = format2.format(starttime);
        String endStr = format2.format(endtime);
        if (midDatetime == null) { // 同一天
            Double data = getDataFromDetail(meter, projectId, starttime, endtime);
            Map<String, Object> map = new HashMap<String, Object>();
            StringBuffer sb = new StringBuffer();
            sb.append(format.format(starttime));
            sb.append(" ");
            sb.append(startStr);
            sb.append("--");
            sb.append(endStr);
            map.put("date", sb.toString());
            map.put("value", data);
            result.add(map);
        } else {
            // 第一天
            Date[] firstDatetime = getFirstDatetime(starttime);
            Double data = getDataFromDetail(meter, projectId, firstDatetime[0], firstDatetime[1]);
            Map<String, Object> map = new HashMap<String, Object>();
            if (data != 0) {
                map.put("date", format.format(firstDatetime[0]) + " " + startStr + "--23:59:59");
                map.put("value", data);
                result.add(map);
            }

            // 中间日期
            List<Map<String, Object>> midList = getDataFromSummary(meter, projectId,
                midDatetime[0], midDatetime[1]);
            result.addAll(midList);
            // 最后一天
            Date[] lastDatetime = getLastDatetime(endtime);
            data = getDataFromDetail(meter, projectId, lastDatetime[0], lastDatetime[1]);
            if (data != 0) {
                map = new HashMap<String, Object>();
                map.put("date", format.format(lastDatetime[0]) + " 00:00:00--" + endStr);
                map.put("value", data);
                result.add(map);
            }
        }
        return result;
    }

    private List<Map<String, Object>> getDataFromSummary(String meter, String projectId,
                                                         Date starttime, Date endtime) {
        SimpleDateFormat format  = new SimpleDateFormat("yyyy-MM-dd");
        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("project_id").is(projectId), Criteria
            .where("timestamp").gte(starttime), Criteria.where("timestamp").lt(endtime));
        Sort sort = new Sort(Direction.ASC, "date");
        List<JSONObject> list = mongoTemplate.find(new Query(criatira).with(sort),
            JSONObject.class, meter + ".summary");
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (JSONObject json : list) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("date", format.format(json.getDate("date") + " 00:00:00--23:59:59"));
            map.put("value", json.getDouble("volume") / 1024);
            result.add(map);
        }
        return result;
    }

    /**
     * @param meter
     * @param projectId
     * @param starttime 大于等于开始时间
     * @param endtime 小时结束时间
     * @return
     */
    private Double getDataFromDetail(String meter, String projectId, Date starttime, Date endtime) {
        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("project_id").is(projectId), Criteria
            .where("timestamp").gte(starttime), Criteria.where("timestamp").lt(endtime));
        List<JSONObject> list = mongoTemplate.find(new Query(criatira), JSONObject.class,
            meter + ".detail");
        Double result = 0d;
        for (JSONObject json : list) {
            result += json.getDouble("volume");
        }
        return result / 1024 / 1024;
    }

    /**
     * 得到第一天的开始-结束时间
     * 
     * @param starttime
     * @return
     */
    private Date[] getFirstDatetime(Date starttime) {
        SimpleDateFormat format  = new SimpleDateFormat("yyyy-MM-dd");
        String s = format.format(starttime);
        Date endtime = null;
        try {
            endtime = new Date(format.parse(s).getTime() + 24 * 60 * 60 * 1000);
        } catch (ParseException e) {
            log.error(e.toString(),e);
        }
        return new Date[] { starttime, endtime };
    }

    private Date[] getLastDatetime(Date endtime) {
        SimpleDateFormat format  = new SimpleDateFormat("yyyy-MM-dd");
        String s = format.format(endtime);
        Date starttime = null;
        try {
            starttime = new Date(format.parse(s).getTime());
        } catch (ParseException e) {
            log.error(e.toString(),e);
        }
        Date end = new Date(endtime.getTime() + 1);
        return new Date[] { starttime, end };
    }

    private Date[] getMidDatetime(Date starttime, Date endtime) {
        SimpleDateFormat format  = new SimpleDateFormat("yyyy-MM-dd");
        String s1 = format.format(starttime);
        String s2 = format.format(endtime);
        if (s1.equals(s2)) {
            return null;
        }
        Date start = null;
        try {
            start = new Date(format.parse(s1).getTime() + 24 * 60 * 60 * 1000);
        } catch (ParseException e) {
            log.error(e.toString(),e);
        }
        Date end = null;
        try {
            end = new Date(format.parse(s2).getTime() - 1);
        } catch (ParseException e) {
            log.error(e.toString(),e);
        }
        return new Date[] { start, end };
    }
}
