package com.eayun.cachetest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(locations="classpath*:com/eayun/cachetest/cache-redis.xml")
public class CacheTest {

	@Autowired
	private NeedCacheFun needCacheFun;
	
	@Test
	public void testIntCache() {
		System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxtestIntCache:"+needCacheFun.intCache());
	}
	
	@Ignore
	@Test
	public void testIntCacheP() {
		System.out.println("xxxxxxxxxxxxxxxxxxxxxxxtestIntCacheP:"+needCacheFun.intCache(2222));
	}
	
	
	@Test
	public void testListIntCacheP() {
//		System.out.println(needCacheFun.listInt("1111"));
		System.out.println("xxxxxxxxxxxxxxxxxxxxxxxtestListCacheP:"+needCacheFun.listInt("2222"));
	}
	
	
	@Test
	public void testListIntObjP() {
		System.out.println("xxxxxxxxxxxxxxxxxxxtestListCacheobjP:"+needCacheFun.listObj("3333"));
	}
	
}
