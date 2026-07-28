package com.starshop.common.mapstruct;

import com.starshop.pojo.entity.User;
import com.starshop.pojo.vo.UserVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
/**
 * 创建mapstruct转换器
 */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface CopyMapper {

    /**
     * User转UserVO
     */
    UserVO usertoUserVO(User user);
}
