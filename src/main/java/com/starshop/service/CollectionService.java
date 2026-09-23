package com.starshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starshop.common.result.SimpleCursorCommonEntity;
import com.starshop.common.result.SimpleCursorCommonResult;
import com.starshop.pojo.entity.ProductCollection;
import com.starshop.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public interface CollectionService extends IService<ProductCollection> {

    /**
     * 新增收藏
     * @param productId
     * @return
     */
    Result addCollection(@NotBlank String productId);

    /**
     * 删除收藏
     * 支持单个或批量删除收藏的商品（商品ID以逗号分隔）
     * @param productIds
     * @return
     */
    Result deleteCollection(String productIds);

    /**
     * 获取用户收藏商品列表
     * @param simpleCursorCommonEntity 简单查询请求参数
     * @return 简单商品封装列表
     */
    Result<SimpleCursorCommonResult> getCollectionList(@Valid SimpleCursorCommonEntity simpleCursorCommonEntity);
}
