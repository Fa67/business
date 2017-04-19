package com.eayun.common.redis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;

/**
 * Redis官方首选Java客户端--Jedis工具类，单例模式
 *                       
 * @Filename: JedisUtil.java
 * @Description: 提供对Redis K-V、列表和集合的操作
 * @Version: 1.0
 * @Author: fan.zhang
 * @Email: fan.zhang@eayun.com
 * @History:<br>
 *<li>Date: 2015年10月28日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Component
public class JedisUtil {
    private static final Logger log = LoggerFactory.getLogger(JedisUtil.class);
    
    private static JedisUtil instance;
    
    private JedisUtil(){
        instance = this;
    }
    
    public static JedisUtil getInstance(){
        return instance;
    }

    @Autowired
    protected RedisTemplate<Serializable, Serializable> redisTemplate;

    /**
     * 获取符合某种pattern的key的集合
     * @param pattern
     * @return
     * @throws Exception
     */
    public Set<String> keys(final String pattern) throws Exception{

        return redisTemplate.execute(new RedisCallback<Set<String>>() {

            @Override
            public Set<String> doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                Set<byte[]> set = connection.keys(serializer.serialize(pattern));
                Set<String> valueSet = new HashSet<String>();
                for (byte[] bytes : set) {
                    valueSet.add(serializer.deserialize(bytes));
                }
                return valueSet;
            }
        });
    }
    
    /**
     * 获取Key的过期时间
     * @param key
     * @return
     * @throws Exception
     */
    public Long getExpire(final String key) throws Exception{
    	return redisTemplate.execute(new RedisCallback<Long>(){
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                return connection.ttl(serializer.serialize(key));
            }
        });
    }
    
    /**
     * 判断给定Key是否存在
     * @param key
     * @return true：存在；false：不存在
     */
    public boolean isKeyExisted(final String key) {
        
        return redisTemplate.execute(new RedisCallback<Boolean>(){
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                return connection.exists(serializer.serialize(key));
            }
            
        });
    }

    /**
     * 为存在的key设置过期时间(单位：s)
     * @param key
     * @param seconds
     * @return
     */
    public boolean expireKey(final String key, final long seconds){
        return redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                return connection.expire(serializer.serialize(key), seconds);
            }
        });
    }
    /**
     * 获取指定key的数据
     * @param key
     * @return value
     * @throws Exception
     */
    public String get(final String key) throws Exception {

        return redisTemplate.execute(new RedisCallback<String>() {

            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                byte[] value = connection.get(serializer.serialize(key));

                return serializer.deserialize(value);
            }
        });
    }
    
    public byte[] getBytes(final String key) throws Exception {

        return redisTemplate.execute(new RedisCallback<byte[]>() {

            @Override
            public byte[] doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                byte[] value = connection.get(serializer.serialize(key));

                return value;
            }
        });
    }
    /**
     * 返回double类型的value
     */
    public Double getDouble(final String key) throws Exception {

        return redisTemplate.execute(new RedisCallback<Double>() {

            @Override
            public Double doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                byte[] value = connection.get(serializer.serialize(key));
                if(null != value && value.length != 0){
                    return Double.valueOf(serializer.deserialize(value));
                }
                return 0.0;
            }
        });
    }

    /**
     * 设置数据
     * @param key
     * @param value
     * @throws Exception
     */
    public void set(final String key, final String value) throws Exception {

        redisTemplate.execute(new RedisCallback<Boolean>() {

            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                connection.set(serializer.serialize(key), serializer.serialize(value));
                return true;
            }
        });
    }
    
    /**
     * 设置带超时时间的数据
     * @param key
     * @param value
     * @param expireSeconds 超时时间，以秒为单位
     * @throws Exception
     */
    public void setEx(final String key, final String value,final long expireSeconds) throws Exception {
        
        redisTemplate.execute(new RedisCallback<Boolean>() {
            
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                connection.setEx(serializer.serialize(key), expireSeconds,serializer.serialize(value));
                return true;
            }
        });
    }

    /**
     * 删除指定的key，key不存在则忽略。
     * @param key
     * @return 
     * @throws Exception
     */
    public boolean delete(final String key) throws Exception {

        return redisTemplate.execute(new RedisCallback<Boolean>() {

            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                connection.del(serializer.serialize(key));
                return true;
            }

        });
    }

    /**
     * 在消息队列中pop指定key的数据，并在队列中移除该element
     * <p>
     * 如果key不存在或者队列已为空，则返回null

     * @param key
     * @return value
     * @throws Exception
     */
    public String pop(final String key) throws Exception {

        return redisTemplate.execute(new RedisCallback<String>() {

            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                byte[] value = connection.rPop(serializer.serialize(key));

                return serializer.deserialize(value);
            }

        });
    }

    /**
     * 将键为key的值value入队列（List）
     * <p>
     * 如果key存在，但并不是指向一个队列，则报错。 
     * @param key
     * @param value
     * @throws Exception
     */
    public boolean push(final String key, final String value) throws Exception {
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {

            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                connection.lPush(serializer.serialize(key), serializer.serialize(value));
                return true;
            }

        });

        return result;
    }

    /**
     * 获取键为key的集合大小
     * @param key
     * @return
     * @throws Exception
     */
    public long getSizeOfSet(final String key) throws Exception {
        return redisTemplate.execute(new RedisCallback<Long>() {

            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                return connection.sCard(serializer.serialize(key));
            }
        });
    }

    /**
     * 获取类型为zset,键为key的集合大小
     * @param key
     * @return
     * @throws Exception
     */
    public long getSizeOfZSet(final String key) throws Exception {
        return redisTemplate.execute(new RedisCallback<Long>() {

            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                return connection.zCard(serializer.serialize(key));
            }
        });
    }

    /**
     * 将members添加至键为key的集合
     * @param key
     * @param members
     * @return
     * @throws Exception
     */
    public void addToSet(final String key, final String... members) throws Exception {
        redisTemplate.execute(new RedisCallback<Boolean>() {

            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();

                byte[][] memberByte = new byte[members.length][];
                for (int i = 0; i < members.length; i++) {
                    memberByte[i] = serializer.serialize(members[i]);
                }
                connection.sAdd(serializer.serialize(key), memberByte);
                return true;
            }
        });
    }

    /**
     * 在键为key的集合中移除members
     * @param key
     * @param members
     * @return
     * @throws Exception
     */
    public boolean removeFromSet(final String key, final String... members) throws Exception {
        return redisTemplate.execute(new RedisCallback<Boolean>() {

            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                byte[][] memberByte = new byte[members.length][];
                for (int i = 0; i < members.length; i++) {
                    memberByte[i] = serializer.serialize(members[i]);
                }
                connection.sRem(serializer.serialize(key), memberByte);
                return true;
            }
        });
    }

    /**
     * 获取键为key的集合成员
     * @param key
     * @return
     * @throws Exception
     */
    public Set<String> getSet(final String key) throws Exception {
        return redisTemplate.execute(new RedisCallback<Set<String>>() {

            @Override
            public Set<String> doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                Set<byte[]> set = connection.sMembers(serializer.serialize(key));
                Set<String> result = new HashSet<String>();
                for (byte[] s : set) {
                    result.add(serializer.deserialize(s));
                }
                return result;
            }
        });
    }

    /**
     * 判断member是否是键为key的集合成员
     * @param key
     * @param member
     * @return
     * @throws Exception
     */
    public boolean isMemberOfSet(final String key, final String member) throws Exception {
        return redisTemplate.execute(new RedisCallback<Boolean>() {

            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                return connection.sIsMember(serializer.serialize(key), serializer.serialize(member));
            }
        });
    }

    /**
     * 获取指定集合的并集
     * @param keys
     * @return
     * @throws Exception
     */
    public Set<String> getUnionOfSet(final String... keys) throws Exception {
        return redisTemplate.execute(new RedisCallback<Set<String>>() {

            @Override
            public Set<String> doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                byte[][] byteKeys = new byte[keys.length][];
                for (int i = 0; i < keys.length; i++) {
                    byteKeys[i] = serializer.serialize(keys[i]);
                }

                Set<byte[]> set = connection.sUnion(byteKeys);

                Set<String> result = new HashSet<String>();
                for (byte[] s : set) {
                    result.add(serializer.deserialize(s));
                }
                return result;
            }
        });
    }

    /**
     * 为存储了整数value的key执行"+1"操作<br>如果key不存在，则直接将key的值置为0，并作+1操作；如果key对应的value是非数值型，则报错。
     * @param key
     * @return 返回最新的key的value
     */
    public long increase(final String key) {
        return redisTemplate.execute(new RedisCallback<Long>() {

            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                return connection.incr(serializer.serialize(key));
            }

        });
    }

    /**
     * 当Redis数据类型为HashMap值的时候，增加指定Field的数量值
     * @param key
     * @param innerKey
     * @param increaseValue
     * @return
     */
    public long increaseHMap(final String key , final String innerKey, final Long increaseValue) {
        return redisTemplate.execute(new RedisCallback<Long>() {

            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                return  connection.hIncrBy(serializer.serialize(key), serializer.serialize(innerKey), increaseValue);
            }

        });
    }

    /**
     * 取得HashMap数据类型中所有不同的key信息
     * @param key
     * @return
     */
    public Set<String> hMapKeys(final String key) {
        return redisTemplate.execute(new RedisCallback<Set<String>>() {

            @Override
            public Set<String> doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                Set<byte[]> allKeys = connection.hKeys(serializer.serialize(key));
                Set<String> allStringKeys = new HashSet<String>() ;
                for (byte[] byteParam : allKeys){
                    allStringKeys.add(serializer.deserialize(byteParam));
                }
                return allStringKeys ;
            }

        });
    }

    /**
     * 根据特定的Key设置HashMap结构指定的Value值
     * @param key
     * @param innerKey
     * @param value
     * @return
     */
    public Boolean hSetHashMapValue(final String key, final String innerKey, final String value) {
        return redisTemplate.execute(new RedisCallback<Boolean>() {

            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                return connection.hSet(serializer.serialize(key), serializer.serialize(innerKey), serializer.serialize(value));
            }

        });
    }
    
    /**
     * 根据特定的key删除HashMap结构指定的属性
     * @param key
     * @param innerKey
     * @return
     */
    public Long hDelHashMapField(final String key, final String innerKey){
        return redisTemplate.execute(new RedisCallback<Long>() {
            
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                return connection.hDel(serializer.serialize(key), serializer.serialize(innerKey));
            }

        });
    }

    /**
     * 根据指定的Key信息获取HashMap对应的Value值
     * @param key
     * @param innerKey
     * @return
     */
    public String getHashMapKeyValue(final String key , final String innerKey) {
        return redisTemplate.execute(new RedisCallback<String>() {

            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                byte[] result = connection.hGet(serializer.serialize(key), serializer.serialize(innerKey)) ;
                return serializer.deserialize(result) ;
            }

        });
    }

    /**
     * 将存储了数值型值的key执行"+value"操作
     * @param key
     * @param value
     * @return
     */
    public long increaseByValue(final String key, final long value){
        return redisTemplate.execute(new RedisCallback<Long>(){

            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                return connection.incrBy(serializer.serialize(key), value);
            }
            
        });
    }
    
    /**
     * 为存储了数值value的key执行"-1"操作<br>如果key不存在，则直接将key的值置为0，并作-1操作；如果key对应的value是非数值型，则报错。
     * @param key
     * @return
     */
    public long decrease(final String key){
        return redisTemplate.execute(new RedisCallback<Long>(){

            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                return connection.decr(serializer.serialize(key));
            }
            
        });
    }
    
    /**
     * 将存储了数值型值的key执行"-value"操作
     * @param key
     * @param value
     * @return
     */
    public long decreaseByValue(final String key, final long value){
        return redisTemplate.execute(new RedisCallback<Long>(){

            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                return connection.decrBy(serializer.serialize(key),value);
            }
            
        });
    }

    /**
     * 添加唯一标志的消息队列
     * 如果存在，则清除已存在的标志并加入新增的队列元素
     * @param value
     * @return
     */
    public void addUnique(final String key, final String value) {
        JSONObject jsonValue = new JSONObject();
        String id = null;
        String type ="";
        try {
            jsonValue = JSONObject.parseObject(value);
            if (null != jsonValue) {
            	if(RedisKey.vmKey.equals(key)){
            		type = "vmId";
            	}
            	else if(RedisKey.volKey.equals(key)){
            		type = "volId";
            	}
            	else if(RedisKey.fwKey.equals(key)){
            		type = "fwId";
            	}
            	else if(RedisKey.imageKey.equals(key)){
            		type = "imageId";
            	}
            	else if(RedisKey.ldMemberKey.equals(key)){
            		type = "memberId";
            	}
            	else if(RedisKey.ldPoolKey.equals(key)){
            		type = "poolId";
            	}
            	else if(RedisKey.ldVipKey.equals(key)){
            		type = "vipId";
            	}
            	else if(RedisKey.volSphKey.equals(key)){
            		type = "snapId";
            	}
                id = jsonValue.getString(type);
                
            }
            List<String> list = redisTemplate.execute(new RedisCallback<List<String>>() {

                @Override
                public List<String> doInRedis(RedisConnection connection)
                                                                         throws DataAccessException {
                    RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                    List<byte[]> list = connection.lRange(serializer.serialize(key), 0, -1);
                    List<String> result = new ArrayList<String>();
                    for (byte[] b : list) {
                        result.add(serializer.deserialize(b));
                    }
                    return result;
                }

            });
            if (null != id && null != list && list.size() > 0) {
                String sameValue = null;
                for (String str : list) {
                    JSONObject json = JSONObject.parseObject(str);
                    if (null != json && id.equals(json.getString(type))) {
                        sameValue = str;
                        break;
                    }
                }
                if (null != sameValue) {
                    rem(key, -1, sameValue);
                }
            }

            redisTemplate.execute(new RedisCallback<Boolean>() {

                @Override
                public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                    RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                    connection.lPush(serializer.serialize(key), serializer.serialize(value));
                    return true;
                }
            });

        } catch (Exception e) {
            log.error(e.getMessage(),e);
            throw e;
        }
    }
    
    public void removeDelResource(final String key, final String value){

        JSONObject jsonValue = new JSONObject();
        String id = null;
        String type ="";
        try {
            jsonValue = JSONObject.parseObject(value);
            if (null != jsonValue) {
            	if(RedisKey.vmKey.equals(key)){
            		type = "vmId";
            	}
            	else if(RedisKey.volKey.equals(key)){
            		type = "volId";
            	}
            	else if(RedisKey.fwKey.equals(key)){
            		type = "fwId";
            	}
            	else if(RedisKey.imageKey.equals(key)){
            		type = "imageId";
            	}
            	else if(RedisKey.ldMemberKey.equals(key)){
            		type = "memberId";
            	}
            	else if(RedisKey.ldPoolKey.equals(key)){
            		type = "poolId";
            	}
            	else if(RedisKey.ldVipKey.equals(key)){
            		type = "vipId";
            	}
            	else if(RedisKey.volSphKey.equals(key)){
            		type = "snapId";
            	}
                id = jsonValue.getString(type);
                
            }
            List<String> list = redisTemplate.execute(new RedisCallback<List<String>>() {

                @Override
                public List<String> doInRedis(RedisConnection connection)
                                                                         throws DataAccessException {
                    RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                    List<byte[]> list = connection.lRange(serializer.serialize(key), 0, -1);
                    List<String> result = new ArrayList<String>();
                    for (byte[] b : list) {
                        result.add(serializer.deserialize(b));
                    }
                    return result;
                }

            });
            if (null != id && null != list && list.size() > 0) {
                String sameValue = null;
                for (String str : list) {
                    JSONObject json = JSONObject.parseObject(str);
                    if (null != json && id.equals(json.getString(type))) {
                        sameValue = str;
                        break;
                    }
                }
                if (null != sameValue) {
                    rem(key, -1, sameValue);
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage(),e);
            throw e;
        }
    }

    public void rem(final String key, long count, final String value) {
        redisTemplate.execute(new RedisCallback<Boolean>() {

            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                connection.lRem(serializer.serialize(key), -1, serializer.serialize(value));
                return true;
            }

        });
    }

    public long sizeOfList(final String key) {
        return redisTemplate.execute(new RedisCallback<Long>() {
        	
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                return connection.lLen(serializer.serialize(key));
            }

        });
    }
    
    /**
     * 为键为key的Sorted Set添加分数为score的值value
     * @param key
     * @param value
     * @param score
     * @return
     */
    public boolean addToSortedSet(final String key,final long score,final String value){
        return redisTemplate.execute(new RedisCallback<Boolean>() {

            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                Boolean isAdded = connection.zAdd(serializer.serialize(key), score, serializer.serialize(value));
                return isAdded;
            }
        });
    }
    
    /**
     * 通过索引区间返回有序集合成指定区间内的成员，分数从低到高
     * @param key 
     * @param start 
     * @param end 
     * @return
     */
    public List<String> getZSetByRange(final String key, final long start, final long end){
    	return redisTemplate.execute(new RedisCallback<List<String>>(){

			@Override
			public List<String> doInRedis(RedisConnection connection) throws DataAccessException {
				RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                Set<byte[]> set = connection.zRange(serializer.serialize(key), start, end);
                List<String> result = new ArrayList<>();
                for (byte[] bytes : set) {
                    result.add(String.valueOf(serializer.deserialize(bytes)));
                }
                return result;
			}
    		
    	});
    }
    
    /**
     * 返回有序集中指定区间内的成员，通过索引，分数从高到低
     * @param key
     * @param start
     * @param end
     * @return
     */
    public List<String> getZSetByRevRange(final String key, final long start, final long end){
    	return redisTemplate.execute(new RedisCallback<List<String>>(){

			@Override
			public List<String> doInRedis(RedisConnection connection) throws DataAccessException {
				RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                Set<byte[]> set = connection.zRevRange(serializer.serialize(key), start, end);
                List<String> result = new ArrayList<>();
                for (byte[] bytes : set) {
                    result.add(String.valueOf(serializer.deserialize(bytes)));
                }
                return result;
			}
    		
    	});
    }
    /**
     * 返回有序集合中指定分数区间的成员列表。有序集成员按分数值递增(从小到大)次序排列。
     * @param key
     * @return
     */
    public List<String> getZSetByScoresASC(final String key, final double min, final double max){
    	return redisTemplate.execute(new RedisCallback<List<String>>(){

			@Override
			public List<String> doInRedis(RedisConnection connection) throws DataAccessException {
				RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                Set<byte[]> set = connection.zRangeByScore(serializer.serialize(key), min, max);
                List<String> result = new ArrayList<>();
                for (byte[] bytes : set) {
                    result.add(String.valueOf(serializer.deserialize(bytes)));
                }
                return result;
			}
    		
    	});
    }
    
    /**
     *  返回有序集中指定分数区间内的所有的成员。有序集成员按分数值递减(从大到小)的次序排列。
     * @param key
     * @return
     */
    public List<String> getZSetByScoresDESC(final String key, final double min, final double max){
    	return redisTemplate.execute(new RedisCallback<List<String>>(){

			@Override
			public List<String> doInRedis(RedisConnection connection) throws DataAccessException {
				RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                Set<byte[]> set = connection.zRevRangeByScore(serializer.serialize(key), min, max);
                List<String> result = new ArrayList<>();
                for (byte[] bytes : set) {
                    result.add(String.valueOf(serializer.deserialize(bytes)));
                }
                return result;
			}
    		
    	});
    }
    
    public List<String> getListByRange(final String key ,final int begin,final int end){
    	return redisTemplate.execute(new RedisCallback<List<String>>(){

			@Override
			public List<String> doInRedis(RedisConnection connection) throws DataAccessException {
				List<String> result = new ArrayList<>();
				RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
				List<byte[]> list = connection.lRange(serializer.serialize(key), begin, end);
                for (byte[] b : list) {
                    result.add(serializer.deserialize(b));
                }
                return result;
			}
    		
    	});
    }
}
