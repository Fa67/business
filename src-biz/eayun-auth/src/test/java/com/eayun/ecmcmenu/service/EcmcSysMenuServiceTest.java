package com.eayun.ecmcmenu.service;

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
import com.eayun.ecmcmenu.model.BaseEcmcSysMenu;
import com.eayun.ecmcmenu.model.EcmcSysMenuTreeGrid;

/**
* @Author fangjun.yang
* @Date 2016年3月3日
*/
@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(locations="classpath*:com/eayun/ecmcmenu/service/applicationContext.xml")
public class EcmcSysMenuServiceTest {
	
	private static final Logger log = LoggerFactory.getLogger(EcmcSysMenuServiceTest.class);
	
	@Autowired
	EcmcSysMenuService ecmcSysMenuService;
	
	@Ignore
	@Test
	public void testAddMenu() throws Exception{
		log.info("测试添加菜单开始");
		BaseEcmcSysMenu ecmcSysMenu = new BaseEcmcSysMenu();
		ecmcSysMenu.setCreatedBy("test");
		ecmcSysMenu.setDescription("测试菜单");
		ecmcSysMenu.setEnableFlag('1');
		ecmcSysMenu.setName("测试菜单");
		ecmcSysMenu.setOrderNo(1);
		ecmcSysMenu.setUrl("http:index.do");
		ecmcSysMenuService.addSysMenu(ecmcSysMenu);
	}
	
	@Ignore
	@Test
	public void testFindAllSysMenu() throws Exception{
		log.info("测试查询所有菜单开始");
		List<BaseEcmcSysMenu> resultList = ecmcSysMenuService.getAllSysMenuList();
		if(CollectionUtils.isNotEmpty(resultList)){
			log.debug("** sys menu info:\n{}", JSONObject.toJSONString(resultList));
		}
	}
	
	@Test
	public void testDeleteMenu() throws Exception{
		log.info("测试删除菜单开始");
		ecmcSysMenuService.deleteSysMenu("40288ee6533a72f501533a72f8f70000");
		
	}
	
	@Ignore
	@Test
	public void testUpdateSysMenu() throws Exception{
		String menuId = "40288ee6533a72dc01533a72e0790000";
		BaseEcmcSysMenu ecmcSysMenu = ecmcSysMenuService.getSysMenuById(menuId);
		ecmcSysMenu.setCreatedBy("test");
		ecmcSysMenu.setDescription("测试菜单");
		ecmcSysMenu.setEnableFlag('N');
		ecmcSysMenu.setName("测试菜单2");
		ecmcSysMenu.setOrderNo(2);
		ecmcSysMenu.setUrl("#mainMenu");
		ecmcSysMenuService.updateSysMenu(ecmcSysMenu);
	}
	
	@Ignore
	@Test
	public void testFindRoleMenuByRoleId() throws Exception{
		String roleId = "40288ee4533b25cc01533b25d0250000";
		log.debug(JSONObject.toJSONString(ecmcSysMenuService.findRoleMenuByRoleId(roleId)));
	}
	
	@Test
	public void testFindMenuByRoleId() throws Exception{
		try{
		List<String> stringList=new ArrayList<String>();
		stringList.add("40288ee4533b25cc01533b25d0250000");
//		stringList.add("");
//		stringList.add("");
//		Arrays.asList(new Object[] {"40288ee4533b25cc01533b25d0250000"});
		log.info("\r\n结果为：{}",JSONObject.toJSONString(ecmcSysMenuService.findRoleMenuByRoleIds(stringList)));
		}catch(Exception e){
		    log.error(e.getMessage(),e);
		}
	}
	
	@Test
	public void testGetMenuGridList() throws Exception{
	    List<EcmcSysMenuTreeGrid> resultList = ecmcSysMenuService.getEcmcSysMenuGridList();
	    log.info("\r\nMenu grid:\r\n{}", JSONObject.toJSONString(resultList));
	}

}

