package com.starshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starshop.pojo.entity.ProductComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface ProductCommentMapper extends BaseMapper<ProductComment> {

    int updateProductCommentLikeCount(@Param("map") Map<Long,Integer> dataMap);

}
