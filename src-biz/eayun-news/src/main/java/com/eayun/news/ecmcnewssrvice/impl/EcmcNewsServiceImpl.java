package com.eayun.news.ecmcnewssrvice.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.ConstantClazz;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.customer.model.BaseCustomer;
import com.eayun.news.dao.NewsRecDao;
import com.eayun.news.dao.NewsSendDao;
import com.eayun.news.ecmcnewsservice.EcmcNewsService;
import com.eayun.news.model.BaseNewsRec;
import com.eayun.news.model.BaseNewsSend;
import com.eayun.news.model.NewsSendVOE;
import com.eayun.news.model.SysSelfUser;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年3月30日
 */
@Transactional
@Service
public class EcmcNewsServiceImpl implements EcmcNewsService {

	private static final Logger log = LoggerFactory.getLogger(EcmcNewsServiceImpl.class);

	@Autowired
	private NewsSendDao newsSendDao;
	@Autowired
	private NewsRecDao newsRecDao;
	@Autowired
	private JedisUtil jedisUtil;

	@Override
	public boolean timeFlag(Long timeStart) {
		log.info("消息验证时间");
		Date date = new Date();
		Long timeNow = date.getTime();
		if ((timeNow - timeStart) > 0) {
			return true;
		}
		return false;
	}
	
	
	public boolean timeFlag30(Long timeStart){
		log.info("删除消息验证时间是否30天");
		Date date=new Date();
		Long timenow=date.getTime();
		Long iftime30=timenow-timeStart;
		Long i=(long)30*24*60*60;
		Long j=(long)1000;
		Long time30=i*j;
		if((iftime30-time30)>0){
			return true;
		}
		return false;
	}

	@Override
	public Map<Boolean,String> deleteById(String id) throws AppException {
		log.info("消息删除");
		
		Map<Boolean,String>map =new HashMap<>();
		String hql = new String("delete BaseNewsRec where newsId = ?");
		BaseNewsSend newsSend = newsSendDao.findOne(id);
		
		
		if(newsSend.getCollected()>0){
			map.put(false, "该条消息已被收藏，无法删除！");
			return map;
		}
		if(!timeFlag30(newsSend.getSendDate().getTime())){
			map.put(false, "该条消息未失效，无法删除！");
			return map;
		}
		if (newsSend != null) {
			newsRecDao.executeUpdate(hql, id);
			newsSendDao.remove(newsSend);
			map.put(true, "删除成功！");
		}
		
		return map;
		
	}

