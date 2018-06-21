package com.cursor.demo.config;

//import com.cursor.demo.controller.TestController;
import com.cursor.demo.thread.MyThread;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * ETL插件类
 *
 * @author
 */
@Configuration
@EnableScheduling
public class PluginsConfig {


    /**
     * 名单任务开关
     */
    @Value("${push.namelist.enable:true}")
    private boolean nameEnable;

    /**
     * 后台管理名单任务
     *
     * @return
     */
//    @Bean
//    public TestController nameListLoaderTask() {
//        if (nameEnable) {
//            return new TestController();
//        } else {
//            return null;
//        }
//    }

    @Bean
    public MyThread mythread() {
        return new MyThread();
    }
}
