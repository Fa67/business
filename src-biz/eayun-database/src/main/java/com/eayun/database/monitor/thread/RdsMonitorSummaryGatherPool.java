package com.eayun.database.monitor.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RdsMonitorSummaryGatherPool {

	public static  ExecutorService pool = new ThreadPoolExecutor(100, 100, 2L, TimeUnit.MINUTES,
            new LinkedBlockingQueue<Runnable>());
}
