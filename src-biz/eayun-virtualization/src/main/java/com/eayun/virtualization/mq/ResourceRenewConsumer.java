package com.eayun.virtualization.mq;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.constant.ResourceType;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.database.instance.model.CloudRDSInstance;
import com.eayun.database.instance.service.RDSInstanceService;
import com.eayun.log.service.LogService;
import com.eayun.order.model.BaseOrderResource;
import com.eayun.order.model.Order;
import com.eayun.order.service.OrderService;
import com.eayun.syssetup.service.SysDataTreeService;
import com.eayun.virtualization.model.CloudFloatIp;
import com.eayun.virtualization.model.CloudLdPool;
import com.eayun.virtualization.model.CloudNetWork;
import com.eayun.virtualization.model.CloudVm;
import com.eayun.virtualization.model.CloudVolume;
import com.eayun.virtualization.model.CloudVpn;
import com.eayun.virtualization.service.CloudFloatIpService;
import com.eayun.virtualization.service.NetWorkService;
import com.eayun.virtualization.service.PoolService;
import com.eayun.virtualization.service.VmService;
import com.eayun.virtualization.service.VolumeService;
import com.eayun.virtualization.service.VpnService;
import com.rabbitmq.client.Channel;
@Transactional
@Component
public class ResourceRenewConsumer  implements ChannelAwareMessageListener{

    private static final Logger log = LoggerFactory.getLogger(ResourceRenewConsumer.class);

    @Autowired
    private VmService vmService;
    @Autowired
    private VolumeService volumeService;
    @Autowired
    private LogService logService;
    @Autowired
    private PoolService lbService;
    @Autowired
    private NetWorkService networkService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private CloudFloatIpService cloudFloatIpService;
    @Autowired
    private VpnService vpnService;
    @Autowired
    private SysDataTreeService sysDataTreeService;
    @Autowired
    private RDSInstanceService rdsInstanceService;

