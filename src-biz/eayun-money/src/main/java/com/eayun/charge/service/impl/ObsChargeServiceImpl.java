package com.eayun.charge.service.impl;

import com.eayun.charge.model.ObsStatsBean;
import com.eayun.charge.service.ObsChargeService;
import com.eayun.common.constant.PayType;
import com.eayun.common.constant.ResourceType;
import com.eayun.common.constant.TransType;
import com.eayun.common.redis.JedisUtil;
import com.eayun.costcenter.bean.RecordBean;
import com.eayun.costcenter.service.ChangeBalanceService;
import com.eayun.obs.service.ObsCdnBucketService;
import com.eayun.obs.service.ObsUsedService;
import com.eayun.order.service.OrderService;
import com.eayun.price.bean.ParamBean;
import com.eayun.price.bean.PriceDetails;
import com.eayun.price.service.BillingFactorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * 对象存储执行扣费的扣费Service
 *
 * @Filename: ObsChargeServiceImpl.java
 * @Description:
 * @Version: 1.0
 * @Author: zhangfan
 * @Email: fan.zhang@eayun.com
 * @History:<br> <li>Date: 2016年8月10日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
@Service
@Transactional
public class ObsChargeServiceImpl implements ObsChargeService {

    private static final Logger log = LoggerFactory.getLogger(ObsChargeServiceImpl.class);

    @Autowired
    private JedisUtil jedisUtil;

    @Autowired
    private BillingFactorService billingFactorService;

    @Autowired
    private ObsUsedService obsUsedService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ChangeBalanceService changeBalanceService;

    @Autowired
    private ObsCdnBucketService obsCdnBucketService;

    @Override
    public BigDecimal doCharge(ObsStatsBean obsStats) throws Exception{
        String dcId = obsStats.getDatacenterId();
        String cusId = obsStats.getCusId();
        Date chargeFrom = obsStats.getChargeFrom();
        Date chargeTo = obsStats.getChargeTo();
        Date cycleStart = get1stDateOfCurrentMonth();//本月第一天凌晨，即计费周期开始
        log.info("客户["+cusId+"]的对象存储服务计费开始");

        //1.获取开始计费时间到本次计费时间的存储容量
        double storage = obsUsedService.getObsStorage(cusId, chargeFrom, chargeTo);

        //2.获取计费周期开始到开始计费时间的下载流量和请求数、计费周期开始到本次计费时间的下载流量和请求数
        Map<String, Object> fromMap = obsUsedService.getObsUsed(cusId, cycleStart, chargeFrom);
//        double lastDownload = (double) fromMap.get("download");
        long lastOps = (long) fromMap.get("requestCount");
        Map<String, Object> toMap = obsUsedService.getObsUsed(cusId, cycleStart, chargeTo);
//        double thisDownload = (double) toMap.get("download");
        long thisOps = (long) toMap.get("requestCount");

        //由彬彬提供接口直接查出月初到上一个整点和月初到这个整点的纯下载流量（即扣除了回源流量）
        double lastDownload = obsCdnBucketService.getBacksourceByCusId(cusId, cycleStart, chargeFrom);
        double thisDownload = obsCdnBucketService.getBacksourceByCusId(cusId, cycleStart, chargeTo);
//        log.info("客户ID"+cusId+"扣除回源流量后的下载流量lastDownload和thisDownload分别是："+"["+lastDownload+"]"+"["+thisDownload+"]");

        //todo 获取CDN下载流量、动态请求数和HTTPS请求数
        Map<String, Object> cdnDetailMap = obsCdnBucketService.getCdnDetail(cusId, chargeFrom, chargeTo);
        long cdnDownload = 0L;//B
        long cdnDreqs = 0L;//次
        long cdnHreqs = 0L;//次
        if(cdnDetailMap.containsKey("cdnDownload")){
            cdnDownload = (long) cdnDetailMap.get("cdnDownload");
        }
        if(cdnDetailMap.containsKey("cdnDreqs")){
            cdnDreqs = (long) cdnDetailMap.get("cdnDreqs");
        }
        if(cdnDetailMap.containsKey("cdnHreqs")){
            cdnHreqs = (long) cdnDetailMap.get("cdnHreqs");
        }

        //3.获取计费金额
        ParamBean pb = new ParamBean();
        pb.setDcId(dcId);
        pb.setPayType(PayType.PAYAFTER);
        pb.setNumber(1);
        pb.setCycleCount(1);//每小时统计一次，则我们认为计费时长就是1h，暂时不考虑漏跑的情况
        pb.setSpaceCapacity(storage);
        pb.setDownValue(new Double[]{lastDownload, thisDownload});
        pb.setRequestCount(new Long[]{lastOps, thisOps});
        pb.setCdnDownloadFlow(cdnDownload);
        pb.setDreqsCount(cdnDreqs);
        pb.setHreqsCount(cdnHreqs);
        PriceDetails priceDetails = billingFactorService.getPriceDetails(pb);

        //4.组织本次扣费的MoneyRecord，调用接口，生成交易记录
        RecordBean rb = new RecordBean();
        rb.setExchangeTime(chargeTo);
        rb.setEcscRemark("消费-对象存储-按需付费");
        rb.setEcmcRemark("消费-对象存储-按需付费");
        rb.setExchangeMoney(priceDetails.getTotalPrice());
        rb.setProductName("对象存储-按需付费");
        String orderNumber = orderService.getObsOrderNumberByCusId(cusId);
        rb.setOrderNo(orderNumber);
        rb.setPayType(PayType.PAYAFTER);
        rb.setIncomeType("2");
        rb.setMonStart(chargeFrom);
        rb.setMonEnd(chargeTo);
        rb.setParamBean(pb);
        rb.setPriceDetails(priceDetails);
        rb.setCusId(cusId);
        rb.setResourceType(ResourceType.OBS);
        rb.setOperType(TransType.EXPEND);
        rb.setDcId(dcId);

        changeBalanceService.changeBalanceByCharge(rb);
        log.info("客户["+cusId+"]的对象存储服务计费结束");
        return priceDetails.getTotalPrice();
    }

    private Date get1stDateOfCurrentMonth(){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH,1);
        c.set(Calendar.HOUR_OF_DAY,0);
        c.set(Calendar.MINUTE,0);
        c.set(Calendar.SECOND,0);
        c.set(Calendar.MILLISECOND,0);
        return c.getTime();
    }
}
