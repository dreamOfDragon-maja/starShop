package com.starshop.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.starshop.constant.DatePatternConstants;
import com.starshop.pojo.enums.OrderStatusEnum;
import com.starshop.pojo.enums.PayTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderWithItemVO {
    private String orderNo;
    private String userId;
    private String addressId;
    private String totalGoodsAmount;
    private String totalAmount;
    private String freight;
    private String remark;
    private OrderStatusEnum status;
    private PayTypeEnum payType;
    @JsonFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    private LocalDateTime payTime;
    @JsonFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    private LocalDateTime createTime;
    @JsonFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    private LocalDateTime updateTime;
    private List<OrderItemVO> orderItems;
    private OrderAddressVO address;
}