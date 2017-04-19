package com.eayun.customer.serivce.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.common.util.StringUtil;
import com.eayun.customer.bean.MailVerify;
import com.eayun.customer.dao.UserDao;
import com.eayun.customer.filter.SystemConfig;
import com.eayun.customer.model.BaseUser;
import com.eayun.customer.serivce.MailVerifyService;
import com.eayun.customer.serivce.UserMailService;
import com.eayun.customer.serivce.UserSmsService;
import com.eayun.mail.service.MailService;

@Service
@Transactional
public class UserMailServiceImpl implements UserMailService{
    private static final Logger log = LoggerFactory.getLogger(UserSmsServiceImpl.class);

    @Autowired
    private MailService mailService;
    
    @Autowired
    private MailVerifyService mailVerifyService;
    
    @Autowired
    private UserSmsService userSmsService;
    
    @Autowired
    private UserDao userDao;
    
    @Override
    public void sendEmail(String userId , String account , String email, String imgCode, String rightIdCode) {
        log.info("发送验证邮件");
        SystemConfig xml = new SystemConfig();
        Map<String,String> urlMap=xml.findNodeMap();
        String url = urlMap.get("ecscUrl")+"/sys/user";
        
        JSONObject json =JSONObject.parseObject(rightIdCode);
        String rightcode = json.getString("code");
        String startTimeStr = json.getString("startTime");
        long startTime = Long.parseLong(startTimeStr);
        long timeDiff = (System.currentTimeMillis()-startTime-180000);
        
        if (StringUtil.isEmpty(imgCode)) {
            throw new AppException("请输入图片验证码");
        }
        if(timeDiff>0){
            throw new AppException("图片验证码已过期，请重新输入");
        }
        if (!imgCode.equals(rightcode)) {
            throw new AppException("图片验证码不正确，请重新输入");
        }
        /*BaseUser baseuser = userDao.findOne(userId);
        baseuser.setUserEmail(email);
        baseuser.setIsMailValid(false);*/
        
        MailVerify mailVerify = new MailVerify();
        mailVerify.setUserId(userId);
        mailVerify.setEmail(email);
        MailVerify backmailVerify = mailVerifyService.addMailVerify(mailVerify);
        userSmsService.sendSmsForMail(userId);
        
        String title = "请验证您的邮箱";
        String context = "<h2><b>尊敬的"+account+"，您好</b></h2></br>"
                + "感谢您完成此重要步骤以验证您的邮箱。点击下方链接后，您的邮箱将会被绑定：</br><a href = '"
                + url+"/validMail/"+backmailVerify.getId()+".do'>"+url+"/validMail/"+backmailVerify.getId()+".do</a></br>"
                +"<p style='padding: 15px 0;'>易云捷讯科技（北京）股份有限公司</p>"
                +"<p style='padding: 15px 0;color: #999999;font-size: 12px;'>此为系统邮件，请勿回复<br>如有任何疑问，可发送邮件至：<span"
                +" style='color: #0077cc;'>service@eayun.com</span>，或者拨打全国统一客服热线：<span"
                +" style='color: #0077cc;'>400-606-6396</span>，我们的客服人员将会在第一时间为您解答。</p>"
                +"<div style='text-align: center;border-top: 1px solid #DCDCDC;'>"
                +"<p style='color: #999999;font-size: 12px;'>&copy; 2011-2017 易云捷讯科技（北京）股份有限公司, 版权所有</p>"
                +"</div>";
        List<String> links = new ArrayList<>();
        links.add(email);
        try {
            mailService.send(title, context, links);
        } catch (Exception e) {
            log.error("邮件发送失败", e);
            throw new AppException("邮件发送失败");
        }
    }

    @Override
    public void againSendEmail(String userId , String account , String email) {
        
        SystemConfig xml = new SystemConfig();
        Map<String,String> urlMap=xml.findNodeMap();
        String url = urlMap.get("ecscUrl")+"/sys/user";
        log.info("重新发送验证邮件");
        BaseUser baseUser = userDao.findOne(userId);
        email = baseUser.getUserEmail();
        
        MailVerify mailVerify = new MailVerify();
        mailVerify.setUserId(userId);
        mailVerify.setEmail(email);
        MailVerify backmailVerify = mailVerifyService.addMailVerify(mailVerify);
        
        String title = "请验证您的邮箱";
        String context = "<h2>尊敬的"+account+"，您好</h2></br>"
                + "感谢您完成此重要步骤以验证您的邮箱。点击下方链接后，您的邮箱将会被绑定：</br><a href = '"
                + url+"/validMail/"+backmailVerify.getId()+".do'>"+url+"/validMail/"+backmailVerify.getId()+".do</a></br>"
                +"<p style='padding: 15px 0;'>易云捷讯科技（北京）股份有限公司</p>"
                +"<p style='padding: 15px 0;color: #999999;font-size: 12px;'>此为系统邮件，请勿回复<br>如有任何疑问，可发送邮件至：<span"
                +" style='color: #0077cc;'>service@eayun.com</span>，或者拨打全国统一客服热线：<span"
                +" style='color: #0077cc;'>400-606-6396</span>，我们的客服人员将会在第一时间为您解答。</p>"
                +"<div style='text-align: center;border-top: 1px solid #DCDCDC;'>"
                +"<p style='color: #999999;font-size: 12px;'>&copy; 2011-2017 易云捷讯科技（北京）股份有限公司, 版权所有</p>"
                +"</div>";
        List<String> links = new ArrayList<>();
        links.add(email);
        try {
            mailService.send(title, context, links);
        } catch (Exception e) {
            log.error("邮件发送失败", e);
            throw new AppException("邮件发送失败");
        }
    }

}
