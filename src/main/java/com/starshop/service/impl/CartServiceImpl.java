package com.starshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.common.annotation.business.SaveCartRedisCacheToMysqlAnnotation;
import com.starshop.constant.MessageConstant;
import com.starshop.constant.RedisKeyConstant;
import com.starshop.context.BaseContext;
import com.starshop.infrastructure.redis.connect.RedisConnector;
import com.starshop.mapper.CartMapper;
import com.starshop.pojo.dto.CartProductDTO;
import com.starshop.pojo.entity.Cart;
import com.starshop.pojo.entity.CartItem;
import com.starshop.pojo.entity.Product;
import com.starshop.pojo.entity.ProductSpec;
import com.starshop.pojo.enums.CommonStatus;
import com.starshop.properties.RedisCacheTtlProperties;
import com.starshop.result.Result;
import com.starshop.service.CartService;
import com.starshop.service.ProductService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    @Resource
    private CartMapper cartMapper;

    @Resource
    private ProductService productService;

    @Resource
    private RedisCacheTtlProperties redisCacheTtlProperties;

    private static final String DELETE_IDS = "deletedIds";
    private static final String SUCCESS_COUNT = "successCount";

    /**
     * 添加商品到购物车
     * @return
     */
    @Override
    @SaveCartRedisCacheToMysqlAnnotation
    public Result addProductToCart(CartProductDTO cartProductDTO) {
        String userId = BaseContext.getUserId();
        String productId = cartProductDTO.getProductId();
        Integer quantity = cartProductDTO.getQuantity();
        String specId = cartProductDTO.getSpecId();
        //先查询缓存
        String cartKey = RedisKeyConstant.PREFIX_CART + RedisKeyConstant.USER + userId;
        String cartHashKey = RedisKeyConstant.PRODUCT + productId + "," + RedisKeyConstant.PRODUCT_SPEC + specId;
        Map<String, Object> redisCacheMap = RedisConnector.opsForHash().entries(cartKey);
        if (redisCacheMap.isEmpty()) {
            //查询数据库更新缓存
            List<CartItem> cartList = cartMapper.getCartList(userId);
            //用户购物车信息为空
            if (Objects.isNull(cartList)||cartList.isEmpty()) {
                //设置基础信息
                CartItem cartItem = CartItem.builder().productId(productId).userId(userId).specId(specId).quantity(quantity).build();
                //根据productId查询商品信息
                Map<Long, Product> productDetailMap = productService.getProductDetailByProductIdSet(Set.of(Long.valueOf(productId)));
                //设置购物车所需基本信息（商品描述，价格等）
                cartItem = replenishCartItem(productDetailMap,cartItem);

                if (Objects.isNull(cartItem)) {
                    return Result.error(MessageConstant.DATA_ERROR);
                }
                //写入缓存
                RedisConnector.opsForHash().put(cartKey, cartHashKey, cartItem);
                RedisConnector.expire(cartKey, redisCacheTtlProperties.getCartTtl(), TimeUnit.SECONDS);
                return Result.success();

            }
            //用户购物车信息不为空
            boolean isFind = false;
            for (CartItem cartItem : cartList) {
                if (StringUtils.equals(productId,cartItem.getProductId()) && StringUtils.equals(specId,cartItem.getSpecId())) {
                    isFind = true;
                    cartItem.setQuantity(cartItem.getQuantity() + quantity);
                }
            }
            //找不到匹配的商品
            if (!isFind) {
                CartItem cartItem = CartItem.builder().userId(userId).productId(productId).specId(specId).quantity(quantity).build();
                cartList.add(cartItem);
            }
            //把list保存到redis
            saveCartListToRedis(cartList, cartKey);
            return Result.success();
        }
        //缓存命中
        CartItem cartItem;
        if (redisCacheMap.containsKey(cartHashKey)) {
            cartItem = (CartItem) redisCacheMap.get(cartHashKey);
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            //key不存在
            cartItem = CartItem.builder().userId(userId).productId(productId)
                    .specId(specId).quantity(quantity).build();
            Map<Long, Product> productDetailMap = productService.getProductDetailByProductIdSet(Set.of(Long.valueOf(productId)));
            cartItem = replenishCartItem(productDetailMap, cartItem);

        }
        //更新缓存
        redisCacheMap.put(cartHashKey, cartItem);
        RedisConnector.opsForHash().putAll(cartKey, redisCacheMap);
        RedisConnector.expire(cartKey, redisCacheTtlProperties.getCartTtl(), TimeUnit.SECONDS);
        return Result.success();
    }

    /**
     * 将购物车信息保存到redis
     * @param cartList
     * @param cartKey
     */
    private void saveCartListToRedis(List<CartItem> cartList, String cartKey) {
        HashMap<String, Object> resultMap = new HashMap<>(cartList.size());
        Set<Long> productIdSet = cartList.stream().map(CartItem::getProductId).map(Long::valueOf).collect(Collectors.toSet());
        Map<Long, Product> productDetailMap = productService.getProductDetailByProductIdSet(productIdSet);
        for (CartItem cartItem : cartList) {
            String productId = cartItem.getProductId();
            String specId = cartItem.getSpecId();
            String cartHashKey = RedisKeyConstant.PRODUCT + productId + "," + RedisKeyConstant.PRODUCT_SPEC + specId;
            cartItem = replenishCartItem(productDetailMap, cartItem);
            resultMap.put(cartHashKey, cartItem);
        }
        RedisConnector.opsForHash().putAll(cartKey, resultMap);
        RedisConnector.expire(cartKey, redisCacheTtlProperties.getCartTtl(), TimeUnit.SECONDS);
    }

    /**
     * 由商品map设置购物车商品信息
     * @param productDetailMap
     * @param cartItem
     * @return
     */
    private CartItem replenishCartItem(Map<Long, Product> productDetailMap, CartItem cartItem) {
        if (Objects.isNull(productDetailMap) || Objects.isNull(cartItem)) {
            return null;

        }
        Product product = productDetailMap.get(Long.valueOf(cartItem.getProductId()));
        List<ProductSpec> specList = product.getSpecList();
        ProductSpec productSpec = null;
        for (ProductSpec productSpecTemp : specList) {
            if (StringUtils.equals(productSpecTemp.getId().toString(), cartItem.getSpecId())) {
                productSpec = productSpecTemp;
            }
        }
        if (Objects.isNull(productSpec)) {
            return null;

        }
        cartItem.setPrice(productSpec.getPrice()).setStock(productSpec.getStock())
                .setProductName(product.getName()).setProductImage(product.getImage())
                .setSpecText(productSpec.getSpecText());
        return cartItem;
    }

    /**
     * 获取购物车列表
     * @return
     */
    @Override
    public Result getCartList() {
        String userId = BaseContext.getUserId();
        //查询redis
        String cartKey = RedisKeyConstant.PREFIX_CART + RedisKeyConstant.USER + userId;
        //构造空对象HashKey
        String emptyCartHashKey = RedisKeyConstant.PRODUCT + "0" + "," + RedisKeyConstant.PRODUCT_SPEC + "0";
        Map<String, Object> cartMap = RedisConnector.opsForHash().entries(cartKey);
        //初始化
        List<CartItem> cartList;
        if (cartMap.isEmpty()) {
            //查数据库
            cartList = cartMapper.getCartList(userId);
            if (cartList.isEmpty()) {
                //缓存空对象
                RedisConnector.opsForHash().put(cartKey,emptyCartHashKey,"");
                return Result.success(Collections.emptyList());
            }
            //写入缓存
            HashMap<String, CartItem> resultMap = new HashMap<>(cartList.size());
            for (CartItem cartItem : cartList) {
                String productId = cartItem.getProductId();
                String specId = cartItem.getSpecId();
                String cartHashKey = RedisKeyConstant.PRODUCT + productId + "," + RedisKeyConstant.PRODUCT_SPEC + specId;
                resultMap.put(cartHashKey,cartItem);
            }
            RedisConnector.opsForHash().putAll(cartKey,resultMap);
            RedisConnector.expire(cartKey, redisCacheTtlProperties.getCartTtl(), TimeUnit.SECONDS);
            return Result.success(cartList);
        }
        //过滤空对象
        if (cartMap.containsKey(emptyCartHashKey)) {
            return Result.success(Collections.emptyList());
        }
        //处理返回数据
        cartList = cartMap.values().stream().map(object -> (CartItem) object).toList();
        return Result.success(cartList);
    }

    /**
     * 清空购物车
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result clearCart() {
        String userId = BaseContext.getUserId();
        List<Cart> removeCarts = lambdaQuery().eq(Cart::getUserId, userId).list();
        if (removeCarts.isEmpty()) {
            return Result.success(removeCarts);
        }
        List<Long> removeCartIds = removeCarts.stream().map(Cart::getId).toList();
        LambdaQueryWrapper<Cart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Cart::getUserId,userId);
        int deletedRowsCount = cartMapper.delete(lambdaQueryWrapper);
        Map<String, Object> map = new HashMap<>(2);
        map.put(DELETE_IDS, removeCartIds);
        map.put(SUCCESS_COUNT, deletedRowsCount);
        return Result.success(map);
    }

    /**
     * 批量删除购物车商品(单个+批量)
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteCartProduct(String productIds, String specIds) {
        if (StringUtils.isBlank(productIds) || StringUtils.isBlank(specIds)) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        String userId = BaseContext.getUserId();
        //将传入的ids处理成list
        List<String> productIdsList = Arrays.stream(productIds.split(",")).toList();
        List<String> specIdsList = Arrays.stream(specIds.split(",")).toList();
        if (productIdsList.isEmpty() && specIdsList.isEmpty()) {
            return Result.error(MessageConstant.CART_NOT_EXIST_ERROR);
        }
        if (productIdsList.size() != specIdsList.size()) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        //构造查询条件
        LambdaQueryWrapper<Cart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        for (int i = 0; i < productIdsList.size(); i++) {
            String productId = productIdsList.get(i);
            String specId = specIdsList.get(i);
            lambdaQueryWrapper.or(wrapper ->
                    wrapper.eq(Cart::getUserId, userId).eq(Cart::getProductId, productId).eq(Cart::getSpecId, specId));
        }
        //查询要删除的购物车
        List<Cart> carts = list(lambdaQueryWrapper);
        if (CollectionUtils.isEmpty(carts)) {
            return Result.error(MessageConstant.CART_NOT_EXIST_ERROR);
        }
        if (carts.size() != productIdsList.size()) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        //删除数据
        boolean removeIsSuccess = remove(lambdaQueryWrapper);
        if (!removeIsSuccess) {
            return Result.error(MessageConstant.DELETE_ERROR);
        }
        HashMap<String, Object> map = new HashMap<>(2);
        map.put(DELETE_IDS, productIdsList);
        map.put(SUCCESS_COUNT, productIdsList.size());
        return Result.success(map);
    }

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
