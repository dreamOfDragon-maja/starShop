package com.starshop.pojo.vo;

import com.starshop.common.utils.BusinessTimeFormatUtils;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductSecondCommentVO {
    private Long id;

    private String productId;

    private String userId;

    private String userNickname;

    private String userAvatar;

    private Integer isBuyer;

    private Integer isAnonymous;

    private String content;

    private String imageUrls;

    private String likeCount;

    private boolean like;

    private String replyUserId;

    private String replyUserNickname;

    private LocalDateTime createTime;

    private String createTimeBusinessText;


    public String getCreateTimeBusinessText() {
        return BusinessTimeFormatUtils.formatTimeDiffWithNow(createTime);
    }
}