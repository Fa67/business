/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.pay.service.impl;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.druid.support.json.JSONUtils;
import com.alipay.config.AlipayConfig;
import com.alipay.util.AlipaySubmit;
import com.alipay.util.AlipayXmlUtil;
import com.eayun.common.exception.AppException;
import com.eayun.common.util.StringUtil;
import com.eayun.pay.model.AlipayConstants;
import com.eayun.pay.model.AlipayConstants.ParamKeys;
import com.eayun.pay.model.AlipayConstants.TradeStatus;
import com.eayun.pay.model.AlipayConstants.XMLKeys;
import com.eayun.pay.model.AlipayConstants.XMLResult;
import com.eayun.pay.model.PayRecord.PayStatus;
import com.eayun.pay.model.TradeResult;
import com.eayun.pay.service.AlipayPaymentService;
import com.eayun.pay.service.EayunPaymentService;

/**
 *                       
 * @Filename: AlipayPaymentService.java
 * @Description: 
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2016年7月28日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Service
@Transactional
public class AlipayPaymentServiceImpl implements AlipayPaymentService {

    private final static Logger logger = LoggerFactory.getLogger(AlipayPaymentServiceImpl.class);

    @Autowired
    private EayunPaymentService eayunPaymentService;

    @Value(value = "#{prop.alipay_trade_notify_url}")
    private String              tradeNotifyUrl;

    @Value(value = "#{prop.alipay_trade_return_url}")
    private String              tradeReturnUrl;

    public String createDirectPayForm(String outTradeNo, BigDecimal amount, String prodName, String prodDesc) throws AppException {
        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = outTradeNo;
        //订单名称，必填
        String subject = StringUtil.filterBlankChars(prodName);
        //付款金额，必填
        String total_fee = new DecimalFormat("0.00").format(amount);
        //商品描述，可空
        String body = StringUtil.filterBlankChars(prodDesc);

        //把请求参数打包成数组
        Map<String, String> sParaTemp = new HashMap<String, String>();
        sParaTemp.put("service", AlipayConstants.Service.CREATE_DIRECT_PAY_BY_USER);
        sParaTemp.put("partner", AlipayConfig.partner);
        sParaTemp.put("seller_id", AlipayConfig.seller_id);
        sParaTemp.put("_input_charset", AlipayConfig.input_charset);
        sParaTemp.put("payment_type", AlipayConfig.payment_type);
        sParaTemp.put("notify_url", tradeNotifyUrl); //异步通知url
        sParaTemp.put("return_url", tradeReturnUrl); //同步通知url
        sParaTemp.put("out_trade_no", out_trade_no); //交易流水号
        sParaTemp.put("subject", subject);
        sParaTemp.put("total_fee", total_fee);
        sParaTemp.put("body", body);
        //返回支付表单
        return AlipaySubmit.buildRequest(sParaTemp, "POST", "确认");
    }

    public void processTradeNotify(Map<String, String> params) throws AppException {
        String outTradeNo = params.get(ParamKeys.OUT_TRADE_NO);
        //支付宝流水号
        String thirdId = params.get(ParamKeys.TRADE_NO);
        //交易状态
        String tradeStatus = params.get(ParamKeys.TRADE_STATUS);
        //该笔交易的买家付款时间。格式为 yyyy-MM-dd HH:mm:ss
        //String gmtPayment = params.get(ParamKeys.GMT_PAYMENT);
        //String totalFee = params.get(ParamKeys.TOTAL_FEE);

        logger.info("received an alipay trade notify: outTradeNo[{}], thirdId[{}], tradeStauts[{}]", outTradeNo, thirdId, tradeStatus);

        if (TradeStatus.TRADE_FINISHED.equals(tradeStatus)) {
            logger.info("alipay trade notify:trade_finished->outTradeNo[{}], thirdId[{}]", outTradeNo, thirdId);
            //判断该笔订单是否在商户网站中已经做过处理
            //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
            //如果有做过处理，不执行商户的业务程序
            //XXX trade finished deal

            //注意：
            //退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
        } else if (TradeStatus.TRADE_SUCCESS.equals(tradeStatus)) {
            logger.info("alipay notify:trade_success-> outTradeNo[{}], thirdId[{}]", outTradeNo, thirdId);
            eayunPaymentService.confirmPaid(outTradeNo, thirdId, true, JSONUtils.toJSONString(params));
        } else {
            logger.info("unknown alipay notify:outTradeNo[{}], thirdId[{}], tradeStauts[{}}", outTradeNo, thirdId, tradeStatus);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public TradeResult singleTradeQuery(String ourTradeNo, String alipayTradeNo) {
        if (StringUtils.isBlank(ourTradeNo) && StringUtils.isBlank(alipayTradeNo)) {
            throw new IllegalArgumentException("支付流水号和支付宝流水号不能同时为空");
        }

        //把请求参数打包成数组
        Map<String, String> sParaTemp = new HashMap<String, String>();
        sParaTemp.put(ParamKeys.SERVICE, AlipayConstants.Service.SINGLE_TRADE_QUERY);
        sParaTemp.put(ParamKeys.PARTNER, AlipayConfig.partner);
        sParaTemp.put(ParamKeys._INPUT_CHARSET, AlipayConfig.input_charset);
        sParaTemp.put(ParamKeys.TRADE_NO, alipayTradeNo);
        sParaTemp.put(ParamKeys.OUT_TRADE_NO, ourTradeNo);
        TradeResult tradeResult = new TradeResult();
        try {
            String sHtmlText = AlipaySubmit.buildRequest("", "", sParaTemp);
            if (StringUtils.isNotBlank(sHtmlText)) {
                logger.info("query alipay single trade,result content:{}", sHtmlText);
                Map<String, Object> resultMap = AlipayXmlUtil.xml2Map(sHtmlText);
                //获取alipay标签下的所有数据
                Map<String, Object> contentMap = (Map<String, Object>) resultMap.get(XMLKeys.ALIPAY);
                //请求是否成功
                String isSuccess = (String) contentMap.get(XMLKeys.IS_SUCCESS);
                tradeResult.setQuerySuccess(XMLResult.T.equals(isSuccess));
                if (XMLResult.T.equals(isSuccess)) {
                    //response 的业务数据
                    Map<String, String> paramsMap = (Map<String, String>) ((Map) contentMap.get(XMLKeys.RESPONSE)).get(XMLKeys.TRADE);
                    //交易状态
                    String tradeStatus = paramsMap.get(ParamKeys.TRADE_STATUS);
                    if (TradeStatus.WAIT_BUYER_PAY.equals(tradeStatus)) {
                        tradeResult.setPayStatus(PayStatus.ON_PAY);
                    } else if (isPaidSuccess(isSuccess, tradeStatus)) {
                        tradeResult.setPayStatus(PayStatus.SUCCESS);
                    } else {
                        tradeResult.setPayStatus(PayStatus.FAIL);
                    }
                    tradeResult.setPayId(paramsMap.get(ParamKeys.OUT_TRADE_NO));
                    tradeResult.setThirdId(paramsMap.get(ParamKeys.TRADE_NO));
                    tradeResult.setThirdResult(JSONUtils.toJSONString(paramsMap));
                }
            }
        } catch (Exception e) {
            logger.error("query alipay single trade failed", e);
            if (StringUtils.isEmpty(tradeResult.getPayStatus())) {
                tradeResult.setPayStatus(PayStatus.ON_PAY);
            }
        }
        return tradeResult;
    }

    protected boolean isPaidSuccess(String isSuccess, String tradeStatus) {
        return XMLResult.T.equals(isSuccess) && (TradeStatus.TRADE_SUCCESS.equals(tradeStatus) || TradeStatus.TRADE_FINISHED.equals(tradeStatus));
    }
}