	@Override
	public Page getNewsList(Page page, QueryMap querymap, Date beginTime, Date endTime, String title, String userId,
			String issyssend) throws AppException {
		log.info("获取消息列表");
		List<Object> list = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append(
				"select s.news_id as id,s.news_title as title,s.memo as memo,s.send_date as sendDate,s.send_person as sendPerson,s.rec_type as recType,"
						+ "s.cus_id as cusId,s.sended as sended,s.is_syssend as sys from news_sendinfo s ");
		sql.append(" where 1=1");
		if (null != beginTime) {
			sql.append(" and s.send_date >= ? ");
			list.add(beginTime);
		}
		if (null != endTime) {
			sql.append(" and s.send_date <= ? ");
			list.add(endTime);
		}
		if (null != title && !title.trim().equals("")) {
			sql.append(" and s.news_title like ? ");
			title = title.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
			list.add("%" + title + "%");
		}

		if (null != issyssend && !"".equals(issyssend)) {
			sql.append(" and s.is_syssend=?");
			list.add(issyssend);
		}
		sql.append(" order by s.send_date desc");
		page = newsSendDao.pagedNativeQuery(sql.toString(), querymap, list.toArray());
		List newlist =  (List) page.getResult();
	

		
		
		StringBuffer hql=new StringBuffer("select cus_id,cus_name,cus_cpname from sys_selfcustomer where cus_id in(");
		List<String> allCusList=new ArrayList<String>();
		
		//获取参数，拼装参数问号
		for (int m = 0; m < newlist.size(); m++) {
			Object[] objs = (Object[]) newlist.get(m);
			String[] cuslist = String.valueOf(objs[6]).split(",");
			for(int k = 0; k < cuslist.length; k++){
				allCusList.add(cuslist[k]);
				hql.append("?,");
			}
			
			
		}
		//删除hql参数最后面一个逗号
		hql.deleteCharAt(hql.length()-1);
		hql.append(") ");
		//list转数组
		Object[] allstrcus=allCusList.toArray();
		List<Object[]> userList=new ArrayList<>();
		if(newlist.size()>0){
			 userList=newsSendDao.createSQLNativeQuery(hql.toString(), allstrcus).getResultList();
		}
		//List<Object[]> userList=newsSendDao.createSQLNativeQuery(hql.toString(), allstrcus).getResultList();
		Map map=new HashMap<>();
		
		
		//查询结果转map
		for(int g=0;g<userList.size();g++){
			Object [] mapsuc= userList.get(g);
			map.put(mapsuc[0], mapsuc);
		}
		
		
		for (int i = 0; i < newlist.size(); i++) {

			
			NewsSendVOE voe = new NewsSendVOE();
			Object[] objs =  (Object[]) newlist.get(i);
			String[] cuslist = String.valueOf(objs[6]).split(",");
				String cusname="";
				String cpname="";
				//从map取数据设值
				for(int d=0;d<cuslist.length;d++){
					if(!"all".equals(cuslist[d])){
					Object [] val=(Object[]) map.get(cuslist[d]);
					if(null!=val){
						if(null!=val[2]&&!"".equals(val[2])){
						
					
							cpname+=val[2]+",";
							cusname+=val[1]+",";
						}else{
							cpname="";
							cusname+=val[1]+",";
					}
					}else{
						cpname="全部公司";
					}
				
			
					}else{
						cpname="全部公司";
					}
			
			
			
			voe.setCusName(cusname);
			voe.setCusCpname(cpname);
			voe.setId(String.valueOf(objs[0]));
			voe.setNewsTitle(String.valueOf(objs[1]));
			voe.setMemo(String.valueOf(objs[2]));
			voe.setSendDate((Date) objs[3]);
			voe.setSendPerson(String.valueOf(objs[4]));
			voe.setRecType(String.valueOf(objs[5]));
			voe.setCusId(String.valueOf(objs[6]));

			voe.setSended(Integer.parseInt(objs[7].toString()));
			voe.setIsSysSend(String.valueOf(objs[8]));
			newlist.set(i, voe);
		
		}
		}

		return page;
	}

	@Override
	public void save(BaseNewsSend ns) throws AppException {
		try {
			List<SysSelfUser> list = new ArrayList<SysSelfUser>();
			/********** 封装newsSend数据bean来入库 开始 **********/
			String cus = ns.getCusId().replace("undefined", "");
			ns.setCusId(cus);
			newsSendDao.saveEntity(ns);
			/********** 封装newsSend数据bean来入库 结束 **********/
			StringBuffer hql = new StringBuffer("from SysSelfUser ssu where 1=1 ");
			if (ns.getRecType().equals("2")) {
				hql.append("and ssu.isAdmin = 1 ");
			}

			String cusstr[] = cus.split(",");

			String sqldata = "";
			for (int i = 0; i < cusstr.length; i++) {
				if ("ALL".equals(cusstr[i])) {
					sqldata = cusstr[i];
					break;
				}

			}
			if (ns.getCusId() != null && !"ALL".equals(sqldata)) {
				hql.append("and ssu.cusId in(?");
				if (cusstr.length > 1) {
					for (int i = 0; i < cusstr.length - 1; i++) {
						hql.append(",?");
					}
				}
				hql.append(") ");

				list = newsSendDao.find(hql.toString(), cusstr);
			} else {
				list = newsSendDao.find(hql.toString());
			}
			/********** 封装newsRec数据bean来入库 开始 **********/
			BaseNewsRec nr = null;
			for (int i = 0; i < list.size(); i++) {
				nr = new BaseNewsRec();
				nr.setSendTime(ns.getSendDate());
				nr.setNewsId(ns.getId());
				nr.setRecPerson(list.get(i).getUserAccount());
				nr.setIsCollect("0");
				nr.setStatu("0");
				nr.setIsDelete("0");
				newsRecDao.save(nr);
			}
			log.info("respCode", ConstantClazz.SUCCESS_CODE);
			/********** 封装newsRec数据bean来入库 结束 **********/
		} catch (Exception e) {
			log.info("respCode", ConstantClazz.ERROR_CODE);
			log.error("", e);
			throw new AppException("error.globe.system", e);
		}
	}

