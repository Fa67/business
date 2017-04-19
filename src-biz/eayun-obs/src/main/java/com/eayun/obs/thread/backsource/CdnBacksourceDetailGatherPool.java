package com.eayun.obs.thread.backsource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CdnBacksourceDetailGatherPool {

	//线程池中控制Running状态的线程为10个
		public static  ExecutorService pool = new ThreadPoolExecutor(10, 10, 5L, TimeUnit.MINUTES,
	            new LinkedBlockingQueue<Runnable>());
}
