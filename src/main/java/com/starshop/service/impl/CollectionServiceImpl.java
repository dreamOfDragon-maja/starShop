package com.starshop.service.impl;

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
}
