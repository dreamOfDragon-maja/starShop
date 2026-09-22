package com.starshop.pojo.dto;

import com.starshop.pojo.enums.CommonStatus;
import lombok.Data;

@Data
public class NoticeDTO {

    private String id;
    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;


    private CommonStatus status;
}
