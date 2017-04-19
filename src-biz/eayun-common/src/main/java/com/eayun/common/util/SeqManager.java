package com.eayun.common.util;

/**
 * <p>Title: Sequence 主键管理</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: telgenius</p>
 * @author 陈玉良
 * @version 1.0
 
 */
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
public class SeqManager 
{
    private static final Logger log = LoggerFactory.getLogger(SeqManager.class);
    private static final SeqManager seqManager = new SeqManager();
    private static int iIndex = 0;
    private SeqManager(){
    }
    public static SeqManager getSeqMang(){//单例保证唯一的对象，如果多个server还的考虑不同server之间产生冲突id的可能。
        return seqManager;
    }
 
   /**
    * 方法名 : getSeqDate
    * 说明   ：得到以当前时间戳的sequence
    * 引数   : 无
    * 返回   : String , 成功返回主键sequence, 失败返回空字符串
    */
    public  synchronized String getSeqForDate()
    {
        String strSeqDate = "";
        try
        {
            String           strDateTemp = "";
            Calendar         cal         = Calendar.getInstance();
            SimpleDateFormat formatter   = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String           strDate     = formatter.format(cal.getTime());
            strDateTemp = strDate.substring(2,4) + strDate.substring(5,7) + strDate.substring(8,10)
                        + strDate.substring(11,13) + strDate.substring(14,16) + strDate.substring(17,19);
            if(iIndex == 1000)iIndex = 0;
            strSeqDate = strDateTemp + String.valueOf(iIndex);
            iIndex ++;
        }
        catch(Exception ex)
        {
            log.error(ex.getMessage(), ex);
        }
        return strSeqDate;
    }
 
}
