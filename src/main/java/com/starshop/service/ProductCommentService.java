package com.starshop.service;

import com.starshop.common.result.CursorCommonEntity;
import com.starshop.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public interface ProductCommentService {

    /**
     * 用户做分类查询商品一级评论
     * @param cursorCommonEntity
     * @param productId
     * @return
     */
    Result<?> getProductCommentBySortType(@Valid CursorCommonEntity cursorCommonEntity, @NotBlank String productId);

    /**
     * 查询指定一级评论下二级评论
     * @param firstCommentId
     * @param cursorCommonEntity
     * @return
     */
    Result<?> getSecondComment(@NotBlank String firstCommentId, @NotNull CursorCommonEntity cursorCommonEntity);
}
