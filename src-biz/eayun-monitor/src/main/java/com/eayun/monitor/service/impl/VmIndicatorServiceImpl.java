package com.eayun.monitor.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.monitor.bean.VmIndicator;
import com.eayun.monitor.dao.MonitorAlarmItemDao;
import com.eayun.monitor.service.VmIndicatorService;

@Transactional
@Service
public class VmIndicatorServiceImpl implements VmIndicatorService {

    private static final Logger log = LoggerFactory.getLogger(VmIndicatorServiceImpl.class);
    
    @Autowired
    private MonitorAlarmItemDao monitorDao;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Autowired
    private JedisUtil           jedisUtil;
    
    @Override
    public Page getvmList(Page page, QueryMap queryMap, String projectId , String vmName) {
        List<Object> list = new ArrayList<Object>();
        StringBuffer hql = new StringBuffer(
            "select vm.vm_id,vm.vm_name,cnet.net_name,vm.vm_ip,sdt.node_name "
            + " FROM cloud_vm vm LEFT JOIN cloud_network cnet ON vm.net_id = cnet.net_id LEFT JOIN sys_data_tree sdt ON vm.sys_type = sdt.node_id "
            + " WHERE vm.is_deleted = '0' and vm.is_visable = '1' ");
        hql.append(" and vm.prj_id = ?");
        list.add(projectId);
        if (null != vmName && !vmName.trim().equals("")) {
            vmName = vmName.replaceAll("\\_", "\\\\_");
            hql.append(" and binary vm.vm_name like ?");
            list.add("%" + vmName + "%");
        }
        hql.append(" order by vm.create_time desc");
        page = monitorDao.pagedNativeQuery(hql.toString(), queryMap, list.toArray());
        List newlist = (List) page.getResult();
        for (int i = 0; i < newlist.size(); i++) {
            Object[] objs = (Object[]) newlist.get(i);
            VmIndicator vmIndicator = new VmIndicator();
            vmIndicator.setVmId(String.valueOf(objs[0]));
            vmIndicator.setVmName(String.valueOf(objs[1]));
            vmIndicator.setNetName(String.valueOf(objs[2]));
            vmIndicator.setVmIp(String.valueOf(objs[3]));
            vmIndicator.setOsType(String.valueOf(objs[4]));
            try {
                Double cpu = jedisUtil.getDouble(RedisKey.MONITOR_CPU+vmIndicator.getVmId());
                Double ram = jedisUtil.getDouble(RedisKey.MONITOR_MEMORY+vmIndicator.getVmId());
                if(cpu > 100d){cpu = 100d;}
                if(ram > 100d){ram = 100d;}
                Double read = jedisUtil.getDouble(RedisKey.MONITOR_DISK_READ+vmIndicator.getVmId());
                Double write = jedisUtil.getDouble(RedisKey.MONITOR_DISK_WRITE+vmIndicator.getVmId());
                Double netin = jedisUtil.getDouble(RedisKey.MONITOR_NETWORK_INCOMING+vmIndicator.getVmId());
                Double netout = jedisUtil.getDouble(RedisKey.MONITOR_NETWORK_OUTGOING+vmIndicator.getVmId());
                
                Double lastcpu = jedisUtil.getDouble(RedisKey.MONITOR_CPU_LAST+vmIndicator.getVmId());
                Double lastram = jedisUtil.getDouble(RedisKey.MONITOR_MEMORY_LAST+vmIndicator.getVmId());
                if(lastcpu > 100d){lastcpu = 100d;}
                if(lastram > 100d){lastram = 100d;}
                Double lastread = jedisUtil.getDouble(RedisKey.MONITOR_DISK_READ_LAST+vmIndicator.getVmId());
                Double lastwrite = jedisUtil.getDouble(RedisKey.MONITOR_DISK_WRITE_LAST+vmIndicator.getVmId());
                Double lastnetin = jedisUtil.getDouble(RedisKey.MONITOR_NETWORK_INCOMING_LAST+vmIndicator.getVmId());
                Double lastnetout = jedisUtil.getDouble(RedisKey.MONITOR_NETWORK_OUTGOING_LAST+vmIndicator.getVmId());
                
                cpu = (Double)(Math.round(cpu*10)/10.0);
                ram = (Double)(Math.round(ram*10)/10.0);
                
                lastcpu = (Double)(Math.round(lastcpu*10)/10.0);
                lastram = (Double)(Math.round(lastram*10)/10.0);
                
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
                
            } catch (Exception e) {
                log.error("获取监控数据失败", e);
                throw new AppException("数据查询异常");
            }
            newlist.set(i, vmIndicator);
        }
        return page;
    }

