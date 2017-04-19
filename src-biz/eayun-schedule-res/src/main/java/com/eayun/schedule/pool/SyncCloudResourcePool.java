package com.eayun.schedule.pool;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyncCloudResourcePool {
	public ExecutorService get(){
		return Executors.newCachedThreadPool();
	}
	
	public CountDownLatch getCountDownLatch(int count){
		return new CountDownLatch(count);
	}

}
