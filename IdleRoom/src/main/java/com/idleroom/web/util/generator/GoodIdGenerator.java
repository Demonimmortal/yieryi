package com.idleroom.web.util.generator;

import com.idleroom.web.entity.SequenceId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import javax.annotation.Resource;

/**
 * 根据数据库 sequence 表进行原子操作生成自增长id
 *
 * @author Unrestraint
 */
@Configuration
public class GoodIdGenerator implements IdGenerator {

    @Resource
    public MongoTemplate mongoTemplate;

    @Override
    public String getNextId() {
        Query query = new Query(Criteria.where("collName").is("good"));
        Update update = new Update();
        update.inc("seqId",1);
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.upsert(true);
        options.returnNew(true);
        SequenceId id =  mongoTemplate.findAndModify(query,update,SequenceId.class);
        return String.valueOf(id.seqId);
    }

    @Bean(name = "goodId")
    public IdGenerator get(MongoTemplate mongoTemplate){
        GoodIdGenerator id = new GoodIdGenerator();
        id.mongoTemplate = mongoTemplate;
        return id;
    }
}
