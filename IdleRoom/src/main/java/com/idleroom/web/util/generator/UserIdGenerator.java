package com.idleroom.web.util.generator;

import com.idleroom.web.entity.SequenceId;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class UserIdGenerator implements IdGenerator  {
    @Resource
    public MongoTemplate mongoTemplate;

    @Override
    public String getNextId() {
        Query query = new Query(Criteria.where("collName").is("user"));
        Update update = new Update();
        update.inc("seqId",1);
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.upsert(true);
        options.returnNew(true);
        SequenceId id =  mongoTemplate.findAndModify(query,update,SequenceId.class);
        return String.valueOf(id.seqId);
    }
    @Bean(name = "userId")
    public IdGenerator get(MongoTemplate mongoTemplate){
        UserIdGenerator id = new UserIdGenerator();
        id.mongoTemplate = mongoTemplate;
        return id;
    }
}
