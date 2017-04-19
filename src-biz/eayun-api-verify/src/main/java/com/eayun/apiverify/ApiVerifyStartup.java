package com.eayun.apiverify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;



public class ApiVerifyStartup {
	
	public static ClassPathXmlApplicationContext context =null;
	
	public static void main(String[] args) throws Exception{
	    Logger log = LoggerFactory.getLogger(ApiVerifyStartup.class);
        log.info("ApiVerifyStartup is now starting....");
        context = new ClassPathXmlApplicationContext("classpath*:spring/*.xml");
        context.start();
        log.info("ApiVerifyStartup has been finished....");
	}
}
