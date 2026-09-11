package com.starshop.infrastructure.es.repositery;

import com.starshop.infrastructure.es.document.ProductDocument;
import com.starshop.pojo.enums.ProductSortTypeEnum;
import com.starshop.pojo.vo.SimpleProductVO;

import java.util.List;

public interface ProductEsRepository {
    /**
     * 根据查询种类和分类 id 首次进行游标查询 (无需开始游标)
     * @param productSortTypeEnum 商品排序格式
     * @param categoryIdList 分类 id 集合
     * @param limit 查询数
     * @return 商品文档列表
     */
    List<ProductDocument> searchLimitByProductSortTypeAndCategoryIdList(ProductSortTypeEnum productSortTypeEnum, List<Long> categoryIdList, Integer limit);


    /**
     * 根据查询种类和分类 id 进行游标查询 (需要开始游标)
     * @param productSortTypeEnum 商品排序格式
     * @param categoryIdList 分类 id 集合
     * @param limit 查询数
     * @param sortValue 开始游标值
     * @param productId 开始商品 id
     * @return 商品文档列表
     */
    List<ProductDocument> searchCursorByProductSortTypeAndCategoryIdList(ProductSortTypeEnum productSortTypeEnum, List<Long> categoryIdList, Integer limit, String sortValue, Long productId);

    /**
     * 根据查询种类和分类 id 首次进行游标查询 (无需开始游标)
     * @param productSortTypeEnum 商品排序格式
     * @param categoryId 查询商品分类 id
     * @param limit 查询数
     * @return 商品文档列表
     */
    List<ProductDocument> searchLimitByProductSortTypeAndCategoryId(ProductSortTypeEnum productSortTypeEnum, Long categoryId, Integer limit);


    /**
     * 根据查询种类和分类 id 进行游标查询 (需要开始游标)
     * @param productSortTypeEnum 商品排序格式
     * @param categoryId 查询商品分类 id
     * @param limit 查询数
     * @param sortValue 开始游标值
     * @param productId 开始商品 id
     * @return 商品文档列表
     */
    List<ProductDocument> searchCursorByProductSortTypeAndCategoryId(ProductSortTypeEnum productSortTypeEnum, Long categoryId, Integer limit, String sortValue, Long productId);

    /**
     * 根据查询种类和商品名 进行首次游标查询(无需开始游标)
     * @param productSortTypeEnum 商品排序格式
     * @param keyword 商品关键词
     * @param limit 查询数
     * @return 商品文档列表
     */
    List<ProductDocument> searchLimitByProductSortTypeAndProductName(ProductSortTypeEnum productSortTypeEnum, String keyword, Integer limit);


    /**
     * 根据查询种类和商品名 进行游标查询(需要开始游标)
     * @param productSortTypeEnum 商品排序格式
     * @param keyword 商品关键词
     * @param limit 查询数
     * @param sortValue 开始游标值
     * @param productId 开始商品 id
     * @return 商品文档列表
     */
    List<ProductDocument> searchCursorByProductSortTypeAndProductName(ProductSortTypeEnum productSortTypeEnum, String keyword, Integer limit, String sortValue, Long productId);


    /**
     * 批量保存商品文档
     * @param documents 要批量保存的文档
     */
    void batchSave(List<ProductDocument> documents);


    /**
     * 根据 id 列表获取商品文档列表
     * @param idList 商品 id 列表
     * @return 商品文档列表
     */
    List<ProductDocument> getByIdList(List<Long> idList);

    /**
     * 根据商品文档名进行查询
     * @param name 商品文档名
     * @param limit 查询数量
     * @return 查询商品文档列表
     */
    List<ProductDocument> searchByName(String name, Integer limit);

    /**
     * 获取最大商品文档 id
     * @return 最大商品文档 id
     */
    Long getMaxId();


    /**
     * 查询指定id后的指定数量的商品文档
     * @param limit
     * @param productId
     * @return
     */
    List<ProductDocument> searchLimitAfterId(Integer limit, Long productId);
}
