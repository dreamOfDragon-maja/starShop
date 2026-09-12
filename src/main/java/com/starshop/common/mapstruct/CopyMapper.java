package com.starshop.common.mapstruct;

import com.starshop.common.result.UserInfo;
import com.starshop.infrastructure.es.document.ProductDocument;
import com.starshop.pojo.dto.CategoryDTO;
import com.starshop.pojo.dto.UserUpdateDTO;
import com.starshop.pojo.entity.*;
import com.starshop.pojo.vo.ProductFirstCommentVO;
import com.starshop.pojo.vo.ProductSecondCommentVO;
import com.starshop.pojo.vo.ProductSpecVO;
import com.starshop.pojo.vo.SimpleProductVO;
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

    @Mapping(source = "id" , target = "id")
    SimpleProductVO productToSimpleProductVO(Product product);

    ProductSpecVO productSpecToProductSpecVO(ProductSpec productSpec);

    ProductFirstCommentVO productCommentToProductFirstCommentVO(ProductComment productComment);

    ProductSecondCommentVO productCommentToProductSecondCommentVO(ProductComment productComment);
}
