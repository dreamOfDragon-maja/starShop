package com.starshop.common.mapstruct;

import com.starshop.common.result.UserInfo;
import com.starshop.pojo.dto.UserUpdateDTO;
import com.starshop.pojo.entity.SysUser;
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
    UserInfo usertoUserInfo(SysUser sysUser);

    /**
     * 使用 UserUpdateDTO 更新 User（忽略 null 值字段）
     * @param dto 更新数据
     * @param sysUser 目标实体
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDTO(UserUpdateDTO dto, @MappingTarget SysUser sysUser);
}
