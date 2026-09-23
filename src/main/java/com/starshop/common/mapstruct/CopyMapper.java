package com.starshop.common.mapstruct;

import com.starshop.common.result.UserInfo;
import com.starshop.infrastructure.es.document.ProductDocument;
import com.starshop.pojo.dto.*;
import com.starshop.pojo.entity.*;
import com.starshop.pojo.vo.*;
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

    @Mapping(source = "id" , target = "id")
    SimpleProductVO productToSimpleProductVO(Product product);

    ProductSpecVO productSpecToProductSpecVO(ProductSpec productSpec);

    ProductFirstCommentVO productCommentToProductFirstCommentVO(ProductComment productComment);

    ProductSecondCommentVO productCommentToProductSecondCommentVO(ProductComment productComment);

    ProductAppendCommentVO productCommentAppendToProductCommentAppendVO(ProductCommentAppend productCommentAppend);

    ProductComment firstProductCommentDTOToProductComment(FirstProductCommentDTO firstProductCommentDTO);

    ProductComment secondProductCommentDTOToProductComment(SecondProductCommentDTO secondProductCommentDTO);

    ProductCommentAppend appendProductFirstCommentDTOToProductCommentAppend(AppendProductFirstCommentDTO appendProductFirstCommentDTO);

    CartItem cartProductDTOToCartItem(CartProductDTO cartProductDTO);

    Address addressDTOToAddress(AddressDTO addressDTO);

    Coupon couponCreateDTOToCoupon(CouponCreateDTO couponCreateDTO);

    FactoryInfoVO factoryInfoToFactoryInfoVO(FactoryInfo factoryInfo);

    Notice noticeDTOToNotice(NoticeDTO noticeDTO);

    Banner bannerDTOToBanner(BannerDTO bannerDTO);

    UserDetailVO sysUserToUserDetailVO(SysUser sysUser);

}