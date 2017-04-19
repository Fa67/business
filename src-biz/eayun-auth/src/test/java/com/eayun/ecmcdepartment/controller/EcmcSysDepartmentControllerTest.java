package com.eayun.ecmcdepartment.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(locations="classpath*:com/eayun/ecmcdepartment/controller/applicationContext.xml")
public class EcmcSysDepartmentControllerTest {
	
	@SuppressWarnings("unused")
    private final static Logger log = LoggerFactory.getLogger(EcmcSysDepartmentControllerTest.class);
	
	@Autowired
	EcmcSysDepartmentController departmentController;

	@Test
	public void testGetDepartTreeGrid() {
//		String reStr = departmentController.getDepartGrid();
//		log.info(reStr);
	}

	@Test
	public void testGetDepartTree() {
//		String reStr = departmentController.getDepartTree();
//		log.info(reStr);
	}

	@Test
	public void testCreateDepart() {
//		String reStr = departmentController.createDepart();
//		log.info(reStr);
	}

	@Test
	public void testModifyDepart() {
//		String reStr = departmentController.modifyDepart();
//		log.info(reStr);
	}

	@Test
	public void testCheckDepartName() {
//		String reStr = departmentController.checkDepartName();
//		log.info(reStr);
	}

}
