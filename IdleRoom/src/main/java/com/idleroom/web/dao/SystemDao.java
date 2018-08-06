package com.idleroom.web.dao;

import com.idleroom.web.entity.SystemEntity;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;


/**
 * 锁前缀 sys:lock
 * 不缓存
 * 系统举报处理 相关Dao层
 * @author Unrestriant
 */

public interface SystemDao extends MongoRepository<SystemEntity, String> {
    /**
     * 未找到抛出 NoSuchElementException 异常
     */
    default SystemEntity getSystemEntity(String id){
        return this.findById(id).get();
    }

    Page<SystemEntity> findAllByStatusIn(Iterable it,Example<? extends SystemEntity> example, Pageable page);

}
