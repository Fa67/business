package com.eayun.database.log.pool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CloudRDSLogSyncThreadPool {
	public static ExecutorService pool = new ThreadPoolExecutor(100, 100, 1L, TimeUnit.MINUTES,
            new LinkedBlockingQueue<Runnable>());
}
