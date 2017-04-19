package com.eayun.costcenter.controller;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.tools.ExportDataToExcel;
import com.eayun.common.util.DateUtil;
import com.eayun.costcenter.bean.ExcelRecord;
import com.eayun.costcenter.model.MoneyAccount;
import com.eayun.costcenter.service.AccountOverviewService;

/**
 * 费用中心-账户总览Controller
 * @author xiangyu.cao@eayun.com
 *
 */
@Controller
@RequestMapping("/costcenter/accountoverview")
public class AccountOverviewController extends BaseController{
	private static final Logger log = LoggerFactory.getLogger(AccountOverviewController.class);
	@Autowired
	private AccountOverviewService accountOverviewSerivce;
	@ResponseBody
	@RequestMapping("/getaccountbalance")
	public String getAcckListPage(HttpServletRequest request) throws Exception{
		log.info("开始获取账户余额");
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		String cusId=sessionUser.getCusId();
		EayunResponseJson json = new EayunResponseJson();
		try {
			MoneyAccount accountMoney=accountOverviewSerivce.getAccountBalance(cusId);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			json.setData(accountMoney);
		} catch (Exception e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
        	json.setMessage(e.toString());
        	throw e;
		}
	
		return JSONObject.toJSONString(json);
	}
	@ResponseBody
	@RequestMapping("/getrecordlist")
	public String getRecordList(HttpServletRequest request,Page page, @RequestBody ParamsMap map) throws Exception{
		log.info("开始获取交易记录");
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		String cusId=sessionUser.getCusId();
		String begin=map.getParams().get("beginTime")==null?null:map.getParams().get("beginTime").toString();
		String end=map.getParams().get("endTime")==null?null:map.getParams().get("endTime").toString();
		Date beginTime = begin!=null&&begin.length()>0?DateUtil.timestampToDate(begin):null;
	    Date endTime = end!=null&&end.length()>0?DateUtil.timestampToDate(end):null;
	    String incomeType=map.getParams().get("incomeType")==null?null:map.getParams().get("incomeType").toString();
	    int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
		QueryMap queryMap=new QueryMap();
		queryMap.setPageNum(pageNumber);
		queryMap.setCURRENT_ROWS_SIZE(pageSize);
		page=accountOverviewSerivce.getRecordPage(page, cusId, beginTime, endTime,incomeType, queryMap);
		return JSONObject.toJSONString(page);
	}
	/**
     * 导出excel
     */
    @SuppressWarnings("unused")
    @RequestMapping("/createrecordexcel")
    public String createRecordExcel(HttpServletRequest request, HttpServletResponse response , String beginTime, 
                              String endTime ,String incomeType , String browser) throws Exception {
        log.info("导出Excel开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        Date begin = beginTime==null?null:DateUtil.timestampToDate(beginTime);
        Date end = endTime==null?null:DateUtil.timestampToDate(endTime);
        Properties props=System.getProperties();
        String os = props.getProperty("os.name").toLowerCase();
        try {
            List<ExcelRecord> list = accountOverviewSerivce.queryRecordExcel(incomeType,begin, end,cusId, false);
            if(list==null||list.size()<=0){
            	list=new ArrayList<ExcelRecord>();
            	list.add(new ExcelRecord());
            }
            ExportDataToExcel<ExcelRecord> excel = new ExportDataToExcel<ExcelRecord>();
            response.setContentType("application/vnd.ms-excel");
            
            String fileName = "";
            SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
            String now=sdf.format(new Date());
            String name="交易记录_"+now+".xls";
            if("Firefox".equals(browser)){
                fileName = new String(name.getBytes(), "iso-8859-1");
            }else{
                fileName = URLEncoder.encode(name, "UTF-8") ;
            }
            response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
            excel.exportData(list, response.getOutputStream(), "易云公有云账户交易记录");
        } catch (Exception e) {
            log.error("导出交易记录excel失败", e);
            throw e;
        }
        return null;
    }
}
