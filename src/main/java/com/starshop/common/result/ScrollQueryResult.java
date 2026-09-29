package com.starshop.common.result;

import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
@Builder
public class ScrollQueryResult {

    private long  endId;

    private List<?> list;
}
