package com.eayun.monitor.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.constant.RedisKey;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.database.configgroup.model.datastore.DatastoreVersion;
import com.eayun.monitor.bean.MonitorAlarmUtil;
import com.eayun.monitor.bean.VmIndicator;
import com.eayun.monitor.dao.MonitorAlarmItemDao;
import com.eayun.monitor.service.InstanceAlarmMonitorService;

@Transactional
@Service
public class InstanceAlarmMonitorServiceImpl implements InstanceAlarmMonitorService {

	private static final Logger log = LoggerFactory.getLogger(InstanceAlarmMonitorServiceImpl.class);
    
    @Autowired
    private MonitorAlarmItemDao monitorDao;
    
    @Autowired
    private JedisUtil           jedisUtil;
	
    /**
     * 获取云数据库资源监控列表
     * @Author: duanbinbin
     * @param page
     * @param queryMap
     * @param prjId
     * @param instanceName
     * @param versionId
     * @return
     *<li>Date: 2017年3月2日</li>
     */
	@Override
	public Page getInstanceMonitorPage(Page page, QueryMap queryMap,
			String prjId, String instanceName, String versionId) {
		log.info("查询数据库实例监控列表");
        List<Object> list = new ArrayList<Object>();
        StringBuffer sql = 
        		new StringBuffer("SELECT rds.rds_id,rds.rds_name,rds.is_master,rds.vm_id,");
        sql.append(" cdv.name AS verName ,cd.`name` AS dataName FROM cloud_rdsinstance rds ")
        .append(" LEFT JOIN cloud_datastoreversion cdv ON rds.version_id = cdv.id ")
        .append(" LEFT JOIN cloud_datastore cd ON cdv.datastore_id = cd.id ")
        .append(" WHERE rds.is_deleted = '0' and rds.is_visible = '1' and rds.prj_id = ? ");
        list.add(prjId);
        if (null != instanceName && !instanceName.trim().equals("")) {
        	instanceName = instanceName.replaceAll("\\_", "\\\\_");
            sql.append(" and binary rds.rds_name like ? ");
            list.add("%" + instanceName + "%");
        }
        if(!StringUtil.isEmpty(versionId)){
        	sql.append(" and rds.version_id = ? ");
            list.add(versionId);
        }
        sql.append(" order by rds.create_time desc");
        page = monitorDao.pagedNativeQuery(sql.toString(), queryMap, list.toArray());
        List newlist = (List) page.getResult();
        int a = newlist.size();
        for (int i = 0; i < newlist.size(); i++) {
            Object[] objs = (Object[]) newlist.get(i);
            VmIndicator vmIndicator = new VmIndicator();
            String instanceId = String.valueOf(objs[0]);
            vmIndicator.setInstanceId(instanceId);
            vmIndicator.setInstanceName(String.valueOf(objs[1]));
            vmIndicator.setIsMaster(String.valueOf(objs[2]));
            String vmId = String.valueOf(objs[3]);
            vmIndicator.setVmId(vmId);
            String verName = String.valueOf(objs[4]);
            String dataName = MonitorAlarmUtil.transferDatastoreName(String.valueOf(objs[5]));
            vmIndicator.setDataVersionName(dataName+verName);
            try {
                Double cpu = jedisUtil.getDouble(RedisKey.MONITOR_CPU+vmId);
                Double ram = jedisUtil.getDouble(RedisKey.MONITOR_MEMORY+vmId);
                Double volused = jedisUtil.getDouble(RedisKey.MONITOR_VOLUME_USED+vmId);
                if(cpu > 100d){cpu = 100d;}
                if(ram > 100d){ram = 100d;}
                if(volused > 100d){volused = 100d;}
                Double read = jedisUtil.getDouble(RedisKey.MONITOR_DISK_READ+vmId);
                Double write = jedisUtil.getDouble(RedisKey.MONITOR_DISK_WRITE+vmId);
                Double netin = jedisUtil.getDouble(RedisKey.MONITOR_NETWORK_INCOMING+vmId);
                Double netout = jedisUtil.getDouble(RedisKey.MONITOR_NETWORK_OUTGOING+vmId);
                
                Double lastcpu = jedisUtil.getDouble(RedisKey.MONITOR_CPU_LAST+vmId);
                Double lastram = jedisUtil.getDouble(RedisKey.MONITOR_MEMORY_LAST+vmId);
                Double lastvolused = jedisUtil.getDouble(RedisKey.MONITOR_VOLUME_USED_LAST+vmId);
                if(lastcpu > 100d){lastcpu = 100d;}
                if(lastram > 100d){lastram = 100d;}
                if(lastvolused > 100d){lastram = 100d;}
                Double lastread = jedisUtil.getDouble(RedisKey.MONITOR_DISK_READ_LAST+vmId);
                Double lastwrite = jedisUtil.getDouble(RedisKey.MONITOR_DISK_WRITE_LAST+vmId);
                Double lastnetin = jedisUtil.getDouble(RedisKey.MONITOR_NETWORK_INCOMING_LAST+vmId);
                Double lastnetout = jedisUtil.getDouble(RedisKey.MONITOR_NETWORK_OUTGOING_LAST+vmId);
                
                cpu = (Double)(Math.round(cpu*10)/10.0);
                ram = (Double)(Math.round(ram*10)/10.0);
                volused = (Double)(Math.round(volused*10)/10.0);
                
                lastcpu = (Double)(Math.round(lastcpu*10)/10.0);
                lastram = (Double)(Math.round(lastram*10)/10.0);
                lastvolused = (Double)(Math.round(lastvolused*10)/10.0);
                
                read = (Double)(Math.round(read*10000)/10000.0);
                write = (Double)(Math.round(write*10000)/10000.0);
                netin = (Double)(Math.round(netin*10000)/10000.0);
                netout = (Double)(Math.round(netout*10000)/10000.0);
                
                lastread = (Double)(Math.round(lastread*10000)/10000.0);
                lastwrite = (Double)(Math.round(lastwrite*10000)/10000.0);
                lastnetin = (Double)(Math.round(lastnetin*10000)/10000.0);
                lastnetout = (Double)(Math.round(lastnetout*10000)/10000.0);
                
                int cpuDiff = 0;
                int ramDiff = 0;
                int volusedDiff = 0;
                int netinDiff = 0;
                int netoutDiff = 0;
                int writeDiff = 0;
                int readDiff = 0;
                if(cpu > lastcpu){
                    cpuDiff = 1;
                }else if(cpu < lastcpu){
                    cpuDiff = -1;
                }
                if(ram > lastram){
                    ramDiff = 1;
                }else if(ram < lastram){
                    ramDiff = -1;
                }
                if(volused > lastvolused){
                	volusedDiff = 1;
                }else if(volused < lastvolused){
                	volusedDiff = -1;
                }
                if(netin > lastnetin){
                    netinDiff = 1;
                }else if(netin < lastnetin){
                    netinDiff = -1;
                }
                if(netout > lastnetout){
                    netoutDiff = 1;
                }else if(netout < lastnetout){
                    netoutDiff = -1;
                }
                if(write > lastwrite){
                    writeDiff = 1;
                }else if(write < lastwrite){
                    writeDiff = -1;
                }
                if(read > lastread){
                    readDiff = 1;
                }else if(read < lastread){
                    readDiff = -1;
                }
                
                vmIndicator.setCpu(cpu);
                vmIndicator.setCpuDiff(cpuDiff);
                vmIndicator.setRam(ram);
                vmIndicator.setRamDiff(ramDiff);
                vmIndicator.setDiskRead(read);
                vmIndicator.setReadDiff(readDiff);
                vmIndicator.setDiskWrite(write);
                vmIndicator.setWriteDiff(writeDiff);
                vmIndicator.setNetIn(netin);
                vmIndicator.setNetinDiff(netinDiff);
                vmIndicator.setNetOut(netout);
                vmIndicator.setNetoutDiff(netoutDiff);
                vmIndicator.setVolumeUsed(volused);
                vmIndicator.setVolumeUsedDiff(volusedDiff);
                
            } catch (Exception e) {
                log.error("获取监控数据失败", e);
                throw new AppException("数据查询异常");
            }
            newlist.set(i, vmIndicator);
        }
        return page;
    }

