package com.starshop.pojo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商品评论DTO（包含一级评论/回复评论）
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true) // 忽略未识别的字段，防止字段拼写错误导致请求失败
public class SecondProductCommentDTO {

    /**
     * 商品ID（核心字段，必传）
     */
    @NotBlank(message = "商品 ID不能为空")
    private String productId;

    /**
     * 商品规格 ID
     */
    private String productSpecId;


    /**
     * 父评论ID（一级评论传0或null，回复评论传对应父评论ID）
     */
    private String parentId;

    /**
     * 评论用户昵称
     */
    @Size(max = 20, message = "用户昵称长度不能超过20个字符")
    private String userNickname;

    /**
     * 评论用户头像
     */
    private String userAvatar;

    /**
     * 评论内容（必传）
     */
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容长度不能超过500个字符")
    private String content;

    /**
     * 被回复用户ID（仅回复评论时必填）
     */
    private String replyUserId;

    /**
     * 被回复用户昵称（仅回复评论时必填）
     */
    private String replyUserNickname;

    /**
     * 是否匿名评论（0-否，1-是）
     */
    @NotNull(message = "是否匿名评论不能为空")
    private Integer isAnonymous;
}