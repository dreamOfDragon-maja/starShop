package com.starshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starshop.pojo.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    List<Product> getBriefProduct(@Param("productIdsList") List<Long> productIdsList);

    Product selectByProductId(String productId, String userId);

    List<Product> getProductDetailByProductIdSet(@Param("productIdSet") Set<Long> productIdSet);
}
