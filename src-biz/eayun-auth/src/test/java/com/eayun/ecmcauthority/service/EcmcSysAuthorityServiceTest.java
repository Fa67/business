package com.eayun.ecmcauthority.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSONObject;
import com.eayun.ecmcauthority.model.BaseEcmcSysAuthority;
import com.eayun.ecmcauthority.model.EcmcSysAuthority;

/**
* @Author fangjun.yang
* @Date 2016年3月1日
*/
@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(locations="classpath*:com/eayun/ecmcauthority/service/applicationContext.xml")
public class EcmcSysAuthorityServiceTest {
	
	private final static Logger log = LoggerFactory.getLogger(EcmcSysAuthorityServiceTest.class);
	
	@Autowired
	EcmcSysAuthorityService ecmcSysAuthorityService;
	
	@Ignore
	@Test
	public void testAddSysAuthtority(){
		BaseEcmcSysAuthority auth = new BaseEcmcSysAuthority();
		auth.setName("test");
		auth.setPermission("test");
		auth.setCreatedBy("test");
		auth.setEnableFlag("1");
		auth.setMenuId("test");
		auth.setDescription("只是个测试");
		ecmcSysAuthorityService.addSysAuthority(auth);
	}
	
	@Ignore
	@Test
	public void testDeleteSysAuthority() throws Exception{
		String authId = "40288eec5336564d0153365651cd0000";
		ecmcSysAuthorityService.deleteSysAuthority(authId);
	}
	
	@Test
	public void testFindSysAutorityByRoleId() throws Exception{
		String roleId = "40288ee4533b25cc01533b25d0250000";
		List<BaseEcmcSysAuthority> resultList = ecmcSysAuthorityService.getSysAutorityListByRoleId(roleId);
		log.debug(JSONObject.toJSONString(resultList));
		if(CollectionUtils.isNotEmpty(resultList)){
			for (BaseEcmcSysAuthority ecmcSysAuthority : resultList) {
				log.debug("\r\n**{} authority info :\r\n{}", ecmcSysAuthority.getId(), JSONObject.toJSONString(ecmcSysAuthority));
			}
		}
	}
	
	@Ignore
	@Test
	public void testUpdateSysAuthority() throws Exception{
		BaseEcmcSysAuthority auth = ecmcSysAuthorityService.findSysAuthorityById("40288eec5336564d0153365651cd0002");
		auth.setEnableFlag("0");
		ecmcSysAuthorityService.updateSysAuthority(auth);
	}
	
	@Test
	public void testFindSysAuthorityByMenuId() throws Exception{
		log.info("测试查询某菜单下的权限列表");
		String menuId = "test";
		List<EcmcSysAuthority> resultList = ecmcSysAuthorityService.getSysAuthorityList(menuId);
		if(CollectionUtils.isNotEmpty(resultList)){
		    for (EcmcSysAuthority ecmcSysAuthority : resultList) {
		        log.debug("\r\n** id:{}, name:{}", ecmcSysAuthority.getId(), ecmcSysAuthority.getName());
            }
		}
		log.debug("\r\n** authority list:\n{}", JSONObject.toJSONString(resultList));
	}
	
	@Test
	public void testFindSysAuthorityByRoleIds() throws Exception{
		try{
			List<String> stringList=new ArrayList<String>();
			stringList.add("40288ee4533a73cb01533a73eebd0000");
			stringList.add("40288ee4533b25cc01533b25d0250000");
			log.info(JSONObject.toJSONString(ecmcSysAuthorityService.getSysAuthorityListByRoleIds(stringList)));
		}catch(Exception e){
		    log.error(e.getMessage(),e);
		}
	}
	
}