    @Override
    public VmIndicator getvmById(String vmId) {
        List<Object> list = new ArrayList<Object>();
        StringBuffer hql = new StringBuffer(
            "select vm.vm_id,vm.vm_name,cnet.net_name,vm.vm_ip,sdt.node_name "
            + " FROM cloud_vm vm LEFT JOIN cloud_network cnet ON vm.net_id = cnet.net_id LEFT JOIN sys_data_tree sdt ON vm.sys_type = sdt.node_id "
            + " WHERE vm.is_deleted = '0' ");
        hql.append(" and vm.vm_id = ?");
        list.add(vmId);
        javax.persistence.Query query = monitorDao.createSQLNativeQuery(hql.toString(), list.toArray());
        VmIndicator vmIndicator = new VmIndicator();
        List vmlist = new ArrayList();
        if(null!=query ){
            vmlist = query.getResultList();
        }
        if(null != vmlist&& vmlist.size() == 1){
            Object[] objs = (Object[]) vmlist.get(0);
            vmIndicator.setVmId(String.valueOf(objs[0]));
            vmIndicator.setVmName(String.valueOf(objs[1]));
            vmIndicator.setNetName(String.valueOf(objs[2]));
            vmIndicator.setVmIp(String.valueOf(objs[3]));
            vmIndicator.setOsType(String.valueOf(objs[4]));
        }
        return vmIndicator;
    }

