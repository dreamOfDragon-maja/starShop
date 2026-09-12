package com.starshop.pojo.vo;

import com.starshop.common.utils.BusinessTimeFormatUtils;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductFirstCommentVO {
    private Long id;

    private String productId;

    private String productSpecId;

    private String productSpecText;

    private String userId;

    private String userNickname;

    private String userAvatar;

    private Integer isBuyer;

    private Integer isAppendComment;

    private Integer isAnonymous;

    private Integer isGoodReview;

    private String rating;

    private String content;

    private String imageUrls;

    private String likeCount;

    private boolean like;

    private LocalDateTime createTime;

    private String createTimeBusinessText;


    public String getCreateTimeBusinessText() {
        return BusinessTimeFormatUtils.formatTimeDiffWithNow(createTime);
    }
}