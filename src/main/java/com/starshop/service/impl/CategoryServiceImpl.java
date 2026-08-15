package com.starshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.constant.DataConstant;
import com.starshop.mapper.CategoryMapper;
import com.starshop.pojo.emums.CommonStatus;
import com.starshop.pojo.entity.Category;
import org.apache.commons.collections.CollectionUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> {

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
}
