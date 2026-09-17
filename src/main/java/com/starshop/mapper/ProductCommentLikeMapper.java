package com.starshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starshop.pojo.entity.ProductCommentLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductCommentLikeMapper extends BaseMapper<ProductCommentLike> {

    int batchUpdate (@Param("list") List<ProductCommentLike> productCommentLikeList);

}
