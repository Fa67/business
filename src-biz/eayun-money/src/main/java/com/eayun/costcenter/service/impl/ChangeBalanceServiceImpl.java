package com.eayun.costcenter.service.impl;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.EayunQueueConstant;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.constant.ResourceType;
import com.eayun.common.exception.AppException;
import com.eayun.common.exception.CuratorException;
import com.eayun.common.template.EayunRabbitTemplate;
import com.eayun.common.util.TransactionHookUtil;
import com.eayun.common.util.TransactionHookUtil.Hook;
import com.eayun.common.zk.DistributedLockBean;
import com.eayun.common.zk.DistributedLockService;
import com.eayun.common.zk.LockService;
import com.eayun.costcenter.bean.RecordBean;
import com.eayun.costcenter.model.MoneyAccount;
import com.eayun.costcenter.model.MoneyRecord;
import com.eayun.costcenter.service.AccountOverviewService;
import com.eayun.costcenter.service.ChangeBalanceService;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.price.bean.ParamBean;
import com.eayun.price.bean.PriceDetails;
import com.eayun.virtualization.service.ResourceDisposeService;
/**
 * 对外提供修改账户金额接口实现类
 * @author xiangyu.cao@eayun.com
 *
 */
@Service
@Transactional
public class ChangeBalanceServiceImpl implements ChangeBalanceService{
    private static final Logger log = LoggerFactory.getLogger(ChangeBalanceServiceImpl.class);
    
	@Autowired
	private DistributedLockService distributedLockService;
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private AccountOverviewService accountOverviewService;
	@Autowired
	private EayunRabbitTemplate eayunRabbitTemplate;
	@Autowired
	private MessageCenterService messageCenterService;
	@Autowired
	private ResourceDisposeService resourceDispostService;
	
