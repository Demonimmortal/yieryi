package com.idleroom.web.dao;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import javax.annotation.PreDestroy;

import com.mongodb.ReadPreference;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.mongo.MongoClientFactory;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * 配置mongodb模式：读取最近、响应速度最快的读写分离模式
 * 由于未找到任何相关参考，所以直接替换了源码中的bean配置，通过将参数写死的方式完成读写分离
 *
 * @author  Unrestraint
 */
@Configuration
@ConditionalOnClass({MongoClient.class})
@EnableConfigurationProperties({MongoProperties.class})
@ConditionalOnMissingBean(
        type = {"org.springframework.data.mongodb.MongoDbFactory"}
)
public class MongoAutoConfiguration {
    private final MongoClientOptions options;
    private final MongoClientFactory factory;
    private MongoClient mongo;

    //忽略错误
    public MongoAutoConfiguration(MongoProperties properties, ObjectProvider<MongoClientOptions> options, Environment environment) {
        MongoClientOptions option = (MongoClientOptions)options.getIfAvailable();
        this.options = new MongoClientOptions.Builder().readPreference(ReadPreference.nearest()).build();
        this.factory = new MongoClientFactory(properties, environment);
    }

    @PreDestroy
    public void close() {
        if (this.mongo != null) {
            this.mongo.close();
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public MongoClient mongo() {
        this.mongo = this.factory.createMongoClient(this.options);
        return this.mongo;
    }
}
