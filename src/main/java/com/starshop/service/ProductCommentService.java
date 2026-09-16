package com.starshop.service;

import com.starshop.common.result.CursorCommonEntity;
import com.starshop.pojo.dto.AppendProductFirstCommentDTO;
import com.starshop.pojo.dto.FirstProductCommentDTO;
import com.starshop.pojo.dto.SecondProductCommentDTO;
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

    /**
     * 查询指定一级评论下的用户追评
     * @return
     */
    Result<?> getAppendComment(@NotBlank String firstCommentId);

    /**
     * 用户发表一级商品评论
     * @param firstProductCommentDTO
     * @return
     */
    Result<?> saveProductFirstComment(@Valid FirstProductCommentDTO firstProductCommentDTO);

    /**
     * 用户发表二级以上商品评论
     * @param secondProductCommentDTO
     * @return
     */
    Result<?> saveProductSecondComment(@Valid SecondProductCommentDTO secondProductCommentDTO);

    /**
     * 用户对一级评论进行追加
     * @param appendProductFirstCommentDTO
     * @return
     */
    Result<?> appendProductFirstComment(@Valid @NotNull AppendProductFirstCommentDTO appendProductFirstCommentDTO);

    /**
     * 统计商品下评论数
     * @param productId
     * @return
     */
    Result<?> getProductCommentCount(@NotBlank String productId);

    /**
     * 对商品进行点赞和取消点赞
     * @param productCommentId
     * @param isLike
     * @param isFirstComment
     * @return
     */
    Result<?> updateProductCommentLike(@NotBlank String productCommentId, @NotNull Integer isLike, @NotNull Integer isFirstComment);
}
