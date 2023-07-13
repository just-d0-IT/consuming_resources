package com.ovo.consuming_resources.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ovo.consuming_resources.thread.ThreadPoolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.util.Util;

import java.text.DecimalFormat;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author ovo
 * @version 1.0.0
 * @ClassName SystemUntil.java
 * @Description TODO
 * @createTime 2023年07月09日 16:13:00
 */
public class SystemUntil {
    private final static Logger logger = LoggerFactory.getLogger(SystemUntil.class);

    // 定义时间片大小（毫秒）
    public static final int TIME = 1000;
    // 100兆
    static final int MB100 = 104857600;

    private static SystemInfo systemInfo = new SystemInfo();
    //获取硬件信息 【硬件抽象层。提供对处理器、内存、电池和磁盘等硬件项目的访问。】
    private static HardwareAbstractionLayer hardware = systemInfo.getHardware();
    //cpu核数
    static int cpuNum = hardware.getProcessor().getLogicalProcessorCount();

    // cpu使用率
    static volatile int cpuUsage = 100;
    // 内存使用率
    static volatile int memoryUsage = 100;
    // 所需消耗的cpu百分比
    static volatile int consumingCpu = 0;
    // 所需消耗的内存百分比
    static int consumingMemory;
    // 系统总内存
    static long totalPhysicalMemorySize = hardware.getMemory().getTotal();

    static Vector vector = new Vector();
    public static ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.getThreadPoolExecutor(cpuNum, cpuNum * 2, cpuNum + 10, "consuming_resource");

    public static void getSystemUsage() {
        getCpuInfo();
        getMemInfo();
        getJvmInfo();
        System.out.println("================================================================================================");
    }