    /**
     * @Author: duanbinbin
     * @param endTime
     * @param count
     * @param vmId
     * @param type
     * @param cusId
     * @return
     *<li>Date: 2017年3月2日</li>
     */
    @Override
    public List<VmIndicator> getDataById(Date endTime, int count, String vmId,String type, String cusId ,String instanceId) {
        List<Object> list = new ArrayList<Object>();// 取消创建前及当前时间之后补数
        StringBuffer hql = new StringBuffer("");
        if(StringUtil.isEmpty(instanceId)){
        	hql.append("select vm.create_time  FROM cloud_vm vm LEFT JOIN cloud_project pro ON vm.prj_id = pro.prj_id ");
        	hql.append(" WHERE vm.is_deleted = '0'  and vm.vm_id = ? AND pro.customer_id = ?");
        	list.add(vmId);
        }else{
        	hql.append(" SELECT rds.create_time FROM cloud_rdsinstance rds ");
        	hql.append(" LEFT JOIN cloud_project pro ON rds.prj_id = pro.prj_id ");
        	hql.append(" WHERE rds.is_deleted = '0' AND rds.rds_id = ? AND pro.customer_id = ? ");
        	list.add(instanceId);
        }
        list.add(cusId);
        javax.persistence.Query query = monitorDao.createSQLNativeQuery(hql.toString(), list.toArray());
        Date createTime = null;
        if(null!=query && query.getResultList().size() > 0){
            Object obj = (Object) query.getResultList().get(0);
            createTime = (Date)obj;
        }else{
        	return null;
        }
        Date firstDate = DateUtil.dateRemoveSec(endTime);
        Date lastDate = DateUtil.dateRemoveSec(endTime);    //计算获取数据的时间
        Date showDate = DateUtil.dateRemoveSec(endTime);    //图表显示时间
        
        String prefix = "";
        switch(type){
            case "cpu":
                prefix = "cpu_util";
                break;
            case "ram":
                prefix = "memory.usage";
                break;
            case "read":
                prefix = "disk.read.bytes.rate";
                break;
            case "write":
                prefix = "disk.write.bytes.rate";
                break;
            case "incomming":
                prefix = "network.incoming.bytes.rate";
                break;
            case "outgoing":
                prefix = "network.outgoing.bytes.rate";
                break;
            case "volumeused":				//磁盘使用率
                prefix = "volume.used";
                break;
          }
        
        String suffix = "";
        int size = 13;
        /**
         * 每个点的数据包含的设置的最小区间的个数
         * 如：30分钟共11个点，每个点是三个一分钟的平均数，最小区间是1分钟，个数是3
         *  12小时共13个点，每个点是一小时内12个5分钟的平均数，最小区间是5分钟，个数是12
         */
        
        /**
         * 2016-01-12:逻辑失误：N个点，但是只有N-1个最小区间合集，因此还要再向前取一个点数据范围的最小区间合集的
         * 每个数据点取的是上一个最小区间的数值
         */
        Calendar c = Calendar.getInstance();  
        c.setTime(firstDate); 
        int minute = c.get(c.MINUTE);
        int newMin = minute;
        /**
         * isminPrefix:最小区间时间是否向前取等，(即是<=X<还是<X<=)
         */
        boolean isminPrefix = true;
        switch(count){
            case 3:
                suffix = ".detail";
                size = 11;
                isminPrefix = true;
                showDate = DateUtil.addDay(firstDate, new int[]{0,0,0,0,-30});
                lastDate = DateUtil.addDay(showDate, new int[]{0,0,0,0,-3});
                break;
            case 5:
                suffix = ".detail";
                isminPrefix = true;
                showDate = DateUtil.addDay(firstDate, new int[]{0,0,0,0,-60});
                lastDate = DateUtil.addDay(showDate, new int[]{0,0,0,0,-5});
                break;
            case 30:
                suffix = ".3min";
                isminPrefix = false;
                showDate = DateUtil.addDay(firstDate, new int[]{0,0,0,0,-360});
                
                newMin = (minute/3)*3;
                firstDate = DateUtil.addDay(firstDate, new int[]{0,0,0,0,newMin-minute});
                lastDate = DateUtil.addDay(firstDate, new int[]{0,0,0,0,-390});
                break;
            case 60:
                suffix = ".5min";
                isminPrefix = false;
                showDate = DateUtil.addDay(firstDate, new int[]{0,0,0,-12});
                newMin = (minute/5)*5;
                firstDate = DateUtil.addDay(firstDate, new int[]{0,0,0,0,newMin-minute});
                lastDate = DateUtil.addDay(firstDate, new int[]{0,0,0,-13});
                break;
            case 120:
                suffix = ".10min";
                isminPrefix = false;
                showDate = DateUtil.addDay(firstDate, new int[]{0,0,0,-24});
                newMin = (minute/10)*10;
                firstDate = DateUtil.addDay(firstDate, new int[]{0,0,0,0,newMin-minute});
                lastDate = DateUtil.addDay(firstDate, new int[]{0,0,0,-26});
                break;
            case 720:
                suffix = ".1h";
                size = 15;
                isminPrefix = false;
                showDate = DateUtil.addDay(firstDate, new int[]{0,0,0,-168});
                
                firstDate = DateUtil.addDay(firstDate, new int[]{0,0,0,0,-minute});
                lastDate = DateUtil.addDay(firstDate, new int[]{0,0,0,-180});
                break;
          }
        
        List<VmIndicator> vmList = new ArrayList<>();
        
        Sort sort = new Sort(Direction.DESC, "timestamp");
        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("vm_id").is(vmId), Criteria.where("timestamp").gte(lastDate), Criteria.where("timestamp").lte(firstDate));
        List<JSONObject> jsonlist = mongoTemplate.find(new Query(criatira).with(sort), JSONObject.class,prefix+suffix);
        
