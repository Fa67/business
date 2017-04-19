package com.eayun.schedule.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.PayType;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.constant.ResourceSyncConstant;
import com.eayun.common.constant.SyncProgress.SyncByDatacenterTypes;
import com.eayun.common.constant.SyncProgress.SyncType;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.sync.SyncProgressUtil;
import com.eayun.common.util.CloudResourceUtil;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.service.OpenstackVpnConnectionService;
import com.eayun.eayunstack.service.OpenstackVpnIkePolicyService;
import com.eayun.eayunstack.service.OpenstackVpnIpSecPolicyService;
import com.eayun.eayunstack.service.OpenstackVpnService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.schedule.service.CloudVpnService;
import com.eayun.virtualization.dao.CloudVpnConnDao;
import com.eayun.virtualization.dao.CloudVpnDao;
import com.eayun.virtualization.dao.CloudVpnIkePolicyDao;
import com.eayun.virtualization.dao.CloudVpnIpSecPolicyDao;
import com.eayun.virtualization.model.BaseCloudVpn;
import com.eayun.virtualization.model.BaseCloudVpnConn;
import com.eayun.virtualization.model.BaseCloudVpnIkePolicy;
import com.eayun.virtualization.model.BaseCloudVpnIpSecPolicy;
import com.eayun.virtualization.model.CloudOrderVpn;
import com.eayun.virtualization.model.CloudVpn;
import com.eayun.virtualization.service.VpnService;
@Transactional
@Service
public class CloudStatusVpnServiceImpl implements CloudVpnService {
    private static final Logger log = LoggerFactory
            .getLogger(CloudStatusVpnServiceImpl.class);
    @Autowired
    private JedisUtil jedisUtil;
    @Autowired
    private OpenstackVpnService openstackVpnService;
    @Autowired
    private OpenstackVpnIkePolicyService openstackIkeService;
    @Autowired
    private OpenstackVpnIpSecPolicyService openstackIpsecService;
    @Autowired
    private OpenstackVpnConnectionService openstackVpnConnService;
    @Autowired
    private CloudVpnDao cloudVpnDao;
    @Autowired
    private CloudVpnIkePolicyDao ikeDao;
    @Autowired
    private CloudVpnIpSecPolicyDao ipsecDao;
    @Autowired
    private CloudVpnConnDao connDao;
    @Autowired
    private VpnService vpnService;
    @Autowired
    private EcmcLogService ecmcLogService;
    @Autowired
    private SyncProgressUtil syncProgressUtil;

