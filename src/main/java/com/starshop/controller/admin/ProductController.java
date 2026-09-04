package com.starshop.controller.admin;

import com.starshop.pojo.entity.ProductDocument;
import com.starshop.pojo.vo.SimpleProductVO;
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

    /**
     * 根据商品id获取商品简单介绍（支持批量，逗号分隔，为空则返回空）
     * @return
     */
    @GetMapping("/product/brief/list")
    public Result<List<SimpleProductVO>> getBriefProduct(@RequestParam(value = "productIds",defaultValue = "") String productIds){
        return productService.getBriefProduct(productIds);
    }
}
