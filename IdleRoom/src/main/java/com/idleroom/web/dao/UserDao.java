package com.idleroom.web.dao;

import java.util.List;
import com.idleroom.web.annotation.Cache;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.idleroom.web.entity.UserEntity;
import org.springframework.data.mongodb.repository.Query;

/**
 * 锁前缀 user:lock
 * 非id 字段只返回部分，不可缓存
 *UerEntity 缓存主键格式 user:id
 *
 * @author Unrestraint
 */
public interface UserDao extends MongoRepository<UserEntity, String> {
    @Query(fields = "{'pwd':1,'type':1}")
    UserEntity findByPhone(String phone);

    @Query(fields = "{'pwd':1,'type':1}")
    UserEntity findByMail(String mail);

    boolean existsByMail(String mail);
    boolean existsByPhone(String phone);
    List<UserEntity> findByType(String type);

    /**
     * 未找到抛出 NoSuchElementException 异常
     */
    @Cache("user")
    default UserEntity getUserEntity(String id){
        return this.findById(id).get();
    }
}
