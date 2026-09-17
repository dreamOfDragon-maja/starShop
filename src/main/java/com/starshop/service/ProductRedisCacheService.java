package com.starshop.service;

import com.starshop.infrastructure.es.document.ProductDocument;

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
     * 获取es最大商品id
     * @return 最大商品id
     */
    Long getMaxProductId();

    /**
     * 初始化最大商品id es -> redis
     */
    void initMaxProductId();
}
