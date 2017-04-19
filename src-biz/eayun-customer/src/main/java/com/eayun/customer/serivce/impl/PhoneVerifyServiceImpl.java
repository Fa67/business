package com.eayun.customer.serivce.impl;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.customer.bean.BasePhoneVerify;
import com.eayun.customer.bean.PhoneVerify;
import com.eayun.customer.dao.PhoneVerifyDao;
import com.eayun.customer.serivce.PhoneVerifyService;

@Service
@Transactional
public class PhoneVerifyServiceImpl implements PhoneVerifyService {

    private static final Logger log = LoggerFactory.getLogger(MailVerifyServiceImpl.class);
    
    @Autowired
    private PhoneVerifyDao phoneVerifyDao;
    /**
     * 1.删除所有该用户的旧短信记录
     * 2.增加一条新纪录
     * 3.状态为未验证
     * @param phoneVerify
     * @return
     */
    @Override
    public PhoneVerify addPhoneVerify(PhoneVerify phoneVerify) {
        log.info("添加手机短信验证记录");
        
        StringBuffer hql = new StringBuffer();
        hql.append("delete BasePhoneVerify where userId = ? and is_newphone = ? ");
        String isNewphone = "";
        if(phoneVerify.isNewphone()){
            isNewphone = "1";
        }else{
            isNewphone = "0";
        }
        phoneVerifyDao.executeUpdate(hql.toString(), phoneVerify.getUserId(),isNewphone);
        
        Date date = new Date();
        phoneVerify.setSendTime(date);
        //短信验证码，时效5分钟
        phoneVerify.setInvalidTime(DateUtil.addDay(date , new int[]{0,0,0,0,5}));
        phoneVerify.setVerify(false);
        
        BasePhoneVerify basePhone = new BasePhoneVerify();
        BeanUtils.copyPropertiesByModel(basePhone , phoneVerify);
        phoneVerifyDao.saveEntity(basePhone);
        BeanUtils.copyPropertiesByModel(phoneVerify , basePhone);
        return phoneVerify;
    }

    @Override
    public PhoneVerify findByUserAndPh(String userId , String phone , String isNew) {
        log.info("根据用户ID,手机号码查询短信验证记录");
        List<BasePhoneVerify> basePhoneList = phoneVerifyDao.findByUserAndPh(userId,phone , isNew);
        PhoneVerify phoneVerify = new PhoneVerify();
        if(basePhoneList != null && basePhoneList.size() > 0){
            BeanUtils.copyPropertiesByModel(phoneVerify, basePhoneList.get(0));
        }
        return phoneVerify;
    }

    @Override
    public void updatePhoneByVerify(PhoneVerify phoneVerify) {
        log.info("验证码通过时修改短信验证记录");
        phoneVerify.setVerify(true);
        phoneVerify.setVerifyTime(new Date());
        BasePhoneVerify basePhoneVerify = new BasePhoneVerify();
        BeanUtils.copyPropertiesByModel(basePhoneVerify, phoneVerify);
        phoneVerifyDao.merge(basePhoneVerify);
    }

}