	/**
	 * 获取云数据库版本列表
	 * @Author: duanbinbin
	 * @return
	 *<li>Date: 2017年3月2日</li>
	 */
	@Override
	public List<DatastoreVersion> getDataVersionList() {
		List<DatastoreVersion> verList = new ArrayList<DatastoreVersion>();
		StringBuffer sql = new StringBuffer("SELECT cdv.id, cdv.`name` AS vName,cd.`name` AS dataName");
		sql.append(" FROM cloud_datastoreversion cdv ")
		.append(" LEFT JOIN cloud_datastore cd ON cdv.datastore_id = cd.id");
		Query query = monitorDao.createSQLNativeQuery(sql.toString());
        List resultList = query.getResultList();
        for(int i=0; i<resultList.size(); i++){
            Object[] objs = (Object[]) resultList.get(i);
            DatastoreVersion version = new DatastoreVersion();
            version.setId(String.valueOf(objs[0]));
            String vName = String.valueOf(objs[1]);
            version.setName(vName);
            String dataName = MonitorAlarmUtil.transferDatastoreName(String.valueOf(objs[2]));
            
            String dataVersionName = dataName+vName;
            version.setDataVersionName(dataVersionName);
            verList.add(version);
        }
		return verList;
	}

	@Override
	public VmIndicator getRdsDetailById(String instanceId) {
		List<Object> list = new ArrayList<Object>();
		StringBuffer sql = 
				new StringBuffer("SELECT rds.rds_id,rds.rds_name,rds.is_master,rds.vm_id,");
        sql.append(" cdv.name AS verName ,cd.`name` AS dataName ")
           .append(" FROM cloud_rdsinstance rds ")
           .append(" LEFT JOIN cloud_datastoreversion cdv ON rds.version_id = cdv.id ")
           .append(" LEFT JOIN cloud_datastore cd ON cdv.datastore_id = cd.id ");
        sql.append(" WHERE rds.is_deleted = '0' and rds.is_visible = '1' and rds.rds_id = ? ");
        list.add(instanceId);
        javax.persistence.Query query = monitorDao.createSQLNativeQuery(sql.toString(), list.toArray());
        VmIndicator instance = new VmIndicator();
        List resultlist = new ArrayList();
        if(null!=query ){
        	resultlist = query.getResultList();
        }
        if(null != resultlist&& resultlist.size() == 1){
            Object[] objs = (Object[]) resultlist.get(0);
            instance.setInstanceId(String.valueOf(objs[0]));
            instance.setInstanceName(String.valueOf(objs[1]));
            instance.setIsMaster(String.valueOf(objs[2]));
            instance.setVmId(String.valueOf(objs[3]));
            String datastoreName = MonitorAlarmUtil.transferDatastoreName(String.valueOf(objs[5]));
            instance.setDataVersionName(datastoreName+String.valueOf(objs[4]));
        }
        return instance;
	}

}
