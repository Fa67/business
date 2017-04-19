/**
 * 
 */
package com.eayun.customer.serivce.impl;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DesUtil;
import com.eayun.common.util.MD5;
import com.eayun.customer.dao.UserDao;
import com.eayun.customer.model.BaseCustomer;
import com.eayun.customer.model.BaseUser;
import com.eayun.customer.model.Role;
import com.eayun.customer.model.User;
import com.eayun.customer.serivce.CustomerService;
import com.eayun.customer.serivce.LoginService;
import com.eayun.customer.serivce.RoleService;

/**
 * @author 陈鹏飞
 *
 */
@Service
@Transactional
public class LoginServiceImpl implements LoginService {
    private static final Logger log = LoggerFactory.getLogger(LoginServiceImpl.class);
    @Autowired
    private UserDao            userDao;
    @Autowired
    private CustomerService    customerService;
    @Autowired
    private RoleService         roleService;

    @Override
    public SessionUserInfo login(String userAccount, String Password, String idCode,
                                 String rightIdCode, String key) {
        log.info("登录");
        SessionUserInfo sessionUserInfo = new SessionUserInfo();
        if(null == key){
            sessionUserInfo.setError("验证码错误，请联系管理员");
            return sessionUserInfo;
        }
        String userPassword =  DesUtil.strDec(Password, key, null, null);
        JSONObject json =JSONObject.parseObject(rightIdCode);
        if(json==null){
        	sessionUserInfo.setError("验证码超时，请重新输入");
    		return sessionUserInfo;
        }
        String code = json.getString("code");
        String startTimeStr = json.getString("startTime");
        long startTime = Long.parseLong(startTimeStr);
        long timeDiff = (System.currentTimeMillis()-startTime-180000);
        
        if(timeDiff>0){
        	sessionUserInfo.setError("验证码超时，请重新输入");
        	return sessionUserInfo;
        }
        else{
        	if (!idCode.equals(code)) {
        		sessionUserInfo.setError("验证码错误，请重新输入");
        		return sessionUserInfo;
        	}
        	String hql = "from BaseUser where userAccount=?";
        	BaseUser baseUser = (BaseUser) userDao.findUnique(hql, userAccount);
        	if (baseUser == null) {
        		sessionUserInfo.setError("帐号或密码不正确，请重新输入");
        		return sessionUserInfo;
        	}
        	MD5 md5 = new MD5();
        	String salt = baseUser.getSalt();
        	String pass = md5.getMD5ofStr(userPassword);
        	if (!md5.getMD5ofStr(pass+salt).equals(baseUser.getUserPassword())) {
        		sessionUserInfo.setError("帐号或密码不正确，请重新输入");
        		return sessionUserInfo;
        	}
        	else{
        		//添加是否被冻结，被冻结的账户提示不能登录
            	if(baseUser.getIsBlocked()){
            		sessionUserInfo.setError("您的账户已冻结，请联系管理员！");
            		return sessionUserInfo;
            	}
            	
        		if(!checkUserByPrj(baseUser)){
        			sessionUserInfo.setError("您还没有数据中心，请联系管理员");
            		return sessionUserInfo;
        		}
        	}
        	
        	
        	if(null != baseUser.getLastTime()){
        		baseUser.setLastTime(new Date());
        		userDao.merge(baseUser);
        	}

            //sessionUserInfo中增加roleName信息
            String roleId = baseUser.getRoleId();
            Role role = roleService.findRoleById(roleId);
            String roleName = "";
            if(role!=null){
                roleName = role.getRoleName()==null?"":role.getRoleName();
            }
            sessionUserInfo.setRoleName(roleName);

            sessionUserInfo.setUserId(baseUser.getUserId());
            sessionUserInfo.setUserName(baseUser.getUserAccount());
            sessionUserInfo.setRoleId(baseUser.getRoleId());
        	sessionUserInfo.setIsAdmin(baseUser.getIsAdmin());
        	sessionUserInfo.setLastTime(baseUser.getLastTime());
        	sessionUserInfo.setCusId(baseUser.getCusId());
        	BaseCustomer customer = customerService.findCustomerById(baseUser.getCusId());
        	sessionUserInfo.setCusOrg(customer.getCusOrg());
        	sessionUserInfo.setCusCpname(customer.getCusCpname());
        	if(baseUser.getIsPhoneValid()){
        		sessionUserInfo.setPhone(baseUser.getUserPhone());
        	}
        	if(baseUser.getIsMailValid()){
        		sessionUserInfo.setEmail(baseUser.getUserEmail());
        	}
        }

        return sessionUserInfo;
    }

    @Override
    public User findUserByUserName(String userAccount) {
        log.info("根据用户名查询用户");
        String hql = "from BaseUser where userAccount=?";
        BaseUser baseUser = (BaseUser) userDao.findUnique(hql, userAccount);
        if (baseUser != null) {
            User user = new User();
            BeanUtils.copyPropertiesByModel(user, baseUser);
            return user;
        }
        return null;
    }
    
    /**
     * 根据当前登录用户 是否有项目权限，有权限 true,否则 false
     * @param baseUser
     * @return
     */
    @SuppressWarnings("rawtypes")
	public boolean checkUserByPrj(BaseUser baseUser){
    	boolean isAuth = false;
    	if(null!=baseUser){
    		Object [] args = new Object [2];
    		int index =0;
    		StringBuffer sql = new StringBuffer();
    		sql.append(" select count(1) from  ");
    		if(baseUser.getIsAdmin()){
    			sql.append(" cloud_project c");
    		}
    		else{
    			sql.append(" ( ");
    			sql.append(" select  ");
    			sql.append("  	s.project_id as prj_id, ");
    			sql.append("  	p.customer_id  ");
    			sql.append(" from sys_selfuserprj s ");
    			sql.append(" left join cloud_project p ");
    			sql.append(" on s.project_id=p.prj_id ");
    			sql.append(" where 1=1 ");
				sql.append(" and user_id = ? ");
				sql.append(" ) c ");
				args[index++] = baseUser.getUserId();
			}
    		sql.append(" where 1=1 ");
    		sql.append(" and c.customer_id = ? ");
    		args[index++] = baseUser.getCusId();
    		Object [] params = new Object[index];
    		System.arraycopy(args, 0, params, 0, index);
    		javax.persistence.Query query = userDao.createSQLNativeQuery(sql.toString(), params);
    		List list = query.getResultList();
    		
    		if(null!=list&&list.size()==1){
    			Object obj = (Object)list.get(0);
    			int count = Integer.parseInt(obj==null?"0":String.valueOf(obj));
    			if(count>0){
    				isAuth = true ;
    			}
    		}
    	}
    	return isAuth;
    }
}
