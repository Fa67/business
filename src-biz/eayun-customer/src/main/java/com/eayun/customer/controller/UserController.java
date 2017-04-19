package com.eayun.customer.controller;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.customer.bean.MailVerify;
import com.eayun.customer.model.User;
import com.eayun.customer.serivce.MailVerifyService;
import com.eayun.customer.serivce.UserMailService;
import com.eayun.customer.serivce.UserService;
import com.eayun.customer.serivce.UserSmsService;
import com.eayun.log.service.LogService;
/**
 * 用户信息管理
 * @Filename: UserController.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2015年10月20日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Controller
@RequestMapping("/sys/user")
@Scope("prototype")
public class UserController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService         userService;
    
    @Autowired
    private UserSmsService      userSmsService;
    
    @Autowired
    private UserMailService     userMailService;
    
    @Autowired
	private LogService logService;
    
    @Autowired
    private MailVerifyService mailVerifyService;

    /**
     * 检测用户名是否存在
     * @param request
     * @param userName
     * @return
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/checkUserName" , method = RequestMethod.POST)
    @ResponseBody
    public boolean checkUserName(HttpServletRequest request, @RequestBody Map map) {
        log.info("检测用户名是否存在开始");
        String userName = map.get("userName").toString();
        return userService.checkUserName(userName);
    }

    /**
     * 修改密码
     * 
     * @param request
     * @param oldPassword
     * @param newPassword
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/modifyPassword" , method = RequestMethod.POST)
    @ResponseBody
    public String modifyPassword(HttpServletRequest request, @RequestBody Map map) {
        log.info("修改密码开始");
        SessionUserInfo sessionUserInfo = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String userId = sessionUserInfo.getUserId();
        String oldPass = map.get("oldPass").toString();
        String newPass = map.get("newPass").toString();
        User user = new User();
        try {
            user = userService.modifyPassword(request,userId, oldPass, newPass);
            logService.addLog("修改密码", "个人账号信息", null, null,ConstantClazz.LOG_STATU_SUCCESS, null);
        } catch (Exception e) {
            log.error("修改密码失败", e);
            logService.addLog("修改密码", "个人账号信息", null, null,ConstantClazz.LOG_STATU_ERROR, e);
        }
        return JSONObject.toJSONString(user);
    }

    /**
     * 设置用户角色
     * 
     * @param request
     * @param oldPassword
     * @param newPassword
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/setUserRole" , method = RequestMethod.POST)
    @ResponseBody
    public String setUserRole(HttpServletRequest request , @RequestBody Map map) {
        log.info("设置用户角色开始");
        String userAccount = map.get("userAccount").toString();
        try {
            String roleId = map.get("roleId").toString();
            String userId = map.get("userId").toString();
            userService.setUserRole(userId, roleId);
            logService.addLog("管理角色", "用户管理", userAccount, null,ConstantClazz.LOG_STATU_SUCCESS, null);
        } catch (Exception e) {
            log.error("设置用户角色失败", e);
            logService.addLog("管理角色", "用户管理", userAccount, null,ConstantClazz.LOG_STATU_ERROR, e);
            throw e;
        }
        return JSONObject.toJSONString("success");
    }

    /**
     * 根据userId查询用户
     * @param request
     * @param userId
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/findUserById" , method = RequestMethod.POST)
    @ResponseBody
    public String findUserById(HttpServletRequest request,  @RequestBody Map params) {
        log.info("根据userId查询用户开始");
        String userId = params.get("userId").toString();
        User user = userService.findUserById(userId);
        return JSONObject.toJSONString(user);
    }

    /**
     * 查询用户
     * @param request
     * @param user
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getListByCustomer" , method = RequestMethod.POST)
    @ResponseBody
    public String getListByCustomer(HttpServletRequest request , Page page, @RequestBody ParamsMap map) {
        log.info("得到当前客户下用户列表开始");
        SessionUserInfo sessionUserInfo = (SessionUserInfo) request.getSession().getAttribute(
            ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUserInfo.getCusId();
        
        int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        
        page = userService.getPageListByCustomer(page,cusId,queryMap);
        //List<User> userList = userService.getListByCustomer(cusId);
        return JSONObject.toJSONString(page);
    }

    /**
     * 新增用户
     * @param request
     * @param user
     * @return
     * @throws Exception 
     * @throws Exception
     */
    @RequestMapping(value = "/addUser" , method = RequestMethod.POST)
    @ResponseBody
    public String addUser(HttpServletRequest request, @RequestBody User user) throws Exception {
        log.info("添加用户开始");
        try {
            user = userService.addUser(request,user);
            logService.addLog("创建用户", "用户管理", user.getUserAccount(), null,ConstantClazz.LOG_STATU_SUCCESS, null);
        } catch (Exception e) {
            log.error("添加用户失败", e);
            logService.addLog("创建用户", "用户管理", user.getUserAccount(), null,ConstantClazz.LOG_STATU_ERROR, e);
            throw e;
        }
        return JSONObject.toJSONString(user);
    }

    /**
     * 修改用户(姓名)
     * @param request
     * @param user
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/updateUser" , method = RequestMethod.POST)
    @ResponseBody
    public String updateUser(HttpServletRequest request, @RequestBody User user) {
        log.info("修改用户姓名开始");
        try {
            user = userService.updateUser(user);
            logService.addLog("修改客户姓名", "个人账号信息", user.getUserAccount(), null,ConstantClazz.LOG_STATU_SUCCESS, null);
        } catch (Exception e) {
            log.error("修改用户姓名失败", e);
            logService.addLog("修改客户姓名", "个人账号信息", user.getUserAccount(), null,ConstantClazz.LOG_STATU_ERROR, e);
        }
        return JSONObject.toJSONString(user);
    }
    
    @RequestMapping(value = "/updateUserExplain" , method = RequestMethod.POST)
    @ResponseBody
    public String updateUserExplain(HttpServletRequest request, @RequestBody User user) {
        log.info("修改用户账号说明开始");
        try {
            user = userService.updateUser(user);
            logService.addLog("编辑用户", "用户管理", user.getUserAccount(), null,ConstantClazz.LOG_STATU_SUCCESS, null);
        } catch (Exception e) {
            log.error("修改用户账号说明失败", e);
            logService.addLog("编辑用户", "用户管理", user.getUserAccount(), null,ConstantClazz.LOG_STATU_ERROR, e);
        }
        return JSONObject.toJSONString(user);
    }

    /**
     * 删除用户
     * @param request
     * @param user
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/deleteUser" , method = RequestMethod.POST)
    @ResponseBody
    public String deleteUser(HttpServletRequest request , @RequestBody Map params) {
        log.info("删除用户开始");
        String userAccount = params.get("userAccount").toString();
        try {
            String userId = params.get("userId").toString();
            userService.deleteUser(userId);
            logService.addLog("删除用户", "用户管理", userAccount, null,ConstantClazz.LOG_STATU_SUCCESS, null);
        } catch (Exception e) {
            log.error("删除用户失败", e);
            logService.addLog("删除用户", "用户管理", userAccount, null,ConstantClazz.LOG_STATU_ERROR, e);
            throw e;
        }
        return JSONObject.toJSONString("success");
    }

    /**
     * 重置密码
     * @param request
     * @param user
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/resetPassword" , method = RequestMethod.POST)
    @ResponseBody
    public String resetPassword(HttpServletRequest request , @RequestBody Map params) {
        log.info("重置密码开始");
        String userAccount = params.get("userAccount").toString();
        try {
            String userId = params.get("userId").toString();
            userService.resetPassword(userId);
            logService.addLog("重置密码", "用户管理", userAccount, null,ConstantClazz.LOG_STATU_SUCCESS, null);
        } catch (Exception e) {
            log.error("重置密码失败", e);
            logService.addLog("重置密码", "用户管理", userAccount, null,ConstantClazz.LOG_STATU_ERROR, e);
        }
        return JSONObject.toJSONString("success");
    }

    /**
     * 发送验证短信
     * 
     * @param request
     * @param user
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/sendValidSms" , method = RequestMethod.POST)
    @ResponseBody
    public String sendValidSms(HttpServletRequest request , @RequestBody Map params) {
        log.info("发送验证短信开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String phone = params.get("phone").toString();
        String type = params.get("type").toString();
        String userId = sessionUser.getUserId();
        try {
            userSmsService.sendValidSms(phone , userId , type);
        } catch (Exception e) {
            log.error("发送验证短信失败", e);
            throw e;
        }
        return JSONObject.toJSONString("success");
    }

    /**
     * 发送验证邮件
     * 
     * @param request
     * @param user
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/sendValidMail" , method = RequestMethod.POST)
    @ResponseBody
    public String sendValidMail(HttpServletRequest request , @RequestBody Map params) {
        log.info("发送验证邮件开始");
//        StringBuffer url = request.getRequestURL();
//        url.delete(url.lastIndexOf("/"),url.length());
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String userId = sessionUser.getUserId();
        String account = sessionUser.getUserName();
        try {
            String email = params.get("email").toString();
            String imgCode = params.get("imgCode").toString();
            String type = params.get("type").toString();
            String rightIdCode = (String) request.getSession().getAttribute(type);
            userMailService.sendEmail(userId, account  , email, imgCode, rightIdCode);
        } catch (Exception e) {
            log.error("发送验证邮件失败", e);
            throw e;
        }
        return JSONObject.toJSONString("success");
    }
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/againSendEmail" , method = RequestMethod.POST)
    @ResponseBody
    public String againSendEmail(HttpServletRequest request , @RequestBody Map params) {
        log.info("重新发送验证邮件开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String userId = sessionUser.getUserId();
        String account = sessionUser.getUserName();
        try {
            String email = params.get("email").toString();
            userMailService.againSendEmail(userId , account , email);
        } catch (Exception e) {
            log.error("重新发送验证邮件失败", e);
            throw e;
        }
        return JSONObject.toJSONString("success");
    }
    /**
     * 验证邮件
     * 激活状态
     * @param request
     * @param user
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/validMail/{verifyId}")
    @ResponseBody
    public String  validMail(HttpServletRequest request , HttpServletResponse response , @PathVariable("verifyId")String verifyId) {
        log.info("激活验证邮箱开始");
        MailVerify mailVerify = mailVerifyService.findById(verifyId);
        User user = null;
        if(null != mailVerify&&mailVerify.getId()!=null){
        	user = userService.findUserById(mailVerify.getUserId());
        }
        boolean isok = false;
        isok = (mailVerify==null || mailVerify.getId()==null)||mailVerify.isVerify()||new Date().after(mailVerify.getInvalidTime());
        String html = "";
		try {
			html = userService.activeEmailToHtml(request , verifyId);
			logService.addLog("修改联系邮箱", null!=user?user.getUserAccount():null,"个人账号信息", null, null,
					null!=user?user.getCusId():null,isok?ConstantClazz.LOG_STATU_ERROR:ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (Exception e) {
			log.error(e.toString(), e);
			logService.addLog("修改联系邮箱", null!=user?user.getUserAccount():null,"个人账号信息", null, null,
					null!=user?user.getCusId():null,ConstantClazz.LOG_STATU_ERROR, e);
		}
        return html;
    }

    /**
     * 验证手机验证码
     * 校验旧的手机验证码是否正确
     * true:进入下一步操作
     * @param request
     * @param params
     * @return
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/checkCode" , method = RequestMethod.POST)
    @ResponseBody
    public String checkCode(HttpServletRequest request , @RequestBody Map params) {
        log.info("验证手机验证码开始");
        String verCode = params.get("verCode").toString();
        String oldPhone = params.get("userPhone").toString();
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String userId = sessionUser.getUserId();
        
        boolean isRight = userService.checkCode(userId , verCode , oldPhone);
        return JSONObject.toJSONString(isRight);
    }
    /**
     * 更新手机号码
     * 
     * @param request
     * @param user
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/updatePhone" , method = RequestMethod.POST)
    @ResponseBody
    public String updatePhone(HttpServletRequest request , @RequestBody Map params) {
        log.info("更新手机号码开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String userId = sessionUser.getUserId();
        try {
            String phone = params.get("userPhone").toString();
            String verCode = params.get("verCode").toString();
            userService.updatePhone(userId,phone,verCode);
            logService.addLog("修改手机号码", "个人账号信息", null, null,ConstantClazz.LOG_STATU_SUCCESS, null);
        } catch (Exception e) {
            log.error("更新手机号码失败", e);
            logService.addLog("修改手机号码", "个人账号信息", null, null,ConstantClazz.LOG_STATU_ERROR, e);
            throw e;
        }
        return JSONObject.toJSONString("success");
    }
    /**
     * 用户自己重置密码
     * （暂无场景）
     * @param request
     * @return
     */
    @RequestMapping(value = "/resetPasswordBySelf" , method = RequestMethod.POST)
    @ResponseBody
    public String resetPasswordBySelf(HttpServletRequest request) {
        log.info("用户重置密码开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String userId = sessionUser.getUserId();
        try {
            userService.resetPassword(userId);
        } catch (Exception e) {
            log.error("用户重置密码失败", e);
            throw e;
        }
        return JSONObject.toJSONString("success");
    }
    /**
     * 获取当前登陆用户信息
     * @param request
     * @return
     */
    @RequestMapping(value = "/findUser" , method = RequestMethod.POST)
    @ResponseBody
    public String findUser(HttpServletRequest request) {
        log.info("获取当前登陆用户信息开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String userId = sessionUser.getUserId();
        User user = userService.findUserById(userId);
        if(null!= user.getUserPhone() && (!"".equals(user.getUserPhone()))){
        	String phone = user.getUserPhone().substring(0,3)+"****"+user.getUserPhone().substring(7,11);
        	user.setUserPhone(phone);
        }
        if(null!= user.getUserEmail() && (!"".equals(user.getUserEmail()))){
        	String email = user.getUserEmail().substring(0,3)+"****"+user.getUserEmail().substring(user.getUserEmail().lastIndexOf("@"),user.getUserEmail().length());
        	user.setUserEmail(email);
        }
        return JSONObject.toJSONString(user);
    }
    /**
     * 检验输入的原密码是否正确
     * @param request
     * @param params
     * @return
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/checkOldPassword" , method = RequestMethod.POST)
    @ResponseBody
    public String checkOldPassword(HttpServletRequest request, @RequestBody Map params) {
        log.info("检验输入的原密码是否正确开始");
        SessionUserInfo sessionUserInfo = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String userId = sessionUserInfo.getUserId();
        String oldPassword = params.get("oldPassword").toString();
        boolean isTrue = userService.checkOldPassword(request,userId, oldPassword);
        return JSONObject.toJSONString(isTrue);
    }
    
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/checknewPhone" , method = RequestMethod.POST)
    @ResponseBody
    public String checknewPhone(HttpServletRequest request, @RequestBody Map params) {
        log.info("检验输入的手机号码是否已使用");
        String newPhone = params.get("newPhone").toString();
        boolean isTrue = userService.checknewPhone( newPhone);
        return JSONObject.toJSONString(isTrue);
    }
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/checknewMail" , method = RequestMethod.POST)
    @ResponseBody
    public String checknewMail(HttpServletRequest request, @RequestBody Map params) {
        log.info("检验输入的邮箱是否已使用");
        String newMail = params.get("newMail").toString();
        boolean isTrue = userService.checknewMail(newMail);
        return JSONObject.toJSONString(isTrue);
    }
    /**
     * 获取登录用户基本信息，用于总览页
     * @param request
     * @param params
     * @return
     */
    @RequestMapping(value = "/getusermessage" , method = RequestMethod.POST)
    @ResponseBody
    public String getUserMessage(HttpServletRequest request) {
        log.info("获取登录用户基本信息，用于总览页");
        SessionUserInfo sessionUserInfo = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        User user = userService.getUserMessage(sessionUserInfo);
        return JSONObject.toJSONString(user);
    }
}