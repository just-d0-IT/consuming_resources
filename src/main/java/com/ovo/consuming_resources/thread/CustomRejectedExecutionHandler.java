package com.ovo.consuming_resources.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author ovo
 * @version 1.0.0
 * @ClassName CustomRejectedExecutionHandler.java
 * @Description TODO
 * @createTime 2023年07月09日 16:04:00
 */
public class CustomRejectedExecutionHandler implements RejectedExecutionHandler {
    private final static Logger logger = LoggerFactory.getLogger(CustomRejectedExecutionHandler.class);

    @Override
    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
        logger.error("任务被拒绝：{}", runnable);
    }
}