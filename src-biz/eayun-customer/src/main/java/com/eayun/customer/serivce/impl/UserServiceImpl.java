package com.eayun.customer.serivce.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.util.AuthUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DesUtil;
import com.eayun.common.util.MD5;
import com.eayun.common.util.StringUtil;
import com.eayun.customer.bean.MailVerify;
import com.eayun.customer.bean.PhoneVerify;
import com.eayun.customer.dao.CustomerDao;
import com.eayun.customer.dao.RoleDao;
import com.eayun.customer.dao.UserDao;
import com.eayun.customer.filter.SystemConfig;
import com.eayun.customer.model.BaseCustomer;
import com.eayun.customer.model.BaseRole;
import com.eayun.customer.model.BaseUser;
import com.eayun.customer.model.User;
import com.eayun.customer.serivce.MailVerifyService;
import com.eayun.customer.serivce.PhoneVerifyService;
import com.eayun.customer.serivce.UserService;
import com.eayun.project.service.ProjectService;
import com.eayun.project.service.UserPrjService;

@Service
@Transactional
@SuppressWarnings("unchecked")
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final String website = "http://www.eayun.cn/";
    @Autowired
    private UserDao             userDao;
    @Autowired
    private UserPrjService  userPrjService;
    @Autowired
    private RoleDao             roleDao;
    @Autowired
    private ProjectService projectService;

    @Autowired
    private MailVerifyService mailVerifyService;
    
    @Autowired
    private PhoneVerifyService phoneVerifyService;
    
    @Autowired
    private CustomerDao customerDao;

    @Override
    public List<User> getListByCustomer(String customerId) {
        log.info("得到当前客户下用户列表");

        StringBuffer strb = new StringBuffer();
        strb.append("from BaseUser where cusId = ? order by isAdmin desc , createTime desc");
        List<BaseUser> baseUserList = userDao.find(strb.toString(), customerId);
        List<User> userList = new ArrayList<User>();
        Map<String, BaseRole> roleMap = new HashMap<String, BaseRole>();
        for (BaseUser baseUser : baseUserList) {
            User sysUser = new User();
            BeanUtils.copyPropertiesByModel(sysUser, baseUser);
            // 角色
            if (!StringUtil.isEmpty(sysUser.getRoleId())) {
                BaseRole role = roleMap.get(sysUser.getRoleId());
                if (role == null) {
                    role = roleDao.findOne(sysUser.getRoleId());
                    roleMap.put(sysUser.getRoleId(), role);
                }
                sysUser.setRoleName(role.getRoleName());
            }

            // 项目
            //List<CloudProject> list = userPrjService.getProjectListByUserId(sysUser.getUserId());
            
            List<String> proNameList = new ArrayList<String>();
            proNameList = projectService.getProNameListByUser(sysUser.getIsAdmin(),sysUser.getCusId(),sysUser.getUserId());
            for (String proName : proNameList) {
                sysUser.getProjectNameList().add(proName+"<br>");
            }
            userList.add(sysUser);
        }
        return userList;
    }
    @Override
    public User findUserById(String userId) {
        log.info("根据userId查询用户");

        BaseUser baseUser = userDao.findOne(userId);
        User user = new User();
        BeanUtils.copyPropertiesByModel(user, baseUser);
        return user;
    }

    @Override
    public BaseUser findBaseUserById(String userId) {
        log.info("根据userId查询基本的用户信息");

        BaseUser baseUser = userDao.findOne(userId);
        return baseUser;
    }

    @Override
    public User addUser(HttpServletRequest request,User user) {
        log.info("新建用户");
        SessionUserInfo sessionUserInfo = (SessionUserInfo) request.getSession().getAttribute(
            ConstantClazz.SYS_SESSION_USERINFO);
        String password = user.getUserAccount() + "guest";
        int salt = (int)((Math.random()*9+1)*100000);
        String userSalt = String.valueOf(salt);
        String saltPassword = AuthUtil.createSaltPassword(password , userSalt);
        user.setUserPassword(saltPassword);
        user.setCreateTime(new Date());
        String cusId = sessionUserInfo.getCusId();
        user.setCusId(cusId);
        user.setIsMailValid(false);
        user.setIsPhoneValid(false);
        user.setIsAdmin(false);
        user.setSalt(userSalt);
        user.setIsBlocked(false);
        BaseUser baseUser = new BaseUser();
        BeanUtils.copyPropertiesByModel(baseUser, user);
        userDao.saveEntity(baseUser);
        BeanUtils.copyPropertiesByModel(user, baseUser);
        return user;
    }

    @Override
    public User updateUser(User user) {
        log.info("修改用户姓名或账号说明");

        BaseUser baseUser = userDao.findOne(user.getUserId());
        baseUser.setUserExplain(user.getUserExplain());
        baseUser.setUserPerson(user.getUserPerson());
        
        userDao.merge(baseUser);
        BeanUtils.copyPropertiesByModel(user, baseUser);
        if(baseUser.getIsAdmin()){
        	BaseCustomer customer = customerDao.findOne(baseUser.getCusId());
            customer.setCusName(baseUser.getUserPerson());
            customerDao.merge(customer);
        }
        return user;
    }

    @Override
    public void deleteUser(String userId) {
        log.info("删除用户");
        userPrjService.deleteByUser(userId);
        
        StringBuffer hql = new StringBuffer();
        hql.append("delete BaseUser where userId = ?");
        userDao.executeUpdate(hql.toString(), userId);
    }

    @Override
    public User modifyPassword(HttpServletRequest request,String userId, String oldPass, String newPass) {
        log.info("修改密码");
        String key = (String) request.getSession().getAttribute(ConstantClazz.PASSWORD_SESSION);
        String oldPassword = DesUtil.strDec(oldPass, key, null, null);
        String newPassword = DesUtil.strDec(newPass, key, null, null);

        BaseUser baseUser = userDao.findOne(userId);
        MD5 md5 = new MD5();
        String oldSalt = baseUser.getSalt();
        String oldPassCheck = md5.getMD5ofStr(oldPassword);
        if (!md5.getMD5ofStr(oldPassCheck+oldSalt).equals(baseUser.getUserPassword())) {
            throw new AppException("原密码错误！");
        }
        int newSalt = (int)((Math.random()*9+1)*100000);
        String userSalt = String.valueOf(newSalt);
        String saltPassword = AuthUtil.createSaltPassword(newPassword , userSalt);
        baseUser.setUserPassword(saltPassword);
        baseUser.setSalt(userSalt);
        if(null == baseUser.getLastTime()){
            baseUser.setLastTime(new Date());
        }
        userDao.merge(baseUser);

        User user = new User();
        BeanUtils.copyPropertiesByModel(user, baseUser);
        return user;
    }

    @Override
    public boolean checkUserName(String userName) {
        log.info("检测用户名是否存在");
        StringBuffer sb = new StringBuffer();
        sb.append("select count(*) from BaseUser where userAccount = ?");
        List<Long> list = userDao.find(sb.toString(), userName);
        long count = list.get(0);
        return count == 0;
    }

    @Override
    public void setUserRole(String userId, String roleId) {
        log.info("设置用户角色");

        StringBuffer sb = new StringBuffer();
        sb.append("update BaseUser set roleId=? where userId=? ");
        userDao.executeUpdate(sb.toString(), roleId, userId);
    }

    @Override
    public void resetPassword(String userId) {
        log.info("重置密码");

        BaseUser user = userDao.findOne(userId);
        String password = user.getUserAccount() + "guest";
        int salt = (int)((Math.random()*9+1)*100000);
        String userSalt = String.valueOf(salt);
        String saltPassword = AuthUtil.createSaltPassword(password , userSalt);
        user.setUserPassword(saltPassword);
        user.setLastTime(null);
        user.setSalt(userSalt);
        
        userDao.merge(user);
    }

    @Override
    public User findUserByUserName(String userName) {
        log.info("根据用户名查询用户");

        String hql = "from BaseUser where userAccount=?";
        BaseUser baseUser = (BaseUser) userDao.findUnique(hql, userName);
        if (baseUser != null) {
            User user = new User();
            BeanUtils.copyPropertiesByModel(user, baseUser);
            return user;
        }

        return null;
    }
    
    @Override
    public boolean checkOldPassword(HttpServletRequest request,String userId, String oldPassword) {
        log.info("检验输入的原密码是否正确");
        String key = (String) request.getSession().getAttribute(ConstantClazz.PASSWORD_SESSION);
        String oldPass = DesUtil.strDec(oldPassword, key, null, null);

        BaseUser baseUser = userDao.findOne(userId);
        String oldSalt = baseUser.getSalt();
        String oldSaltPass = AuthUtil.createSaltPassword(oldPass, oldSalt);
        boolean isSame = oldSaltPass.equals(baseUser.getUserPassword());
        return isSame;
    }

    /**
     * 第一步通过后（或原无绑定手机），检验并绑定新手机号
     * 1.校验验证码
     * 2.修改记录状态
     * 3.修改用户状态和手机号码
     * @param userId
     * @param phone     新手机号码
     * @param verCode
     */
    @Override
    public void updatePhone(String userId, String phone , String verCode) {
        log.info("更新手机号码");
        BaseUser baseuser = userDao.findOne(userId);
        if(phone.contains("****") && !baseuser.getIsPhoneValid()){
        	phone = baseuser.getUserPhone();
        }
        if (StringUtil.isEmpty(verCode)) {
            throw new AppException("请输入验证码!");
        }
        PhoneVerify phoneVerify = phoneVerifyService.findByUserAndPh(userId , phone , "1");
        if(phoneVerify == null||phoneVerify.getId() ==null){
            throw new AppException("您还未发送验证码或手机号码输入错误");
        }
        if(!verCode.equals(phoneVerify.getPhoneCode())){
            throw new AppException("手机验证码不正确，请重新输入");
        }
        if(phoneVerify.isVerify()){
            throw new AppException("手机验证码已使用，请重新获取");
        }
        Date date = new Date();
        if(date.after(phoneVerify.getInvalidTime())){
            throw new AppException("手机验证码已过期，请重新获取");
        }
        
        baseuser.setIsPhoneValid(true);
        baseuser.setUserPhone(phone);
        userDao.merge(baseuser);
        
        if(baseuser.getIsAdmin()){
            BaseCustomer customer = customerDao.findOne(baseuser.getCusId());
            customer.setCusPhone(phone);
            customerDao.merge(customer);
        }
        phoneVerifyService.updatePhoneByVerify(phoneVerify);
    }
    
    @Override
    public String activeEmailToHtml(HttpServletRequest request , String verifyId) {
        String message = activationEmail(request , verifyId);
        SystemConfig xml = new SystemConfig();
        Map<String,String> urlMap=xml.findNodeMap();
        String logUrl = urlMap.get("ecscUrl");
        
        String webPath = request.getSession().getServletContext().getRealPath("");
        String path = webPath.replace("\\", "/");
        String filePath  = path + File.separator+"html"+File.separator+"mailsuccess.html";
        
        File file = new File(filePath);
        StringBuffer mailHtml= new StringBuffer();
        try {  
            if (!file.exists() || file.isDirectory()){
                throw new FileNotFoundException();
            }  
            BufferedReader br = new BufferedReader
                    (new InputStreamReader(new FileInputStream(file),"utf-8"));  
            String line = null;  
            while ((line = br.readLine()) != null) {  
                mailHtml.append(line);  
            }  
            br.close();
        } catch (IOException ie) {  
            log.error(ie.getMessage(),ie); 
        }
        String html = mailHtml.toString();
        html=html.replace("{imgUrl}", logUrl+"/images/email.png");
        html=html.replace("{message}", message);
        html=html.replace("{logmng}", "登录管理控制台");
        html=html.replace("{backeayun}", "返回易云捷讯官网");
        html=html.replace("{logUrl}", logUrl);
        html=html.replace("{website}", website);
        try {
            html = new String(html.getBytes("UTF-8"),"ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            log.error("激活邮箱失败", e);
        }
        return html;
    }
    private String activationEmail(HttpServletRequest request , String verifyId) {
        log.info("激活验证邮箱");
        MailVerify mailVerify = mailVerifyService.findById(verifyId);
        Date date = new Date();
        String message = "";
        if(mailVerify==null || mailVerify.getId()==null){
            message = "邮件已失效";
            return message;
            //throw new AppException(message);
        }
        if(mailVerify.isVerify()){
            message = "该邮箱已验证";
            return message;
            //throw new AppException(message);
        }
        if(date.after(mailVerify.getInvalidTime())){
            message = "链接已失效，请重新验证";
            return message;
            //throw new AppException(message);
        }
        BaseUser baseuser = userDao.findOne(mailVerify.getUserId());
        /*if(!mailVerify.getEmail().equals(baseuser.getUserEmail())){
            message = "验证邮箱与最新填写邮箱不符，请重新发送!";
            return message;
            //throw new AppException(message);
        }*/
        message = "您的邮箱<span class='blue'>"+mailVerify.getEmail()+"</span> 已验证成功！";
        mailVerifyService.updateByVerify(mailVerify);
        baseuser.setIsMailValid(true);
        baseuser.setUserEmail(mailVerify.getEmail());
        userDao.merge(baseuser);
        if(baseuser.getIsAdmin()){
            BaseCustomer customer = customerDao.findOne(baseuser.getCusId());
            customer.setCusEmail(mailVerify.getEmail());
            customerDao.merge(customer);
        }
        return message;
    }

    /**
     * 校验旧的手机验证码是否正确
     * @param userId
     * @param verCode
     * @return
     */
    @Override
    public boolean checkCode(String userId, String verCode , String oldPhone) {
        log.info("校验短信验证码");
        BaseUser baseUser = userDao.findOne(userId);
        oldPhone = baseUser.getUserPhone();
        if (StringUtil.isEmpty(verCode)) {
            throw new AppException("请输入验证码!");
        }
        PhoneVerify phoneVerify = phoneVerifyService.findByUserAndPh(userId,oldPhone,"0");
        if(phoneVerify == null||phoneVerify.getId() ==null){
            throw new AppException("您还未发送验证码");
        }
        if(!verCode.equals(phoneVerify.getPhoneCode())){
            throw new AppException("手机验证码不正确，请重新输入");
        }
        if(phoneVerify.isVerify()){
            throw new AppException("手机验证码已使用，请重新获取");
        }
        Date date = new Date();
        if(date.after(phoneVerify.getInvalidTime())){
            throw new AppException("手机验证码已过期，请重新获取");
        }
        if(verCode.equals(phoneVerify.getPhoneCode())){
            phoneVerifyService.updatePhoneByVerify(phoneVerify);
            return true;
        }else{
            return false;
        }
    }

    @Override
    public long getCountByRole(String roleId) {
        log.info("某个角色的用户数");
        StringBuffer sb = new StringBuffer();
        sb.append("select count(*) from BaseUser where roleId = ?");
        List<Long> list = userDao.find(sb.toString(), roleId);
        long count = list.get(0);
        return count;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Page getPageListByCustomer(Page page,String cusId,QueryMap queryMap) {
        log.info("得到当前客户下用户列表");

        StringBuffer strb = new StringBuffer();
        strb.append("from BaseUser where cusId = ? order by isAdmin desc , createTime desc");
        page = userDao.pagedQuery(strb.toString(), queryMap, cusId);
        List<BaseUser> baseUserList = (List) page.getResult();
        Map<String, BaseRole> roleMap = new HashMap<String, BaseRole>();
        for (int i = 0; i < baseUserList.size(); i++) {
            BaseUser baseUser = baseUserList.get(i);
            User sysUser = new User();
            BeanUtils.copyPropertiesByModel(sysUser, baseUser);
            // 角色
            if (!StringUtil.isEmpty(sysUser.getRoleId())) {
                BaseRole role = roleMap.get(sysUser.getRoleId());
                if (role == null) {
                    role = roleDao.findOne(sysUser.getRoleId());
                    roleMap.put(sysUser.getRoleId(), role);
                }
                sysUser.setRoleName(role.getRoleName());
            }

            List<String> proNameList = new ArrayList<String>();
            proNameList = projectService.getProNameListByUser(sysUser.getIsAdmin(),sysUser.getCusId(),sysUser.getUserId());
            for (String proName : proNameList) {
                sysUser.getProjectNameList().add(proName+"<br>");
            }
            baseUserList.set(i, sysUser);
        }
        return page;
    }
    @Override
    public boolean checknewPhone(String newPhone) {
        log.info("校验手机号码是否已使用");
        boolean isUpdateName = false;
        int num;
        int cusnum;
        cusnum = customerDao.getCountByPhone(newPhone);
        if(cusnum > 0){
            isUpdateName = false;
        }else{
            num = userDao.getCountByPhone(newPhone);
            if(num > 0){
                isUpdateName = false;
            }else{
                isUpdateName = true;
            }
        }
        return isUpdateName;
    }
    @Override
    public boolean checknewMail(String newMail) {
        log.info("校验邮箱是否已使用");
        boolean isUpdateName = false;
        int num;
        int cusnum;
        cusnum = customerDao.getCountByMail(newMail);
        if(cusnum > 0){
            isUpdateName = false;
        }else{
            num = userDao.getCountByMail(newMail);
            if(num > 0){
                isUpdateName = false;
            }else{
                isUpdateName = true;
            }
        }
        return isUpdateName;
    }
    public boolean updateUserBySql(User user){
    	boolean flag = false;
    	try{
	    	StringBuffer sql = new StringBuffer();
	    	sql.append("  update sys_selfuser  ");
	    	sql.append("  set user_password =?, ");
	    	sql.append("  salt =? ");
	    	sql.append("   where user_id = ? ");
	    	
	    	userDao.execSQL(sql.toString(), new Object[]{
	    		user.getUserPassword(),
	    		user.getSalt(),
	    		user.getUserId()});
	    	
	    	flag =true;
    	}
    	catch(Exception e){
    		flag =false;
    		throw e;
    	}
    	return flag;
    }
	@Override
	public User getUserMessage(SessionUserInfo sessionUserInfo) {
		BaseUser baseUser = userDao.findOne(sessionUserInfo.getUserId());
		BaseCustomer customer = customerDao.findOne(sessionUserInfo.getCusId());
        User user = new User();
        BeanUtils.copyPropertiesByModel(user, baseUser);
        user.setCusCpname(customer.getCusCpname());
        String phone = (null == user.getUserPhone() || "".equals(user.getUserPhone()))?"未设置":user.getUserPhone().substring(0,3)+"****"+user.getUserPhone().substring(7,11);;
        String mail = "".equals(user.getUserEmail())||user.getUserEmail()==null?"未设置":user.getUserEmail().substring(0,3)+"****"+user.getUserEmail().substring(user.getUserEmail().lastIndexOf('@'),user.getUserEmail().length());
        user.setUserPhone(phone);
        user.setUserEmail(mail);
        return user;
	}
	
	/**
	 * 查询客户下的超级管理员的用户<br>
	 * ---------------------------
	 * 
	 * @author zhouhaitao
	 * @param cusId
	 * @return
	 */
	public User queryAdminByCusId(String cusId){
		BaseUser baseUser = userDao.findAdminByCusId(cusId);
		User user = null;
		if(null != baseUser){
			user = new User();
			BeanUtils.copyPropertiesByModel(user, baseUser);
		}
		return user;
	}
}