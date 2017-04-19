package com.eayun.common.zk;

public class DistributedLockBean {
    private String      granularity; //主节点地址,以谁为粒度加锁(例如按客户加锁,则传入cusId)
    private boolean     lockSuccess; //是否获取锁成功
    private LockService lockService; //具体调用方法
    private Object      result;     //方法返回结果

    public String getGranularity() {
        return granularity;
    }

    /**
     * 设置锁业务的粒度
     * 
     * @param granularity
     */
    public void setGranularity(String granularity) {
        this.granularity = "/" + granularity;
    }

    protected boolean isLockSuccess() {
        return lockSuccess;
    }

    protected void setLockSuccess(boolean lockSuccess) {
        this.lockSuccess = lockSuccess;
    }
    /**
     * 在锁内执行的操作
     * @return
     */
    public LockService getLockService() {
        return lockService;
    }

    public void setLockService(LockService lockService) {
        this.lockService = lockService;
    }

    protected Object getResult() {
        return result;
    }

    protected void setResult(Object result) {
        this.result = result;
    }

}
