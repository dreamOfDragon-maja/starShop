package com.starshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.common.utils.CaffeineUtils;
import com.starshop.mapper.ProductSearchKeywordMapper;
import com.starshop.pojo.enums.CommonStatus;
import com.starshop.pojo.entity.ProductSearchKeyword;
import com.starshop.result.Result;
import com.starshop.service.ProductSearchKeywordService;
import jakarta.annotation.Resource;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProductSearchKeywordServiceImpl extends ServiceImpl<ProductSearchKeywordMapper, ProductSearchKeyword> implements ProductSearchKeywordService {

    @Resource
    private CaffeineUtils caffeineUtils;

    /**
     * 管理员获取搜索关键词列表
     * @return
     */
    @Override
    public Result<?> getProductSearchKeywordListAdmin() {
        List<ProductSearchKeyword> resultList = lambdaQuery().list();
        return Result.success(resultList);
    }

    /**
     * 用户获取热门搜索关键词列表
     * @return
     */
    @Override
    public Result<?> getProductSearchKeywordListUser() {
        //caffeine查询缓存
        List<String> hotProductSearchKeyword = caffeineUtils.getHotProductSearchKeyword();
        //打乱顺序
        Collections.shuffle(hotProductSearchKeyword);
        //查询5条数据
        List<String> resultList = hotProductSearchKeyword.stream().limit(5).toList();
        return Result.success(resultList);
    }
}
