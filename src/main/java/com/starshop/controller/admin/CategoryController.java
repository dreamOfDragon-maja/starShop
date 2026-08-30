package com.starshop.controller.admin;

import com.starshop.pojo.dto.CategoryDTO;
import com.starshop.result.Result;
import com.starshop.service.CategoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Slf4j
public class CategoryController {

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

    /**
     * 删除分类
     * @return
     */
    @DeleteMapping("/admin/category/{categoryId}")
    public Result deleteCategory(@PathVariable String categoryId){
        log.info("删除分类{}",categoryId);
        return categoryService.deleteCategory(categoryId);
    }

    /**
     * 获取分类树
     * @return
     */
    @GetMapping("/category/tree")
    public Result getCategoryTree(){
        log.info("获取分类树");
        return categoryService.getCategoryTree();
    }

    /**
     * 更新分类
     * @param id
     * @param categoryDTO
     * @return
     */
    @PutMapping("/admin/category/{id}")
    public Result updateCategoryInfo(@PathVariable String id, @RequestBody CategoryDTO categoryDTO){
        log.info("更新分类，{}，{}",id,categoryDTO);
        return categoryService.updateCategoryInfo(id,categoryDTO);
    }


    /**
     * 更新分类状态
     * @param id
     * @param status
     * @return
     */
    @PutMapping("/admin/category/{id}/status")
    public Result updateCategoryStatus(@PathVariable String id ,@RequestParam String status){
        log.info("更新分类状态,{},{}",id,status);
        return categoryService.updateCategoryStatus(id,status);
    }


}
