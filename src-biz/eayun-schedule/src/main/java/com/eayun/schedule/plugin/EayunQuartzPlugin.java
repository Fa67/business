package com.eayun.schedule.plugin;

import org.quartz.Scheduler;
import org.quartz.SchedulerConfigException;
import org.quartz.SchedulerException;
import org.quartz.spi.SchedulerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eayun.monitor.thread.EcmcStatusCalculateThreadPool;
import com.eayun.monitor.thread.StatusCalculateThreadPool;


public class EayunQuartzPlugin implements SchedulerPlugin {


    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Data members.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    private boolean cleanShutdown = true;

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public EayunQuartzPlugin() {
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * Determine whether or not the plug-in is configured to cause a clean
     * shutdown of the scheduler.
     * 
     * <p>
     * The default value is <code>true</code>.
     * </p>
     * 
     * @see org.quartz.Scheduler#shutdown(boolean)
     */
    public boolean isCleanShutdown() {
        return cleanShutdown;
    }

    /**
     * Set whether or not the plug-in is configured to cause a clean shutdown
     * of the scheduler.
     * 
     * <p>
     * The default value is <code>true</code>.
     * </p>
     * 
     * @see org.quartz.Scheduler#shutdown(boolean)
     */
    public void setCleanShutdown(boolean b) {
        cleanShutdown = b;
    }

    protected Logger getLog() {
        return log;
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * SchedulerPlugin Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Called during creation of the <code>Scheduler</code> in order to give
     * the <code>SchedulerPlugin</code> a chance to initialize.
     * </p>
     * 
     * @throws SchedulerConfigException
     *           if there is an error initializing.
     */
    public void initialize(String name, final Scheduler scheduler) throws SchedulerException {

        getLog().info("Registering Quartz shutdown hook.");

        Thread t = new Thread("Quartz Shutdown-Hook "
                + scheduler.getSchedulerName()) {
            @Override
            public void run() {
                getLog().info("Shutting down Quartz...");
                try {
                	getLog().info("当前正在执行的任务个数为：{}", scheduler.getCurrentlyExecutingJobs().size());
                	getLog().info("开始关闭定时任务主线程…");
                    scheduler.shutdown(isCleanShutdown());
                    getLog().info("关闭定时任务主线程结束…");
                    //关闭线程池
                    shutdownPools();
                    getLog().info("关闭Schedule结束，线程即将退出…");
                } catch (SchedulerException e) {
                    getLog().info(
                            "Error shutting down Quartz: " + e.getMessage(), e);
                }
            }
        };

        Runtime.getRuntime().addShutdownHook(t);
    }

    public void start() {
        // do nothing.
    }

    /**
     * <p>
     * Called in order to inform the <code>SchedulerPlugin</code> that it
     * should free up all of it's resources because the scheduler is shutting
     * down.
     * </p>
     */
    public void shutdown() {
        // nothing to do in this case (since the scheduler is already shutting
        // down)
    }
    
    private void shutdownPools(){
    	getLog().info("开始关闭线程池...");
        try {
        	getLog().info("关闭线程池 StatusCalculateThreadPool...");
        	StatusCalculateThreadPool.pool.shutdown();
        	getLog().info("关闭线程池 EcmcStatusCalculateThreadPool...");
        	EcmcStatusCalculateThreadPool.pool.shutdown();
		} catch (Exception e) {
			getLog().info("关闭线程池异常...{}", e.getMessage());
		}
        getLog().info("关闭线程池完成...");
    }

}
