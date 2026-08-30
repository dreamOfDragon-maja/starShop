package com.starshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.starshop.common.annotation.UpdateCategoryTreeRedisCacheAnnotation;
import com.starshop.common.mapstruct.CopyMapper;
import com.starshop.common.utils.CaffeineUtils;
import com.starshop.constant.CaffeineConstant;
import com.starshop.constant.DataConstant;
import com.starshop.constant.MessageConstant;
import com.starshop.constant.RedisKeyConstant;
import com.starshop.infrastructure.redis.connect.RedisConnector;
import com.starshop.mapper.CategoryMapper;
import com.starshop.pojo.dto.CategoryDTO;
import com.starshop.pojo.emums.CommonStatus;
import com.starshop.pojo.entity.Category;
import com.starshop.result.Result;
import com.starshop.service.CategoryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.smartcardio.CommandAPDU;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Resource
    private CaffeineUtils caffeineUtils;
    @Resource
    private Cache<String, Map<Long, Category>> categoryMapCache;
    @Resource
    private Cache<String, List<Category>> categoryTreeCache;
    @Resource
    private CopyMapper copyMapper;


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
        //获取子分类树,同时处理下一次调用的父级分类树
        parentCategories.forEach(category -> {
            List<Category> childrenCategories = groupMap.getOrDefault(category.getId(), new ArrayList<>());
            category.setChildren(childrenCategories);
            nextCategoriesList.addAll(childrenCategories);
        });
        buildCategoryTree(allCategories,nextCategoriesList);
    }
    /**
     * 统一刷新分类缓存（只查一次库！）
     */
    public void refreshCategoryCache() {
        // 1. 只查一次数据库
        List<Category> allCategories = lambdaQuery()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSort)
                .list();

        // 2. 生成扁平 Map（利用 Stream 瞬间完成）
        Map<Long, Category> flatMap = allCategories.stream()
                .collect(Collectors.toMap(Category::getId, Function.identity(), (v1, v2) -> v1));

        // 3. 生成树形结构
        List<Category> rootCategories = allCategories.stream()
                .filter(c -> c.getParentId().equals(DataConstant.ZERO_LONG))
                .toList();
        buildCategoryTree(allCategories, rootCategories); // 复用你原来地递归方法

        // 4. 同时写入两个缓存（没有循环依赖，没有二次查库）
        categoryTreeCache.put(CaffeineConstant.CACHE_KEY_CATEGORY_TREE, rootCategories);
        categoryMapCache.put(CaffeineConstant.CACHE_KEY_CATEGORY_MAP, flatMap);
    }
    /**
     * 清除树缓存
     */
    public void invalidateCache() {
        //清除缓存
        categoryTreeCache.invalidate(CaffeineConstant.CACHE_KEY_CATEGORY_TREE);
        categoryMapCache.invalidate(CaffeineConstant.CACHE_KEY_CATEGORY_MAP);
    }

    /**
     * 获取子节点
     * @param id
     * @return
     */
    private Category getCategoryChildren(Long id) {
        Map<Long, Category> categoryMap = categoryMapCache.getIfPresent(CaffeineConstant.CACHE_KEY_CATEGORY_MAP);
        if (CollectionUtils.isEmpty(categoryMap)) {
            refreshCategoryCache();
            categoryMap = categoryMapCache.getIfPresent(CaffeineConstant.CACHE_KEY_CATEGORY_MAP);
        }
        Category category = categoryMap.get(id);
        return category;
    }

    /**
     * 更新分类状态
     * @param id
     * @param status
     * @return
     */
    @Override
    @UpdateCategoryTreeRedisCacheAnnotation
    public Result updateCategoryStatus(String id, String status) {
        //根据lambdaUpdate更新状态
        boolean isSuccess = lambdaUpdate().eq(Category::getId, id).set(Category::getStatus, status).update();
        if (!isSuccess) {
            return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);
        }
        Map<String, Object> map = new HashMap<>(2);
        map.put(Category.Fields.id,id);
        map.put(Category.Fields.status, CommonStatus.getValueByNumber(Integer.valueOf(status)));
        invalidateCache();
        return Result.success(map);
    }

    /**
     * 更新分类
     * @param id
     * @param categoryDTO
     * @return
     */
    @Override
    @UpdateCategoryTreeRedisCacheAnnotation
    public Result updateCategoryInfo(String id, CategoryDTO categoryDTO) {
        Category category = copyMapper.categoryDTOToCategory(categoryDTO);
        category.setId(Long.valueOf(id));
        boolean isSuccess = updateById(category);
        if (!isSuccess) {
            return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);
        }
        //清除缓存
        invalidateCache();
        return Result.success(category);
    }

    /**
     * 获取分类树
     * @return
     */
    @Override
    public Result getCategoryTree() {
        //TODO这里为什么前端的返回结果中没有数据只显示操作成功
        //获取分类缓存
        List<Category> categoryTree = caffeineUtils.getCategoryTree();
        return Result.success(categoryTree);
    }

    /**
     * 删除分类
     * @param categoryId
     * @return
     */
    @Override
    @UpdateCategoryTreeRedisCacheAnnotation
    public Result deleteCategory(String categoryId) {
        boolean isSuccess = removeById(Long.valueOf(categoryId));
        if (!isSuccess) {
            return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);
        }
        invalidateCache();
        return Result.success();
    }

    /**
     * 新增分类
     * @param categoryDTO
     * @return
     */
    @Override
    @UpdateCategoryTreeRedisCacheAnnotation
    public Result addCategory(CategoryDTO categoryDTO) {
        Category category = copyMapper.categoryDTOToCategory(categoryDTO);
        boolean isSuccess = save(category);
        if (!isSuccess) {
            return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);
        }
        //清理树缓存
        invalidateCache();
        //返回新增节点的子节点（或者返回成功提示）
        Category savedCategory = getCategoryChildren(category.getId());
        return Result.success(savedCategory);
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
