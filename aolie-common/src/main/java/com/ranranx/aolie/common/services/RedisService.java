package com.ranranx.aolie.common.services;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/8 0008 21:22
 **/

import com.ranranx.aolie.common.interfaces.SessionStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * spring redis 工具类
 **/
@SuppressWarnings(value = {"unchecked", "rawtypes"})
@Service
public class RedisService implements SessionStoreService {
    Logger logger = LoggerFactory.getLogger(RedisService.class);


    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate redisTemplate;


    /**
     * 模糊查询key
     *
     * @return
     */
    public Set<String> listKeys(final String key) {
        Set<String> keys = redisTemplate.keys(key);
        return keys;
    }

    /**
     * 重命名
     */
    public void rename(final String oldKey, final String newKey) {
        redisTemplate.rename(oldKey, newKey);
    }

    /**
     * 模糊获取
     *
     * @param pattern
     * @return
     */
    public List<Object> listPattern(final String pattern) {
        List<Object> result = new ArrayList<>();
        Set<Serializable> keys = redisTemplate.keys(pattern);
        for (Serializable str : keys) {
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            Object obj = operations.get(str.toString());
            if (obj != null) {
                result.add(obj);
            }
        }
        return result;
    }


    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @return
     */
    public boolean set(final String key, Object value) {
        boolean result = false;
        try {
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            operations.set(key, value);
            result = true;
        } catch (Exception e) {
            logger.error("set fail ,key is:" + key, e);
        }
        return result;
    }


    /**
     * 批量写入缓存
     *
     * @param map
     * @return
     */
    public boolean multiSet(Map<String, Object> map) {
        boolean result = false;
        try {
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            operations.multiSet(map);
            result = true;
        } catch (Exception e) {
            logger.error("multiSet fail ", e);
        }
        return result;
    }

    /**
     * 集合出栈
     *
     * @param key
     */
    public Object leftPop(String key) {
        ListOperations list = redisTemplate.opsForList();
        return list.leftPop(key);
    }

    public Object llen(final String key) {
        final ListOperations list = this.redisTemplate.opsForList();
        return list.size((Object) key);
    }

    /**
     * 写入缓存设置时效时间
     *
     * @param key
     * @param value
     * @return
     */
    public boolean set(final String key, Object value, Long expireTime) {
        boolean result = false;
        try {
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            operations.set(key, value);
            redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
            result = true;
        } catch (Exception e) {
            logger.error("set fail ", e);
        }
        return result;
    }

    /**
     * 写入缓存设置时效时间
     *
     * @param key
     * @param value
     * @return
     */
    public boolean setnx(final String key, Object value, Long expireTime) {
        boolean res = false;
        try {
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            res = operations.setIfAbsent(key, value);
            if (res) {
                redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            logger.error("setnx fail ", e);
        }
        return res;
    }

    /**
     * 缓存设置时效时间
     *
     * @param key
     * @param expireTime
     * @return
     */
    public void expire(final String key, Long expireTime) {
        redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
    }


    /**
     * 自增操作
     *
     * @param key
     * @return
     */
    public long incr(final String key) {
        RedisAtomicLong entityIdCounter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        return entityIdCounter.getAndIncrement();

    }


    /**
     * 批量删除
     *
     * @param keys
     */
    public void removeKeys(final List<String> keys) {
        if (keys.size() > 0) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 批量删除key
     *
     * @param pattern
     */
    public void removePattern(final String pattern) {
        Set<Serializable> keys = redisTemplate.keys(pattern);
        if (keys.size() > 0) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 删除对应的value
     *
     * @param key
     */
    public void remove(final String key) {
        if (exists(key)) {
            redisTemplate.delete(key);
        }
    }

    /**
     * 判断缓存中是否有对应的value
     *
     * @param key
     * @return
     */
    public boolean exists(final String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 判断缓存中是否有对应的value(模糊匹配)
     *
     * @param pattern
     * @return
     */
    public boolean existsPattern(final String pattern) {
        if (redisTemplate.keys(pattern).size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 读取缓存
     *
     * @param key
     * @return
     */
    public Object get(final String key) {
        ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
        return operations.get(key);
    }

    /**
     * 哈希 添加
     */
    public void hmSet(String key, Object hashKey, Object value) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        hash.put(key, hashKey, value);
    }

    /**
     * 哈希 添加
     */
    public Boolean hmSet(String key, Object hashKey, Object value, Long expireTime, TimeUnit timeUnit) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        hash.put(key, hashKey, value);
        return redisTemplate.expire(key, expireTime, timeUnit);
    }

    /**
     * 哈希获取数据
     *
     * @param key
     * @param hashKey
     * @return
     */
    public Object hmGet(String key, Object hashKey) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        return hash.get(key, hashKey);
    }

    /**
     * 哈希获取所有数据
     *
     * @param key
     * @return
     */
    public Object hmGetValues(String key) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        return hash.values(key);
    }

    /**
     * 哈希获取所有键值
     *
     * @param key
     * @return
     */
    public Object hmGetKeys(String key) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        return hash.keys(key);
    }

    /**
     * 哈希获取所有键值对
     *
     * @param key
     * @return
     */
    public Object hmGetMap(String key) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        return hash.entries(key);
    }

