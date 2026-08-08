package com.starshop.common.mapstruct;

import com.starshop.pojo.dto.UserUpdateDTO;
import com.starshop.pojo.entity.User;
import com.starshop.pojo.vo.UserVO;
import org.mapstruct.*;

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

    /**
     * 使用 UserUpdateDTO 更新 User（忽略 null 值字段）
     * @param dto 更新数据
     * @param user 目标实体
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDTO(UserUpdateDTO dto, @MappingTarget User user);
}
