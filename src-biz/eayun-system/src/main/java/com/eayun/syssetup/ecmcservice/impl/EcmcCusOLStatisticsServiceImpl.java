package com.eayun.syssetup.ecmcservice.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.model.IpInfo;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.service.IpService;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.SerializeUtil;
import com.eayun.syssetup.bean.CusOLExcel;
import com.eayun.syssetup.bean.CusOLStatistics;
import com.eayun.syssetup.ecmcservice.EcmcCusOLStatisticsService;

@Transactional
@Service
public class EcmcCusOLStatisticsServiceImpl implements
		EcmcCusOLStatisticsService {

	private static final Logger log = LoggerFactory.getLogger(EcmcCusOLStatisticsServiceImpl.class);
	
	private static List<CusOLStatistics> cusOLForExcelList = new ArrayList<CusOLStatistics>();
	
	private static Date queryTime = new Date();
	
	@Autowired
	private JedisUtil jedisUtil;
	
	@Autowired
	private IpService ipService;
	
	@Override
	public Page getCusOLPage(Page page, QueryMap queryMap) {
		List<CusOLStatistics> dataList = new ArrayList<CusOLStatistics>();
		List<CusOLStatistics> resultList = new ArrayList<CusOLStatistics>();
		dataList = getDataList();
		
		Collections.sort(dataList,new Comparator<CusOLStatistics>(){
            public int compare(CusOLStatistics arg0, CusOLStatistics arg1) {
            	Date value0 = new Date();
            	Date value1 = new Date();
            	
            	value0 = arg0.getLastOpTime();
            	value1 = arg1.getLastOpTime();
            	int result = 0;
            	result = value1.compareTo(value0);
                return result;
            }
        });
		
        int pageSize = queryMap.getCURRENT_ROWS_SIZE();
        int pageNumber = queryMap.getPageNum();
        
        int start = (pageNumber-1)*pageSize;
        if(dataList.size()>0){
            int end = start+pageSize;
            resultList = dataList.subList(start, end < dataList.size()?end:dataList.size());
        }
        page = new Page(start, dataList.size(), pageSize, resultList);
		return page;
	}

	@Override
	public JSONObject getOLCusAmount() {
		JSONObject json = new JSONObject();
		List<CusOLStatistics> dataList = getDataList();
		Set<String> ips = new HashSet<String>();
		for(CusOLStatistics cusOL : dataList){
			String ip = cusOL.getIp();
			ips.add(ip);
		}
		int total = dataList.size();
		json.put("total", total);
		json.put("independent", ips.size());
		return json;
	}

	private List<CusOLStatistics> getDataList(){
		List<CusOLStatistics> dataList = new ArrayList<CusOLStatistics>();
		queryTime = new Date();
		try {
			Set<String> sets = jedisUtil.keys(RedisKey.ECSC_SESSIONID+"*");
			for (String s : sets) {
				byte[] data = jedisUtil.getBytes(s);
				Map<String, Object> attributes = SerializeUtil.unserialize(data);
				if(attributes.isEmpty()){
					log.info(s+" is empty!!!");
					continue;
				}
				SessionUserInfo sessionUserInfo = (SessionUserInfo) attributes.get(ConstantClazz.SYS_SESSION_USERINFO);
				if(null != sessionUserInfo){
					String str = (String) attributes.get("login");
					JSONObject json = JSONObject.parseObject(str);
					Long time = (Long) json.get("startTime");
					Timestamp ts = new Timestamp(time);
					Date startTime =ts;
					
					Long ttl = jedisUtil.getExpire(s);
					int seconds = Integer.valueOf(String.valueOf(ttl));
					Date now = new Date();
					Date lastDate = DateUtil.addDay(now, new int[]{0,0,0,0,0,seconds-1800});
					
					CusOLStatistics cusOL = new CusOLStatistics();
					cusOL.setCusId(sessionUserInfo.getCusId());
					cusOL.setCusAccount(sessionUserInfo.getUserName());
					cusOL.setCusName(sessionUserInfo.getCusOrg());
					cusOL.setLoginTime(startTime);
					cusOL.setLastOpTime(lastDate);
					cusOL.setIp(sessionUserInfo.getIP());
					IpInfo info = ipService.getIp(sessionUserInfo.getIP());
					cusOL.setIpInfo(info);
					String addr = "";
					if(info != null){
					    addr = info.getCountry();
					    if(null != info.getArea() && !"".equals(info.getArea())){
	                        addr = addr+"-"+info.getArea();
	                    }
	                    if(null != info.getRegion() && !"".equals(info.getRegion())){
	                        addr = addr+"-"+info.getRegion();
	                    }
	                    if(null != info.getCity() && !"".equals(info.getCity())){
	                        addr = addr+"-"+info.getCity();
	                    }
	                    if(null != info.getCounty() && !"".equals(info.getCounty())){
	                        addr = addr+"-"+info.getCounty();
	                    }
					}
					cusOL.setLoginAddr(addr);
					
					dataList.add(cusOL);
				}else{
					log.info(s+" The SessionUserInfo is empty!!!");
				}
			}
		} catch (Exception e) {
			log.error(e.toString(),e);
		}
		cusOLForExcelList = dataList;
		return dataList;
	}

	@Override
	public JSONObject getExcelDataList() {
		JSONObject result = new JSONObject();
		List<CusOLExcel> list = new ArrayList<CusOLExcel>();
		for(CusOLStatistics cusOL : cusOLForExcelList){
			CusOLExcel excel = new CusOLExcel();
			excel.setCusAccount(cusOL.getCusAccount());
			excel.setCusName(cusOL.getCusName());
			excel.setIp(cusOL.getIp());
			excel.setLoginTime(DateUtil.dateToString(cusOL.getLoginTime()));
			excel.setLastOpTime(DateUtil.dateToString(cusOL.getLastOpTime()));
			excel.setLoginAddr(cusOL.getLoginAddr());
			list.add(excel);
		}
		String time = DateUtil.dateToString(queryTime);
		time = time.replace("-", "");
		time = time.replace(":", "");
		time = time.replace(" ", "");
		result.put("list", list);
		result.put("time", time);
		return result;
	}
}
