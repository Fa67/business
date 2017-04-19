package com.eayun.schedule.thread.status;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.schedule.service.CloudVpnService;
import com.eayun.virtualization.model.CloudVpn;

public class CloudVpnStatusThread implements Runnable {
    private static final Logger log = LoggerFactory
            .getLogger(CloudVpnStatusThread.class);
    private CloudVpnService cloudVpnService;

    public CloudVpnStatusThread(CloudVpnService cloudVpnService) {
        this.cloudVpnService = cloudVpnService;
    }

    @Override
    public void run() {
        String value = null;
        boolean isSync = false;
        try {
            value = cloudVpnService.pop(RedisKey.vpnKey);
            JSONObject valueJson = JSONObject.parseObject(value);
            CloudVpn cloudVpn = new CloudVpn();
//            CloudVpn cloudVpn = JSON.parseObject(value, CloudVpn.class);
            if (null != value) {
                cloudVpn.setOrderNo(valueJson.getString("orderNo"));
                cloudVpn.setDcId(valueJson.getString("dcId"));
                cloudVpn.setPrjId(valueJson.getString("prjId"));
                cloudVpn.setVpnId(valueJson.getString("vpnId"));
                cloudVpn.setVpnserviceId(valueJson.getString("vpnserviceId"));
                cloudVpn.setVpnServiceStatus(valueJson.getString("vpnStatus"));
                cloudVpn.setVpnStatus(valueJson.getString("connStatus"));
                cloudVpn.setCusId(valueJson.getString("cusId"));
                cloudVpn.setCount(Integer.parseInt(valueJson.getString("count")));
                log.info("从VPN队列中取出：" + value);
                JSONObject json = cloudVpnService.get(valueJson);
                log.info("底层返回JSON:" + json);
                String stackVpnStatus = json.getString("vpnStatus");
                String stackConnStatus = json.getString("connStatus");
                if ("true".equals(json.getString("deletingStatus"))) {
                    isSync = true;
                    cloudVpnService.deleteVpn(cloudVpn);
                }
                if (!StringUtils.isEmpty(stackVpnStatus) && !StringUtils.isEmpty(stackConnStatus)) {
                    stackVpnStatus = stackVpnStatus.toUpperCase();
                    stackConnStatus = stackConnStatus.toUpperCase();
                    if (!stackVpnStatus.equals(cloudVpn.getVpnServiceStatus()) || !stackConnStatus.equals(cloudVpn.getVpnStatus())) {
                        cloudVpn.setVpnServiceStatus(stackVpnStatus);
                        cloudVpn.setVpnStatus(stackConnStatus);
//                        isSync = true;

                        isSync = cloudVpnService.updateVpn(cloudVpn);
                    }
                }
                if (isSync) {
                    log.info("VPN ID：" + cloudVpn.getVpnserviceId() + "状态刷新成功，移除任务调度！");
                } else {
                    int count = cloudVpn.getCount();
                    if (count > 100) {
                        log.info("VPN ID：" + cloudVpn.getVpnserviceId() + "已执行" + count + "次状态未刷新，移除任务调度！");
                        cloudVpnService.vpnRollBack(cloudVpn);
                    } else {
                        valueJson.put("count", count + 1);
                        log.info("VPN ID：" + cloudVpn.getVpnserviceId() + "状态未刷新，等待下次调度！");
                        cloudVpnService.push(RedisKey.vpnKey, valueJson.toJSONString());
                    }
                }
            }

        } catch (Exception e) {
            if (null != value) {
                cloudVpnService.push(RedisKey.vpnKey, value);
            }
            log.error(e.getMessage(), e);
        }
    }
}
