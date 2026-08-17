package com.starshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.common.utils.CaffeineUtils;
import com.starshop.constant.DataConstant;
import com.starshop.constant.RedisKeyConstant;
import com.starshop.infrastructure.redis.connect.RedisConnector;
import com.starshop.mapper.CategoryMapper;
import com.starshop.pojo.emums.CommonStatus;
import com.starshop.pojo.entity.Category;
import com.starshop.service.CategoryService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {


    @Resource
    private CaffeineUtils caffeineUtils;
    /**
     * loadingCache 存储分类树的缓存
     * @return
     */
    public @Nullable List<Category> getCategoryTreeCache() {
        //查询所有的分类树
        List<Category> allCategories = lambdaQuery().eq(Category::getStatus, CommonStatus.ACTIVE.getNumber())
                .orderByAsc(Category::getSort).list();
        //在所有分类树中查找父级分类树
        List<Category> rootCategories = allCategories.stream().filter(category ->
                        category.getParentId().equals(DataConstant.ZERO_LONG))
                          .collect(Collectors.toList());
        buildCategoryTree(allCategories, rootCategories);

        return rootCategories;
    }

    /**
     * 递归 为分类树 set List<Category> children
     *
     * @param allCategories
     * @param parentCategories
     */
    private void buildCategoryTree(List<Category> allCategories, List<Category> parentCategories) {
        //递归结束条件
        if (CollectionUtils.isEmpty(allCategories) || CollectionUtils.isEmpty(parentCategories)) {
            return;
        }
        //根据父级分类树分组
        Map<Long, List<Category>> groupMap = allCategories.stream().collect(Collectors.groupingBy(Category::getParentId));
        //创建arrayList用于存储下一次调用的父级分类树
        List<Category> nextCategoriesList = new ArrayList<>();
        //获取孩童分类树,同时处理下一次调用的父级分类树
        parentCategories.forEach(category -> {
            List<Category> childrenCategories = groupMap.getOrDefault(category.getParentId(), new ArrayList<>());
            category.setChildren(childrenCategories);
            nextCategoriesList.addAll(childrenCategories);
        });
        buildCategoryTree(allCategories,nextCategoriesList);
    }

    /**
     * 更新 categoryTreeId 在 Redis 里的缓存
     */
    @Override
    public void updateCategoryTreeRedisCache() {
        //key = category: + tree
        String key = RedisKeyConstant.PREFIX_CATEGORY + RedisKeyConstant.TREE;
        //先根据key删除原有数据
        RedisConnector.delete(key);
        //调用工具类获取分类树
        List<Category> categoryTree = caffeineUtils.getCategoryTree();
        //更新策略opsforhash
        //hashkey =firstCategory: + firstCategoryId;
        HashMap<String, Object> map = new HashMap<>(categoryTree.size());
        for (Category category : categoryTree) {
            Long firstCategoryId = category.getId();
            String hashkey =RedisKeyConstant.FIRST_CATEGORY+ firstCategoryId;
            List<Long> secondCategoryId = category.getChildren().stream().map(Category::getId).collect(Collectors.toList());
            map.put(hashkey,secondCategoryId);
        }

        RedisConnector.opsForHash().putAll(key,map);
    }
}
