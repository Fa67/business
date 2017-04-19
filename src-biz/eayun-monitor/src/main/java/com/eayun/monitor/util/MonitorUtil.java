package com.eayun.monitor.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class MonitorUtil {
    private static Object lock = new Object();

    private static Map<String, String> monitorZbNameMap = null;
    private static Map<String, String> methodMap        = null;
    private static Map<String, String> monitorUnit      = null;

    /**
     * 加上注解@Service，希望在服务启动的时候执行构造函数，初始化map
     * 
     * 构建一个空的<code>MonitorUtil.java</code>
     */
    public MonitorUtil() {
        synchronized (lock) {
            if (monitorZbNameMap == null) {
                monitorZbNameMap = new HashMap<String, String>();
                monitorZbNameMap.put("vmCpuRate", "CPU使用率");
                monitorZbNameMap.put("vmMemoryRate", "内存使用率");
                monitorZbNameMap.put("vmDiskRate", "磁盘使用率");
                monitorZbNameMap.put("vmDiskRead", "硬盘读IO");
                monitorZbNameMap.put("vmDiskWrite", "硬盘写IO");
                monitorZbNameMap.put("vmNetworkIn", "入流量");
                monitorZbNameMap.put("vmNetworkOut", "出流量");
                
                monitorZbNameMap.put("pmCpuRate", "CPU使用率");
                monitorZbNameMap.put("pmMemoryRate", "内存使用率");
                monitorZbNameMap.put("pmDiskRate", "磁盘使用率");
                monitorZbNameMap.put("pmDiskRead", "硬盘读IO");
                monitorZbNameMap.put("pmDiskWrite", "磁盘写IO");
                monitorZbNameMap.put("pmNetworkIn", "入流量");
                monitorZbNameMap.put("pmNetworkOut", "出流量");
                monitorZbNameMap.put("pmConnNum", "连接数");
            }
        }

        synchronized (lock) {
            if (methodMap == null) {
                methodMap = new HashMap<String, String>();
                methodMap.put("AVG", "平均值");
                methodMap.put("MIN", "最小值");
                methodMap.put("MAX", "最大值");
            }
        }
        
        synchronized (lock) {
            if (monitorUnit == null) {
                monitorUnit = new HashMap<String, String>();
                monitorUnit.put("vmCpuRate", "%");
                monitorUnit.put("vmMemoryRate", "%");
                monitorUnit.put("vmDiskRate", "%");
                monitorUnit.put("vmDiskRead", "B/s");
                monitorUnit.put("vmDiskWrite", "B/s");
                monitorUnit.put("vmNetworkIn", "");
                monitorUnit.put("vmNetworkOut", "");
                
                monitorUnit.put("pmCpuRate", "%");
                monitorUnit.put("pmMemoryRate", "%");
                monitorUnit.put("pmDiskRate", "%");
                monitorUnit.put("pmDiskRead", "KB/s");
                monitorUnit.put("pmDiskWrite", "KB/s");
                monitorUnit.put("pmNetworkIn", "Kbps");
                monitorUnit.put("pmNetworkOut", "Kbps");
                monitorUnit.put("pmConnNum", "个");
            }
        }
    }

    public static String getZbName(String zb) {
        return monitorZbNameMap.get(zb);
    }

    public static String getMenthodName(String method) {
        return methodMap.get(method);
    }

    public static String getUnitName(String zb) {
        return monitorUnit.get(zb);
    }
}