package com.eayun.customer.serivce;

import com.eayun.customer.bean.PhoneVerify;

public interface PhoneVerifyService {

    /**
     * 增加一条短信验证记录
     * @param phoneVerify
     * @return
     */
    public PhoneVerify addPhoneVerify(PhoneVerify phoneVerify);
    /**
     * 根据ID查询短信验证记录
     * @param verifyId
     * @return
     */
    public PhoneVerify findByUserAndPh(String verifyId , String phone , String isNew);
    
    public void updatePhoneByVerify(PhoneVerify phoneVerify);
}
