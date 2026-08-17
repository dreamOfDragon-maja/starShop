package com.starshop.common.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.starshop.constant.CaffeineConstant;
import com.starshop.pojo.entity.Category;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public  class CaffeineUtils {

    @Resource
    private LoadingCache<String, List<String>> hotProductSearchKeywordCache;

    @Resource
    private LoadingCache<String, List<Category>> categoryTreeCache;

    @Resource(name = "MaxAndMinProductIdInDataCache")
    private Cache<String, Map<String,Long>> maxAndMinProductIdInDataCache;


    /**
     * 查询热门搜索关键词
     */
    public  List<String> getHotProductSearchKeyword() {
        return hotProductSearchKeywordCache.get(CaffeineConstant.CACHE_KEY_HOT_PRODUCT_SEARCH_KEYWORD);
    }

    /**
     * 清除热门搜索关键词
     */
    public void invalidateHotProductSearchKeywordCache() {
        hotProductSearchKeywordCache.invalidate(CaffeineConstant.CACHE_KEY_HOT_PRODUCT_SEARCH_KEYWORD);
    }

    /**
     * 查询分类树
     */
    public List<Category> getCategoryTree() {
        return categoryTreeCache.get(CaffeineConstant.CACHE_KEY_CATEGORY_TREE);
    }

    /**
     * 删除分类树
     */
    public void invalidateCategoryTree() {
        categoryTreeCache.invalidate(CaffeineConstant.CACHE_KEY_CATEGORY_TREE);
    }




}