	@Override
	public void editNewsRec(NewsSendVOE nsv) throws AppException {
		String hqlNewsRec = new String("from BaseNewsRec where newsId = ?");
		List<BaseNewsRec> listNewsRec = newsRecDao.find(hqlNewsRec, new String[] { nsv.getId() });
		for (int i = 0; i < listNewsRec.size(); i++) {
			newsRecDao.remove(listNewsRec.get(i));
		}
		StringBuffer hqlUser = new StringBuffer("from SysSelfUser ssu where 1=1 ");
		List<SysSelfUser> list = new ArrayList<SysSelfUser>();

		if (nsv.getRecType().equals("2")) {
			hqlUser.append("and ssu.isAdmin = 1 ");

		}
		String cusstr[] = nsv.getCusId().split(",");
		List<String> listdata = new ArrayList<String>();
		Map<String, Object> params = new HashMap<>();
		String sqldata = "";
		for (int i = 0; i < cusstr.length; i++) {
			if ("ALL".equals(cusstr[i])) {
				sqldata = cusstr[i];
			}

		}
		if (nsv.getCusId() != null && !"ALL".equals(sqldata)) {
			hqlUser.append("and ssu.cusId in(?");
			if (cusstr.length > 1) {
				for (int i = 0; i < cusstr.length - 1; i++) {
					hqlUser.append(",?");
				}
			}
			hqlUser.append(") ");
			params.put("cusIds", listdata);
			// list = newsSendDao.find(hql.toString(),listdata.toArray());
			list = newsSendDao.find(hqlUser.toString(), cusstr);
		} else {
			list = newsSendDao.find(hqlUser.toString());
		}

		// List<SysSelfUser> listSysSelfUser =
		// newsRecDao.find(hqlUser.toString(),listParams.toArray(new
		// String[]{}));
		/********** 封装newsRec数据bean来入库 开始 **********/
		BaseNewsRec nr = null;
		for (int i = 0; i < list.size(); i++) {
			nr = new BaseNewsRec();
			nr.setNewsId(nsv.getId());
			nr.setRecPerson(list.get(i).getUserAccount());
			nr.setIsCollect("0");
			nr.setStatu("0");
			nr.setIsDelete("0");
			nr.setSendTime(nsv.getSendDate());
			newsRecDao.save(nr);
		}
		/********** 封装newsRec数据bean来入库 结束 **********/
	}

	@Override
	public void edit(NewsSendVOE nsv) throws AppException {
		BaseNewsSend newsSend =newsSendDao.findOne(nsv.getId()); 
		if(newsSend==null){
			return ;
		}
		newsSend.setNewsTitle(nsv.getNewsTitle());
		newsSend.setMemo(nsv.getMemo());
		newsSend.setCusId(nsv.getCusId());
		newsSend.setRecType(nsv.getRecType());
		newsSend.setSendDate(nsv.getSendDate());
		newsSend.setSendPerson(nsv.getSendPerson());
		newsSend.setSended(nsv.getSended());
		newsSend.setReaded(nsv.getReaded());
		newsSend.setIsSended(nsv.getIsSended());
		newsSendDao.saveOrUpdate(newsSend);
	}

