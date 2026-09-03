package com.starshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.mapper.ProductMapper;
import com.starshop.pojo.entity.Product;
import com.starshop.pojo.entity.ProductDocument;
import com.starshop.properties.RedisCacheCountProperties;
import com.starshop.service.ProductRedisCacheService;
import com.starshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final ProductRedisCacheService productRedisCacheService;

    private final RedisCacheCountProperties redisCacheCountProperties;

    /**
     * 获取热门商品
     * 按照销量进行排名
     * @return
     */
    @Override
    public List<ProductDocument> getHotProduct(Integer limit) {
        if (limit > redisCacheCountProperties.getHotProductCacheSize()) {
            throw new RuntimeException("超出热门商品最大缓存数量");
        }
        //从redis里取出热门商品及其id
        List<ProductDocument> hotProductList = productRedisCacheService.getHotProduct();
        List<Long> hotProductIdList = productRedisCacheService.getHotProductIdList();
        //TODO 这里后续优化
        if (hotProductList == null) {
            hotProductList = Collections.emptyList();
        }
        if (hotProductIdList == null) {
            hotProductIdList = Collections.emptyList();
        }
        //热门商品本身无序，利用idList进行排序
        HashMap<Long,ProductDocument> map = new HashMap<>(hotProductList.size());
        for (ProductDocument productDocument : hotProductList) {
            map.put(productDocument.getId(),productDocument);
        }
        //创建一个arraylist来存放最终结果
        List<ProductDocument> productDocumentResultList = new ArrayList<>(hotProductIdList.size());
        for (Long id : hotProductIdList) {
            ProductDocument productDocument = map.get(id);
            productDocumentResultList.add(productDocument);
        }
        return productDocumentResultList.stream().limit(limit).toList();
    }
}
