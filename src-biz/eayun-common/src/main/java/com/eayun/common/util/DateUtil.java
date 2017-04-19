package com.eayun.common.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Date工具类
 * @Filename: DateUtil.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2015年10月10日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class DateUtil {
    private static final Logger log = LoggerFactory.getLogger(DateUtil.class);
    private static Calendar GMT_CAL = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    private static SimpleDateFormat RFC2822_DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z",
        Locale.ENGLISH);
    
    private static  SimpleDateFormat UTC_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private static  SimpleDateFormat UTC_FORMAT_Z = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static  SimpleDateFormat UTC_FORMAT_Z_SSS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSS'Z'");

    public static String PURCHASE = "PURCHASE"; //新购
    public static String RENEWAL = "RENEWAL"; //续费
    
    /**
     * 得到RFC-2822格式日期
     * 
     * @param date
     * @return
     */
    public static synchronized String getRFC2822Date(Date date){
        RFC2822_DATE_FORMAT.setCalendar(GMT_CAL);
        return RFC2822_DATE_FORMAT.format(date);
    }
    
    /**
     * 根据UTC格式日期返回Date类型，若转换失败则返回null
     * 
     * @param strDate
     * @return
     */
    public static synchronized Date formatUTCDate(String strDate){
        UTC_FORMAT.setCalendar(GMT_CAL);
        try {
            return UTC_FORMAT.parse(strDate);
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    public static synchronized String getUTCDate(Date date){
        UTC_FORMAT.setCalendar(GMT_CAL);
        return UTC_FORMAT.format(date);
    }
    
    
    /**
     * Timestamp(String类型时间戳)转换为Date类型
     * @param timestamp
     * @return
     */
    public static Date timestampToDate(String timestamp){
        Date date = null;
        if(timestamp!=null&&!(timestamp.equals(""))){
            long times = Long.parseLong(timestamp);
            Timestamp ts = new Timestamp(times);
            try {   
                date = ts;
            } catch (Exception e) {   
                log.error(e.getMessage(), e);
            }
        }
        return date;
    }
    
    /**
     * String 转换为Date类型
     * @param strDate：yyyy-MM-dd
     * @return
     */
   public static Date strToDate(String strDate) {
       Date date = null;
       if(strDate!=null&&!(strDate.equals(""))){
           SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
           try {
               date = sdf.parse(strDate);
           } catch (ParseException e) {
               log.error(e.getMessage(), e);
           }
       }
        return date; 
    } 
   /**
    * String 转换为Date类型
    * @param strDate：yyyy-MM-dd HH:mm:ss
    * @return
    */
   public static Date stringToDate(String strDate) {
       Date date = null;
       if(strDate!=null&&!(strDate.equals(""))){
           SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
           try {
               date = sdf.parse(strDate);
           } catch (ParseException e) {
               log.error(e.getMessage(), e);
           }
       }
       return date; 
   }
   /**
    * Date转换为String类型
    * @param date
    * @return：yyyy-MM-dd
    */
   public static String dateToStr(Date date){
       String dateStr = "";
       if(date != null){
           DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");   
           try {   
               dateStr = sdf.format(date);  
           } catch (Exception e) {   
               log.error(e.getMessage(), e); 
           }
       }
       return dateStr;
   }
   /**
    * Date转换为String类型
    * @param date
    * @return：yyyy-MM-dd HH:mm:ss
    */
   public static String dateToString(Date date){
       String dateStr = "";
       if(date != null){
           DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");   
           try {   
               dateStr = sdf.format(date);  
           } catch (Exception e) {   
               log.error(e.getMessage(), e);
           }
       }
       return dateStr;
   }
   /**
    * Date转换为String类型
    * @param date
    * @return：yyyy-MM-ddTHH:mm:ssZ
    */
   public static String dateToStringDate(Date date){
	   String dateStr = "";
       if(date != null){
    	   DateFormat sdf0 = new SimpleDateFormat("yyyy-MM-dd");  
           DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");   
           try {   
               dateStr = sdf.format(date);
               String dateStr1=sdf0.format(date);
               String dateStr2=dateStr.replaceAll(dateStr1, "");
               dateStr=dateStr1.trim()+"T"+dateStr2.trim()+"Z";
               
           } catch (Exception e) {   
               log.error(e.getMessage(), e);
           }
       }
       return dateStr;
   }
   /**
    * Date转换为String类型
    * @param date
    * @return：yyyy/MM/dd HH:mm:ss
    */
   public static String dateToStringTwo(Date date){
       String dateStr = "";
       if(date != null){
           DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");   
           try {   
               dateStr = sdf.format(date);  
           } catch (Exception e) {   
               log.error(e.getMessage(), e);
           }
       }
       return dateStr;
   }
   
   /**
    * 日期增加一天
    * @param times
    * @return
    */
   public static long dayToAdd(long times){
       return times + 86400000;
   }
   /**
    * 改变日期
    * 【年,月,日,时,分,秒】
    * 后位可省略
    * @param date
    * @return
    */
   public static Date addDay(Date date , int[] day) {  
       if (null == date) {  
           return date;  
       }  
       Calendar c = Calendar.getInstance();  
       c.setTime(date);             //设置日期
       if(day.length > 0){
           c.add(Calendar.YEAR,day[0]);
       }
       if(day.length > 1){
           c.add(Calendar.MONTH,day[1]);
       }
       if(day.length > 2){
           c.add(Calendar.DATE, day[2]);
       }
       if(day.length > 3){
           c.add(Calendar.HOUR,day[3]);
       }
       if(day.length > 4){
           c.add(Calendar.MINUTE,day[4]);
       }
       if(day.length > 5){
           c.add(Calendar.SECOND,day[5]);
       }
       date = c.getTime();  
       return date;  
   }
   
   /**
    * 计算两个日期时间的小时数
    * @param start
    * @param end
    * @return
    */
   public static long dayToDay(Date start , Date end){
       long startTime =  start.getTime();
       long endTime = end.getTime();
       long gap = endTime - startTime;
       long hours = gap / 3600000;
       if(gap % 3600000 != 0){
           hours = hours +1;
       }
       return hours;
   }
   /**
    * 去除时间里的秒数及毫秒
    * @param date
    * @return
    */
   public static Date dateRemoveSec(Date date) {
       String dateStr = "";
       if(date != null){
           DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");   
           try {   
               dateStr = sdf.format(date);  
           } catch (Exception e) {   
               log.error(e.getMessage(), e);
           }
       }
       if(dateStr!=null&&!(dateStr.equals(""))){
           SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
           try {
               date = sdf.parse(dateStr);
           } catch (ParseException e) {
               log.error(e.getMessage(), e);
           }
       }
       return date;
   }
    /**
     * 获取服务到期时间
     *
     * @param startDate 开始时间
     * @param duration  购买时长（单位：月。1年=12个月，2年=24个月，以此类推）
     * @param biz       业务类型枚举项：新购（DateUtil.PURCHASE）和续费（DateUtil.RENEWAL）
     * @return
     */
    public static Date getExpirationDate(Date startDate, int duration, String biz) {
        Date expiration = null;
        Calendar c = Calendar.getInstance();
        if (biz.equals(PURCHASE)) {
            expiration = addDay(startDate, new int[]{0, duration, 1});
        } else if (biz.equals(RENEWAL)) {
            expiration = addDay(startDate, new int[]{0, duration});
        }
        if(expiration!=null){
            c.setTime(expiration);
            c.set(Calendar.HOUR_OF_DAY,0);
            c.set(Calendar.MINUTE,0);
            c.set(Calendar.SECOND,0);
            c.set(Calendar.MILLISECOND,0);
        }
        return c.getTime();
    }
    
    /**
     * 获取预付费资源升级的剩余天数
     * 
     * @author zhouhaitao
     * @param endTime 资源到期时间
     * @return
     */
    public static Integer getUgradeRemainDays(Date startTime , Date endTime){
    	long start = startTime.getTime();
    	long end = endTime.getTime();
    	long btwTime = end - start;
    	int days = (int)(btwTime/(24*60*60*1000));
    	if(btwTime%(24*60*60*1000) != 0){
    		days = days +1;
    	}
    	
    	return days;
    }
    
    /**
     * 根据正常的Date对象获取标准的UTC日期
     * 
     * @author zhouhaitao
     * @param date
     * @param isSSSTime 布尔值，是否精确到毫秒
     * @return
     */
    public static synchronized String getUTCDateZ(Date date, boolean ... isSSSTime){
        if (isSSSTime.length != 0 && isSSSTime[0] == true){
            UTC_FORMAT_Z_SSS.setCalendar(GMT_CAL);
            return UTC_FORMAT_Z_SSS.format(date);
        }else {
            UTC_FORMAT_Z.setCalendar(GMT_CAL);
            return UTC_FORMAT_Z.format(date);
        }
    }

    /**
     * 根据UTC时间字符串转换为标准的北京时间
     * @param timeString
     * @return
     * @throws Exception
     */
    public static synchronized Date getBeijingTimeByUTCTimeString(String timeString){
        try {
            UTC_FORMAT_Z.setCalendar(GMT_CAL);
            return UTC_FORMAT_Z.parse(timeString);
        }catch (Exception e){
            e.printStackTrace();
            return null ;
        }
    }
    
    /**
     * 获取近几个自然日、自然月、自然年的00:00:00，今日是2017-03-21，则近七日是2017-03-14的00:00:00
     * @author bo.zeng@eayun.com
     * @param date 当前时间
     * @param num 几个单位
     * @param type 类型：Calendar.YEAR、Calendar.MONTH、Calendar.DATE
     * @param needLast 获取[年的][最后一个月的][最后一天的]最后一秒23:59:59
     * @return
     */
	public static Date getNearlyDateTime(Date date, int num, int type, boolean needLast) {
		if (date == null) {
			return null;
		}
		int[] period = new int[] { 0, 0, 0 };
		if (type == Calendar.YEAR) {
			period[0] = num;
		}
		if (type == Calendar.MONTH) {
			period[1] = num;
		}
		if (type == Calendar.DATE) {
			period[2] = num;
		}
		Date newDate = addDay(date, period);
		Calendar c = Calendar.getInstance();
		c.setTime(newDate);
		if (needLast) {
			if (type == Calendar.YEAR) {
				c.set(Calendar.MONTH, 11);
				c.add(Calendar.MONTH, 1);
				c.set(Calendar.DAY_OF_MONTH, 0);
			}
			if (type == Calendar.MONTH) {
				c.add(Calendar.MONTH, 1);
				c.set(Calendar.DAY_OF_MONTH, 0);
			}
			c.set(Calendar.HOUR_OF_DAY, 23);
			c.set(Calendar.MINUTE, 59);
			c.set(Calendar.SECOND, 59);
			return c.getTime();
		}
		if (type == Calendar.YEAR) {
			c.set(Calendar.MONTH, 0);
			c.set(Calendar.DAY_OF_MONTH, 1);
		}
		if (type == Calendar.MONTH) {
			c.set(Calendar.DAY_OF_MONTH, 1);
		}
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		return c.getTime();
	}
	
	/**
	 * 获取年份字符串
	 * @author bo.zeng@eayun.com
	 * @param date
	 * @return
	 */
	public static String getYearString(Date date) {
		String dateStr = "";
		if (date != null) {
			DateFormat sdf = new SimpleDateFormat("yyyy");
			try {
				dateStr = sdf.format(date);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		return dateStr;
	}
    
    public static void main(String args[]){
    	DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");   
    	System.out.println(sdf.format(getNearlyDateTime(new Date(), -1, Calendar.DATE, true)));
    }
}
