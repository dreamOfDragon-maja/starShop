package com.starshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starshop.pojo.entity.Product;
import com.starshop.pojo.entity.ProductDocument;
import com.starshop.pojo.vo.SimpleProductVO;
import com.starshop.result.Result;

import java.util.List;

public interface ProductService extends IService<Product> {

    /**
     * 获取热门商品
     * @return
     */
    List<ProductDocument> getHotProduct(Integer limit);

    /**
     * 获取商品简单信息
     * @param productIds
     * @return
     */
    Result<List<SimpleProductVO>> getBriefProduct(String productIds);

    /**
     * 获取商品详细信息
     * @param productId
     * @param userId
     * @return
     */
    Result<?> getProductDetail(String productId, String userId);
}
