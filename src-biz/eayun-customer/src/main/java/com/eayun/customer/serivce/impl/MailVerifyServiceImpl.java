package com.eayun.customer.serivce.impl;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.customer.bean.BaseMailVerify;
import com.eayun.customer.bean.MailVerify;
import com.eayun.customer.dao.MailVerifyDao;
import com.eayun.customer.serivce.MailVerifyService;
@Service
@Transactional
public class MailVerifyServiceImpl implements MailVerifyService {
    private static final Logger log = LoggerFactory.getLogger(MailVerifyServiceImpl.class);

    @Autowired
    private MailVerifyDao mailVerifyDao;
    /**
     * 添加邮箱发送记录，时效24小时
     * @param mailVerify
     * @return
     */
    @Override
    public MailVerify addMailVerify(MailVerify mailVerify) {
        log.info("添加邮箱验证记录");
        
        StringBuffer hql = new StringBuffer();
        hql.append("delete BaseMailVerify where userId = ?");
        mailVerifyDao.executeUpdate(hql.toString(), mailVerify.getUserId());
        
        Date date= new Date();
        mailVerify.setSendTime(date);
        mailVerify.setInvalidTime(DateUtil.addDay(date , new int[]{0,0,1}));
        mailVerify.setVerify(false);
        
        BaseMailVerify baseMailVerify = new BaseMailVerify();
        BeanUtils.copyPropertiesByModel(baseMailVerify , mailVerify);
        mailVerifyDao.saveEntity(baseMailVerify);
        BeanUtils.copyPropertiesByModel(mailVerify , baseMailVerify);
        
        return mailVerify;
    }

    @Override
    public MailVerify findById(String verifyId) {
        log.info("根据ID查询邮箱验证记录");
        BaseMailVerify baseMailVerify = mailVerifyDao.findOne(verifyId);
        MailVerify mailVerify = new MailVerify();
        if(baseMailVerify != null){
            BeanUtils.copyPropertiesByModel(mailVerify, baseMailVerify);
        }
        return mailVerify;
    }

    @Override
    public void updateByVerify(MailVerify mailVerify) {
        log.info("点击验证通过时返回修改邮箱验证记录");
        mailVerify.setVerify(true);
        mailVerify.setVerifyTime(new Date());
        BaseMailVerify baseMailVerify = new BaseMailVerify();
        BeanUtils.copyPropertiesByModel(baseMailVerify, mailVerify);
        mailVerifyDao.merge(baseMailVerify);
    }
}
