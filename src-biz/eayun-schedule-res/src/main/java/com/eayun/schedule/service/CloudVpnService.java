package com.eayun.schedule.service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.virtualization.model.CloudVpn;

public interface CloudVpnService {

    /**
     * 从消息队列中取出对应Key的其中一条
     * @author gaoxiang
     * @param groupKey
     * @return
     */
    public String pop(String groupKey);
    /**
     * 重新放回队列
     * @author gaoxiang
     * @param groupKey
     * @param value
     * @return
     */
    public boolean push(String groupKey,String value);
    /**
     * 获取底层指定id的资源信息
     * @author gaoxiang
     * @param valueJson
     * @return
     * @throws Exception
     */
    public JSONObject get(JSONObject valueJson) throws Exception;
    /**
     * 修改VPN状态信息
     * @author gaoxiang
     * @param cloudVpn
     * @return
     */
    public boolean updateVpn(CloudVpn cloudVpn) throws Exception;
    /**
     * 删除底层不存在的vpn
     * @author gaoxiang
     * @param cloudPool
     * @return
     */
    public boolean deleteVpn(CloudVpn cloudVpn);
    /**
     * 同步底层数据中心下的vpn资源
     * @author gaoxiang
     * @date 2016-8-31
     * @param dataCenter
     */
    public void synchData(BaseDcDataCenter dataCenter) throws Exception;
    /**
     * 执行资源回滚
     * @author gaoxiang
     */
    public void vpnRollBack(CloudVpn cloudVpn) throws Exception;
}
