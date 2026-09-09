package com.starshop.infrastructure.es.service;

import com.starshop.infrastructure.es.document.ProductDocument;
import com.starshop.pojo.enums.ProductSortTypeEnum;
import com.starshop.pojo.vo.SimpleProductVO;

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

    /**
     * 根据商品关键词进行查询
     * @param limit 查询数
     * @param productSortTypeEnum 商品排序枚举
     * @param sortValue 游标开始值
     * @param productId 商品 id
     * @param keyword 关键词
     * @return 查询文档列表
     */
    List<ProductDocument> searchByCursorByName(Integer limit, ProductSortTypeEnum productSortTypeEnum ,String sortValue, Long productId, String keyword);


    /**
     * 批量保存商品文档列表
     * @param productDocumentList 商品文档列表
     */
    void batchSaveProductDocument(List<ProductDocument> productDocumentList);

    /**
     * 通过 id 列表批量获取商品文档
     * @param idList id 列表
     * @return 商品文档列表
     */
    List<ProductDocument> getProductDocumentByIdList(List<Long> idList);
}
