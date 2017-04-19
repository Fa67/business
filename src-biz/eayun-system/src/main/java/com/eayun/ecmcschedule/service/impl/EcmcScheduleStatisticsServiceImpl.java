package com.eayun.ecmcschedule.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.ecmcschedule.dao.EcmcScheduleStatisticsDao;
import com.eayun.ecmcschedule.model.BaseEcmcScheduleStatistics;
import com.eayun.ecmcschedule.service.EcmcScheduleStatisticsService;

@Service
@Transactional
public class EcmcScheduleStatisticsServiceImpl implements EcmcScheduleStatisticsService {
    
    private static final Logger log = LoggerFactory
            .getLogger(EcmcScheduleStatisticsServiceImpl.class);

	@Autowired
	private EcmcScheduleStatisticsDao ecmcScheduleStatisticsDao;

	@Override
	public void add(BaseEcmcScheduleStatistics baseEcmcScheduleStatistics) {
		ecmcScheduleStatisticsDao.saveOrUpdate(baseEcmcScheduleStatistics);
	}

	@SuppressWarnings("unchecked")
    @Override
	public Page getByTriggerName(String taskId, String startTime, String endTime, QueryMap queryMap) {
		try {
			List<String> params = new ArrayList<>();
			StringBuffer sqlBuff = new StringBuffer();
			Date startDate = (startTime == null ? DateUtil.addDay(new Date(), new int[] { 0, 0, -6 })
					: DateUtil.timestampToDate(startTime));
			Date endDate = (endTime == null ? new Date() : DateUtil.timestampToDate(endTime));
			sqlBuff.append(
					"select t.id, t.trigger_name, t.statistics_date, t.total_count, t.suc_count, t.fal_count from schedule_statistics t where 1=1 ");
			if (!StringUtil.isEmpty(taskId)) {
				sqlBuff.append("and t.trigger_name = ? ");
				params.add(taskId);
			}
			sqlBuff.append("and t.statistics_date >= ? ");
			params.add(DateUtil.dateToStr(startDate));
			sqlBuff.append("and t.statistics_date <=  ? ");
			params.add(DateUtil.dateToStr(endDate));
			sqlBuff.append("order by t.statistics_date desc");
			Page page = ecmcScheduleStatisticsDao.pagedNativeQuery(sqlBuff.toString(), queryMap, params.toArray());
			List<Object[]> pageResult = (List<Object[]>) page.getResult();
			List<BaseEcmcScheduleStatistics> sqlResult = new ArrayList<BaseEcmcScheduleStatistics>();
			List<BaseEcmcScheduleStatistics> newPageResult = new ArrayList<BaseEcmcScheduleStatistics>();
			List<String> dateList = new ArrayList<String>();
			while (!(DateUtil.dateToStr(startDate).compareTo(DateUtil.dateToStr(endDate)) > 0)) {
				dateList.add(DateUtil.dateToStr(startDate));
				startDate = DateUtil.addDay(startDate, new int[] { 0, 0, 1 });
			}
			if (pageResult != null && pageResult.size() > 0) {
				for (Object[] objects : pageResult) {
					BaseEcmcScheduleStatistics scheduleStatistics = new BaseEcmcScheduleStatistics();
					scheduleStatistics.setId(ObjectUtils.toString(objects[0], null));
					scheduleStatistics.setTriggerName(ObjectUtils.toString(objects[1], null));
					scheduleStatistics.setStatisticsDate(DateUtil.strToDate(ObjectUtils.toString(objects[2], null)));
					scheduleStatistics.setTotalCount(Integer.parseInt(ObjectUtils.toString(objects[3], "0")));
					scheduleStatistics.setSucCount(Integer.parseInt(ObjectUtils.toString(objects[4], "0")));
					scheduleStatistics.setFalCount(Integer.parseInt(ObjectUtils.toString(objects[5], "0")));
					sqlResult.add(scheduleStatistics);
				}
			}
			for (String date : dateList) {
				BaseEcmcScheduleStatistics statistics = null;
				for (BaseEcmcScheduleStatistics scheduleStatistics : sqlResult) {
					if (scheduleStatistics.getStatisticsDate().equals(DateUtil.strToDate(date))) {
						statistics = scheduleStatistics;
					}
				}
				if (statistics == null) {
					statistics = new BaseEcmcScheduleStatistics(taskId, DateUtil.strToDate(date));
				}
				newPageResult.add(statistics);
			}
			page.setResult(newPageResult);
			return page;
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
    @Override
	public BaseEcmcScheduleStatistics getTriggerNameAndDate(String triggerName, Date statisticsDate) {
		String sqlString = "select t.id, t.trigger_name as triggerName, t.statistics_date as statisticsDate, t.total_count as totalCount, t.suc_count as sucCount, t.fal_count as falCount from schedule_statistics t where t.trigger_name = ? and t.statistics_date = ?";
		List<Object[]> resultList = ecmcScheduleStatisticsDao
				.createSQLNativeQuery(sqlString, triggerName, DateUtil.dateToStr(statisticsDate)).getResultList();
		if (resultList != null && resultList.size() > 0) {
			Object[] object = resultList.get(0);
			BaseEcmcScheduleStatistics scheduleStatistics = new BaseEcmcScheduleStatistics();
			scheduleStatistics.setId(ObjectUtils.toString(object[0], null));
			scheduleStatistics.setTriggerName(ObjectUtils.toString(object[1], null));
			scheduleStatistics.setStatisticsDate(DateUtil.strToDate(ObjectUtils.toString(object[2], null)));
			scheduleStatistics.setTotalCount(Integer.parseInt(ObjectUtils.toString(object[3], "0")));
			scheduleStatistics.setSucCount(Integer.parseInt(ObjectUtils.toString(object[4], "0")));
			scheduleStatistics.setFalCount(Integer.parseInt(ObjectUtils.toString(object[5], "0")));
			return scheduleStatistics;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
    @Override
	public Map<String, Object> getChartData(String taskId, String startTime, String endTime) {
		try {
			Date startDate = (startTime == null ? DateUtil.addDay(new Date(), new int[] { 0, 0, -6 })
					: DateUtil.timestampToDate(startTime));
			Date endDate = (endTime == null ? new Date() : DateUtil.timestampToDate(endTime));
			List<String> xData = new ArrayList<String>();
			String x = null;
			int ySuc = 0;
			int yFal = 0;
			List<Integer> ySucData = new ArrayList<Integer>();
			List<Integer> yFalData = new ArrayList<Integer>();
			Map<String, Object> resultMap = new HashMap<String, Object>();
			List<String> params = new ArrayList<>();
			StringBuffer sqlBuff = new StringBuffer();
			sqlBuff.append(
					"select t.statistics_date, sum(t.suc_count), sum(t.fal_count) from schedule_statistics t where 1=1 ");
			if (!StringUtil.isEmpty(taskId)) {
				sqlBuff.append("and t.trigger_name = ? ");
				params.add(taskId);
			}
			sqlBuff.append("and t.statistics_date >= ? ");
			params.add(DateUtil.dateToStr(startDate));
			sqlBuff.append("and t.statistics_date <= ? ");
			params.add(DateUtil.dateToStr(endDate));
			sqlBuff.append("group by t.statistics_date order by t.statistics_date");
			List<Object[]> resultList = ecmcScheduleStatisticsDao
					.createSQLNativeQuery(sqlBuff.toString(), params.toArray()).getResultList();
			List<String> dateList = new ArrayList<String>();
			while (!(DateUtil.dateToStr(startDate).compareTo(DateUtil.dateToStr(endDate)) > 0)) {
				dateList.add(DateUtil.dateToStr(startDate));
				startDate = DateUtil.addDay(startDate, new int[] { 0, 0, 1 });
			}
			if (resultList != null && resultList.size() > 0) {
				for (String date : dateList) {
					x = date;
					ySuc = 0;
					yFal = 0;
					for (Object[] object : resultList) {
						if (ObjectUtils.toString(object[0], "").equals(date)) {
							ySuc = Integer.parseInt(ObjectUtils.toString(object[1], "0"));
							yFal = Integer.parseInt(ObjectUtils.toString(object[2], "0"));
							break;
						}
					}
					xData.add(x);
					ySucData.add(ySuc);
					yFalData.add(yFal);
				}
			}
			resultMap.put("xData", xData);
			resultMap.put("ySucData", ySucData);
			resultMap.put("yFalData", yFalData);
			return resultMap;
		} catch (Exception e) {
			throw e;
		}
	}

}