    public String pop(String groupKey) {
        String value = null;
        try {
            value = jedisUtil.pop(groupKey);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return value;
    }

    public boolean push(String groupKey, String value) {
        boolean flag = false;
        try {
            flag = jedisUtil.push(groupKey, value);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            flag = false;
        }
        return flag;
    }

    public JSONObject get(JSONObject valueJson) throws Exception {
        JSONObject result = new JSONObject();
        JSONObject vpn = null;
        JSONObject conn = null;
        if (null != valueJson) {
            JSONObject jsonVpn = openstackVpnService.get(
                    valueJson.getString("dcId"),
                    valueJson.getString("vpnserviceId"));
            JSONObject jsonConn = openstackVpnConnService.get(
                    valueJson.getString("dcId"),
                    valueJson.getString("vpnId"));
            if (null != jsonVpn && null != jsonConn) {
                String jsonVpnStr = jsonVpn.toJSONString();
                String jsonConnStr = jsonConn.toJSONString();
                boolean isDeleted = jsonVpnStr.contains("NotFound") || jsonConnStr.contains("NotFound");
                if (!isDeleted) {
                    vpn = jsonVpn
                            .getJSONObject(OpenstackUriConstant.VPN_DATA_NAME);
                    conn = jsonConn
                            .getJSONObject(OpenstackUriConstant.IPSEC_CONNECTION_DATA_NAME);
                    result.put("vpnStatus", vpn.getString("status"));
                    result.put("connStatus", conn.getString("status"));
                } else {
                    result.put("deletingStatus", isDeleted + "");
                }
            }
        }
        return result;
    }

    public boolean updateVpn(CloudVpn cloudVpn) throws Exception {
        boolean flag = false;
        try {
            BaseCloudVpn vpn = cloudVpnDao.findOne(cloudVpn.getVpnserviceId());
            BaseCloudVpnConn conn = connDao.findOne(cloudVpn.getVpnId());
            vpn.setVpnServiceStatus(cloudVpn.getVpnServiceStatus());
            conn.setVpnStatus(cloudVpn.getVpnStatus());
            if ("ACTIVE".equals(cloudVpn.getVpnServiceStatus())
                    && ("ACTIVE".equals(cloudVpn.getVpnStatus()) || "DOWN".equals(cloudVpn.getVpnStatus()))) {
                conn.setIsVisible("1");
                vpnService.vpnCreateSuccessHandle(cloudVpn.getDcId(), cloudVpn.getOrderNo(), cloudVpn.getCusId(), conn.getVpnId(), vpn.getVpnServiceName(), conn.getPayType());
                flag = true;
            } else if ("DOWN".equals(cloudVpn.getVpnServiceStatus())) {
                CloudOrderVpn orderVpn = assembleBeanForOrder(conn, cloudVpn.getCusId(), cloudVpn.getOrderNo());
                vpnService.vpnCreateCallback(orderVpn, 4);
                return true;
            }
            cloudVpnDao.saveOrUpdate(vpn);
            connDao.saveOrUpdate(conn);
        } catch (Exception e) {
            flag = false;
            log.error(e.getMessage(), e);
            throw e;
        }
        return flag;
    }

    public boolean deleteVpn(CloudVpn cloudVpn) {
        boolean flag = false;
        log.info("**********************************delete vpn**********************************");
        BaseCloudVpnConn baseConn = connDao.findOne(cloudVpn.getVpnId());
        if (baseConn != null) {
            cloudVpnDao.delete(baseConn.getVpnserviceId());
            ikeDao.delete(baseConn.getIkeId());
            ipsecDao.delete(baseConn.getIpsecId());
            connDao.delete(baseConn);
        }
        return flag;
    }
    
    public void synchData(BaseDcDataCenter dataCenter) throws Exception {
        synchVpnserviceData(dataCenter);
        synchIkeData(dataCenter);
        synchIpsecData(dataCenter);
        synchVpnConnData(dataCenter);
    }
    /**
     * 同步底层数据中心下的vpnservice资源
     * @author gaoxiang
     * @data 2016-8-31
     * @param dataCenter
     */
    private void synchVpnserviceData(BaseDcDataCenter dataCenter) {
        
    }
    /**
     * 同步底层数据中心下的ike资源
     * @author gaoxiang
     * @data 2016-8-31
     * @param dataCenter
     */
    private void synchIkeData(BaseDcDataCenter dataCenter) {
            
    }
    /**
     * 同步底层数据中心下的ipsec资源
     * @author gaoxiang
     * @data 2016-8-31
     * @param dataCenter
     */
    private void synchIpsecData(BaseDcDataCenter dataCenter) {
        
    }
    /**
     * 同步底层数据中心下的vpnconn资源
     * @author gaoxiang
     * @data 2016-8-31
     * @param dataCenter
     */
    private void synchVpnConnData(BaseDcDataCenter dataCenter) throws Exception {
        Map<String, BaseCloudVpnConn> dbMap = new HashMap<String, BaseCloudVpnConn>();
        Map<String, BaseCloudVpnConn> stackMap = new HashMap<String, BaseCloudVpnConn>();
        try {
            List<BaseCloudVpnConn> dbList= queryCloudVpnConnListByDcId(dataCenter.getId());
            
            List<BaseCloudVpnConn> stackList = openstackVpnConnService.getStackList(dataCenter);
            /*map存储上层数据库资源数据*/
            if (null != dbList) {
                for (BaseCloudVpnConn vpnConn : dbList) {
                    dbMap.put(vpnConn.getVpnId(), vpnConn);
                }
            }
            long total = stackList == null ? 0L : stackList.size();
            syncProgressUtil.initResourceTotal(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.VPN, total);
            /*底层数据更新本地数据库*/
            if (null != stackList) {
                for (BaseCloudVpnConn vpnConn : stackList) {
                    /*校验底层获取的vpn数据资源是否完整*/
                    boolean isCompleted = isStackDataCompleted(dataCenter.getId(), vpnConn);
                    if (isCompleted) {
                        BaseCloudVpn vpn = openstackVpnService.getStackById(dataCenter.getId(), vpnConn.getVpnserviceId());
                        vpn.setVpnServiceId(vpnConn.getVpnserviceId());
                        BaseCloudVpnIkePolicy ike = openstackIkeService.getStackById(dataCenter.getId(), vpnConn.getIkeId());
                        ike.setIkeId(vpnConn.getIkeId());
                        BaseCloudVpnIpSecPolicy ipsec = openstackIpsecService.getStackById(dataCenter.getId(), vpnConn.getIpsecId());
                        ipsec.setIpSecId(vpnConn.getIpsecId());
                        if(dbMap.containsKey(vpnConn.getVpnId())){  
                            //底层数据存在本地数据库中 更新本地数据
                            updateCloudVpnConnFromStack(vpnConn);
                            updateCloudVpnServiceFromStack(vpn);
                            updateCloudVpnIkePolicyFromStack(ike);
                            updateCloudVpnIpSecPolicyFromStack(ipsec);
                        } else {
                            /*底层有 上层没有的数据 添加进本地数据库 不可见*/
                            vpnConn.setIsVisible("0");
                            connDao.save(vpnConn);
                            cloudVpnDao.save(vpn);
                            ikeDao.save(ike);
                            ipsecDao.save(ipsec);
                        }
                        stackMap.put(vpnConn.getVpnId(), vpnConn);
                    }
                    syncProgressUtil.incrResourceDone(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.VPN);
                }
            }
            /*删除本地存在 底层不存在的数据资源*/
            if (null != dbList) {
                for (BaseCloudVpnConn vpnConn : dbList) {
                    //删除本地数据库中不存在于底层的数据
                    if (!stackMap.containsKey(vpnConn.getVpnId())) {
                        connDao.delete(vpnConn.getVpnId());
                        if (getNumberOfVpnService(vpnConn.getVpnserviceId()) == 0) {
                            cloudVpnDao.delete(vpnConn.getVpnserviceId());
                        }
                        if (getNumberOfVpnIkePolicy(vpnConn.getIkeId()) == 0) {
                            ikeDao.delete(vpnConn.getIkeId());
                        }
                        if (getNumberOfVpnIpSecPolicy(vpnConn.getIpsecId()) == 0) {
                            ipsecDao.delete(vpnConn.getIpsecId());
                        }
                        ecmcLogService.addLog("同步资源清除数据", toType(vpnConn), vpnConn.getVpnName(), vpnConn.getPrjId(), 1, vpnConn.getVpnId(), null);
                        
                        JSONObject json = new JSONObject();
                        json.put("resourceType", ResourceSyncConstant.VPN);
                        json.put("resourceId", vpnConn.getVpnId());
                        json.put("resourceName", vpnConn.getVpnName());
                        json.put("synTime", new Date());
                        jedisUtil.push(RedisKey.DATACENTER_SYNC_DELETED_RESOURCE, json.toJSONString());
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }
    
    @SuppressWarnings("unchecked")
    public List<BaseCloudVpnConn> queryCloudVpnConnListByDcId(String dcId) {
        StringBuffer hql = new StringBuffer();
        hql.append(" from BaseCloudVpnConn ");
        hql.append(" where dcId = ? ");
        
        return connDao.find(hql.toString(), new Object[]{dcId});
    }
    /*判断底层vpn数据是否完整*/
    private boolean isStackDataCompleted (String dcId, BaseCloudVpnConn vpnConn) {
        try {
            BaseCloudVpn vpnservice = openstackVpnService.getStackById(dcId, vpnConn.getVpnserviceId());
            if (null != vpnservice) {
                BaseCloudVpnIkePolicy ikePolicy = openstackIkeService.getStackById(dcId, vpnConn.getIkeId());
                if (null != ikePolicy) {
                    BaseCloudVpnIpSecPolicy ipsecPolicy = openstackIpsecService.getStackById(dcId, vpnConn.getIpsecId());
                    if (null != ipsecPolicy) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return false;
    }
    
    private boolean updateCloudVpnConnFromStack(BaseCloudVpnConn vpnConn) {
        boolean flag = false;
        try {
            StringBuffer sql = new StringBuffer();
            sql.append(" update cloud_vpnconn   ");
            sql.append(" set                    ");
            sql.append("    vpn_name = ?        ");
            sql.append("    ,vpn_status = ?     ");
            sql.append("    ,prj_id = ?         ");
            sql.append("    ,peer_address = ?  ");
            sql.append("    ,peer_id = ?        ");
            sql.append("    ,peer_cidrs = ?     ");
            sql.append("    ,psk_key = ?        ");
            sql.append("    ,mtu = ?            ");
            sql.append("    ,dpd_action = ?     ");
            sql.append("    ,dpd_interval = ?   ");
            sql.append("    ,dpd_timeout = ?    ");
            sql.append("    ,initiator = ?      ");
            sql.append("    ,vpnservice_id = ?  ");
            sql.append("    ,ike_id = ?         ");
            sql.append("    ,ipsec_id = ?       ");
            sql.append(" where vpn_id = ?       ");
            connDao.execSQL(sql.toString(), new Object[]{
                vpnConn.getVpnName(),
                vpnConn.getVpnStatus(),
                vpnConn.getPrjId(),
                vpnConn.getPeerAddress(),
                vpnConn.getPeerId(),
                vpnConn.getPeerCidrs(),
                vpnConn.getPskKey(),
                vpnConn.getMtu(),
                vpnConn.getDpdAction(),
                vpnConn.getDpdInterval(),
                vpnConn.getDpdTimeout(),
                vpnConn.getInitiator(),
                vpnConn.getVpnserviceId(),
                vpnConn.getIkeId(),
                vpnConn.getIpsecId(),
                
                vpnConn.getVpnId()
            });
        } catch (Exception e) {
            flag = false;
            log.error(e.getMessage(),e);
            throw e;
        }
        return flag;
    }
    
    private boolean updateCloudVpnServiceFromStack(BaseCloudVpn vpn) {
        boolean flag = false;
        try {
            StringBuffer sql = new StringBuffer();
            sql.append(" update cloud_vpnservice    ");
            sql.append(" set                        ");
            sql.append("    vpnservice_name = ?     ");
            sql.append("    ,vpn_status = ?  ");
            sql.append("    ,prj_id = ?             ");
            sql.append("    ,route_id = ?           ");
            sql.append("    ,subnet_id = ?          ");
            sql.append(" where vpnservice_id = ?    ");
            connDao.execSQL(sql.toString(), new Object[]{
                vpn.getVpnServiceName(),
                vpn.getVpnServiceStatus(),
                vpn.getPrjId(),
                vpn.getRouteId(),
                vpn.getSubnetId(),
                
                vpn.getVpnServiceId()
            });
        } catch (Exception e) {
            flag = false;
            log.error(e.getMessage(),e);
            throw e;
        }
        return flag;
    }
    
    private boolean updateCloudVpnIkePolicyFromStack(BaseCloudVpnIkePolicy ike) {
        boolean flag = false;
        try {
            StringBuffer sql = new StringBuffer();
            sql.append(" update cloud_vpnikepolicy      ");
            sql.append(" set                            ");
            sql.append("    ike_name = ?                ");
            sql.append("    ,ike_version = ?            ");
            sql.append("    ,prj_id = ?                 ");
            sql.append("    ,auth_algorithm = ?         ");
            sql.append("    ,encryption_algorithm = ?   ");
            sql.append("    ,negotiation_mode = ?       ");
            sql.append("    ,lifetime_value = ?         ");
            sql.append("    ,dh_algorithm = ?           ");
            sql.append(" where ike_id = ?               ");
            connDao.execSQL(sql.toString(), new Object[]{
                ike.getIkeName(),
                ike.getIkeVersion(),
                ike.getPrjId(),
                ike.getAuthAlgorithm(),
                ike.getEncryption(),
                ike.getNegotiation(),
                ike.getLifetimeValue(),
                ike.getDhAlgorithm(),
                
                ike.getIkeId()
            });
        } catch (Exception e) {
            flag = false;
            log.error(e.getMessage(),e);
            throw e;
        }
        return flag;
    }
    
    private boolean updateCloudVpnIpSecPolicyFromStack(BaseCloudVpnIpSecPolicy ipsec) {
        boolean flag = false;
        try {
            StringBuffer sql = new StringBuffer();
            sql.append(" update cloud_vpnipsecpolicy      ");
            sql.append(" set                            ");
            sql.append("    ipsec_name = ?              ");
            sql.append("    ,prj_id = ?                 ");
            sql.append("    ,transform_protocol = ?     ");
            sql.append("    ,auth_algorithm = ?         ");
            sql.append("    ,encapsulation_mode = ?     ");
            sql.append("    ,encryption_algorithm = ?   ");
            sql.append("    ,lifetime_value = ?         ");
            sql.append("    ,dh_algorithm = ?           ");
            sql.append(" where ipsec_id = ?             ");
            connDao.execSQL(sql.toString(), new Object[]{
                ipsec.getIpSecName(),
                ipsec.getPrjId(),
                ipsec.getTransform(),
                ipsec.getAuthAlgorithm(),
                ipsec.getEncapsulation(),
                ipsec.getEncryption(),
                ipsec.getLifetimeValue(),
                ipsec.getDhAlgorithm(),
                
                ipsec.getIpSecId()
            });
        } catch (Exception e) {
            flag = false;
            log.error(e.getMessage(),e);
            throw e;
        }
        return flag;
    }
    
    @SuppressWarnings("unchecked")
    private int getNumberOfVpnService(String vpnserviceId) {
        StringBuffer hql = new StringBuffer();
        hql.append("FROM                    ");
        hql.append("    BaseCloudVpnConn    ");
        hql.append("WHERE                   ");
        hql.append("    vpnserviceId = ?    ");
        List<BaseCloudVpnConn> list = connDao.find(hql.toString(), new Object[]{vpnserviceId});
        if (list != null) {
            return list.size();
        } else {
            return 0;
        }
    }
    
    @SuppressWarnings("unchecked")
    private int getNumberOfVpnIkePolicy(String ikeId) {
        StringBuffer hql = new StringBuffer();
        hql.append("FROM                        ");
        hql.append("    BaseCloudVpnIkePolicy   ");
        hql.append("WHERE                       ");
        hql.append("    ikeId = ?               ");
        List<BaseCloudVpnIkePolicy> list = ikeDao.find(hql.toString(), new Object[]{ikeId});
        if (list != null) {
            return list.size();
        } else {
            return 0;
        }
    }
    
    @SuppressWarnings("unchecked")
    private int getNumberOfVpnIpSecPolicy(String ipsecId) {
        StringBuffer hql = new StringBuffer();
        hql.append("FROM                        ");
        hql.append("    BaseCloudVpnIpSecPolicy ");
        hql.append("WHERE                       ");
        hql.append("    ipSecId = ?             ");
        List<BaseCloudVpnIpSecPolicy> list = ikeDao.find(hql.toString(), new Object[]{ipsecId});
        if (list != null) {
            return list.size();
        } else {
            return 0;
        }
    }
    /**
     * 拼装cloudordervpn的bean，来为调用资源回滚接口做准备
     * @param vpnConn
     * @param cusId
     * @param orderNo
     * @return
     */
    private CloudOrderVpn assembleBeanForOrder(BaseCloudVpnConn vpnConn, String cusId, String orderNo) {
        CloudOrderVpn cloudOrderVpn = new CloudOrderVpn();
        cloudOrderVpn.setDcId(vpnConn.getDcId());
        cloudOrderVpn.setPrjId(vpnConn.getPrjId());
        cloudOrderVpn.setCusId(cusId);
        cloudOrderVpn.setOrderNo(orderNo);
        cloudOrderVpn.setVpnId(vpnConn.getVpnId());
        cloudOrderVpn.setVpnName(vpnConn.getVpnName());
        cloudOrderVpn.setVpnserviceId(vpnConn.getVpnserviceId());
        cloudOrderVpn.setIkeId(vpnConn.getIkeId());
        cloudOrderVpn.setIpsecId(vpnConn.getIpsecId());
        return cloudOrderVpn;
    }
    @Override
    public void vpnRollBack(CloudVpn cloudVpn) throws Exception {
        BaseCloudVpnConn conn = connDao.findOne(cloudVpn.getVpnId());
        CloudOrderVpn orderVpn = assembleBeanForOrder(conn, cloudVpn.getCusId(), cloudVpn.getOrderNo());
        try {
            vpnService.vpnCreateCallback(orderVpn, 4);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }
    /**
     * 拼装同步删除发送日志的资源类型
     * @author gaoxiang
     * @param vpnConn
     * @return
     */
    private String toType(BaseCloudVpnConn vpnConn) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuffer resourceType = new StringBuffer();
        resourceType.append(ResourceSyncConstant.VPN);
        resourceType.append("-").append(CloudResourceUtil.escapePayType(vpnConn.getPayType()));
        if(null != vpnConn && null != vpnConn.getCreateTime()){
        	resourceType.append(ResourceSyncConstant.SEPARATOR).append("创建时间：").append(sdf.format(vpnConn.getCreateTime()));
        }
        if (PayType.PAYBEFORE.equals(vpnConn.getPayType())) {
            resourceType.append(ResourceSyncConstant.SEPARATOR).append("到期时间：").append(sdf.format(vpnConn.getEndTime()));
        }
        return resourceType.toString();
    }
}
