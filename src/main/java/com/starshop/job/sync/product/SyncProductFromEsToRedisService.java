package com.starshop.job.sync.product;

public interface SyncProductFromEsToRedisService {

    /**
     * 同步商品最大id缓存
     */
    void syncMaxProductIdCache();
}
