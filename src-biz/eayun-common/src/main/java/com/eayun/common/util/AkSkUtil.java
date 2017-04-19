package com.eayun.common.util;
/**
 * 生成accessKey和secretKey 
 * @author xiangyu.cao@eayun.com
 *
 */
public class AkSkUtil {
	/**
	 * 生成secretkey
	 * @return
	 */
	public static String getSecretKeyStr(){
		StringBuffer result=new StringBuffer();
		for(int i=0; i<40;i++){
			int a = (int) ( Math.random () * 3 );
			if(a==0){
				result.append((int) ( Math.random () * 10 ));
			}else if(a==1){
				result.append((char) ( (int) ( Math.random () * 26 ) + 65 ) );
			}else{
				result.append((char) ( (int) ( Math.random () * 26 ) + 97 ));
			}
		}
		return result.toString();
	}
	public static String getAccessKeyStr(){
		StringBuffer result=new StringBuffer();
		for(int i=0; i<20;i++){
			int a = (int) ( Math.random () * 2 );
			if(a==0){
				result.append((int) ( Math.random () * 10 ));
			}else if(a==1){
				result.append((char) ( (int) ( Math.random () * 26 ) + 65 ) );
			}
		}
		return result.toString();
	}
}
