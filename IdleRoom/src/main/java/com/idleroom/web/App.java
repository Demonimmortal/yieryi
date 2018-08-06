package com.idleroom.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

//@MapperScan("com.idleroom.web.*")
//@EnableCaching
//@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 60*60*2)
@SpringBootApplication(exclude = MongoAutoConfiguration.class)
public class App   {

    public static void main( String[] args )
    {
        SpringApplication.run(App.class,args);
    }

    /**
     * 配置线程池
     * @return
     */
    @Bean
    public TaskScheduler taskScheduler(){
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(2);
        return taskScheduler;
    }
}
