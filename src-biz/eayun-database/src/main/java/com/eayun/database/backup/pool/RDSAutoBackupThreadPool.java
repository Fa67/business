package com.eayun.database.backup.pool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 自动备份线程池
 *
 * @author fan.zhang
 */
public class RDSAutoBackupThreadPool {
    //线程池中控制Running状态的线程为100个
    public static ExecutorService pool = new ThreadPoolExecutor(100, 100, 1L, TimeUnit.MINUTES,
            new LinkedBlockingQueue<Runnable>());

}
