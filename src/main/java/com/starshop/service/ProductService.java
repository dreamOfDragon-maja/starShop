package com.starshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starshop.common.result.CursorCommonEntity;
import com.starshop.common.result.CursorCommonResult;
import com.starshop.common.result.SimpleCursorCommonResult;
import com.starshop.infrastructure.es.document.ProductDocument;
import com.starshop.pojo.entity.Product;
import com.starshop.pojo.vo.SimpleProductVO;
import com.starshop.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public interface ProductService extends IService<Product> {

    /**
     * 获取热门商品
     * @return
     */
    List<ProductDocument> getHotProduct(Integer limit);

    /**
     * 获取商品简单信息
     * @param productIds
     * @return
     */
    Result<List<SimpleProductVO>> getBriefProduct(String productIds);

    /**
     * 获取商品详细信息
     * @param productId
     * @param userId
     * @return
     */
    Result<?> getProductDetail(String productId, String userId);

    /**
     * 分类游标查询指定分类下的简单商品列表
     * @param cursorCommonEntity
     * @param categoryId
     * @param isFirstCategoryId
     * @return
     */
    CursorCommonResult getCategorySimpleProduct(@Valid @NotNull CursorCommonEntity cursorCommonEntity, Long categoryId, boolean isFirstCategoryId);

    /**
     * 关键词游标分类搜索商品
     * @param cursorCommonEntity
     * @param keyword
     * @return
     */
    CursorCommonResult searchProductList(@Valid CursorCommonEntity cursorCommonEntity, String keyword);

    /**
     * 获取相关商品
     * @param productName 商品名
     * @param limit 查询数量
     * @return
     */
    List<ProductDocument> getProductRelated(String productName, Integer limit);


    /**
     * 查询商品规格价格
     * @param productId 商品id
     * @param specId 规格id
     * @return
     */
    Result<?> getProductSpecPrice(String productId, String specId);

    /**
     * 滚动查询商品列表
     * @param beginId
     * @param querySize
     * @return
     */
    SimpleCursorCommonResult getSimpleProductByScrollQuery(Long beginId, Integer querySize);
}
