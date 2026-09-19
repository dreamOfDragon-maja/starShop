package com.starshop.controller.user;

import com.starshop.common.annotation.business.SaveCartRedisCacheToMysqlAnnotation;
import com.starshop.pojo.dto.CartDTO;
import com.starshop.pojo.dto.CartProductDTO;
import com.starshop.result.Result;
import com.starshop.service.CartService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Resource
    private CartService cartService;

    /**
     * 添加商品到购物车
     * @param cartProductDTO
     * @return
     */
    @PostMapping("/add")
    @SaveCartRedisCacheToMysqlAnnotation
    public Result addProductToCart(@RequestBody CartProductDTO cartProductDTO){
        return cartService.addProductToCart(cartProductDTO);
    }

    /**
     * 获取购物车列表
     * @return
     */
    @GetMapping("/list")
    public Result getCartList(){
        return cartService.getCartList();
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clear")
    @SaveCartRedisCacheToMysqlAnnotation
    public Result clearCart() {
        return cartService.clearCart();
    }

    /**
     * 批量删除购物车商品(单个+批量)
     * @return
     */
    @DeleteMapping("/products")
    @SaveCartRedisCacheToMysqlAnnotation
    public Result deleteCartProduct(@RequestParam("productIds") String productIds, @RequestParam("specIds") String specIds) {
        return cartService.deleteCartProduct(productIds, specIds);
    }

    /**
     * 将前端的购物车数据(List)更新到redis->延迟队列更新mysql
     * @param cartDTO
     * @return
     */
    @PutMapping("/update")
    @SaveCartRedisCacheToMysqlAnnotation
    public Result mergeCart(@RequestBody CartDTO cartDTO) {
        return cartService.mergeCart(cartDTO);
    }

}
