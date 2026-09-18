package com.starshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starshop.pojo.entity.Cart;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartMapper extends BaseMapper<Cart> {
}
