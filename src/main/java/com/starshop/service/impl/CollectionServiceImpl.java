package com.starshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.common.annotation.business.RemoveProductCollectionRedisCacheAnnotation;
import com.starshop.constant.MessageConstant;
import com.starshop.context.BaseContext;
import com.starshop.mapper.CollectionMapper;
import com.starshop.pojo.entity.ProductCollection;
import com.starshop.result.Result;
import com.starshop.service.CollectionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class CollectionServiceImpl extends ServiceImpl<CollectionMapper, ProductCollection> implements CollectionService {

    /**
     * 新增收藏
     * @param productId
     * @return
     */
    @Override
    @RemoveProductCollectionRedisCacheAnnotation
    public Result addCollection(String productId) {
        if (StringUtils.isBlank(productId)) {
            return Result.error(MessageConstant.TOM_CAT_ERROR);
        }
        String userId = BaseContext.getUserId();
        ProductCollection productCollection = new ProductCollection();
        productCollection.setUserId(Long.valueOf(userId)).setProductId(Long.valueOf(productId));
        boolean isSuccess = save(productCollection);
        if (!isSuccess) {
            return Result.error(MessageConstant.TOM_CAT_ERROR);
        }
        return Result.success();
    }

    /**
     * 删除收藏
     * 支持单个或批量删除收藏的商品（商品ID以逗号分隔）
     * @param productIds
     * @return
     */
    @Override
    @RemoveProductCollectionRedisCacheAnnotation
    public Result deleteCollection(String productIds) {
        if (StringUtils.isBlank(productIds)) {
            return Result.error(MessageConstant.CONTENT_NOT_EXIST_ERROR);
        }
        String userId = BaseContext.getUserId();
        List<String> productIdList = Arrays.stream(StringUtils.split(productIds, ",")).toList();
        LambdaQueryWrapper<ProductCollection> lambdaQueryWrapper = new LambdaQueryWrapper<ProductCollection>()
                .eq(ProductCollection::getUserId, userId)
                .in(ProductCollection::getProductId, productIdList);
        boolean isSuccess = remove(lambdaQueryWrapper);
        if (!isSuccess) {
            return Result.error(MessageConstant.TOM_CAT_ERROR);
        }
        return Result.success(productIdList.size());
    }
}
