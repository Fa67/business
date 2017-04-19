package com.eayun.common.service.impl;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.IpInfo;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.service.IpService;
import com.eayun.common.util.StringUtil;
import com.eayun.common.zk.DistributedLockBean;
import com.eayun.common.zk.DistributedLockService;
import com.eayun.common.zk.LockService;

/**
 * 获得ip信息，通过调用taobao的api，结果缓存在redis中，超时1天
 *                       
 * @Filename: IpServiceImpl.java
 * @Description: 
 * @Version: 1.0
 * @Author: chenhao
 * @Email: hao.chen@eayun.com
 * @History:<br>
 *<li>Date: 2016年7月29日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Service
public class IpServiceImpl implements IpService {
    @Autowired
    private JedisUtil           jedisUtil;

    private static String       TAOBAO_URL     = "http://ip.taobao.com/service/getIpInfo.php?ip=";
    // IP缓存7天
    private static long         EXPIRE_SECONDS = 7 * 24 * 60 * 60l;
    private static final Logger log            = LoggerFactory.getLogger(IpServiceImpl.class);
    
    @Autowired
	private DistributedLockService distributedLockService;
    
    @Override
    public IpInfo getIp(final String ip) {
        if (StringUtil.isEmpty(ip)) {
            throw new AppException("IP不能为空");
        }
        
        DistributedLockBean dlBean=new DistributedLockBean();
	    dlBean.setGranularity("ip:"+ip);
	    dlBean.setLockService(new LockService() {
			@Override
			public Object doService() throws Exception {
				IpInfo bean = null;
				try {
		            String ipStr = jedisUtil.get(RedisKey.IP + ip);
		            if (StringUtil.isEmpty(ipStr)) {
		                ipStr = getTaobaoIp(ip);
		            }
		            if (!StringUtil.isEmpty(ipStr)) {
		                bean = JSONObject.parseObject(ipStr, IpInfo.class);
		            }
		        } catch (Exception e) {
		            log.error(e.getMessage(), e);
		        }
				return bean;
			}
	    });
	    IpInfo bean = (IpInfo) distributedLockService.doServiceByLock(dlBean);
        
        return bean;
    }

    /**
     * 通过淘宝的api得到ip信息，若合法，放入redis中
     * 
     * @param ip
     * @return
     */
    private String getTaobaoIp(String ip) throws Exception {
        String response = getResponse(ip);
        JSONObject json = JSONObject.parseObject(response);

        Integer code = (Integer) json.get("code");
        if (code == 1) {
            return null;
        } else {
            String data = json.getString("data");
            jedisUtil.setEx(RedisKey.IP + ip, data, EXPIRE_SECONDS);
            return data;
        }
    }

    private String getResponse(String ip) throws Exception {
        String resData = "";
        HttpClient httpclient = HttpClients.createDefault();
        HttpGet get = new HttpGet(TAOBAO_URL + ip);
        HttpResponse res;
        res = httpclient.execute(get);
        HttpEntity resEntity = res.getEntity();
        if (resEntity != null) {
            resData = EntityUtils.toString(resEntity);
        }
        return resData;
    }
}
