package com.starshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starshop.common.mapstruct.CopyMapper;
import com.starshop.common.utils.JacksonUtils;
import com.starshop.constant.BucketConstant;
import com.starshop.constant.DataConstant;
import com.starshop.constant.RedisKeyConstant;
import com.starshop.infrastructure.redis.connect.RedisConnector;
import com.starshop.infrastructure.redis.connect.StringRedisConnector;
import com.starshop.mapper.ProductMapper;
import com.starshop.pojo.entity.Product;
import com.starshop.pojo.entity.ProductDocument;
import com.starshop.properties.RedisBucketTtlProperties;
import com.starshop.properties.RedisCacheCountProperties;
import com.starshop.service.ProductRedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
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
     * 刷新热门商品缓存：DB 按销量 Top N → Redis Hash + ID列表
     * 采用「先写副本 → 切换」的方式保证读不中断
     */
    @Override
    public void refreshHotProductCache() {
        //数据库查询商品并且按照降序排序
        int size = redisCacheCountProperties.getHotProductCacheSize();
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, DataConstant.ONE_INT)
                .orderByDesc(Product::getSalesCount)
                .last("LIMIT " + size);
        List<Product> productList = productMapper.selectList(wrapper);

        //判断是否不存在
        if (productList == null || productList.isEmpty()) {
            log.error("无商品信息");
            return;
        }
        //利用copymapper将product转化
        List<ProductDocument> documentList = productList.stream().map(copyMapper::productToDocument).toList();

        //先写入副本(先删除后更新)
        RedisConnector.delete(dataCopyKey);
        RedisConnector.delete(idListCopyKey);

        Map<String, Object> hashMap = new HashMap<>();
        for (ProductDocument doc : documentList) {
            hashMap.put(String.valueOf(doc.getId()), doc);
        }

        RedisConnector.opsForHash().putAll(dataCopyKey, hashMap);

        List<Long> idList = documentList.stream().map(ProductDocument::getId).toList();
        String json = JacksonUtils.toJson(idList);
        StringRedisConnector.opsForValue().set(idListCopyKey, json);

        //再加锁标记写入redis
        RBucket<BucketConstant.BucketSign> bucket = redissonClient.getBucket(signKey);
        RBucket<BucketConstant.BucketSign> bucketCp = redissonClient.getBucket(idSignKey);
        BucketConstant.BucketSign writeSign = new BucketConstant.BucketSign(
                BucketConstant.BucketThreadType.WRITE_THREAD, UUID.randomUUID().toString()
        );
        bucket.set(writeSign, Duration.ofSeconds(redisBucketTtlProperties.getHotProductWriteBucketTtl()));
        bucketCp.set(writeSign, Duration.ofSeconds(redisBucketTtlProperties.getHotProductWriteBucketTtl()));

        //切换主副更新redis中的数据
        RedisConnector.delete(dataKey);
        RedisConnector.delete(idListKey);
        RedisConnector.rename(dataCopyKey, dataKey);
        RedisConnector.rename(idListCopyKey, idListKey);

        // 清除写锁
        bucket.delete();
        bucketCp.delete();


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
}