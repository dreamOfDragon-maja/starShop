package com.starshop.service;

import com.starshop.pojo.entity.ProductDocument;

import java.util.List;

public interface ProductRedisCacheService {

    /**
     * 获取热门商品
     * @return 商品文档列表
     */
    List<ProductDocument> getHotProduct();

    /**
     * 查询热门商品ID列表
     */
    List<Long> getHotProductIdList();

    /**
     * 刷新热门商品缓存（从 DB 按销量 Top N 写入 Redis 双缓存）
     */
    void refreshHotProductCache();
}
