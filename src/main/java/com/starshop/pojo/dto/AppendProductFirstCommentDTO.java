package com.starshop.pojo.dto;

import lombok.Data;

@Data
public class AppendProductFirstCommentDTO {

    private String productId;

    private String orderNo;

    private String content;

    private String imageUrls;
}