package com.starshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starshop.pojo.entity.ProductCollection;
import com.starshop.result.Result;
import jakarta.validation.constraints.NotBlank;

public interface CollectionService extends IService<ProductCollection> {

    /**
     * 新增收藏
     * @param productId
     * @return
     */
    Result addCollection(@NotBlank String productId);
}
