package com.starshop.infrastructure.es.service;

import com.starshop.infrastructure.es.document.ProductDocument;
import com.starshop.pojo.enums.ProductSortTypeEnum;

import java.util.List;

public interface ProductDocumentService {

    /**
     * 根据分类id进行查询
     * @param limit 查询数
     * @param productSortTypeEnum 商品排序枚举
     * @param productId 商品 id
     * @param categoryId 分类 id (一级或二级分类 id )
     * @param isFirstCategoryId 是否为一级分类 id
     * @return 查询文档列表
     */
    List<ProductDocument> searchByCursorByCategoryId(Integer limit, ProductSortTypeEnum productSortTypeEnum, String sortValue, Long productId, Long categoryId, boolean isFirstCategoryId);

}
