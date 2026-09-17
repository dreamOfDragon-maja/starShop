package com.starshop.job.sync.product.impl;

import com.starshop.job.sync.product.SyncProductFromEsToRedisService;
import com.starshop.service.ProductRedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SyncProductFromEsToRedisServiceImpl implements SyncProductFromEsToRedisService {

    private final ProductRedisCacheService productRedisCacheService;

    /**
     * 从es同步最大商品id到redis缓存
     */
    @Override
    public void syncMaxProductIdCache() {
        productRedisCacheService.initMaxProductId();
    }
}
