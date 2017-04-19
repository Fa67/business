package com.eayun.api.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Api测试基类
 * 
 * @author zhujun
 * @date 2016年9月28日
 *
 */
@ContextConfiguration({
	"classpath:spring/*",
	"file:src/main/webapp/WEB-INF/spring-mvc.xml"
})
@WebAppConfiguration("src/main/webapp")
public abstract class AbstrackApiTests extends AbstractTestNGSpringContextTests {

	private static MockMvc MockMvc;
	
	@Autowired
	protected WebApplicationContext webApplicationContext;
	
	
	protected MockMvc getMockMvc() {
		synchronized (AbstrackApiTests.class) {
			if (MockMvc == null) {
				MockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
			}
		}
		
		return MockMvc;
	}
}
