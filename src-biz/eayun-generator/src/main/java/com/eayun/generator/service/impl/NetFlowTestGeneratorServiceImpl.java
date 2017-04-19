package com.eayun.generator.service.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.util.DateUtil;
import com.eayun.generator.service.NetFlowTestGeneratorService;
import com.eayun.generator.service.NetTestGeneratorService;
import com.eayun.virtualization.model.CloudProject;

@Transactional
@Service
public class NetFlowTestGeneratorServiceImpl implements
		NetFlowTestGeneratorService {

	@Autowired
    private  NetTestGeneratorService netTestGeneratorService;
	
	@Autowired
    private MongoTemplate mongoTemplate;
	
	/**
	 * 压力测试下的每个客户项目下：
	 * 上下行流量当天向前两小时内每分钟加一条数据
	 * 向前一个月每天加一条一条汇总数据
	 * @Author: duanbinbin
	 *<li>Date: 2016年12月23日</li>
	 */
	@Override
	public void createBatchFlow() {
		List<CloudProject> prjList = netTestGeneratorService.getTestPrj();
		if(!prjList.isEmpty()){
			
			for(CloudProject pro:prjList){
				
				Date now = new Date();
				now = DateUtil.dateRemoveSec(now);
				for(int i = 0;i < 120;i++){
					JSONObject obj = new JSONObject();
					obj.put("resource_id", pro.getLabelOutId());
					obj.put("project_id", pro.getProjectId());
					obj.put("message_id", UUID.randomUUID().toString().replace("-", ""));
					obj.put("recorded_at", new Date());
					obj.put("counter_name", "test_batch");
                    // 设置时间
                    obj.put("timestamp", DateUtil.addDay(now, new int[]{0,0,0,0,-1}));
                    obj.put("counter_volume", 1024);
                    obj.put("counter_unit", "MB");
                    // 入库
                    mongoTemplate.insert(obj, MongoCollectionName.BANDWIDTH_NETWORK_INCOMING_DETAIL);
                    mongoTemplate.insert(obj, MongoCollectionName.BANDWIDTH_NETWORK_OUTGOING_DETAIL);
                    now = DateUtil.addDay(now, new int[]{0,0,0,0,-1});
				}
				
				Date nows = new Date();
				nows = DateUtil.dateRemoveSec(nows);
				String strday = DateUtil.dateToStr(nows);
				Date day = DateUtil.strToDate(strday);
				for(int i = 0;i < 30;i++){
					JSONObject json = new JSONObject();
		            json.put("counter_volume", 10240);
		            json.put("project_id", pro.getProjectId());
		            json.put("unit", "MB");
		            json.put("date", DateUtil.addDay(day, new int[]{0,0,-1}));
		            json.put("meter", "test_batch");
		            json.put("timestamp", now);
		            
                    mongoTemplate.insert(json, "bandwidth.network.outgoing.summary");
                    mongoTemplate.insert(json, "bandwidth.network.incoming.summary");
                    day = DateUtil.addDay(day, new int[]{0,0,-1});
				}
			}
		}
	}

}
