package com.starshop.common.mapstruct;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
/**
 * 创建mapstruct转换器
 */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface CopyMapper {

}
