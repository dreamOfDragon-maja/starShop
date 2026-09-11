package com.starshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starshop.pojo.entity.ProductSearchKeyword;
import com.starshop.result.Result;

import java.util.List;

public interface ProductSearchKeywordService extends IService<ProductSearchKeyword> {
    /**
     * 用户获取热门搜索关键词列表
     * @return
     */
    Result<?> getProductSearchKeywordListUser();

    /**
     * 管理员获取搜索关键词列表
     * @return
     */
    Result<?> getProductSearchKeywordListAdmin();

    /**
     * 管理员修改搜索关键词
     * @param productSearchKeywordList
     * @return
     */
    Result<?> updateProductSearchListAdmin(List<ProductSearchKeyword> productSearchKeywordList);
}
