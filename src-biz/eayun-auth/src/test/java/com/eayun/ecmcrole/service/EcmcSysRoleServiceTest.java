package com.eayun.ecmcrole.service;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSON;


@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(locations="classpath*:com/eayun/ecmcrole/service/applicationContext.xml")
public class EcmcSysRoleServiceTest {
	
	private final static Logger log = LoggerFactory.getLogger(EcmcSysRoleServiceTest.class);
	
	@Autowired
	EcmcSysRoleService ecmcRoleService;

	@Ignore
	@Test
	public void testDelRole(){
		ecmcRoleService.delRole("40288ee4533a9aff01533a9b208e0000");
	}
	

	
	@Test
	public void testFindAllRole(){
		log.info(JSON.toJSONString(ecmcRoleService.findAllRole()));
	}
	
	@Test
	public void testFindRoleById(){
		log.info(JSON.toJSONString(ecmcRoleService.findRoleById("40288ee653c589fd0153c58a03d60000")));
	}
	
}

