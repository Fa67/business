package com.eayun.database.monitor.thread;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.eayun.database.instance.model.CloudRDSInstance;
import com.eayun.database.monitor.service.InstanceMonitorService;
import com.eayun.virtualization.model.CloudProject;

public class RdsMonitorSummaryGatherThread implements Runnable {
	
	private static final Logger log    = LoggerFactory.getLogger(RdsMonitorSummaryGatherThread.class);
	
	private CloudProject        		project;
    private CloudRDSInstance    		rds;
    private MongoTemplate       		mongoTemplate;
    private InstanceMonitorService    	instanceMonitorService;
    private Date   						now;
    private String              		interval;

    private static String[]     meters = new String[] { "cpu_util", "memory.usage",
            "disk.read.bytes.rate", "disk.write.bytes.rate", "network.incoming.bytes.rate",
            "network.outgoing.bytes.rate" ,"volume.used"};

	public RdsMonitorSummaryGatherThread(CloudProject project,
			CloudRDSInstance rds, MongoTemplate mongoTemplate,
			InstanceMonitorService instanceMonitorService, String interval,
			Date now) {
		this.project = project;
        this.rds = rds;
        this.mongoTemplate = mongoTemplate;
        this.instanceMonitorService = instanceMonitorService;
        this.interval = interval;
        this.now = now;
	}

	@Override
	public void run() {
		log.info("开始汇总数据库实例【" + rds.getRdsName() + "】的指标");
        for (String meter : meters) {
        	instanceMonitorService.summaryMonitor(mongoTemplate, meter, project.getProjectId(), rds,
        			now,interval);
        }
	}

}