        Date now = new Date();
        boolean seconds = now.after(firstDate)&&DateUtil.addDay(now, new int[]{0,0,0,0,0,-20}).before(firstDate);
        
        for (int i = 0 ;i < size;i++) {
            VmIndicator vm = new VmIndicator();
            if(i == 0){
                vm.setTimestamp(showDate);      //图表显示时间
            }else{
                Date nDate = vmList.get(i-1).getTimestamp();    //list上一个对象的显示时间
                nDate = DateUtil.addDay(nDate, new int[]{0,0,0,0,count});   //加上一个时间间隔
                vm.setTimestamp(nDate);         //本条对象的显示时间
            }
            int diff = i*count;
            Date diffDate = DateUtil.addDay(lastDate, new int[]{0,0,0,0,diff});
            Date dDate = DateUtil.addDay(lastDate, new int[]{0,0,0,0,diff+count});
            Double data = 0d;
            /**
             * 记录每个点里包含的最小时间单位中，有几个取到了数据，
             * 如30min时，每3min一个点，记录用于获取改点数据的三个一分钟里实际取到的是几个
             */
            int minTnter = 0;
            boolean isdata = false; //是否取到mongDB的数据，mongdb无数据则取左右数据补位，若mongdb数据即为0，则为0即可
            if(!(jsonlist.isEmpty() || jsonlist.size() == 0)){
                for(int j = 0 ;j < jsonlist.size();j++){
                    JSONObject json = jsonlist.get(j);              //每个数据点取的应该是上一个最小区间的数值
                    if(isminPrefix){
                        if((json.getDate("timestamp").after(diffDate) || json.getDate("timestamp").equals(diffDate))
                                && json.getDate("timestamp").before(dDate)){
                            data += json.getDouble("counter_volume");
                            isdata = true;
                            minTnter++;
                        }
                    }else{
                        if(json.getDate("timestamp").after(diffDate)
                                && (json.getDate("timestamp").before(dDate)||json.getDate("timestamp").equals(dDate))){
                            data += json.getDouble("counter_volume");
                            isdata = true;
                            minTnter++;
                        }
                    }
                    
                }
            }
            //计算平均为实际取到点数的平均
            if(minTnter != 0){
                data=data/minTnter;
            }
            vm.setMongodb(isdata);
            
            switch(type){
                case "cpu":
                    if(seconds){            //当前时间位于离截止时间最近的取数时间的20秒内
                        if(i == size-1){
                            if(data == 0.0){//此时最后的一个数据若为0，则将其左边的数据补上
                                data = vmList.get(i-1).getCpu();
                            }
                        }
                    }
                    if(data > 100d){data = 100d;}
                    vm.setCpu(data);
                    break;
                case "ram":
                    if(seconds){
                        if(i == size-1){
                            if(data == 0.0){
                                data = vmList.get(i-1).getRam();
                            }
                        }
                    }
                    if(data > 100d){data = 100d;}
                    vm.setRam(data);
                    break;
                case "read":
                    if(seconds){
                        if(i == size-1){
                            if(data == 0.0){
                                data = vmList.get(i-1).getDiskRead();
                            }
                        }
                    }
                    vm.setDiskRead(data);
                    break;
                case "write":
                    if(seconds){
                        if(i == size-1){
                            if(data == 0.0){
                                data = vmList.get(i-1).getDiskWrite();
                            }
                        }
                    }
                    vm.setDiskWrite(data);
                    break;
                case "incomming":
                    if(seconds){
                        if(i == size-1){
                            if(data == 0.0){
                                data = vmList.get(i-1).getNetIn();
                            }
                        }
                    }
                    vm.setNetIn(data);
                    break;
                case "outgoing":
                    if(seconds){
                        if(i == size-1){
                            if(data == 0.0){
                                data = vmList.get(i-1).getNetOut();
                            }
                        }
                    }
                    vm.setNetOut(data);
                    break;
                case "volumeused":
                    if(seconds){
                        if(i == size-1){
                            if(data == 0.0){
                                data = vmList.get(i-1).getVolumeUsed();
                            }
                        }
                    }
                    vm.setVolumeUsed(data);
                    break;
              }
            vmList.add(i, vm);
        }
        for(int i = 0;i < vmList.size();i++){
            VmIndicator vmdata = vmList.get(i);
            if(null!=createTime&&createTime.before(vmdata.getTimestamp())&&vmdata.getTimestamp().before(now)){
            
                if(!vmdata.getMongodb()){           //如果没有从Mongo里取到数据，则从左右补数
                    switch(type){
                        case "cpu":
                            if(i == 0){
                                vmdata.setCpu(vmList.get(i+1).getCpu());
                            }else if(i > 0 && i < vmList.size() - 1){
                                Double data = (vmList.get(i-1).getCpu()+vmList.get(i+1).getCpu())/2d;
                                vmdata.setCpu(data);
                            }else if(i == vmList.size() - 1){
                                vmdata.setCpu(vmList.get(i-1).getCpu());
                            }
                            break;
                        case "ram":
                            if(i == 0){
                                vmdata.setRam(vmList.get(i+1).getRam());
                            }else if(i > 0 && i < vmList.size() - 1){
                                Double data = (vmList.get(i-1).getRam()+vmList.get(i+1).getRam())/2d;
                                vmdata.setRam(data);
                            }else if(i == vmList.size() - 1){
                                vmdata.setRam(vmList.get(i-1).getRam());
                            }
                            break;
                        case "read":
                            if(i == 0){
                                vmdata.setDiskRead(vmList.get(i+1).getDiskRead());
                            }else if(i > 0 && i < vmList.size() - 1){
                                Double data = (vmList.get(i-1).getDiskRead()+vmList.get(i+1).getDiskRead())/2d;
                                vmdata.setDiskRead(data);
                            }else if(i == vmList.size() - 1){
                                vmdata.setDiskRead(vmList.get(i-1).getDiskRead());
                            }
                            break;
                        case "write":
                            if(i == 0){
                                vmdata.setDiskWrite(vmList.get(i+1).getDiskWrite());
                            }else if(i > 0 && i < vmList.size() - 1){
                                Double data = (vmList.get(i-1).getDiskWrite()+vmList.get(i+1).getDiskWrite())/2d;
                                vmdata.setDiskWrite(data);
                            }else if(i == vmList.size() - 1){
                                vmdata.setDiskWrite(vmList.get(i-1).getDiskWrite());
                            }
                            break;
                        case "incomming":
                            if(i == 0){
                                vmdata.setNetIn(vmList.get(i+1).getNetIn());
                            }else if(i > 0 && i < vmList.size() - 1){
                                Double data = (vmList.get(i-1).getNetIn()+vmList.get(i+1).getNetIn())/2d;
                                vmdata.setNetIn(data);
                            }else if(i == vmList.size() - 1){
                                vmdata.setNetIn(vmList.get(i-1).getNetIn());
                            }
                            break;
                        case "outgoing":
                            if(i == 0){
                                vmdata.setNetOut(vmList.get(i+1).getNetOut());
                            }else if(i > 0 && i < vmList.size() - 1){
                                Double data = (vmList.get(i-1).getNetOut()+vmList.get(i+1).getNetOut())/2d;
                                vmdata.setNetOut(data);
                            }else if(i == vmList.size() - 1){
                                vmdata.setNetOut(vmList.get(i-1).getNetOut());
                            }
                            break;
                        case "volumeused":
                            if(i == 0){
                                vmdata.setVolumeUsed(vmList.get(i+1).getVolumeUsed());
                            }else if(i > 0 && i < vmList.size() - 1){
                                Double data = (vmList.get(i-1).getVolumeUsed()+vmList.get(i+1).getVolumeUsed())/2d;
                                vmdata.setVolumeUsed(data);
                            }else if(i == vmList.size() - 1){
                                vmdata.setVolumeUsed(vmList.get(i-1).getVolumeUsed());
                            }
                            break;
                      }
                }
            }
        }
        return vmList;
    }
}
