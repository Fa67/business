package com.eayun.obs.thread.cdn;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CdnRefreshBucketPool {

	public static ThreadPoolExecutor pool = new ThreadPoolExecutor(100, 100, 5L, TimeUnit.MINUTES,
			new LinkedBlockingQueue<Runnable>());
}
