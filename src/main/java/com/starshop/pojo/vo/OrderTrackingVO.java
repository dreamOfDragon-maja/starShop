package com.starshop.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.starshop.constant.DatePatternConstants;
import com.starshop.pojo.enums.OrderTrackingStatusEnum;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class OrderTrackingVO {

    private OrderTrackingStatusEnum logisticsStatus;

    private String location;

    private String description;

    @DateTimeFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    @JsonFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    private LocalDateTime createTime;
}
