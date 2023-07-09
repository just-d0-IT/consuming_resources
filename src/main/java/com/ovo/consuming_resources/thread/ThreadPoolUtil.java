package com.ovo.consuming_resources.thread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
/**
 * @author ovo
 * @version 1.0.0
 * @ClassName ThreadPoolUtil.java
 * @Description TODO
 * @createTime 2023年07月09日 16:07:00
 */

/**
 * 自定义线程池，业务线程名称可自定义
 */
public class ThreadPoolUtil {
    /**
     * @param businessName 业务名称
     * @return 线程池
     */
    public static ThreadPoolExecutor getThreadPoolExecutor(int coreSize, int maxSize, int blockingSize, String businessName) {
        //阻塞队列
        BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(blockingSize);
        //创建线程池  核心线程数为coreSize  最大线程数为maxSize 非核心线程空闲存活时间为60s
        return new ThreadPoolExecutor(
                // 核心线程数
                coreSize,
                // 最大线程数
                maxSize,
                // 线程空闲时间
                1,
                // 空闲时间单位
                TimeUnit.MINUTES,
                blockingQueue,
                // 创建自定义线程工厂
                new CustomThreadFactory(businessName),
                // 创建自定义拒绝策略
                new CustomRejectedExecutionHandler());
    }
}