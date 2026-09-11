package com.starshop.controller.admin;

import com.starshop.common.result.CursorCommonEntity;
import com.starshop.common.result.CursorCommonResult;
import com.starshop.common.result.SimpleCursorCommonResult;
import com.starshop.infrastructure.es.document.ProductDocument;
import com.starshop.infrastructure.es.mapstruct.EsCopyMapper;
import com.starshop.pojo.entity.Product;
import com.starshop.pojo.vo.SimpleProductVO;
import com.starshop.result.Result;
import com.starshop.service.ProductSearchKeywordService;
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

    private final EsCopyMapper esCopyMapper;

    private final ProductSearchKeywordService productSearchKeywordService;


    /**
     * 获取热门商品
     * @return
     */
    @GetMapping("/product/hot")
    public Result<List<SimpleProductVO>> getHotProduct(@RequestParam(name = "limit", defaultValue = "10") Integer limit){
        List<ProductDocument> hotProduct = productService.getHotProduct(limit);
        List<SimpleProductVO> simpleProductVOS = hotProduct.stream().map(esCopyMapper::ProductDocumentToSimpleProductVO).toList();
        return Result.success(simpleProductVOS);
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

    /**
     * 获取相关商品
     * @param productName 商品名
     * @param limit 查询数量
     * @return
     */
    @GetMapping("/product/related")
    public Result<List<SimpleProductVO>> getProductRelated(@RequestParam("productName") String productName,
                                                          @RequestParam(value = "limit" , defaultValue = "10") Integer limit ){
        List<ProductDocument> productRelated = productService.getProductRelated(productName,limit);
        List<SimpleProductVO> simpleProductVOS = productRelated.stream().map(esCopyMapper::ProductDocumentToSimpleProductVO).toList();
        return Result.success(simpleProductVOS);
    }


    /**
     * 查询商品规格价格
     * @param productId 商品id
     * @param specId 规格id
     * @return
     */
    @GetMapping("/product/spec/price")
    public Result<?> getProductSpecPrice(@RequestParam String productId,@RequestParam String specId){
        return productService.getProductSpecPrice(productId,specId);
    }


    /**
     * 滚动查询商品列表
     * @param beginId
     * @param querySize
     * @return
     */
    @GetMapping("/product/scroll/query/list")
    public Result<SimpleCursorCommonResult> getSimpleProductByScrollQuery(Long beginId,@RequestParam(defaultValue = "80") Integer querySize){
        SimpleCursorCommonResult simpleProductByScrollQuery = productService.getSimpleProductByScrollQuery(beginId,querySize);
        return Result.success(simpleProductByScrollQuery);
    }

    /**
     * 用户获取热门搜索关键词列表
     * @return
     */
    @GetMapping("/product/user/keyword/list")
    public Result<?> getProductSearchKeywordListUser() {
        return productSearchKeywordService.getProductSearchKeywordListUser();
    }

    /**
     * 管理员获取搜索关键词列表
     * @return
     */
    @GetMapping("/product/admin/keyword/list")
    public Result<?> getProductSearchKeywordListAdmin() {
        return productSearchKeywordService.getProductSearchKeywordListAdmin();
    }

}