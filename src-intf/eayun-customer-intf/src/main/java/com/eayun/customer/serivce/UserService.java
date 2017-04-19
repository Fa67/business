package com.eayun.customer.serivce;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.customer.model.BaseUser;
import com.eayun.customer.model.User;

public interface UserService {

    /**
     * 给用户赋指定的角色
     * 
     * @param userId
     * @param roleId
     */
    public void setUserRole(String userId, String roleId);

    /**
     * 检查指定用户名在系统中是否存在
     * 
     * @param userName
     * @return
     */
    public boolean checkUserName(String userName);

    /**
     * 修改密码
     * 
     * @param userId
     * @param oldPassword
     * @param newPassword
     * @return
     */
    public User modifyPassword(HttpServletRequest request,String userId, String oldPassword, String newPassword);

    /**
     * 查询用户
     * 
     * @param user
     * @return
     */
    public List<User> getListByCustomer(String customerId);

    /**
     * 根据userId查询指定用户
     * @param userId
     * @return
     */
    public User findUserById(String userId);


    /**
     * 根据用户ID获取基本的用户对象属性
     * @param userId
     * @return
     */
    public BaseUser findBaseUserById(String userId);

    
    /**
     * 根据用户名查询用户
     * 
     * @param userName
     * @return
     */
    public User findUserByUserName(String userName);

    /**
     * 添加用户
     * @param user
     * @return
     */
    public User addUser(HttpServletRequest request,User user);

    /**
     * 修改用户
     * @param user
     * @return
     */
    public User updateUser(User user);

    /**
     * 删除用户
     * 需要删除用户项目
     * 
     * @param user
     * @return
     */
    public void deleteUser(String userId);

    /**
     * 重置密码
     * 
     * @param userId
     */
    public void resetPassword(String userId);
    /**
     * 检验输入的原密码是否正确
     * @param userId
     * @param oldPassword
     * @return
     */
    public boolean checkOldPassword(HttpServletRequest request,String userId, String oldPassword);
    /**
     * 更新手机号码
     * @param userId
     * @param phone
     * @param verCode
     */
    public void updatePhone(String userId, String phone , String verCode);
    
    /**
     * 根据邮件激活填写的邮箱
     * @param verifyId
     */
    public String activeEmailToHtml(HttpServletRequest request , String verifyId);
    
    /**
     * 校验手机验证码
     * @param userId
     * @param verCode
     * @return
     */
    public boolean checkCode(String userId , String verCode , String oldPhone);
    /**
     * 特定角色的用户数
     * @param roleId
     * @return
     */
    public long getCountByRole(String roleId);

    public Page getPageListByCustomer(Page page,String cusId,QueryMap queryMap);

    /**
     * 检验手机号码是否已使用
     * @param userId
     * @param newPhone
     * @return
     */
    public boolean checknewPhone(String newPhone);

    /**
     * 检验邮箱是否已使用
     * @param userId
     * @param newMail
     * @return
     */
    public boolean checknewMail(String newMail);
    /**
     * 忘记密码界面二次获取用户信息的时采用的方法。
     * @param user
     * @return
     */
    public boolean updateUserBySql(User user);

    /**
     * 查询当前登录用户信息，用于总览页
     * @param sessionUserInfo
     * @return
     */
	public User getUserMessage(SessionUserInfo sessionUserInfo);
	
	/**
	 * 查询客户下的超级管理员的用户<br>
	 * ---------------------------
	 * 
	 * @author zhouhaitao
	 * @param cusId
	 * @return
	 */
	public User queryAdminByCusId(String cusId);
}
