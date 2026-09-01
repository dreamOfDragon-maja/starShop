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
}
