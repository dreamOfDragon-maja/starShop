package com.starshop.service.impl;

import com.starshop.constant.BucketConstant;
import com.starshop.constant.RedisKeyConstant;
import com.starshop.infrastructure.redis.connect.RedisConnector;
import com.starshop.pojo.entity.ProductDocument;
import com.starshop.properties.RedisBucketTtlProperties;
import com.starshop.service.ProductRedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductRedisCacheServiceImpl implements ProductRedisCacheService {

    private final RedissonClient redissonClient;

    private final RedisBucketTtlProperties redisBucketTtlProperties;

    /**
     * 获取热门商品 (采用双缓存 + 读写标记)
     * @return 商品文档列表
     */
    @Override
    public List<ProductDocument> getHotProduct() {
        String key =RedisKeyConstant.BUCKET_PREFIX + RedisKeyConstant.PREFIX_PRODUCT + RedisKeyConstant.HOT;
        //构造Bucket对象
        RBucket<BucketConstant.BucketSign> bucket = redissonClient.getBucket(key);

        //获取当前线程执行类型
        BucketConstant.BucketThreadType bucketThreadType = bucket.get().bucketThreadType();
        //判断是否有在读线程
        if (!bucket.isExists() || BucketConstant.BucketThreadType.READ_THREAD.equals(bucketThreadType)) {
            //直接获取数据
        //创建一个读写标记，并使用UUID防止篡改
            BucketConstant.BucketSign bucketSign = new BucketConstant.BucketSign(
                    BucketConstant.BucketThreadType.READ_THREAD,
                    UUID.randomUUID().toString()
            );
        //尝试获取数据,先存入标记并存入过期时间
            try {
                bucket.set(bucketSign, Duration.ofSeconds(redisBucketTtlProperties.getHotProductReadBucketTtl()));
                return RedisConnector.opsForHash()
                        .entries(key)
                        .values()
                        .stream()
                        .map(o -> (ProductDocument)o)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                log.error("查询redis热门商品主数据失败", e);
            } finally {
                //清除标记
                if (bucket.isExists() && bucketSign.equals(bucket.get())) {
                    bucket.delete();
                }
            }
        }else {
            //通过副本拷贝获取数据
            String copyKey =RedisKeyConstant.BUCKET_PREFIX + RedisKeyConstant.PREFIX_COPY + RedisKeyConstant.PREFIX_PRODUCT + RedisKeyConstant.HOT;
            RBucket<BucketConstant.BucketSign> bucketCopy = redissonClient.getBucket(copyKey);
            BucketConstant.BucketSign bucketSign = new BucketConstant.BucketSign(
                    BucketConstant.BucketThreadType.READ_THREAD,
                    UUID.randomUUID().toString()
            );
            try {
                bucketCopy.set(bucketSign, Duration.ofSeconds(redisBucketTtlProperties.getHotProductReadBucketTtl()));
                return RedisConnector.opsForHash()
                        .entries(copyKey)
                        .values()
                        .stream()
                        .map(o -> (ProductDocument) o)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                log.error("查询redis热门商品副本数据失败", e);
            } finally {
                if (bucketCopy.isExists() && bucketSign.equals(bucketCopy.get())) {
                    bucketCopy.delete();
                }
            }
        }
        return null;
    }


    /**
     * 查询热门商品ID列表
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Long> getHotProductIdList() {
        String key =RedisKeyConstant.BUCKET_PREFIX + RedisKeyConstant.PREFIX_PRODUCT + RedisKeyConstant.HOT + ":" + RedisKeyConstant.ID_LIST;
        RBucket<BucketConstant.BucketSign> bucket = redissonClient.getBucket(key);

        if (!bucket.isExists() || BucketConstant.BucketThreadType.READ_THREAD.equals(bucket.get().bucketThreadType())) {
            BucketConstant.BucketSign bucketSign = new BucketConstant.BucketSign(
                    BucketConstant.BucketThreadType.READ_THREAD,
                    UUID.randomUUID().toString()
            );
            try {
                bucket.set(bucketSign, Duration.ofSeconds(redisBucketTtlProperties.getHotProductReadBucketTtl()));
                return  (List<Long>) RedisConnector.opsForValue().get(key);
            } catch (Exception e) {
                log.error("查询redis热门商品ID列表失败", e);
            } finally {
                if (bucket.isExists() && bucketSign.equals(bucket.get())) {
                    bucket.delete();
                }
            }
        } else {
            String copyKey =RedisKeyConstant.BUCKET_PREFIX +RedisKeyConstant.PREFIX_COPY+ RedisKeyConstant.PREFIX_PRODUCT + RedisKeyConstant.HOT + ":" + RedisKeyConstant.ID_LIST;
            RBucket<BucketConstant.BucketSign> bucketCopy = redissonClient.getBucket(copyKey);
            BucketConstant.BucketSign bucketSign = new BucketConstant.BucketSign(
                    BucketConstant.BucketThreadType.READ_THREAD,
                    UUID.randomUUID().toString()
            );
            try {
                bucketCopy.set(bucketSign, Duration.ofSeconds(redisBucketTtlProperties.getHotProductReadBucketTtl()));
                return  (List<Long>) RedisConnector.opsForValue().get(copyKey);
            } catch (Exception e) {
                log.error("查询redis热门商品ID副本列表失败", e);
            } finally {
                if (bucketCopy.isExists() && bucketSign.equals(bucketCopy.get())) {
                    bucketCopy.delete();
                }
            }
        }
        return null;
    }
}
