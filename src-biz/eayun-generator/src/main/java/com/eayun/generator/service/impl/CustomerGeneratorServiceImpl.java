package com.eayun.generator.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.charge.bean.ChargeConstant;
import com.eayun.common.ConstantClazz;
import com.eayun.common.constant.*;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.service.SerialNumService;
import com.eayun.common.tools.SeqTool;
import com.eayun.common.util.*;
import com.eayun.costcenter.bean.RecordBean;
import com.eayun.costcenter.model.MoneyAccount;
import com.eayun.costcenter.model.MoneyRecord;
import com.eayun.costcenter.service.AccountOverviewService;
import com.eayun.costcenter.service.ChangeBalanceService;
import com.eayun.customer.dao.CustomerDao;
import com.eayun.customer.dao.RoleDao;
import com.eayun.customer.dao.RolePowerDao;
import com.eayun.customer.dao.UserDao;
import com.eayun.customer.ecmcservice.EcmcCustomerService;
import com.eayun.customer.model.*;
import com.eayun.customer.serivce.CustomerService;
import com.eayun.customer.serivce.UserService;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.datacenter.model.DcDataCenter;
import com.eayun.datacenter.service.DataCenterService;
import com.eayun.generator.constant.CusGeneratorConstant;
import com.eayun.generator.service.CustomerGeneratorService;
import com.eayun.monitor.model.AlarmMessage;
import com.eayun.monitor.service.MonitorAlarmService;
import com.eayun.order.dao.OrderDao;
import com.eayun.order.model.BaseOrder;
import com.eayun.order.model.BaseOrderStateRecord;
import com.eayun.order.model.Order;
import com.eayun.price.bean.ParamBean;
import com.eayun.price.bean.PriceDetails;
import com.eayun.project.dao.CloudProjectDao;
import com.eayun.virtualization.dao.CloudVmDao;
import com.eayun.virtualization.model.BaseCloudProject;
import com.eayun.virtualization.model.BaseCloudVm;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.service.SecurityGroupService;
import com.eayun.virtualization.service.VmService;
import com.sun.xml.internal.bind.v2.model.annotation.RuntimeAnnotationReader;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by ZH.F on 2016/12/21.
 */
@Service
@Transactional
public class CustomerGeneratorServiceImpl implements CustomerGeneratorService {

    private final static Logger log = LoggerFactory.getLogger(CustomerGeneratorServiceImpl.class);

    @Autowired
    private EcmcCustomerService ecmcCustomerService;

    @Autowired
    private AccountOverviewService accountOverviewService;

    @Autowired
    private UserService userService;

    @Autowired
    private MonitorAlarmService monitorAlarmService;

    @Autowired
    private ChangeBalanceService changeBalanceService;

    @Autowired
    private CloudVmDao vmDao;

    @Autowired
    private SerialNumService snService;

    @Autowired
    private CloudProjectDao cloudProjectDao;

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private RolePowerDao rolePowerDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private DataCenterService dataCenterService;

    @Override
    public Map<String, Object> createProject(CloudProject cloudProject, Customer customer, boolean b) throws Exception {
        List<DcDataCenter> dcList = dataCenterService.getAllList();
        DcDataCenter dc = dcList.get(0);
        cloudProject.setDcId(dc.getId());
        Map<String, Object> returnMap = new HashMap<String, Object>();
        customer.setCusNumber(customer.getCusNumber() + "Admin");
        cloudProject.setBelongOrg(customer.getCusOrg());
        cloudProject.setRouteCount(cloudProject.getNetWork());
        if (cloudProject.getSafeGroup() < 3) {
            cloudProject.setSafeGroup(3);
        }
        SeqTool.generateNextNumber(customer.getCusOrg(), "_", 2);//确认保存序号+1
        String dcId = cloudProject.getDcId();// 获取数据中心ID
        BaseDcDataCenter datacenter = dataCenterService.getById(dcId);// 获取数据中心
        try {
            // Skip...封装要保存到虚拟化平台的数据
            customer.setIsBlocked(false);
            customer.setBlockopStatus(false);
            if (customer.getCusId() == null) {
                customer = this.addCustomer(customer);
            } else {
                ecmcCustomerService.updateCustomer(customer, true);
            }
            returnMap.put("customer", customer);
            cloudProject.setCustomerId(customer.getCusId());
            cloudProject.setLabelInId(CusGeneratorConstant.LABLE_IN_ID);
            cloudProject.setLabelOutId(CusGeneratorConstant.LABLE_OUT_ID);
            BaseCloudProject baseProject = new BaseCloudProject();
            BeanUtils.copyPropertiesByModel(baseProject, cloudProject);
            baseProject.setCreateDate(new Date());
            baseProject = cloudProjectDao.save(baseProject);
            returnMap.put("project", baseProject);
            return returnMap;
        } catch (Exception e) {
            log.error("创建项目发生异常：", e.getMessage());
            throw e;
        }
    }

