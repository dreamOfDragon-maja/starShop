package com.starshop.infrastructure.redis.task;

import com.starshop.service.ProductRedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 热门商品缓存定时刷新任务
 * <p>
 * 策略：每 5 分钟从 DB 按销量 Top N 写入 Redis 双缓存
 * 首次启动时会立即执行一次（initialDelay = 5秒）
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HotProductCacheTask {

    private final ProductRedisCacheService productRedisCacheService;

    /**
     * 定时刷新热门商品缓存
     * - 首次启动延迟 5 秒执行
     * - 之后每 5 分钟执行一次
     */
    @Scheduled(initialDelay = 0, fixedDelay = 5 * 60 * 1000)
    public void refreshHotProductCache() {
        try {
            productRedisCacheService.refreshHotProductCache();
        } catch (Exception e) {
            log.error("热门商品缓存定时刷新异常", e);
        }
    }
}