package com.eayun.obs.ecmcservice.impl;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.RedisNodeIdConstant;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.ObsUtil;
import com.eayun.costcenter.service.CostReportService;
import com.eayun.obs.dao.CdnBucketDao;
import com.eayun.obs.ecmcmodel.EcmcObsEchartsBean;
import com.eayun.obs.ecmcservice.EcmcObsCdnService;
import com.eayun.obs.model.BaseCdnBucket;
import com.eayun.obs.model.CdnBucket;
import com.eayun.obs.model.ObsUsedType;

@Service
@Scope("prototype")
@Transactional
public class EcmcObsCdnServiceImpl implements EcmcObsCdnService {

	private static final Logger log = LoggerFactory.getLogger(EcmcObsCdnServiceImpl.class);
	
	@Autowired
	private CdnBucketDao cdnBucketDao;
	
	@Autowired
    private MongoTemplate mongoTemplate;
	
	@Autowired
    private JedisUtil jedisUtil;
	
	@Autowired
	private CostReportService costReportService;
	
	private long kb = 1024;
	private long mb = kb * 1024;
	private long gb = mb * 1024;
	private long tb = gb * 1024;
	
	/**
	 * 所有曾经开通过CDN的客户列表
	 * @Author: duanbinbin
	 * @return
	 *<li>Date: 2016年6月30日</li>
	 */
	@SuppressWarnings("rawtypes")
    @Override
	public List<CdnBucket> getObsCdnCusList() {
		String sql = "SELECT cdn.cus_id,cus.cus_cpname,cus.cus_org FROM	cdn_bucket cdn "
				+ "LEFT JOIN sys_selfcustomer cus ON cdn.cus_id = cus.cus_id "
				+ "GROUP BY cdn.cus_id";
		javax.persistence.Query query = cdnBucketDao.createSQLNativeQuery(sql, null);
		List<CdnBucket> list = new ArrayList<CdnBucket>();
		List resultList = query.getResultList();
		for (int i = 0; i < resultList.size(); i++) {
			Object[] obj = (Object[]) resultList.get(i);
			CdnBucket cdn = new CdnBucket();
			cdn.setCusId(String.valueOf(obj[0]));
			cdn.setCusCpname(String.valueOf(obj[1]));
			cdn.setCusOrg(String.valueOf(obj[2]));
			list.add(cdn);
		}
		return list;
	}

