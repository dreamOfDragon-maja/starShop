package com.starshop.common.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 游标查询通用返回类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CursorCommonResult {
    //通用游标返回实体
    private CursorCommonEntity cursorCommonEntity;

    //查询结果列表
    private List<?> list;

    //是否查完
    private Boolean isEnd = false;
}
