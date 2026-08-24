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


}
