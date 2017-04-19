package com.eayun.common.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eayun.common.constant.RedisKey;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.service.SerialNumService;

@Service
public class SerialNumServiceImpl implements SerialNumService {
    private static final Logger log = LoggerFactory.getLogger(SerialNumServiceImpl.class);
    @Autowired
    private JedisUtil jedisUtil;

    @Override
    public String getSerialNum(String prefix, int suffixLength) {

        long serialNum = jedisUtil.increase(RedisKey.SERIALNUM + prefix);
        StringBuffer result = new StringBuffer();
        result.append(prefix);

        int zeroLenth = suffixLength - String.valueOf(serialNum).length();

        for (int i = 0; i < zeroLenth; i++) {
            result.append("0");
        }
        result.append(serialNum);

        log.info("生成流水号:"+result.toString());
        return result.toString();
    }

}
