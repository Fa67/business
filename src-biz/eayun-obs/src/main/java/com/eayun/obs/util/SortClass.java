package com.eayun.obs.util;

import java.util.Comparator;

import com.eayun.obs.model.ObsBucket;

/** 定义比较器（比较时间）
 */
@SuppressWarnings("rawtypes")
public class SortClass implements Comparator{
	  
    public int compare(Object arg0,Object arg1){  
    	ObsBucket bucket0 = (ObsBucket)arg0;  
    	ObsBucket bucket1 = (ObsBucket)arg1;  
        int flag = bucket1.getCreationDate().compareTo(bucket0.getCreationDate());  
        return flag;  
    }  
   
}
