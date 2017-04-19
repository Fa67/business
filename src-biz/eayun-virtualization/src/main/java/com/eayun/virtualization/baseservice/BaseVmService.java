package com.eayun.virtualization.baseservice;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.charge.model.ChargeRecord;
import com.eayun.common.ConstantClazz;
import com.eayun.common.constant.EayunQueueConstant;
import com.eayun.common.constant.OrderType;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.constant.ResourceType;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.template.EayunRabbitTemplate;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.util.CloudResourceUtil;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.common.util.TransactionHookUtil;
import com.eayun.common.util.TransactionHookUtil.Hook;
import com.eayun.eayunstack.model.InterfaceAttachment;
import com.eayun.eayunstack.model.Vm;
import com.eayun.eayunstack.service.OpenstackVmService;
import com.eayun.notice.model.MessageOrderResourceNotice;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.price.bean.ParamBean;
import com.eayun.price.bean.UpgradeBean;
import com.eayun.price.service.BillingFactorService;
import com.eayun.sys.model.SysDataTree;
import com.eayun.virtualization.dao.CloudVmDao;
import com.eayun.virtualization.model.BaseCloudBatchResource;
import com.eayun.virtualization.model.BaseCloudSecurityGroup;
import com.eayun.virtualization.model.BaseCloudVm;
import com.eayun.virtualization.model.BaseCloudVmSgroup;
import com.eayun.virtualization.model.BaseCloudVolume;
import com.eayun.virtualization.model.BaseSecretkeyVm;
import com.eayun.virtualization.model.CloudBatchResource;
import com.eayun.virtualization.model.CloudFloatIp;
import com.eayun.virtualization.model.CloudImage;
import com.eayun.virtualization.model.CloudOrderVm;
import com.eayun.virtualization.model.CloudVm;
import com.eayun.virtualization.service.CloudBatchResourceService;
import com.eayun.virtualization.service.SecretkeyVmService;
import com.eayun.virtualization.service.VmSecurityGroupService;
import com.eayun.virtualization.service.VolumeService;

@Transactional
@Service
public class BaseVmService {
	@Autowired
	private CloudVmDao cloudVmDao;
	@Autowired
	private OpenstackVmService openstackVmService;
	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
	private BillingFactorService billingFactorService;
	@Autowired
	private CloudBatchResourceService cloudBatchResourceService;
	@Autowired
	private VmSecurityGroupService vmSgService;
	@Autowired
	private VolumeService volumeService;
	@Autowired 
	private MessageCenterService messageCenterService;
	@Autowired
	private EayunRabbitTemplate rabbitTemplate;
	@Autowired
	private SecretkeyVmService secretkeyVmService;
	

