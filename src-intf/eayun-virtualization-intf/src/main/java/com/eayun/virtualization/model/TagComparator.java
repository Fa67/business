package com.eayun.virtualization.model;

import java.util.Comparator;

@SuppressWarnings("rawtypes")
public class TagComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        Tag tag1 = (Tag)o1;
        Tag tag2 = (Tag)o2;
        //如果创建日期相同，则比较名字
        int cmpDate = tag1.getCreateDate().compareTo(tag2.getCreateDate());
        if(cmpDate==0){
            //如果创建日期相同，名称按照字典序升序排列
            return tag1.getName().compareTo(tag2.getName());
        }else {
            //*(-1)目的是按照创建日期的降序排列，最新的排在最上面。
            return cmpDate*(-1); 
        }
    }


}
