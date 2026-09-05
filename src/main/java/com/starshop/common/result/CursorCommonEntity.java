package com.starshop.common.result;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CursorCommonEntity {

    //查询种类
    @NotNull
    private String sortType;
    //末尾查询值
    private String sortValue;
    //末尾查询 id
    private Long sortId;
    //查询数量
    private Integer querySize = 20;
}
