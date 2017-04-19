package com.eayun.cachetest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;

/**
 * 
 * @author zhujun
 * @date 2016年3月11日
 *
 */
public class NeedCacheFun {

	@Cacheable(value="intCache")
	public Integer intCache() {
		return 50;
	}
	
	@Cacheable(value="intCache")
	public Integer intCache(Integer p) {
		return 10 + p;
	}
	
	
	@Cacheable(value="listInt", key="#p")
	public List<Integer> listInt(String p) {
		List<Integer> list = new ArrayList<>();
		list.add(13);
		list.add(60);
		list.add(4);
		
		return list;
	}
	
	
	@Cacheable(value="listObj", key="'cache:listObj:'.concat(#p)")
	public List<Object> listObj(String p) {
		List<Object> list = new ArrayList<>();
		Map<String, Object> map = new HashMap<>();
		map.put("key1", "111111111");
		map.put("key2", "2222222222");
		list.add(map);
		list.add(13);
		list.add("111111");
		list.add(new Date());
		
		return list;
	}
	
	
	
	
	
}
