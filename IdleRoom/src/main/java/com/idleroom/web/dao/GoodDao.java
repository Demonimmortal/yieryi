package com.idleroom.web.dao;

import java.util.Date;
import com.idleroom.web.annotation.Cache;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.idleroom.web.entity.GoodEntity;
import org.springframework.data.mongodb.repository.Query;

/**
 * 锁前缀 good:lock
 * GooEntity 缓存主键格式 good:id
 * @author  Unrestraint
 */
public interface GoodDao extends MongoRepository<GoodEntity, String> {
    /**
     * 未找到抛出 NoSuchElementException 异常
     */
    @Cache("good")
    default  GoodEntity getGoodEntity(String id){
        return this.findById(id).get();
    }
    @Query("{$or:[{'title':{$regex:?1}},{'info':{$regex:?2}}],'status':?0}")
    Page<GoodEntity> findByStatusEqualsAndTitleLikeOrInfoLike(String status, String title, String info, Pageable page);
	Page<GoodEntity> findByDateGreaterThan(Date date,Pageable page);

	Page<GoodEntity> findByStatusEquals(String status,Pageable page);
	Page<GoodEntity> findAllByTypeInAndStatusEquals(Iterable<String> iterable,String status,Pageable page);
	Page<GoodEntity> findAllByStatusInAndTargetEquals(Iterable<String> iterable,String target,Pageable page);
}