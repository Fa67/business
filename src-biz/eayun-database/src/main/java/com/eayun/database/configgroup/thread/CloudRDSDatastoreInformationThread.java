package com.eayun.database.configgroup.thread;

import com.eayun.common.exception.AppException;
import com.eayun.database.configgroup.service.RdsDatastoreService;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.datacenter.model.DcDataCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.Callable;

/**
 * Created by Administrator on 2017/4/5.
 */
public class CloudRDSDatastoreInformationThread implements Callable<String> {

    private RdsDatastoreService rdsDatastoreService ;
    private BaseDcDataCenter dcDataCenter ;
    private static Logger logger = LoggerFactory.getLogger(CloudRDSDatastoreInformationThread.class);

    public CloudRDSDatastoreInformationThread(RdsDatastoreService rdsDatastoreService){
        this.rdsDatastoreService = rdsDatastoreService ;
    }

    @Override
    public String call() throws Exception {
        logger.info("同步云数据库版本信息开始");
        //同步底层Datastore版本信息，并且自动添加数据库默认配置文件信息
        try {
            rdsDatastoreService.syncDatastoreDataFromStackToCloud(rdsDatastoreService.syncDatastoreDatas(dcDataCenter));
            logger.info("同步云数据库版本信息成功");
            return "success";
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            logger.info("同步云数据库版本信息失败");
            return "failed";
        }
    }

    public void setDcDataCenter(BaseDcDataCenter dcDataCenter) {
        this.dcDataCenter = dcDataCenter;
    }

    public BaseDcDataCenter getDcDataCenter() {
        return dcDataCenter;
    }
}
