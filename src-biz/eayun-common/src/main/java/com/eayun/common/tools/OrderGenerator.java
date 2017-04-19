package com.eayun.common.tools;

/**
 * 顺序号生成器
 * 生成十位随机顺序号
 * @author dell
 *
 */
public class OrderGenerator {
  private static long maxorder = System.currentTimeMillis();
  private static Object o = new Object();
  private static final String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  public static String newOrder(){
    long l;
    synchronized(o){
      maxorder++;
      l=maxorder;
    }
    StringBuffer rt = new StringBuffer(10);
    while(l>0){
      rt.insert(0, chars.charAt((int)(l%36)));
      l = l/36;
    }
    return rt.toString();
  }
}