    /**
     * 哈希 删除域
     *
     * @param key
     * @param hashKey
     */
    public Long hdel(String key, Object hashKey) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        return hash.delete(key, hashKey);
    }

    /**
     * 列表添加
     *
     * @param k
     * @param v
     */
    public void rPush(String k, Object v) {
        ListOperations<String, Object> list = redisTemplate.opsForList();
        list.rightPush(k, v);
    }

    /**
     * 列表删除
     *
     * @param k
     * @param v
     */
    public void listRemove(String k, Object v) {
        ListOperations<String, Object> list = redisTemplate.opsForList();
        list.remove(k, 1, v);
    }

    public void rPushAll(String k, Collection var2) {
        ListOperations<String, Object> list = redisTemplate.opsForList();
        list.rightPushAll(k, var2);
    }


    /**
     * 列表获取
     *
     * @param k
     * @param begin
     * @param end
     * @return
     */
    public Object lRange(String k, long begin, long end) {
        ListOperations<String, Object> list = redisTemplate.opsForList();
        return list.range(k, begin, end);
    }

    /**
     * 集合添加
     *
     * @param key
     * @param value
     */
    public void add(String key, Object value) {
        SetOperations<String, Object> set = redisTemplate.opsForSet();
        set.add(key, value);
    }


    /**
     * 判断元素是否在集合中
     *
     * @param key
     * @param value
     */
    public Boolean isMember(String key, Object value) {
        SetOperations<String, Object> set = redisTemplate.opsForSet();
        return set.isMember(key, value);
    }


    /**
     * 集合获取
     *
     * @param key
     * @return
     */
    public Set<Object> setMembers(String key) {
        SetOperations<String, Object> set = redisTemplate.opsForSet();
        return set.members(key);
    }

    /**
     * 有序集合添加
     *
     * @param key
     * @param value
     * @param scoure
     */
    public void zAdd(String key, Object value, double scoure) {
        ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
        zset.add(key, value, scoure);
    }

    /**
     * 有序集合获取
     *
     * @param key
     * @param scoure
     * @param scoure1
     * @return
     */
    public Set<Object> rangeByScore(String key, double scoure, double scoure1) {
        ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
        return zset.rangeByScore(key, scoure, scoure1);
    }

    /**
     * 有序集合根据区间删除
     *
     * @param key
     * @param scoure
     * @param scoure1
     * @return
     */
    public void removeRangeByScore(String key, double scoure, double scoure1) {
        ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
        zset.removeRangeByScore(key, scoure, scoure1);
    }

    /**
     * 列表添加
     *
     * @param k
     * @param v
     */
    public void lPush(String k, Object v) {
        ListOperations<String, Object> list = redisTemplate.opsForList();
        list.rightPush(k, v);
    }

    /**
     * 获取当前key的超时时间
     *
     * @param key
     * @return
     */
    public Long getExpireTime(final String key) {
        return redisTemplate.opsForValue().getOperations().getExpire(key, TimeUnit.SECONDS);
    }

    public Long extendExpireTime(final String key, Long extendTime) {
        Long curTime = redisTemplate.opsForValue().getOperations().getExpire(key, TimeUnit.SECONDS);
        long total = curTime.longValue() + extendTime;
        redisTemplate.expire(key, total, TimeUnit.SECONDS);
        return total;
    }

    public Set getKeys(String k) {
        return redisTemplate.keys(k);
    }

    @Override
    public String getValue(String key) {
        return (String) get(key);
    }

    @Override
    public void setValue(String key, String value) {
        set(key, value);
    }

    @Override
    public void setValue(String key, String value, long timeout) {
        set(key, value, timeout);
    }

    /**
     * 写入缓存设置时效时间
     *
     * @param key
     * @param expireTime
     * @return
     */
    @Override
    public void setExpire(String key, Long expireTime) {
        expire(key, expireTime);
    }
}