	/**
	 * 修改账户余额(按时扣费)
	 * @param recordBean
	 * @return
	 * @throws Exception
	 */
	@Override
	public BigDecimal changeBalanceByCharge(RecordBean recordBean){
		log.info("开始修改账户余额(按时扣费),交易金额："+recordBean.getExchangeMoney().toString()+",交易客户："+recordBean.getCusId());
		final MoneyRecord moneyRecord=genRecord(recordBean);
		DistributedLockBean dlBean=new DistributedLockBean();
	    dlBean.setGranularity("changemoney"+recordBean.getCusId());
	    dlBean.setLockService(new LockService() {
		
			@Override
			public Object doService() throws Exception {
				MoneyAccount money=accountOverviewService.changeBalanceByCharge(moneyRecord);
				return money;
			}
	    });
	    MoneyAccount moneyAccount=null;
	    try {
	    	moneyAccount=(MoneyAccount)distributedLockService.doServiceByLock(dlBean,true);
			if(moneyAccount.isSendMessage()){
				int count=resourceDispostService.getWillStopResourceCount(moneyAccount.getCusId(), null);
				if(count>0){
					messageCenterService.accountArrearsMessage(moneyAccount.getCusId(), count);
				}
			}
	    } catch (CuratorException e) {
	        log.error(e.getMessage(), e);
			mongoTemplate.insert(javaToJson(moneyRecord),MongoCollectionName.LOG_MONEY_FAILED);
			throw e;
		} 
		
		return moneyAccount.getMoney();
	}
	/**
	 * 修改账户余额(充值,退款,系统,第三方支付消费)并发送消息
	 * @param recordBean
	 * @return
	 * @throws Exception
	 */
	@Override
	public BigDecimal changeBalance(RecordBean recordBean){
		log.info("开始修改账户余额(充值,退款,系统,第三方支付消费)并发送消息,交易金额："+recordBean.getExchangeMoney().toString()+",交易客户："+recordBean.getCusId());
		final MoneyRecord moneyRecord=genRecord(recordBean);
		DistributedLockBean dlBean=new DistributedLockBean();
	    dlBean.setGranularity("changemoney"+recordBean.getCusId());
	    dlBean.setLockService(new LockService() {
		
			@Override
			public Object doService() throws Exception {
				MoneyAccount money=null;
				boolean result=false;
				if("1".equals(moneyRecord.getIncomeType())&&moneyRecord.getOrderNo()!=null){
					money=new MoneyAccount();
					result=accountOverviewService.thisOrderIsRefunded(moneyRecord.getOrderNo());
					money.setRefunded(result);
				}
				if(!result){
					money=accountOverviewService.changeBalanceByCharge(moneyRecord);
				}
				return money;
			}
	    });
	    MoneyAccount moneyAccount=null;
	    try {
	    	moneyAccount=(MoneyAccount)distributedLockService.doServiceByLock(dlBean);
	    	if(moneyAccount!=null){
	    		if(moneyAccount.isRefunded()){
	    			throw new AppException("该订单已退过款");
	    		}
	    		sendNotice(recordBean,moneyAccount);
	    		if(moneyAccount.isSendMessage()){
	    			int count=resourceDispostService.getWillStopResourceCount(moneyAccount.getCusId(), null);
	    			if(count>0){
	    				messageCenterService.accountArrearsMessage(moneyAccount.getCusId(), count);
	    			}
	    		}
	    	}
	    } catch (CuratorException e) {
	        log.error(e.getMessage(), e);
	    	mongoTemplate.insert(javaToJson(moneyRecord),MongoCollectionName.LOG_MONEY_FAILED);
			throw e;
		}
		return moneyAccount==null?null:moneyAccount.getMoney();
	}
	/**
	 * 修改账户余额(使用余额支付,购买/续费/升级)
	 * @param recordBean
	 * @return
	 * @throws Exception
	 */
	@Override
	public BigDecimal changeBalanceByPay(RecordBean recordBean) throws Exception{
		log.info("开始修改账户余额(使用余额支付,购买/续费/升级),交易金额："+recordBean.getExchangeMoney().toString()+",交易客户："+recordBean.getCusId());
		final MoneyRecord moneyRecord = genRecord(recordBean);
		DistributedLockBean dlBean=new DistributedLockBean();
	    dlBean.setGranularity("changemoney"+recordBean.getCusId());
	    dlBean.setLockService(new LockService() {
		
		@Override
		public Object doService() throws Exception {
			MoneyAccount moneyAccount=accountOverviewService.changeBalanceByPay(moneyRecord);
			return moneyAccount;
		}
	    });
	    BigDecimal accountMoney=null;
	    try {
	    	MoneyAccount account=(MoneyAccount)distributedLockService.doServiceByLock(dlBean);
	    	if(account!=null&&account.isCanPay()){
	    		accountMoney=account.getMoney();
	    		sendNotice(recordBean,account);
	    		if(account.isSendMessage()){
	    			int count=resourceDispostService.getWillStopResourceCount(account.getCusId(), null);
	    			if(count>0){
	    				messageCenterService.accountArrearsMessage(account.getCusId(), count);
	    			}
	    		}
	    	}
	    	if(account!=null&&!account.isCanPay()){
	 	    	throw new AppException("账户余额不足以支付本次费用");
	 	    }
	    } catch (CuratorException e) {
	        log.error(e.getMessage(), e);
	    	mongoTemplate.insert(javaToJson(moneyRecord),MongoCollectionName.LOG_MONEY_FAILED);
			throw e;
		}
	   
		
		return accountMoney;
	}
	@Override
	public BigDecimal changeBalanceForThird(RecordBean recordBean){
		log.info("开始修改账户余额(充值,退款,第三方支付消费)并发送消息,交易金额："+recordBean.getExchangeMoney().toString()+",交易客户："+recordBean.getCusId());
		final MoneyRecord moneyRecord=genRecord(recordBean);
		DistributedLockBean dlBean=new DistributedLockBean();
	    dlBean.setGranularity("changemoney"+recordBean.getCusId());
	    dlBean.setLockService(new LockService() {
		
			@Override
			public Object doService() throws Exception {
				MoneyAccount money=null;
				boolean result=false;
				if("1".equals(moneyRecord.getIncomeType())&&moneyRecord.getOrderNo()!=null){
					money=new MoneyAccount();
					result=accountOverviewService.thisOrderIsRefunded(moneyRecord.getOrderNo());
					money.setRefunded(result);
				}
				if(!result){
					money=accountOverviewService.changeBalanceForThird(moneyRecord);
				}
				return money;
			}
	    });
	    MoneyAccount moneyAccount=null;
	    try {
	    	moneyAccount=(MoneyAccount)distributedLockService.doServiceByLock(dlBean);
	    	if(moneyAccount!=null){
	    		if(moneyAccount.isRefunded()){
	    			throw new AppException("该订单已退过款");
	    		}
	    		sendNotice(recordBean,moneyAccount);
	    		if(moneyAccount.isSendMessage()){
	    			int count=resourceDispostService.getWillStopResourceCount(moneyAccount.getCusId(), null);
	    			if(count>0){
	    				messageCenterService.accountArrearsMessage(moneyAccount.getCusId(), count);
	    			}
	    		}
	    	}
	    } catch (CuratorException e) {
	        log.error(e.getMessage(), e);
	    	mongoTemplate.insert(javaToJson(moneyRecord),MongoCollectionName.LOG_MONEY_FAILED);
			throw e;
		}
		return moneyAccount==null?null:moneyAccount.getMoney();
	}
	private void sendNotice(RecordBean recordBean,MoneyAccount moneyAccount) {
		final JSONObject json=new JSONObject();
		json.put("customer", recordBean.getCusId());
		if("1".equals(recordBean.getIncomeType())){
			json.put("revenue", recordBean.getExchangeMoney());
		}
		String biz=recordBean.getOperType();
		json.put("biz", biz);
		BigDecimal balance=moneyAccount.getMoney();
		json.put("balance", balance);
		TransactionHookUtil.registAfterCommitHook(new Hook() {
			
			@Override
			public void execute() {
				eayunRabbitTemplate.send(EayunQueueConstant.QUEUE_BALANCE_CHANGE, json.toString());
			}
		});
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
			}else if(ResourceType.RDS.equals(recordBean.getResourceType())){
				JSONObject jsonCpuAndRam=new JSONObject();
				String cpuAndRamName="CPU："+paramBean.getCloudMySQLCPU()+"核&nbsp;&nbsp;&nbsp;&nbsp;内存："+paramBean.getCloudMySQLRAM()+"GB";
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
				Integer dataDiskOrdinary=paramBean.getStorageMySQLOrdinary();
				Integer dataDiskBetter=paramBean.getStorageMySQLBetter();
				Integer dataDiskBest=paramBean.getStorageMySQLBest();
				Integer dataDisk=0;
				if(dataDiskOrdinary!=null){
					dataDisk=dataDiskOrdinary;
				}else if(dataDiskBetter!=null){
					dataDisk=dataDiskBetter;
				}else if(dataDiskBest!=null){
					dataDisk=dataDiskBest;
				}
				String sysDiskName="存储："+dataDisk+"GB";
				BigDecimal diskPrice=new BigDecimal("0.000");
				if(priceDetails.getDataDiskPrice()!=null){
					diskPrice=priceDetails.getDataDiskPrice();
				}
				String sysDiskPrice=diskPrice.toString();
				jsonDisk.put("name", sysDiskName);
				jsonDisk.put("price", sysDiskPrice);
				jsonDisk.put("units", units);
				array.add(jsonDisk);
			}
			moneyRecord.setMonConfigure(array.toString());
			
		}
		moneyRecord.setMonContract(recordBean.getMonContract());
		return moneyRecord;
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

    private String formatDate(Date date,String pattern){
		SimpleDateFormat sdf=new SimpleDateFormat(pattern);
		return sdf.format(date);
	}
	
	private String javaToJson(Object obj){
		JSONObject json=(JSONObject) JSONObject.toJSON(obj);
		json.put("timestamp", new Date());
		return json.toJSONString();
	}
}
