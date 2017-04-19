package com.eayun.virtualization.model;

import java.util.Comparator;

@SuppressWarnings("rawtypes")
public class TagGroupComparator implements Comparator{

    @Override
    public int compare(Object o1, Object o2) {
        TagGroup tg1 = (TagGroup)o1;
        TagGroup tg2 = (TagGroup)o2;
        //a negative integer: o1 < o2
        //zero: o1=o2
        //a positive integer: o1 > o2
        int cmpDate = tg1.getCreateDate().compareTo(tg2.getCreateDate());
        if(cmpDate==0){
            return tg1.getName().compareTo(tg2.getName());
        }else {
            return cmpDate*(-1); 
        }
    }

}