	/**
	 * 客户本月内曾使用过的加速域名列表
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
	public Page getMonthDomainList(Page page, QueryMap queryMap, String cusId) {
		log.info("查询客户本月内使用过的加速域名列表");
		List<Object> list = new ArrayList<Object>();
		
		String path = getCdnUrlByNodeID(RedisNodeIdConstant.CDN_ACCELERATE_ADDRESS);
		
		String host = ObsUtil.getEayunObsHost();//源地址
		
		Calendar c = Calendar.getInstance();    
        c.add(Calendar.MONTH, 0);
        c.set(Calendar.DAY_OF_MONTH,1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        Date firstDay = c.getTime();
        
		StringBuffer hql = new StringBuffer("from BaseCdnBucket where cusId = ? and (isOpencdn = '1' or (isOpencdn = '0' and closeTime > ?))");
		list.add(cusId);
		list.add(firstDay);
		
		page = cdnBucketDao.pagedQuery(hql.toString(), queryMap, list.toArray());
		List<BaseCdnBucket> baseCdnBucketList = (List) page.getResult();
		//List<BaseCdnBucket> baseCdnBucketList = cdnBucketDao.find(hql.toString(), list.toArray());
		for(int i = 0; i < baseCdnBucketList.size();i++){
			CdnBucket cdnBucket = new CdnBucket();
			BaseCdnBucket baseCdn = baseCdnBucketList.get(i);
			BeanUtils.copyPropertiesByModel(cdnBucket, baseCdn);
			cdnBucket.setEosPath(cdnBucket.getBucketName()+"."+host);
			cdnBucket.setBucketPath(cdnBucket.getBucketName()+"."+path);
			
			if("1".equals(cdnBucket.getIsDelete())){
				String showPath = cdnBucket.getBucketPath()+"  ("+DateUtil.dateToString(cdnBucket.getDeleteTime())+"  已删除)";
				cdnBucket.setBucketPath(showPath);
			}
			
			String domainId = cdnBucket.getDomainId();
			cdnBucket = this.getMonthFlowByDomain(domainId,cdnBucket);
//			String cdnFlowStr = this.cdnFlowUnit(cdnFlow);
			
			long backData = this.getMonthBackByDomain(domainId);
			String backStr = this.cdnFlowUnit(backData);
			cdnBucket.setBacksource(backData);
			cdnBucket.setBacksourceStr(backStr);
			
//			cdnBucket.setCdnFlow(cdnFlow);
//			cdnBucket.setCdnFlowStr(cdnFlowStr);
			baseCdnBucketList.set(i, cdnBucket);
		}
		return page;
	}
	/**
	 * 查询加速域名本月内使用的CDN流量
	 * @Author: duanbinbin
	 * @param domainId
	 * @return
	 *<li>Date: 2016年7月1日</li>
	 */
	private CdnBucket getMonthFlowByDomain(String domainId,CdnBucket cdnBucket){
		Date now = new Date();
		Calendar c = Calendar.getInstance();    
        c.add(Calendar.MONTH, 0);
        c.set(Calendar.DAY_OF_MONTH,1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        Date firstDay = c.getTime();
        
        /**包括当天数据时：gt.lte."obs.cdn.1h"	不包含当天数据时：gte.lte."obs.cdn.1d"*/
        Aggregation agg = Aggregation.newAggregation(
        		Aggregation.match(Criteria.where("domain_id").is(domainId)),
		        Aggregation.match(Criteria.where("timestamp").gt(firstDay)),
		        Aggregation.match(Criteria.where("timestamp").lte(now))
		        );
		AggregationResults<JSONObject> totalresult = mongoTemplate.aggregate(agg,MongoCollectionName.OBS_CDN_1H, JSONObject.class);
		List<JSONObject> totallist = totalresult.getMappedResults();
		long cdnFlowData = 0;
		long cdnHreqs=0;
		long cdnDreqs=0;
		if(totallist.size() > 0){
			for (JSONObject jsonObject : totallist) {
				cdnFlowData += jsonObject.getLongValue("flow_data");
				cdnHreqs+=jsonObject.getLongValue("hreqs");
				cdnDreqs+=jsonObject.getLongValue("dreqs");
			}
//			JSONObject obj = totallist.get(0);
//			cdnFlowData = obj.getLongValue("sum");
		}
		cdnBucket.setCdnFlow(cdnFlowData);
		cdnBucket.setCdnFlowStr(this.cdnFlowUnit(cdnFlowData));
		cdnBucket.setCdnHreqs(cdnHreqs);
		cdnBucket.setCdnHreqsStr(this.convertRequestCountUnit(cdnHreqs));
		cdnBucket.setCdnDreqs(cdnDreqs);
		cdnBucket.setCdnDreqsStr(this.convertRequestCountUnit(cdnDreqs));
		return cdnBucket;
	}
	/**
	 * 查询某加速域名本月内使用的纯回源流量
	 * @param domainId
	 * @return
	 */
	private long getMonthBackByDomain(String domainId){
		Date now = new Date();
		Calendar c = Calendar.getInstance();    
        c.add(Calendar.MONTH, 0);
        c.set(Calendar.DAY_OF_MONTH,1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        Date firstDay = c.getTime();
        
        Aggregation agg = Aggregation.newAggregation(
        		Aggregation.match(Criteria.where("domain_id").is(domainId)),
		        Aggregation.match(Criteria.where("timestamp").gt(firstDay)),
		        Aggregation.match(Criteria.where("timestamp").lte(now)),
		        Aggregation.group().sum("backsource").as("sum")
		        );
		AggregationResults<JSONObject> totalresult = mongoTemplate.aggregate(
				agg,MongoCollectionName.CDN_BACKSOURCE_1H, JSONObject.class);
		List<JSONObject> totallist = totalresult.getMappedResults();
		long backData = 0;
		if(totallist.size() > 0){
			JSONObject obj = totallist.get(0);
			backData = obj.getLongValue("sum");
		}
		return backData;
	}

	/**
	 * 查询客户所有加速域名
	 * @Author: duanbinbin
	 * @param cusId
	 * @return
	 *<li>Date: 2016年6月30日</li>
	 */
	@SuppressWarnings("unchecked")
    @Override
	public List<CdnBucket> getAllDomainList(String cusId) {
		List<CdnBucket> cdnBucketList = new ArrayList<CdnBucket>();
		List<Object> list = new ArrayList<Object>();
		String path = getCdnUrlByNodeID(RedisNodeIdConstant.CDN_ACCELERATE_ADDRESS);
		
		String host = ObsUtil.getEayunObsHost();//源地址
        
		StringBuffer hql = new StringBuffer("from BaseCdnBucket where cusId = ? ");
		list.add(cusId);
		
		List<BaseCdnBucket> baseCdnBucketList = cdnBucketDao.find(hql.toString(), list.toArray());
		for(BaseCdnBucket baseCdn : baseCdnBucketList){
			CdnBucket cdnBucket = new CdnBucket();
			BeanUtils.copyPropertiesByModel(cdnBucket, baseCdn);
			cdnBucket.setEosPath(cdnBucket.getBucketName()+"."+host);
			cdnBucket.setBucketPath(cdnBucket.getBucketName()+"."+path);
			if("1".equals(cdnBucket.getIsDelete())){
				String showPath = cdnBucket.getBucketPath()+"  ("+DateUtil.dateToString(cdnBucket.getDeleteTime())+"  已删除)";
				cdnBucket.setBucketPath(showPath);
			}
			cdnBucketList.add(cdnBucket);
		}
		return cdnBucketList;
	}

	/**
	 * 获取 配置的CDN加速域名，如file.eayun.com
	 * @Author: duanbinbin
	 * @param nodeId
	 * @return
	 *<li>Date: 2016年6月30日</li>
	 */
	private String getCdnUrlByNodeID(String nodeId) {
        String cdnUrl = null;
        try {
            String jsonStr = jedisUtil.get("sys_data_tree:"+nodeId);
            JSONObject json = JSONObject.parseObject(jsonStr);
            
            cdnUrl = json.getString("para1");
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
        return cdnUrl;
    }
	
	/**
	 * 图表数据显示
	 * @Author: duanbinbin
	 * @param domain
	 * @param startTime
	 * @param endTime
	 * @return
	 *<li>Date: 2016年6月30日</li>
	 */
	@Override
	public EcmcObsEchartsBean getDomainData(String cusId,String domain, Date startTime,
			Date endTime , String type ,String queryType) throws Exception {
		EcmcObsEchartsBean echartsBean = new EcmcObsEchartsBean();
		List<BigDecimal> yDataList = new ArrayList<BigDecimal>();
		echartsBean = getCdnFlowOrBack(cusId, domain, startTime, endTime , type);
		
		if (null != echartsBean) {
			for (BigDecimal decimal : echartsBean.getyData()) {
				yDataList.add(decimal);
			}
			
			Collections.sort(yDataList, new Comparator<BigDecimal>() {
				@Override
				public int compare(BigDecimal o1, BigDecimal o2) {
					return o1.compareTo(o2);
				}
			});
			BigDecimal minData = yDataList.get(0);
			BigDecimal maxData = yDataList.get(yDataList.size() - 1);
			echartsBean.setOriginalDataMax(maxData.toString());
			minData = minData.multiply(BigDecimal.valueOf(0.8)).setScale(2,
					BigDecimal.ROUND_HALF_UP);
			maxData = maxData.multiply(BigDecimal.valueOf(1.2)).setScale(2,
					BigDecimal.ROUND_HALF_UP);
			echartsBean.setyDataMin(minData.toString());
			echartsBean.setyDataMax(maxData.toString());
		}
		return echartsBean;
	}
	/**
	 * 查询域名CDN下载流量或回源流量
	 * 用于曲线图显示
	 * @param cusId
	 * @param domain
	 * @param start
	 * @param end
	 * @param type
	 * @return
	 */
	private EcmcObsEchartsBean getCdnFlowOrBack(String cusId, String domain, Date start, Date end , String type) {
		DateFormat sdf = new SimpleDateFormat("MM-dd HH:mm"); 
		DateFormat sdfMonthDay = new SimpleDateFormat("MM-dd"); 
		
		String collectionName = MongoCollectionName.OBS_CDN_1H;
		String attributeName = "flow_data";
		if(type.equals("BACK")){			//查询回源
			collectionName = MongoCollectionName.CDN_BACKSOURCE_1H;
			attributeName = "backsource";
		}else if(type.equals("DREQS")){
			attributeName = "dreqs";
		}else if(type.equals("HREQS")){
			attributeName = "hreqs";
		}

		
		EcmcObsEchartsBean echartsBean = new EcmcObsEchartsBean();
		// 得到相差天数
		int value = (int) ((end.getTime() - start.getTime()) / (24 * 60 * 60 * 1000));
		List<BigDecimal> yData = new ArrayList<BigDecimal>();
		List<String> xTime = new ArrayList<String>();
		
		if (value <= 4) {
			int hourse = (value + 1) * 24 / 6;
			Date beginTime = start;
			Date endTime = DateUtil.addDay(beginTime, new int[] { 0, 0, 0,+hourse});
			for (int i = 0; i < 6; i++) {
				Criteria criatira = new Criteria();
				if("".equals(domain)){
					criatira.andOperator(
							Criteria.where("cus_id").is(cusId),
							Criteria.where("timestamp").gte(DateUtil.addDay(beginTime, new int[] { 0, 0, 0,+1 })),
							Criteria.where("timestamp").lt(DateUtil.addDay(endTime, new int[] { 0, 0, 0,+1 })));
				}else{
					criatira.andOperator(
							Criteria.where("cus_id").is(cusId),
							Criteria.where("domain_id").is(domain),
							Criteria.where("timestamp").gte(DateUtil.addDay(beginTime, new int[] { 0, 0, 0,+1 })),
							Criteria.where("timestamp").lt(DateUtil.addDay(endTime, new int[] { 0, 0, 0,+1 })));
				}
				
				List<JSONObject> jsonList = mongoTemplate.find(new Query(criatira), JSONObject.class, collectionName);
				long data = 0l;
				for (JSONObject json : jsonList) {
					long flow = 0l;
					flow = json.getLongValue(attributeName);
					data = data + flow;
				}
				if("CDN".equals(type)||"BACK".equals(type)){
					BigDecimal cdnLoad = new BigDecimal(data);
					//将下载流量字节转换为MB
					cdnLoad = cdnLoad.divide(new BigDecimal(1024 * 1024), 2,BigDecimal.ROUND_HALF_UP);
					
					String cdnLoadStr = "";
					cdnLoadStr = sdf.format(endTime);
					beginTime = endTime;
					endTime = DateUtil.addDay(endTime,new int[] { 0, 0, 0, +hourse });
					
					yData.add(cdnLoad);
					xTime.add(cdnLoadStr);
				}else{
					BigDecimal reqs = new BigDecimal(data);
					String cdnLoadStr = "";
					cdnLoadStr = sdf.format(endTime);
					beginTime = endTime;
					endTime = DateUtil.addDay(endTime,new int[] { 0, 0, 0, +hourse });
					
					yData.add(reqs);
					xTime.add(cdnLoadStr);
				}
				
				
			}
		} else {
			Date beginTime = start;
			Date endTime = DateUtil.addDay(beginTime, new int[] { 0, 0, +1 });
			for (int i = 0; i <= value; i++) {
				Criteria criatira = new Criteria();
				Sort sort = new Sort(Direction.DESC, "timestamp");
				
				if("".equals(domain)){
					criatira.andOperator(
							Criteria.where("cus_id").is(cusId),
							Criteria.where("timestamp").gte(beginTime),
							Criteria.where("timestamp").lt(endTime));
				}else{
					criatira.andOperator(
							Criteria.where("cus_id").is(cusId),
							Criteria.where("domain_id").is(domain),
							Criteria.where("timestamp").gte(beginTime),
							Criteria.where("timestamp").lt(endTime));
				}
				
				List<JSONObject> usedList = mongoTemplate.find(new Query(criatira).with(sort), JSONObject.class, collectionName);
				long load = 0l;
				for (JSONObject json : usedList) {
					long flow = 0;
					flow = json.getLongValue(attributeName);
					load = load+flow;
				}
				if("CDN".equals(type)||"BACK".equals(type)){
					//将下载流量字节转换为MB
					BigDecimal cdnLoad = new BigDecimal(load);
					cdnLoad = new BigDecimal(load).divide(new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP);
					String cdnLoadStr = "";
					cdnLoadStr = sdfMonthDay.format(beginTime);

					beginTime = endTime;
					endTime = DateUtil.addDay(endTime, new int[] { 0, 0, +1 });
					
					yData.add(cdnLoad);
					xTime.add(cdnLoadStr);
				}else{
					BigDecimal reqs = new BigDecimal(load);
					String cdnLoadStr = "";
					cdnLoadStr = sdfMonthDay.format(beginTime);

					beginTime = endTime;
					endTime = DateUtil.addDay(endTime, new int[] { 0, 0, +1 });
					
					yData.add(reqs);
					xTime.add(cdnLoadStr);
				}
				
			}
		}
		echartsBean.setyData(yData);
		echartsBean.setxTime(xTime);
		return echartsBean;
	}

	/**
	 * 查询客户本月CDN用量
	 * @Author: duanbinbin
	 * @param cusId
	 * @return
	 *<li>Date: 2016年6月30日</li>
	 */
	@Override
	public ObsUsedType getMonthDomainData(String cusId) throws Exception{
		ObsUsedType type = new ObsUsedType();
		Date now = new Date();
		Calendar c = Calendar.getInstance();    
        c.add(Calendar.MONTH, 0);
        c.set(Calendar.DAY_OF_MONTH,1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        Date firstDay = c.getTime();
        
        /**包括当天数据时：gt.lte."obs.cdn.1h"	不包含当天数据时：gte.lte."obs.cdn.1d"*/
        Aggregation agg = Aggregation.newAggregation(
        		Aggregation.match(Criteria.where("cus_id").is(cusId)),
		        Aggregation.match(Criteria.where("timestamp").gt(firstDay)),
		        Aggregation.match(Criteria.where("timestamp").lte(now))
		        );
		AggregationResults<JSONObject> totalresult = mongoTemplate.aggregate(
				agg,MongoCollectionName.OBS_CDN_1H, JSONObject.class);
		List<JSONObject> totallist = totalresult.getMappedResults();
		long cdnFlowData = 0;
		long cdnHreqs=0;
		long cdnDreqs=0;
		if(totallist!=null&&totallist.size() > 0){
			for (JSONObject jsonObject : totallist) {
				cdnFlowData += jsonObject.getLongValue("flow_data");
				cdnHreqs+=jsonObject.getLongValue("hreqs");
				cdnDreqs+=jsonObject.getLongValue("dreqs");
			}
		}
		//回源流量
		Aggregation backAgg = Aggregation.newAggregation(
        		Aggregation.match(Criteria.where("cus_id").is(cusId)),
		        Aggregation.match(Criteria.where("timestamp").gt(firstDay)),
		        Aggregation.match(Criteria.where("timestamp").lte(now)),
		        Aggregation.group().sum("backsource").as("sum")
		        );
		AggregationResults<JSONObject> backResult = mongoTemplate.aggregate(
				backAgg,MongoCollectionName.CDN_BACKSOURCE_1H, JSONObject.class);
		List<JSONObject> backList = backResult.getMappedResults();
		long backData = 0;
		if(null != backList && !backList.isEmpty()){
			JSONObject obj = backList.get(0);
			backData = obj.getLongValue("sum");
		}
		type.setBacksource(backData);
		type.setBacksourceStr(this.cdnFlowUnit(backData));
		type.setCdnFlow(cdnFlowData);
		type.setCdnFlowStr(this.cdnFlowUnit(cdnFlowData));
		type.setCdnHreqs(cdnHreqs);
		type.setCdnHreqsStr(this.convertRequestCountUnit(cdnHreqs));
		type.setCdnDreqs(cdnDreqs);
		type.setCdnDreqsStr(this.convertRequestCountUnit(cdnDreqs));
		BigDecimal cost=costReportService.getCostForObs(cusId, DateUtil.addDay(firstDay, new int[]{0,0,0,1}), now, "cdn");
		String costStr=formatCost(cost);
		type.setCost(costStr+"元");
		return type;
	}
	/**
	 * 格式化金额
	 * @param cost
	 * @return
	 */
	private String formatCost(BigDecimal cost) {
		DecimalFormat df = new DecimalFormat("0.00");
		String result = df.format(cost.doubleValue());
		return result;
	}
	/**
	 * 换算单位：请求次数（百次、万次）
	 */
	private String convertRequestCountUnit(long requestCountAgo) {
		DecimalFormat df1 = new DecimalFormat("0.00");// 格式化小数
		String request = "";
		// 单位 （万次）:请求次数
		if (requestCountAgo >= 10000) {
			float requestCount = (float) requestCountAgo / 10000;
			String requestNum = df1.format(requestCount);
			request = requestNum + "万次";
		} else {
			float requestCount = (float) requestCountAgo / 100;
			String requestNum = df1.format(requestCount);
			request = requestNum + "百次";
		}
		return request;
	}
	/**
	 * 转换为带单位的字符串
	 * @Author: duanbinbin
	 * @param cdnFlowData
	 * @return
	 *<li>Date: 2016年6月30日</li>
	 */
	private String cdnFlowUnit(long cdnFlowData){
		 DecimalFormat df1 = new DecimalFormat("0.00");// 格式化小数
		 String cdnFlowStr = "";
		// 单位 （GB）:下载流量
		if (cdnFlowData >= tb) {
			float loadsize = (float) cdnFlowData / tb;
			String load = df1.format(loadsize);
			cdnFlowStr = load+"TB";
		} else if (cdnFlowData >= gb && cdnFlowData < tb) {
			float loadsize = (float) cdnFlowData / gb;
			String load = df1.format(loadsize);
			cdnFlowStr = load+"GB";
		} else if (cdnFlowData >= mb && cdnFlowData < gb) {
			float loadsize = (float) cdnFlowData / mb;
			String load = df1.format(loadsize);
			cdnFlowStr = load+"MB";
		} else if (cdnFlowData >= kb && cdnFlowData < mb) {
			float loadsize = (float) cdnFlowData / kb;
			String load = df1.format(loadsize);
			cdnFlowStr = load+"KB";
		} else {
			cdnFlowStr = cdnFlowData+"B";
		}
		return cdnFlowStr;
	 }

	@Override
	public Page getCDNResources(Page page, QueryMap queryMap, Date useStart,
			Date useEnd, String cusId) throws Exception {
		Date now=new Date();
		List<ObsUsedType> list=new ArrayList<ObsUsedType>();
		List<ObsUsedType> resultList = new ArrayList<ObsUsedType>();
		SimpleDateFormat formatDay=new SimpleDateFormat("yyyy-MM-dd");
		String todayStr=formatDay.format(now);
		String endStr=formatDay.format(useEnd);
		
		String beginStr=formatDay.format(useStart);
		Date begin=formatDay.parse(beginStr);
		int value = (int) ((useEnd.getTime() - begin.getTime()) / (24 * 60 * 60 * 1000));
		for (int i = 0; i < value; i++) {
			Date endTime=DateUtil.addDay(begin, new int[]{0,0,1});
			Sort sort = new Sort(Direction.DESC, "timestamp");
			Criteria backsourceCriatira = new Criteria();
			backsourceCriatira.andOperator(
					Criteria.where("cus_id").is(cusId),
					Criteria.where("timestamp").gte(begin),
					Criteria.where("timestamp").lt(endTime));
			List<JSONObject> jsonList = mongoTemplate.find(new Query(backsourceCriatira).with(sort), JSONObject.class, MongoCollectionName.CDN_BACKSOURCE_1D);
			Criteria cdnCriatira = new Criteria();
			cdnCriatira.andOperator(
					Criteria.where("cus_id").is(cusId),
					Criteria.where("timestamp").gte(begin),
					Criteria.where("timestamp").lt(endTime));
			List<JSONObject> cdnlist = mongoTemplate.find(new Query(cdnCriatira).with(sort), JSONObject.class, MongoCollectionName.OBS_CDN_1D);
			if(jsonList!=null&&jsonList.size()>0&&cdnlist!=null&&cdnlist.size()>0){//cdn和回源都有
				list=getAllResult(list, formatDay, jsonList, cdnlist);	
			}else if((jsonList!=null&&jsonList.size()>0)&&!(cdnlist!=null&&cdnlist.size()>0)){//回源有 cdn没有
				list=getBackResult(list, formatDay, jsonList, cdnlist);
			}else if(!(jsonList!=null&&jsonList.size()>0)&&(cdnlist!=null&&cdnlist.size()>0)){//回源没有  cdn有
				list=getCdnResult(list, formatDay, jsonList, cdnlist);
			}
			begin=DateUtil.addDay(begin, new int[]{0,0,1});
		}
		if(todayStr.equals(endStr)){//包含当天
			ObsUsedType type = new ObsUsedType();
			Date todayBegin=formatDay.parse(todayStr);
			Sort sort = new Sort(Direction.DESC, "timestamp");
			Criteria backsourceCriatira = new Criteria();
			backsourceCriatira.andOperator(
					Criteria.where("cus_id").is(cusId),
					Criteria.where("timestamp").gte(todayBegin),
					Criteria.where("timestamp").lt(DateUtil.addDay(useEnd, new int[]{0,0,1})));
			List<JSONObject> jsonList = mongoTemplate.find(new Query(backsourceCriatira).with(sort), JSONObject.class, MongoCollectionName.CDN_BACKSOURCE_1H);
			long backFlow=0;
			for (JSONObject jsonObject : jsonList) {
				long back=jsonObject.getLongValue("backsource");
				backFlow+=back;
			}
			Criteria cdnCriatira = new Criteria();
			cdnCriatira.andOperator(
					Criteria.where("cus_id").is(cusId),
					Criteria.where("timestamp").gte(todayBegin),
					Criteria.where("timestamp").lt(DateUtil.addDay(useEnd, new int[]{0,0,1})));
			List<JSONObject> cdnlist = mongoTemplate.find(new Query(cdnCriatira).with(sort), JSONObject.class, MongoCollectionName.OBS_CDN_1H);
			long cdnFlow=0;
			long cdnDreqs=0;
			long cdnHreqs=0;
			for (JSONObject jsonObject : cdnlist) {
				long flow=jsonObject.getLongValue("flow_data");
				cdnFlow+=flow;
				long dreqs=jsonObject.getLongValue("dreqs");
				cdnDreqs+=dreqs;
				long hreqs=jsonObject.getLongValue("hreqs");
				cdnHreqs+=hreqs;
			}
			type.setBacksource(backFlow);
			type.setBacksourceStr(this.cdnFlowUnit(backFlow));
			type.setCdnFlow(cdnFlow);
			type.setCdnFlowStr(this.cdnFlowUnit(cdnFlow));
			type.setCdnDreqs(cdnDreqs);
			type.setCdnDreqsStr(this.convertRequestCountUnit(cdnDreqs));
			type.setCdnHreqs(cdnHreqs);
			type.setCdnHreqsStr(this.convertRequestCountUnit(cdnHreqs));
			type.setTimeDis(todayStr);
			list.add(0,type);
		}
		//构造分页
		int startIndex = (queryMap.getPageNum() - 1)*queryMap.getCURRENT_ROWS_SIZE();
		int end = (startIndex + queryMap.getCURRENT_ROWS_SIZE()) - 1;
		if (list.size() - 1 < end) {
			end = list.size() - 1;
		}
		for (int i = 0; i < list.size(); i++) {
			if (startIndex <= i && i <= end) {
				resultList.add(list.get(i));
			}
		}
		ObsUsedType useType=getConsumeStatisticsAvg(list);
		//将阈值信息放入list
		for(ObsUsedType obsUsedType:resultList){
	    	obsUsedType.setSumBackFlow(useType.getSumBackFlow());
	    	obsUsedType.setSumCdnFlow(useType.getSumCdnFlow());
	    	obsUsedType.setSumCdnDreqs(useType.getSumCdnDreqs());
	    	obsUsedType.setSumCdnHreqs(useType.getSumCdnHreqs());
	    }
	    page = new Page(startIndex, list.size(), 10, resultList);
		return page;
	}

	private ObsUsedType getConsumeStatisticsAvg(List<ObsUsedType> list) {
		ObsUsedType used = new ObsUsedType();
		long back=0;
		long cdnFlow=0;
		long dReqs=0;
		long hReqs=0;
		for(ObsUsedType use:list){
			back = back + use.getBacksource();
			cdnFlow = cdnFlow + use.getCdnFlow();
			dReqs = dReqs + use.getCdnDreqs();
			hReqs = hReqs + use.getCdnHreqs();
		}
		used.setSumBackFlow(this.cdnFlowUnit(back));
		used.setSumCdnFlow(this.cdnFlowUnit(cdnFlow));
		used.setSumCdnDreqs(this.convertRequestCountUnit(dReqs));
		used.setSumCdnHreqs(this.convertRequestCountUnit(hReqs));
		return used;
	}

	private List<ObsUsedType> getCdnResult(List<ObsUsedType> list,
			SimpleDateFormat formatDay, List<JSONObject> jsonList,
			List<JSONObject> cdnlist) {
		for (JSONObject jsonObject : cdnlist) {
			ObsUsedType type = new ObsUsedType();
			Date timestamp=jsonObject.getDate("timestamp");
			String timeStr=formatDay.format(timestamp);
			type.setTimeDis(timeStr);
			long cdnFlow=jsonObject.getLongValue("flow_data");
			type.setCdnFlow(cdnFlow);
			type.setCdnFlowStr(this.cdnFlowUnit(cdnFlow));
			long cdnHreqs=jsonObject.getLongValue("hreqs");
			type.setCdnHreqs(cdnHreqs);
			type.setCdnHreqsStr(this.convertRequestCountUnit(cdnHreqs));
			long cdnDreqs=jsonObject.getLongValue("dreqs");
			type.setCdnHreqs(cdnHreqs);
			type.setCdnDreqsStr(this.convertRequestCountUnit(cdnDreqs));
			type.setBacksource(0);
			type.setBacksourceStr("0B");
			list.add(0,type);
		}
		return list;
	}

	private List<ObsUsedType> getBackResult(List<ObsUsedType> list,
			SimpleDateFormat formatDay, List<JSONObject> jsonList,
			List<JSONObject> cdnlist) {
		for (JSONObject json : jsonList) {
			ObsUsedType type = new ObsUsedType();
			long flow = json.getLongValue("backsource");
			Date timestamp=json.getDate("timestamp");
			String timeStr=formatDay.format(timestamp);
			type.setTimeDis(timeStr);
			type.setBacksource(flow);
			type.setBacksourceStr(this.cdnFlowUnit(flow));
			type.setCdnFlow(0);
			type.setCdnFlowStr("0B");
			type.setCdnDreqs(0);
			type.setCdnDreqsStr("0.00百次");
			type.setCdnHreqs(0);
			type.setCdnHreqsStr("0.00百次");
			list.add(0,type);
		}
		return list;
	}

	private List<ObsUsedType> getAllResult(List<ObsUsedType> list,
			SimpleDateFormat formatDay, List<JSONObject> jsonList,
			List<JSONObject> cdnlist) {
		for (JSONObject json : jsonList) {
			ObsUsedType type = new ObsUsedType();
			Date timestamp=json.getDate("timestamp");
			String backTimeStr=formatDay.format(timestamp);
			long flow = json.getLongValue("backsource");
			type.setTimeDis(backTimeStr);
			type.setBacksource(flow);
			type.setBacksourceStr(this.cdnFlowUnit(flow));
			for (JSONObject jsonObject : cdnlist) {
				Date cdnTimestamp=jsonObject.getDate("timestamp");
				String cdnTimeStr=formatDay.format(cdnTimestamp);
				if(cdnTimeStr.equals(backTimeStr)){
					long cdnFlow=jsonObject.getLongValue("flow_data");
					type.setCdnFlow(cdnFlow);
					type.setCdnFlowStr(this.cdnFlowUnit(cdnFlow));
					long cdnHreqs=jsonObject.getLongValue("hreqs");
					type.setCdnHreqs(cdnHreqs);
					type.setCdnHreqsStr(this.convertRequestCountUnit(cdnHreqs));
					long cdnDreqs=jsonObject.getLongValue("dreqs");
					type.setCdnDreqs(cdnDreqs);
					type.setCdnDreqsStr(this.convertRequestCountUnit(cdnDreqs));
					break;
				}else{
					type.setCdnFlow(0);
					type.setCdnFlowStr("0B");
					type.setCdnHreqs(0);
					type.setCdnHreqsStr("0.00百次");
					type.setCdnDreqs(0);
					type.setCdnDreqsStr("0.00百次");
				}
				list.add(0,type);
			}
		}
		return list;
	}

	@Override
	public Page getCdnHistoryResources(Page page, QueryMap queryMap,
			String cusId) throws Exception {
		SimpleDateFormat formatMonth=new SimpleDateFormat("yyyy-MM");
		List<ObsUsedType> list=new ArrayList<ObsUsedType>();
		List<ObsUsedType> resultList = new ArrayList<ObsUsedType>();
		Sort sort = new Sort(Direction.DESC, "timestamp");
		Criteria cdnCriatira = new Criteria();
		cdnCriatira.andOperator(
				Criteria.where("cus_id").is(cusId));
		List<JSONObject> cdnlist = mongoTemplate.find(new Query(cdnCriatira).with(sort), JSONObject.class, MongoCollectionName.OBS_CDN_1MON);
		for (JSONObject jsonObject : cdnlist) {
			ObsUsedType type = new ObsUsedType();
			Date timestamp=jsonObject.getDate("timestamp");
			String timeStr=formatMonth.format(timestamp);
			type.setTimeDis(timeStr);
			long cdnFlow=jsonObject.getLongValue("flow_data");
			type.setCdnFlow(cdnFlow);
			type.setCdnFlowStr(this.cdnFlowUnit(cdnFlow));
			long cdnHreqs=jsonObject.getLongValue("hreqs");
			type.setCdnHreqs(cdnHreqs);
			type.setCdnHreqsStr(this.convertRequestCountUnit(cdnHreqs));
			long cdnDreqs=jsonObject.getLongValue("dreqs");
			type.setCdnHreqs(cdnHreqs);
			type.setCdnDreqsStr(this.convertRequestCountUnit(cdnDreqs));
			Date begin=formatMonth.parse(timeStr);
			BigDecimal cost=costReportService.getCostForObs(cusId, DateUtil.addDay(begin, new int[]{0,0,0,1}), DateUtil.addDay(begin, new int[]{0,1}), "cdn");
			String costStr=formatCost(cost);
			type.setCost(costStr+"元");
			list.add(type);
		}
		//构造分页
		int startIndex = (queryMap.getPageNum() - 1)*queryMap.getCURRENT_ROWS_SIZE();
		int end = (startIndex + queryMap.getCURRENT_ROWS_SIZE()) - 1;
		if (list.size() - 1 < end) {
			end = list.size() - 1;
		}
		for (int i = 0; i < list.size(); i++) {
			if (startIndex <= i && i <= end) {
				resultList.add(list.get(i));
			}
		}
	    page = new Page(startIndex, list.size(), 10, resultList);
		return page;
	}

}
