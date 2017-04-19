package com.eayun.virtualization.dao;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.eayun.virtualization.ecmcvo.CloudLdpoolVoe;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/com/eayun/virtualization/dao/applicationContext-mysql.xml")
public class CloudLdPoolDaoTest {

	@Autowired
	private CloudLdPoolDao cloudLdPoolDao;
	
	@Test
	public void testFindByStringListOfStringListOfStringStringPageable() {
		PageRequest pageReq = new PageRequest(0, 20);
		Page<CloudLdpoolVoe> page = cloudLdPoolDao.findBy(null, null, null, null, pageReq);
		System.out.println(page.getTotalElements());
	}

}
