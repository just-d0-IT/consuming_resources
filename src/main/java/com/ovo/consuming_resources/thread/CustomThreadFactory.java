package com.ovo.consuming_resources.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * @author ovo
 * @version 1.0.0
 * @ClassName CustomThreadFactory.java
 * @Description TODO
 * @createTime 2023年07月09日 16:06:00
 */

/**
 * 自定义线程工厂
 */
public class CustomThreadFactory implements ThreadFactory {

    private String threadNamePrefix;
    final AtomicInteger threadNumber = new AtomicInteger(1);

    public CustomThreadFactory(String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(runnable, threadNamePrefix + "-" + threadNumber.getAndIncrement());
        if (thread.isDaemon())
            thread.setDaemon(true);
        if (thread.getPriority() != Thread.NORM_PRIORITY)
            thread.setPriority(Thread.NORM_PRIORITY);
        return thread;
    }
}

