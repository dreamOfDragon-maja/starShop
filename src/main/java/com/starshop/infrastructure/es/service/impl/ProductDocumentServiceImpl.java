package com.starshop.infrastructure.es.service.impl;

import com.starshop.constant.RedisKeyConstant;
import com.starshop.infrastructure.es.document.ProductDocument;
import com.starshop.infrastructure.es.repositery.ProductEsRepository;
import com.starshop.infrastructure.es.service.ProductDocumentService;
import com.starshop.infrastructure.redis.connect.RedisConnector;
import com.starshop.pojo.enums.ProductSortTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ProductDocumentServiceImpl implements ProductDocumentService {

    private final ProductEsRepository productEsRepository;

    /**
     * 根据分类id进行查询
     * @param limit 查询数
     * @param productSortTypeEnum 商品排序枚举
     * @param productId 商品 id
     * @param categoryId 分类 id (一级或二级分类 id )
     * @param isFirstCategoryId 是否为一级分类 id
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<ProductDocument> searchByCursorByCategoryId(Integer limit, ProductSortTypeEnum productSortTypeEnum, String sortValue, Long productId, Long categoryId, boolean isFirstCategoryId) {
        //判断是否为一级分类
        if (Objects.isNull(isFirstCategoryId)) {
            throw new RuntimeException("是否为一级分类id isFirstCategoryId, 传参为 null");
        }
        //如果是一级分类，从redis查询出二级分类
        if (isFirstCategoryId) {
            String key = RedisKeyConstant.PREFIX_CATEGORY + RedisKeyConstant.TREE;
            String hashKey = RedisKeyConstant.FIRST_CATEGORY + categoryId;
            List<Long> secondCategoryIdList = RedisConnector.getHashField(key, hashKey, ArrayList.class);
            //首次一级分类查询
            if (Objects.isNull(sortValue) || Objects.isNull(productId)) {
                return productEsRepository.searchLimitByProductSortTypeAndCategoryIdList(productSortTypeEnum, secondCategoryIdList, limit);
            }
            //一级分类游标查询
            return productEsRepository.searchCursorByProductSortTypeAndCategoryIdList(productSortTypeEnum, secondCategoryIdList, limit, sortValue, productId);
        }
        //不是一级分类直接根据传的categoryId查
        //首次二级分类查询
        if (Objects.isNull(sortValue) || Objects.isNull(productId)) {
            return productEsRepository.searchLimitByProductSortTypeAndCategoryId(productSortTypeEnum, categoryId, limit);
        }
        //二级分类游标查询
        return productEsRepository.searchCursorByProductSortTypeAndCategoryId(productSortTypeEnum, categoryId, limit, sortValue, productId);

    }
    /**
     * 根据商品关键词进行查询
     * @param limit 查询数
     * @param productSortTypeEnum 商品排序枚举
     * @param sortValue 游标开始值
     * @param productId 商品 id
     * @param keyword 关键词
     * @return 查询文档列表
     */
    @Override
    public List<ProductDocument> searchByCursorByName(Integer limit, ProductSortTypeEnum productSortTypeEnum, String sortValue, Long productId, String keyword) {
        //首次进行游标查询
        if (Objects.isNull(sortValue) || Objects.isNull(productId)) {
            return productEsRepository.searchLimitByProductSortTypeAndProductName(productSortTypeEnum, keyword, limit);
        }
        //游标查询
        return productEsRepository.searchCursorByProductSortTypeAndProductName(productSortTypeEnum, keyword, limit, sortValue, productId);

    }

    /**
     * 批量保存商品文档列表
     * @param productDocumentList 商品文档列表
     */
    @Override
    public void batchSaveProductDocument(List<ProductDocument> productDocumentList) {
        productEsRepository.batchSave(productDocumentList);
    }
}
