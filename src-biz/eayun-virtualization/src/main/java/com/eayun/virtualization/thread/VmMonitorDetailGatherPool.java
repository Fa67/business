package com.eayun.virtualization.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class VmMonitorDetailGatherPool {
    //线程池中控制Running状态的线程为10个
    public static  ExecutorService pool = new ThreadPoolExecutor(10, 10, 2L, TimeUnit.MINUTES,
                                     new LinkedBlockingQueue<Runnable>());
}
