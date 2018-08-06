package com.idleroom.web.cache;

import com.idleroom.web.controller.GlobalExceptionHandler;
import com.idleroom.web.dao.GoodDao;
import com.idleroom.web.entity.GoodEntity;
import com.idleroom.web.entity.Page;
import com.mongodb.ReadPreference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 由于性能问题，该类已废弃
 */

/**
 *缓存主键 list:type:status:sort  type类型; status 状态; sort 根据哪个字段排序
 * 缓存时分布式锁前缀为 list:lock:type:status
 * minHitRate 最小命中率 如果缓存命中率小于最小命中率放弃缓存
 * @author  Unrestraint
 */
@Component
@Deprecated
public class GoodIndexCache {
    private  static final Logger log = LoggerFactory.getLogger(GoodIndexCache.class);

    @Autowired
    private StringRedisTemplate template;
    @Autowired
    private RedisTemplate<String, Serializable> goodTemplate;
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    private GoodDao goodDao;


    /**
     *当超出范围时获取最后一页
     *
     * @param type
     * @param status
     * @param sort
     * @param page  从0开始计数
     * @param size
     * @return
     */
    public Page<?> listGood(String type, String status, String sort, long page, long size){
        Page p = new Page();
        //分页信息处理
        p.count = this.count(type,status,sort);
        if(page*size>=p.count){
            page=p.count/size;
        }
        p.curPage=page;
        p.pageSize=size;

        //从缓存中读取索引
        Set<String> set = this.listGoodIndex(type,status,sort,page,size);
        p.num = set.size();

        log.info(set.toString());

        //从缓存中读取商品信息
        p.content= this.listGood(set);

        if(p.content.contains(null)){
            //整理未命中缓存
            Map map = new HashMap((int)(p.num*1.3));//调整集合大小
            Iterator<String> it = set.iterator();
            for(int i=0;i<p.content.size();i++){
                String id = it.next();
                if(((List) p.content).get(i)==null){
                    map.put(id,id.split(":")[1]);
                }
            }
            //批量查库
            Iterable<GoodEntity> list = goodDao.findAllById(map.values());
            //整理数据
            for(GoodEntity g : list){
                map.replace("good:"+g.id,g);
            }
            //批量存入缓存
            goodTemplate.opsForValue().multiSet(map);

            //将数据库查询结果放在返回结果中
            it = set.iterator();
            for(int i=0;i<p.content.size();i++){
                String id=it.next();
                if(((List)p.content).get(i)==null){
                    ((List) p.content).set(i,map.get(id));
                }
            }
        }

        return  p;
    }

    /**
     * 返回总数
     * @param type
     * @param status
     * @param sort
     * @return
     */
    public long count(String type,String status,String sort){
        return template.opsForZSet().size("list:"+type+":"+status+":"+sort);
    }

    /**
     * 返回指定区间内的索引
     * @param type  商品类型
     * @param status 状态 为GoodEntity中的STTUS状态
     * @param sort  排序主键
     * @param page 第几页 从0开始
     * @param size 页面大小
     * @return
     */
    private Set<String> listGoodIndex(String type, String status, String sort, long page, long size){
        return template.opsForZSet().reverseRange("list:"+type+":"+status+":"+sort,page,(page+1)*size);
    }

    private List listGood(Collection<String> idList){
        List list = goodTemplate.opsForValue().multiGet(idList);
        return list;
    }


    /**
     * zset集合元素
     */
    public static class Tuple implements ZSetOperations.TypedTuple<String>{
        Double score;
        String value;
        public Tuple(Double score,String value){
            this.score=score;
            this.value=value;
        }
        @Override
        public String getValue() {
            return value;
        }

        @Override
        public Double getScore() {
            return score;
        }

        @Override
        public int compareTo(ZSetOperations.TypedTuple<String> o) {
            return  value.compareTo(o.getValue());
        }
        @Override
        public String toString(){
            return new StringBuilder().append(score).append(":").append(value).toString();
        }
    }

    /**
     * 对商品进行索引缓存
     */
    //@Scheduled(cron="0 * * * * *")
    public void cacheBegin(){
            cacheGoodIndex();
    }

    private void cacheGoodIndex(){
        try {
            //获取type属性中的种类
            List<String> types = new ArrayList<String>();
            mongoTemplate.getCollection(mongoTemplate.getCollectionName(GoodEntity.class)).distinct("type", String.class).spliterator().forEachRemaining(e -> types.add(e));
            log.info(types.toString());

            //商品状态
            List<String> status = new ArrayList<String>();
            status.add(GoodEntity.STATUS.SELL);
            status.add(GoodEntity.STATUS.WANT);

            //排序字段
            List<String> sorts = new ArrayList<String>();
            sorts.add("date");


            //缓存时分布式锁前缀为 list:lock:type:status:sort
            //逐个缓存
            status.forEach(s -> {
                types.forEach(t -> {

                    //获取分布式锁
                    String key = "list:lock" + ":" + t + ":" + s;
                    log.info("获取索引锁：" + key);
                    if(goodTemplate.opsForValue().setIfAbsent(key,1)){
                        //锁十分钟
                        goodTemplate.expire(key, 10, TimeUnit.MINUTES);

                        log.info("更新索引：" + key);

                        //查数据库组织数据
                        Set<ZSetOperations.TypedTuple<String>> dateSortSet = new HashSet<>();

                        Query query = new Query(Criteria.where("type").is(t).andOperator(Criteria.where("status").is(s)));
                        mongoTemplate.find(query, GoodEntity.class).forEach(e -> dateSortSet.add(new Tuple(Double.valueOf(e.date.getTime()), "good:"+e.id)));


                        //更新索引缓存
                        String cacheKey = "list:" + t + ":" + s + ":" + "date";
                        String indexCache="list:*:"+s+"date";
                        template.delete(cacheKey);

                        //log.info(dateSortSet.toString());
                        if (!dateSortSet.isEmpty()) {
                            template.opsForZSet().add(cacheKey, dateSortSet);
                            template.expire(cacheKey, 2, TimeUnit.DAYS);

                            template.opsForZSet().add(indexCache,dateSortSet);
                            template.expire(indexCache,2,TimeUnit.DAYS);
                        }

                    } else {
                        //获取锁失败表示有其他机器正在更新
                    }
                });
            });

        }catch (Throwable e){
            GlobalExceptionHandler.seriousError(e);
        }
    }
}
