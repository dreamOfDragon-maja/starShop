package com.starshop.controller.admin;

import com.starshop.pojo.dto.CategoryDTO;
import com.starshop.result.Result;
import com.starshop.service.CategoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Slf4j
public class CategoryController {
    //TODO 完成分类接口

    @Resource
    private CategoryService categoryService;
    /**
     * 新增分类
     * @param categoryDTO
     * @return
     */
    @PostMapping("/admin/category/add")
    public Result addCategory(@RequestBody CategoryDTO categoryDTO) {
        log.info("新增分类{}", categoryDTO);
        return categoryService.addCategory(categoryDTO);
    }
}
