package com.eayun.syssetup.ecmccontroller;

import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.tools.ExportDataToExcel;
import com.eayun.common.util.DateUtil;
import com.eayun.syssetup.bean.CusOLExcel;
import com.eayun.syssetup.ecmcservice.EcmcCusOLStatisticsService;

@Controller
@RequestMapping("/ecmc/syssetup/ol")
@Scope("prototype")
public class EcmcCusOLStatisticsController {

	private static final Logger log = LoggerFactory.getLogger(EcmcCusOLStatisticsController.class);

    @Autowired
    private EcmcCusOLStatisticsService ecmcCusOLStatisticsService;

    @RequestMapping(value="/getpagedolcuslist", method = RequestMethod.POST)
    @ResponseBody
    public String getPagedOLCusList(HttpServletRequest request, Page page, @RequestBody ParamsMap map){
    	log.info("查询在线用户列表");
    	int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        page=ecmcCusOLStatisticsService.getCusOLPage(page, queryMap);
        return JSONObject.toJSONString(page);
    }
    
    @RequestMapping(value="/getolcusamount", method = RequestMethod.POST)
    @ResponseBody
    public String getOLCusAmount(HttpServletRequest request){
    	log.info("查询在线用户人数");
    	EayunResponseJson json = new EayunResponseJson();
    	JSONObject result;
		try {
			result = ecmcCusOLStatisticsService.getOLCusAmount();
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			json.setData(result);
		} catch (Exception e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			json.setMessage(e.toString());
			log.error(e.toString(),e);
		}
        return JSONObject.toJSONString(json);
    }
    
    @RequestMapping("/exportolstatistics2excel")
    public String exportOLStatistics2Excel(HttpServletRequest request, HttpServletResponse response ,String browser) throws Exception {
        log.info("导出在线用户数据");
        try {
        	JSONObject result = ecmcCusOLStatisticsService.getExcelDataList();
            List<CusOLExcel> list = (List<CusOLExcel>) result.get("list");
            String time = result.getString("time");
            ExportDataToExcel<CusOLExcel> excel = new ExportDataToExcel<CusOLExcel>();
            response.setContentType("application/vnd.ms-excel");
            String name = "在线统计_"+time+".xls";
            String fileName = "";
            if("Firefox".equals(browser)){
                fileName = new String(name.getBytes(), "iso-8859-1");
            }else{
                fileName = URLEncoder.encode(name, "UTF-8") ;
            }
            response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
            if(list.isEmpty()){
            	CusOLExcel cusOlExcel = new CusOLExcel();
            	list.add(cusOlExcel);
            }
            excel.exportData(list, response.getOutputStream(), "在线统计"+time);
        } catch (Exception e) {
            log.error("导出在线用户数据excel失败", e);
            throw e;
        }
        return null;
    }
}
