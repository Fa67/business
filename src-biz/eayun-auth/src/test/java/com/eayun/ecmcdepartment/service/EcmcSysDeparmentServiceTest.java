package com.eayun.ecmcdepartment.service;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(locations="classpath*:com/eayun/ecmcdepartment/service/applicationContext.xml")
public class EcmcSysDeparmentServiceTest {
	
	@Autowired
	EcmcSysDepartmentService departmentService;



}
