/**
 * 
 */
package com.eayun.customer.serivce;

import com.eayun.common.filter.SessionUserInfo;
import com.eayun.customer.model.User;

/**
 * @author 陈鹏飞
 *
 */
public interface LoginService {

    /**
     * 登录，先验证验证码是否正确，再验证用户名密码是否正确。错误信息在SessionUserInfo的error属性中体现
     * 
     * @param userAccount 用户名
     * @param userPassword 密码明文
     * @param idCode 验证码
     * @param rightIdCode 正确的验证码
     * @return
     */
    public SessionUserInfo login(String userAccount, String userPassword, String idCode,
                                 String rightIdCode , String key);
    
    public User findUserByUserName(String userAccount);
}
