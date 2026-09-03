package com.starshop.controller.admin;

import com.starshop.pojo.entity.ProductDocument;
import com.starshop.result.Result;
import com.starshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
