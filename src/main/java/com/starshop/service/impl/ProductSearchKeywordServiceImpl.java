package com.starshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.mapper.ProductSearchKeywordMapper;
import com.starshop.pojo.emums.CommonStatus;
import com.starshop.pojo.entity.ProductSearchKeyword;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProductSearchKeywordServiceImpl extends ServiceImpl<ProductSearchKeywordMapper, ProductSearchKeyword> {

    /**
     * 用户获取热门商品搜索关键词
     * @param key
     * @return
     */
    public @Nullable List<String> getHotProductSearchKeywordListUser(String key) {
        //在数据库中查询（条件满足为展示商品和为热门商品）
        List<ProductSearchKeyword> list = lambdaQuery().eq(ProductSearchKeyword::getIsShow, CommonStatus.ACTIVE.getNumber())
                .eq(ProductSearchKeyword::getIsHot, CommonStatus.ACTIVE.getNumber()).list();
        if(Objects.isNull(list)){
            return Collections.emptyList();
        }
        return list.stream().map(ProductSearchKeyword::getKeyword).collect(Collectors.toList());
    }
}
