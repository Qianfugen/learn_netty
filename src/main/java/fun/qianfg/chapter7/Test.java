package fun.qianfg.chapter7;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Function:
 *
 * @author qianfg
 * @date 2022/1/2 16:27
 * @Email: 287541326@qq.com
 */
public class Test {
    public static void main(String[] args) {
        //10s后执行任务，一旦调度任务完成，就会关闭ScheduledExecutorService以释放资源
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);
        executor.schedule(() -> System.out.println("10 seconds later"), 10, TimeUnit.SECONDS);
        executor.shutdown();
    }
}
