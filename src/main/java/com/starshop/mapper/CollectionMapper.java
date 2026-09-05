package com.starshop.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starshop.pojo.entity.ProductCollection;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CollectionMapper extends BaseMapper<ProductCollection> {
}
