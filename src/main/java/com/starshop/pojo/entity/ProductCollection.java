package com.starshop.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.starshop.constant.DatePatternConstants;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品收藏表 实体类
 * @TableName collection
 */
@Data
@TableName(value = "collection")
@Accessors(chain = true)
public class ProductCollection implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 收藏ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联用户ID（外键）
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 关联商品ID（外键）
     */
    @TableField(value = "product_id")
    private Long productId;

    /**
     * 收藏时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @DateTimeFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    @JsonFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    private LocalDateTime createTime;


}