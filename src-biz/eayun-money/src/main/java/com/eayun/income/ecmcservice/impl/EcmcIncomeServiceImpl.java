/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.income.ecmcservice.impl;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.util.DateUtil;
import com.eayun.costcenter.dao.MoneyRecordDao;
import com.eayun.income.bean.IncomeInfoResultItem;
import com.eayun.income.bean.IncomeInfoSearchParams;
import com.eayun.income.service.EcmcIncomeService;

/**
 *                       
 * @Filename: EcmcIncomeServiceImpl.java
 * @Description: 
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2017年4月13日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Service
@Transactional
public class EcmcIncomeServiceImpl implements EcmcIncomeService {

    private final static Logger logger = LoggerFactory.getLogger(EcmcIncomeServiceImpl.class);

    @Autowired
    private MoneyRecordDao      moneyRecordDao;

    public Page queryIncomeInfo(QueryMap queryMap, IncomeInfoSearchParams searchParams) {
        logger.info("查询收入信息");
        StringBuffer queryTotalSqlBuffer = new StringBuffer();
        queryTotalSqlBuffer.append("select sum(a.mon_money) as total ");
        StringBuffer columnSqlBuffer = new StringBuffer();
        columnSqlBuffer.append("select a.mon_id, a.serial_number,a.cus_id");
        columnSqlBuffer.append(",d.third_id, a.mon_time");
        columnSqlBuffer.append(", a.mon_money");
        columnSqlBuffer.append(", oper_type");
        columnSqlBuffer.append(", case");
        columnSqlBuffer.append(" when a.oper_type = '1' then '支付宝充值'");
        columnSqlBuffer.append(" when a.oper_type = '3' then '支付宝付款'");
        columnSqlBuffer.append(" when a.oper_type = '4' then 'ECMC实际充值'");
        columnSqlBuffer.append(" end as incomeTypeName");
        columnSqlBuffer.append(",a.pay_type");
        columnSqlBuffer.append(",e.cus_org,e.cus_phone");
        columnSqlBuffer.append(", case");
        columnSqlBuffer.append(" when a.oper_type = '1' or a.oper_type = '4' then '充值'");
        columnSqlBuffer.append(" else b.prod_name");
        columnSqlBuffer.append(" end as prodName");
        columnSqlBuffer.append(", b.resource_type,b.dc_id, f.dc_name");

        StringBuffer whereSqlBuffer = new StringBuffer();

        whereSqlBuffer.append(" from money_record a");
        whereSqlBuffer.append(" left join order_info b on a.order_no = b.order_no");
        whereSqlBuffer.append(" left join pay_orderrecord c on c.order_no = b.order_no");
        whereSqlBuffer.append(" left join pay_record d on d.trade_no = c.trade_no and d.pay_status='1'");
        whereSqlBuffer.append(" left join sys_selfcustomer e on a.cus_id = e.cus_id");
        whereSqlBuffer.append(" left join dc_datacenter f on f.id = b.dc_id");
        whereSqlBuffer.append(" where (");
        //订单扣费
        whereSqlBuffer.append("(a.pay_type='1' and a.income_type='2' and d.pay_type ='1' and d.pay_status='1')");
        //支付宝充值
        whereSqlBuffer.append(" or (a.income_type ='1' and oper_type ='1')");
        //ecmc实际充值
        whereSqlBuffer.append(" or a.mon_ecmcremark like '%实际充值')");
        List<Object> params = new ArrayList<Object>();

        if (StringUtils.isNotEmpty(searchParams.getThirdId())) {
            whereSqlBuffer.append(" and d.third_id like ? escape '/'");
            params.add("%" + escapeSpecialChar(searchParams.getThirdId()) + "%");
        }

        if (StringUtils.isNotEmpty(searchParams.getCusName())) {
            whereSqlBuffer.append(" and e.cus_org like ? escape '/'");
            params.add("%" + escapeSpecialChar(searchParams.getCusName()) + "%");
        }

        if (StringUtils.isNotEmpty(searchParams.getCusPhone())) {
            whereSqlBuffer.append(" and e.cus_phone like ? escape '/'");
            params.add("%" + escapeSpecialChar(searchParams.getCusPhone()) + "%");
        }

        if (StringUtils.isNotEmpty(searchParams.getProdName())) {
            whereSqlBuffer.append(" and b.prod_name like ? escape '/'");
            params.add("%" + escapeSpecialChar(searchParams.getProdName()) + "%");
        }

        if (StringUtils.isNotEmpty(searchParams.getSerialNumber())) {
            whereSqlBuffer.append(" and a.serial_number like ? escape '/'");
            params.add("%" + escapeSpecialChar(searchParams.getSerialNumber()) + "%");
        }

        if (StringUtils.isNotEmpty(searchParams.getStartTime())) {
            whereSqlBuffer.append(" and a.mon_time >= ? ");
            params.add(DateUtil.timestampToDate(searchParams.getStartTime()));
        }

        if (StringUtils.isNotEmpty(searchParams.getEndTime())) {
            whereSqlBuffer.append(" and a.mon_time <= ? ");
            params.add(DateUtil.addDay(DateUtil.timestampToDate(searchParams.getEndTime()), new int[] { 0, 0, 1 }));
        }

        if (StringUtils.isNotEmpty(searchParams.getAmountMin())) {
            whereSqlBuffer.append(" and a.mon_money >= ? ");
            params.add(searchParams.getAmountMin());
        }

        if (StringUtils.isNotEmpty(searchParams.getAmountMax())) {
            whereSqlBuffer.append(" and a.mon_money <= ? ");
            params.add(searchParams.getAmountMax());
        }
        StringBuffer sortSqlBuffer = new StringBuffer();
        if (StringUtils.isNotEmpty(searchParams.getSortBy()) && StringUtils.isNotEmpty(searchParams.getSort())) {
            sortSqlBuffer.append(" order by ");
            //按什么排序
            if ("time".equals(searchParams.getSortBy())) {
                sortSqlBuffer.append("a.mon_time");
            } else if ("amount".equals(searchParams.getSortBy())) {
                sortSqlBuffer.append("a.mon_money");
            } else {
                sortSqlBuffer.append("a.mon_time");
            }
            //排序方式
            if ("ASC".equals(searchParams.getSort())) {
                sortSqlBuffer.append(" asc");
            } else {
                sortSqlBuffer.append(" desc");
            }
        }else {
            sortSqlBuffer.append(" order by a.mon_time desc");
        }
        Page page = moneyRecordDao.pagedNativeQuery(columnSqlBuffer.append(whereSqlBuffer).append(sortSqlBuffer).toString(), queryMap, params.toArray());
        Object totalAmount = moneyRecordDao.createSQLNativeQuery(queryTotalSqlBuffer.append(whereSqlBuffer).toString(), params.toArray()).getSingleResult();

        @SuppressWarnings("unchecked")
        List<Object[]> queryList = (ArrayList<Object[]>) page.getResult();
        List<IncomeInfoResultItem> resultList = new ArrayList<IncomeInfoResultItem>();
        if (queryList != null && queryList.size() > 0) {
            for (Object[] objects : queryList) {
                IncomeInfoResultItem item = new IncomeInfoResultItem();
                item.setMonId(String.valueOf(objects[0]));
                item.setSerialNumber(String.valueOf(objects[1]));
                item.setCusId(String.valueOf(objects[2]));
                item.setThirdId(objects[3] == null ? null : String.valueOf(objects[3]));
                item.setPaidTime((Date) objects[4]);
                item.setAmount((BigDecimal)objects[5]);
                item.setIncomeType(String.valueOf(objects[6]));
                item.setIncomeTypeName(String.valueOf(objects[7]));
                item.setPayType(objects[8] == null ? null : String.valueOf(objects[8]));
                item.setCusName(String.valueOf(objects[9]));
                item.setCusPhone(String.valueOf(objects[10]));
                item.setProdName(String.valueOf(objects[11]));
                item.setResourceType(objects[12] == null ? null : String.valueOf(objects[12]));
                item.setDcName(objects[13] == null ? null :String.valueOf(objects[13]));
                resultList.add(item);
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("resultList", resultList);
        data.put("totalAmount", totalAmount);
        page.setResult(data);
        return page;
    }

    public void exportIncomeInfoExcel(OutputStream out, IncomeInfoSearchParams searchParams) {

    }

    private String escapeSpecialChar(String str) {
        if (StringUtils.isNotBlank(str)) {
            String[] specialChars = { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|", "%" };
            for (String key : specialChars) {
                if (str.contains(key)) {
                    str = str.replace(key, "/" + key);
                }
            }
        }
        return str;
    }

}
