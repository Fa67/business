package com.eayun.common.util;

/**
 *                       
 * @Filename: AuthUtil.java
 * @Description: 
 * @Version: 1.0
 * @Author: chenhao
 * @Email: hao.chen@eayun.com
 * @History:<br>
 *<li>Date: 2016年3月4日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class AuthUtil {
    
    /**
     * 密码加盐，算法是：md5(md5(password)+salt)
     * 
     * @param password
     * @param salt
     * @return
     */
    public static String createSaltPassword(String password , String salt) {
        String saltPassword = "";
        MD5 md5 = new MD5();
        String pass = md5.getMD5ofStr(password);
        saltPassword = md5.getMD5ofStr(pass+salt);
        return saltPassword;
    }

}
