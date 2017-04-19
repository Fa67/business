package com.eayun.common.job;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.quartz.SchedulerException;
import org.quartz.utils.DBConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.LocalDataSourceJobStore;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * QRTZ在集群环境下使用，若注释掉trigger，之前入库的数据不会被删掉，因此需要手工删除
 *                       
 * @Filename: EayunSchedulerFactoryBean.java
 * @Description: 
 * @Version: 1.0
 * @Author: chenhao
 * @Email: hao.chen@eayun.com
 * @History:<br>
 *<li>Date: 2015年12月29日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class EayunSchedulerFactoryBean extends SchedulerFactoryBean {

    private static final Logger log         = LoggerFactory
                                                .getLogger(EayunSchedulerFactoryBean.class);

    private static String[]     TABLE_NAMES = new String[] { "QRTZ_BLOB_TRIGGERS",
            "QRTZ_CALENDARS", "QRTZ_CRON_TRIGGERS", "QRTZ_FIRED_TRIGGERS", "QRTZ_JOB_DETAILS",
            "QRTZ_LOCKS", "QRTZ_PAUSED_TRIGGER_GRPS", "QRTZ_SCHEDULER_STATE",
            "QRTZ_SIMPLE_TRIGGERS", "QRTZ_SIMPROP_TRIGGERS", "QRTZ_TRIGGERS" };

    public EayunSchedulerFactoryBean() {
        setGlobalTriggerListeners(new EayunTriggerListeners());
    }

    protected void registerJobsAndTriggers() throws SchedulerException {
        String scheduleName = getScheduler().getSchedulerName();
        deleteExistsTriggerData(scheduleName);

        super.registerJobsAndTriggers();
    }

    private void deleteExistsTriggerData(String scheduleName) {
        log.info("删除计划任务【" + scheduleName + "】的QRTZ数据");
        Connection conn = null;
        try {
            conn = DBConnectionManager.getInstance().getConnection(
                LocalDataSourceJobStore.TX_DATA_SOURCE_PREFIX + scheduleName);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        for (String tableName : TABLE_NAMES) {
            StringBuffer sb = new StringBuffer();
            sb.append("delete from ");
            sb.append(tableName);
            sb.append(" where SCHED_NAME = ?");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = conn.prepareStatement(sb.toString());
                ps.setString(1, scheduleName);
                ps.executeUpdate();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                closeResultSet(rs);
                closeStatement(ps);
            }
        }
        closeConnection(conn);
    }

    private void closeResultSet(ResultSet rs) {
        if (null != rs) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void closeStatement(Statement statement) {
        if (null != statement) {
            try {
                statement.close();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void closeConnection(Connection conn) {
        if (null != conn) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

}