	/**
	 * 编辑云主机<br>
	 * ------------------
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public void modifyVm(CloudVm cloudVm) throws AppException {
		try {
			BaseCloudVm vm = new BaseCloudVm();
			vm = cloudVmDao.findOne(cloudVm.getVmId());
			if (!cloudVm.getVmName().equals(vm.getVmName())) {
				openstackVmService.modifyVm(cloudVm);
				vm.setVmName(cloudVm.getVmName());
			}
			vm.setVmDescripstion(cloudVm.getVmDescripstion());
			
			cloudVmDao.merge(vm);
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			throw new AppException("error.openstack.message");
		}
	}

	/**
	 * 启动云主机<br>
	 * ------------------
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public void restartVm(CloudVm cloudVm) throws AppException {
		try {
			openstackVmService.restartVm(cloudVm);

			BaseCloudVm vm = new BaseCloudVm();
			vm = cloudVmDao.findOne(cloudVm.getVmId());
			vm.setVmStatus("STARTING");

			cloudVmDao.merge(vm);

			JSONObject json = new JSONObject();
			json.put("vmId", vm.getVmId());
			json.put("dcId", vm.getDcId());
			json.put("prjId", vm.getPrjId());
			json.put("vmStatus", vm.getVmStatus());
			json.put("perStatus", "SHUTOFF");
			json.put("count", "0");
			json.put("isExsit", "0");

			final JSONObject data = json;
			TransactionHookUtil.registAfterCommitHook(new Hook() {
				@Override
				public void execute() {
					jedisUtil.addUnique(RedisKey.vmKey, data.toJSONString());
				}
			});
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			throw new AppException("error.openstack.message");
		}
	}

	/**
	 * 关闭云主机 <br>
	 * ------------------
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public void shutdownVm(CloudVm cloudVm) throws AppException {
		try {
			openstackVmService.shutdownVm(cloudVm);

			BaseCloudVm vm = new BaseCloudVm();
			vm = cloudVmDao.findOne(cloudVm.getVmId());
			vm.setVmStatus("SHUTOFFING");

			cloudVmDao.merge(vm);
			JSONObject json = new JSONObject();
			json.put("vmId", vm.getVmId());
			json.put("dcId", vm.getDcId());
			json.put("prjId", vm.getPrjId());
			json.put("vmStatus", vm.getVmStatus());
			json.put("perStatus", "ACTIVE");
			json.put("count", "0");
			json.put("isExsit", "0");

			final JSONObject data = json;
			TransactionHookUtil.registAfterCommitHook(new Hook() {
				@Override
				public void execute() {
					jedisUtil.addUnique(RedisKey.vmKey, data.toJSONString());
				}
			});
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			throw new AppException("error.openstack.message");
		}
	}
	
	/**
	 * 软重启云主机 <br>
	 * ------------------
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public void softRestartVm(CloudVm cloudVm) throws AppException {
		try {
			openstackVmService.softRestartVm(cloudVm);

			BaseCloudVm vm = new BaseCloudVm();
			vm = cloudVmDao.findOne(cloudVm.getVmId());
			vm.setVmStatus("REBOOT");

			cloudVmDao.merge(vm);

			JSONObject json = new JSONObject();
			json.put("vmId", vm.getVmId());
			json.put("dcId", vm.getDcId());
			json.put("prjId", vm.getPrjId());
			json.put("vmStatus", vm.getVmStatus());
			json.put("count", "0");
			json.put("isExsit", "1");
			
			final JSONObject data = json;
			TransactionHookUtil.registAfterCommitHook(new Hook() {
				@Override
				public void execute() {
					jedisUtil.addUnique(RedisKey.vmKey, data.toJSONString());
				}
			});

		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			throw new AppException("error.openstack.message");
		}
	}
	
	/**
	 * 硬重启云主机<br> 
	 * ------------------
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public void hardRestartVm(CloudVm cloudVm) throws AppException {
		try {
			openstackVmService.hardRestartVm(cloudVm);

			BaseCloudVm vm = new BaseCloudVm();
			vm = cloudVmDao.findOne(cloudVm.getVmId());
			vm.setVmStatus("HARD_REBOOT");

			cloudVmDao.merge(vm);

			JSONObject json = new JSONObject();
			json.put("vmId", vm.getVmId());
			json.put("dcId", vm.getDcId());
			json.put("prjId", vm.getPrjId());
			json.put("vmStatus", vm.getVmStatus());
			json.put("count", "0");
			json.put("isExsit", "1");
			
			final JSONObject data = json;
			TransactionHookUtil.registAfterCommitHook(new Hook() {
				@Override
				public void execute() {
					jedisUtil.addUnique(RedisKey.vmKey, data.toJSONString());
				}
			});
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			throw new AppException("error.openstack.message");
		}
	}
	
	/**
	 * 挂起云主机<br> 
	 * ------------------
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public void suspendVm(CloudVm cloudVm) throws AppException {
		try {
			openstackVmService.suspendVm(cloudVm);

			BaseCloudVm vm = new BaseCloudVm();
			vm = cloudVmDao.findOne(cloudVm.getVmId());
			vm.setVmStatus("SUSPENDEDING");

			cloudVmDao.merge(vm);

			JSONObject json = new JSONObject();
			json.put("vmId", vm.getVmId());
			json.put("dcId", vm.getDcId());
			json.put("prjId", vm.getPrjId());
			json.put("vmStatus", vm.getVmStatus());
			json.put("perStatus", "ACTIVE");
			json.put("count", "0");
			json.put("isExsit", "0");

			final JSONObject data = json;
			TransactionHookUtil.registAfterCommitHook(new Hook() {
				@Override
				public void execute() {
					jedisUtil.addUnique(RedisKey.vmKey, data.toJSONString());
				}
			});
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			throw new AppException("error.openstack.message");
		}
	}

	/**
	 * 恢复云主机<br>
	 * ------------------
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public void resumeVm(CloudVm cloudVm) throws AppException {
		try {
			openstackVmService.resumeVm(cloudVm);

			BaseCloudVm vm = new BaseCloudVm();
			vm = cloudVmDao.findOne(cloudVm.getVmId());
			vm.setVmStatus("RESUMING");

			cloudVmDao.merge(vm);

			JSONObject json = new JSONObject();
			json.put("vmId", vm.getVmId());
			json.put("dcId", vm.getDcId());
			json.put("prjId", vm.getPrjId());
			json.put("vmStatus", vm.getVmStatus());
			json.put("perStatus", "SUSPENDED");
			json.put("count", "0");
			json.put("isExsit", "0");

			final JSONObject data = json;
			TransactionHookUtil.registAfterCommitHook(new Hook() {
				@Override
				public void execute() {
					jedisUtil.addUnique(RedisKey.vmKey, data.toJSONString());
				}
			});
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			throw new AppException("error.openstack.message");
		}
	}
	
	/**
	 * 打开云主机控制台 <br>
	 * ------------------
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public String consoleVm(CloudVm cloudVm) throws AppException {
		String url = "";
		try {
			url = openstackVmService.consoleVm(cloudVm);
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			throw new AppException("error.openstack.message");
		}
		return url;
	}

	/**
	 * 获取云主机日志<br>
	 * ------------------
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public String getVmLogs(CloudVm cloudVm) throws AppException {
		String logs = null;
		try {
			logs = openstackVmService.getVmLogs(cloudVm);
			if (!StringUtils.isEmpty(logs)) {
				logs = logs.replaceAll("\n", "<br>");
			}
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			throw new AppException("error.openstack.message");
		}

		return logs;

	}
    
    /**
     * 查询项目下的已关联云主机的安全组信息 ------------------
     * 
     * @author zhouhaitao
     * @param vmId
     * 
     * @throws AppException
     */
    public List<BaseCloudSecurityGroup> getSecurityGroupByVm(String vmId) {
        List<BaseCloudSecurityGroup> list = new ArrayList<BaseCloudSecurityGroup>();
        StringBuffer sql = new StringBuffer();
        sql.append(" select vsg.sg_id,sg.sg_name from  ");
        sql.append(" cloud_vmsecuritygroup vsg ");
        sql.append(" left join cloud_securitygroup sg ");
        sql.append(" on vsg.sg_id = sg.sg_id ");
        sql.append(" where vsg.vm_id = ? ");

        javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), new Object[] { vmId });
        @SuppressWarnings("rawtypes")
        List listResult = query.getResultList();
        for (int i = 0; i < listResult.size(); i++) {
            Object[] obj = (Object[]) listResult.get(i);
            BaseCloudSecurityGroup cloudSg = new BaseCloudSecurityGroup();
            cloudSg.setSgId(String.valueOf(obj[0]));
            cloudSg.setSgName(String.valueOf(obj[1]));
            if("default".equals(cloudSg.getSgName())){
                cloudSg.setSgName("默认安全组");
            }
            
            list.add(cloudSg);
        }
        return list;
    }
	
	/**
     * <p>校验云主机在同数据中心下是否重名 </p>
     * --------------------------
     * 
     * <p>1.查询cloud_vm表中是否存在主机名称</p>
     * 
     * <p>2.查询cloudorder_vm表中是否存在主机名称</p>
     * 
     * <p>3.查询cloud_vm处于回收站是否存在主机名称</p>
     * 
     * 若 满足 上述 1,2,3都不存在则返回 true;否则 返回 false;
     * 
     * @author zhouhaitao
     * @param map
     *            请求参数
     * @return
     */
    public boolean checkVmExistByName(CloudVm cloudVm) {
        boolean flag = false;
        String[] nameList = null;

        int num = cloudVm.getNumber();
        if (num != 0) {
            nameList = new String[num];
        }
        if (num == 1) {
            nameList[0] = cloudVm.getVmName();
        } else if (num > 1) {
            for (int i = 1; i <= num; i++) {
                nameList[i - 1] = cloudVm.getVmName() + "_" + i;
            }
        }

        StringBuffer hql = new StringBuffer();
        hql.append(" SELECT count(*)  ");
        hql.append(" from  ");
        hql.append(" BaseCloudVm v where 1=1 ");
        hql.append(" and v.prjId = :prjId ");
        hql.append(" and v.isDeleted in ('0','2') ");
        hql.append(" and v.isVisable = '1' ");
        hql.append(" and binary(v.vmName) in (:names)");
        if (!StringUtils.isEmpty(cloudVm.getVmId())) {
            hql.append(" and v.vmId <> :vmId");
        }
        Query query = cloudVmDao.createQuery(hql.toString());
        query.setParameter("prjId", cloudVm.getPrjId());
        query.setParameterList("names", nameList);
        if (!StringUtils.isEmpty(cloudVm.getVmId())) {
            query.setParameter("vmId", cloudVm.getVmId());
        }
        int vmCount = Integer.parseInt(String.valueOf(query.uniqueResult()));
        flag = vmCount == 0;
        if(flag){
            boolean isContains = false;
            int suffixNum = 0;
            String perfixName = "";
            String regex = "";
            String vmName = cloudVm.getVmName();
            int _index = vmName.lastIndexOf('_');
            if(_index != -1){
                perfixName = vmName.substring(0, _index);
                String suffixName = vmName.substring(_index+1);
                Pattern pattern = Pattern.compile("[1-9]|1[0-9]|20"); 
                Matcher isNum = pattern.matcher(suffixName);
                if(isNum.matches()){
                    isContains = true;
                    suffixNum = Integer.parseInt(suffixName);
                }
            }
            if(cloudVm.getNumber()>1){
                regex = "^"+vmName + "_";
                if(cloudVm.getNumber() > 1 && cloudVm.getNumber()<=9){
                    regex = regex + "([1-"+cloudVm.getNumber()+"])$";
                }
                else if(cloudVm.getNumber()== 10){
                    regex = regex + "([1-9]|10)$";
                }
                else if (cloudVm.getNumber()>10 && cloudVm.getNumber()<=19){
                    regex = regex + "([1-9]|1[0-"+(cloudVm.getNumber()-10)+"])$";
                }
                else{
                    regex = regex + "([1-9]|1[0-9]|20)$";
                }
            }
            StringBuffer orderVmHql = new StringBuffer();
            orderVmHql.append(" SELECT                                                 ");
            orderVmHql.append("     count(cov.ordervm_id)                              ");
            orderVmHql.append(" FROM                                                   ");
            orderVmHql.append("     cloudorder_vm cov                                  ");
            orderVmHql.append(" LEFT JOIN order_info oi ON oi.order_no = cov.order_no  ");
            orderVmHql.append(" WHERE                                                  ");
            orderVmHql.append("     1=1                                                ");
            orderVmHql.append(" AND cov.order_type = '0'                               ");
            orderVmHql.append(" AND oi.order_state in ('1','2')                        ");
            orderVmHql.append(" AND cov.prj_id = ?                                     ");
            if(cloudVm.getNumber() == 1){
                orderVmHql.append("AND ( ( binary(cov.vm_name) = '"+vmName+"' and count = 1) ");
                if(isContains){
                    orderVmHql.append(" or ( binary(cov.vm_name) = '"+perfixName+"' and count >= "+suffixNum+" ) )");
                }
                else{
                    orderVmHql.append(")");
                }
            }
            else if(cloudVm.getNumber() > 1){
                orderVmHql.append("AND ( ( binary(cov.vm_name) = '"+vmName+"' and count > 1) ");
                orderVmHql.append(" or ( binary(cov.vm_name) REGEXP '"+regex+"' and count = 1)   ");
                orderVmHql.append(" ) ");
            }
            javax.persistence.Query orderVmQuery = cloudVmDao.createSQLNativeQuery(orderVmHql.toString(),new Object[]{cloudVm.getPrjId()});
            Object obj = orderVmQuery.getSingleResult();
            int objInt = Integer.parseInt(String.valueOf(obj));
            if(objInt >0){
                flag = false;
            }
        }
        
        return flag;
    }
	
	/**
     * 云主机修改子网
     * 
     * @author zhouhaitao
     * @param cloudVm
     *              主机信息
     */
    public void modifySubnet(CloudVm cloudVm){
        BaseCloudVm baseCloudVm = cloudVmDao.findOne(cloudVm.getVmId());
        List<String> sgList = querySecurityGroupByVm(cloudVm.getVmId());
        String [] sgIds = sgList.toArray(new String[]{});
        if(!StringUtils.isEmpty(baseCloudVm.getSubnetId()) 
                && !StringUtils.isEmpty(cloudVm.getSubnetId())
                &&!baseCloudVm.getSubnetId().equals(cloudVm.getSubnetId())){
            openstackVmService.unbindPort(baseCloudVm.getDcId(), baseCloudVm.getPrjId(), baseCloudVm.getVmId(), baseCloudVm.getPortId());
            InterfaceAttachment interAtt = openstackVmService.bindPort(baseCloudVm.getDcId(), baseCloudVm.getPrjId(), baseCloudVm.getVmId(), baseCloudVm.getNetId(), 
                    cloudVm.getSubnetId(), sgIds);
            
            baseCloudVm.setSubnetId(cloudVm.getSubnetId());
            baseCloudVm.setPortId(interAtt.getPort_id());
            baseCloudVm.setVmIp(interAtt.getFixed_ips()[0].getIp_address());
            
        }
        if(!StringUtils.isEmpty(baseCloudVm.getSubnetId()) 
                && StringUtils.isEmpty(cloudVm.getSubnetId())){
            openstackVmService.unbindPort(baseCloudVm.getDcId(), baseCloudVm.getPrjId(), baseCloudVm.getVmId(), baseCloudVm.getPortId());
            
            baseCloudVm.setSubnetId(null);
            baseCloudVm.setPortId(null);
            baseCloudVm.setVmIp(null);
        }
        if(StringUtils.isEmpty(baseCloudVm.getSubnetId()) 
                && !StringUtils.isEmpty(cloudVm.getSubnetId())){
            
            InterfaceAttachment interAtt = openstackVmService.bindPort(baseCloudVm.getDcId(), baseCloudVm.getPrjId(), baseCloudVm.getVmId(), baseCloudVm.getNetId(), 
                    cloudVm.getSubnetId(), sgIds);
            
            baseCloudVm.setSubnetId(cloudVm.getSubnetId());
            baseCloudVm.setPortId(interAtt.getPort_id());
            baseCloudVm.setVmIp(interAtt.getFixed_ips()[0].getIp_address());
        }
        
        
        if(!StringUtils.isEmpty(baseCloudVm.getSelfSubnetId()) 
                && !StringUtils.isEmpty(cloudVm.getSelfSubnetId())
                &&!baseCloudVm.getSelfSubnetId().equals(cloudVm.getSelfSubnetId())){
            openstackVmService.unbindPort(baseCloudVm.getDcId(), baseCloudVm.getPrjId(), baseCloudVm.getVmId(), baseCloudVm.getSelfPortId());
            InterfaceAttachment interAtt = openstackVmService.bindPort(baseCloudVm.getDcId(), baseCloudVm.getPrjId(), baseCloudVm.getVmId(), baseCloudVm.getNetId(), 
                            cloudVm.getSelfSubnetId(), sgIds);
            
            baseCloudVm.setSelfSubnetId(cloudVm.getSelfSubnetId());
            baseCloudVm.setSelfPortId(interAtt.getPort_id());
            baseCloudVm.setSelfIp(interAtt.getFixed_ips()[0].getIp_address());
        }
        if(!StringUtils.isEmpty(baseCloudVm.getSelfSubnetId()) 
                && StringUtils.isEmpty(cloudVm.getSelfSubnetId())){
            openstackVmService.unbindPort(baseCloudVm.getDcId(), baseCloudVm.getPrjId(), baseCloudVm.getVmId(), baseCloudVm.getSelfPortId());
            
            baseCloudVm.setSelfSubnetId(null);
            baseCloudVm.setSelfPortId(null);
            baseCloudVm.setSelfIp(null);
        }
        if(StringUtils.isEmpty(baseCloudVm.getSelfSubnetId()) 
                && !StringUtils.isEmpty(cloudVm.getSelfSubnetId())){
            InterfaceAttachment interAtt = openstackVmService.bindPort(baseCloudVm.getDcId(), baseCloudVm.getPrjId(), baseCloudVm.getVmId(), baseCloudVm.getNetId(), 
                    cloudVm.getSelfSubnetId(), sgIds);
    
            baseCloudVm.setSelfSubnetId(cloudVm.getSelfSubnetId());
            baseCloudVm.setSelfPortId(interAtt.getPort_id());
            baseCloudVm.setSelfIp(interAtt.getFixed_ips()[0].getIp_address());
        }
        
        cloudVmDao.saveOrUpdate(baseCloudVm);
    }
    
    /**
     * 查询云主机关联的安全组列表
     * 
     * @author zhouhaitao
     * @param vmId
     * @return
     */
    private List<String> querySecurityGroupByVm(String vmId){
        List<String> sgList = new ArrayList<String>();
        StringBuffer sql = new StringBuffer();
        
        sql.append("    SELECT                  ");
        sql.append("        sg_id                 ");
        sql.append("    FROM                    ");
        sql.append("        cloud_vmsecuritygroup ");
        sql.append("    WHERE                   ");
        sql.append("        vm_id = ?             ");
        
        javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), new Object[] {vmId});
        @SuppressWarnings("rawtypes")
        List list = query.getResultList();
        if(null != list && list.size() > 0){
            for(int i=0;i<list.size();i++){
                
                sgList.add(String.valueOf(list.get(i)));
                
            }
        }
        return sgList;
        
    }

    /**
     * 校验云主机是否绑定公网IP
     * @author gaoxiang
     * @param instanceId
     * @return
     */
    public boolean checkVmBindingFloatIp (String instanceId) {
        CloudFloatIp floatIp = queryFloatIpByVm(instanceId);
        return floatIp != null;
    }
    
    /**
     * 校验云主机是否负载均衡器成员
     * @author gaoxiang
     * @param instanceId
     * @return
     */
    public boolean checkVmIsLdMember (String instanceId) {
        return checkLdMemberByVm(instanceId);
    }
    
    /**
     * 查询云主机对应的公网IP
     * 
     * @param vmId
     * @return
     */
    private CloudFloatIp queryFloatIpByVm(String vmId){
        CloudFloatIp floatIp = null;
        StringBuffer sql = new StringBuffer();
        sql.append(" select ");
        sql.append("        flo_id, ");
        sql.append("        flo_ip, ");
        sql.append("        dc_id,  ");
        sql.append("        prj_id  ");
        sql.append(" from cloud_floatip ");
        sql.append(" where resource_type = 'vm' ");
        sql.append(" and resource_id= ? ");
        sql.append(" and is_deleted ='0' ");

        javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), new Object[] {vmId});
        @SuppressWarnings("rawtypes")
        List list = query.getResultList();
        if(null != list && list.size() == 1){
            floatIp = new CloudFloatIp();
            int index = 0 ;
            Object [] objs = (Object [])list.get(0);
            
            floatIp.setFloId(String.valueOf(objs[index++]));
            floatIp.setFloIp(String.valueOf(objs[index++]));
            floatIp.setDcId(String.valueOf(objs[index++]));
            floatIp.setPrjId(String.valueOf(objs[index++]));
        }
        return floatIp;
    }
    
    /**
     * 查询云主机对应的负载均衡成员
     * 
     * @param vmId
     * @return
     */
    private boolean checkLdMemberByVm(String vmId){
        StringBuffer sql = new StringBuffer();
        sql.append(" select ");
        sql.append("        member_id   ");
        sql.append(" from cloud_ldmember ");
        sql.append(" where  1=1 ");
        sql.append(" and vm_id= ? ");
        
        javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), new Object[] {vmId});
        @SuppressWarnings("rawtypes")
        List list = query.getResultList();
        return null != list && list.size()>0;
    }
    
    /**
	 * 查询vmId和prjId查询云主机，不存在则返回null<br>
	 * ------------------------------------------
	 * @author zhouhaitao
	 * 
	 * @param vmId
	 * @param prjId
	 * @return
	 */
	public CloudVm queryVmById(String vmId,String prjId){
		CloudVm cloudVm = null ;
		StringBuffer sql = new StringBuffer();
		sql.append("			SELECT                	    	");
		sql.append("				cv.vm_id,              		");
		sql.append("				cv.dc_id,           	   	");
		sql.append("				cv.prj_id,          	   	");
		sql.append("				cv.from_imageid,        	");
		sql.append("				cv.net_id,        	        ");
		sql.append("				cv.subnet_id,        	    ");
		sql.append("				cv.self_subnetid,        	");
		sql.append("				cv.vm_status, 	         	");
		sql.append("				cv.is_deleted,  	       	");
		sql.append("				cv.pay_type,        	   	");
		sql.append("				cv.charge_state,        	");
		sql.append("				cv.vm_name,       	 		");
		sql.append("				cv.os_type,	        		");
		sql.append("				cv.sys_type,        		");
		sql.append("				cv.end_time,        		");
		sql.append("				cv.delete_time,        		");
		sql.append("				cf.flavor_vcpus,        	");
		sql.append("				cf.flavor_ram,        		");
		sql.append("				cf.flavor_disk,        		");
		sql.append("				cv.is_visable        		");
		sql.append("			FROM               		   	    ");
		sql.append("				cloud_vm cv        		   	");
		sql.append("			LEFT JOIN cloud_flavor cf  		");
		sql.append("			ON cf.flavor_id = cv.flavor_id	");
		sql.append("			AND cf.dc_id = cv.dc_id     	");
		sql.append("			WHERE                 	   		");
		sql.append("				cv.vm_id = ?         	  	");
		sql.append("			AND cv.prj_id = ?        		");
		sql.append("			AND cv.is_deleted <> '1' 		");
		
		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), new Object[]{vmId,prjId});
		@SuppressWarnings("rawtypes")
		List result = query.getResultList();
		if(null != result && result.size() == 1){
			cloudVm = new CloudVm();
			int index = 0;
			Object [] objs = (Object []) result.get(0);
			
			cloudVm.setVmId(String.valueOf(objs[index++]));
			cloudVm.setDcId(String.valueOf(objs[index++]));
			cloudVm.setPrjId(String.valueOf(objs[index++]));
			cloudVm.setFromImageId(String.valueOf(objs[index++]));
			cloudVm.setNetId(String.valueOf(objs[index++]));
			cloudVm.setSubnetId(String.valueOf(objs[index++]));
			cloudVm.setSelfSubnetId(String.valueOf(objs[index++]));
			cloudVm.setVmStatus(String.valueOf(objs[index++]));
			cloudVm.setIsDeleted(String.valueOf(objs[index++]));
			cloudVm.setPayType(String.valueOf(objs[index++]));
			cloudVm.setChargeState(String.valueOf(objs[index++]));
			cloudVm.setVmName(String.valueOf(objs[index++]));
			cloudVm.setOsType(objs[index++]==null ? null:String.valueOf(objs[index-1]));
			String systemType = String.valueOf(objs[index++]);
			if (!StringUtil.isEmpty(systemType)) {
				SysDataTree sdt = DictUtil.getDataTreeByNodeId(systemType);
				if(null != sdt){
					cloudVm.setSysType(systemType);
					cloudVm.setSysTypeEn(sdt.getNodeNameEn());
				}
			}
			cloudVm.setEndTime((Date) objs[index++]);
			cloudVm.setDeleteTime((Date) objs[index++]);
			cloudVm.setCpus(Integer.parseInt(String.valueOf(objs[index++]==null?"0":objs[index-1])));
			cloudVm.setRams(Integer.parseInt(String.valueOf(objs[index++]==null?"0":objs[index-1])));
			cloudVm.setDisks(Integer.parseInt(String.valueOf(objs[index++]==null?"0":objs[index-1])));
			if(null != cloudVm.getEndTime()){
				cloudVm.setCycleCount(DateUtil.getUgradeRemainDays(new Date(), cloudVm.getEndTime()));
			}
			cloudVm.setIsVisable(String.valueOf(objs[index]));
		}
		return cloudVm;
	}
	
	/**
	 * 查询当前主机创建中的自定义镜像
	 * 
	 * @return
	 */
	public boolean checkSavingSnapshot(CloudVm cloudVm) {
		boolean flag = false;
		StringBuffer sql = new StringBuffer();
		sql.append(" select count(1) from cloud_image    ");
		sql.append(" where image_ispublic ='2'  ");
		sql.append(" and from_vmid =?  ");
		sql.append(" and (image_status = 'SAVING' or image_status = 'QUEUED')  ");

		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(),
				new Object[] { cloudVm.getVmId() });
		@SuppressWarnings("rawtypes")
		List listResult = query.getResultList();
		if (null != listResult && listResult.size() == 1) {
			BigInteger bi = (BigInteger) listResult.get(0);
			int count = bi.intValue();
			if (count > 0)
				flag = true;
		}
		return flag;
	}
	
	/**
	 * 查询镜像的信息
	 * 
	 * @author zhouhaitao
	 * @param imageId
	 * @return
	 */
	public CloudImage getImageById(String imageId){
		CloudImage image = null;
		StringBuffer sql = new StringBuffer();
		
		sql.append("	SELECT                          ");
		sql.append("		ci.dc_id,                   ");
		sql.append("		ci.prj_id,                  ");
		sql.append("		ci.image_id,                ");
		sql.append("		ci.image_name,              ");
		sql.append("		ci.image_status,            ");
		sql.append("		ci.min_cpu,                 ");
		sql.append("		ci.min_ram,                 ");
		sql.append("		ci.min_disk,                ");
		sql.append("		ci.max_cpu,                 ");
		sql.append("		ci.max_ram,                 ");
		sql.append("		ci.sysdisk_size,            ");
		sql.append("		ci.os_type,                 ");
		sql.append("		ci.sys_type,                ");
		sql.append("		ci.image_ispublic,          ");
		sql.append("		ci.source_id,               ");
		sql.append("		ci.is_use,	                ");
		sql.append("		img.max_cpu as sourceMaxCpu,");
		sql.append("		img.max_ram as sourceMaxRam ");
		sql.append("	FROM                            ");
		sql.append("		cloud_image ci              ");
		sql.append("	LEFT JOIN cloud_image img       ");
		sql.append("	ON ci.source_id = img.image_id  ");
		sql.append("	AND ci.image_ispublic ='2'      ");
		sql.append("	WHERE                           ");
		sql.append("		ci.image_id =  ?            ");
		
		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), new Object[]{imageId});
		@SuppressWarnings("rawtypes")
		List listResult = query.getResultList();
		if(null != listResult && listResult.size() == 1) {
			image = new CloudImage();
			Object[] obj = (Object[]) listResult.get(0);
			int ind = 0;
			image.setDcId(String.valueOf(obj[ind++]));
			image.setPrjId(String.valueOf(obj[ind++]));
			image.setImageId(String.valueOf(obj[ind++]));
			image.setImageName(String.valueOf(obj[ind++]));
			image.setImageStatus(String.valueOf(obj[ind++]));
			image.setMinCpu(Long.parseLong(obj[ind++] == null ? "0" : String.valueOf(obj[ind-1])));
			image.setMinRam(Long.parseLong(obj[ind++] == null ? "0" : String.valueOf(obj[ind-1])));
			image.setMinDisk(Long.parseLong(obj[ind++] == null ? "0" : String.valueOf(obj[ind-1])));
			image.setMaxCpu(Integer.parseInt(obj[ind++] == null ? "0" : String.valueOf(obj[ind-1])));
			image.setMaxRam(Integer.parseInt(obj[ind++] == null ? "0" : String.valueOf(obj[ind-1])));
			image.setSysdiskSize(Long.parseLong(obj[ind++] == null ? "0" : String.valueOf(obj[ind-1])));
			image.setOsType(String.valueOf(obj[ind++]));
			image.setSysType(String.valueOf(obj[ind++]));
			String imageType = String.valueOf(obj[ind++]);
			image.setImageIspublic(Character.valueOf(imageType.charAt(0)));
			image.setSourceId(obj[ind++] == null ? "" : String.valueOf(obj[ind-1]));
			String isUsed = obj[ind++] == null ? "" : String.valueOf(obj[ind-1]);
			image.setIsUse("".equals(isUsed)?null:isUsed.charAt(0));
			image.setSourceMaxCpu(Integer.parseInt(obj[ind++] == null ? "0" : String.valueOf(obj[ind-1])));
			image.setSourceMaxRam(Integer.parseInt(obj[ind++] == null ? "0" : String.valueOf(obj[ind-1])));
		}
		
		return image;
	}
	
	/**
	 * 云主机计费总价 <br>
	 *  -----------------
	 * 
	 * @author zhouhaitao
	 * 
	 * @param orderVm
	 *            订单信息
	 * @return 配置总价
	 */
	public BigDecimal calcVmPrice(CloudOrderVm orderVm) {
		BigDecimal totalPrice = null;
		if(OrderType.NEW.equals(orderVm.getOrderType())){
			ParamBean paramBean = new ParamBean();
			
			paramBean.setDcId(orderVm.getDcId());
			paramBean.setPayType(orderVm.getPayType());
			paramBean.setCpuSize(orderVm.getCpu());
			paramBean.setRamCapacity(orderVm.getRam() / 1024);
			String imageId = queryChargeImageId(orderVm.getImageId());
			paramBean.setImageId(imageId);
			
			//增加分类计价
			if("1".equals(orderVm.getSysDiskType())){
				paramBean.setSysDiskOrdinary(orderVm.getDisk());
			}else if("2".equals(orderVm.getSysDiskType())){
				paramBean.setSysDiskBetter(orderVm.getDisk());
			}else if("3".equals(orderVm.getSysDiskType())){
				paramBean.setSysDiskBest(orderVm.getDisk());
			}else{
				paramBean.setSysDiskCapacity(orderVm.getDisk());
			}
			
			if("1".equals(orderVm.getDataDiskType())){
				paramBean.setDataDiskOrdinary(orderVm.getDataDisk());
			}else if("2".equals(orderVm.getDataDiskType())){
				paramBean.setDataDiskBetter(orderVm.getDataDisk());
			}else if("3".equals(orderVm.getDataDiskType())){
				paramBean.setDataDiskBest(orderVm.getDataDisk());
			}else{
				paramBean.setDataDiskCapacity(orderVm.getDataDisk());
			}
			
			
			if("1".equals(orderVm.getBuyFloatIp())){
				paramBean.setIpCount(1);
			}
			paramBean.setCycleCount(orderVm.getBuyCycle());
			paramBean.setNumber(orderVm.getCount());
			
			totalPrice = billingFactorService.getPriceByFactor(paramBean);
		}
		if(OrderType.UPGRADE.equals(orderVm.getOrderType())){
			UpgradeBean upgradeBean = new UpgradeBean();
			upgradeBean.setDcId(orderVm.getDcId());
			if((orderVm.getCpu() - orderVm.getVmCpu())>0){
				upgradeBean.setCpuSize(orderVm.getCpu() - orderVm.getVmCpu());
			}
			if((orderVm.getRam() - orderVm.getVmRam() ) / 1024>0){
				upgradeBean.setRamCapacity((orderVm.getRam() - orderVm.getVmRam() ) / 1024);
			}
			upgradeBean.setCycleCount(orderVm.getCycleCount());
			
			totalPrice = billingFactorService.updateConfigPrice(upgradeBean);
		}
		
		return totalPrice.setScale(2, RoundingMode.FLOOR);
	}

	/**
	 * 根据云主机订单组装 云主机配置<br>
	 * ---------------------------------
	 * @author zhouhaitao
	 * @param order
	 * @return
	 */
	public String vmConfig(CloudOrderVm order) {
		StringBuffer buffer = new StringBuffer();
		if(OrderType.NEW.equals(order.getOrderType())){
			buffer.append("数据中心：").append(order.getDcName()).append("<br>");
			buffer.append("私有网络：").append(order.getNetName()).append("<br>");
			buffer.append("受管子网：").append(!StringUtils.isEmpty(order.getSubnetId())
					? (order.getSubnetName() + "(" + order.getCidr() + ")") : "未分配").append("<br>");
			buffer.append("自管子网：").append(!StringUtils.isEmpty(order.getSelfSubnetId())
					? (order.getSelfSubnetName() + "(" + order.getSelfCidr() + ")") : "未分配").append("<br>");
			buffer.append("公网 IP：").append("1".endsWith(order.getBuyFloatIp()) ? "已购买" : "未购买").append("<br>");
			buffer.append("主机规格：").append(order.getCpu() + "核/" + order.getRam()/1024 + "GB")
			.append("<br>");
			buffer.append("系统盘：").append(order.getSysTypeAs() + order.getDisk() + "GB")
			.append("<br>");
			buffer.append("数据盘：").append(order.getDataTypeAs() + order.getDataDisk() + "GB")
			.append("<br>");
			buffer.append("镜像：").append(order.getImageName());
		}
		else if(OrderType.UPGRADE.equals(order.getOrderType())){
			buffer.append("数据中心：").append(order.getDcName()).append("<br>");
			buffer.append("云主机ID：").append(order.getVmId()).append("<br>");
			buffer.append("云主机名称：").append(order.getVmName()).append("<br>");
			buffer.append("系统：").append(DictUtil.getDataTreeByNodeId(order.getSysType()).getNodeName()).append("<br>");
			buffer.append("当前CPU和内存：").append(order.getVmCpu() + "核/" + order.getVmRam()/1024+"GB").append("<br>");
			buffer.append("调整后CPU和内存：").append(order.getCpu() + "核/" + order.getRam()/1024+"GB");
		}

		return buffer.toString();
	}
	
	/**
	 * 查询创建同一订单 云主机创建出的状态并处理
	 * 
	 * @author zhouhaitao
	 * @param order
	 *            订单信息
	 * @param floatIpList
	 *            创建成功的公网IP列表
	 * @param vmList
	 *            创建成功返回的云主机列表
	 * @param flavorId
	 *            云主机创建的Flavor配置的ID
	 * @return
	 * @throws Exception
	 */
	public List<BaseCloudVm> saveVmAndVolume(CloudOrderVm order, List<CloudFloatIp> floatIpList, List<Vm> vmList,
			String flavorId, List<BaseCloudVolume> volumeList) throws Exception {
		List<BaseCloudVm> result = new ArrayList<BaseCloudVm>();
		if (null != vmList && vmList.size() > 0) {
			String url = "?flavor="+flavorId;
			List<Vm> allVmListByProject = openstackVmService.list(order.getDcId(), order.getPrjId(),url);
			for (int i = 0; i<vmList.size();i++) {
				Vm sucVm = vmList.get(i);
				for (Vm vm : allVmListByProject) {
					if (sucVm.getId().equals(vm.getId())) {
						BaseCloudVm tempVm = new BaseCloudVm();
						tempVm.setVmId(vm.getId());
						tempVm.setVmName(vm.getName());
						tempVm.setVmStatus(vm.getVm_state().toUpperCase());
						tempVm.setHostId(vm.getHostId());
						tempVm.setHostName(vm.getHypervisor_hostname());
						tempVm.setCreateName(order.getCreateUser());
						tempVm.setFlavorId(flavorId);
						tempVm.setCreateTime(new Date());
						tempVm.setDcId(order.getDcId());
						tempVm.setPrjId(order.getPrjId());
						tempVm.setNetId(order.getNetId());
						tempVm.setSubnetId(order.getSubnetId());
						tempVm.setSelfSubnetId(order.getSelfSubnetId());
						tempVm.setOsType(order.getOsType());
						tempVm.setSysType(order.getSysType());
						tempVm.setFromImageId(order.getImageId());
						tempVm.setIsDeleted("0");
						tempVm.setIsVisable("0");
						tempVm.setChargeState(CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE);
						tempVm.setEndTime(order.getEndTime());
						tempVm.setPayType(order.getPayType());
						tempVm.setVmIp(sucVm.getIp());
						tempVm.setPortId(sucVm.getPortId());
						tempVm.setSelfIp(sucVm.getSelfIp());
						tempVm.setSelfPortId(sucVm.getSelfPortId());
						tempVm.setVmFrom(order.getImageType());

						cloudVmDao.saveOrUpdate(tempVm);

						BaseCloudVmSgroup vsg = new BaseCloudVmSgroup();
						vsg.setSgId(order.getSgId());
						vsg.setVmId(tempVm.getVmId());

						vmSgService.saveOrUpdate(vsg);

						if(ConstantClazz.VM_LOGIN_TYPE_SSH.equals(order.getLoginType())){
							BaseSecretkeyVm bcsk = new BaseSecretkeyVm();
							bcsk.setVmId(tempVm.getVmId());
							bcsk.setSecretkeyId(order.getSecretKey());
							secretkeyVmService.saveOrUpdate(bcsk);
						}
						
						BaseCloudBatchResource resource = new BaseCloudBatchResource();
						resource.setOrderNo(order.getOrderNo());
						resource.setResourceId(tempVm.getVmId());
						resource.setResourceType(CloudBatchResource.RESOURCE_VM);

						cloudBatchResourceService.save(resource);

						if (null != vm.getVolumes_attached() && vm.getVolumes_attached().size() > 0) {
							for (String volId : vm.getVolumes_attached()) {
								BaseCloudVolume vol = new BaseCloudVolume();
								vol.setVolId(volId);
								vol.setVolName(volId);
								vol.setCreateName(order.getCreateUser());
								vol.setCreateTime(new Date());
								vol.setPrjId(order.getPrjId());
								vol.setDcId(order.getDcId());
								vol.setVolBootable("1");
								vol.setOsType(order.getOsType());
								vol.setSysType(order.getSysType());
								vol.setDiskFrom(order.getImageType());
								vol.setVmId(vm.getId());
								vol.setFromImageId(order.getImageId());
								vol.setVolSize(order.getDisk());
								vol.setVolStatus("CREATING");
								vol.setIsDeleted("0");
								vol.setPayType(order.getPayType());
								vol.setChargeState("0");
								vol.setIsVisable("0");
								vol.setVolTypeId(order.getSysTypeId());

								volumeList.add(vol);
								volumeService.insertVolumeDB(vol);
							}
						}

						result.add(tempVm);
					}
				}
			}
		}

		return result;
	}
	
	/**
	 * 根据订单编号 查询对应的已经创建的云主机列表 -----------------------------------
	 * 
	 * @author zhouhaitao
	 * @param orderNo
	 *            订单编号
	 * @return
	 * 
	 */
	public List<CloudVm> queryVmListByOrder(String orderNo) {
		List<CloudVm> vmList = new ArrayList<CloudVm>();
		StringBuffer sql = new StringBuffer();

		sql.append("		SELECT                                                ");
		sql.append("			vm.dc_id,                                         ");
		sql.append("			vm.prj_id,                                        ");
		sql.append("			vm.vm_id,                                         ");
		sql.append("			vm.vm_status                                       ");
		sql.append("		FROM                                                  ");
		sql.append("			cloud_batchresource cbr                           ");
		sql.append("		LEFT JOIN cloud_vm vm ON vm.vm_id = cbr.resource_id   ");
		sql.append("		AND resource_type = 'vm'                              ");
		sql.append("		WHERE                                                 ");
		sql.append("			cbr.order_no = ?                                  ");

		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), new Object[] { orderNo });

		@SuppressWarnings("rawtypes")
		List result = query.getResultList();
		if (result != null && result.size() > 0) {
			for (int i = 0; i < result.size(); i++) {
				int index = 0;
				Object[] objs = (Object[]) result.get(i);
				CloudVm cloudVm = new CloudVm();

				cloudVm.setDcId(String.valueOf(objs[index++]));
				cloudVm.setPrjId(String.valueOf(objs[index++]));
				cloudVm.setVmId(String.valueOf(objs[index++]));
				cloudVm.setVmStatus(String.valueOf(objs[index++]));

				vmList.add(cloudVm);
			}
		}

		return vmList;
	}
	
	/**
	 * 
	 * 订单创建失败，删除订单以创建成功的资源过程中发生删除失败的处理
	 * 
	 * @author zhouhaitao
	 * @param order
	 *            订单信息
	 */
	public void deleteFailedHandler(CloudOrderVm order) {
		List<MessageOrderResourceNotice> resources = cloudBatchResourceService.queryResourceByOrder(order.getOrderNo());
		messageCenterService.delecteResourFailMessage(resources, order.getOrderNo());
	}

	/**
	 * 新购云主机计费
	 * 
	 * @author zhouhaitao
	 * @param record
	 * 
	 */
	public void vmPurchaseCharge(final CloudOrderVm orderVm, final BaseCloudVm vm) {
		
		TransactionHookUtil.registAfterCommitHook(new Hook() {
			@Override
			public void execute() {
				ChargeRecord record = new ChargeRecord();
				ParamBean param = new ParamBean();
				
				param.setNumber(1);
				param.setCpuSize(orderVm.getCpu());
				param.setRamCapacity(orderVm.getRam() / 1024);
				String imageId = queryChargeImageId(orderVm.getImageId());
				param.setImageId(imageId);
				
				if(null!=orderVm.getSysDiskType()&&"1".equals(orderVm.getSysDiskType())){
					param.setSysDiskOrdinary(orderVm.getDisk());
				}else if(null!=orderVm.getSysDiskType()&&"2".equals(orderVm.getSysDiskType())){
					param.setSysDiskBetter(orderVm.getDisk());
				}else if(null!=orderVm.getSysDiskType()&&"3".equals(orderVm.getSysDiskType())){
					param.setSysDiskBest(orderVm.getDisk());
				}else{
					param.setSysDiskCapacity(orderVm.getDisk());
				}
				
				
				record.setParam(param);
				record.setDatecenterId(orderVm.getDcId());
				record.setOrderNumber(orderVm.getOrderNo());
				record.setCusId(orderVm.getCusId());
				record.setResourceId(vm.getVmId());
				record.setResourceType(ResourceType.VM);
				record.setChargeFrom(orderVm.getOrderCompleteDate());
				
				rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_PURCHASE, JSONObject.toJSONString(record));
			}
		});
	}
	
	/**
	 * 查询主机是否有正在处理的订单
	 * 
	 * @author zhouhaitao
	 * @param vmId
	 * @return
	 */
	public boolean checkVmOrderExsit(String vmId,boolean isResize,boolean isRenew){
		StringBuffer sql = new StringBuffer();
		sql.append("		SELECT                                               ");
		sql.append("			vm.ordervm_id                                    ");
		sql.append("		FROM                                                 ");
		sql.append("			cloudorder_vm vm                                 ");
		sql.append("		LEFT JOIN order_info oi                              ");
		sql.append("		ON vm.order_no = oi.order_no                         ");
		sql.append("		WHERE                                                ");
		sql.append("			vm.vm_id = ?                                     ");
		sql.append("		AND (                                                ");
		sql.append("			oi.order_state = '1'                             ");
		sql.append("			OR oi.order_state = '2'                          ");
		sql.append("		)                                                    ");
		sql.append("		AND (                                                ");
		sql.append("			1 <> 1 					                         ");
		if(isResize){
			sql.append("			OR vm.order_type = '2'                       ");
		}
		if(isRenew){
			sql.append("			OR vm.order_type = '1'                       ");
		}
		sql.append("		)                                                    ");
		
		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), new Object[]{vmId});
		@SuppressWarnings("rawtypes")
		List resultList = query.getResultList();
		return resultList != null && resultList.size()>0 ;
	}
	
	/**
	 * 查询镜像的原始的镜像ID（计费的镜像）
	 * @param imageId 镜像ID
	 * @return
	 */
	public String queryChargeImageId(String imageId){
		StringBuffer sql = new StringBuffer();
		
		sql.append("		SELECT                             ");
		sql.append("			CASE                           ");
		sql.append("		WHEN image_ispublic = '2' THEN     ");
		sql.append("			source_id                      ");
		sql.append("		ELSE                               ");
		sql.append("			image_id                       ");
		sql.append("		END AS image_id                    ");
		sql.append("		FROM                               ");
		sql.append("			cloud_image                    ");
		sql.append("		WHERE                              ");
		sql.append("			image_id = ?                   ");
		
		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), new Object[]{imageId});
		@SuppressWarnings("rawtypes")
		List resultList = query.getResultList();
		if(null != resultList && resultList.size()==1){
			return String.valueOf(resultList.get(0));
		}
		return null;
		
	}
	
}
