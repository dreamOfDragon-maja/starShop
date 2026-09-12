package com.starshop.pojo.vo;

import com.starshop.common.utils.BusinessTimeFormatUtils;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductAppendCommentVO {

    private String id;

    private String userId;

    private String content;

    private String imageUrls;

    private LocalDateTime createTime;

    private String createTimeBusinessText;

    public String getCreateTimeBusinessText() {
        return BusinessTimeFormatUtils.formatTimeDiffWithNow(createTime);
    }

}
