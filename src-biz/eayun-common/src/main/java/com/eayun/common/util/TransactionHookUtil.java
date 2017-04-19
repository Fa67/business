package com.eayun.common.util;

import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Spring事务钩子工具
 * 
 * <p>注册Spring事务 钩子逻辑</p>
 * 
 * @author zhujun
 * @date 2016年9月13日
 *
 */
public abstract class TransactionHookUtil {

	/**
	 * 注册 事务成功提交后 逻辑
	 * @author zhujun
	 * @date 2016年9月13日
	 *
	 * @param hook
	 */
	public static void registAfterCommitHook(final Hook hook) {
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter(){
			@Override
			public void afterCommit() {
				hook.execute();
			}
		});
	}
	
	/**
	 * 注册事务完成（包括 提交、回滚）后逻辑
	 * 
	 * @author zhujun
	 * @date 2016年11月2日
	 *
	 * @param hook
	 */
	public static void registAfterCompletionHook(final CompletionHook hook) {
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter(){
			@Override
			public void afterCompletion(int status) {
				hook.execute(status);
			}
		});
	}
	
	/**
	 * 当前是否在数据库事务中
	 * 
	 * @author zhujun
	 * @date 2016年11月2日
	 *
	 * @return
	 */
	public static boolean isInTransaction() {
		return TransactionSynchronizationManager.isActualTransactionActive();
	}
	
	
	/**
	 * 钩子逻辑
	 * 
	 * @author zhujun
	 * @date 2016年9月13日
	 *
	 */
	public interface Hook {
		void execute();
	}
	
	/**
	 * 事务完成钩子
	 * 
	 * @author zhujun
	 * @date 2016年11月2日
	 *
	 */
	public interface CompletionHook {
		/**
		 * 
		 * @param status STATUS_COMMITTED = 0,STATUS_ROLLED_BACK = 1,STATUS_UNKNOWN = 2
		 * <p>status常量在类：org.springframework.transaction.support.TransactionSynchronization</p>
		 * 
		 */
		void execute(int status);
	}
	
}
