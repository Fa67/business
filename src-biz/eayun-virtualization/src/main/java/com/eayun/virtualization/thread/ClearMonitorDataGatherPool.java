package com.eayun.virtualization.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ClearMonitorDataGatherPool {
    public static  ExecutorService pool = 
    		new ThreadPoolExecutor(20, 20, 60L, TimeUnit.MINUTES,new LinkedBlockingQueue<Runnable>());
}
