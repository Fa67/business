package com.eayun.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;



public class ScheduleResourceStartup {
	
	public static ClassPathXmlApplicationContext context =null;
	
	public static void main(String[] args) throws Exception{
	    Logger log = LoggerFactory.getLogger(ScheduleResourceStartup.class);
        log.info("Schedule Resource is now starting....");
        context = new ClassPathXmlApplicationContext("classpath*:spring/*.xml");
        context.start();
        log.info("Schedule Resource start has been finished....");
	}
}
