package com.eayun.ecmcuser.service;

import java.util.HashMap;
import java.util.Map;

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
@ContextConfiguration(locations="classpath*:com/eayun/ecmcuser/service/applicationContext.xml")
public class EcmcSysUserServiceTest {
	
	private final static Logger log = LoggerFactory.getLogger(EcmcSysUserServiceTest.class);
	
	@Autowired
	EcmcSysUserService ecmcUserService;

	@Ignore
	@Test
	public void testDelUser(){
		ecmcUserService.delUser("40288ee4533b94d601533b94da2f0000");
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
	public void testAddUser(){
		try{
			Map user = new HashMap();
			user.put("account", "王三");
			user.put("password", "123456");
			user.put("name", "王三");
			user.put("tel", "15882109220");
			user.put("mail", "aa@qq.com");
			user.put("departId", "40288eed53c07df90153c07dfea70000");
			user.put("sex", "1");
			String[] roles = {"40288ee653c588cc0153c588d2d60000","40288ee653c589560153c5895cfd0000"};
			user.put("roles", roles);
			log.info(JSON.toJSONString(ecmcUserService.addUser(user)));
		}catch(Exception e){
		    log.error(e.getMessage(),e);
		}
	}
	
	@Ignore
	@Test
	public void testFindUserByDepartmentId(){
		try{
			log.info(JSON.toJSONString(ecmcUserService.findUserByDepartmentId("40288ee65336b0b9015336b0dd210000")));
		}catch(Exception e){
		    log.error(e.getMessage(),e);
		}
	}
	
	@Test
	public void testFindUserById(){
		try{
			log.info(JSON.toJSONString(ecmcUserService.findUserById("40288ee653c58c210153c58c282b0000")));
		}catch(Exception e){
		    log.error(e.getMessage(),e);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
	public void testUpdateUser(){
		try{
			Map user = new HashMap();
			user.put("id", "40288ef153ca86fc0153ca8703770000");
			user.put("account", "王一");
			user.put("password", "123456");
			user.put("name", "王一");
			user.put("tel", "15882109220");
			user.put("mail", "aa@qq.com");
			user.put("departId", "40288eed53c07ef30153c07ef7dd0000");
			user.put("sex", "1");
			String[] roles = {"40288ee653c589a70153c589ae280000","40288ee653c589fd0153c58a03d60000"};
			user.put("roles", roles);
			log.info(JSON.toJSONString(ecmcUserService.updateUser(user)));
		}catch(Exception e){
		    log.error(e.getMessage(),e);
		}
	}
	
}

