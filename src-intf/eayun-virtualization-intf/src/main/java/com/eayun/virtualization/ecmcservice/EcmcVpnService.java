package com.eayun.virtualization.ecmcservice;

import java.util.Map;

import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.virtualization.model.CloudOrderVpn;
import com.eayun.virtualization.model.CloudVpn;

public interface EcmcVpnService {
    /**
     * 获取vpn列表页数据
     * @author gaoxiang
     * @param page
     * @param paramsMap
     * @return
     */
    public Page getVpnList(Page page, ParamsMap paramsMap);
    /**
     * 获取vpn详情页信息
     * @author gaoxiang
     * @param vpnId
     * @return
     */
    public CloudVpn getVpnInfo(String vpnId);
    /**
     * 删除VPN信息
     * @param cloudOrderVpn
     */
	public boolean deleteVpn(CloudOrderVpn cloudOrderVpn) throws AppException;
	/**
     * 根据项目查询VPN使用量
     * @author liuzhuangzhuang
     * @param prjId
     * @return
     */
    public int getCountByPrjId(String prjId);
    /**
     * 修改VPN
     * @author liuzhuangzhuang
     * @param cloudVpn
     * @return
     */
	public CloudVpn updateVpn(CloudVpn cloudVpn) throws AppException;
	/**
	 * 检测VPN是否重名
	 * @author liuzhuangzhuang
	 * @param map
	 * @return
	 */
	public boolean checkVpnNameExist(Map<String, String> map) throws Exception ;
}
