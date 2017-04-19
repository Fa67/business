package com.eayun.common.tools;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sql.rowset.CachedRowSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @Title:SeqTool
 * @description:流水号生成器
 * @author 李海峰
 * @date 2014年9月17日11:20:37
 * @version V1.0
 */
/**
 * @author EAYUN
 *
 */
public class SeqTool {
	private static final    Log log = LogFactory.getLog(SeqTool.class);   
	private static String QUERY_NUM_SQL = "select * from order_number t where prefix=? and s_date=?";
	private static String INSERT_NUM_SQL = "insert into order_number(prefix,s_date,max_seq) values(?,?,?)";
	private static String UPDATE_NUM_SQL = "update order_number set max_seq=? where prefix=? and s_date=?";
	private static String DELETE_NUM_SQL = "delete from order_number where prefix=? and s_date=?";
	public synchronized static void delNumSeq(String prefix,String format){
		if(prefix==null || "".equals(prefix)){
			prefix = "GD";
		}
		String dateStr=format;
		if(format.contains("yyyy") || format.contains("MM") || format.contains("dd")){
			dateStr = formatDate(format);
		}
		JdbcDbExecutor.executeSql(DELETE_NUM_SQL, new Object[]{prefix,dateStr});
	}
	public synchronized static String getSeqId(String prefix,String format,int length){
		if(prefix==null || "".equals(prefix)){
			prefix = "GD";
		}
		String dateStr=format;
		if(format.contains("yyyy") || format.contains("MM") || format.contains("dd")){
			dateStr = formatDate(format);
		}
		CachedRowSet crs = JdbcDbExecutor.querySql(QUERY_NUM_SQL, new Object[]{prefix,dateStr});
		int maxNum = 0;
		try {
			if(crs!=null && crs.next()){				        
				maxNum = crs.getInt("MAX_SEQ");
			}
		} catch (SQLException e) {
			log.error(e,e);			  
		}
		
		String nf = "";
//		if(length<4 || length>8){
//			length = 8;
//		}
		for(int i=0;i<length;i++){
			nf = "0"+nf;
		}
		DecimalFormat df = new DecimalFormat(nf); 
		String j = df.format(++maxNum);
		return prefix+dateStr+j;
	}
	/**
	 * prefix：生成前缀
	 * format：生成部分格式
	 * length：工单流水号位数
	 */
	public synchronized static String generateNextNumber(String prefix,String format,int length){
		if(prefix==null || "".equals(prefix)){
			prefix = "GD";
		}
		String dateStr=format;
		if(format.contains("yyyy") || format.contains("MM") || format.contains("dd")){
			dateStr = formatDate(format);
		}
		CachedRowSet crs = JdbcDbExecutor.querySql(QUERY_NUM_SQL, new Object[]{prefix,dateStr});
		int maxNum = 0;
		try {
			if(crs!=null && crs.next()){				        
				maxNum = crs.getInt("MAX_SEQ");
			}
		} catch (SQLException e) {
			log.error(e,e);			  
		}
		if(maxNum == 0){
			JdbcDbExecutor.executeSql(INSERT_NUM_SQL,new Object[]{prefix,dateStr,++maxNum} );
		}else{
			JdbcDbExecutor.executeSql(UPDATE_NUM_SQL,new Object[]{++maxNum,prefix,dateStr} );
		}
		String nf = "";
//		if(length<4 || length>8){
//			length = 8;
//		}
		for(int i=0;i<length;i++){
			nf = "0"+nf;
		}
		DecimalFormat df = new DecimalFormat(nf); 
		String j = df.format(maxNum);
		return prefix+dateStr+j;
	}
	private static String formatDate(String format){
		 if(format==null || "".equals(format.trim())){
			 format = "yyyyMMdd";
		 }
		 SimpleDateFormat formatter = new SimpleDateFormat(format);
		 return formatter.format(new Date());
	}
	
	
}
