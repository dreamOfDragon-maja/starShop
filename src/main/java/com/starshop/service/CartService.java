package com.starshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starshop.pojo.entity.Cart;

public interface CartService extends IService<Cart> {

    /**
     * 将 Redis 缓存中的购物车同步到 MySQL
     * @param userId 用户ID
     */
    void syncCartToMysql(String userId);

}
