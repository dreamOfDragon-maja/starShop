package com.starshop.service;

import com.starshop.common.result.CursorCommonEntity;
import com.starshop.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public interface ProductCommentService {

    /**
     * 用户做分类查询商品一级评论
     * @param cursorCommonEntity
     * @param productId
     * @return
     */
    Result<?> getProductCommentBySortType(@Valid CursorCommonEntity cursorCommonEntity, @NotBlank String productId);
}
