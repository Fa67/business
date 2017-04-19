package com.eayun.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;



public class ScheduleStartup {
	
	public static ClassPathXmlApplicationContext context =null;
	
	public static void main(String[] args) throws Exception{
	    Logger log = LoggerFactory.getLogger(ScheduleStartup.class);
        log.info("Schedule is now starting....");
        context = new ClassPathXmlApplicationContext("classpath*:spring/*.xml");
        context.start();
        log.info("Schedule start has been finished....");
	}
}
