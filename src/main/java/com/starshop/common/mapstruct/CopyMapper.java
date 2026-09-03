package com.starshop.common.mapstruct;

import com.starshop.common.result.UserInfo;
import com.starshop.pojo.dto.CategoryDTO;
import com.starshop.pojo.dto.UserUpdateDTO;
import com.starshop.pojo.entity.Category;
import com.starshop.pojo.entity.Product;
import com.starshop.pojo.entity.ProductDocument;
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

    Category categoryDTOToCategory(CategoryDTO categoryDTO);

    /**
     * Product → ProductDocument
     */
    @Mapping(target = "status", expression = "java(product.getStatus() != null ? product.getStatus().getNumber() : 1)")
    ProductDocument productToDocument(Product product);
}
