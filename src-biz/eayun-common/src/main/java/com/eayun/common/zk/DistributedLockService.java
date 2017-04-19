package com.eayun.common.zk;

import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.eayun.common.exception.CuratorException;
import com.eayun.common.util.TransactionHookUtil;
import com.eayun.common.util.TransactionHookUtil.CompletionHook;
import com.eayun.common.zk.internal.CuratorSingleton;

/**
 * 分布式锁(Curator)
 * 
 * @Author: xiangyu.cao
 * @Email: xiangyu.cao@eayun.com
 *
 */
@Component
public class DistributedLockService {

    private static final Logger log = LoggerFactory.getLogger(DistributedLockService.class);

    /**
     * 通过分布式锁执行相应的业务操作
     * 
     * @throws Exception
     */
    public Object doServiceByLock(DistributedLockBean dlBean) throws CuratorException {
        return doServiceByLock(dlBean,false);
    }
    
    private void releaseLock(InterProcessMutex lock, CuratorFramework client) {
        try {
            lock.release();
            log.debug("释放锁成功");
        } catch (Exception e) {
            client.close();
            log.error("释放锁失败", e);
        }
    }
    
    /**
     * 通过分布式锁执行相应的业务操作
     * 
     * @param dlBean
     * @param releaseLockAfterTranction 在事务提交后才释放锁。<br>警告：如果是true，需要注意这个方法不能在一个循环中调用，如果各循环体中包含相同的granularity，有死锁的危险！！！
     * @return
     * @throws CuratorException
     */
    public Object doServiceByLock(DistributedLockBean dlBean,boolean releaseLockAfterTransaction) throws CuratorException {
        try {
            final CuratorFramework client = CuratorSingleton.getInstance();
            final InterProcessMutex lock = new InterProcessMutex(client, dlBean.getGranularity());
            if (lock.acquire(60, TimeUnit.SECONDS)) {// 等待60s,如果60s未获得锁,则不再获取锁
                try {
                    log.debug("获取锁成功,开始执行业务操作");
                    Object result = dlBean.getLockService().doService();
                    dlBean.setResult(result);
                    dlBean.setLockSuccess(true);
                } finally {
                    if(releaseLockAfterTransaction && TransactionHookUtil.isInTransaction()){
                    	// 如果参数指定在事务之后释放，并且当前在事务中
                        TransactionHookUtil.registAfterCompletionHook(new CompletionHook() {
							@Override
							public void execute(int status) {
								releaseLock(lock, client);
							}
						});
                    }else{ 
                    	// 直接释放锁
                        releaseLock(lock, client);
                    }
                }
            }
            if (!dlBean.isLockSuccess()) {
                throw new CuratorException("获取锁失败");
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            throw new CuratorException(e.getMessage(), e);
        }
        return dlBean.getResult();
    }

}
