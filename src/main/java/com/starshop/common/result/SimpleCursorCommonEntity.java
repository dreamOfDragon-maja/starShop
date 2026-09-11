package com.starshop.common.result;

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
public class SimpleCursorCommonEntity {

    private String sortValue;

    private Long sortId;

    private Integer querySize;


    public Integer getQuerySize() {
        return querySize == null ? 20 : querySize;
    }
}
