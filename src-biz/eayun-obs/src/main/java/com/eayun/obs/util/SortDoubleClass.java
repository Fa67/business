package com.eayun.obs.util;

import java.util.Comparator;

import com.eayun.obs.model.BucketStorageBean;

/** 定义比较器（比较Double类型）
 */
@SuppressWarnings("rawtypes")
public class SortDoubleClass implements Comparator{
	public int compare(Object arg0,Object arg1){  
		BucketStorageBean a0 = (BucketStorageBean)arg0;
		BucketStorageBean a1 = (BucketStorageBean)arg1;
        double a =Double.parseDouble(a0.getBucketStorage()+"");
        double b =Double.parseDouble(a1.getBucketStorage()+"");
        return Double.compare(a, b);  
    }  
   
}
