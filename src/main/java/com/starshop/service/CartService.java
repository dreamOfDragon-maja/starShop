package com.starshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starshop.pojo.dto.CartDTO;
import com.starshop.pojo.dto.CartProductDTO;
import com.starshop.pojo.entity.Cart;
import com.starshop.result.Result;

public interface CartService extends IService<Cart> {

    /**
     * 将 Redis 缓存中的购物车同步到 MySQL
     * @param userId 用户ID
     */
    void syncCartToMysql(String userId);

    /**
     * 添加商品到购物车
     * @return
     */
    Result addProductToCart(CartProductDTO cartProductDTO);

    /**
     * 获取购物车列表
     * @return
     */
    Result getCartList();

    /**
     * 清空购物车
     * @return
     */
    Result clearCart();

    /**
     * 批量删除购物车商品(单个+批量)
     * @return
     */
    Result deleteCartProduct(String productIds, String specIds);

    /**
     * 将前端的购物车数据(List)更新到redis->延迟队列更新mysql
     * @param cartDTO
     * @return
     */
    Result mergeCart(CartDTO cartDTO);
}
