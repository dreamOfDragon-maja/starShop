package com.starshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.constant.RedisKeyConstant;
import com.starshop.infrastructure.redis.connect.RedisConnector;
import com.starshop.mapper.CartMapper;
import com.starshop.pojo.entity.Cart;
import com.starshop.pojo.entity.CartItem;
import com.starshop.pojo.enums.CommonStatus;
import com.starshop.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    /**
     * 将 Redis 缓存中的购物车同步到 MySQL
     * @param userId 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncCartToMysql(String userId) {
        //查询缓存
        String cartKey = RedisKeyConstant.PREFIX_CART + RedisKeyConstant.USER + userId;
        Map<String, Object> cartMap = RedisConnector.opsForHash().entries(cartKey);
        //根据userId删除数据库信息
        lambdaUpdate().eq(Cart::getUserId, userId).remove();
        if (cartMap.isEmpty()) {
            return;
        }
        //转成Cart对象
        List<Cart> cartList = cartMap.values().stream()
                .map(obj -> (CartItem) obj)
                .map(item -> Cart.builder()
                        .userId(Long.valueOf(userId))
                        .productId(Long.valueOf(item.getProductId()))
                        .specId(Long.valueOf(item.getSpecId()))
                        .quantity(item.getQuantity())
                        .checked(CommonStatus.INACTIVE.getNumber())
                        .build())
                .toList();
        //批量新增
        saveBatch(cartList);
    }
}
