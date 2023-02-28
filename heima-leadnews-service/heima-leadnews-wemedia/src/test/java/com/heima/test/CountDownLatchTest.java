package com.heima.test;

import java.util.concurrent.CountDownLatch;

public class CountDownLatchTest {

    public static void main(String[] args) {
        int count = 5;

        CountDownLatch countDownLatch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            new WorkThread(i, countDownLatch).start();
        }

        try {
            countDownLatch.await();
            System.out.println(count + "个线程已运行完毕");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

class WorkThread extends Thread {
    private int seq;
    private CountDownLatch countDownLatch;

    public WorkThread(int seq, CountDownLatch countDownLatch) {
        this.seq = seq;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        try {
            System.out.println("线程" + this.seq + "开始运行...");
            Thread.sleep(2000);
            System.out.println("线程" + this.seq + "运行结束");

            countDownLatch.countDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