    public static void calcCpu(int expectCpuUsage) {
        if (expectCpuUsage <= cpuUsage - consumingCpu) {
            logger.info("no need consuming cpu");
            return;
        }
        consumingCpu += expectCpuUsage - cpuUsage;
        logger.info("need consuming cpu percent is {}%", consumingCpu);
        for (int i = 0; i < cpuNum; i++) {
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    lineGraph(consumingCpu);
                }
            });
        }
    }

    public static void calcMemory(int expectMemoryUsage) {
        if (expectMemoryUsage <= memoryUsage) {
            logger.info("no need consuming memory");
            return;
        }
        vector.clear();
        System.gc();
        long byteSize = totalPhysicalMemorySize * (expectMemoryUsage - memoryUsage) / 100;
        logger.info("need consuming memory formatByteSize is {},byteSize is {}", formatByte(byteSize), byteSize);
        while (byteSize != 0) {
            byte b1[] = null;
            if (byteSize > MB100) {
                b1 = new byte[MB100];
                byteSize -= MB100;
            } else {
                b1 = new byte[(int) byteSize];
                byteSize = 0;
            }
            vector.add(b1);
        }
    }

    /**
     * 占用固定比例CPU
     *
     * @param rate 比例
     */
    private static void lineGraph(int rate) {
        double percent = Double.valueOf(rate) / 100;
        // 1. 调用做点简单工作的方法
        doSomeSimpleWork(percent * TIME);
        // 2. 线程休眠
        // try {
        //     long sleep = (long) (TIME - percent * TIME);
        //     if (sleep < 1) {
        //         sleep = 1L;
        //     }
        //     Thread.sleep(sleep);
        // } catch (Exception exception) {
        //     exception.printStackTrace();
        // }
    }

    /**
     * 占用CPU方法
     *
     * @param time
     */
    private static void doSomeSimpleWork(double time) {
        // 计算当前时间和开始时间的差值是否小于时间片的比例
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < time) {
            // do nothing
            // logger.info(Thread.currentThread().getName() + "running----");
        }
    }

    /**
     * 获取CPU相关信息
     *
     * @return
     */
    public static void getCpuInfo() {
        // CPU信息
        JSONObject cpuInfo = new JSONObject();
        CentralProcessor processor = hardware.getProcessor();
        // 第一次获取：获取系统范围的 CPU 负载滴答计数器：
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        //等待一段时间
        Util.sleep(TIME);
        //第二次获取：获取系统范围的 CPU 负载滴答计数器：
        long[] ticks = processor.getSystemCpuLoadTicks();


        // ============================== 计算相关性能指标: ==========================================.
        //  计算出该时间间隔内的 CPU 负载
        //  Nice 和 IOWait 信息在 Windows 上不可用，
        // 而 IOwait 和 IRQ 信息在 macOS 上不可用，
        // 因此这些滴答始终为零

        //时间段内：用户态的CPU时间
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        //时间段内：nice值为负的进程所占用的CPU时间
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        //时间段内：内核态CPU时间
        long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        //时间段内：空闲时间，除硬盘IO等待时间以外其它等待时间
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        //时间段内：等待I/O的CPU时间
        long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        //时间段内：硬中断的CPU时间
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        //时间段内：软中断的CPU时间
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        //时间段内：当系统运行在虚拟机中的时候，被其他虚拟机占用的CPU时间
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];

        //时间段内：CPU时间
        long totalCpu = user + nice + cSys + idle + iowait + irq + softirq + steal;

        //cpu供应商
        //cpuInfo.put("cpuVendor", processor.getProcessorIdentifier().getVendor());
        //cpu名称
        //cpuInfo.put("cpuName", processor.getProcessorIdentifier().getName());

        cpuInfo.put("cpuNum", cpuNum);

        cpuInfo.put("user", new DecimalFormat("#.##%").format(user * 1.0 / totalCpu));
        cpuInfo.put("cSys", new DecimalFormat("#.##%").format(cSys * 1.0 / totalCpu));
        cpuInfo.put("idle", new DecimalFormat("#.##%").format(idle * 1.0 / totalCpu));
        cpuInfo.put("iowait", new DecimalFormat("#.##%").format(iowait * 1.0 / totalCpu));

        cpuInfo.put("nice", new DecimalFormat("#.##%").format(nice * 1.0 / totalCpu));
        cpuInfo.put("irq", new DecimalFormat("#.##%").format(irq * 1.0 / totalCpu));
        cpuInfo.put("softirq", new DecimalFormat("#.##%").format(softirq * 1.0 / totalCpu));
        cpuInfo.put("steal", new DecimalFormat("#.##%").format(steal * 1.0 / totalCpu));
        cpuInfo.put("cpuLoad", new DecimalFormat("#.##%").format(1 - idle * 1.0 / totalCpu));
        cpuUsage = (int) (processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100);
        cpuInfo.put("cpuUsage", cpuUsage);
        cpuInfo.put("consumingCpu", consumingCpu);
        logger.info(JSON.toJSONString(cpuInfo));
    }

    /**
     * 系统内存信息
     */
    public static void getMemInfo() {
        JSONObject memInfo = new JSONObject();

        // ============================== 获取相关性能指标: ==========================================.
        GlobalMemory memory = hardware.getMemory();
        //总内存
        long totalByte = memory.getTotal();
        //剩余【当前可用的物理内存量】
        long availableByte = memory.getAvailable();
        //已使用
        long useByte = totalByte - availableByte;
        //公共变量赋值
        double usageRate = (useByte) * 1.0 / totalByte;
        memoryUsage = (int) (usageRate * 100);
        memInfo.put("memoryUsage", memoryUsage);

        //总内存
        memInfo.put("total", formatByte(totalByte));
        //使用内存
        memInfo.put("used", formatByte(useByte));
        //剩余内存
        memInfo.put("free", formatByte(availableByte));

        //空闲率
        memInfo.put("freeRate", new DecimalFormat("#.##%").format((availableByte) * 1.0 / totalByte));
        //使用率
        memInfo.put("usageRate", new DecimalFormat("#.##%").format(usageRate));

        logger.info(JSON.toJSONString(memInfo));
    }

    /**
     * 系统jvm信息
     */
    public static void getJvmInfo() {
        JSONObject jvmInfo = new JSONObject();
        Properties props = System.getProperties();

        // ============================== 获取相关性能指标: ==========================================.
        Runtime runtime = Runtime.getRuntime();
        long jvmTotalMemoryByte = runtime.totalMemory();
        long jvmFreeMemoryByte = runtime.freeMemory();
        long jvmUseMemoryByte = jvmTotalMemoryByte - jvmFreeMemoryByte;
        long jvmMaxMemory = runtime.maxMemory();
        //jvm总内存
        jvmInfo.put("total", formatByte(jvmTotalMemoryByte));
        //空闲空间
        jvmInfo.put("free", formatByte(jvmFreeMemoryByte));
        //jvm已使用内存
        jvmInfo.put("user", formatByte(jvmUseMemoryByte));
        //jvm最大可申请
        jvmInfo.put("max", formatByte(jvmMaxMemory));

        //jvm内存使用率
        jvmInfo.put("usageRate", new DecimalFormat("#.##%").format((jvmUseMemoryByte) * 1.0 / jvmTotalMemoryByte));
        //jvm空闲
        jvmInfo.put("freeRate", new DecimalFormat("#.##%").format((jvmFreeMemoryByte) * 1.0 / jvmTotalMemoryByte));

        //jdk版本
        jvmInfo.put("jdkVersion", props.getProperty("java.version"));
        //jdk路径
        jvmInfo.put("jdkHome", props.getProperty("java.home"));
        logger.info(JSON.toJSONString(jvmInfo));
    }


    /**
     * 该方法将字节数转换为可读的内存大小单位（KB、MB、GB等）进行显示。
     * 单位转换
     *
     * @param byteNumber
     * @return
     */
    private static String formatByte(long byteNumber) {
        //换算单位
        double FORMAT = 1024.0;
        double kbNumber = byteNumber / FORMAT;
        if (kbNumber < FORMAT) {
            return new DecimalFormat("#.##KB").format(kbNumber);
        }
        double mbNumber = kbNumber / FORMAT;
        if (mbNumber < FORMAT) {
            return new DecimalFormat("#.##MB").format(mbNumber);
        }
        double gbNumber = mbNumber / FORMAT;
        if (gbNumber < FORMAT) {
            return new DecimalFormat("#.##GB").format(gbNumber);
        }
        double tbNumber = gbNumber / FORMAT;
        return new DecimalFormat("#.##TB").format(tbNumber);
    }
}
