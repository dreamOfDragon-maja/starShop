package com.starshop.controller.user;

import com.starshop.common.annotation.business.SaveCartRedisCacheToMysqlAnnotation;
import com.starshop.pojo.dto.CartProductDTO;
import com.starshop.result.Result;
import com.starshop.service.CartService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
