package com.eayun.common.zk.internal;

import java.io.IOException;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryUntilElapsed;
import org.springframework.stereotype.Component;

/**
 * 单例模式获取zookeeper对象
 * @author xiangyu.cao@eayun.com
 *
 */
@Component
public class CuratorSingleton {
    private static CuratorFramework client = null;
    private static final String     ZKHOST = ZKUtils.getEayunZookeeperHost();

    private CuratorSingleton() throws InterruptedException {
        if (client == null) {
            synchronized (CuratorSingleton.class) {
                if (client == null) {
                    start();
                }
            }
        }
    }

    /**
     * 单例模式获取CuratorFramework对象
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static CuratorFramework getInstance() throws InterruptedException {
        if (client == null) {
            synchronized (CuratorSingleton.class) {
                if (client == null) {
                    start();
                }
            }
        } else {
            if (client.getState().toString().equals("STOPPED")) {
                synchronized (CuratorSingleton.class) {
                    if (client.getState().toString().equals("STOPPED")) {
                        start();
                    }
                }
            }
        }
        return client;
    }

    private static void start() throws InterruptedException {
        RetryPolicy retryPolicy = new RetryUntilElapsed(30000, 1000);
        client = CuratorFrameworkFactory.newClient(ZKHOST, retryPolicy);
        client.start();
        client.getZookeeperClient().blockUntilConnectedOrTimedOut();
    }
}
