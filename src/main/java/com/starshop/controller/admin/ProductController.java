package com.starshop.controller.admin;

import com.starshop.common.result.CursorCommonEntity;
import com.starshop.common.result.CursorCommonResult;
import com.starshop.infrastructure.es.document.ProductDocument;
import com.starshop.pojo.vo.SimpleProductVO;
import com.starshop.result.Result;
import com.starshop.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;


    /**
     * 获取热门商品
     * @return
     */
    @GetMapping("/product/hot")
    public Result<List<ProductDocument>> getHotProduct(@RequestParam(name = "limit", defaultValue = "10") Integer limit){
         List<ProductDocument> hotProduct = productService.getHotProduct(limit);
         return Result.success(hotProduct);
    }

    /**
     * 根据商品id获取商品简单介绍（支持批量，逗号分隔，为空则返回空）
     * @return
     */
    @GetMapping("/product/brief/list")
    public Result<List<SimpleProductVO>> getBriefProduct(@RequestParam(value = "productIds",defaultValue = "") String productIds){
        return productService.getBriefProduct(productIds);
    }

    /**
     * 获取商品详细信息
     * @param productId
     * @param userId
     * @return
     */
    @GetMapping("/product/detail")
    public Result<?> getProductDetail(@RequestParam("productId") String productId ,
                                      @RequestHeader(value = "X-User-Id", required = false) String userId){
        return productService.getProductDetail(productId,userId);
    }

    /**
     * 分类游标查询指定分类下的简单商品列表
     * @param cursorCommonEntity
     * @param categoryId
     * @param isFirstCategoryId
     * @return
     */
    @GetMapping("/product/category/list")
    public Result<CursorCommonResult> getCategorySimpleProduct(@Valid CursorCommonEntity cursorCommonEntity,
                                                               Long categoryId, boolean isFirstCategoryId){
        CursorCommonResult cursorCommonResult = productService.getCategorySimpleProduct(cursorCommonEntity,categoryId,isFirstCategoryId);
        return Result.success(cursorCommonResult);
    }

    /**
     * 关键词游标分类搜索商品,默认排序方式为default(销量)
     * @param cursorCommonEntity
     * @param keyword
     * @return
     */
    @GetMapping("/product/search")
    public Result<CursorCommonResult> searchProductList(@Valid CursorCommonEntity cursorCommonEntity ,String keyword){
        CursorCommonResult cursorCommonResult = productService.searchProductList(cursorCommonEntity,keyword);
        return Result.success(cursorCommonResult);
    }

}
