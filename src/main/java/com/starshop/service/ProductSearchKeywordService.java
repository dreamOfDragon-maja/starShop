package com.starshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starshop.pojo.entity.ProductSearchKeyword;
import com.starshop.result.Result;

public interface ProductSearchKeywordService extends IService<ProductSearchKeyword> {
    /**
     * 用户获取热门搜索关键词列表
     * @return
     */
    Result<?> getProductSearchKeywordListUser();

}
