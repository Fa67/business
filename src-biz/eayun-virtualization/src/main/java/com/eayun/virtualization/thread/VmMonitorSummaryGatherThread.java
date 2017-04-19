package com.eayun.virtualization.thread;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.model.CloudVm;
import com.eayun.virtualization.service.VmMonitorService;

public class VmMonitorSummaryGatherThread implements Runnable {

    private static final Logger log    = LoggerFactory
                                           .getLogger(VmMonitorSummaryGatherThread.class);

    private CloudProject        project;
    private CloudVm             vm;
    private MongoTemplate       mongoTemplate;
    private VmMonitorService    vmMonitorService;
    private String              interval;
    private Date              	now;

    private static String[]     meters = new String[] { "cpu_util", "memory.usage",
            "disk.read.bytes.rate", "disk.write.bytes.rate", "network.incoming.bytes.rate",
            "network.outgoing.bytes.rate"};

    public VmMonitorSummaryGatherThread(CloudProject project, CloudVm vm,
                                        MongoTemplate mongoTemplate,
                                        VmMonitorService vmMonitorService, String interval,Date now) {
        this.project = project;
        this.vm = vm;
        this.mongoTemplate = mongoTemplate;
        this.vmMonitorService = vmMonitorService;
        this.interval = interval;
        this.now = now;
    }

    @Override
    public void run() {
        log.info("开始汇总云主机【" + vm.getVmName() + "】的指标");
        for (String meter : meters) {
            vmMonitorService.summary(mongoTemplate, meter, project.getProjectId(), vm.getVmId(),
                interval,now);
        }
    }
}