    @Override
    public void onMessage(Message msg,Channel channel) throws Exception {
        log.info("监听到预付费资源续费成功消息");
        String msgBody = new String(msg.getBody());
        JSONObject json = JSONObject.parseObject(msgBody);
        String orderNo = json.getString("orderNo");
        String actPerson =json.getString("actPerson");
        String cusId =json.getString("cusId"); 

        JSONObject params = json.getJSONObject("params");
        String resourceType = params.getString("resourceType");
        String resourceId = params.getString("resourceId");
        String resourceName = params.getString("resourceName");
        Date expirationDate = params.getTimestamp("expirationDate");
        int duration = params.getInteger("duration");
        String operatorIp = params.getString("operatorIp");

        int count = 0;
        while(count<100){
        	count++;
        	Order order = orderService.getOrderByNo(orderNo);
        	if(order != null && !StringUtil.isEmpty(order.getOrderId())){
        		log.info("订单【"+orderNo+"】已保存。");
        		break;
        	}
        	log.info("订单【"+orderNo+"】还未保存，先等待一会。等待"+count+"次");
        	Thread.sleep(50);
        }

        //判断是否需要恢复资源服务，如果需要恢复则表示超过保留时长，即在保留时长内=false
        String keepTimeStr = sysDataTreeService.getRecoveryTime();
        int keepTime = Integer.valueOf(keepTimeStr);
        boolean isResumable = isResumable(expirationDate,keepTime);
        //计算得到续费后的到期时间
        Date endTime = this.getRenewEndTime(expirationDate, duration);

        List<BaseOrderResource> resourceList = new ArrayList<>();
        BaseOrderResource baseOrderResource = new BaseOrderResource();
        baseOrderResource.setOrderNo(orderNo);
        baseOrderResource.setResourceId(resourceId);
        baseOrderResource.setResourceName(resourceName);
        resourceList.add(baseOrderResource);

        Object obj = new Object();
        try{
            if(resourceType.equals(ResourceType.VM)){
                CloudVm cloudVm = vmService.findVm(resourceId);
                obj = cloudVm;

                vmService.modifyStateForVm(resourceId, "0", endTime, false, isResumable);
                //修改系统盘的状态    需要根据当前vmId获取系统盘
				CloudVolume  CloudVolume = volumeService.getOsVolumeByVmId(resourceId);
				volumeService.modifyStateForVol(CloudVolume.getVolId(), "0", endTime, false);
				
                logService.addLog("云主机续费",actPerson, ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),cusId,ConstantClazz.LOG_STATU_SUCCESS, operatorIp, null);
            }
            else if(resourceType.equals(ResourceType.VDISK)){
                CloudVolume cloudVolume = volumeService.getVolumeById(null, null, resourceId);
                obj = cloudVolume;

                volumeService.modifyStateForVol(resourceId, "0", endTime, false);
                logService.addLog("云硬盘续费",actPerson, ConstantClazz.LOG_TYPE_DISK, cloudVolume.getVolName(), cloudVolume.getPrjId(), cusId, ConstantClazz.LOG_STATU_SUCCESS, operatorIp, null);
            }
            else if(resourceType.equals(ResourceType.NETWORK)){
                CloudNetWork net = networkService.findNetWorkByNetId(resourceId);
                obj = net;
                //私有网络带宽即使超过保留时长也不需要恢复到原带宽，所以最后一个参数为false，为了不影响下面根据isResumable去判断是否在保留时长内并调用完成订单，这里之恩那个手动写一个false，而不能把ieResumable=false！！！
                networkService.modifyStateForNetWork(resourceId, "0", endTime, false, false);
                logService.addLog("私有网络续费", actPerson, ConstantClazz.LOG_TYPE_NET, net.getNetName(), net.getPrjId(), cusId, ConstantClazz.LOG_STATU_SUCCESS, operatorIp, null);
            }
            else if(resourceType.equals(ResourceType.QUOTAPOOL)){
                CloudLdPool lb = lbService.getLoadBalanceById(resourceId);
                obj = lb;

                lbService.modifyStateForLdPool(resourceId, "0", endTime, false, isResumable);
                logService.addLog("负载均衡续费", actPerson, ConstantClazz.LOG_TYPE_POOL, lb.getPoolName(), lb.getPrjId(), cusId, ConstantClazz.LOG_STATU_SUCCESS, operatorIp, null);
            }
            else if(resourceType.equals(ResourceType.FLOATIP)){
            	CloudFloatIp floatIp = cloudFloatIpService.findFloatIpById(resourceId);
            	obj = floatIp;
            	
            	cloudFloatIpService.modifyStateForFloatIp(resourceId, "0", endTime);
            	logService.addLog("弹性公网IP续费", actPerson, ConstantClazz.LOG_TYPE_FLOATIP, floatIp.getFloIp(), floatIp.getPrjId(), cusId, ConstantClazz.LOG_STATU_SUCCESS, operatorIp, null);
            }
            else if(resourceType.equals(ResourceType.VPN)){
                CloudVpn vpn = vpnService.getVpnById(resourceId);
                obj = vpn;

                vpnService.modifyStateForVPN(resourceId, "0", endTime, false, isResumable);
                logService.addLog("VPN续费", actPerson, ConstantClazz.LOG_TYPE_VPN, vpn.getVpnName(), vpn.getPrjId(), cusId, ConstantClazz.LOG_STATU_SUCCESS, operatorIp, null);
            }
            else if(resourceType.equals(ResourceType.RDS)){
            	CloudRDSInstance rdsInstance = rdsInstanceService.findRDSInstanceByRdsId(resourceId);
            	obj = rdsInstance;
            	
            	rdsInstanceService.modifyStateForRdsInstance(resourceId, "0", endTime,false, isResumable);
            	logService.addLog("云数据库续费", actPerson, ConstantClazz.LOG_TYPE_RDS, rdsInstance.getRdsName(), rdsInstance.getPrjId(), cusId, ConstantClazz.LOG_STATU_SUCCESS, operatorIp, null);
            }
            //状态、到期时间修改完毕后，调用完成订单接口
            orderService.completeOrder(orderNo, true, resourceList, !isResumable, expirationDate);
        }catch(Exception e){
            if(resourceType.equals(ResourceType.VM)){
                CloudVm cloudVm = (CloudVm) obj;
                logService.addLog("云主机续费", actPerson, ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(), cusId, ConstantClazz.LOG_STATU_ERROR, operatorIp, e);
            }else if(resourceType.equals(ResourceType.VDISK)){
                CloudVolume cloudVolume = (CloudVolume) obj;
                logService.addLog("云硬盘续费",actPerson, ConstantClazz.LOG_TYPE_DISK, cloudVolume.getVolName(), cloudVolume.getPrjId(),cusId, ConstantClazz.LOG_STATU_ERROR, operatorIp, e);
            }else if(resourceType.equals(ResourceType.NETWORK)){
                CloudNetWork net = (CloudNetWork) obj;
                logService.addLog("私有网络续费", actPerson, ConstantClazz.LOG_TYPE_NET, net.getNetName(), net.getPrjId(), cusId, ConstantClazz.LOG_STATU_ERROR, operatorIp, e);

            }else if(resourceType.equals(ResourceType.QUOTAPOOL)){
                CloudLdPool lb = (CloudLdPool) obj;
                logService.addLog("负载均衡续费", actPerson, ConstantClazz.LOG_TYPE_POOL, lb.getPoolName(), lb.getPrjId(),cusId,  ConstantClazz.LOG_STATU_ERROR, operatorIp, e);

            }else if(resourceType.equals(ResourceType.FLOATIP)){
            	CloudFloatIp floatIp = (CloudFloatIp) obj;
            	logService.addLog("弹性公网IP续费", actPerson, ConstantClazz.LOG_TYPE_FLOATIP, floatIp.getFloIp(), floatIp.getPrjId(),cusId,  ConstantClazz.LOG_STATU_ERROR, operatorIp, e);
            }else if(resourceType.equals(ResourceType.VPN)){
                CloudVpn vpn = (CloudVpn) obj;
                logService.addLog("VPN续费", actPerson, ConstantClazz.LOG_TYPE_VPN, vpn.getVpnName(), vpn.getPrjId(),cusId,  ConstantClazz.LOG_STATU_ERROR, operatorIp, e);
            }else if(resourceType.equals(ResourceType.RDS)){
                CloudRDSInstance rds = (CloudRDSInstance) obj;
                logService.addLog("云数据库续费", actPerson, ConstantClazz.LOG_TYPE_RDS, rds.getRdsName(), rds.getPrjId(),cusId,  ConstantClazz.LOG_STATU_ERROR, operatorIp, e);
            }
            orderService.completeOrder(orderNo, false, resourceList, !isResumable, expirationDate);
            log.error("资源续费成功后监听中处理失败", e);
        } finally {
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
        }
    }

    /**
     * 求最终"到期时间"
     * （原始到期时间+保留时常）compare 当前时间
     * 大于等于： 取"原到期时间"
     * 小于：  取"当前时间"
     * */
    private Date getRenewEndTime(Date originalEndTime,int duration){
    	int keepTime = Integer.parseInt(sysDataTreeService.getRecoveryTime());//获取获取欠费的保留时长
        Date newDate = new Date();
        Date expiration = DateUtil.addDay(originalEndTime, new int[]{0, 0, 0, keepTime});//加小时  具体保留时长可以配置。
        Date endTime = null;

        int num = 0;
        num = expiration.compareTo(newDate);

        if(num>=0){//到期时间+保留时长>当前续费日期，即当前续费日期在保留市场内
            endTime = DateUtil.getExpirationDate(originalEndTime, duration, DateUtil.RENEWAL);
        }else if(num<0){//即当前续费日期在保留市场外
            endTime =  DateUtil.getExpirationDate(newDate, duration, DateUtil.RENEWAL);
        }
        return endTime;
    }

    /**
     * 计算是否恢复服务
     * 根据原始到期时间和当前时间做比较：
     * 原始到期时间>当前续费时间，取原始到期时间；
     * 反之，取反。
     * @return boolean
     * */
    private boolean isResumable(Date originalEndTime,int hour){

        boolean flag = false;
        Date newDate = new Date();
        Date expiration = DateUtil.addDay(originalEndTime, new int[]{0, 0, 0, hour});//加小时

        int num = 0;
        num = newDate.compareTo(expiration);
        if(num>=0){//超过保留时长 h
            flag = true;
        }
        return flag;
    }


}
