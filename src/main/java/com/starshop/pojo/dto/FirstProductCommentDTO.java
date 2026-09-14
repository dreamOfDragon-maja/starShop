package com.starshop.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 商品一级评论提交 DTO
 */
@Data
public class FirstProductCommentDTO {

    @NotBlank(message = "商品 ID不能为空")
    private String productId;

    @NotBlank(message = "商品规格 ID不能为空")
    private String productSpecId;

    @NotBlank(message = "商品规格文本不能为空")
    private String productSpecText;

    @NotBlank(message = "订单单号不能为空")
    private String orderNo;

    @NotBlank(message = "用户昵称不能为空")
    private String userNickname;

    @NotBlank(message = "用户头像不能为空")
    private String userAvatar;

    @NotBlank(message = "评论内容不能为空")
    private String content;

    private String imageUrls;

    private int rating;

    private int isAnonymous = 0;
}