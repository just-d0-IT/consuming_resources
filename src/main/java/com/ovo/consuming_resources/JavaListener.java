package com.ovo.consuming_resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.util.Util;

import static com.ovo.consuming_resources.util.SystemUntil.*;
/**
 * @author ovo
 * @version 1.0.0
 * @ClassName JavaListener.java
 * @Description TODO
 * @createTime 2023年07月09日 16:03:00
 */

/**
 * CPU 内存 控制
 */
public class JavaListener {
    private final static Logger logger = LoggerFactory.getLogger(JavaListener.class);


    /**
     * 使用方式：
     * java -jar consuming_resources-1.0-SNAPSHOT.jar -c:50 -m:50
     * -c:50 表示cpu使用率50%左右 （注：使用率需为0-100之间的整数）
     * -m:50 表示内存使用率50%左右（注：使用率需为0-100之间的整数）
     *
     * @param args
     * @return void
     * @author sun
     */
    public static void main(String[] args) {
        if (args == null || args.length < 1) {
            exit();
        }
        int expectCpuUsage = 0, expectMemoryUsage = 0;
        for (String param : args) {
            if (param.startsWith("-c:")) {
                // cpu参数
                param = param.substring(3).trim();
                if (param != null && param.length() > 0) {
                    expectCpuUsage = Integer.parseInt(param);
                }
            }
            if (param.startsWith("-m:")) {
                // 内存参数
                param = param.substring(3).trim();
                if (param != null && param.length() > 0) {
                    expectMemoryUsage = Integer.parseInt(param);
                }
            }
        }

        if ((expectCpuUsage <= 0 || expectCpuUsage >= 100) && (expectMemoryUsage <= 0 || expectMemoryUsage >= 100)) {
            exit();
        }

        threadPoolExecutor.execute(() -> {
            while (true) {
                getSystemUsage();
            }
        });
        Util.sleep(10 * TIME);
        int finalExpectCpuUsage = expectCpuUsage;
        threadPoolExecutor.execute(() -> {
            while (true) {
                try {
                    calcCpu(finalExpectCpuUsage);
                    Util.sleep(TIME);
                } catch (Exception exception) {
                    logger.error("calcCpu error...", exception);
                }
            }
        });
        int finalExpectMemoryUsage = expectMemoryUsage;
        threadPoolExecutor.execute(() -> {
            while (true) {
                try {
                    calcMemory(finalExpectMemoryUsage);
                    Util.sleep(30 * TIME);
                } catch (Exception exception) {
                    logger.error("calcMemory error...", exception);
                }
            }
        });
    }

    private static void exit() {
        System.out.println("请输入参数");
        System.out.println("例如: java -jar consuming_resources-1.0-SNAPSHOT.jar -c:50 -m:50");
        System.out.println("-c表示控制核心，-m表示控制内存");
        System.out.println("-c:50 表示cpu使用率50%左右 （注：使用率需为0-100之间的整数）");
        System.out.println("-m:50 表示内存使用率50%左右（注：使用率需为0-100之间的整数）");
        System.exit(0);
    }

}
