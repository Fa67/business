package com.eayun.news.service;

import java.util.Set;

public interface NewsCountService {
	
	public String pop(String key);
	
	public Set<String> unionSet(Set<String> A,Set<String> B);
	
	public void updateStatu(String newsId,int count);
	
	public void updateCollect(String newsId,int count);
}
