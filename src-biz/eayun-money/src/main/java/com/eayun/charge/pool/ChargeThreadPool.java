package com.eayun.charge.pool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 后付费资源（除OBS外）计费线程线程池
 *
 * @Filename: ChargeJob.java
 * @Description:
 * @Version: 1.0
 * @Author: zhangfan
 * @Email: fan.zhang@eayun.com
 * @History:<br> <li>Date: 2016年8月2日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
public class ChargeThreadPool {
    //线程池中控制Running状态的线程为100个
    public static ExecutorService pool = new ThreadPoolExecutor(100, 100, 1L, TimeUnit.MINUTES,
            new LinkedBlockingQueue<Runnable>());
}
