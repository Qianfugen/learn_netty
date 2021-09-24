package Thermo;

import lombok.SneakyThrows;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CountDownLatchTest {

    private static CountDownLatch latch;

    public static void main(String[] args) {

        System.out.println("主线程开始执行…… ……");

        //第一个子线程执行
        ExecutorService es1 = Executors.newSingleThreadExecutor();
        es1.execute(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    latch = new CountDownLatch(1);
                    System.out.println("发送数据：" + i);
                    latch.await();
                }
            }
        });
        es1.shutdown();

        //第二个子线程执行
        ExecutorService es2 = Executors.newSingleThreadExecutor();
        es2.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 10; i++) {
                        Thread.sleep(1000);
                        System.out.println("解开同步锁");
                        latch.countDown();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        es2.shutdown();
    }
}