	@Override
	public Map<String, List<NewsSendVOE>> getList() throws AppException {
		Map<String, List<NewsSendVOE>> mapl = new HashMap<String, List<NewsSendVOE>>();
		String sqlCustomer = "select cus_id as cusid,cus_name as name,cus_cpname as cpname from sys_selfcustomer where cus_falg = 1";
		List listmap = newsSendDao.createSQLNativeQuery(sqlCustomer).getResultList();
		List<NewsSendVOE> listCustomer = new ArrayList<NewsSendVOE>();
		NewsSendVOE nsv = null;
		Object[] objs = null;
		for (int i = 0; i < listmap.size(); i++) {
			objs = (Object[]) listmap.get(i);
			nsv = new NewsSendVOE();
			nsv.setCusId(String.valueOf(objs[0]));
			nsv.setCusName(String.valueOf(objs[1]));
			nsv.setCusCpname(String.valueOf(objs[2]));
			listCustomer.add(nsv);
		}
		String sqlSendPerson = "select distinct send_person from news_sendinfo ";
		List listmapSendPerson = newsSendDao.createSQLNativeQuery(sqlSendPerson).getResultList();
		List<NewsSendVOE> listSendPerson = new ArrayList<NewsSendVOE>();
		for (int i = 0; i < listmapSendPerson.size(); i++) {
			nsv = new NewsSendVOE();
			nsv.setSendPerson(listmapSendPerson.get(i).toString());
			listSendPerson.add(nsv);
		}
		mapl.put("customer", listCustomer);
		mapl.put("sendPerson", listSendPerson);
		return mapl;
	}

	@Override
	public Object getCount(NewsSendVOE nsv) throws AppException {
		try {

			/********** 消息已发送数 开始 **********/
			if (timeFlag(nsv.getSendDate().getTime())) {
				/********** 获取各个计数 开始 **********/
				String redisStatuCount = jedisUtil.get(RedisKey.MESSAGE_STATUS_COUNT + nsv.getId()); // redis计数器
				String redisCollectCount = jedisUtil.get(RedisKey.MESSAGE_COLLECT_COUNT + nsv.getId());
				String redisUncollectCount = jedisUtil.get(RedisKey.MESSAGE_UNCOLLECT_COUNT + nsv.getId());
				int collected = 0;
				if (redisCollectCount != null) {
					if (redisUncollectCount != null) {
						collected = Integer.parseInt(redisCollectCount) - Integer.parseInt(redisUncollectCount);
					} else {
						collected = Integer.parseInt(redisCollectCount);
					}
				}
				/********** 获取各个计数 结束 **********/
				/********** 消息已读数 开始 **********/
				nsv.setIsSended("1");
				StringBuffer sql = new StringBuffer("select count(*) from news_recinfo where 1=1 ");
				if (nsv.getId() != null) {
					sql.append("and news_id = ? ");
				}
				int sendCount = ((Number) newsRecDao.createSQLNativeQuery(sql.toString(), nsv.getId())
						.getSingleResult()).intValue();
				nsv.setSended(sendCount);
				edit(nsv);
				/********** 消息已读数 开始 **********/
				if (redisStatuCount != null) {
					nsv.setReaded(Integer.parseInt(redisStatuCount));
				} else {
					BaseNewsSend newsSend = newsSendDao.findOne(nsv.getId());
					nsv.setReaded(newsSend.getReaded());
				}
				/********** 消息已读数 结束 **********/
				/********** 消息收藏数 开始 **********/
				if (collected != 0) {
					nsv.setCollected(collected);
				} else {
					BaseNewsSend newsSend = newsSendDao.findOne(nsv.getId());
					nsv.setCollected(newsSend.getCollected());
				}
				/********** 消息收藏数 结束 **********/
			}
			/********** 消息已发送数 结束 **********/
			return nsv;

		} catch (Exception e) {
			throw new AppException("error.globe.system", e);
		}
	}

	private String[] setVoe(NewsSendVOE voe, String hql, String[] cuslist) {
		List<BaseCustomer> listcus = newsSendDao.find(hql, cuslist);
		String cusname = "";
		String cpname = "";

		for (int j = 0; j < listcus.size(); j++) {

			BaseCustomer cusobj = listcus.get(j);
			if (null != cusobj) {
				cusname += cusobj.getCusName() + ",";
				cpname += cusobj.getCusCpname() + ",";

			}

		}

		return new String[] { cusname, cpname };

	}
}
