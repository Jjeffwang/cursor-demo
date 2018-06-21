package com.cursor.demo.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class ExecuteService {

    Logger logger = LoggerFactory.getLogger(ExecuteService.class);

    protected ScheduledExecutorService scheduledService;

    protected int getServiceSize() {
        return 1;
    }

    protected void shutdown() {

    }

    /**
     * 关闭线程池
     */
    @PreDestroy
    public void destory() {
        shutdown();
        logger.info("Destory {}", this.getClass().getName());
        if (scheduledService != null) {
            scheduledService.shutdown();
            try {
                while (!scheduledService.awaitTermination(2, TimeUnit.SECONDS)) ;
            } catch (InterruptedException e) {
                logger.error("wait for termination error.", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 创建线程池
     */
    @PostConstruct
    public void execute() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    scheduledService = Executors.newScheduledThreadPool(getServiceSize());
                    doExecute();
                } catch (IOException e) {
                    logger.error("{} execute error. {}", this.getClass(), e);
                }
            }
        }).start();
    }

    abstract protected void doExecute() throws IOException;
}
