package com.starshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starshop.pojo.dto.CategoryDTO;
import com.starshop.pojo.entity.Category;
import com.starshop.result.Result;

public interface CategoryService extends IService<Category> {

    /**
     * 更新 categoryTreeId 在 Redis 里的缓存
     */
    void updateCategoryTreeRedisCache();

    /**
     * 新增分类
     * @param categoryDTO
     * @return
     */
    Result addCategory(CategoryDTO categoryDTO);

    /**
     * 删除分类
     * @param categoryId
     * @return
     */
    Result deleteCategory(String categoryId);

    /**
     * 获取分类树
     * @return
     */
    Result getCategoryTree();

    /**
     * 更新分类
     * @param id
     * @param categoryDTO
     * @return
     */
    Result updateCategoryInfo(String id, CategoryDTO categoryDTO);

    /**
     * 更新分类状态
     * @param id
     * @param status
     * @return
     */
    Result updateCategoryStatus(String id, String status);
}
