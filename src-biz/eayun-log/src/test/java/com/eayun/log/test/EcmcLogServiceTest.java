package com.eayun.log.test;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.eayun.log.ecmcsevice.EcmcLogService;



/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年3月29日
 */
@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(locations="classpath*:com/eayun/log/test/applicationContext.xml")
public class EcmcLogServiceTest {
	
	@SuppressWarnings("unused")
    private final static Logger log = LoggerFactory.getLogger(EcmcLogServiceTest.class);
	@Autowired
	private EcmcLogService ecmclogservice;

	@Ignore
	@Test
	public void testaddlog(){
		
	}
	
	@Test
	public void testgetloglist(){

	}
}
