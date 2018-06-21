package com.cursor.demo.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;



@Configuration
public class CursorDemoConfig {

    @Bean("dataSource")
    @Primary
    @ConfigurationProperties(prefix = "cursor")
    public DataSource dataSource(){
        return DataSourceBuilder.create().build();
    }



    @Bean
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setConfigLocation(new ClassPathResource(String.format("MybatisConfigMysql.xml", "MySql")));
        return bean.getObject();
    }

    @Bean(destroyMethod = "shutdown", name = "threadPool")
    public ThreadPoolExecutor threadPool() {
        ThreadFactory tf = new ThreadFactoryBuilder().setNameFormat("扫描数据线程-%d").build();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10, tf);
        return executor;
    }
}
