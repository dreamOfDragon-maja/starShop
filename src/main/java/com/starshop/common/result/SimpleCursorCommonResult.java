package com.starshop.common.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 简单游标查询通用返回类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimpleCursorCommonResult {

    private SimpleCursorCommonEntity simpleCursorCommonEntity;

    private List<?> list;

    private Boolean isEnd = false;
}
