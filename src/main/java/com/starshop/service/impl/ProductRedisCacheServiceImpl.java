package com.starshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.starshop.common.mapstruct.CopyMapper;
import com.starshop.common.utils.JacksonUtils;
import com.starshop.constant.BucketConstant;
import com.starshop.constant.DataConstant;
import com.starshop.constant.RedisKeyConstant;
import com.starshop.infrastructure.es.document.ProductDocument;
import com.starshop.infrastructure.es.service.ProductDocumentService;
import com.starshop.infrastructure.redis.connect.RedisConnector;
import com.starshop.infrastructure.redis.connect.StringRedisConnector;
import com.starshop.mapper.ProductMapper;
import com.starshop.pojo.entity.Product;
import com.starshop.properties.RedisBucketTtlProperties;
import com.starshop.properties.RedisCacheCountProperties;
import com.starshop.service.ProductRedisCacheService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductRedisCacheServiceImpl implements ProductRedisCacheService {


    private final RedissonClient redissonClient;

    private final RedisBucketTtlProperties redisBucketTtlProperties;

    private final RedisCacheCountProperties redisCacheCountProperties;

    private final ProductMapper productMapper;

    private final CopyMapper copyMapper;

    private final ProductDocumentService productDocumentService;

    @Resource(name = "executorSchedulerCommon")
    private ThreadPoolTaskExecutor threadPoolExecutor;



    //TODO 后续抽出来
    String signKey =RedisKeyConstant.BUCKET_SIGN_PREFIX + RedisKeyConstant.PREFIX_PRODUCT + RedisKeyConstant.HOT;
    String dataKey =RedisKeyConstant.PREFIX_PRODUCT + RedisKeyConstant.HOT;

    String signCopyKey =RedisKeyConstant.BUCKET_SIGN_PREFIX + RedisKeyConstant.PREFIX_COPY + RedisKeyConstant.PREFIX_PRODUCT + RedisKeyConstant.HOT;
    String dataCopyKey =RedisKeyConstant.PREFIX_COPY + RedisKeyConstant.PREFIX_PRODUCT + RedisKeyConstant.HOT;

    String idSignKey =RedisKeyConstant.BUCKET_SIGN_PREFIX + RedisKeyConstant.PREFIX_PRODUCT + RedisKeyConstant.HOT + ":" + RedisKeyConstant.ID_LIST;
    String idListKey =RedisKeyConstant.PREFIX_PRODUCT + RedisKeyConstant.HOT + ":" + RedisKeyConstant.ID_LIST;

    String idSignCopyKey =RedisKeyConstant.BUCKET_SIGN_PREFIX +RedisKeyConstant.PREFIX_COPY+ RedisKeyConstant.PREFIX_PRODUCT + RedisKeyConstant.HOT + ":" + RedisKeyConstant.ID_LIST;
    String idListCopyKey =RedisKeyConstant.PREFIX_COPY+ RedisKeyConstant.PREFIX_PRODUCT + RedisKeyConstant.HOT + ":" + RedisKeyConstant.ID_LIST;

    /**
     * 获取热门商品 (采用双缓存 + 读写标记)
     * @return 商品文档列表
     */
    @Override
    public List<ProductDocument> getHotProduct() {
        //构造Bucket对象
        RBucket<BucketConstant.BucketSign> bucket = redissonClient.getBucket(signKey);

        //判断是否有在读线程
        if (!bucket.isExists() || BucketConstant.BucketThreadType.READ_THREAD.equals(bucket.get().bucketThreadType())) {
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
                        .entries(dataKey)
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
            RBucket<BucketConstant.BucketSign> bucketCopy = redissonClient.getBucket(signCopyKey);
            BucketConstant.BucketSign bucketSign = new BucketConstant.BucketSign(
                    BucketConstant.BucketThreadType.READ_THREAD,
                    UUID.randomUUID().toString()
            );
            try {
                bucketCopy.set(bucketSign, Duration.ofSeconds(redisBucketTtlProperties.getHotProductReadBucketTtl()));
                return RedisConnector.opsForHash()
                        .entries(dataCopyKey)
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
        return Collections.emptyList();
    }

    /**
     * 查询热门商品ID列表
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Long> getHotProductIdList() {
        RBucket<BucketConstant.BucketSign> bucket = redissonClient.getBucket(idSignKey);

        if (!bucket.isExists() || BucketConstant.BucketThreadType.READ_THREAD.equals(bucket.get().bucketThreadType())) {
            BucketConstant.BucketSign bucketSign = new BucketConstant.BucketSign(
                    BucketConstant.BucketThreadType.READ_THREAD,
                    UUID.randomUUID().toString()
            );
            try {
                bucket.set(bucketSign, Duration.ofSeconds(redisBucketTtlProperties.getHotProductReadBucketTtl()));
                String json = StringRedisConnector.opsForValue().get(idListKey);
                if (json == null || json.isBlank()) {
                    return Collections.emptyList();
                }
                return JacksonUtils.fromJson(json, new TypeReference<>() {});
            } catch (Exception e) {
                log.error("查询redis热门商品ID列表失败", e);
                RedisConnector.delete(idListKey);
            } finally {
                if (bucket.isExists() && bucketSign.equals(bucket.get())) {
                    bucket.delete();
                }
            }
        } else {
            RBucket<BucketConstant.BucketSign> bucketCopy = redissonClient.getBucket(idSignCopyKey);
            BucketConstant.BucketSign bucketSign = new BucketConstant.BucketSign(
                    BucketConstant.BucketThreadType.READ_THREAD,
                    UUID.randomUUID().toString()
            );
            try {
                bucketCopy.set(bucketSign, Duration.ofSeconds(redisBucketTtlProperties.getHotProductReadBucketTtl()));
                String json = StringRedisConnector.opsForValue().get(idListCopyKey);
                if (json == null || json.isBlank()) {
                    return Collections.emptyList();
                }
                return JacksonUtils.fromJson(json, new TypeReference<>() {});
            } catch (Exception e) {
                log.error("查询redis热门商品ID副本列表失败", e);
                RedisConnector.delete(idListCopyKey);
            } finally {
                if (bucketCopy.isExists() && bucketSign.equals(bucketCopy.get())) {
                    bucketCopy.delete();
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * 获取es最大商品id
     * @return 最大商品id
     */
    @Override
    public Long getMaxProductId() {
        String maxProductIdKey = RedisKeyConstant.PREFIX_PRODUCT + RedisKeyConstant.MAX_PRODUCT_ID;
        Object maxProductIdObject = RedisConnector.opsForValue().get(maxProductIdKey);

        //如果查询结果为null
        if (Objects.isNull(maxProductIdObject)) {
            //开启线程任务查询es
            threadPoolExecutor.execute(this::initMaxProductId);
            //直接查询es返回数据
            return productDocumentService.getMaxProductDocumentId();
        }

        return Long.valueOf(maxProductIdObject.toString());
    }

    /**
     * 初始化最大商品id es -> redis
     */
    @Override
    public void initMaxProductId() {
        //查询es
        Long maxProductDocumentId = productDocumentService.getMaxProductDocumentId();
        String maxProductIdKey = RedisKeyConstant.PREFIX_PRODUCT + RedisKeyConstant.MAX_PRODUCT_ID;
        RedisConnector.opsForValue().set(maxProductIdKey,maxProductDocumentId);
    }
}