    @Override
    public void bulkCreateOrders() throws Exception {
        //1,查找自动化测试使用的客户列表
        //2.查找客户下的用户列表
        //3.每个用户创建n个订单，李华说十几条，那么我们就取n=20
        int n = 20;
        List<DcDataCenter> dcList = dataCenterService.getAllList();
        DcDataCenter dc = dcList.get(0);
        List<Customer> customerList = getAutoGeneratedCustomers();
        for (Customer cus : customerList) {
            List<User> userList = userService.getListByCustomer(cus.getCusId());
            for (User user : userList) {
                for(int i=0;i<n;i++){
                    Order order = new Order();
                    order.setOrderType(OrderType.NEW);
                    order.setDcId(dc.getId());
                    order.setProdCount(1);
                    order.setProdConfig("This is a test product config detail");
                    order.setPayType(PayType.PAYAFTER);
                    order.setResourceType(ResourceType.VM);
                    order.setUserId(user.getUserId());
                    order.setCusId(user.getCusId());
                    order.setProdName("Stress Testing");
                    try {
                        createOrder(order);
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }
            }
        }
    }

    @Override
    public void bulkCreateTradeRecords() throws Exception {
        //获取客户列表
        //每个客户m=100条交易记录
        int m=100;
        List<DcDataCenter> dcList = dataCenterService.getAllList();
        DcDataCenter dc = dcList.get(0);
        List<Customer> customerList = getAutoGeneratedCustomers();
        Date currentTime = new Date();
        Date chargeFrom = DateUtil.addDay(currentTime,new int[]{0,0,0,-1});
        for(Customer cus : customerList){
            TradeGenThread thread = new TradeGenThread(cus, dc, currentTime, this.accountOverviewService);
            new ThreadPoolExecutor(100, 100, 1L, TimeUnit.MINUTES,new LinkedBlockingQueue<Runnable>()).submit(thread);
//            for(int i=0;i<m; i++){
//                RecordBean rb = new RecordBean();
//                rb.setExchangeTime(currentTime);
//                rb.setEcscRemark("消费-StressTest-按需付费");
//                rb.setEcmcRemark("消费-StressTest-按需付费");
//                rb.setExchangeMoney(new BigDecimal(66.66));
//                rb.setProductName(ResourceType.VM + "-按需付费");
//                rb.setOrderNo("012016083000000063");
//                rb.setResourceId("FAKE_RES_ID_"+cus.getCusNumber());
//                rb.setResourceName("FAKE_RES_NAME");
//                rb.setPayType(PayType.PAYAFTER);
//                rb.setIncomeType("2");
//                rb.setMonStart(chargeFrom);
//                rb.setMonEnd(currentTime);
//                rb.setParamBean(new ParamBean());
//                rb.setPriceDetails(new PriceDetails());
//                rb.setCusId(cus.getCusId());
//                rb.setResourceType(ResourceType.VM);
//                rb.setOperType(TransType.EXPEND);
//                rb.setDcId(dc.getId());
//                MoneyRecord moneyRecord=genRecord(rb);
//                accountOverviewService.changeBalanceByCharge(moneyRecord);
//            }
        }

    }

    @Override
    public void bulkCreateAlarmMessage() {
        //1. 查询所有的自动生成的vm记录
        List<BaseCloudVm> vmList = getFakeVmRecord();
        //2. 每条vm记录生成一条报警信息
        for(BaseCloudVm vm: vmList){
            genFakeAlarmMessage(vm);
        }

    }

    private void genFakeAlarmMessage(BaseCloudVm vm) {
        log.info("为vm-"+vm.getVmId()+"生成报警信息");
        AlarmMessage alarmMsg = new AlarmMessage();
        alarmMsg.setVmId(vm.getVmId());
        alarmMsg.setMonitorType("云主机");
        alarmMsg.setAlarmType("False alarm");

        StringBuffer detail = new StringBuffer();
        detail.append("指标xxx")
                .append(">=").append("阈值xxx")
                .append("%")
                .append("已持续5min" );
        alarmMsg.setDetail(detail.toString());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStr = sdf.format(new Date());
        Date time = null;
        try {
            time = sdf.parse(timeStr);
        } catch (ParseException e) {
            throw new AppException(e.getMessage());
        }
        alarmMsg.setTime(time);
        alarmMsg.setAlarmTime(timeStr);

        alarmMsg.setMonitorAlarmItemId("XXXXXX");
        alarmMsg.setAlarmRuleId("XXXXXX");

        //这个状态只有在报警信息前台展现时可以操作更改为已处理，其他地方操作不了
        alarmMsg.setIsProcessed("0");
        //将报警信息保存入库
        monitorAlarmService.addAlarmMessage(alarmMsg);
    }

    private MoneyRecord genRecord(RecordBean recordBean) {
        MoneyRecord moneyRecord=new MoneyRecord();
        moneyRecord.setIncomeType(recordBean.getIncomeType());
        moneyRecord.setMonEcmcRemark(recordBean.getEcmcRemark());
        moneyRecord.setMonEcscRemark(recordBean.getEcscRemark());
        moneyRecord.setMoney(recordBean.getExchangeMoney());
        Date exchangeTime=recordBean.getExchangeTime();
        moneyRecord.setMonTime(exchangeTime);
        moneyRecord.setMonPaymonth(formatDate(exchangeTime,"yyyy-MM"));
        moneyRecord.setOperType(recordBean.getOperType());
        moneyRecord.setOrderNo(recordBean.getOrderNo());
        moneyRecord.setPayType(recordBean.getPayType());
        moneyRecord.setProductName(recordBean.getProductName());
        moneyRecord.setResourceId(recordBean.getResourceId());
        moneyRecord.setResourceName(recordBean.getResourceName());
        moneyRecord.setMonStart(recordBean.getMonStart());
        moneyRecord.setMonEnd(recordBean.getMonEnd());
        moneyRecord.setResourceType(recordBean.getResourceType());
        moneyRecord.setCusId(recordBean.getCusId());
        moneyRecord.setDcId(recordBean.getDcId());
        ParamBean paramBean=recordBean.getParamBean();
        PriceDetails priceDetails=recordBean.getPriceDetails();
        if(paramBean!=null&&priceDetails!=null){
            JSONArray array=new JSONArray();
            if(ResourceType.VM.equals(recordBean.getResourceType())){
                JSONObject jsonCpuAndRam=new JSONObject();
                String cpuAndRamName="CPU："+paramBean.getCpuSize()+"核&nbsp;&nbsp;&nbsp;&nbsp;内存："+paramBean.getRamCapacity()+"GB";
                double result=recordBean.getMonEnd().getTime()-recordBean.getMonStart().getTime();
                int hours=(int) Math.ceil(result/(60*60*1000));
                String units=hours+"小时";
                BigDecimal cpuPrice=new BigDecimal("0.000");
                if(priceDetails.getCpuPrice()!=null){
                    cpuPrice=priceDetails.getCpuPrice();
                }
                BigDecimal ramPrice=new BigDecimal("0.000");
                if(priceDetails.getRamPrice()!=null){
                    ramPrice=priceDetails.getRamPrice();
                }
                String cpuAndRamPrice=cpuPrice.add(ramPrice).toString();
                jsonCpuAndRam.put("name", cpuAndRamName);
                jsonCpuAndRam.put("price", cpuAndRamPrice);
                jsonCpuAndRam.put("units", units);
                array.add(jsonCpuAndRam);
                JSONObject jsonDisk=new JSONObject();
                String sysDiskName="系统盘："+paramBean.getSysDiskCapacity()+"GB";
                BigDecimal diskPrice=new BigDecimal("0.000");
                if(priceDetails.getSysDiskPrice()!=null){
                    diskPrice=priceDetails.getSysDiskPrice();
                }
                String sysDiskPrice=diskPrice.toString();
                jsonDisk.put("name", sysDiskName);
                jsonDisk.put("price", sysDiskPrice);
                jsonDisk.put("units", units);
                array.add(jsonDisk);
                JSONObject jsonImage=new JSONObject();
                String imageName="镜像："+recordBean.getImageName();
                BigDecimal imaPrice=new BigDecimal("0.000");
                if(priceDetails.getImagePrice()!=null){
                    imaPrice=priceDetails.getImagePrice();
                }
                String imagePrice=imaPrice.toString() ;
                jsonImage.put("name", imageName);
                jsonImage.put("price", imagePrice);
                jsonImage.put("units", units);
                array.add(jsonImage);
            }else if(ResourceType.VDISK.equals(recordBean.getResourceType())){
                JSONObject json=new JSONObject();
                String dataDiskName="容量："+paramBean.getDataDiskCapacity()+"GB";
                BigDecimal diskPrice=new BigDecimal("0.000");
                if(priceDetails.getDataDiskPrice()!=null){
                    diskPrice=priceDetails.getDataDiskPrice();
                }
                String dataDiskPrice=diskPrice.toString();
                double result=recordBean.getMonEnd().getTime()-recordBean.getMonStart().getTime();
                int hours=(int) Math.ceil(result/(60*60*1000));
                String units=hours+"小时";
                json.put("name", dataDiskName);
                json.put("price", dataDiskPrice);
                json.put("units", units);
                array.add(json);
            }else if(ResourceType.DISKSNAPSHOT.equals(recordBean.getResourceType())){
                JSONObject json=new JSONObject();
                String name="备份大小："+paramBean.getSnapshotSize()+"GB";
                BigDecimal snapShotPrice=new BigDecimal("0.000");
                if(priceDetails.getSnapshotPrice()!=null){
                    snapShotPrice=priceDetails.getSnapshotPrice();
                }
                String price=snapShotPrice.toString();
                double result=recordBean.getMonEnd().getTime()-recordBean.getMonStart().getTime();
                int hours=(int) Math.ceil(result/(60*60*1000));
                String units=hours+"小时";
                json.put("name", name);
                json.put("price", price);
                json.put("units", units);
                array.add(json);
            }else if(ResourceType.NETWORK.equals(recordBean.getResourceType())){
                JSONObject json=new JSONObject();
                String name="带宽："+paramBean.getBandValue()+"M";
                BigDecimal bandWidthPrice=new BigDecimal("0.000");
                if(priceDetails.getBandWidthPrice()!=null){
                    bandWidthPrice=priceDetails.getBandWidthPrice();
                }
                String price=bandWidthPrice.toString();
                double result=recordBean.getMonEnd().getTime()-recordBean.getMonStart().getTime();
                int hours=(int) Math.ceil(result/(60*60*1000));
                String units=hours+"小时";
                json.put("name", name);
                json.put("price", price);
                json.put("units", units);
                array.add(json);
            }else if(ResourceType.QUOTAPOOL.equals(recordBean.getResourceType())){
                JSONObject json=new JSONObject();
                String name="连接数："+paramBean.getConnCount();
                BigDecimal poolPrice=new BigDecimal("0.000");
                if(priceDetails.getPoolPrice()!=null){
                    poolPrice=priceDetails.getPoolPrice();
                }
                String price=poolPrice.toString();
                double result=recordBean.getMonEnd().getTime()-recordBean.getMonStart().getTime();
                int hours=(int) Math.ceil(result/(60*60*1000));
                String units=hours+"小时";
                json.put("name", name);
                json.put("price", price);
                json.put("units", units);
                array.add(json);
            }else if(ResourceType.FLOATIP.equals(recordBean.getResourceType())){
                JSONObject json=new JSONObject();
                String name="公网IP地址："+recordBean.getResourceName();
                BigDecimal ipPrice=new BigDecimal("0.000");
                if(priceDetails.getIpPrice()!=null){
                    ipPrice=priceDetails.getIpPrice();
                }
                String price=ipPrice.toString();
                double result=recordBean.getMonEnd().getTime()-recordBean.getMonStart().getTime();
                int hours=(int) Math.ceil(result/(60*60*1000));
                String units=hours+"小时";
                json.put("name", name);
                json.put("price", price);
                json.put("units", units);
                array.add(json);
            }else if(ResourceType.OBS.equals(recordBean.getResourceType())){
                JSONObject jsonSpaceCapacity=new JSONObject();
                String spaceCapacity="存储空间";
                BigDecimal spacePrice=new BigDecimal("0.000");
                if(priceDetails.getSpacePrice()!=null){
                    spacePrice=priceDetails.getSpacePrice();
                }
                String spaceCapacityPrice=spacePrice.toString();
                DecimalFormat df = new DecimalFormat("#,##0.000");
                String spaceCapacityUnits=df.format(paramBean.getSpaceCapacity())+"GB";
                jsonSpaceCapacity.put("name", spaceCapacity);
                jsonSpaceCapacity.put("price", spaceCapacityPrice);
                jsonSpaceCapacity.put("units", spaceCapacityUnits);
                array.add(jsonSpaceCapacity);
                JSONObject jsonDownValue=new JSONObject();
                String downValue="下载流量";
                BigDecimal downPrice=new BigDecimal("0.000");
                if(priceDetails.getDownPrice()!=null){
                    downPrice=priceDetails.getDownPrice();
                }
                String downValuePrice=downPrice.toString();
                Double[] downs=paramBean.getDownValue();
                Double down=downs[1]-downs[0];
                String downValueUnits=df.format(down)+"GB";
                jsonDownValue.put("name", downValue);
                jsonDownValue.put("price", downValuePrice);
                jsonDownValue.put("units", downValueUnits);
                array.add(jsonDownValue);
                JSONObject jsonRequestCount=new JSONObject();
                String requestCount="请求次数";
                BigDecimal reqPrice=new BigDecimal("0.000");
                if(priceDetails.getRequestPrice()!=null){
                    reqPrice=priceDetails.getRequestPrice();
                }
                String requestCountPrice=reqPrice.toString();
                Long[] counts=paramBean.getRequestCount();
                Long count=counts[1]-counts[0];
                DecimalFormat dfLong = new DecimalFormat("#,###");
                String requestCountUnits=dfLong.format(count)+"次";
                jsonRequestCount.put("name", requestCount);
                jsonRequestCount.put("price", requestCountPrice);
                jsonRequestCount.put("units", requestCountUnits);
                array.add(jsonRequestCount);
                //todo 增加CDN下载流量、CDN动态请求数、CDN-HTTPS请求数
                array.add(generateCdnDownloadRecord(priceDetails, paramBean));
                array.add(generateCdnDreqsRecord(priceDetails, paramBean));
                array.add(generateCdnHreqsRecord(priceDetails, paramBean));
            }else if(ResourceType.VPN.equals(recordBean.getResourceType())){
                JSONObject json=new JSONObject();
                String name=recordBean.getVpnInfo();
                BigDecimal vpnPrice=new BigDecimal("0.000");
                if(priceDetails.getVpnPrice()!=null){
                    vpnPrice=priceDetails.getVpnPrice();
                }
                String price=vpnPrice.toString();
                double result=recordBean.getMonEnd().getTime()-recordBean.getMonStart().getTime();
                int hours=(int) Math.ceil(result/(60*60*1000));
                String units=hours+"小时";
                json.put("name", name);
                json.put("price", price);
                json.put("units", units);
                array.add(json);
            }
            moneyRecord.setMonConfigure(array.toString());

        }
        moneyRecord.setMonContract(recordBean.getMonContract());
        return moneyRecord;
    }

    private String formatDate(Date date,String pattern){
        SimpleDateFormat sdf=new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    private JSONObject generateCdnHreqsRecord(PriceDetails priceDetails, ParamBean paramBean) {
        JSONObject hreqs = new JSONObject();
        String name = "CDN-HTTPS请求数";
        BigDecimal hreqsPrice = new BigDecimal("0.000");
        if(priceDetails.getCdnHreqsPrice()!=null){
            hreqsPrice = priceDetails.getCdnHreqsPrice();
        }
        String hreqsPriceStr = hreqsPrice.toString();
        Long cdnHreqs = paramBean.getHreqsCount();
        Double count = cdnHreqs/10000.0;
        DecimalFormat dfLong = new DecimalFormat("#.###");
        String unit = dfLong.format(count)+"万次";
        hreqs.put("name", name);
        hreqs.put("price", hreqsPriceStr);
        hreqs.put("units", unit);
        return hreqs;
    }

    private JSONObject generateCdnDreqsRecord(PriceDetails priceDetails, ParamBean paramBean) {
        JSONObject dreqs = new JSONObject();
        String name = "CDN动态请求数";
        BigDecimal dreqsPrice = new BigDecimal("0.000");
        if(priceDetails.getCdnDreqsPrice()!=null){
            dreqsPrice = priceDetails.getCdnDreqsPrice();
        }
        String dreqsPriceStr = dreqsPrice.toString();
        Long cdnDreqs = paramBean.getDreqsCount();
        Double count = cdnDreqs/1000.0;
        DecimalFormat dfLong = new DecimalFormat("#.###");
        String unit = dfLong.format(count)+"千次";
        dreqs.put("name", name);
        dreqs.put("price", dreqsPriceStr);
        dreqs.put("units", unit);
        return dreqs;
    }

    private JSONObject generateCdnDownloadRecord(PriceDetails priceDetails, ParamBean paramBean) {
        JSONObject cdnDownload=new JSONObject();
        String name="CDN下载流量";
        BigDecimal cdnDownloadPrice=new BigDecimal("0.000");
        if(priceDetails.getCdnDownloadPrice()!=null){
            cdnDownloadPrice=priceDetails.getCdnDownloadPrice();
        }
        String cdnDownloadPriceStr=cdnDownloadPrice.toString();
        Long download=paramBean.getCdnDownloadFlow();//CDN下载流量，存储是单位是B
        Double count = download/1024/1024/1024.0;
        DecimalFormat dfLong = new DecimalFormat("#.###");
        String unit=dfLong.format(count)+"GB";
        cdnDownload.put("name", name);
        cdnDownload.put("price", cdnDownloadPriceStr);
        cdnDownload.put("units", unit);
        return cdnDownload;
    }

    private void createOrder(Order order) throws Exception {
        log.info("开始生成订单。");
        // 是否记录支付日志标识
        boolean addPayLogFlag = false;
        Date date = new Date();
        // 生成订单编号
        order.setOrderNo(getOrderNo(date));
        order.setCreateTime(date);
        order.setPayExpireTime(DateUtil.addDay(date, new int[]{0, 0, 1})); // 设置超时时间24小时
        order.setThirdPartType(ThirdPartType.ALIPAY);
        order.setVersion(0);

        // 余额支付
        String tradeNo = null;
        // 设置订单状态
        if (order.getPayType().equals(PayType.PAYBEFORE)
                && order.getThirdPartPayment().compareTo(BigDecimal.ZERO) == 1) {
            // 订单为预付费，且第三方支付金额大于0，订单状态为待支付
            order.setOrderState(OrderStateType.TO_BE_PAID);
        } else if (order.getPayType().equals(PayType.PAYAFTER) || (order.getPayType().equals(PayType.PAYBEFORE)
                && (tradeNo != null || order.getAccountPayment().compareTo(BigDecimal.ZERO) == 0)
                && order.getThirdPartPayment().compareTo(BigDecimal.ZERO) == 0)) {
            // 订单为后付费，或者订单为预付费且余额支付所有费用，订单状态为资源创建中
            order.setOrderState(OrderStateType.BUILDING_RESOURCE);
        }
        BaseOrder baseOrder = new BaseOrder();
        try {
            log.info("订单信息：{}", JSON.toJSONString(order));
            BeanUtils.copyPropertiesByModel(baseOrder, order);
            // 保存订单信息
            orderDao.save(baseOrder);
        } catch (Exception e) {
            throw e;
        }
    }

    private String getOrderNo(Date date) {
        if (date == null) {
            date = new Date();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(date);
        String jedisKey = "01" + dateStr;
        return snService.getSerialNum(jedisKey, 8);
    }


    private Customer addCustomer(Customer customer) throws Exception {
        customer.setCreatTime(new Date());
        customer.setCusFalg('1');

        BaseCustomer baseCustomer = new BaseCustomer();
        BeanUtils.copyPropertiesByModel(baseCustomer, customer);
        baseCustomer = customerDao.save(baseCustomer);
        BeanUtils.copyPropertiesByModel(customer, baseCustomer);
        //------添加超级管理员角色
        String roleId = createRoles(baseCustomer.getCusId());

        //------添加超级管理员用户
        User selfUser = new User();
        selfUser.setCusId(customer.getCusId());
        selfUser.setUserAccount(customer.getCusNumber());
        selfUser.setRoleId(roleId);
        selfUser.setCusName(customer.getCusName());
        selfUser.setUserPerson(customer.getCusName());
        selfUser.setUserEmail(customer.getCusEmail());
        selfUser.setUserPhone(customer.getCusPhone());
        selfUser.setIsPhoneValid(true);
        selfUser.setIsMailValid(false);
        selfUser.setIsAdmin(true);
        selfUser.setCreateTime(new Date());
        selfUser.setUserExplain("超级管理员");
        selfUser.setIsBlocked(false);
        selfUser.setLastTime(new Date());
        this.addSelfUser(selfUser);
        return customer;
    }

    private String createRoles(String cusId) throws BiffException, IOException {
        String rootRoleId = "";
        List<BasePower> powerList = new ArrayList<BasePower>();
        StringBuffer hql = new StringBuffer("from BasePower");
        powerList = customerDao.find(hql.toString());
        List<List<String>> rolepowList = getData();
        for (int i = 0; i < rolepowList.size(); i++) {
            BaseRole baseRole = new BaseRole();
            baseRole.setCusId(cusId);
            if (i == 0) {
                baseRole.setRoleName("超级管理员");
                baseRole.setRoleDesc("超级管理员");
            }
            if (i == 1) {
                baseRole.setRoleName("管理员");
                baseRole.setRoleDesc("管理员");
            }
            if (i == 2) {
                baseRole.setRoleName("普通用户");
                baseRole.setRoleDesc("普通用户");
            }
            baseRole = roleDao.save(baseRole);
            if (i == 0) {
                rootRoleId = baseRole.getRoleId();
            }

            List<String> powers = rolepowList.get(i);
            for (int j = 0; j < powerList.size(); j++) {
                for (int k = 0; k < powers.size(); k++) {
                    if (powers.get(k).equals(powerList.get(j).getPowerRoute())) {
                        BaseRolePower rolePower = new BaseRolePower();
                        rolePower.setRoleId(baseRole.getRoleId());
                        rolePower.setPowerId(powerList.get(j).getPowerId());
                        rolePowerDao.save(rolePower);
                    }
                }
            }
        }
        return rootRoleId;
    }

    private List<List<String>> getData() throws BiffException, IOException {
        String filePath = getClass().getResource("/").getFile().toString();
        InputStream is = new FileInputStream(filePath + "/role-power.xls");
        Workbook rwb = Workbook.getWorkbook(is);
        Sheet sheet = rwb.getSheet(0);
        int row = sheet.getRows();
        int col = sheet.getColumns();
        List<List<String>> rolepowList = new ArrayList<List<String>>();
        for (int i = 0; i < col; i++) {
            Cell[] firstcell = sheet.getColumn(0);
            if (i <= 1) continue;
            Cell[] cell = sheet.getColumn(i);
            if (cell.length == 0) continue;
            List<String> powList = new ArrayList<String>();
            for (int j = 0; j < cell.length; j++) {
                if (i == 0) continue;
                String RowString = cell[j].getContents();
                String Route = firstcell[j].getContents();
                if (RowString.equals("Y")) {
                    powList.add(Route);
                }
            }
            rolepowList.add(powList);
        }
        return rolepowList;
    }


    private void addSelfUser(User user) throws Exception {
        user.setUserAccount(user.getUserAccount());
        Random random = new Random();
        int salt = random.nextInt(899999) + 100000;//生成一个临时的6位数
        user.setSalt(String.valueOf(salt));
        String password = user.getUserAccount() + "P@ssw0rd";
        MD5 md5 = new MD5();
        user.setUserPassword(md5.getMD5ofStr(md5.getMD5ofStr(password) + String.valueOf(salt)));

        BaseUser baseUser = new BaseUser();
        BeanUtils.copyPropertiesByModel(baseUser, user);
        userDao.save(baseUser);
        log.info("添加客户：[" + user.getUserAccount() + "]成功，密码为：[" + password + "]");
    }

    public List<Customer> getAutoGeneratedCustomers() {
        StringBuffer hql = new StringBuffer();
        hql.append(" from BaseCustomer where cusNumber like ?");
        List<BaseCustomer> baseCusList = customerDao.find(hql.toString(), CusGeneratorConstant.CUS_PREFIX+"%");
        List<Customer> cusList = new ArrayList<>();
        for (BaseCustomer baseCus : baseCusList) {
            Customer cus = new Customer();
            BeanUtils.copyPropertiesByModel(cus, baseCus);
            cusList.add(cus);
        }
        return cusList;
    }

    public List<BaseCloudVm> getFakeVmRecord() {
        List<BaseCloudVm> vmList = new ArrayList<>();
        StringBuffer hql = new StringBuffer();
        hql.append(" from BaseCloudVm ");
        hql.append(" where isDeleted = '0' and vmStatus in ('SHUTOFF','ACTIVE','SUSPENDED') and prjId like ?  ");

        vmList = vmDao.find(hql.toString(), new Object[] { "ID_FOR_AUTO_GENERATED_CUS%" });
        return vmList;
    }

    class TradeGenThread implements Runnable{
        private final Logger log = LoggerFactory.getLogger(TradeGenThread.class);
        private Date currentTime;
        private Customer customer;
        private DcDataCenter dataCenter;
        private AccountOverviewService accountOverviewService;
        TradeGenThread(Customer customer, DcDataCenter dataCenter, Date currentTime, AccountOverviewService accountOverviewService){
            this.customer = customer;
            this.dataCenter = dataCenter;
            this.currentTime = currentTime;
            this.accountOverviewService = accountOverviewService;
        }

        @Override
        public void run() {
            log.info("开始执行TradeGenThread-"+customer.getCusNumber());
            Date chargeFrom = DateUtil.addDay(currentTime, new int[]{0,0,0,-1});
            int m=100;
            for(int i=0;i<m; i++){
                RecordBean rb = new RecordBean();
                rb.setExchangeTime(currentTime);
                rb.setEcscRemark("消费-StressTest-按需付费");
                rb.setEcmcRemark("消费-StressTest-按需付费");
                rb.setExchangeMoney(new BigDecimal(66.66));
                rb.setProductName(ResourceType.VM + "-按需付费");
                rb.setOrderNo("012016083000000063");
                rb.setResourceId("FAKE_RES_ID_"+customer.getCusNumber());
                rb.setResourceName("FAKE_RES_NAME");
                rb.setPayType(PayType.PAYAFTER);
                rb.setIncomeType("2");
                rb.setMonStart(chargeFrom);
                rb.setMonEnd(currentTime);
                rb.setParamBean(new ParamBean());
                rb.setPriceDetails(new PriceDetails());
                rb.setCusId(customer.getCusId());
                rb.setResourceType(ResourceType.VM);
                rb.setOperType(TransType.EXPEND);
                rb.setDcId(dataCenter.getId());
                MoneyRecord moneyRecord=genRecord(rb);
                try {
                    accountOverviewService.changeBalanceByCharge(moneyRecord);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            log.info("完成执行TradeGenThread-"+customer.getCusNumber());
        }
    }
}
